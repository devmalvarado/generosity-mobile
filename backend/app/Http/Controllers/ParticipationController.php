<?php

namespace App\Http\Controllers;

use App\Exceptions\ApiProblem;
use App\Models\Center;
use App\Models\Contribution;
use App\Services\FeatureFlags;
use App\Services\Mutation;
use App\Services\ParticipationRecords;
use Illuminate\Http\Request;
use Illuminate\Support\Carbon;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;

class ParticipationController extends Controller
{
    public function createCampaign(Request $request)
    {
        $data = $request->validate(['type' => 'required|in:reforestation,cleanup,meals,assessment']);

        return Mutation::run($request, $data, fn () => null, function () use ($request) {
            FeatureFlags::require('campaigns.publish', $request->user()->id);
            if (config('generosity.campaign_organizer_min_points') === null) {
                ParticipationRecords::unavailable('organizer_unavailable', 'organizer_threshold_unconfigured');
            }
            ParticipationRecords::unavailable('organizer_unavailable', 'organizer_evidence_unavailable');
        });
    }

    public function publishOccurrence(Request $request, Center $center)
    {
        $data = $request->validate($this->occurrenceRules() + [
            'id' => 'required|regex:/^[a-z0-9][a-z0-9-]{0,99}$/',
            'address' => 'prohibited', 'household' => 'prohibited', 'payment' => 'prohibited',
        ]);

        return Mutation::run($request, $data, fn () => Mutation::staff($request->user()->id, $center->id), function () use ($request, $center, $data) {
            FeatureFlags::require('events.publish', $request->user()->id);
            if (DB::table('event_occurrences')->where('id', $data['id'])->exists()) {
                throw new ApiProblem('already_exists', 409);
            }
            $now = now();
            DB::table('event_occurrences')->insert([
                'id' => $data['id'], 'owner_type' => 'center', 'center_id' => $center->id, 'status' => 'published',
                'title' => ParticipationRecords::encode($data['title']), 'description' => ParticipationRecords::encode($data['description']),
                'activity' => $data['activity'], 'starts_at' => $data['startsAt'], 'ends_at' => $data['endsAt'],
                'timezone' => $data['timezone'], 'venue' => $data['venue'], 'capacity' => $data['capacity'],
                'reserved' => 0, 'version' => 1, 'created_at' => $now, 'updated_at' => $now,
            ]);

            return response()->json($this->occurrenceRow($data['id']), 201);
        });
    }

    public function updateOccurrence(Request $request, Center $center, string $occurrence)
    {
        $data = $request->validate($this->occurrenceRules() + ['version' => 'required|integer|min:1', 'address' => 'prohibited']);

        return Mutation::run($request, $data, fn () => Mutation::staff($request->user()->id, $center->id), function () use ($center, $occurrence, $data) {
            $current = $this->lockOccurrence($occurrence, $center->id);
            $this->assertVersion($current, $data['version']);
            $moved = ! Carbon::parse($current->starts_at)->equalTo(Carbon::parse($data['startsAt']))
                || ! Carbon::parse($current->ends_at)->equalTo(Carbon::parse($data['endsAt']))
                || $current->timezone !== $data['timezone'] || $current->venue !== $data['venue'];
            if ($current->reserved > 0 && $moved) {
                throw new ApiProblem('replacement_required', 409);
            }
            if ($data['capacity'] < $current->reserved) {
                throw new ApiProblem('capacity_below_commitments', 409, ['reserved' => $current->reserved]);
            }
            DB::table('event_occurrences')->where('id', $current->id)->update([
                'title' => ParticipationRecords::encode($data['title']), 'description' => ParticipationRecords::encode($data['description']),
                'activity' => $data['activity'], 'starts_at' => $data['startsAt'], 'ends_at' => $data['endsAt'],
                'timezone' => $data['timezone'], 'venue' => $data['venue'], 'capacity' => $data['capacity'],
                'version' => $current->version + 1, 'updated_at' => now(),
            ]);

            return response()->json($this->occurrenceRow($current->id));
        });
    }

    public function cancelOccurrence(Request $request, Center $center, string $occurrence)
    {
        $data = $request->validate(['reason' => 'required|string|max:500', 'version' => 'required|integer|min:1']);

        return Mutation::run($request, $data, fn () => Mutation::staff($request->user()->id, $center->id), function () use ($center, $occurrence, $data) {
            $current = $this->lockOccurrence($occurrence, $center->id);
            $this->assertVersion($current, $data['version']);
            if ($current->status !== 'cancelled') {
                DB::table('event_occurrences')->where('id', $current->id)->update([
                    'status' => 'cancelled', 'cancel_reason' => $data['reason'], 'version' => $current->version + 1, 'updated_at' => now(),
                ]);
            }

            return response()->json(['id' => $current->id, 'status' => 'cancelled', 'noticesSent' => false, 'reason' => 'inbox_requires_need']);
        });
    }

