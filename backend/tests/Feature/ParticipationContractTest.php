<?php

namespace Tests\Feature;

use App\Models\Contribution;
use App\Models\Need;
use App\Models\User;
use App\Services\CatalogImport;
use Illuminate\Foundation\Testing\DatabaseMigrations;
use Illuminate\Support\Carbon;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;
use Tests\TestCase;

class ParticipationContractTest extends TestCase
{
    use DatabaseMigrations;

    protected function setUp(): void
    {
        parent::setUp();
        Carbon::setTestNow('2026-11-04 15:00:00');
        $this->assertSame('generosity_test', DB::connection()->getDatabaseName());
        app(CatalogImport::class)->run($this->catalog(), 'test');
    }

    protected function tearDown(): void
    {
        Carbon::setTestNow();
        parent::tearDown();
    }

    public function test_public_reads_hide_drafts_private_fields_and_do_not_infer_fulfillment(): void
    {
        [$owner] = $this->actor();
        DB::table('campaigns')->insert($this->campaignRow('draft-secret', $owner->id, 'draft', 'CANARY-DRAFT', 'CANARY-ADDRESS'));
        DB::table('campaigns')->insert($this->campaignRow('public-drive', $owner->id, 'published', 'Jornada pública', 'CANARY-PLAN', 'center-a'));
        $this->callApi('GET', 'campaigns')->assertOk()->assertJsonCount(1, 'data')->assertJsonPath('data.0.id', 'public-drive')->assertJsonPath('data.0.centerId', null);
        $hidden = $this->callApi('GET', 'campaigns/draft-secret')->assertNotFound();
        $this->assertStringNotContainsString('CANARY-DRAFT', $hidden->getContent());
        $this->assertStringNotContainsString('public-drive', $hidden->getContent());
        $public = $this->callApi('GET', 'campaigns/public-drive')->assertOk()->assertJsonPath('alternatives', []);
        $this->assertStringNotContainsString('CANARY-PLAN', $public->getContent());
        $this->callApi('GET', 'occurrences')->assertOk()->assertJsonCount(0, 'data');
        $this->callApi('GET', 'meals')->assertOk()->assertJsonCount(0, 'data');
        $this->callApi('GET', 'occurrences/missing')->assertNotFound();

        Need::whereKey('need-a')->update(['quantity_required' => null]);
        $progress = $this->callApi('GET', 'needs/need-a/progress')->assertOk()->assertJsonPath('requested', null)
            ->assertJsonPath('requestedKnown', false)->assertJsonPath('reported', null)->assertJsonPath('reportedKnown', false)
            ->assertJsonPath('fulfilled', false);
        $this->assertFalse($progress->json('fulfilled'));
        $preview = $this->callApi('GET', 'opportunities/need/need-a?channel=abcd1234')->assertOk()
            ->assertJsonPath('attributionRecorded', false)->assertJsonPath('remainingSeatsPromise', false)
            ->assertJsonPath('qrImage', false)->assertJsonPath('alternatives', [])->assertJsonPath('canonicalPath', '/opportunities/need/need-a');
        $this->assertStringNotContainsString('next', $preview->json('canonicalPath'));
        $this->assertSame(0, DB::table('visit_signals')->count());
        $this->callApi('GET', 'opportunities/need/need-a?next=/admin')->assertUnprocessable();
        $this->callApi('GET', 'opportunities/unknown/need-a')->assertNotFound();
    }

    public function test_cancelled_occurrence_keeps_its_status_and_does_not_offer_another_record(): void
    {
        [, $staff] = $this->actor('center-a');
        [, $other] = $this->actor('center-a');
        $this->enable('events.publish');
        $this->callApi('POST', 'staff/centers/center-a/occurrences', $staff, $this->occurrence('cleanup-1'))->assertCreated();
        $this->callApi('POST', 'staff/centers/center-a/occurrences', $other, $this->occurrence('cleanup-2'))->assertCreated();
        $this->callApi('POST', 'staff/centers/center-a/occurrences/cleanup-1/cancel', $staff, ['reason' => 'Lluvia', 'version' => 1])
            ->assertOk()->assertJsonPath('status', 'cancelled')->assertJsonPath('noticesSent', false);
        $this->assertSame(0, DB::table('inbox_notices')->count());
        $this->callApi('GET', 'occurrences')->assertOk()->assertJsonCount(1, 'data')->assertJsonPath('data.0.id', 'cleanup-2');
        $this->callApi('GET', 'occurrences/cleanup-1')->assertOk()->assertJsonPath('status', 'cancelled')->assertJsonPath('alternatives', []);
    }

    public function test_campaign_publication_stays_unavailable_without_a_ledger(): void
    {
        [, $token] = $this->actor();
        $this->callApi('POST', 'campaigns', $token, ['type' => 'cleanup'])->assertStatus(409)->assertJsonPath('error.code', 'feature_disabled');
        $this->enable('campaigns.publish');
        $this->callApi('POST', 'campaigns', $token, ['type' => 'cleanup'])->assertStatus(503)
            ->assertJsonPath('error.code', 'organizer_unavailable')->assertJsonPath('error.details.reason', 'organizer_threshold_unconfigured');
        config(['generosity.campaign_organizer_min_points' => 25]);
        $this->callApi('POST', 'campaigns', $token, ['type' => 'cleanup'])->assertStatus(503)
            ->assertJsonPath('error.details.reason', 'organizer_evidence_unavailable');
        $this->assertSame(0, DB::table('campaigns')->count());
    }

