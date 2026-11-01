<!doctype html>
<html lang="en">
<head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><meta name="referrer" content="no-referrer"><title>Reset password · Generosity</title></head>
<body>
<main style="max-width:32rem;margin:3rem auto;padding:1rem;font-family:system-ui">
    <h1>Reset your password</h1>
    <p>Use at least 12 characters, including uppercase and lowercase letters and a number.</p>
    <form method="post" action="{{ url('/reset-password') }}">
        @csrf
        <input type="hidden" name="token" value="{{ $token }}">
        <input type="hidden" name="email" value="{{ $email }}">
        <p><label>New password <input type="password" name="password" autocomplete="new-password" minlength="12" maxlength="72" required></label></p>
        <p><label>Confirm password <input type="password" name="password_confirmation" autocomplete="new-password" minlength="12" maxlength="72" required></label></p>
        <button type="submit">Reset password</button>
    </form>
</main>
</body>
</html>