    public function enroll(Request $request, string $occurrence)
    {
        $data = $request->validate(['payment' => 'prohibited', 'method' => 'prohibited']);

        return Mutation::run($request, $data, fn () => null, function () use ($request, $occurrence) {
            FeatureFlags::require('events.enroll', $request->user()->id);

            return $this->takeSeat($occurrence, $request->user()->id, null, $request->user()->id);
        });
    }

    public function enrollAssisted(Request $request, string $occurrence)
    {
        $data = $request->validate(['subjectId' => 'required|uuid', 'payment' => 'prohibited', 'email' => 'prohibited']);

        return Mutation::run($request, $data, function () use ($request, $occurrence, $data) {
            $event = $this->lockOccurrence($occurrence);
            abort_unless($event->center_id, 403);
            Mutation::staff($request->user()->id, $event->center_id);
            $subject = DB::table('assisted_subjects')->where('id', $data['subjectId'])->where('center_id', $event->center_id)->where('status', 'active')->first();
            abort_unless($subject, 404);
        }, function () use ($request, $occurrence, $data) {
            FeatureFlags::require('assistance.record', $request->user()->id);

            return $this->takeSeat($occurrence, null, $data['subjectId'], $request->user()->id);
        });
    }

    public function cancelEnrollment(Request $request, string $enrollment)
    {
        $data = $request->validate(['version' => 'required|integer|min:1']);
        $event = $row = null;

        return Mutation::run($request, $data, function () use ($request, $enrollment, &$event, &$row) {
            $peek = DB::table('event_enrollments')->where('id', $enrollment)->first() ?? abort(404);
            $event = $this->lockOccurrence($peek->occurrence_id);
            $row = DB::table('event_enrollments')->where('id', $enrollment)->lockForUpdate()->first() ?? abort(404);
            $allowed = $row->user_id === $request->user()->id || ($row->assisted_subject_id && $row->actor_user_id === $request->user()->id);
            abort_unless($allowed, 403);
            if ($row->assisted_subject_id) {
                Mutation::staff($request->user()->id, $event->center_id);
            }
        }, function () use ($data, &$event, &$row) {
            if ($row->status === 'cancelled') {
                return response()->json($this->enrollment($row));
            }
            $this->assertVersion($row, $data['version']);
            if (now()->gte(Carbon::parse($event->starts_at))) {
                throw new ApiProblem('cancellation_closed', 409);
            }
            DB::table('event_enrollments')->where('id', $row->id)->update(['status' => 'cancelled', 'version' => $row->version + 1, 'updated_at' => now()]);
            DB::table('event_occurrences')->where('id', $event->id)->where('reserved', '>=', 1)->update([
                'reserved' => DB::raw('reserved - 1'), 'version' => DB::raw('version + 1'), 'updated_at' => now(),
            ]);

            return response()->json($this->enrollment(DB::table('event_enrollments')->where('id', $row->id)->first()));
        });
    }

    public function attendance(Request $request, string $occurrence)
    {
        $data = $request->validate(['enrollmentId' => 'required|uuid', 'actualMinutes' => 'required|integer|min:0|max:525600', 'version' => 'required|integer|min:1']);
        $event = $row = null;

        return Mutation::run($request, $data, function () use ($request, $occurrence, $data, &$event, &$row) {
            $event = $this->lockOccurrence($occurrence);
            abort_unless($event->center_id, 403);
            Mutation::staff($request->user()->id, $event->center_id);
            if ($event->campaign_id) {
                $organizer = DB::table('campaigns')->where('id', $event->campaign_id)->value('organizer_user_id');
                abort_if((int) $organizer === $request->user()->id, 403);
            }
            $row = DB::table('event_enrollments')->where('id', $data['enrollmentId'])->where('occurrence_id', $event->id)->lockForUpdate()->first() ?? abort(404);
            abort_if($row->user_id === $request->user()->id, 403);
        }, function () use ($request, $data, &$event, &$row) {
            FeatureFlags::require('events.validate', $request->user()->id);
            abort_unless($event->status === 'published', 409);
            if ($row->contribution_id) {
                return response()->json(['enrollmentId' => $row->id, 'contributionId' => $row->contribution_id, 'validated' => true]);
            }
            $this->assertVersion($row, $data['version']);
            abort_unless($row->status === 'enrolled', 409);
            if (now()->lt(Carbon::parse($event->ends_at))) {
                throw new ApiProblem('event_open', 409);
            }
            $minutes = Carbon::parse($event->starts_at)->diffInMinutes(Carbon::parse($event->ends_at));
            if ($data['actualMinutes'] > $minutes) {
                throw new ApiProblem('invalid_duration', 422);
            }
            if ($row->user_id === null) {
                DB::table('event_enrollments')->where('id', $row->id)->update(['status' => 'attended', 'version' => $row->version + 1, 'updated_at' => now()]);

                return response()->json(['enrollmentId' => $row->id, 'contributionId' => null, 'validated' => false, 'award' => false]);
            }
            $contribution = Contribution::create([
                'id' => (string) Str::uuid(), 'user_id' => $row->user_id, 'center_id' => $event->center_id,
                'method' => 'time', 'minutes' => $data['actualMinutes'], 'actual_minutes' => $data['actualMinutes'],
                'activity' => $event->activity, 'provenance' => 'center_validated', 'status' => 'fulfilled',
                'title' => json_decode($event->title, true, 512, JSON_THROW_ON_ERROR),
            ]);
            DB::table('event_enrollments')->where('id', $row->id)->update([
                'status' => 'validated', 'contribution_id' => $contribution->id, 'version' => $row->version + 1, 'updated_at' => now(),
            ]);

            return response()->json(['enrollmentId' => $row->id, 'contributionId' => $contribution->id, 'validated' => true, 'award' => false], 201);
        });
    }