    public function test_enrollment_capacity_is_shared_and_cancellation_releases_once(): void
    {
        [, $staff] = $this->actor('center-a');
        [, $outsider] = $this->actor();
        [$firstUser, $first] = $this->actor();
        [, $second] = $this->actor();
        $this->callApi('POST', 'staff/centers/center-a/occurrences', $outsider, $this->occurrence('cleanup-1', 1))->assertForbidden();
        $this->callApi('GET', 'occurrences')->assertOk()->assertJsonCount(0, 'data');
        $this->callApi('POST', 'occurrences/cleanup-1/enrollments', $first, [])->assertStatus(409)->assertJsonPath('error.code', 'feature_disabled');
        $this->enable('events.publish', 'events.enroll', 'assistance.record');
        $this->callApi('POST', 'staff/centers/center-a/occurrences', $staff, $this->occurrence('cleanup-1', 1))->assertCreated();
        $this->callApi('POST', 'staff/centers/center-a/occurrences', $staff, $this->occurrence('cleanup-2', 1))->assertCreated();
        $seat = $this->callApi('POST', 'occurrences/cleanup-1/enrollments', $first, [])->assertCreated()->json();
        $key = (string) Str::uuid();
        $this->callApi('POST', 'occurrences/cleanup-1/enrollments', $first, [], $key)->assertOk()->assertJsonPath('id', $seat['id']);
        $this->callApi('POST', 'occurrences/cleanup-1/enrollments', $first, [], $key)->assertOk();
        $this->callApi('POST', 'occurrences/cleanup-1/enrollments', $second, [])->assertStatus(409)
            ->assertJsonPath('error.code', 'capacity_exceeded')->assertJsonPath('error.details.available', 0);
        $this->assertSame(1, DB::table('event_occurrences')->where('id', 'cleanup-1')->value('reserved'));
        $this->callApi('POST', 'occurrences/cleanup-1/enrollments', $first, ['payment' => 'card'])->assertUnprocessable();

        $users = User::count();
        $subject = $this->callApi('POST', 'staff/centers/center-a/assisted-subjects', $staff, [
            'consentMethod' => 'in_person', 'contact' => 'CANARY-CONTACT', 'label' => 'Mesa 3',
        ])->assertCreated()->assertJsonPath('accountCreated', false)->json();
        $this->assertSame($users, User::count());
        $this->assertStringNotContainsString('CANARY-CONTACT', json_encode($subject));
        $this->callApi('POST', 'occurrences/cleanup-2/assisted-enrollments', $outsider, ['subjectId' => $subject['id']])->assertForbidden();
        $assisted = $this->callApi('POST', 'occurrences/cleanup-2/assisted-enrollments', $staff, ['subjectId' => $subject['id']])->assertCreated()->json();
        $this->callApi('POST', 'occurrences/cleanup-2/enrollments', $first, [])->assertStatus(409)->assertJsonPath('error.details.available', 0);
        $this->assertNull(DB::table('event_enrollments')->where('id', $assisted['id'])->value('user_id'));

        DB::table('feature_flags')->where('name', 'events.enroll')->update(['mode' => 'off']);
        $this->callApi('POST', 'enrollments/'.$seat['id'].'/cancel', $first, ['version' => 1])->assertOk();
        $this->assertSame(0, DB::table('event_occurrences')->where('id', 'cleanup-1')->value('reserved'));
        $this->callApi('POST', 'enrollments/'.$seat['id'].'/cancel', $first, ['version' => 2])->assertOk();
        $this->assertSame(0, DB::table('event_occurrences')->where('id', 'cleanup-1')->value('reserved'));
        $this->callApi('POST', 'enrollments/'.$seat['id'].'/cancel', $second, ['version' => 2])->assertForbidden();
        $this->enable('events.enroll');
        $reopened = $this->callApi('POST', 'occurrences/cleanup-1/enrollments', $first, [])->assertOk()->json();
        $this->assertSame($seat['id'], $reopened['id']);
        $this->assertSame(1, DB::table('event_occurrences')->where('id', 'cleanup-1')->value('reserved'));
        Carbon::setTestNow('2026-11-05 16:00:00');
        $this->callApi('POST', 'enrollments/'.$seat['id'].'/cancel', $first, ['version' => $reopened['version']])->assertStatus(409)
            ->assertJsonPath('error.code', 'cancellation_closed');
        $this->assertSame(1, DB::table('event_occurrences')->where('id', 'cleanup-1')->value('reserved'));
    }

    public function test_reserved_occurrence_rejects_a_silent_move_and_a_capacity_cut(): void
    {
        [, $staff] = $this->actor('center-a');
        [, $volunteer] = $this->actor();
        $this->enable('events.publish', 'events.enroll');
        $this->callApi('POST', 'staff/centers/center-a/occurrences', $staff, $this->occurrence('cleanup-1', 2))->assertCreated();
        $this->callApi('POST', 'occurrences/cleanup-1/enrollments', $volunteer, [])->assertCreated();
        $same = $this->occurrence('cleanup-1', 2);
        unset($same['id']);
        $same['version'] = 2;
        $this->callApi('PUT', 'staff/centers/center-a/occurrences/cleanup-1', $staff, $same)->assertOk()->assertJsonPath('reserved', 1);
        $moved = $same;
        $moved['venue'] = 'Otra sede';
        $moved['version'] = 3;
        $this->callApi('PUT', 'staff/centers/center-a/occurrences/cleanup-1', $staff, $moved)->assertStatus(409)
            ->assertJsonPath('error.code', 'replacement_required');
        [, $second] = $this->actor();
        $this->callApi('POST', 'occurrences/cleanup-1/enrollments', $second, [])->assertCreated();
        $cut = $same;
        $cut['capacity'] = 1;
        $cut['version'] = 4;
        $this->callApi('PUT', 'staff/centers/center-a/occurrences/cleanup-1', $staff, $cut)->assertStatus(409)
            ->assertJsonPath('error.code', 'capacity_below_commitments');
    }

