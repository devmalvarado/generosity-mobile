<?php

namespace Tests\Feature;

use App\Models\Center;
use App\Models\User;
use Illuminate\Foundation\Testing\DatabaseMigrations;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Notification;
use Illuminate\Support\Str;
use Tests\TestCase;

class CenterApplicationReviewTest extends TestCase
{
    use DatabaseMigrations;

    private function details(array $extra = []): array
    {
        return array_replace([
            'organization_name' => 'Asociación de Apoyo', 'site_name' => 'Sede Niñez',
            'activities' => 'Comedor y capacitación', 'country' => 'México', 'city' => 'Monterrey',
            'contact_name' => 'María Peña', 'contact_role' => 'Coordinadora',
            'representation_confirmed' => true, 'contact_consent' => true,
        ], $extra);
    }

    private function headers(User $user, ?string $key = null): array
    {
        $this->app['auth']->forgetGuards();

        return ['Authorization' => 'Bearer '.$user->createToken('test', ['*'], now()->addDay())->plainTextToken,
            'Idempotency-Key' => $key ?? (string) Str::uuid()];
    }

    private function center(string $id = 'center-claim'): Center
    {
        return Center::create(['id' => $id, 'details' => [
            'name' => ['es' => 'Centro importado', 'en' => 'Imported center'],
            'verificationStatus' => 'imported',
        ]]);
    }

    private function submit(User $owner, array $extra = []): string
    {
        return $this->postJson('/api/v1/center-applications', $this->details($extra), $this->headers($owner))
            ->assertCreated()->assertJsonPath('application.status', 'pending')->json('application.id');
    }

    public function test_claim_references_an_existing_center_without_publishing_or_granting_staff(): void
    {
        $center = $this->center();
        $before = $center->details;
        $owner = User::factory()->create();
        $id = $this->submit($owner, ['claimed_center_id' => $center->id]);
        $this->assertSame($before, Center::find($center->id)->details);
        $this->assertDatabaseCount('centers', 1);
        $this->assertDatabaseCount('center_memberships', 0);
        $this->getJson('/api/v1/centers')->assertJsonCount(1, 'data')->assertJsonPath('data.0.id', $center->id);
        $this->getJson('/api/v1/staff/centers', $this->headers($owner))->assertJsonCount(0, 'centers');
        $this->getJson('/api/v1/me/center-applications', $this->headers($owner))
            ->assertJsonPath('data.0.id', $id)->assertJsonPath('data.0.claimed_center_id', $center->id)
            ->assertJsonMissingPath('data.0.contact_email')->assertJsonMissingPath('data.0.details');
        $this->postJson('/api/v1/center-applications', $this->details(['claimed_center_id' => 'missing-center']), $this->headers($owner))
            ->assertUnprocessable()->assertJsonPath('error.code', 'validation_failed');
        $this->postJson('/api/v1/center-applications', $this->details(['claimed_center_id' => $center->id, 'status' => 'approved']), $this->headers($owner))
            ->assertUnprocessable();
        $this->assertDatabaseCount('center_applications', 1);
    }

