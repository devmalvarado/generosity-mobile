<?php

use App\Http\Controllers\AuthController;
use App\Http\Controllers\BrowserAuthController;
use Illuminate\Support\Facades\Route;

Route::get('/', fn () => ['service' => 'Generosity API', 'version' => 1]);
Route::get('/reset-password/{token}', fn (string $token) => response()->view('reset-password', ['token' => $token, 'email' => request('email')])
    ->header('Cache-Control', 'no-store')->header('Referrer-Policy', 'no-referrer'))->name('password.reset');
Route::post('/reset-password', [AuthController::class, 'reset'])->middleware('throttle:accounts');

Route::prefix('auth')->middleware('throttle:identity')->group(function () {
    Route::get('sign-in', [BrowserAuthController::class, 'page'])->name('login');
    Route::get('session', [BrowserAuthController::class, 'session']);
    Route::post('logout', [BrowserAuthController::class, 'logout']);
    Route::post('password', [BrowserAuthController::class, 'password'])->middleware('throttle:accounts');
    Route::post('{provider}/start', [BrowserAuthController::class, 'start'])->whereIn('provider', ['google', 'apple']);
    Route::get('google/callback', [BrowserAuthController::class, 'callback'])->defaults('provider', 'google');
    Route::post('apple/callback', [BrowserAuthController::class, 'callback'])->defaults('provider', 'apple');
});
