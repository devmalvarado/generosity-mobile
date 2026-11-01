# Sign-in branding assets

Downloaded from provider sources on 2026-11-01; hosted locally so the account page
does not contact an identity provider before an explicit sign-in action.

- `google.png`: unmodified 4× light icon button from Google's
  [pre-approved asset ZIP](https://developers.google.com/static/identity/images/signin-assets.zip).
  CSS displays the central mark without the square button's surrounding border.
- `google-sans-latin.woff2`: Google Sans Medium, Latin subset from Google Fonts.
  `OFL.txt` retains the font's SIL Open Font License. The subset includes Spanish
  accents; other scripts use the system fallback until their fonts are added.
- `apple.png`: unmodified logo-only image from Apple's documented
  [button image service](https://developer.apple.com/documentation/signinwithapplerestapi/get-a-sign-in-with-apple-button-that-contains-just-the-apple-logo.).
  Source: `https://appleid.cdn-apple.com/appleid/button/logo?color=black&border=false&height=44&width=44&scale=3`.
  CSS retains the image's full padding and aspect ratio.

Guidance: [Google](https://developers.google.com/identity/branding-guidelines),
[Apple](https://developer.apple.com/design/human-interface-guidelines/sign-in-with-apple).
Provider names/logos remain their owners' trademarks. Buttons remain disabled
until the corresponding server credentials are configured.