    public function test_occurrence_version_can_be_refreshed_after_a_reservation(): void
    {
        [, $staff] = $this->actor('center-a');
        [, $volunteer] = $this->actor();
        $this->enable('events.publish', 'events.enroll');
        $this->callApi('POST', 'staff/centers/center-a/occurrences', $staff, $this->occurrence('cleanup-1'))->assertCreated();
        $this->callApi('POST', 'occurrences/cleanup-1/enrollments', $volunteer, [])->assertCreated();
        $path = 'staff/centers/center-a/occurrences/cleanup-1/cancel';
        $this->callApi('POST', $path, $staff, ['reason' => 'Lluvia', 'version' => 1])->assertStatus(409)
            ->assertJsonPath('error.code', 'version_conflict');
        $current = $this->callApi('GET', 'occurrences/cleanup-1')->assertOk()->assertJsonPath('version', 2)->json();
        $this->callApi('GET', 'occurrences')->assertOk()->assertJsonPath('data.0.version', $current['version']);
        DB::table('feature_flags')->whereIn('name', ['events.publish', 'events.enroll'])->update(['mode' => 'off']);
        $this->callApi('POST', $path, $staff, ['reason' => 'Lluvia', 'version' => $current['version']])->assertOk();
        $this->callApi('GET', 'occurrences/cleanup-1')->assertOk()->assertJsonPath('status', 'cancelled')->assertJsonPath('version', 3);
    }

    public function test_assisted_cancellation_requires_current_staff_including_replays(): void
    {
        [$coordinator, $staff] = $this->actor('center-a');
        $this->enable('events.publish', 'assistance.record');
        $subject = $this->callApi('POST', 'staff/centers/center-a/assisted-subjects', $staff, ['consentMethod' => 'in_person'])->assertCreated()->json();
        $seats = [];
        foreach (['cleanup-1', 'cleanup-2'] as $occurrence) {
            $this->callApi('POST', 'staff/centers/center-a/occurrences', $staff, $this->occurrence($occurrence))->assertCreated();
            $seats[] = $this->callApi('POST', 'occurrences/'.$occurrence.'/assisted-enrollments', $staff, ['subjectId' => $subject['id']])->assertCreated()->json();
        }
        DB::table('feature_flags')->whereIn('name', ['events.enroll', 'assistance.record'])->update(['mode' => 'off']);
        $key = (string) Str::uuid();
        $path = 'enrollments/'.$seats[0]['id'].'/cancel';
        $accepted = $this->callApi('POST', $path, $staff, ['version' => 1], $key)->assertOk()->json();
        $this->callApi('POST', $path, $staff, ['version' => 1], $key)->assertOk()->assertExactJson($accepted);
        $this->assertDatabaseHas('event_occurrences', ['id' => 'cleanup-1', 'reserved' => 0]);
        $this->artisan('center:staff', ['center' => 'center-a', 'email' => $coordinator->email, '--revoke' => true, '--operator' => 'test'])->assertSuccessful();
        $this->callApi('POST', $path, $staff, ['version' => 1], $key)->assertForbidden()->assertJsonMissingPath('occurrenceId');
        $this->callApi('POST', 'enrollments/'.$seats[1]['id'].'/cancel', $staff, ['version' => 1])->assertForbidden();
        $this->assertDatabaseHas('event_enrollments', ['id' => $seats[1]['id'], 'status' => 'enrolled', 'version' => 1]);
        $this->assertDatabaseHas('event_occurrences', ['id' => 'cleanup-2', 'reserved' => 1]);
    }

    public function test_attendance_validates_once_and_assisted_attendance_awards_nothing(): void
    {
        [$staffUser, $staff] = $this->actor('center-a');
        [, $validator] = $this->actor('center-a');
        [$volunteerUser, $volunteer] = $this->actor();
        $this->enable('events.publish', 'events.enroll', 'events.validate', 'assistance.record');
        $this->callApi('POST', 'staff/centers/center-a/occurrences', $staff, $this->occurrence('cleanup-1', 2))->assertCreated();
        $seat = $this->callApi('POST', 'occurrences/cleanup-1/enrollments', $volunteer, [])->assertCreated()->json();
        $subject = $this->callApi('POST', 'staff/centers/center-a/assisted-subjects', $staff, ['consentMethod' => 'telephone'])->assertCreated()->json();
        $assisted = $this->callApi('POST', 'occurrences/cleanup-1/assisted-enrollments', $staff, ['subjectId' => $subject['id']])->assertCreated()->json();
        Carbon::setTestNow('2026-11-05 18:00:01');
        $payload = ['enrollmentId' => $seat['id'], 'actualMinutes' => 90, 'version' => 1];
        $this->callApi('POST', 'occurrences/cleanup-1/attendance', $volunteer, $payload)->assertForbidden();
        $before = Contribution::count();
        $validated = $this->callApi('POST', 'occurrences/cleanup-1/attendance', $validator, $payload)->assertCreated()
            ->assertJsonPath('validated', true)->assertJsonPath('award', false)->json();
        $this->callApi('POST', 'occurrences/cleanup-1/attendance', $validator, $payload)->assertOk()->assertJsonPath('contributionId', $validated['contributionId']);
        $this->assertSame($before + 1, Contribution::count());
        $this->assertDatabaseHas('contributions', [
            'id' => $validated['contributionId'], 'user_id' => $volunteerUser->id, 'provenance' => 'center_validated', 'status' => 'fulfilled',
        ]);
        $this->assertNotSame($staffUser->id, Contribution::find($validated['contributionId'])->user_id);
        $this->callApi('POST', 'occurrences/cleanup-1/attendance', $validator, [
            'enrollmentId' => $assisted['id'], 'actualMinutes' => 30, 'version' => 1,
        ])->assertOk()->assertJsonPath('contributionId', null)->assertJsonPath('validated', false)->assertJsonPath('award', false);
        $this->assertSame($before + 1, Contribution::count());
        $this->assertNull(DB::table('event_enrollments')->where('id', $assisted['id'])->value('contribution_id'));
    }

