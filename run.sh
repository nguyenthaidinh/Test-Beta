#!/usr/bin/env bash
cd "$(dirname "$0")"

exec java -server \
-Xms512m -Xmx2g \
-Dfile.encoding=UTF-8 \
-cp "dist/NgocRongOnline.jar:lib/*" \
nro.models.server.ServerManager
