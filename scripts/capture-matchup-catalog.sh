#!/bin/sh
set -eu

usage() {
  echo "usage: $0 --serial ADB_SERIAL --output NEW_DIRECTORY" >&2
  echo "Captures an already-installed debug build; it never installs or starts an emulator." >&2
  exit 2
}

serial=""
output=""
while [ "$#" -gt 0 ]; do
  case "$1" in
    --serial)
      [ "$#" -ge 2 ] || usage
      serial="$2"
      shift 2
      ;;
    --output)
      [ "$#" -ge 2 ] || usage
      output="$2"
      shift 2
      ;;
    *) usage ;;
  esac
done

[ -n "$serial" ] && [ -n "$output" ] || usage
command -v adb >/dev/null 2>&1 || { echo "adb is required" >&2; exit 1; }
command -v sips >/dev/null 2>&1 || { echo "sips is required" >&2; exit 1; }
command -v shasum >/dev/null 2>&1 || { echo "shasum is required" >&2; exit 1; }
[ ! -e "$output" ] || { echo "refusing to overwrite existing output: $output" >&2; exit 1; }

device_state="$(adb -s "$serial" get-state 2>/dev/null || true)"
[ "$device_state" = "device" ] || { echo "ADB target is not ready: $serial" >&2; exit 1; }

package="com.nasfinder.whattoeat"
activity="$package/.MainActivity"
adb -s "$serial" shell pm path "$package" >/dev/null 2>&1 || {
  echo "Debug app is not installed. Build/install separately only with explicit approval." >&2
  exit 1
}
adb -s "$serial" shell run-as "$package" true >/dev/null 2>&1 || {
  echo "Installed package is not a debuggable build; matchup fixtures are unavailable." >&2
  exit 1
}

repo_root="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
if git -C "$repo_root" rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  revision="$(git -C "$repo_root" rev-parse HEAD)"
  if [ -n "$(git -C "$repo_root" status --porcelain)" ]; then revision="$revision+dirty"; fi
else
  revision="no-git"
fi

one_line() {
  tr '\t\r\n' '   ' | sed 's/  */ /g; s/^ //; s/ $//'
}

mkdir -p "$output"
manifest="$output/capture-manifest.tsv"
printf 'platform\tstate\tfile\tpixels\tsha256\tserial\tmodel\tos_version\tapi_level\tdisplay_id\tdisplay_size\tdensity\tapp_bounds\tsystem_insets\tfont_scale\tlocale\ttimezone\ttheme/night_mode\torientation\tfixture\tpackage_version\tbuild_revision\n' > "$manifest"
model="$(adb -s "$serial" shell getprop ro.product.model | tr -d '\r')"
os_version="$(adb -s "$serial" shell getprop ro.build.version.release | tr -d '\r')"
api_level="$(adb -s "$serial" shell getprop ro.build.version.sdk | tr -d '\r')"
display_size="$(adb -s "$serial" shell wm size | one_line)"
density="$(adb -s "$serial" shell wm density | tr '\n' ';' | tr -d '\r')"
locale="$(adb -s "$serial" shell getprop persist.sys.locale | tr -d '\r')"
timezone="$(adb -s "$serial" shell getprop persist.sys.timezone | tr -d '\r')"
font_scale="$(adb -s "$serial" shell settings get system font_scale | tr -d '\r')"
package_version="$(adb -s "$serial" shell dumpsys package "$package" | awk -F= '/versionName=/{print $2; exit}' | tr -d '\r')"
night_mode="$(adb -s "$serial" shell cmd uimode night 2>/dev/null | one_line || true)"
[ -n "$night_mode" ] || night_mode="unavailable"
adb -s "$serial" shell input keyevent KEYCODE_WAKEUP >/dev/null
adb -s "$serial" shell wm dismiss-keyguard >/dev/null 2>&1 || true
sleep 1
display_id="$(adb -s "$serial" shell dumpsys display 2>/dev/null | sed -n "s/.*isActive=true, displayId=[0-9][0-9]*, uniqueId='local:\([0-9][0-9]*\)'.*/\1/p" | head -n 1 | tr -d '\r')"
[ -n "$display_id" ] || display_id="unavailable"

states="home region region-search loading results decision decision-recorded history-empty history-populated favorites-empty favorites-populated settings"
for state in $states; do
  adb -s "$serial" shell am force-stop "$package"
  adb -s "$serial" shell am start -W -n "$activity" -e matchup_state "$state" >/dev/null
  sleep 1
  focused_window="$(adb -s "$serial" shell dumpsys window 2>/dev/null | awk '/mCurrentFocus=Window/ { value=$0 } END { print value }' | one_line)"
  case "$focused_window" in
    *"$package"*) ;;
    *) echo "App is not visible for $state; unlock and wake the target first: $focused_window" >&2; exit 1 ;;
  esac
  file="$output/$state.png"
  if [ "$display_id" = "unavailable" ]; then
    adb -s "$serial" exec-out screencap -p > "$file"
  else
    adb -s "$serial" exec-out screencap -d "$display_id" -p > "$file"
  fi
  width="$(sips -g pixelWidth "$file" | awk '/pixelWidth/ {print $2}')"
  height="$(sips -g pixelHeight "$file" | awk '/pixelHeight/ {print $2}')"
  hash="$(shasum -a 256 "$file" | awk '{print $1}')"
  if [ "$width" -gt "$height" ]; then orientation="landscape"; else orientation="portrait"; fi
  window_dump="$(adb -s "$serial" shell dumpsys window windows 2>/dev/null || true)"
  app_bounds="$(printf '%s\n' "$window_dump" | awk -v pkg="$package" '
    index($0, pkg) && /Window/ { in_app=1 }
    in_app && /mFrame=/ { sub(/^.*mFrame=/, ""); print; exit }
  ' | one_line)"
  [ -n "$app_bounds" ] || app_bounds="unavailable"
  system_insets="$(printf '%s\n' "$window_dump" | awk -v pkg="$package" '
    index($0, pkg) && /Window/ { in_app=1 }
    in_app && /Window #[0-9]+/ && !index($0, pkg) { exit }
    in_app && (/mInsetsState=/ || /mGivenContentInsets=/ || /mGivenVisibleInsets=/ || /InsetsSource.*(statusBars|navigationBars|ime)/) { print }
  ' | head -n 12 | one_line)"
  [ -n "$system_insets" ] || system_insets="unavailable"
  activity_dump="$(adb -s "$serial" shell dumpsys activity top 2>/dev/null || true)"
  app_configuration="$(printf '%s\n' "$activity_dump" | awk -v pkg="$package" '
    index($0, pkg) { in_app=1 }
    in_app && (/mLastReportedConfiguration=/ || /mOverrideConfiguration=/) { print; exit }
  ' | one_line)"
  [ -n "$app_configuration" ] || app_configuration="unavailable"
  theme_night_mode="app_configuration=$app_configuration;system=$night_mode"
  printf 'android\t%s\t%s.png\t%sx%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\tmatchup_state=%s\t%s\t%s\n' \
    "$state" "$state" "$width" "$height" "$hash" "$serial" "$model" "$os_version" "$api_level" "$display_id" "$display_size" "$density" "$app_bounds" "$system_insets" "$font_scale" "$locale" "$timezone" "$theme_night_mode" "$orientation" "$state" "$package_version" "$revision" >> "$manifest"
done

echo "Captured 12 states in $output"
