<!doctype html><html lang="{{ $locale }}"><head>
<meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>{{ $document === 'terms' ? __('Terms and conditions') : __('Privacy notice') }} — Generosity</title>
@include('auth-styles')
<style>main{max-width:56rem}article{overflow-wrap:anywhere}h2{margin-top:2rem}table{border-collapse:collapse;width:100%}td,th{padding:.6rem;border:1px solid var(--gen-line);text-align:left}nav{display:flex;flex-wrap:wrap;gap:1rem}nav a{min-height:44px;display:inline-flex;align-items:center}ul,ol{padding-left:1.3rem}</style>
</head><body><main>
<nav aria-label="{{ __('Language') }}"><a href="?locale=es" lang="es">Español</a><a href="?locale=en" lang="en">English</a><a href="{{ config('identity.portal_url') }}">Generosity</a></nav>
<article>{!! $content !!}</article>
<nav><a href="/legal/terms?locale={{ $locale }}">{{ __('Terms and conditions') }}</a><a href="/legal/privacy?locale={{ $locale }}">{{ __('Privacy notice') }}</a></nav>
</main></body></html>
