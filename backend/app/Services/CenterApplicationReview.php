<?php

namespace App\Services;

use App\Exceptions\ApiProblem;
use App\Models\Center;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;
use Illuminate\Validation\ValidationException;

class CenterApplicationReview
{
    public static function summary(object $row): array
    {
        $details = json_decode($row->details, true, 512, JSON_THROW_ON_ERROR);
        $summary = [
            'id' => $row->id, 'organization_name' => $details['organization_name'],
            'site_name' => $details['site_name'], 'status' => $row->status,
            'version' => (int) ($row->version ?? 1), 'submitted_at' => (string) $row->created_at,
        ];
        if (isset($details['claimed_center_id'])) {
            $summary['claimed_center_id'] = $details['claimed_center_id'];
        }
        $review = self::latest($row->id, ['approve', 'request_changes', 'reject', 'withdraw']);
        if ($review && $row->status !== 'pending') {
            $summary['decision'] = [
                'action' => $review->action, 'rationale' => $review->rationale,
                'recorded_at' => (string) $review->created_at,
            ];
        }
        if ($row->status === 'approved' && $review?->action === 'approve') {
            $accepted = self::latest($row->id, ['accept']);
            $summary['center_id'] = $review->center_id;
            $summary['account_id'] = (int) $review->account_id;
            $summary['roles'] = json_decode($review->roles, true, 512, JSON_THROW_ON_ERROR);
            $summary['acceptance_required'] = $accepted === null;
            if ($accepted) {
                $summary['accepted_at'] = (string) $accepted->created_at;
            }
        }

        return $summary;
    }

    public static function show(Request $request, string $applicationId): array
    {
        self::requireReviewer($request);
        $row = DB::table('center_applications')->where('id', $applicationId)->first() ?? abort(404);
        self::rejectSelfReview($request, $row);
        $details = json_decode($row->details, true, 512, JSON_THROW_ON_ERROR);
        $claimed = $details['claimed_center_id'] ?? null;

        return ['application' => [
            'id' => $row->id, 'applicant_id' => (int) $row->user_id, 'contact_email' => $row->contact_email,
            'status' => $row->status, 'version' => (int) $row->version, 'submitted_at' => (string) $row->created_at,
            'details' => $details, 'possible_duplicate' => self::duplicateClaim($row->id, $claimed),
            'decisions' => DB::table('center_application_decisions')->where('application_id', $row->id)
                ->orderBy('position')->get()->map(fn ($decision) => self::decision($decision))->all(),
        ]];
    }

    public static function decide(Request $request, string $applicationId)
    {
        $data = $request->validate([
            'decision' => 'required|in:approve,request_changes,reject',
            'rationale' => 'required|string|max:2000',
            'version' => 'required|integer|min:1',
            'center_id' => 'prohibited_unless:decision,approve|required_if:decision,approve|string|max:100|regex:/^[a-z0-9][a-z0-9-]*$/',
            'account_id' => 'prohibited_unless:decision,approve|required_if:decision,approve|integer|min:1',
            'roles' => 'prohibited_unless:decision,approve|required_if:decision,approve|array|size:1',
            'roles.*' => 'required|in:staff|distinct',
            'verified' => 'prohibited', 'status' => 'prohibited', 'user_id' => 'prohibited',
            'operator' => 'prohibited', 'tax_eligible' => 'prohibited', 'provider_recipient' => 'prohibited',
        ]);
        $data['rationale'] = trim($data['rationale']);
        if ($data['rationale'] === '') {
            throw ValidationException::withMessages(['rationale' => 'A rationale is required.']);
        }
        if ($data['decision'] === 'approve') {
            $data['roles'] = ['staff'];
        }

        return Mutation::run($request, $data, function () use ($request, $applicationId) {
            self::requireReviewer($request);
            $row = DB::table('center_applications')->where('id', $applicationId)->first() ?? abort(404);
            self::rejectSelfReview($request, $row);
        }, function () use ($request, $data, $applicationId) {
            $row = self::lock($applicationId);
            self::rejectSelfReview($request, $row);
            self::rejectRepresentedStaff($request, $row, $data['center_id'] ?? null);
            self::assertVersion($row, (int) $data['version']);
            if (! in_array($row->status, ['pending', 'changes_requested'], true)) {
                throw new ApiProblem('invalid_transition', 409, ['current' => self::current($row)]);
            }
            $centerId = $accountId = $roles = null;
            $status = match ($data['decision']) {
                'approve' => 'approved', 'request_changes' => 'changes_requested', 'reject' => 'rejected',
            };
            if ($data['decision'] === 'approve') {
                $center = Center::whereKey($data['center_id'])->lockForUpdate()->first();
                $account = User::whereKey($data['account_id'])->whereNotNull('email_verified_at')->lockForUpdate()->first();
                if (! $center) {
                    throw new ApiProblem('center_unavailable', 409);
                }
                if (! $account) {
                    throw new ApiProblem('account_unavailable', 422);
                }
                $centerId = $center->id;
                $accountId = $account->id;
                $roles = ['staff'];
            }
            self::record($row, $request->user()->id, $data['decision'], $data['rationale'], $centerId, $accountId, $roles, $status);

            return response()->json(['application' => self::summary(self::lock($applicationId))]);
        });
    }