    public function test_scoped_participation_replays_check_membership_before_returning_results(): void
    {
        [$coordinator, $staff] = $this->actor('center-a');
        [, $volunteer] = $this->actor();
        $this->enable('events.publish', 'events.enroll', 'events.validate', 'assistance.record', 'meals.plan', 'outcomes.publish');
        config(['generosity.meal_withdrawal_channel' => 'fixture-counter']);
        $this->callApi('POST', 'staff/centers/center-a/occurrences', $staff, $this->occurrence('cleanup-1'))->assertCreated();
        $seat = $this->callApi('POST', 'occurrences/cleanup-1/enrollments', $volunteer, [])->assertCreated()->json();
        $subject = $this->callApi('POST', 'staff/centers/center-a/assisted-subjects', $staff, ['consentMethod' => 'telephone'])->assertCreated()->json();
        $path = 'occurrences/cleanup-1/assisted-enrollments';
        $data = ['subjectId' => $subject['id']];
        $key = (string) Str::uuid();
        $accepted = $this->callApi('POST', $path, $staff, $data, $key)->assertCreated()->json();
        $operations = [[$path, $data, $key, 201, $accepted]];
        $plan = $this->callApi('POST', 'staff/centers/center-a/meal-plans', $staff, [
            'title' => ['es' => 'Comida'], 'portionUnit' => 'ración', 'privateDetails' => ['address' => 'CANARY-PRIVATE'],
            'slots' => [[
                'localDate' => '2026-11-06', 'windowStart' => '09:00', 'windowEnd' => '11:00',
                'timezone' => 'America/Mexico_City', 'quantityRequired' => 2, 'cutoffAt' => '2026-11-05 12:00:00',
            ]],
        ])->assertCreated()->json();
        $outcome = $this->callApi('POST', 'staff/centers/center-a/outcome-updates', $staff, [
            'body' => ['es' => 'Resultado'], 'identifiesBeneficiary' => false, 'publicationPermission' => false,
        ])->assertCreated()->json();
        Carbon::setTestNow('2026-11-05 18:00:01');
        foreach ([
            ['occurrences/cleanup-1/attendance', ['enrollmentId' => $seat['id'], 'actualMinutes' => 30, 'version' => 1], 201],
            ['meal-plans/'.$plan['id'].'/approval', ['method' => 'recipient', 'version' => 1], 200],
            ['meal-plans/'.$plan['id'].'/withdrawal', ['version' => 2], 200],
            ['outcome-updates/'.$outcome['id'].'/transition', ['action' => 'publish', 'version' => 1], 200],
        ] as [$path, $data, $status]) {
            $key = (string) Str::uuid();
            $accepted = $this->callApi('POST', $path, $staff, $data, $key)->assertStatus($status)->json();
            $operations[] = [$path, $data, $key, $status, $accepted];
        }
        DB::table('feature_flags')->update(['mode' => 'off']);
        foreach ($operations as [$path, $data, $key, $status, $accepted]) {
            $this->callApi('POST', $path, $staff, $data, $key)->assertStatus($status)->assertExactJson($accepted);
        }
        $this->artisan('center:staff', ['center' => 'center-a', 'email' => $coordinator->email, '--revoke' => true, '--operator' => 'test'])->assertSuccessful();
        foreach ($operations as [$path, $data, $key]) {
            $this->callApi('POST', $path, $staff, $data, $key)->assertForbidden()->assertExactJson(['error' => ['code' => 'forbidden', 'details' => []]]);
        }
        $this->assertDatabaseCount('event_enrollments', 2);
        $this->assertDatabaseCount('contributions', 1);
    }

