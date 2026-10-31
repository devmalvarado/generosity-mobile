<?php

namespace Tests;

use Illuminate\Foundation\Testing\TestCase as BaseTestCase;

abstract class TestCase extends BaseTestCase
{
    public function createApplication()
    {
        $app = parent::createApplication();
        if ($app->make('db')->connection()->getDatabaseName() !== 'generosity_test') {
            throw new \RuntimeException('Tests require the isolated generosity_test database.');
        }

        return $app;
    }
}
