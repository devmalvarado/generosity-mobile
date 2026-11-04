<!doctype html><html lang="{{ app()->getLocale() }}"><head>
<meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>{{ __('Feature controls') }} — Generosity</title>
@include('auth-styles')
<style>main{max-width:48rem}form{padding:1rem 0;border-top:1px solid var(--gen-line)}label{display:block;margin-top:1rem}input,select,textarea{width:100%;min-height:44px;box-sizing:border-box}nav{display:flex;gap:1rem}h2{overflow-wrap:anywhere}</style>
</head><body><main>
<nav><a href="?locale=es">Español</a><a href="?locale=en">English</a><a href="{{ config('identity.portal_url') }}">Generosity</a></nav>
<h1>{{ __('Feature controls') }}</h1>
<p>{{ __('Environment') }}: <strong>{{ config('generosity.channel') }}</strong></p>
<p>{{ __('Turning off new actions keeps history, cancellations and fulfillment available. Tester access never grants center permissions.') }}</p>
@if(session('saved'))<p role="status">{{ __('Changes saved.') }}</p>@endif
@if($errors->any())<div role="alert">{{ __('Check the fields and try again.') }}</div>@endif
@foreach($flags as $flag)
<form method="post" action="/admin/features/{{ $flag->name }}?locale={{ app()->getLocale() }}">
@csrf <input type="hidden" name="version" value="{{ $flag->version }}">
<h2>{{ __('features.'.$flag->name) }}</h2>
<label>{{ __('Availability') }}<select name="mode">@foreach(['off' => 'Off', 'testers' => 'Test accounts only', 'all' => 'Everyone'] as $value => $label)<option value="{{ $value }}" @selected($flag->mode === $value)>{{ __($label) }}</option>@endforeach</select></label>
<label>{{ __('Verified tester account IDs, separated by commas') }}<input name="tester_ids" value="{{ implode(',', json_decode($flag->tester_ids, true)) }}" maxlength="2000"></label>
<label>{{ __('Reason for the change') }}<textarea name="reason" required maxlength="500"></textarea></label>
<button type="submit">{{ __('Save changes') }}</button>
</form>
@endforeach
</main></body></html>