    public function test_meal_commitment_stays_closed_until_a_channel_exists_and_then_rejects_a_partial(): void
    {
        [, $staff] = $this->actor('center-a');
        [, $donor] = $this->actor();
        $this->enable('meals.plan', 'meals.commit');
        $plan = $this->callApi('POST', 'staff/centers/center-a/meal-plans', $staff, [
            'title' => ['es' => 'Comida del viernes'], 'portionUnit' => 'ración',
            'privateDetails' => ['address' => 'CANARY-HOUSEHOLD'],
            'slots' => [[
                'localDate' => '2026-11-06', 'windowStart' => '09:00', 'windowEnd' => '11:00',
                'timezone' => 'America/Mexico_City', 'quantityRequired' => 2, 'cutoffAt' => '2026-11-05 12:00:00',
            ]],
        ])->assertCreated();
        $this->assertStringNotContainsString('CANARY-HOUSEHOLD', $plan->getContent());
        $planId = $plan->json('id');
        $slot = $plan->json('slots.0');
        $this->callApi('GET', 'meals/'.$slot)->assertNotFound();
        $this->callApi('POST', 'meal-plans/'.$planId.'/approval', $staff, ['method' => 'recipient', 'version' => 1])
            ->assertOk()->assertJsonPath('commitmentsAvailable', false)->assertJsonPath('accountCreated', false);
        $public = $this->callApi('GET', 'meals/'.$slot)->assertOk();
        $this->assertStringNotContainsString('CANARY-HOUSEHOLD', $public->getContent());
        $this->assertArrayNotHasKey('privateDetails', $public->json());
        $this->callApi('POST', 'meal-slots/'.$slot.'/commitments', $donor, ['quantity' => 1])->assertStatus(503)
            ->assertJsonPath('error.details.reason', 'withdrawal_channel_unconfigured');
        $this->callApi('POST', 'meal-plans/'.$planId.'/withdrawal', $staff, ['version' => 2])->assertStatus(503);
        $this->assertSame('approved', DB::table('meal_plans')->where('id', $planId)->value('status'));
        $this->assertSame(0, DB::table('meal_commitments')->count());

        config(['generosity.meal_withdrawal_channel' => 'fixture-counter']);
        $this->callApi('POST', 'meal-slots/'.$slot.'/commitments', $donor, ['quantity' => 3])->assertStatus(409)
            ->assertJsonPath('error.code', 'capacity_exceeded')->assertJsonPath('error.details.available', 2);
        $commitment = $this->callApi('POST', 'meal-slots/'.$slot.'/commitments', $donor, ['quantity' => 2])->assertCreated()->json();
        $this->callApi('POST', 'meal-slots/'.$slot.'/commitments', $donor, ['quantity' => 1])->assertOk()->assertJsonPath('id', $commitment['id']);
        $this->assertSame(2, DB::table('meal_slots')->where('id', $slot)->value('quantity_committed'));
        $this->assertSame(0, DB::table('inbox_notices')->count());
        $this->callApi('POST', 'meal-plans/'.$planId.'/withdrawal', $staff, ['version' => 2])
            ->assertOk()->assertJsonPath('noticesSent', false);
        $this->assertSame(1, DB::table('meal_commitments')->count());
        $this->callApi('GET', 'meals/'.$slot)->assertNotFound();
    }

