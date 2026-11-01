<?php

use App\Http\Controllers\BrowserAuthController;
use Illuminate\Support\Facades\Route;

Route::get('/', fn () => ['service' => 'Generosity API', 'version' => 1]);
Route::get('/legal/{document}', function (\Illuminate\Http\Request $request, string $document) {
    $locale = $request->query('locale', 'es');
    abort_unless(in_array($locale, ['es', 'en'], true), 404);
    app()->setLocale($locale);
    $markdown = file_get_contents(resource_path('legal/'.$document.'.'.$locale.'.md'));
    $content = \Illuminate\Support\Str::markdown($markdown, ['html_input' => 'strip', 'allow_unsafe_links' => false]);

    return response()->view('legal', compact('content', 'locale', 'document'));
})->whereIn('document', ['terms', 'privacy'])->name('legal');
Route::get('/reset-password/{token}', [BrowserAuthController::class, 'resetPage'])->name('password.reset');
Route::post('/reset-password', [BrowserAuthController::class, 'reset'])->middleware('throttle:accounts');
Route::get('/verify-email/{id}/{hash}', [BrowserAuthController::class, 'verify'])->middleware('throttle:accounts')->name('verification.browser');

Route::prefix('auth')->middleware('throttle:identity')->group(function () {
    Route::get('sign-in', [BrowserAuthController::class, 'page'])->name('login');
    Route::get('session', [BrowserAuthController::class, 'session']);
    Route::post('logout', [BrowserAuthController::class, 'logout']);
    Route::post('password', [BrowserAuthController::class, 'password'])->middleware('throttle:accounts');
    Route::post('account/{action}', [BrowserAuthController::class, 'accountAction'])->whereIn('action', ['register', 'recover', 'verify'])->middleware('throttle:accounts');
    Route::post('{provider}/start', [BrowserAuthController::class, 'start'])->whereIn('provider', ['google', 'apple']);
    Route::get('google/callback', [BrowserAuthController::class, 'callback'])->defaults('provider', 'google');
    Route::post('apple/callback', [BrowserAuthController::class, 'callback'])->defaults('provider', 'apple');
});