    public function enrollments(Request $request)
    {
        $request->validate(['page' => 'sometimes|integer|min:1|max:100000']);
        $subjects = DB::table('assisted_subjects')->where('claimed_user_id', $request->user()->id)->pluck('id');

        return DB::table('event_enrollments')->where(function ($query) use ($request, $subjects) {
            $query->where('user_id', $request->user()->id)->orWhereIn('assisted_subject_id', $subjects);
        })->orderByDesc('created_at')->orderBy('id')->paginate(25)->through(fn ($row) => $this->enrollment($row));
    }

    public function createMealPlan(Request $request, Center $center)
    {
        $data = $request->validate([
            'title' => 'required|array', 'title.es' => 'required_without:title.en|string|max:200', 'title.en' => 'required_without:title.es|string|max:200',
            'portionUnit' => 'required|string|max:60', 'privateDetails' => 'required|array|max:20',
            'slots' => 'required|array|min:1|max:31', 'slots.*.localDate' => 'required|date_format:Y-m-d|after:today',
            'slots.*.windowStart' => 'required|date_format:H:i', 'slots.*.windowEnd' => 'required|date_format:H:i',
            'slots.*.timezone' => 'required|timezone', 'slots.*.quantityRequired' => 'required|integer|min:1|max:100000',
            'slots.*.cutoffAt' => 'required|date|after:now', 'address' => 'prohibited',
        ]);

        return Mutation::run($request, $data, fn () => Mutation::staff($request->user()->id, $center->id), function () use ($request, $center, $data) {
            FeatureFlags::require('meals.plan', $request->user()->id);
            $plan = (string) Str::uuid();
            $now = now();
            DB::table('meal_plans')->insert([
                'id' => $plan, 'center_id' => $center->id, 'coordinator_user_id' => $request->user()->id, 'status' => 'private',
                'portion_unit' => $data['portionUnit'], 'public_title' => ParticipationRecords::encode($data['title']),
                'private_details' => json_encode($data['privateDetails'], JSON_THROW_ON_ERROR), 'version' => 1,
                'created_at' => $now, 'updated_at' => $now,
            ]);
            $slots = [];
            foreach ($data['slots'] as $slot) {
                $id = (string) Str::uuid();
                DB::table('meal_slots')->insert([
                    'id' => $id, 'plan_id' => $plan, 'local_date' => $slot['localDate'], 'window_start' => $slot['windowStart'],
                    'window_end' => $slot['windowEnd'], 'timezone' => $slot['timezone'], 'quantity_required' => $slot['quantityRequired'],
                    'cutoff_at' => $slot['cutoffAt'], 'version' => 1, 'created_at' => $now, 'updated_at' => $now,
                ]);
                $slots[] = $id;
            }

            return response()->json(['id' => $plan, 'status' => 'private', 'slots' => $slots], 201);
        });
    }

