# 部署说明

本项目使用 Docker Compose 部署测试环境，目标是独立目录、独立端口、独立 PostgreSQL、独立 Redis，不影响服务器上的其他产品。

## 服务端口

测试环境默认端口：

```text
API:   http://<server-ip>:18200
Admin: http://<server-ip>:18201
```

内部服务：

```text
PostgreSQL: 仅 Docker 网络内部 5432
Redis:      仅 Docker 网络内部 6379
```

不要暴露 PostgreSQL 和 Redis 到公网。

## 启动方式

在项目根目录执行：

```bash
docker compose -f deploy/docker-compose.yml up -d --build
```

查看服务：

```bash
docker compose -f deploy/docker-compose.yml ps
```

查看日志：

```bash
docker logs -f ball-saas-api
docker logs -f ball-saas-admin
```

停止本项目服务：

```bash
docker compose -f deploy/docker-compose.yml down
```

如需保留测试数据，不要删除 Docker volume。

## 环境变量

API 容器使用以下环境变量：

```text
PORT=18200
DB_URL=jdbc:postgresql://ball-saas-postgres:5432/ball_saas
DB_USERNAME=ball_saas
DB_PASSWORD=ball_saas_dev
REDIS_HOST=ball-saas-redis
REDIS_PORT=6379
```

生产环境应将数据库密码、微信支付密钥、证书路径等敏感配置迁移到安全的环境变量或密钥管理系统，不要提交到代码仓库。

## 数据库迁移

后端启动时由 Flyway 自动执行迁移。当前迁移包括：

```text
V1__init.sql
V2__match_share_payment.sql
V3__audit_log.sql
```

查看迁移状态：

```bash
docker exec ball-saas-postgres psql -U ball_saas -d ball_saas -c "select version, description, success from flyway_schema_history order by installed_rank;"
```


## 后台访问密码

管理后台 Nginx 已启用 HTTP Basic Auth。部署前需要在服务器或本地创建 `admin/.htpasswd`，该文件不会提交到 Git 仓库。

示例：

```bash
cd /path/to/ball-saas
printf 'demo:{PLAIN}your-password\n' > admin/.htpasswd
```

说明：

- `admin/.htpasswd` 会通过 Docker Compose 挂载到 `/etc/nginx/.htpasswd`。演示环境可使用 `{PLAIN}`，生产环境建议改用更强的访问控制或生成安全哈希。
- 没有该文件时，`ball-saas-admin` 容器会无法正常提供后台页面。
- `/` 页面访问需要 Basic Auth；`/api` 代理不启用 Basic Auth，避免与前端业务 Bearer token 冲突。
## 健康检查

```bash
curl http://127.0.0.1:18200/actuator/health
curl http://127.0.0.1:18201/
curl http://127.0.0.1:18201/api/admin/dashboard
```

## 服务器约束

- 不使用 80/443。
- 不停止、删除、覆盖非本项目容器。
- 不修改服务器全局 Docker 配置。
- 不复用宿主机已有 Redis。
- PostgreSQL 和 Redis 只在 Docker 内部网络使用。
- 保留 PostgreSQL volume，除非明确需要重建测试库。

## 正式小程序 HTTPS 入口

微信小程序正式环境不能直接使用 `http://<server-ip>:18200`。

正式链路建议：

```text
https://api.example.com:443
  -> HTTPS 网关 / Nginx / 云负载均衡
  -> http://<server-ip>:18200
```

如果当前业务服务器不能使用 80/443，可以使用独立 HTTPS 网关、云托管、API 网关或另一台反向代理服务器。证书申请建议使用 DNS 校验，避免依赖服务器 80 端口。

## 上线前必须补齐

- 备案域名
- HTTPS 证书
- 微信小程序合法域名
- 微信支付商户号
- API v3 密钥
- 商户证书/平台证书
- 支付和退款公网 HTTPS 回调地址
- 生产级后台账号、员工管理、菜单权限
- 微信开发者工具预览、编译、真机测试



