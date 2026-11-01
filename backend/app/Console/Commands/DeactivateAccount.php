<?php

namespace App\Console\Commands;

use App\Models\User;
use Illuminate\Console\Command;

class DeactivateAccount extends Command
{
    protected $signature = 'account:deactivate {user : Numeric account ID} {--confirmed : Ownership and request verified by the operator} {--operator= : Required internal operator label}';

    protected $description = 'Permanently block account access and revoke credentials; does not erase personal records';

    public function handle(): int
    {
        $operator = $this->option('operator');
        if (! $this->option('confirmed') || ! ctype_digit($this->argument('user')) || ! is_string($operator) || trim($operator) === '' || strlen($operator) > 100) {
            $this->error('A numeric account ID, verified request (--confirmed) and operator label are required.');

            return self::FAILURE;
        }
        $user = User::withoutGlobalScope('active')->find($this->argument('user'));
        if (! $user) {
            $this->error('Account not found.');

            return self::FAILURE;
        }
        $user->deactivate($operator);
        $this->info('Account access permanently blocked. Personal-data cancellation remains a separate process.');

        return self::SUCCESS;
    }
}
