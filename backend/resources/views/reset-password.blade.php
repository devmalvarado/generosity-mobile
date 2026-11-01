<!doctype html>
<html lang="{{ $context['locale'] }}">
<head>
<meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><meta name="referrer" content="no-referrer">
<title>{{ __('Reset your password') }} · Generosity</title>
@include('auth-styles')
</head>
<body><main>
<h1>{{ __('Reset your password') }}</h1>
@include('auth-errors')
<p id="password-help">{{ __('Use at least 12 characters, uppercase and lowercase letters, and a number. Very long passwords may need to be shortened.') }}</p>
<form method="post" action="{{ url('/reset-password') }}">
    @csrf
    @foreach($context as $key => $value)<input type="hidden" name="{{ $key }}" value="{{ $value }}">@endforeach
    <input type="hidden" name="token" value="{{ $token }}">
    <input type="hidden" name="email" value="{{ $email }}">
    <label for="new-password">{{ __('New password') }}</label>
    <input id="new-password" name="password" type="password" autocomplete="new-password" minlength="12" maxlength="72" required aria-describedby="password-help{{ $errors->any() ? ' form-errors' : '' }}" @if($errors->has('password')) aria-invalid="true" @endif>
    <label for="confirm-password">{{ __('Confirm password') }}</label>
    <input id="confirm-password" name="password_confirmation" type="password" autocomplete="new-password" minlength="12" maxlength="72" required @if($errors->has('password')) aria-invalid="true" aria-describedby="form-errors" @endif>
    <button type="submit">{{ __('Reset Password') }}</button>
</form>
<p><a href="{{ url('/auth/sign-in').'?'.http_build_query(array_diff_key($context, ['screen' => true]) + ['screen' => 'recover']) }}">{{ __('Request a new recovery link') }}</a></p>
</main></body></html>