    public function test_review_stays_unavailable_until_reviewer_ids_are_configured(): void
    {
        $owner = User::factory()->create();
        $id = $this->submit($owner);
        $decision = ['decision' => 'reject', 'rationale' => 'No hay representación suficiente.', 'version' => 1];
        $this->app['auth']->forgetGuards();
        $this->postJson('/api/v1/center-applications/'.$id.'/decision', $decision)->assertUnauthorized();
        $denied = $this->postJson('/api/v1/center-applications/'.$id.'/decision', $decision, $this->headers($owner));
        $denied->assertStatus(503)->assertJsonPath('error.code', 'review_unavailable');
        $this->assertStringNotContainsString('Asociación de Apoyo', $denied->getContent());
        $this->getJson('/api/v1/center-applications/'.$id, $this->headers($owner))->assertStatus(503);
        config(['generosity.application_reviewer_ids' => ['not-an-account']]);
        $this->postJson('/api/v1/center-applications/'.$id.'/decision', $decision, $this->headers($owner))->assertStatus(503);
        $reviewer = User::factory()->create();
        config(['generosity.application_reviewer_ids' => [(string) $reviewer->id]]);
        $hidden = $this->postJson('/api/v1/center-applications/'.$id.'/decision', $decision, $this->headers($owner));
        $hidden->assertNotFound()->assertJsonPath('error.code', 'not_found');
        $this->assertStringNotContainsString($owner->email, $hidden->getContent());
        $this->postJson('/api/v1/center-applications/'.$id.'/decision', $decision, $this->headers(User::factory()->unverified()->create()))
            ->assertForbidden();
        $this->assertSame('pending', DB::table('center_applications')->where('id', $id)->value('status'));
        $this->assertDatabaseCount('center_application_decisions', 0);
        $this->assertDatabaseCount('center_memberships', 0);
    }

    public function test_applicant_cannot_approve_their_own_request_even_with_another_operational_role(): void
    {
        $target = $this->center();
        $other = $this->center('center-other');
        $applicant = User::factory()->create();
        DB::table('center_memberships')->insert(['user_id' => $applicant->id, 'center_id' => $other->id]);
        config(['generosity.application_reviewer_ids' => [(string) $applicant->id]]);
        $id = $this->submit($applicant, ['claimed_center_id' => $target->id]);
        $this->getJson('/api/v1/center-applications/'.$id, $this->headers($applicant))
            ->assertForbidden()->assertJsonPath('error.code', 'self_review_forbidden');
        $this->postJson('/api/v1/center-applications/'.$id.'/decision', [
            'decision' => 'approve', 'rationale' => 'Intento de autoaprobación.', 'version' => 1,
            'center_id' => $target->id, 'account_id' => $applicant->id, 'roles' => ['staff'],
        ], $this->headers($applicant))->assertForbidden()->assertJsonPath('error.code', 'self_review_forbidden');
        $this->assertSame('pending', DB::table('center_applications')->where('id', $id)->value('status'));
        $this->assertDatabaseMissing('center_memberships', ['user_id' => $applicant->id, 'center_id' => $target->id]);
        $this->assertSame('imported', Center::find($target->id)->details['verificationStatus']);
        $this->assertDatabaseCount('center_application_decisions', 0);
    }

    public function test_represented_staff_cannot_decide_a_claim_on_their_center(): void
    {
        $center = $this->center();
        $owner = User::factory()->create();
        $reviewer = User::factory()->create();
        DB::table('center_memberships')->insert(['user_id' => $reviewer->id, 'center_id' => $center->id]);
        config(['generosity.application_reviewer_ids' => [(string) $reviewer->id]]);
        $id = $this->submit($owner, ['claimed_center_id' => $center->id]);
        $this->postJson('/api/v1/center-applications/'.$id.'/decision', [
            'decision' => 'reject', 'rationale' => 'El personal de la sede no puede decidirla.', 'version' => 1,
        ], $this->headers($reviewer))->assertForbidden()->assertJsonPath('error.code', 'represented_review_forbidden');
        $this->assertSame('pending', DB::table('center_applications')->where('id', $id)->value('status'));
        $this->assertDatabaseCount('center_application_decisions', 0);
        $this->assertSame(1, DB::table('center_memberships')->count());
    }