    public function test_assisted_claim_does_not_search_by_name_or_copy_history(): void
    {
        [$coordinator, $staff] = $this->actor('center-a');
        [$claimant, $token] = $this->actor();
        [$reviewerUser, $reviewer] = $this->actor();
        $users = User::count();
        $memberships = DB::table('center_memberships')->count();
        $this->enable('assistance.record', 'events.publish', 'events.enroll');
        $subject = $this->callApi('POST', 'staff/centers/center-a/assisted-subjects', $staff, [
            'consentMethod' => 'in_person', 'label' => 'CANARY-LABEL', 'contact' => 'CANARY-CONTACT',
        ])->assertCreated()->json();
        $this->assertSame($users, User::count());
        $this->assertSame($memberships, DB::table('center_memberships')->count());
        $this->callApi('POST', 'staff/centers/center-a/occurrences', $staff, $this->occurrence('cleanup-1'))->assertCreated();
        $assisted = $this->callApi('POST', 'occurrences/cleanup-1/assisted-enrollments', $staff, ['subjectId' => $subject['id']])->assertCreated()->json();
        $this->callApi('POST', 'assisted-claims', $token, ['name' => 'CANARY-LABEL'])->assertUnprocessable();
        $missing = $this->callApi('POST', 'assisted-claims', $token, ['subjectId' => (string) Str::uuid()])->assertNotFound();
        $this->assertStringNotContainsString('CANARY-LABEL', $missing->getContent());
        $claim = $this->callApi('POST', 'assisted-claims', $token, ['subjectId' => $subject['id']])->assertCreated()->json();
        $this->assertSame(['id', 'status', 'subjectId'], array_keys($claim));
        $this->assertNull(DB::table('assisted_subjects')->where('id', $subject['id'])->value('claimed_user_id'));
        $this->callApi('POST', 'assisted-claims/'.$claim['id'].'/decision', $reviewer, ['action' => 'approve', 'version' => 1])
            ->assertStatus(503)->assertJsonPath('error.details.reason', 'reviewer_ids_unconfigured');
        config(['generosity.assisted_claim_reviewer_ids' => [(string) $coordinator->id, (string) $claimant->id]]);
        $this->callApi('POST', 'assisted-claims/'.$claim['id'].'/decision', $staff, ['action' => 'approve', 'version' => 1])->assertForbidden();
        $this->callApi('POST', 'assisted-claims/'.$claim['id'].'/decision', $token, ['action' => 'approve', 'version' => 1])->assertForbidden();
        config(['generosity.assisted_claim_reviewer_ids' => [(string) $reviewerUser->id]]);
        $key = (string) Str::uuid();
        $this->callApi('POST', 'assisted-claims/'.$claim['id'].'/decision', $reviewer, ['action' => 'approve', 'version' => 1], $key)
            ->assertOk()->assertJsonPath('award', false)->assertJsonPath('historyCopied', false);
        $this->assertSame($claimant->id, (int) DB::table('assisted_subjects')->where('id', $subject['id'])->value('claimed_user_id'));
        $this->assertNull(DB::table('event_enrollments')->where('id', $assisted['id'])->value('user_id'));
        $this->assertSame(0, Contribution::where('user_id', $claimant->id)->count());
        $this->callApi('GET', 'me/enrollments', $token)->assertOk()->assertJsonPath('data.0.id', $assisted['id']);
        $this->callApi('GET', 'me/enrollments', $staff)->assertJsonCount(0, 'data');
        $this->callApi('POST', 'assisted-claims/'.$claim['id'].'/decision', $reviewer, ['action' => 'approve', 'version' => 1], $key)->assertOk();
        config(['generosity.assisted_claim_reviewer_ids' => [(string) $coordinator->id]]);
        $this->callApi('POST', 'assisted-claims/'.$claim['id'].'/decision', $reviewer, ['action' => 'approve', 'version' => 1], $key)
            ->assertForbidden()->assertJsonMissingPath('status');
        config(['generosity.assisted_claim_reviewer_ids' => [(string) $reviewerUser->id]]);

        $this->callApi('POST', 'occurrences/cleanup-1/enrollments', $token, [])->assertStatus(409)->assertJsonPath('error.code', 'enrollment_conflict');
        $this->callApi('POST', 'occurrences/cleanup-1/assisted-enrollments', $staff, ['subjectId' => $subject['id']])->assertOk()->assertJsonPath('id', $assisted['id']);
        $this->assertDatabaseHas('event_occurrences', ['id' => 'cleanup-1', 'reserved' => 1]);
        $this->callApi('GET', 'me/enrollments', $token)->assertOk()->assertJsonCount(1, 'data');
        $this->callApi('POST', 'staff/centers/center-a/occurrences', $staff, $this->occurrence('cleanup-2'))->assertCreated();
        $own = $this->callApi('POST', 'occurrences/cleanup-2/enrollments', $token, [])->assertCreated()->json();
        $this->callApi('POST', 'occurrences/cleanup-2/assisted-enrollments', $staff, ['subjectId' => $subject['id']])
            ->assertStatus(409)->assertJsonPath('error.code', 'enrollment_conflict');
        $this->callApi('POST', 'occurrences/cleanup-2/enrollments', $token, [])->assertOk()->assertJsonPath('id', $own['id']);
        $this->assertDatabaseHas('event_occurrences', ['id' => 'cleanup-2', 'reserved' => 1]);

        $otherSubject = $this->callApi('POST', 'staff/centers/center-a/assisted-subjects', $staff, ['consentMethod' => 'telephone'])->assertCreated()->json();
        $otherClaim = $this->callApi('POST', 'assisted-claims', $token, ['subjectId' => $otherSubject['id']])->assertCreated()->json();
        $this->callApi('POST', 'assisted-claims/'.$otherClaim['id'].'/decision', $reviewer, ['action' => 'approve', 'version' => 1])->assertOk();
        $this->callApi('POST', 'occurrences/cleanup-1/assisted-enrollments', $staff, ['subjectId' => $otherSubject['id']])
            ->assertStatus(409)->assertJsonPath('error.code', 'enrollment_conflict');
        $this->assertDatabaseCount('event_enrollments', 2);
    }

    public function test_claim_overlap_is_rejected_without_merging_reservations_or_evidence(): void
    {
        [, $staff] = $this->actor('center-a');
        [$claimant, $token] = $this->actor();
        [$reviewerUser, $reviewer] = $this->actor();
        config(['generosity.assisted_claim_reviewer_ids' => [(string) $reviewerUser->id]]);
        $this->enable('events.publish', 'events.enroll', 'events.validate', 'assistance.record');
        $this->callApi('POST', 'staff/centers/center-a/occurrences', $staff, $this->occurrence('cleanup-1'))->assertCreated();
        $subject = $this->callApi('POST', 'staff/centers/center-a/assisted-subjects', $staff, ['consentMethod' => 'in_person'])->assertCreated()->json();
        $assisted = $this->callApi('POST', 'occurrences/cleanup-1/assisted-enrollments', $staff, ['subjectId' => $subject['id']])->assertCreated()->json();
        $own = $this->callApi('POST', 'occurrences/cleanup-1/enrollments', $token, [])->assertCreated()->json();
        $claim = $this->callApi('POST', 'assisted-claims', $token, ['subjectId' => $subject['id']])->assertCreated()->json();
        $path = 'assisted-claims/'.$claim['id'].'/decision';
        $this->callApi('POST', $path, $reviewer, ['action' => 'approve', 'version' => 1])->assertStatus(409)->assertJsonPath('error.code', 'claim_conflict');
        $this->assertDatabaseHas('event_occurrences', ['id' => 'cleanup-1', 'reserved' => 2]);
        $this->assertDatabaseHas('event_enrollments', ['id' => $assisted['id'], 'status' => 'enrolled', 'version' => 1]);
        $this->assertDatabaseHas('event_enrollments', ['id' => $own['id'], 'status' => 'enrolled', 'version' => 1]);

        Carbon::setTestNow('2026-11-05 18:00:01');
        $this->callApi('POST', 'occurrences/cleanup-1/attendance', $staff, ['enrollmentId' => $assisted['id'], 'actualMinutes' => 30, 'version' => 1])->assertOk();
        $validated = $this->callApi('POST', 'occurrences/cleanup-1/attendance', $staff, ['enrollmentId' => $own['id'], 'actualMinutes' => 30, 'version' => 1])->assertCreated()->json();
        $this->callApi('POST', $path, $reviewer, ['action' => 'approve', 'version' => 1])->assertStatus(409)->assertJsonPath('error.code', 'claim_conflict');
        $this->assertDatabaseHas('assisted_claims', ['id' => $claim['id'], 'status' => 'pending', 'version' => 1]);
        $this->assertDatabaseHas('assisted_subjects', ['id' => $subject['id'], 'claimed_user_id' => null]);
        $this->assertDatabaseHas('event_enrollments', ['id' => $assisted['id'], 'user_id' => null, 'status' => 'attended', 'contribution_id' => null]);
        $this->assertDatabaseHas('event_enrollments', ['id' => $own['id'], 'user_id' => $claimant->id, 'status' => 'validated', 'contribution_id' => $validated['contributionId']]);
        $this->assertDatabaseCount('event_enrollments', 2);
        $this->assertDatabaseCount('contributions', 1);
    }

