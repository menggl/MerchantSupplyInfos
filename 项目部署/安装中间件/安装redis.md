### 安装redis
#### 更新系统
sudo yum update -y 

#### 安装 EPEL 仓库
sudo yum install epel-release -y

#### 安装 Redis
sudo yum install redis -y

#### 启动 Redis 服务
sudo systemctl stop redis
sudo systemctl start redis
sudo systemctl enable redis
sudo systemctl status redis

#### 测试 Redis
redis-cli ping

#### 编辑配置文件 
查看redis.conf地址
sudo find / -name "redis.conf"
vi /etc/redis.conf


##### 绑定 IP
默认是
bind 127.0.0.1
改成
bind 0.0.0.0
指定 网卡IP
bind 127.0.0.1 192.168.1.131
##### 保护模式
protected-mode no
##### 设置访问密码（推荐）
requirepass Root@N8Hz@u#1-e
##### 重启 Redis
sudo systemctl restart redis
##### 测试远程访问
redis-cli -h 192.168.31.156 -p 6379 -a Root@N8Hz@u#1-e ping