    public function test_approval_names_center_account_and_staff_role_but_membership_waits_for_acceptance(): void
    {
        Notification::fake();
        $center = $this->center();
        $before = $center->details;
        $owner = User::factory()->create();
        $reviewer = User::factory()->create();
        $stranger = User::factory()->create();
        config(['generosity.application_reviewer_ids' => [(string) $reviewer->id]]);
        $id = $this->submit($owner);
        $this->postJson('/api/v1/center-applications/'.$id.'/decision', [
            'decision' => 'approve', 'rationale' => ' ', 'version' => 1,
            'center_id' => $center->id, 'account_id' => $owner->id, 'roles' => ['staff'],
        ], $this->headers($reviewer))->assertUnprocessable();
        $this->postJson('/api/v1/center-applications/'.$id.'/decision', [
            'decision' => 'approve', 'rationale' => 'La representación declarada es suficiente para membresía.', 'version' => 1,
            'center_id' => $center->id, 'account_id' => $owner->id, 'roles' => ['owner'],
        ], $this->headers($reviewer))->assertUnprocessable();
        $this->postJson('/api/v1/center-applications/'.$id.'/decision', [
            'decision' => 'approve', 'rationale' => 'La sede todavía no está en el catálogo.', 'version' => 1,
            'center_id' => 'missing-center', 'account_id' => $owner->id, 'roles' => ['staff'],
            'verified' => true,
        ], $this->headers($reviewer))->assertUnprocessable();
        $this->postJson('/api/v1/center-applications/'.$id.'/decision', [
            'decision' => 'approve', 'rationale' => 'La sede indicada no existe.', 'version' => 1,
            'center_id' => 'missing-center', 'account_id' => $owner->id, 'roles' => ['staff'],
        ], $this->headers($reviewer))->assertStatus(409)->assertJsonPath('error.code', 'center_unavailable');
        $this->postJson('/api/v1/center-applications/'.$id.'/decision', [
            'decision' => 'approve', 'rationale' => 'La cuenta indicada no puede aceptar.', 'version' => 1,
            'center_id' => $center->id, 'account_id' => User::factory()->unverified()->create()->id, 'roles' => ['staff'],
        ], $this->headers($reviewer))->assertStatus(422)->assertJsonPath('error.code', 'account_unavailable');
        $key = (string) Str::uuid();
        $approved = $this->postJson('/api/v1/center-applications/'.$id.'/decision', [
            'decision' => 'approve', 'rationale' => 'Se identifica la sede, la cuenta y el rol de personal.', 'version' => 1,
            'center_id' => $center->id, 'account_id' => $owner->id, 'roles' => ['staff'],
        ], $this->headers($reviewer, $key))->assertOk()
            ->assertJsonPath('application.status', 'approved')->assertJsonPath('application.acceptance_required', true)
            ->assertJsonPath('application.center_id', $center->id)->assertJsonPath('application.account_id', $owner->id)
            ->assertJsonPath('application.roles', ['staff'])->assertJsonMissingPath('application.decision.actor_id');
        $this->assertStringNotContainsString($reviewer->email, $approved->getContent());
        $this->postJson('/api/v1/center-applications/'.$id.'/decision', [
            'decision' => 'approve', 'rationale' => 'Se identifica la sede, la cuenta y el rol de personal.', 'version' => 1,
            'center_id' => $center->id, 'account_id' => $owner->id, 'roles' => ['staff'],
        ], $this->headers($reviewer, $key))->assertOk()->assertExactJson($approved->json());
        $this->postJson('/api/v1/center-applications/'.$id.'/decision', [
            'decision' => 'reject', 'rationale' => 'Otra decisión con la misma llave.', 'version' => 1,
        ], $this->headers($reviewer, $key))->assertConflict()->assertJsonPath('error.code', 'operation_reused');
        $this->assertDatabaseCount('center_application_decisions', 1);
        $this->assertDatabaseCount('center_memberships', 0);
        $this->assertSame($before, Center::find($center->id)->details);
        $this->getJson('/api/v1/staff/centers', $this->headers($owner))->assertJsonCount(0, 'centers');
        $this->postJson('/api/v1/center-applications/'.$id.'/acceptance', ['version' => 2], $this->headers($reviewer))
            ->assertNotFound();
        $this->postJson('/api/v1/center-applications/'.$id.'/acceptance', ['version' => 2], $this->headers($stranger))
            ->assertNotFound();
        $this->postJson('/api/v1/center-applications/'.$id.'/acceptance', ['version' => 2, 'verified' => true], $this->headers($owner))
            ->assertUnprocessable();
        $accepted = $this->postJson('/api/v1/center-applications/'.$id.'/acceptance', ['version' => 2], $this->headers($owner))
            ->assertOk()->assertJsonPath('application.status', 'approved')
            ->assertJsonPath('application.acceptance_required', false)->assertJsonPath('application.roles', ['staff']);
        $this->postJson('/api/v1/center-applications/'.$id.'/acceptance', ['version' => 2], $this->headers($owner))
            ->assertOk()->assertJsonPath('application.accepted_at', $accepted->json('application.accepted_at'));
        $this->assertDatabaseCount('center_memberships', 1);
        $this->assertDatabaseHas('center_memberships', ['user_id' => $owner->id, 'center_id' => $center->id]);
        $this->assertSame(1, DB::table('center_application_decisions')->where('action', 'accept')->count());
        $this->assertSame($before, Center::find($center->id)->details);
        $this->assertArrayNotHasKey('taxEligible', Center::find($center->id)->details);
        $this->getJson('/api/v1/staff/centers', $this->headers($owner))->assertJsonCount(1, 'centers')->assertJsonPath('centers.0.id', $center->id);
        $this->getJson('/api/v1/staff/centers', $this->headers($reviewer))->assertJsonCount(0, 'centers');
        Notification::assertNothingSent();
        $this->postJson('/api/v1/center-applications/'.$id.'/withdrawal', ['version' => 3], $this->headers($owner))
            ->assertStatus(409)->assertJsonPath('error.code', 'invalid_transition');
        $this->assertDatabaseCount('center_memberships', 1);
        $this->assertDatabaseCount('center_application_decisions', 2);
    }