    public function test_outcomes_reject_private_content_and_withdrawal_clears_the_public_body(): void
    {
        [, $staff] = $this->actor('center-a');
        [, $outsider] = $this->actor();
        $this->enable('outcomes.publish');
        $body = ['body' => ['es' => 'CANARY-BODY'], 'identifiesBeneficiary' => false, 'publicationPermission' => false];
        $this->callApi('POST', 'staff/centers/center-a/outcome-updates', $outsider, $body)->assertForbidden();
        $private = $body;
        $private['body'] = ['es' => 'Casa', 'household' => 'secret'];
        $this->callApi('POST', 'staff/centers/center-a/outcome-updates', $staff, $private)->assertUnprocessable()
            ->assertJsonPath('error.code', 'private_content_rejected');
        $private['body'] = ['es' => 'persona@example.test'];
        $this->callApi('POST', 'staff/centers/center-a/outcome-updates', $staff, $private)->assertUnprocessable();
        $this->assertSame(0, DB::table('outcome_updates')->count());
        $outcome = $this->callApi('POST', 'staff/centers/center-a/outcome-updates', $staff, $body)->assertCreated()->json();
        $this->callApi('GET', 'outcome-updates/'.$outcome['id'])->assertNotFound();
        DB::table('feature_flags')->where('name', 'outcomes.publish')->update(['mode' => 'off']);
        $this->callApi('POST', 'outcome-updates/'.$outcome['id'].'/transition', $staff, ['action' => 'publish', 'version' => 1])
            ->assertStatus(409)->assertJsonPath('error.code', 'feature_disabled');
        $this->assertDatabaseHas('outcome_updates', ['id' => $outcome['id'], 'status' => 'draft', 'version' => 1]);
        $this->callApi('GET', 'outcome-updates/'.$outcome['id'])->assertNotFound();
        $this->enable('outcomes.publish');
        $key = (string) Str::uuid();
        $accepted = $this->callApi('POST', 'outcome-updates/'.$outcome['id'].'/transition', $staff, ['action' => 'publish', 'version' => 1], $key)->assertOk()->json();
        DB::table('feature_flags')->where('name', 'outcomes.publish')->update(['mode' => 'off']);
        $this->callApi('POST', 'outcome-updates/'.$outcome['id'].'/transition', $staff, ['action' => 'publish', 'version' => 1], $key)->assertOk()->assertExactJson($accepted);
        $this->callApi('GET', 'outcome-updates/'.$outcome['id'])->assertOk()->assertJsonPath('body.es', 'CANARY-BODY');
        $this->callApi('POST', 'outcome-updates/'.$outcome['id'].'/transition', $staff, ['action' => 'correct', 'version' => 2, 'body' => ['es' => 'Corrección']])->assertOk();
        $this->callApi('POST', 'outcome-updates/'.$outcome['id'].'/transition', $staff, ['action' => 'withdraw', 'version' => 3])->assertOk();
        $withdrawn = $this->callApi('GET', 'outcome-updates/'.$outcome['id'])->assertOk()->assertJsonPath('status', 'withdrawn')->assertJsonPath('body', null);
        $this->assertStringNotContainsString('CANARY-BODY', $withdrawn->getContent());
        $this->assertArrayNotHasKey('fulfilled', $withdrawn->json());
    }

    public function test_follows_do_not_send_and_email_stays_unconfigured(): void
    {
        [, $token] = $this->actor();
        $this->enable('follows.update');
        $this->callApi('POST', 'follows', $token, ['targetType' => 'center', 'targetId' => 'center-a', 'channel' => 'email'])
            ->assertStatus(503)->assertJsonPath('error.details.reason', 'consent_unconfigured');
        config(['generosity.follow_email_consent_version' => 'fixture-2026-11-04']);
        $this->callApi('POST', 'follows', $token, ['targetType' => 'center', 'targetId' => 'center-a', 'channel' => 'email'])
            ->assertStatus(503)->assertJsonPath('error.details.reason', 'delivery_unconfigured');
        $this->assertSame(0, DB::table('follows')->count());
        $follow = $this->callApi('POST', 'follows', $token, ['targetType' => 'center', 'targetId' => 'center-a', 'channel' => 'inbox'])
            ->assertCreated()->assertJsonPath('sent', false)->json();
        $this->callApi('POST', 'follows', $token, ['targetType' => 'center', 'targetId' => 'center-a', 'channel' => 'push'])
            ->assertCreated()->assertJsonPath('sent', false)->assertJsonPath('deliveryConfigured', false);
        $this->assertSame(0, DB::table('inbox_notices')->count());
        $this->assertSame(0, DB::table('push_deliveries')->count());
        DB::table('feature_flags')->where('name', 'follows.update')->update(['mode' => 'off']);
        $this->callApi('POST', 'follows/'.$follow['id'].'/unsubscribe', $token, [])->assertOk()->assertJsonPath('sent', false)->assertJsonPath('unsubscribed', true);
        $follows = $this->callApi('GET', 'me/follows', $token)->assertOk()->json();
        $inbox = collect($follows)->firstWhere('id', $follow['id']);
        $this->assertTrue($inbox['unsubscribed']);
        $this->assertFalse($inbox['sent']);
    }

