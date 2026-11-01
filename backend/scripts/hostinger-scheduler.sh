#!/bin/sh
set -eu
umask 077
cd "$(dirname "$0")/.."

exec /usr/bin/flock -n -E 0 storage/scheduler.lock \
    /opt/alt/php84/usr/bin/php artisan schedule:run \
    >> storage/logs/scheduler-cron.log 2>&1
