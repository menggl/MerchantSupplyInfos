#!/bin/bash

# ================= 配置部分 =================
SERVER_IP="8.141.125.139"
SERVER_USER="root"
# 注意：密码中包含特殊字符，必须使用单引号包围
SERVER_PASS='kshVmByD)<>4Aw+'

# 远程目录结构
REMOTE_BASE_DIR="/data/msi-backend"
REMOTE_CONFIG_DIR="/data/config"
REMOTE_SCRIPT_DIR="/root"
# ===========================================

# 获取脚本所在目录作为项目根目录
PROJECT_ROOT=$(pwd)
BACKEND_DIR="$PROJECT_ROOT/backend"

# 检查 sshpass 是否安装
if ! command -v sshpass &> /dev/null; then
    echo "❌ 错误: 未检测到 sshpass 工具。"
    echo "MacOS 请运行: brew install sshpass"
    echo "Linux 请运行: apt-get install sshpass 或 yum install sshpass"
    exit 1
fi

echo "🚀 开始 backend 项目自动化部署流程..."

# 1. 构建后端
echo ""
echo "📦 [1/3] 构建后端项目 (merchant-supply-infos)..."
cd "$BACKEND_DIR"
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ 后端构建失败！"
    exit 1
fi
echo "✅ 后端构建成功。"

# 2. 准备远程环境
echo ""
echo "☁️  [2/3] 连接服务器并创建目录..."
sshpass -p "$SERVER_PASS" ssh -o StrictHostKeyChecking=no "$SERVER_USER@$SERVER_IP" "mkdir -p $REMOTE_BASE_DIR/backend $REMOTE_CONFIG_DIR"

# 3. 上传文件
echo ""
echo "Bs  [3/3] 上传文件到服务器..."

echo "正在上传配置文件 (.env -> $REMOTE_CONFIG_DIR/.env)..."
# 直接上传本地 .env 文件 (使用绝对路径)
sshpass -p "$SERVER_PASS" scp -o StrictHostKeyChecking=no "$PROJECT_ROOT/.env" "$SERVER_USER@$SERVER_IP:$REMOTE_CONFIG_DIR/.env"

echo "正在上传启动脚本 (start-backend.sh -> $REMOTE_SCRIPT_DIR/start-backend.sh)..."
sshpass -p "$SERVER_PASS" scp -o StrictHostKeyChecking=no "$PROJECT_ROOT/start-backend.sh" "$SERVER_USER@$SERVER_IP:$REMOTE_SCRIPT_DIR/start-backend.sh"
# 赋予脚本执行权限
sshpass -p "$SERVER_PASS" ssh -o StrictHostKeyChecking=no "$SERVER_USER@$SERVER_IP" "chmod +x $REMOTE_SCRIPT_DIR/start-backend.sh"

echo "正在上传后端文件 (merchant-supply-infos jar -> $REMOTE_BASE_DIR/backend)..."
sshpass -p "$SERVER_PASS" scp -o StrictHostKeyChecking=no "$BACKEND_DIR/target/merchant-supply-infos-0.0.1-SNAPSHOT.jar" "$SERVER_USER@$SERVER_IP:$REMOTE_BASE_DIR/backend/"

echo ""
echo "🎉 部署完成！"
echo "服务器目录结构："
echo "  启动脚本: $REMOTE_SCRIPT_DIR/start-backend.sh"
echo "  配置文件: $REMOTE_CONFIG_DIR/.env"
echo "  后端:     $REMOTE_BASE_DIR/backend"

echo "你可以登录服务器执行: /root/start-backend.sh 来启动服务"
