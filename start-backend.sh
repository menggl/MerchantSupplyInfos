#!/bin/bash

# 定义路径配置
ENV_FILE="/data/config/.env"
JAR_DIR="/data/msi-backend/backend"
JAR_NAME="merchant-supply-infos-0.0.1-SNAPSHOT.jar"
LOG_FILE="$JAR_DIR/app.log"
SERVER_PORT=8080

# 检查 .env 文件是否存在
if [ -f "$ENV_FILE" ]; then
    echo "📄 Loading environment variables from $ENV_FILE..."
    
    # 使用 export $(cat ... | xargs) 方式加载环境变量
    export $(cat "$ENV_FILE" | grep -v '^#' | xargs)
    
    echo "🔍 Debug: MYSQL_URL is set to: $MYSQL_URL"
else
    echo "❌ Error: Configuration file $ENV_FILE not found!"
    exit 1
fi

# 检查 jar 包是否存在
if [ ! -f "$JAR_DIR/$JAR_NAME" ]; then
    echo "❌ Error: Jar file not found at $JAR_DIR/$JAR_NAME"
    exit 1
fi

# 检查并停止旧进程
echo "🔍 Checking for existing process..."
EXISTING_PID=$(pgrep -f "$JAR_NAME")
if [ -n "$EXISTING_PID" ]; then
    echo "🛑 Found existing process (PID: $EXISTING_PID). Killing it..."
    kill -9 $EXISTING_PID
    echo "✅ Process killed."
else
    echo "ℹ️ No existing process found."
fi

echo "🚀 Starting $JAR_NAME on port $SERVER_PORT..."

# 切换到 Jar 包所在目录执行
cd "$JAR_DIR"

# 启动 Java 应用
nohup java -jar "$JAR_NAME" --server.port=$SERVER_PORT > "$LOG_FILE" 2>&1 &

PID=$!
echo "✅ Application started with PID: $PID"
echo "📝 Logs are being written to $LOG_FILE"
echo "👀 Tailing log file (Ctrl+C to stop viewing logs, application will keep running)..."
tail -f "$LOG_FILE"
