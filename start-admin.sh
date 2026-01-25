#!/bin/bash

# 定义路径配置
ENV_FILE="/data/config/.env"
JAR_DIR="/data/msi-admin/backend"
JAR_NAME="admin-backend-0.0.1-SNAPSHOT.jar"
LOG_FILE="$JAR_DIR/app.log"
SERVER_PORT=8081

# 检查 .env 文件是否存在
if [ -f "$ENV_FILE" ]; then
    echo "📄 Loading environment variables from $ENV_FILE..."
    
    # 使用 export $(cat ... | xargs) 方式加载环境变量
    # 注意：如果 .env 中的值包含空格或特殊字符，可能会有问题，但这是用户指定的方式
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

# 切换到 Jar 包所在目录执行（确保生成日志在正确位置，或者相对路径资源能找到）
cd "$JAR_DIR"

# 启动 Java 应用
# 直接使用环境变量（上面 source 已经加载并导出），不再通过命令行参数传递
# 这样如果变量为空，Spring Boot 会使用 application.yml 中的默认值
nohup java -jar "$JAR_NAME" --server.port=$SERVER_PORT > "$LOG_FILE" 2>&1 &

PID=$!
echo "✅ Application started with PID: $PID"
echo "📝 Logs are being written to $LOG_FILE"
echo "👀 Tailing log file (Ctrl+C to stop viewing logs, application will keep running)..."
tail -f "$LOG_FILE"
