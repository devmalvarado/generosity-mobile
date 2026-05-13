#!/usr/bin/env sh
set -eu

android_en="androidApp/src/main/res/values/strings.xml"
android_es="androidApp/src/main/res/values-es/strings.xml"
ios_en="iosApp/GenerosityApp/en.lproj/Localizable.strings"
ios_es="iosApp/GenerosityApp/es.lproj/Localizable.strings"

tmp_dir="${TMPDIR:-/tmp}/generosity-localization-check"
mkdir -p "$tmp_dir"

grep -o 'name="[^"]*"' "$android_en" | sed 's/name="//; s/"//' | sort > "$tmp_dir/android-en.keys"
grep -o 'name="[^"]*"' "$android_es" | sed 's/name="//; s/"//' | sort > "$tmp_dir/android-es.keys"
grep -o '^"[^"]*"' "$ios_en" | sed 's/"//g' | sort > "$tmp_dir/ios-en.keys"
grep -o '^"[^"]*"' "$ios_es" | sed 's/"//g' | sort > "$tmp_dir/ios-es.keys"

diff -u "$tmp_dir/android-en.keys" "$tmp_dir/android-es.keys"
diff -u "$tmp_dir/ios-en.keys" "$tmp_dir/ios-es.keys"

echo "Localization keys are covered for English and Spanish."

