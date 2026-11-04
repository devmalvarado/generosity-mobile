<?php

namespace App\Services;

use App\Exceptions\ApiProblem;
use Illuminate\Support\Carbon;

class ParticipationRecords
{
    public static function localized(mixed $value): array
    {
        $data = is_string($value) ? json_decode($value, true, 512, JSON_THROW_ON_ERROR) : $value;
        if (! is_array($data)) {
            return [];
        }
        $text = [];
        foreach (['es', 'en'] as $locale) {
            if (isset($data[$locale]) && is_string($data[$locale])) {
                $text[$locale] = $data[$locale];
            }
        }

        return $text;
    }

    public static function encode(array $text): string
    {
        return json_encode(self::localized($text), JSON_THROW_ON_ERROR);
    }

    public static function stamp(mixed $value): string
    {
        return Carbon::parse($value)->utc()->toISOString();
    }

    public static function assertAggregate(array $body, bool $identifies, bool $permitted): void
    {
        if ($identifies && ! $permitted) {
            throw new ApiProblem('publication_permission_required', 422);
        }
        $forbidden = ['household', 'address', 'recipient', 'dietary', 'roster', 'email', 'phone', 'userid', 'name'];
        $walk = function (mixed $value, ?string $key = null) use (&$walk, $forbidden): void {
            if ($key !== null && in_array(strtolower($key), $forbidden, true)) {
                throw new ApiProblem('private_content_rejected', 422);
            }
            if (is_string($value) && (str_contains($value, '@') || preg_match('/\+?[0-9][0-9 ()-]{7,18}/', $value) === 1)) {
                throw new ApiProblem('private_content_rejected', 422);
            }
            if (is_array($value)) {
                foreach ($value as $childKey => $child) {
                    $walk($child, is_string($childKey) ? $childKey : null);
                }
            }
        };
        $walk($body);
    }

    public static function reviewers(string $key): array
    {
        $ids = config($key, []);

        return is_array($ids) ? array_map('strval', $ids) : [];
    }

    public static function unavailable(string $code, string $reason): void
    {
        throw new ApiProblem($code, 503, ['reason' => $reason]);
    }
}