    public function approveMealPlan(Request $request, string $plan)
    {
        $data = $request->validate(['method' => 'required|in:recipient,representative', 'version' => 'required|integer|min:1']);
        $row = null;

        return Mutation::run($request, $data, function () use ($request, $plan, &$row) {
            $row = DB::table('meal_plans')->where('id', $plan)->lockForUpdate()->first() ?? abort(404);
            Mutation::staff($request->user()->id, $row->center_id);
        }, function () use ($plan, $data, &$row) {
            $this->assertVersion($row, $data['version']);
            if (! in_array($row->status, ['private', 'approved'], true)) {
                throw new ApiProblem('invalid_transition', 409);
            }
            if ($row->status === 'private') {
                DB::table('meal_plans')->where('id', $plan)->update([
                    'status' => 'approved', 'approval_method' => $data['method'], 'approved_at' => now(),
                    'version' => $row->version + 1, 'updated_at' => now(),
                ]);
            }
            $open = config('generosity.meal_withdrawal_channel') !== '';

            return response()->json(['id' => $plan, 'status' => 'approved', 'commitmentsAvailable' => $open, 'accountCreated' => false]);
        });
    }

    public function withdrawMealPlan(Request $request, string $plan)
    {
        $data = $request->validate(['version' => 'required|integer|min:1']);
        $row = null;

        return Mutation::run($request, $data, function () use ($request, $plan, &$row) {
            $row = DB::table('meal_plans')->where('id', $plan)->lockForUpdate()->first() ?? abort(404);
            Mutation::staff($request->user()->id, $row->center_id);
        }, function () use ($plan, $data, &$row) {
            if (config('generosity.meal_withdrawal_channel') === '') {
                ParticipationRecords::unavailable('withdrawal_unavailable', 'withdrawal_channel_unconfigured');
            }
            $this->assertVersion($row, $data['version']);
            DB::table('meal_slots')->where('plan_id', $plan)->orderBy('id')->lockForUpdate()->get();
            DB::table('meal_plans')->where('id', $plan)->update(['status' => 'withdrawn', 'version' => $row->version + 1, 'updated_at' => now()]);
            DB::table('meal_slots')->where('plan_id', $plan)->where('status', 'open')->update(['status' => 'cancelled', 'updated_at' => now()]);

            return response()->json(['id' => $plan, 'status' => 'withdrawn', 'noticesSent' => false]);
        });
    }

    public function commitMeal(Request $request, string $slot)
    {
        $data = $request->validate(['quantity' => 'required|integer|min:1|max:100000', 'payment' => 'prohibited', 'method' => 'prohibited']);

        return Mutation::run($request, $data, fn () => null, function () use ($request, $slot, $data) {
            FeatureFlags::require('meals.commit', $request->user()->id);
            if (config('generosity.meal_withdrawal_channel') === '') {
                ParticipationRecords::unavailable('withdrawal_unavailable', 'withdrawal_channel_unconfigured');
            }
            $peek = DB::table('meal_slots')->where('id', $slot)->first() ?? abort(404);
            $plan = DB::table('meal_plans')->where('id', $peek->plan_id)->lockForUpdate()->first();
            $current = DB::table('meal_slots')->where('id', $slot)->lockForUpdate()->first() ?? abort(404);
            abort_unless($plan && $plan->status === 'approved' && $current->status === 'open', 404);
            if (now()->gte(Carbon::parse($current->cutoff_at))) {
                throw new ApiProblem('slot_closed', 409);
            }
            $existing = DB::table('meal_commitments')->where('slot_id', $slot)->where('user_id', $request->user()->id)->first();
            if ($existing) {
                return response()->json($this->commitment($existing));
            }
            $available = $current->quantity_required - $current->quantity_committed;
            if ($data['quantity'] > $available) {
                throw new ApiProblem('capacity_exceeded', 409, ['available' => $available]);
            }
            $id = (string) Str::uuid();
            DB::table('meal_commitments')->insert([
                'id' => $id, 'slot_id' => $slot, 'user_id' => $request->user()->id, 'actor_user_id' => $request->user()->id,
                'quantity' => $data['quantity'], 'status' => 'reserved', 'version' => 1, 'created_at' => now(), 'updated_at' => now(),
            ]);
            DB::table('meal_slots')->where('id', $slot)->update([
                'quantity_committed' => $current->quantity_committed + $data['quantity'], 'version' => $current->version + 1, 'updated_at' => now(),
            ]);

            return response()->json($this->commitment(DB::table('meal_commitments')->where('id', $id)->first()), 201);
        });
    }

