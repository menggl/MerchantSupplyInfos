#!/bin/bash

# 加载当前目录下的 .env 文件
if [ -f .env ]; then
  export $(cat .env | grep -v '^#' | xargs)
fi

# 停止旧的进程（可选，如果需要先杀掉旧进程再启动）
# pkill -f merchant-supply-infos-0.0.1-SNAPSHOT.jar

# 启动应用
nohup java -jar merchant-supply-infos-0.0.1-SNAPSHOT.jar > app.log 2>&1 &

# 等待一秒让日志文件创建
sleep 1

# 查看日志
tail -f app.log
