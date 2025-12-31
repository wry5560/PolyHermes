---
name: prod-server
description: 生产服务器运维操作。部署应用、查看日志、重启服务、检查状态、数据库备份等。当用户提到"生产服务器"、"生产环境"、"部署"、"服务器状态"、"远程"、"prod"时使用此技能。
allowed-tools: Bash(ssh:*), Bash(scp:*), Read, Write
---

# 生产服务器运维 (Production Server Operations)

## 服务器信息

| 项目 | 值 |
|------|-----|
| **IP 地址** | 8.221.142.8 |
| **操作系统** | Ubuntu 24.04 LTS |
| **SSH 用户** | root |
| **SSH 密钥** | opt/AlphaQuant.pem |
| **应用端口** | 80 (HTTP), 443 (HTTPS) |

## SSH 连接命令

```bash
# 基础连接模板
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "<命令>"

# 交互式连接（不推荐在 Claude 中使用）
ssh -i opt/AlphaQuant.pem root@8.221.142.8
```

## 常用运维命令

### 1. 服务状态检查

```bash
# 检查系统状态
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "uptime && free -h && df -h"

# 检查 Docker 容器状态
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "docker ps -a"

# 检查应用健康状态
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "curl -s http://localhost/api/health"

# 检查端口监听
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "ss -tlnp | grep -E ':(80|443|8000|3306)'"
```

### 2. 日志查看

```bash
# 查看应用日志（最近 100 行）
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "docker logs --tail 100 polyhermes"

# 实时查看日志（需要 timeout 限制）
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "timeout 10 docker logs -f polyhermes"

# 查看 MySQL 日志
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "docker logs --tail 50 polyhermes-mysql"

# 查看系统日志
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "journalctl -n 50 --no-pager"
```

### 3. 服务管理

```bash
# 重启应用容器
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "cd /opt/polyhermes && docker-compose -f docker-compose.prod.yml restart app"

# 重启所有服务
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "cd /opt/polyhermes && docker-compose -f docker-compose.prod.yml restart"

# 停止服务
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "cd /opt/polyhermes && docker-compose -f docker-compose.prod.yml down"

# 启动服务
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "cd /opt/polyhermes && docker-compose -f docker-compose.prod.yml up -d"

# 重新拉取镜像并更新
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "cd /opt/polyhermes && docker-compose -f docker-compose.prod.yml pull && docker-compose -f docker-compose.prod.yml up -d"
```

### 4. 部署更新

```bash
# 方式1: 使用 Docker Hub 镜像更新
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "cd /opt/polyhermes && docker-compose -f docker-compose.prod.yml pull && docker-compose -f docker-compose.prod.yml up -d"

# 方式2: 上传本地构建的镜像
# 先本地构建
docker build -t polyhermes:latest .
docker save polyhermes:latest | gzip > /tmp/polyhermes.tar.gz
scp -i opt/AlphaQuant.pem /tmp/polyhermes.tar.gz root@8.221.142.8:/tmp/
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "gunzip -c /tmp/polyhermes.tar.gz | docker load && cd /opt/polyhermes && docker-compose -f docker-compose.prod.yml up -d"
```

### 5. 数据库操作

```bash
# 备份数据库
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "docker exec polyhermes-mysql mysqldump -u root -p\$(grep DB_PASSWORD /opt/polyhermes/.env | cut -d= -f2) polyhermes > /opt/polyhermes/backup_\$(date +%Y%m%d_%H%M%S).sql"

# 查看备份文件
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "ls -lh /opt/polyhermes/*.sql"

# 下载备份到本地
scp -i opt/AlphaQuant.pem root@8.221.142.8:/opt/polyhermes/backup_*.sql ./backups/

# 进入 MySQL 命令行
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "docker exec -it polyhermes-mysql mysql -u root -p polyhermes"
```

### 6. 资源监控

```bash
# 查看 CPU 和内存使用
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "top -bn1 | head -20"

# 查看磁盘使用
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "df -h && echo '---' && du -sh /opt/polyhermes/* 2>/dev/null"

# 查看 Docker 资源使用
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "docker stats --no-stream"

# 查看网络连接
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "ss -s"
```

### 7. 文件传输

```bash
# 上传文件到服务器
scp -i opt/AlphaQuant.pem <本地文件> root@8.221.142.8:<远程路径>

# 下载文件到本地
scp -i opt/AlphaQuant.pem root@8.221.142.8:<远程文件> <本地路径>

# 上传目录
scp -i opt/AlphaQuant.pem -r <本地目录> root@8.221.142.8:<远程路径>
```

### 8. 环境配置

```bash
# 查看环境变量配置（隐藏敏感信息）
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "cat /opt/polyhermes/.env | sed 's/=.*/=***/' "

# 编辑环境配置（需要交互，建议通过 scp 上传）
# 1. 下载配置
scp -i opt/AlphaQuant.pem root@8.221.142.8:/opt/polyhermes/.env ./temp_env
# 2. 本地编辑 temp_env
# 3. 上传配置
scp -i opt/AlphaQuant.pem ./temp_env root@8.221.142.8:/opt/polyhermes/.env
# 4. 重启服务
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "cd /opt/polyhermes && docker-compose -f docker-compose.prod.yml restart"
```

## 首次部署流程

```bash
# 1. 创建部署目录
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "mkdir -p /opt/polyhermes"

# 2. 上传配置文件
scp -i opt/AlphaQuant.pem docker-compose.prod.yml root@8.221.142.8:/opt/polyhermes/
scp -i opt/AlphaQuant.pem docker-compose.prod.env.example root@8.221.142.8:/opt/polyhermes/.env

# 3. 在服务器上编辑 .env 配置密钥（或本地编辑后上传）

# 4. 安装 Docker（如果没有）
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "curl -fsSL https://get.docker.com | sh"

# 5. 启动服务
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "cd /opt/polyhermes && docker-compose -f docker-compose.prod.yml up -d"

# 6. 检查状态
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "docker ps && curl -s http://localhost/api/health"
```

## 故障排查

### 应用无法访问
```bash
# 检查容器状态
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "docker ps -a"

# 检查容器日志
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "docker logs --tail 200 polyhermes"

# 检查端口
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "ss -tlnp | grep 80"

# 检查防火墙
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "ufw status"
```

### 数据库连接失败
```bash
# 检查 MySQL 容器
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "docker logs --tail 50 polyhermes-mysql"

# 测试数据库连接
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "docker exec polyhermes-mysql mysqladmin -u root -p ping"
```

### 磁盘空间不足
```bash
# 清理 Docker 缓存
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "docker system prune -af"

# 清理旧备份
ssh -i opt/AlphaQuant.pem root@8.221.142.8 "find /opt/polyhermes -name '*.sql' -mtime +7 -delete"
```

## 安全注意事项

1. SSH 密钥文件 `opt/AlphaQuant.pem` 已加入 `.gitignore`，不要提交到仓库
2. 不要在日志中输出密码或密钥
3. 定期轮换 JWT_SECRET 和其他密钥
4. 定期备份数据库