    public function test_inbox_and_preferences_are_owner_scoped_and_do_not_imply_consent(): void
    {
        [$owner, $token] = $this->actor();
        [, $other] = $this->actor();
        DB::table('inbox_notices')->insert([
            'id' => (string) Str::uuid(), 'event_id' => (string) Str::uuid(), 'user_id' => $owner->id,
            'need_id' => 'need-a', 'center_id' => 'center-a', 'title' => json_encode(['es' => 'Aviso'], JSON_THROW_ON_ERROR),
            'created_at' => now(),
        ]);
        $this->callApi('GET', 'me/notifications', $token)->assertOk()->assertJsonPath('unread', 1)->assertJsonCount(1, 'items');
        $this->callApi('GET', 'me/notifications', $other)->assertOk()->assertJsonPath('unread', 0)->assertJsonCount(0, 'items');
        $this->callApi('GET', 'me/notification-preferences', $token)->assertOk()->assertExactJson(['saved' => false]);
        $this->callApi('GET', 'operator/funnel', $token)->assertStatus(503)->assertJsonPath('error.details.reason', 'operator_ids_unconfigured');
        config(['generosity.funnel_operator_ids' => [(string) $owner->id]]);
        $funnel = $this->callApi('GET', 'operator/funnel', $token)->assertOk()->assertJsonPath('shareSheetOpens', null)
            ->assertJsonPath('shareSheetOpensKnown', false)->assertJsonPath('returnParticipation', null)->json();
        $this->assertStringContainsString('Not a completed activity', $funnel['definitions']['commitments']);
        $this->callApi('GET', 'operator/funnel', $other)->assertForbidden();
    }

    private function catalog(): array
    {
        $center = [
            'id' => 'center-a', 'name' => ['es' => 'Centro de apoyo', 'en' => 'Support center'],
            'description' => ['es' => 'Niñas, niños y familias'], 'address' => ['es' => 'Ciudad de México'],
            'categories' => ['health'], 'coordinates' => ['latitude' => 19.4, 'longitude' => -99.1],
            'contact' => ['email' => 'test@example.test'], 'verificationStatus' => 'imported',
            'importedAtIsoDate' => '2026-10-31',
            'needs' => [['id' => 'need-a', 'title' => ['es' => 'Útiles'], 'description' => ['es' => 'Lápices'],
                'helpMethods' => ['goods', 'time'], 'urgent' => true, 'quantityRequired' => 2,
                'expiresOnIsoDate' => '2099-12-31', 'unit' => 'kit']],
        ];

        return ['schemaVersion' => 1, 'centers' => [$center, array_merge($center, ['id' => 'center-b', 'needs' => []])]];
    }

    private function actor(?string $center = null): array
    {
        $user = User::factory()->create();
        if ($center) {
            DB::table('center_memberships')->insert(['user_id' => $user->id, 'center_id' => $center]);
        }

        return [$user, $user->createToken('test', ['*'], now()->addDays(7))->plainTextToken];
    }

    private function callApi(string $method, string $path, ?string $token = null, array $data = [], ?string $key = null)
    {
        $this->app['auth']->forgetGuards();

        return $this->json($method, '/api/v1/'.$path, $data, array_filter([
            'Authorization' => $token ? 'Bearer '.$token : null,
            'Idempotency-Key' => $key ?? (string) Str::uuid(),
        ]));
    }

    private function enable(string ...$names): void
    {
        DB::table('feature_flags')->whereIn('name', $names)->update(['mode' => 'all']);
    }

    private function occurrence(string $id, int $capacity = 2): array
    {
        return [
            'id' => $id, 'title' => ['es' => 'Limpieza'], 'description' => ['es' => 'Parque'],
            'activity' => 'limpieza', 'startsAt' => '2026-11-05 16:00:00', 'endsAt' => '2026-11-05 18:00:00',
            'timezone' => 'America/Mexico_City', 'venue' => 'Parque público', 'capacity' => $capacity,
        ];
    }

    private function campaignRow(string $id, int $owner, string $status, string $title, string $secret, ?string $center = null): array
    {
        return [
            'id' => $id, 'organizer_user_id' => $owner, 'type' => 'cleanup', 'status' => $status,
            'title' => json_encode(['es' => $title], JSON_THROW_ON_ERROR),
            'purpose' => json_encode(['es' => 'Espacio público'], JSON_THROW_ON_ERROR),
            'area' => 'norte', 'center_id' => $center, 'partnership_status' => 'none',
            'private_plan' => json_encode(['address' => $secret], JSON_THROW_ON_ERROR),
            'version' => 1, 'created_at' => now(), 'updated_at' => now(),
        ];
    }
}
