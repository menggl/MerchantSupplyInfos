### 安装mysql8
#### centos版本
```
cat /etc/redhat-release
CentOS Linux release 7.6.1810 (Core) 
```
#### 安装 MySQL 官方 yum 仓库
```
sudo yum install -y https://repo.mysql.com/mysql80-community-release-el7-11.noarch.rpm
```
安装完成后，会生成：
```
/etc/yum.repos.d/mysql-community.repo
```
#### 检查 MySQL repo 是否启用
```
yum repolist enabled | grep mysql
```
你应该能看到类似：
```
mysql80-community/x86_64
```
#### 安装 MySQL Server
```
sudo yum install -y mysql-community-server -y
```
#### 启动 MySQL & 初始密码
```
sudo systemctl start mysqld
sudo systemctl enable mysqld
```
查看初始 root 密码：
```
sudo grep 'temporary password' /var/log/mysqld.log
```
登录：
```
mysql -u root -p
```
#### 登录后修改密码
```
ALTER USER 'root'@'localhost' IDENTIFIED BY 'Root@jKF5k(FbvS';
```
修改远程登录密码
```
CREATE USER 'root'@'%' IDENTIFIED BY 'Root@YMhmO=9iA5';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;
```
防火墙
```
## 查看防火墙状态
sudo systemctl status firewalld
## 查看当前zone
sudo firewall-cmd --get-active-zones
## 查看public zone的端口
sudo firewall-cmd --zone=public --list-ports
## 开启3306端口
sudo firewall-cmd --zone=public --add-port=3306/tcp --permanent
sudo firewall-cmd --reload
```


#### 修改my.cnf配置
```
sudo vi /etc/my.cnf
```
添加如下配置：
```
[mysqld]
bind-address = 0.0.0.0
```
重启mysql服务
```
sudo systemctl restart mysqld
sudo systemctl status mysqld
```
