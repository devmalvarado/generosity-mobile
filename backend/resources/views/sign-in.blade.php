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
$register = ($context['screen'] ?? null) === 'register';
$signInContext = array_diff_key($context, ['screen' => true]);
@endphp
<!doctype html><html lang="{{ $context['locale'] }}"><head>
<meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>{{ __('Sign in to Generosity') }}</title>
@include('auth-styles')</head><body><main>
<h1>{{ __('Your Generosity account') }}</h1>
<p>{{ __('One account for the web, Android and iOS. Exclusively for Generosity.') }}</p>
<nav class="account-modes" aria-label="{{ __('Your Generosity account') }}">
<a href="{{ url('/auth/sign-in').'?'.http_build_query($signInContext) }}" @if(!$register) aria-current="page" @endif>{{ __('Sign in') }}</a>
<a href="{{ url('/auth/sign-in').'?'.http_build_query($signInContext + ['screen' => 'register']) }}" @if($register) aria-current="page" @endif>{{ __('Create an account') }}</a>
</nav>
@if($error)<p role="alert">{{ $messages[$error] ?? __('We could not verify your sign-in. Please try again.') }}</p>@endif
@include('auth-errors')
@if(auth()->check())
<p>{{ __('Signed in as') }} <strong>{{ auth()->user()->name }}</strong></p>
<p>{{ __('Link another provider to use the same account. A recent sign-in is required.') }}</p>
@endif
@foreach($providers as $provider => $enabled)
<form method="post" action="/auth/{{ $provider }}/start">@csrf
@foreach($context as $key => $value)<input type="hidden" name="{{ $key }}" value="{{ $value }}">@endforeach
@if(auth()->check())<input type="hidden" name="link" value="1">@endif
@if(!auth()->check() && $enabled) @include('account-policy') @endif
<button type="submit" class="provider provider-{{ $provider }}" @disabled(!$enabled) @if(!$enabled) aria-describedby="providers-unavailable" @endif>
<span class="provider-mark" aria-hidden="true"><img src="/auth-assets/{{ $provider }}.png" alt="" width="44" height="44"></span>
<span>{{ auth()->check() ? __('Link') : __('Continue with') }} {{ ucfirst($provider) }}</span>
</button>
</form>
@endforeach
@if(in_array(false, $providers, true))<p class="provider-note" id="providers-unavailable">{{ __('Available after the provider is configured.') }}</p>@endif
<hr>
@if(!$register)
<h2>{{ __('Already have an email account?') }}</h2>
<form method="post" action="/auth/password">@csrf
@foreach($context as $key => $value)<input type="hidden" name="{{ $key }}" value="{{ $value }}">@endforeach
<label for="email">{{ __('Email') }}</label><input id="email" name="email" type="email" autocomplete="username" maxlength="254" value="{{ $input['email'] ?? '' }}" required>
<label for="password">{{ __('Password') }}</label><input id="password" name="password" type="password" autocomplete="current-password" maxlength="72" required>
<button type="submit">{{ __('Sign in') }}</button></form>
@else
<h2>{{ __('Create an account') }}</h2>
<form method="post" action="/auth/account/register">@csrf
@foreach($context as $key => $value)<input type="hidden" name="{{ $key }}" value="{{ $value }}">@endforeach
<label for="new-name">{{ __('Name') }}</label><input id="new-name" name="name" autocomplete="name" maxlength="120" value="{{ $input['name'] ?? '' }}" required>
<label for="new-email">{{ __('Email') }}</label><input id="new-email" name="email" type="email" autocomplete="email" maxlength="254" value="{{ $input['email'] ?? '' }}" required>
<label for="new-password">{{ __('Password') }}</label><input id="new-password" name="password" type="password" autocomplete="new-password" minlength="12" maxlength="72" required aria-describedby="password-help">
<small id="password-help">{{ __('Use at least 12 characters, uppercase and lowercase letters, and a number. Very long passwords may need to be shortened.') }}</small>
@include('account-policy')
<button type="submit">{{ __('Create an account') }}</button></form>
@endif
@foreach(['recover' => 'Recover password', 'verify' => 'Resend verification email'] as $action => $label)
<details @if(($context['screen'] ?? null) === $action) open @endif><summary>{{ __($label) }}</summary><form method="post" action="/auth/account/{{ $action }}">@csrf
@foreach($context as $key => $value)<input type="hidden" name="{{ $key }}" value="{{ $value }}">@endforeach
<label for="{{ $action }}-email">{{ __('Email') }}</label><input id="{{ $action }}-email" name="email" type="email" autocomplete="email" maxlength="254" value="{{ ($context['screen'] ?? null) === $action ? ($input['email'] ?? '') : '' }}" required>
<button type="submit">{{ __($label) }}</button></form></details>
@endforeach
<p>{{ __('Your first Google or Apple sign-in creates an account. Signing in does not grant center permissions.') }}</p>
<p><a href="{{ url('/legal/terms').'?locale='.$context['locale'] }}">{{ __('Terms and conditions') }}</a> · <a href="{{ url('/legal/privacy').'?locale='.$context['locale'] }}">{{ __('Privacy notice') }}</a></p>
@if($context['client'] === 'web')<a href="{{ config('identity.portal_url') }}">{{ __('Return to Generosity') }}</a>
@else<p>{{ __('Close this window to return to the app as a guest.') }}</p>@endif
</main></body></html>
