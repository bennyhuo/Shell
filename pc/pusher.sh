#!/bin/bash

echo $PATH
#取脚本所在目录
dir=$(cd `dirname $0`; pwd)

$dir/eadb push $dir/server.dex  /data/local/tmp
$dir/eadb push $dir/launcher.sh  /data/local/tmp
$dir/eadb push $dir/shellserver  /data/local/tmp

# 不输出 后台执行
# 注意，第一个 nohup ... &是为了不阻塞电脑；第二个 nohup 是为了不让 Android 在 USB 断开后杀掉这个进程。
# 第二个 nohup 如果后面加 &，oppo R9S 服务会在执行完命令立即关掉，奇怪
# >/dev/null 会卡住 Gradle，&>/dev/null Gradle 任务执行完后会退出（对于 Mac Terminal 这两个效果一样）
nohup $dir/eadb shell "nohup /data/local/tmp/launcher.sh > /dev/null 2>&1" &>/dev/null &
echo 'SUCCESSFUL'