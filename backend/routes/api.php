<?php

use App\Http\Controllers\AuthController;
use App\Http\Controllers\BrowserAuthController;
use App\Http\Controllers\CatalogController;
use App\Http\Controllers\ContributionController;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Route;

Route::prefix('v1')->group(function () {
    Route::post('auth/exchange', [BrowserAuthController::class, 'exchange'])->middleware('throttle:identity');
    Route::get('health', function () {
        DB::table('migrations')->count();

        return ['status' => 'ready', 'apiVersion' => 1, 'payments' => 'disabled'];
    });
    Route::get('centers', [CatalogController::class, 'centers']);
    Route::get('centers/{center}', [CatalogController::class, 'center']);
    Route::get('needs', [CatalogController::class, 'needs']);
    Route::get('needs/{need}', [CatalogController::class, 'need']);
    Route::middleware('throttle:accounts')->prefix('auth')->group(function () {
        Route::post('register', [AuthController::class, 'register']);
        Route::post('login', [AuthController::class, 'login']);
        Route::post('verification', [AuthController::class, 'resend']);
        Route::get('verify/{id}/{hash}', [AuthController::class, 'verify'])->middleware('signed')->name('verification.verify');
        Route::post('forgot-password', [AuthController::class, 'forgot']);
        Route::post('reset-password', [AuthController::class, 'reset']);
    });
    Route::middleware(['auth:sanctum', 'verified'])->group(function () {
        Route::get('me', fn (Request $request) => ['user' => $request->user()]);
        Route::post('auth/logout', [AuthController::class, 'logout']);
        Route::get('me/contributions', [ContributionController::class, 'history']);
        Route::get('me/impact', [ContributionController::class, 'impact']);
        Route::post('contributions', [ContributionController::class, 'store']);
        Route::get('contributions/{contribution}', [ContributionController::class, 'show']);
        Route::post('contributions/{contribution}/transition', [ContributionController::class, 'transition']);
        Route::get('staff/centers/{center}/contributions', [ContributionController::class, 'centerHistory']);
        Route::get('staff/centers/{center}/needs', [CatalogController::class, 'staffNeeds']);
        Route::post('staff/centers/{center}/needs', [CatalogController::class, 'createNeed']);
        Route::put('staff/centers/{center}/needs/{need}', [CatalogController::class, 'updateNeed']);
    });
});
