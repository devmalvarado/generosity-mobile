@php
$messages = [
    'provider_unavailable' => __('This provider is not configured yet.'),
    'account_link_required' => __('An account already uses this email. Sign in using your existing method, then link this provider from this page.'),
    'reauthentication_required' => __('Sign in again before linking another provider.'),
    'authorization_cancelled' => __('Sign-in was cancelled. Try again or continue as a guest.'),
    'invalid_credentials' => __('The email or password is incorrect.'),
    'email_not_verified' => __('Verify your email before signing in.'),
    'email_requested' => __('If this address is eligible, we will send an email with the next steps. After completing them, return here to sign in.'),
];
@endphp
<!doctype html><html lang="{{ $context['locale'] }}"><head>
<meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>{{ __('Sign in to Generosity') }}</title>
<style>
*{box-sizing:border-box}body{font:1rem/1.5 system-ui,sans-serif;background:#faf6f2;color:#202020;margin:0;padding:24px}main{max-width:440px;margin:4vh auto;background:white;padding:24px;border-radius:20px}h1{font-size:1.6rem}label,input,button{display:block;width:100%}input,button{font:inherit;padding:12px;border:1px solid #999;border-radius:10px;margin:8px 0 16px;min-height:48px}button{background:#a8310b;color:white;border:0;cursor:pointer}button:disabled{background:#eee;color:#555;cursor:default}a{color:#a8310b}small{display:block}p[role=alert]{padding:12px;background:#fff1df;border-left:4px solid #a8310b}hr{border:0;border-top:1px solid #ddd;margin:24px 0}:focus-visible{outline:3px solid #265fa4;outline-offset:3px}
</style></head><body><main>
<h1>{{ __('Your Generosity account') }}</h1>
<p>{{ __('One account for the web, Android and iOS. Exclusively for Generosity.') }}</p>
@if($error)<p role="alert">{{ $messages[$error] ?? __('We could not verify your sign-in. Please try again.') }}</p>@endif
@if($errors->any())<p role="alert">{{ __('Check your email and password.') }}</p>@endif
@if(auth()->check())
<p>{{ __('Signed in as') }} <strong>{{ auth()->user()->name }}</strong></p>
<p>{{ __('Link another provider to use the same account. A recent sign-in is required.') }}</p>
@endif
@foreach($providers as $provider => $enabled)
<form method="post" action="/auth/{{ $provider }}/start">@csrf
@foreach($context as $key => $value)<input type="hidden" name="{{ $key }}" value="{{ $value }}">@endforeach
@if(auth()->check())<input type="hidden" name="link" value="1">@endif
<button type="submit" @disabled(!$enabled)>{{ auth()->check() ? __('Link') : __('Continue with') }} {{ ucfirst($provider) }}</button>
@if(!$enabled)<small>{{ __('Available after the provider is configured.') }}</small>@endif
</form>
@endforeach
<hr><h2>{{ __('Already have an email account?') }}</h2>
<form method="post" action="/auth/password">@csrf
@foreach($context as $key => $value)<input type="hidden" name="{{ $key }}" value="{{ $value }}">@endforeach
<label for="email">{{ __('Email') }}</label><input id="email" name="email" type="email" autocomplete="username" maxlength="254" required>
<label for="password">{{ __('Password') }}</label><input id="password" name="password" type="password" autocomplete="current-password" maxlength="72" required>
<button type="submit">{{ __('Sign in') }}</button></form>
<details id="register" @if(($context['screen'] ?? null) === 'register') open @endif><summary>{{ __('Create an account') }}</summary>
<form method="post" action="/auth/account/register">@csrf
@foreach($context as $key => $value)<input type="hidden" name="{{ $key }}" value="{{ $value }}">@endforeach
<label for="new-name">{{ __('Name') }}</label><input id="new-name" name="name" autocomplete="name" maxlength="120" value="{{ old('name') }}" required>
<label for="new-email">{{ __('Email') }}</label><input id="new-email" name="email" type="email" autocomplete="email" maxlength="254" value="{{ old('email') }}" required>
<label for="new-password">{{ __('Password') }}</label><input id="new-password" name="password" type="password" autocomplete="new-password" minlength="12" maxlength="72" required aria-describedby="password-help">
<small id="password-help">{{ __('Use at least 12 characters, uppercase and lowercase letters, and a number. Maximum 72 UTF-8 bytes.') }}</small>
<button type="submit">{{ __('Create an account') }}</button></form></details>
@foreach(['recover' => 'Recover password', 'verify' => 'Resend verification email'] as $action => $label)
<details><summary>{{ __($label) }}</summary><form method="post" action="/auth/account/{{ $action }}">@csrf
@foreach($context as $key => $value)<input type="hidden" name="{{ $key }}" value="{{ $value }}">@endforeach
<label for="{{ $action }}-email">{{ __('Email') }}</label><input id="{{ $action }}-email" name="email" type="email" autocomplete="email" maxlength="254" required>
<button type="submit">{{ __($label) }}</button></form></details>
@endforeach
<p>{{ __('Your first Google or Apple sign-in creates an account. Signing in does not grant center permissions.') }}</p>
@if($context['client'] === 'web')<a href="{{ config('identity.portal_url') }}">{{ __('Return to Generosity') }}</a>
@else<p>{{ __('Close this window to return to the app as a guest.') }}</p>@endif
</main></body></html>