    public function test_request_changes_and_rejection_record_rationale_without_side_effects(): void
    {
        $center = $this->center();
        $owner = User::factory()->create();
        $other = User::factory()->create();
        $reviewer = User::factory()->create();
        config(['generosity.application_reviewer_ids' => [(string) $reviewer->id]]);
        $first = $this->submit($owner, ['claimed_center_id' => $center->id, 'organization_name' => 'Primera asociación']);
        $second = $this->submit($other, ['claimed_center_id' => $center->id, 'organization_name' => 'Segunda asociación', 'site_name' => 'Otra sede']);
        $case = $this->getJson('/api/v1/center-applications/'.$first, $this->headers($reviewer))->assertOk()
            ->assertJsonPath('application.possible_duplicate', true)->assertJsonPath('application.applicant_id', $owner->id)
            ->assertJsonMissingPath('application.decisions.0.actor_id');
        $this->assertStringNotContainsString($other->email, $case->getContent());
        $this->assertStringNotContainsString('Segunda asociación', $case->getContent());
        $this->getJson('/api/v1/center-applications/'.$first, $this->headers($owner))->assertNotFound();
        $changed = $this->postJson('/api/v1/center-applications/'.$second.'/decision', [
            'decision' => 'request_changes', 'rationale' => 'Falta precisar la sede pública.', 'version' => 1,
        ], $this->headers($reviewer))->assertOk()->assertJsonPath('application.status', 'changes_requested');
        $this->assertStringNotContainsString($reviewer->email, $changed->getContent());
        $this->getJson('/api/v1/me/center-applications', $this->headers($other))
            ->assertJsonPath('data.0.status', 'changes_requested')
            ->assertJsonPath('data.0.decision.rationale', 'Falta precisar la sede pública.')
            ->assertJsonMissingPath('data.0.decision.actor_id')->assertJsonMissingPath('data.0.contact_email');
        $this->postJson('/api/v1/center-applications/'.$second.'/decision', [
            'decision' => 'reject', 'rationale' => 'La solicitud no corresponde a una sede operable.', 'version' => 2,
        ], $this->headers($reviewer))->assertOk()->assertJsonPath('application.status', 'rejected');
        $this->postJson('/api/v1/center-applications/'.$second.'/decision', [
            'decision' => 'approve', 'rationale' => 'Demasiado tarde.', 'version' => 3,
            'center_id' => $center->id, 'account_id' => $other->id, 'roles' => ['staff'],
        ], $this->headers($reviewer))->assertStatus(409)->assertJsonPath('error.code', 'invalid_transition');
        $this->assertDatabaseCount('center_memberships', 0);
        $this->assertSame('imported', Center::find($center->id)->details['verificationStatus']);
        $this->assertSame(2, DB::table('center_application_decisions')->where('application_id', $second)->count());
    }

