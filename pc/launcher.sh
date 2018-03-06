#!/system/bin/sh
echo "Start Server."
pkill -f shellserver
rootDir=/data/local/tmp
exec ${rootDir}/shellserver