    public function createSubject(Request $request, Center $center)
    {
        $data = $request->validate([
            'consentMethod' => 'required|in:in_person,telephone', 'label' => 'nullable|string|max:80',
            'contact' => 'nullable|string|max:200', 'email' => 'prohibited', 'password' => 'prohibited',
        ]);

        return Mutation::run($request, $data, fn () => Mutation::staff($request->user()->id, $center->id), function () use ($request, $center, $data) {
            FeatureFlags::require('assistance.record', $request->user()->id);
            $id = (string) Str::uuid();
            DB::table('assisted_subjects')->insert([
                'id' => $id, 'center_id' => $center->id, 'coordinator_user_id' => $request->user()->id,
                'consent_method' => $data['consentMethod'], 'consent_at' => now(), 'label' => $data['label'] ?? null,
                'private_contact' => $data['contact'] ?? null, 'status' => 'active', 'version' => 1,
                'created_at' => now(), 'updated_at' => now(),
            ]);

            return response()->json(['id' => $id, 'status' => 'active', 'accountCreated' => false], 201);
        });
    }

    public function claim(Request $request)
    {
        $data = $request->validate(['subjectId' => 'required|uuid', 'name' => 'prohibited', 'email' => 'prohibited', 'phone' => 'prohibited']);

        return Mutation::run($request, $data, fn () => null, function () use ($request, $data) {
            $subject = DB::table('assisted_subjects')->where('id', $data['subjectId'])->first() ?? abort(404);
            $existing = DB::table('assisted_claims')->where('subject_id', $subject->id)->where('claimant_user_id', $request->user()->id)->first();
            if ($existing) {
                return response()->json(['id' => $existing->id, 'status' => $existing->status, 'subjectId' => $subject->id]);
            }
            $id = (string) Str::uuid();
            DB::table('assisted_claims')->insert([
                'id' => $id, 'subject_id' => $subject->id, 'claimant_user_id' => $request->user()->id,
                'status' => 'pending', 'version' => 1, 'created_at' => now(), 'updated_at' => now(),
            ]);

            return response()->json(['id' => $id, 'status' => 'pending', 'subjectId' => $subject->id], 201);
        });
    }

    public function decideClaim(Request $request, string $claim)
    {
        $data = $request->validate(['action' => 'required|in:approve,reject', 'version' => 'required|integer|min:1']);
        $row = $subject = null;

        return Mutation::run($request, $data, function () use ($request, $claim, &$row, &$subject) {
            $reviewers = ParticipationRecords::reviewers('generosity.assisted_claim_reviewer_ids');
            if ($reviewers === []) {
                ParticipationRecords::unavailable('review_unavailable', 'reviewer_ids_unconfigured');
            }
            abort_unless(in_array((string) $request->user()->id, $reviewers, true), 403);
            $row = DB::table('assisted_claims')->where('id', $claim)->lockForUpdate()->first() ?? abort(404);
            $subject = DB::table('assisted_subjects')->where('id', $row->subject_id)->lockForUpdate()->firstOrFail();
            abort_if($row->claimant_user_id === $request->user()->id || $subject->coordinator_user_id === $request->user()->id, 403);
        }, function () use ($data, &$row, &$subject) {
            $this->assertVersion($row, $data['version']);
            $status = $data['action'] === 'approve' ? 'approved' : 'rejected';
            if ($row->status !== 'pending') {
                if ($row->status !== $status) {
                    throw new ApiProblem('invalid_transition', 409);
                }

                return response()->json(['id' => $row->id, 'status' => $row->status, 'award' => false, 'historyCopied' => false]);
            }
            if ($status === 'approved' && $subject->claimed_user_id && (int) $subject->claimed_user_id !== (int) $row->claimant_user_id) {
                throw new ApiProblem('claim_conflict', 409);
            }
            if ($status === 'approved') {
                // Serialize association with self-service and assisted reservations for this account.
                DB::table('users')->where('id', $row->claimant_user_id)->lockForUpdate()->firstOrFail();
                $subjects = DB::table('assisted_subjects')->where('claimed_user_id', $row->claimant_user_id)
                    ->where('id', '!=', $subject->id)->lockForUpdate()->pluck('id');
                $occurrences = DB::table('event_enrollments')->where('assisted_subject_id', $subject->id)->lockForUpdate()->pluck('occurrence_id');
                $overlap = DB::table('event_enrollments')->whereIn('occurrence_id', $occurrences)
                    ->where(function ($query) use ($row, $subjects) {
                        $query->where('user_id', $row->claimant_user_id)->orWhereIn('assisted_subject_id', $subjects);
                    })->lockForUpdate()->first();
                if ($overlap) {
                    throw new ApiProblem('claim_conflict', 409);
                }
            }
            DB::table('assisted_claims')->where('id', $row->id)->update(['status' => $status, 'version' => $row->version + 1, 'updated_at' => now()]);
            if ($status === 'approved') {
                DB::table('assisted_subjects')->where('id', $subject->id)->update([
                    'claimed_user_id' => $row->claimant_user_id, 'version' => $subject->version + 1, 'updated_at' => now(),
                ]);
            }

            return response()->json(['id' => $row->id, 'status' => $status, 'award' => false, 'historyCopied' => false]);
        });
    }