    public function test_withdrawal_retains_the_application_and_prior_decisions(): void
    {
        $owner = User::factory()->create();
        $reviewer = User::factory()->create();
        $other = User::factory()->create();
        config(['generosity.application_reviewer_ids' => [(string) $reviewer->id]]);
        $id = $this->submit($owner);
        $this->postJson('/api/v1/center-applications/'.$id.'/decision', [
            'decision' => 'request_changes', 'rationale' => 'Aclara el horario público.', 'version' => 1,
        ], $this->headers($reviewer))->assertOk();
        $this->postJson('/api/v1/center-applications/'.$id.'/withdrawal', ['version' => 2], $this->headers($other))->assertNotFound();
        $this->postJson('/api/v1/center-applications/'.$id.'/withdrawal', ['version' => 1], $this->headers($owner))
            ->assertStatus(409)->assertJsonPath('error.code', 'version_conflict');
        $this->postJson('/api/v1/center-applications/'.$id.'/withdrawal', ['version' => 2, 'rationale' => 'Ya no represento a la sede.'], $this->headers($owner))
            ->assertOk()->assertJsonPath('application.status', 'withdrawn')->assertJsonPath('application.decision.action', 'withdraw');
        $this->assertDatabaseHas('center_applications', ['id' => $id, 'status' => 'withdrawn']);
        $this->assertSame(2, DB::table('center_application_decisions')->where('application_id', $id)->count());
        $this->assertDatabaseHas('center_application_decisions', ['application_id' => $id, 'action' => 'request_changes']);
        $this->postJson('/api/v1/center-applications/'.$id.'/decision', [
            'decision' => 'approve', 'rationale' => 'No debe revivir una solicitud retirada.', 'version' => 3,
            'center_id' => 'center-claim', 'account_id' => $owner->id, 'roles' => ['staff'],
        ], $this->headers($reviewer))->assertStatus(409);
        $this->assertSame(2, DB::table('center_application_decisions')->where('application_id', $id)->count());
        $this->assertDatabaseCount('center_memberships', 0);
    }

    public function test_browser_decision_requires_csrf_and_review_writes_are_rate_limited(): void
    {
        $owner = User::factory()->create();
        $reviewer = User::factory()->create();
        config(['generosity.application_reviewer_ids' => [(string) $reviewer->id]]);
        $id = $this->submit($owner);
        config(['sanctum.stateful' => ['generosity.global']]);
        $this->app->instance('env', 'production');
        $this->actingAs($reviewer, 'web');
        $this->postJson('https://api.generosity.test/api/v1/center-applications/'.$id.'/decision', [
            'decision' => 'reject', 'rationale' => 'Falta un dato público.', 'version' => 1,
        ], ['Origin' => 'https://generosity.global'])->assertStatus(419);
        $this->app->instance('env', 'testing');
        $this->app['auth']->forgetGuards();
        for ($i = 0; $i < 10; $i++) {
            $this->postJson('/api/v1/center-applications/'.$id.'/decision', [
                'decision' => 'reject', 'rationale' => 'Intento '.$i, 'version' => 1,
            ], $this->headers($reviewer))->assertStatus($i === 0 ? 200 : 409);
        }
        $this->postJson('/api/v1/center-applications/'.$id.'/decision', [
            'decision' => 'reject', 'rationale' => 'Demasiados intentos.', 'version' => 1,
        ], $this->headers($reviewer))->assertStatus(429)->assertJsonPath('error.code', 'rate_limited');
    }
}