    public static function accept(Request $request, string $applicationId)
    {
        $data = $request->validate([
            'version' => 'required|integer|min:1',
            'verified' => 'prohibited', 'status' => 'prohibited', 'roles' => 'prohibited',
            'center_id' => 'prohibited', 'account_id' => 'prohibited', 'tax_eligible' => 'prohibited',
            'provider_recipient' => 'prohibited',
        ]);

        return Mutation::run($request, $data, fn () => null, function () use ($request, $data, $applicationId) {
            $row = self::lock($applicationId);
            $approval = self::latest($row->id, ['approve']);
            if (! $approval || (int) $approval->account_id !== (int) $request->user()->id || $row->status !== 'approved') {
                abort(404);
            }
            if (self::latest($row->id, ['accept'])) {
                return response()->json(['application' => self::summary($row)]);
            }
            self::assertVersion($row, (int) $data['version']);
            $center = Center::whereKey($approval->center_id)->lockForUpdate()->first();
            if (! $center) {
                throw new ApiProblem('center_unavailable', 409);
            }
            DB::table('center_memberships')->insertOrIgnore([
                'user_id' => $request->user()->id, 'center_id' => $center->id,
            ]);
            self::record($row, $request->user()->id, 'accept', null, $center->id, $request->user()->id, ['staff'], 'approved');

            return response()->json(['application' => self::summary(self::lock($applicationId))]);
        });
    }

    public static function withdraw(Request $request, string $applicationId)
    {
        $data = $request->validate([
            'version' => 'required|integer|min:1',
            'rationale' => 'nullable|string|max:2000',
            'status' => 'prohibited', 'verified' => 'prohibited', 'center_id' => 'prohibited',
        ]);
        $data['rationale'] = isset($data['rationale']) ? trim($data['rationale']) : null;
        if ($data['rationale'] === '') {
            $data['rationale'] = null;
        }

        return Mutation::run($request, $data, fn () => null, function () use ($request, $data, $applicationId) {
            $row = self::lock($applicationId);
            if ((int) $row->user_id !== (int) $request->user()->id) {
                abort(404);
            }
            self::assertVersion($row, (int) $data['version']);
            if (self::latest($row->id, ['accept']) || ! in_array($row->status, ['pending', 'changes_requested', 'approved'], true)) {
                throw new ApiProblem('invalid_transition', 409, ['current' => self::current($row)]);
            }
            self::record($row, $request->user()->id, 'withdraw', $data['rationale'], null, null, null, 'withdrawn');

            return response()->json(['application' => self::summary(self::lock($applicationId))]);
        });
    }

    public static function lockClaimedCenter(string $centerId): void
    {
        if (! Center::whereKey($centerId)->lockForUpdate()->exists()) {
            throw new ApiProblem('center_unavailable', 409);
        }
    }

    private static function requireReviewer(Request $request): void
    {
        $ids = array_values(array_filter(
            array_map(fn ($id) => (string) $id, config('generosity.application_reviewer_ids', [])),
            fn (string $id) => ctype_digit($id)
        ));
        if ($ids === [] || ! in_array((string) $request->user()->id, $ids, true)) {
            if ($ids === []) {
                throw new ApiProblem('review_unavailable', 503);
            }
            abort(404);
        }
    }

    private static function rejectSelfReview(Request $request, object $row): void
    {
        if ((int) $row->user_id === (int) $request->user()->id) {
            throw new ApiProblem('self_review_forbidden', 403);
        }
    }

    private static function rejectRepresentedStaff(Request $request, object $row, ?string $targetCenterId): void
    {
        $details = json_decode($row->details, true, 512, JSON_THROW_ON_ERROR);
        $ids = array_values(array_filter([$details['claimed_center_id'] ?? null, $targetCenterId]));
        if ($ids !== [] && DB::table('center_memberships')->where('user_id', $request->user()->id)->whereIn('center_id', $ids)->exists()) {
            throw new ApiProblem('represented_review_forbidden', 403);
        }
    }

    private static function assertVersion(object $row, int $version): void
    {
        if ((int) $row->version !== $version) {
            throw new ApiProblem('version_conflict', 409, ['current' => self::current($row)]);
        }
    }

    private static function lock(string $applicationId): object
    {
        return DB::table('center_applications')->where('id', $applicationId)->lockForUpdate()->first() ?? abort(404);
    }

    private static function record(object $row, int $actorId, string $action, ?string $rationale, ?string $centerId, ?int $accountId, ?array $roles, string $status): void
    {
        $position = ((int) DB::table('center_application_decisions')->where('application_id', $row->id)->max('position')) + 1;
        DB::table('center_application_decisions')->insert([
            'id' => (string) Str::uuid(), 'application_id' => $row->id, 'position' => $position, 'actor_id' => $actorId,
            'action' => $action, 'rationale' => $rationale, 'center_id' => $centerId, 'account_id' => $accountId,
            'roles' => $roles === null ? null : json_encode($roles, JSON_THROW_ON_ERROR), 'created_at' => now(),
        ]);
        DB::table('center_applications')->where('id', $row->id)->update([
            'status' => $status, 'version' => ((int) $row->version) + 1, 'updated_at' => now(),
        ]);
    }

    private static function latest(string $applicationId, array $actions): ?object
    {
        return DB::table('center_application_decisions')->where('application_id', $applicationId)
            ->whereIn('action', $actions)->orderByDesc('position')->first();
    }

    private static function duplicateClaim(string $applicationId, ?string $centerId): bool
    {
        if ($centerId === null) {
            return false;
        }

        return DB::table('center_applications')->where('id', '!=', $applicationId)
            ->where('details->claimed_center_id', $centerId)
            ->whereIn('status', ['pending', 'changes_requested', 'approved'])->exists();
    }

    private static function decision(object $row): array
    {
        return [
            'action' => $row->action, 'rationale' => $row->rationale, 'center_id' => $row->center_id,
            'account_id' => $row->account_id === null ? null : (int) $row->account_id,
            'roles' => $row->roles === null ? null : json_decode($row->roles, true, 512, JSON_THROW_ON_ERROR),
            'recorded_at' => (string) $row->created_at,
        ];
    }

    private static function current(object $row): array
    {
        return ['status' => $row->status, 'version' => (int) $row->version];
    }
}
