<?php

namespace App\Console\Commands;

use App\Models\Center;
use App\Models\User;
use Illuminate\Console\Command;
use Illuminate\Support\Facades\DB;

class CenterStaff extends Command
{
    protected $signature = 'center:staff {center} {email} {--revoke} {--operator= : Required operator label for the audit log}';

    protected $description = 'Grant or revoke a verified account\'s center-scoped staff membership';

    public function handle(): int
    {
        if (! $this->option('operator') || strlen($this->option('operator')) > 100) {
            $this->error('An operator label is required.');

            return self::FAILURE;
        }
        DB::transaction(function () {
            $user = User::where('email', strtolower($this->argument('email')))->whereNotNull('email_verified_at')->lockForUpdate()->firstOrFail();
            $center = Center::whereKey($this->argument('center'))->lockForUpdate()->firstOrFail();
            $assignment = ['user_id' => $user->id, 'center_id' => $center->id];
            if ($this->option('revoke')) {
                DB::table('center_memberships')->where($assignment)->delete();
            } else {
                DB::table('center_memberships')->insertOrIgnore($assignment);
            }
            DB::table('operator_audits')->insert([
                'operator' => $this->option('operator'), 'action' => $this->option('revoke') ? 'staff_revoked' : 'staff_granted',
                'details' => json_encode($assignment + ['uid' => getmyuid()], JSON_THROW_ON_ERROR), 'created_at' => now(),
            ]);
        }, 5);
        $this->info('Membership updated and audited.');

        return self::SUCCESS;
    }
}
