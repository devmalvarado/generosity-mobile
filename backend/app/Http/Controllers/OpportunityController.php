<?php

namespace App\Http\Controllers;

use App\Models\Need;
use App\Services\ParticipationRecords;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class OpportunityController extends Controller
{
    public function campaigns(Request $request)
    {
        $request->validate(['page' => 'sometimes|integer|min:1|max:100000']);

        return DB::table('campaigns')->where('status', 'published')->orderBy('id')->paginate(25)
            ->through(fn ($row) => $this->campaignView($row));
    }

    public function campaign(string $campaign)
    {
        return $this->campaignView($this->visible('campaigns', $campaign));
    }

    public function occurrences(Request $request)
    {
        $request->validate(['page' => 'sometimes|integer|min:1|max:100000']);

        return DB::table('event_occurrences')->where('status', 'published')->orderBy('starts_at')->orderBy('id')->paginate(25)
            ->through(fn ($row) => $this->occurrenceView($row));
    }

    public function occurrence(string $occurrence)
    {
        return $this->occurrenceView($this->visible('event_occurrences', $occurrence));
    }

    public function meals(Request $request)
    {
        $request->validate(['page' => 'sometimes|integer|min:1|max:100000']);

        return $this->publicSlots()->where('meal_slots.status', 'open')->orderBy('local_date')->orderBy('meal_slots.id')->paginate(25)
            ->through(fn ($row) => $this->mealView($row));
    }

    public function meal(string $meal)
    {
        $row = $this->publicSlots()->where('meal_slots.id', $meal)->first() ?? abort(404);

        return $this->mealView($row);
    }

    public function preview(Request $request, string $kind, string $id)
    {
        $request->validate(['channel' => 'sometimes|regex:/^[a-z0-9]{8,32}$/', 'next' => 'prohibited', 'url' => 'prohibited']);
        $record = match ($kind) {
            'need' => $this->needPreview($id),
            'center' => $this->centerPreview($id),
            'campaign' => $this->campaignView($this->visible('campaigns', $id)),
            'occurrence' => $this->occurrenceView($this->visible('event_occurrences', $id)),
            'meal' => $this->mealView($this->publicSlots()->where('meal_slots.id', $id)->first() ?? abort(404)),
            default => abort(404),
        };
        $path = '/opportunities/'.$kind.'/'.$id;

        return [
            'kind' => $kind,
            'id' => $id,
            'status' => $record['status'],
            'title' => $record['title'] ?? $record['name'] ?? null,
            'purpose' => $record['purpose'] ?? $record['description'] ?? null,
            'area' => $record['area'] ?? $record['venue'] ?? null,
            'date' => $record['startsAt'] ?? $record['localDate'] ?? $record['expiresOnIsoDate'] ?? null,
            'canonicalPath' => $path,
            'canonicalUrl' => $request->getSchemeAndHttpHost().'/api/v1'.$path,
            'generatedOn' => now()->utc()->toDateString(),
            'remainingSeatsPromise' => false,
            'qrImage' => false,
            'attributionRecorded' => false,
            'alternatives' => [],
        ];
    }

    public function progress(Need $need)
    {
        abort_unless($need->status === 'published', 404);
        $need->loadSum(['contributions as quantity_received' => fn ($query) => $query->where('method', 'goods')->where('status', 'fulfilled')], 'quantity');

        return [
            'needId' => $need->id,
            'requested' => $need->quantity_required,
            'requestedKnown' => $need->quantity_required !== null,
            'committed' => $need->quantity_committed,
            'committedKnown' => true,
            'reported' => null,
            'reportedKnown' => false,
            'validated' => (int) $need->quantity_received,
            'validatedKnown' => true,
            'unit' => $need->details['unit'] ?? null,
            'updatedAt' => $need->updated_at->toISOString(),
            'fulfilled' => false,
        ];
    }

    public function outcome(string $outcome)
    {
        $row = DB::table('outcome_updates')->where('id', $outcome)->first() ?? abort(404);
        abort_unless(in_array($row->status, ['published', 'corrected', 'withdrawn'], true), 404);
        $withdrawn = $row->status === 'withdrawn';

        return [
            'id' => $row->id,
            'status' => $row->status,
            'body' => $withdrawn ? null : ParticipationRecords::localized($row->body),
            'requested' => $withdrawn ? null : $row->requested,
            'committed' => $withdrawn ? null : $row->committed,
            'reported' => $withdrawn ? null : $row->reported,
            'validated' => $withdrawn ? null : $row->validated,
            'unit' => $withdrawn ? null : $row->unit,
            'updatedAt' => ParticipationRecords::stamp($row->updated_at),
            'downloadedCopiesRecallable' => false,
        ];
    }

    private function visible(string $table, string $id): object
    {
        $row = DB::table($table)->where('id', $id)->first() ?? abort(404);
        abort_unless(in_array($row->status, ['published', 'cancelled'], true), 404);

        return $row;
    }

    private function publicSlots()
    {
        return DB::table('meal_slots')->join('meal_plans', 'meal_plans.id', '=', 'meal_slots.plan_id')
            ->where('meal_plans.status', 'approved')
            ->select([
                'meal_slots.id', 'meal_slots.local_date', 'meal_slots.window_start', 'meal_slots.window_end',
                'meal_slots.timezone', 'meal_slots.quantity_required', 'meal_slots.quantity_committed',
                'meal_slots.status', 'meal_slots.updated_at', 'meal_plans.public_title', 'meal_plans.portion_unit',
            ]);
    }

    private function campaignView(object $row): array
    {
        $accepted = $row->partnership_status === 'accepted';

        return [
            'id' => $row->id,
            'status' => $row->status,
            'type' => $row->type,
            'title' => ParticipationRecords::localized($row->title),
            'purpose' => ParticipationRecords::localized($row->purpose),
            'area' => $row->area,
            'partnershipAccepted' => $accepted,
            'centerId' => $accepted ? $row->center_id : null,
            'alternatives' => [],
            'updatedAt' => ParticipationRecords::stamp($row->updated_at),
        ];
    }

    private function occurrenceView(object $row): array
    {
        return [
            'id' => $row->id,
            'status' => $row->status,
            'version' => $row->version,
            'title' => ParticipationRecords::localized($row->title),
            'description' => ParticipationRecords::localized($row->description),
            'activity' => $row->activity,
            'startsAt' => ParticipationRecords::stamp($row->starts_at),
            'endsAt' => ParticipationRecords::stamp($row->ends_at),
            'timezone' => $row->timezone,
            'venue' => $row->venue,
            'capacity' => $row->capacity,
            'reserved' => $row->reserved,
            'available' => max(0, $row->capacity - $row->reserved),
            'ownerType' => $row->owner_type,
            'centerId' => $row->center_id,
            'campaignId' => $row->campaign_id,
            'alternatives' => [],
            'updatedAt' => ParticipationRecords::stamp($row->updated_at),
        ];
    }

    private function mealView(object $row): array
    {
        return [
            'id' => $row->id,
            'status' => $row->status,
            'title' => ParticipationRecords::localized($row->public_title),
            'localDate' => (string) $row->local_date,
            'windowStart' => $row->window_start,
            'windowEnd' => $row->window_end,
            'timezone' => $row->timezone,
            'portionUnit' => $row->portion_unit,
            'quantityRequired' => $row->quantity_required,
            'quantityCommitted' => $row->quantity_committed,
            'available' => max(0, $row->quantity_required - $row->quantity_committed),
            'alternatives' => [],
            'updatedAt' => ParticipationRecords::stamp($row->updated_at),
        ];
    }

    private function needPreview(string $id): array
    {
        $need = Need::whereKey($id)->first() ?? abort(404);
        abort_unless($need->status === 'published', 404);

        return $need->publicData();
    }

    private function centerPreview(string $id): array
    {
        $center = DB::table('centers')->where('id', $id)->first() ?? abort(404);
        $details = json_decode($center->details, true, 512, JSON_THROW_ON_ERROR);

        return [
            'id' => $center->id,
            'status' => 'published',
            'name' => ParticipationRecords::localized($details['name'] ?? []),
            'description' => ParticipationRecords::localized($details['description'] ?? []),
        ];
    }
}
