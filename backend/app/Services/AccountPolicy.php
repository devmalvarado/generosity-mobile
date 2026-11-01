<?php

namespace App\Services;

use Illuminate\Support\Facades\Validator;
use Illuminate\Validation\Rule;

class AccountPolicy
{
    public const VERSION = '2026-11-01';

    public static function acceptance(array $input): array
    {
        Validator::make($input, [
            'adult_confirmed' => 'required|accepted',
            'terms_accepted' => 'required|accepted',
            'privacy_acknowledged' => 'required|accepted',
            'policy_version' => ['required', 'string', Rule::in([self::VERSION])],
        ])->validate();

        return ['policy_version' => self::VERSION, 'policy_accepted_at' => now(), 'adult_confirmed_at' => now()];
    }
}
