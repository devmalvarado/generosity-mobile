@php
$messages = [
    'email_requested' => ['Check your email', 'If this address is eligible, we will send an email with the next steps. After completing them, return here to sign in.'],
    'email_verified' => ['Email verified', 'Your email is verified. You can now sign in to Generosity.'],
    'password_reset' => ['Password updated', 'Your password has been updated and previous sessions have been closed. Sign in with your new password.'],
    'invalid_verification' => ['This verification link is not valid', 'The link may have expired or changed. Request a new verification email.'],
    'invalid_reset_token' => ['This recovery link is not valid', 'The link may have expired or already been used. Request a new recovery link.'],
    'session_expired' => ['Please try again', 'Your form session has expired. Open the sign-in page and try again.'],
    'rate_limited' => ['Please try again shortly', 'Too many attempts. Wait a few minutes before trying again.'],
];
[$heading, $message] = $messages[$outcome];
$signInContext = array_diff_key($context, ['screen' => true]);
@endphp
<!doctype html><html lang="{{ $context['locale'] }}"><head>
<meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><meta name="referrer" content="no-referrer">
<title>{{ __($heading) }} · Generosity</title>
@include('auth-styles')
</head><body><main>
<h1>{{ __($heading) }}</h1>
<p role="status">{{ __($message) }}</p>
<p><a href="{{ url('/auth/sign-in').'?'.http_build_query($signInContext) }}">{{ __('Sign in') }}</a></p>
@if(in_array($outcome, ['email_requested', 'invalid_verification']))
<p><a href="{{ url('/auth/sign-in').'?'.http_build_query($signInContext + ['screen' => 'verify']) }}">{{ __('Resend verification email') }}</a></p>
@endif
@if(in_array($outcome, ['email_requested', 'invalid_reset_token']))
<p><a href="{{ url('/auth/sign-in').'?'.http_build_query($signInContext + ['screen' => 'recover']) }}">{{ __('Request a new recovery link') }}</a></p>
@endif
@if($context['client'] !== 'web')
<p>{{ __('If you opened this email on another device, return to the app and sign in there.') }}</p>
@else
<p><a href="{{ config('identity.portal_url') }}">{{ __('Return to Generosity') }}</a></p>
@endif
</main></body></html>
