<?php

namespace App\Services;

use LogicException;

class DeploymentEnvironment
{
    public static function validate(): void
    {
        $channel = config('generosity.channel');
        if (! in_array($channel, ['production', 'preview'], true)) {
            throw new LogicException('Invalid Generosity channel.');
        }
        if ($channel !== 'preview') {
            return;
        }
        $database = config('database.connections.'.config('database.default').'.database');
        $host = strtolower(rtrim((string) parse_url(config('app.url'), PHP_URL_HOST), '.'));
        $portalHost = strtolower(rtrim((string) parse_url(config('identity.portal_url'), PHP_URL_HOST), '.'));
        if (! is_string($database) || ! str_ends_with($database, '_preview')
            || parse_url(config('app.url'), PHP_URL_SCHEME) !== 'https'
            || parse_url(config('identity.portal_url'), PHP_URL_SCHEME) !== 'https'
            || ! $host || in_array($host, ['api.generosity.global', 'generosity.global', 'www.generosity.global'], true)
            || ! $portalHost || in_array($portalHost, ['api.generosity.global', 'generosity.global', 'www.generosity.global'], true)
            || config('session.domain') !== null
            || (config('push.enabled') && config('push.project_id') === 'generosity-v01')
            || ! in_array(config('mail.default'), ['array', 'log', 'smtp'], true)
            || (config('mail.default') === 'smtp' && ! in_array(config('mail.mailers.smtp.host'), ['mailpit', '127.0.0.1', 'localhost'], true))) {
            throw new LogicException('Preview requires an isolated *_preview database, API/portal, host-only cookies, isolated push project and a local mail sink.');
        }
    }
}
