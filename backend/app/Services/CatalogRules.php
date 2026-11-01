<?php

namespace App\Services;

use Closure;

class CatalogRules
{
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
