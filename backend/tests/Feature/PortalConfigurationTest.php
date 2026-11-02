<?php

namespace Tests\Feature;

use Tests\TestCase;

class PortalConfigurationTest extends TestCase
{
    public function test_deployment_rejects_missing_portal_session_or_cors_configuration(): void
    {
        config(['identity.portal_url' => 'https://generosity.global/#acceso',
            'sanctum.stateful' => [], 'cors.allowed_origins' => ['https://generosity.global'],
            'cors.supports_credentials' => true]);
        $this->artisan('auth:check-portal')->assertFailed();
        config(['sanctum.stateful' => ['generosity.global']]);
        $this->artisan('auth:check-portal')->assertSuccessful();
        config(['cors.allowed_origins' => ['https://unrelated.example']]);
        $this->artisan('auth:check-portal')->assertFailed();
        config(['cors.allowed_origins' => ['https://generosity.global'], 'cors.supports_credentials' => false]);
        $this->artisan('auth:check-portal')->assertFailed();
    }

    public function test_origin_matching_includes_ports_and_does_not_trust_a_host_suffix(): void
    {
        config(['identity.portal_url' => 'http://localhost:47321/#acceso',
            'sanctum.stateful' => ['localhost:47321'], 'cors.allowed_origins' => ['http://localhost:47321'],
            'cors.supports_credentials' => true]);
        $this->artisan('auth:check-portal')->assertSuccessful();
        config(['sanctum.stateful' => ['localhost:8088']]);
        $this->artisan('auth:check-portal')->assertFailed();
        config(['identity.portal_url' => 'https://generosity.global.attacker.test/#acceso',
            'sanctum.stateful' => ['generosity.global'],
            'cors.allowed_origins' => ['https://generosity.global.attacker.test']]);
        $this->artisan('auth:check-portal')->assertFailed();
    }
}
