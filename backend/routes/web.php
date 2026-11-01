<?php

use App\Http\Controllers\AuthController;
use Illuminate\Support\Facades\Route;

Route::get('/', fn () => ['service' => 'Generosity API', 'version' => 1]);
Route::get('/reset-password/{token}', fn (string $token) => response()->view('reset-password', ['token' => $token, 'email' => request('email')])
    ->header('Cache-Control', 'no-store')->header('Referrer-Policy', 'no-referrer'))->name('password.reset');
Route::post('/reset-password', [AuthController::class, 'reset'])->middleware('throttle:accounts');
