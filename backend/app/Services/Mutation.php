<?php

namespace App\Services;

use App\Exceptions\ApiProblem;
use App\Models\Center;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Validator;
use Laravel\Sanctum\TransientToken;

class Mutation
{
    public static function staff(int $userId, string $centerId): void
    {
        Center::whereKey($centerId)->lockForUpdate()->firstOrFail();
        abort_unless(DB::table('center_memberships')->where('user_id', $userId)->where('center_id', $centerId)->exists(), 403);
    }

    public static function run(Request $request, array $data, callable $authorize, callable $write)
    {
        $key = $request->header('Idempotency-Key');
        Validator::make(['key' => $key], ['key' => 'required|uuid'])->validate();
        $canonical = function (array $value) use (&$canonical): array {
            if (! array_is_list($value)) {
                ksort($value);
            }
            foreach ($value as &$item) {
                if (is_array($item)) {
                    $item = $canonical($item);
                }
            }

            return $value;
        };
        $hash = hash('sha256', $request->method().' '.$request->path().' '.json_encode($canonical($data), JSON_THROW_ON_ERROR));

        return DB::transaction(function () use ($request, $key, $hash, $authorize, $write) {
            $user = User::whereKey($request->user()->id)->lockForUpdate()->firstOrFail();
            $credential = $request->user()->currentAccessToken();
            if ($credential instanceof TransientToken) {
                // Stateful Sanctum middleware validates the session and CSRF before this transaction.
                abort_unless($request->hasSession() && auth('web')->id() === $user->id, 401);
            } else {
                $token = $credential ? $user->tokens()->find($credential->getKey()) : null;
                abort_unless($token && (! $token->expires_at || $token->expires_at->isFuture()), 401);
            }
            $authorize();
            $prior = DB::table('operations')->where('user_id', $user->id)->where('key', $key)->first();
            if ($prior) {
                if (! hash_equals($prior->request_hash, $hash)) {
                    throw new ApiProblem('operation_reused');
                }

                return response()->json(json_decode($prior->response, true, 512, JSON_THROW_ON_ERROR), $prior->status);
            }
            $response = $write();
            DB::table('operations')->insert([
                'user_id' => $user->id, 'key' => $key, 'request_hash' => $hash,
                'response' => $response->getContent(), 'status' => $response->getStatusCode(), 'created_at' => now(),
            ]);

            return $response;
        }, 5);
    }
}