    public function createOutcome(Request $request, Center $center)
    {
        $data = $this->outcomeData($request);

        return Mutation::run($request, $data, fn () => Mutation::staff($request->user()->id, $center->id), function () use ($request, $center, $data) {
            FeatureFlags::require('outcomes.publish', $request->user()->id);
            ParticipationRecords::assertAggregate($request->input('body'), $data['identifiesBeneficiary'], $data['publicationPermission']);
            $id = (string) Str::uuid();
            DB::table('outcome_updates')->insert($this->outcomeRow($id, $center->id, $request->user()->id, $data, 'draft'));

            return response()->json(['id' => $id, 'status' => 'draft'], 201);
        });
    }

    public function transitionOutcome(Request $request, string $outcome)
    {
        $data = $request->validate(['action' => 'required|in:publish,correct,withdraw', 'version' => 'required|integer|min:1', 'body' => 'required_if:action,correct|array']);
        $row = null;

        return Mutation::run($request, $data, function () use ($request, $outcome, &$row) {
            $row = DB::table('outcome_updates')->where('id', $outcome)->lockForUpdate()->first() ?? abort(404);
            abort_unless($row->center_id, 403);
            Mutation::staff($request->user()->id, $row->center_id);
        }, function () use ($request, $data, &$row) {
            if ($data['action'] === 'publish') {
                FeatureFlags::require('outcomes.publish', $request->user()->id);
            }
            $this->assertVersion($row, $data['version']);
            $status = match ($data['action']) {
                'publish' => $row->status === 'draft' ? 'published' : null,
                'correct' => in_array($row->status, ['published', 'corrected'], true) ? 'corrected' : null,
                'withdraw' => in_array($row->status, ['published', 'corrected'], true) ? 'withdrawn' : null,
            };
            if (! $status) {
                throw new ApiProblem('invalid_transition', 409);
            }
            $update = ['status' => $status, 'version' => $row->version + 1, 'updated_at' => now()];
            if ($data['action'] === 'correct') {
                ParticipationRecords::assertAggregate($request->input('body'), false, false);
                $update['body'] = ParticipationRecords::encode($request->input('body'));
            }
            if ($status === 'withdrawn') {
                $update['body'] = null;
                $update['requested'] = null;
                $update['committed'] = null;
                $update['reported'] = null;
                $update['validated'] = null;
            }
            DB::table('outcome_updates')->where('id', $row->id)->update($update);

            return response()->json(['id' => $row->id, 'status' => $status]);
        });
    }

    public function follow(Request $request)
    {
        $data = $request->validate([
            'targetType' => 'required|in:center,campaign', 'targetId' => 'required|string|max:100',
            'channel' => 'required|in:email,inbox,push',
        ]);

        return Mutation::run($request, $data, fn () => null, function () use ($request, $data) {
            FeatureFlags::require('follows.update', $request->user()->id);
            if ($data['channel'] === 'email') {
                $reason = config('generosity.follow_email_consent_version') === '' ? 'consent_unconfigured' : 'delivery_unconfigured';
                ParticipationRecords::unavailable('follow_unavailable', $reason);
            }
            if ($data['targetType'] === 'center') {
                abort_unless(DB::table('centers')->where('id', $data['targetId'])->exists(), 404);
            } else {
                abort_unless(DB::table('campaigns')->where('id', $data['targetId'])->where('status', 'published')->exists(), 404);
            }
            $existing = DB::table('follows')->where('user_id', $request->user()->id)->where('target_type', $data['targetType'])
                ->where('target_id', $data['targetId'])->where('channel', $data['channel'])->first();
            if ($existing && $existing->unsubscribed_at === null) {
                return response()->json($this->followRow($existing, false));
            }
            $id = $existing ? $existing->id : (string) Str::uuid();
            DB::table('follows')->updateOrInsert(['id' => $id], [
                'user_id' => $request->user()->id, 'target_type' => $data['targetType'], 'target_id' => $data['targetId'],
                'channel' => $data['channel'], 'consented_at' => now(), 'unsubscribed_at' => null,
                'created_at' => $existing->created_at ?? now(), 'updated_at' => now(),
            ]);

            return response()->json($this->followRow(DB::table('follows')->where('id', $id)->first(), false), $existing ? 200 : 201);
        });
    }

    public function unsubscribe(Request $request, string $follow)
    {
        return Mutation::run($request, [], fn () => null, function () use ($request, $follow) {
            $row = DB::table('follows')->where('id', $follow)->where('user_id', $request->user()->id)->lockForUpdate()->first() ?? abort(404);
            if ($row->unsubscribed_at === null) {
                DB::table('follows')->where('id', $row->id)->update(['unsubscribed_at' => now(), 'updated_at' => now()]);
            }

            return response()->json(['id' => $row->id, 'unsubscribed' => true, 'sent' => false]);
        });
    }

