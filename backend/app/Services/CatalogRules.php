<?php

namespace App\Services;

use Closure;

class CatalogRules
{
    public static function directory(): array
    {
        $fields = 'legalName,branchName,activities,hours,waysToContribute,socialLinks,fundingSources,fundingOrigin,administrationType';

        return [
            'fundingOrigin' => 'sometimes|in:public,private,mixed,not_informed',
            'administrationType' => 'sometimes|in:public,private,mixed,not_informed',
            'expectedVersion' => 'sometimes|integer|min:0',
            'directoryProfile' => 'sometimes|array:legalName,branchName,activities,hours,waysToContribute,socialLinks,fundingSources,evidence',
            'directoryProfile.legalName' => 'sometimes|nullable|string|max:500',
            'directoryProfile.branchName' => 'sometimes|nullable|string|max:250',
            'directoryProfile.activities' => ['sometimes', ...self::localized()],
            'directoryProfile.hours' => ['sometimes', ...self::localized()],
            'directoryProfile.waysToContribute' => ['sometimes', ...self::localized()],
            'directoryProfile.socialLinks' => 'sometimes|array|max:10',
            'directoryProfile.socialLinks.*' => 'required|url:https|max:2048|distinct',
            'directoryProfile.fundingSources' => 'sometimes|array|max:6',
            'directoryProfile.fundingSources.*' => 'required|in:donations,recovery_fees,grants,public_support,own_revenue,other|distinct',
            'directoryProfile.evidence' => 'sometimes|array|max:30',
            'directoryProfile.evidence.*' => 'array:fields,url,reviewedOn,asOf,statement',
            'directoryProfile.evidence.*.fields' => 'required|array|min:1|max:10',
            'directoryProfile.evidence.*.fields.*' => 'required|in:'.$fields.'|distinct',
            'directoryProfile.evidence.*.url' => 'required|url:https|max:2048',
            'directoryProfile.evidence.*.reviewedOn' => 'required|date_format:Y-m-d|before_or_equal:today',
            'directoryProfile.evidence.*.asOf' => 'nullable|date_format:Y-m-d|before_or_equal:today',
            'directoryProfile.evidence.*.statement' => 'required|string|max:1000',
        ];
    }

    public static function googleMapsUrl(): array
    {
        return ['bail', 'nullable', 'string', 'url:https', 'max:2048', function (string $attribute, mixed $value, Closure $fail) {
            $url = parse_url($value);
            $host = strtolower($url['host'] ?? '');
            $path = $url['path'] ?? '';
            $valid = (in_array($host, ['google.com', 'www.google.com', 'maps.google.com'], true) && preg_match('~^/maps(?:/|$)~', $path))
                || ($host === 'maps.app.goo.gl' && strlen($path) > 1)
                || ($host === 'goo.gl' && str_starts_with($path, '/maps/'));
            if (! $valid || isset($url['user']) || isset($url['pass'])) {
                $fail('Use a Google Maps place or share URL.');
            }
        }];
    }

    public static function localized(): array
    {
        return ['required', 'array', 'min:1', 'max:20', function (string $attribute, mixed $value, Closure $fail) {
            if (! is_array($value)) {
                $fail('Use an object of language codes and text.');

                return;
            }
            foreach ($value as $locale => $text) {
                if (! preg_match('/^[a-z]{2,3}(-[A-Za-z0-9]{2,8})*$/', (string) $locale) || ! is_string($text) || trim($text) === '' || mb_strlen($text) > 4000) {
                    $fail('Use language codes and non-empty text of at most 4000 characters.');
                }
            }
        }];
    }

    public static function need(): array
    {
        return [
            'title' => self::localized(), 'description' => self::localized(),
            'helpMethods' => 'required|array|min:1|max:3',
            'helpMethods.*' => 'required|in:goods,time,money|distinct',
            'urgent' => 'required|boolean', 'expiresOnIsoDate' => 'nullable|date_format:Y-m-d',
            'quantityRequired' => 'nullable|integer|min:1|max:1000000',
            'unit' => 'nullable|string|max:60',
        ];
    }
}
