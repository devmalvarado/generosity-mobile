#!/bin/sh
set -eu
umask 077
cd "$(dirname "$0")/.."

# hPanel requires a script for output redirection; flock prevents overlapping workers.
exec /usr/bin/flock -n -E 0 storage/queue.lock \
    /opt/alt/php84/usr/bin/php artisan queue:work database \
    --stop-when-empty --max-time=45 --timeout=20 --tries=3 --backoff=60 \
    >> storage/logs/queue-cron.log 2>&1