    public function follows(Request $request)
    {
        return DB::table('follows')->where('user_id', $request->user()->id)->orderBy('id')->get()->map(fn ($row) => $this->followRow($row, false))->values();
    }

    public function funnel(Request $request)
    {
        $request->validate(['from' => 'sometimes|date_format:Y-m-d', 'to' => 'sometimes|date_format:Y-m-d']);
        $operators = ParticipationRecords::reviewers('generosity.funnel_operator_ids');
        if ($operators === []) {
            ParticipationRecords::unavailable('metrics_unavailable', 'operator_ids_unconfigured');
        }
        abort_unless(in_array((string) $request->user()->id, $operators, true), 403);
        $from = $request->input('from');
        $to = $request->filled('to') ? Carbon::parse($request->input('to'))->addDay() : null;
        $visits = DB::table('visit_signals');
        $commitments = DB::table('event_enrollments')->where('status', 'enrolled');
        $meals = DB::table('meal_commitments')->where('status', 'reserved');
        $validated = DB::table('contributions')->where('provenance', 'center_validated')->where('status', 'fulfilled');
        if ($from) {
            $visits->where('occurred_at', '>=', $from);
            $commitments->where('created_at', '>=', $from);
            $meals->where('created_at', '>=', $from);
            $validated->where('created_at', '>=', $from);
        }
        if ($to) {
            $visits->where('occurred_at', '<', $to);
            $commitments->where('created_at', '<', $to);
            $meals->where('created_at', '<', $to);
            $validated->where('created_at', '<', $to);
        }

        return [
            'period' => ['from' => $request->input('from'), 'to' => $request->input('to')],
            'visits' => $visits->count(),
            'commitments' => $commitments->count(),
            'mealCommitments' => $meals->count(),
            'validatedCompletions' => $validated->count(),
            'returnParticipation' => null,
            'returnParticipationKnown' => false,
            'shareSheetOpens' => null,
            'shareSheetOpensKnown' => false,
            'definitions' => [
                'visits' => 'Authenticated center visit_signals in the period. Not opportunity opens, share-sheet opens, or delivered invitations.',
                'commitments' => 'Server-accepted event enrollments that are still enrolled. Cancelled rows are excluded. Not a completed activity.',
                'mealCommitments' => 'Reserved meal quantities still held. Not added to event commitments and not a completed delivery.',
                'validatedCompletions' => 'Distinct center_validated fulfilled contributions. Replays do not add another row.',
                'returnParticipation' => 'Not calculated. No cross-activity identity definition is configured.',
            ],
        ];
    }

    private function occurrenceRules(): array
    {
        return [
            'title' => 'required|array', 'title.es' => 'required_without:title.en|string|max:200', 'title.en' => 'required_without:title.es|string|max:200',
            'description' => 'required|array', 'description.es' => 'required_without:description.en|string|max:2000', 'description.en' => 'required_without:description.es|string|max:2000',
            'activity' => 'required|string|max:120', 'startsAt' => 'required|date|after:now', 'endsAt' => 'required|date|after:startsAt',
            'timezone' => 'required|timezone', 'venue' => 'required|string|max:200', 'capacity' => 'required|integer|min:1|max:100000',
        ];
    }

    private function takeSeat(string $occurrence, ?int $userId, ?string $subjectId, int $actorId)
    {
        $participantId = $userId;
        if ($subjectId) {
            $subject = DB::table('assisted_subjects')->where('id', $subjectId)->lockForUpdate()->firstOrFail();
            $participantId = $subject->claimed_user_id;
        }
        $subjects = [];
        if ($participantId !== null) {
            DB::table('users')->where('id', $participantId)->lockForUpdate()->firstOrFail();
            $subjects = DB::table('assisted_subjects')->where('claimed_user_id', $participantId)->lockForUpdate()->pluck('id');
        }
        $event = $this->lockOccurrence($occurrence);
        abort_unless($event->status === 'published', 404);
        abort_unless(now()->lt(Carbon::parse($event->starts_at)), 409);
        $query = DB::table('event_enrollments')->where('occurrence_id', $event->id)->lockForUpdate();
        $existing = $userId ? (clone $query)->where('user_id', $userId)->first() : (clone $query)->where('assisted_subject_id', $subjectId)->first();
        if ($participantId !== null) {
            $overlap = (clone $query)->where(function ($query) use ($participantId, $subjects) {
                $query->where('user_id', $participantId)->orWhereIn('assisted_subject_id', $subjects);
            });
            if ($existing) {
                $overlap->where('id', '!=', $existing->id);
            }
            if ($overlap->first()) {
                throw new ApiProblem('enrollment_conflict', 409);
            }
        }
        if ($existing && $existing->status !== 'cancelled') {
            return response()->json($this->enrollment($existing));
        }
        if ($event->reserved >= $event->capacity) {
            throw new ApiProblem('capacity_exceeded', 409, ['available' => 0]);
        }
        DB::table('event_occurrences')->where('id', $event->id)->update([
            'reserved' => $event->reserved + 1, 'version' => $event->version + 1, 'updated_at' => now(),
        ]);
        if ($existing) {
            DB::table('event_enrollments')->where('id', $existing->id)->update([
                'status' => 'enrolled', 'actor_user_id' => $actorId, 'version' => $existing->version + 1, 'updated_at' => now(),
            ]);

            return response()->json($this->enrollment(DB::table('event_enrollments')->where('id', $existing->id)->first()));
        }
        $id = (string) Str::uuid();
        DB::table('event_enrollments')->insert([
            'id' => $id, 'occurrence_id' => $event->id, 'user_id' => $userId, 'assisted_subject_id' => $subjectId,
            'actor_user_id' => $actorId, 'status' => 'enrolled', 'version' => 1, 'created_at' => now(), 'updated_at' => now(),
        ]);

        return response()->json($this->enrollment(DB::table('event_enrollments')->where('id', $id)->first()), 201);
    }

    private function lockOccurrence(string $id, ?string $centerId = null): object
    {
        $query = DB::table('event_occurrences')->where('id', $id);
        if ($centerId) {
            $query->where('center_id', $centerId);
        }

        return $query->lockForUpdate()->first() ?? abort(404);
    }

    private function assertVersion(object $row, int $version): void
    {
        if ((int) $row->version !== $version) {
            throw new ApiProblem('version_conflict', 409);
        }
    }

    private function occurrenceRow(string $id): array
    {
        $row = DB::table('event_occurrences')->where('id', $id)->first();

        return ['id' => $row->id, 'status' => $row->status, 'version' => $row->version, 'reserved' => $row->reserved];
    }

    private function enrollment(object $row): array
    {
        return [
            'id' => $row->id, 'occurrenceId' => $row->occurrence_id, 'status' => $row->status,
            'version' => $row->version, 'assisted' => $row->assisted_subject_id !== null,
        ];
    }

    private function commitment(object $row): array
    {
        return ['id' => $row->id, 'slotId' => $row->slot_id, 'quantity' => $row->quantity, 'status' => $row->status, 'version' => $row->version];
    }

    private function outcomeData(Request $request): array
    {
        return $request->validate([
            'body' => 'required|array', 'body.es' => 'required_without:body.en|string|max:2000', 'body.en' => 'required_without:body.es|string|max:2000',
            'identifiesBeneficiary' => 'required|boolean', 'publicationPermission' => 'required|boolean',
            'requested' => 'nullable|integer|min:0|max:1000000', 'committed' => 'nullable|integer|min:0|max:1000000',
            'reported' => 'nullable|integer|min:0|max:1000000', 'validated' => 'nullable|integer|min:0|max:1000000',
            'unit' => 'nullable|string|max:60', 'periodStart' => 'nullable|date_format:Y-m-d', 'periodEnd' => 'nullable|date_format:Y-m-d',
        ]);
    }

    private function outcomeRow(string $id, string $centerId, int $author, array $data, string $status): array
    {
        return [
            'id' => $id, 'center_id' => $centerId, 'author_user_id' => $author, 'status' => $status,
            'body' => ParticipationRecords::encode($data['body']), 'identifies_beneficiary' => $data['identifiesBeneficiary'],
            'publication_permission' => $data['publicationPermission'], 'requested' => $data['requested'] ?? null,
            'committed' => $data['committed'] ?? null, 'reported' => $data['reported'] ?? null, 'validated' => $data['validated'] ?? null,
            'unit' => $data['unit'] ?? null, 'period_start' => $data['periodStart'] ?? null, 'period_end' => $data['periodEnd'] ?? null,
            'version' => 1, 'created_at' => now(), 'updated_at' => now(),
        ];
    }

    private function followRow(object $row, bool $sent): array
    {
        return [
            'id' => $row->id, 'targetType' => $row->target_type, 'targetId' => $row->target_id, 'channel' => $row->channel,
            'unsubscribed' => $row->unsubscribed_at !== null, 'sent' => $sent,
            'deliveryConfigured' => $row->channel === 'push' ? false : null,
        ];
    }
}
