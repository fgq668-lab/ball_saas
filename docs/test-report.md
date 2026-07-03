# 场馆预约与球员约战 SaaS 系统测试报告

## 1. 测试时间

2026-06-26

## 2. 测试环境

本地工作目录：

```text
D:\develop\product\ball_project
```

已启用项目私有工具链，不修改系统默认 Java：

```text
Java: Eclipse Temurin 21.0.11，路径 .tools/jdk21/jdk-21.0.11+10
Maven: Apache Maven 3.9.9，路径 .tools/apache-maven-3.9.9
Maven 本地仓库: .m2/repository
Node.js / npm: 已用于管理后台构建
npm 缓存: admin/.npm-cache
```

说明：本机系统默认 Java 仍为 Java 8，且系统未安装 Maven。后端编译测试通过项目内 `.tools` 工具链完成，避免影响本机其他项目。

## 3. 代码版本

当前目录不是可用 Git 仓库，`git status` 无法读取提交信息。后续应初始化或修复 Git 仓库后补充提交号。

## 4. 当前实现范围

已创建工程结构：

```text
backend
admin
miniprogram
deploy
docs
scripts
```

已实现后端骨架：

- Spring Boot 3 Maven 项目配置
- 应用入口
- 统一 API 响应
- 全局异常处理
- Spring Security 基础配置
- 健康检查接口
- PostgreSQL / Redis 配置
- Flyway 初始化脚本

已实现后端领域模型：

- 场馆
- 场地
- 价格规则
- 预约订单
- 30 分钟场地时间槽
- 支付单
- 支付回调日志
- 退款单
- 约战房间
- 约战成员
- 约战 AA 支付记录
- 约战 AA 退款记录

已实现 P0/P1 主链路雏形：

- 创建场馆、审核场馆
- 创建场地、创建价格规则
- 创建预约订单
- Redis 临时锁场
- PostgreSQL 时间槽防重
- 创建支付单
- 模拟支付成功
- 支付成功后确认预约并写入时间槽
- 到场核销接口
- 退款申请、超额退款防护、全额退款释放未开始场地时间槽
- 基于已预约订单创建约战
- 成员加入约战
- 约战 AA 支付单创建
- AA 支付成功后达到人数自动成局
- 未成局自动取消与退款记录雏形

已创建前端骨架：

- Vue 3 + Element Plus 管理后台基础布局
- 微信小程序基础页面结构
- 清爽运动服务型样式基线

已创建部署配置：

- 后端 Dockerfile
- 管理后台 Dockerfile
- Docker Compose
- PostgreSQL 容器
- Redis 容器
- API 端口 `18200`
- Admin 端口 `18201`

已创建构建脚本：

- `scripts/backend-test.ps1`
- `scripts/admin-build.ps1`

## 5. 已执行测试

### 5.1 文档和结构检查

结果：通过。

检查项：

- 目标文档存在。
- `goal-prompt.md` 存在。
- 后端、后台、小程序、部署目录已创建。
- 未引入 RuoYi / 若依。
- 测试端口保持为 `18200` / `18201`。
- 小程序正式上线要求仍明确为备案域名 + HTTPS，不能直接使用 `1.69.143.156:18200`。

### 5.2 JSON 静态检查

执行结果：

```text
json-ok
```

覆盖文件：

- `admin/package.json`
- `admin/tsconfig.json`
- `miniprogram/app.json`
- `miniprogram/project.config.json`

结果：通过。

### 5.3 后端 Maven 编译与单元测试

执行命令：

```powershell
.\scripts\backend-test.ps1
```

结果：通过。

关键输出：

```text
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

覆盖测试：

- `PriceServiceTest.calculatesByThirtyMinuteSlots`
- `PriceServiceTest.rejectsNonThirtyMinuteDuration`
- `PriceServiceTest.rejectsMissingPriceRule`

说明：

- 后端已使用 Java 21 编译通过。
- 当前测试覆盖价格计算核心边界，仍需继续补充预约并发、支付幂等、退款幂等和约战成局集成测试。

### 5.4 管理后台构建

执行命令：

```powershell
.\scripts\admin-build.ps1
```

结果：通过。

说明：

- Vite 生产构建成功。
- 存在 chunk size warning，不阻塞当前验收，后续可通过按路由拆包优化。
- 首次 `npm install` 需使用项目内缓存：`npm install --cache .\.npm-cache`，避免写入系统 npm cache 目录。

## 6. 尚未完成测试

以下测试尚未完成，原因是本机 Docker daemon 未启动，无法拉起 PostgreSQL / Redis / Testcontainers：

- Spring Boot 连接 PostgreSQL 启动测试
- Flyway 迁移落库测试
- Redis 锁场集成测试
- 支付回调幂等测试
- 退款幂等测试
- 并发预约测试
- 约战未成局自动退款集成测试

以下测试尚未完成，原因是尚未使用微信开发者工具：

- 小程序开发者工具编译预览
- 小程序真机调试
- 小程序合法域名和 HTTPS 联调

以下测试尚未完成，原因是尚未部署到服务器：

- `1.69.143.156:18200` 健康检查
- `1.69.143.156:18201` 后台访问
- 不影响服务器其他产品验证
- 备案域名 + HTTPS 网关转发验证

## 7. 风险项

- Docker daemon 当前未运行，导致数据库/Redis 集成测试和 Docker Compose 部署验证尚未执行。
- 微信支付真实服务商模式尚未接入，当前仅提供模拟支付成功接口用于开发验证。
- 微信退款真实接口和回调幂等处理尚未完成。
- 微信小程序正式上线所需的备案域名、HTTPS 证书、合法域名配置和公网回调地址尚未完成。
- 管理后台和小程序已有部分接口接入，但尚未完成全量页面联调。
- 当前权限体系仍为开发期 mock token，生产前必须补充真实登录态、角色权限和场馆数据隔离验证。
- 报表统计仍需按场馆维度严格过滤，避免 SaaS 多租户数据串场。

## 8. 当前验收结论

当前状态：未达到 `goal-prompt.md` 的最终完成标准，但“后端编译受 Java/Maven 环境限制”的问题已解决。

已完成的重要修正：

1. 下载并启用项目私有 Java 21 + Maven，不修改系统默认 Java。
2. 新增可重复执行的后端测试脚本和后台构建脚本。
3. 后端 Maven 编译通过。
4. 后端价格计算单元测试 3 项通过。
5. 管理后台生产构建通过。
6. `.gitignore` 已忽略 `.tools`、`.m2`、npm 缓存和构建产物。

下一步必须完成：

1. 继续补充后端业务测试，优先覆盖预约并发、支付幂等、退款幂等、约战 AA 支付和未成局退款。
2. 解决 Docker daemon 或改用可控 PostgreSQL / Redis 实例，执行集成测试。
3. 完成后台与小程序全量接口联调。
4. 部署到 `1.69.143.156` 的非 80/443 测试端口，并验证不影响服务器其他产品。
5. 域名、备案、HTTPS 完成后，再进行小程序正式上线联调。
## 9. 服务器测试部署记录

测试时间：2026-06-28

测试服务器：

```text
1.69.143.156
SSH: 2218
用户: frank
```

部署目录：

```text
/home/frank/apps/ball-saas
```

已启动容器：

```text
ball-saas-postgres: PostgreSQL 16，仅 Docker 内部网络，不暴露宿主机 5432
ball-saas-redis: Redis 7，仅 Docker 内部网络，不暴露宿主机 6379
ball-saas-api: 宿主机 18200 -> 容器 18200
ball-saas-admin: 宿主机 18201 -> 容器 80
```

环境补齐情况：

- Docker Hub 访问超时，已改用可达镜像源拉取基础镜像，再在服务器本地打官方镜像标签。
- 已补齐 `postgres:16`、`redis:7`、`maven:3.9.9-eclipse-temurin-21`、`eclipse-temurin:21-jre`、`node:20-alpine`、`nginx:1.27-alpine`。
- Maven Central 和 npm registry 在服务器可访问。
- 未修改服务器全局 Docker daemon 配置。
- 未复用宿主机已有 Redis，测试系统使用独立 Redis 容器。

已执行自测：

- `http://1.69.143.156:18200/actuator/health` 返回 `UP`。
- `http://1.69.143.156:18201/` 返回管理后台 HTML。
- `http://1.69.143.156:18201/api/admin/dashboard` 可通过后台 Nginx 代理访问后端。
- Flyway 成功执行 2 个数据库迁移，数据库版本到 `v2`。
- 创建场馆成功。
- 审核场馆成功。
- 创建场地成功。
- 创建价格规则成功。
- 创建预约订单成功。
- 创建支付单成功。
- 模拟支付成功后，预约订单状态变为 `RESERVED`。
- `court_time_slot` 写入 2 条 30 分钟时间槽。
- Redis 临时锁场 key 存在，并设置 TTL 自动过期。
- 重复创建同一未支付时间段预约返回 400，锁场生效。

部署自测发现的问题：

- 管理后台生产 Nginx 原本缺少 `/api` 代理，已补充 `admin/nginx.conf` 并更新 Dockerfile。
- `ReportService` 当前只统计订单数量，金额字段仍返回 0，属于业务实现遗留，不是部署环境问题。
- 后端日志中出现 Spring Data Redis repository 扫描提示，当前不影响运行，但后续可通过配置 repository 扫描范围降低噪音。
- 当前仍是 HTTP 测试环境，不能作为微信小程序正式上线入口。

当前服务器部署结论：

```text
测试部署成功。
API、后台、PostgreSQL、Redis 均已运行。
基础业务链路自测通过。
未发现占用 80/443，未发现暴露本项目 PostgreSQL/Redis 到宿主机公网端口。
```
## 10. 下一阶段开发与回归记录

测试时间：2026-06-28

本轮完成内容：

- 报表金额统计从占位 `0` 改为按当天订单状态真实汇总。
- 场馆维度报表按 `venue_id` 汇总交易金额、退款金额和待处理退款。
- 新增订单列表、订单详情、用户取消待支付订单接口。
- 新增支付超时关闭任务实现，已支付订单不会被超时任务关闭。
- 模拟支付成功增加 `payment_notify_log` 记录。
- 重复模拟支付会记录 `DUPLICATE`，不会重复确认预约或重复写入时间槽。
- 新增 mock token 结构和解析能力。
- 场馆后台接口接入场馆数据隔离：场地、价格、订单、退款、场馆报表。
- 新增约战列表、约战详情、AA 支付状态列表接口。
- 管理后台从单一总览页扩展为核心业务工作台：登录、平台/场馆总览、场馆审核、场地价格、预约订单、退款管理、约战管理。
- 小程序订单列表、订单详情、约战列表、约战详情接入测试 API。
- 小程序 API 默认指向测试服务器，并保留正式 HTTPS 域名切换位置。

本地测试：

```text
后端测试：通过
Tests run: 9, Failures: 0, Errors: 0, Skipped: 0

管理后台构建：通过
Vite build success，仍存在 chunk size warning，不阻塞测试验收。

JSON 静态检查：通过
admin/package.json
admin/tsconfig.json
miniprogram/app.json
miniprogram/project.config.json
```

服务器回归：

```text
API: http://1.69.143.156:18200
管理后台: http://1.69.143.156:18201
容器: ball-saas-api / ball-saas-admin / ball-saas-postgres / ball-saas-redis 均运行中
```

已验证：

- `/actuator/health` 返回 200。
- 管理后台首页返回 200。
- Flyway 校验通过，数据库 schema 维持 v2。
- 定时任务已关闭 1 笔超时待支付订单。
- 场馆管理员 token 可正常获取。
- 场馆管理员用 `X-Venue-Id=2` 访问 venue 1 token 范围外数据返回 403。
- 新建预约并模拟支付成功后，支付状态为 `PAID`。
- 同一支付单重复模拟支付，第二次仍返回 `PAID`，但不会重复写时间槽。
- 新订单 60 分钟预约只写入 2 条 30 分钟时间槽。
- `payment_notify_log` 对同一支付单记录 1 条 `SUCCESS` 和 1 条 `DUPLICATE`。
- 平台今日交易金额和场馆今日交易金额已返回真实金额 `12000` 分，不再是占位 0。

仍未完成：

- 真实微信支付、退款、验签和公网 HTTPS 回调尚未完成。
- 并发预约 10 用户压测尚未自动化执行。
- 退款幂等、全额退款释放时间槽、部分退款不释放时间槽还需要补自动化测试和服务器回归。
- 约战未成局自动退款仍是记录型模拟退款，未接真实微信退款。
- 小程序尚未使用微信开发者工具做真机预览和真机调试。
- 后端仍存在 Spring Data Redis repository 扫描提示，不影响运行，但需要优化配置降低日志噪音。
- 当前后台是测试可操作工作台，基础审计日志已补；生产级菜单权限和账号管理仍需继续完善。

当前结论：

```text
本阶段继续向 P0/P1 验收推进。
服务器测试环境已完成本轮部署回归。
报表金额、支付幂等日志、场馆数据隔离、后台核心工作台、小程序订单/约战页面已有实质进展。
但尚未达到 goal-prompt-next.md 的最终完成标准。
```
## 11. 并发与退款自动化回归记录

测试时间：2026-06-28

本轮新增自动化内容：

- 新增 `scripts/server-smoke.ps1`，用于服务器测试环境自动化冒烟。
- 新增 `RefundServiceTest`，覆盖退款核心规则。

本地后端测试结果：

```text
Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

新增单元测试覆盖：

- 未来预约全额退款后释放时间槽。
- 部分退款不释放时间槽。
- 超额退款被拒绝。

服务器自动化冒烟执行命令：

```powershell
.\scripts\server-smoke.ps1
```

服务器冒烟结果：

```json
{
  "ConcurrentAttempts": 10,
  "ConcurrentSuccess": 1,
  "SixtyMinuteBookingId": 4,
  "NinetyMinuteBookingId": 5,
  "FullRefundId": 1,
  "PartialRefundId": 2,
  "VenueTodayOrders": 3,
  "VenueTodayAmountCent": 21000,
  "VenueTodayRefundCent": 9000
}
```

数据库验证：

```text
booking_order_id | slots
5                | 3
6                | 2
```

说明：

- 订单 4 为 60 分钟预约，全额退款后状态为 `REFUNDED`，时间槽已释放，因此不再出现在 `court_time_slot` 查询结果中。
- 订单 5 为 90 分钟预约，仍为 `RESERVED`，保留 3 个 30 分钟时间槽。
- 订单 6 为 60 分钟预约，部分退款后状态为 `PARTIALLY_REFUNDED`，仍保留 2 个 30 分钟时间槽。
- 10 个用户并发预约同一场馆同一场地同一时间段，仅 1 个成功，Redis 锁场生效。

当前新增通过项：

- 并发预约测试：通过。
- 60 分钟订单写 2 个时间槽：通过。
- 90 分钟订单写 3 个时间槽：通过。
- 全额退款释放时间槽：通过。
- 部分退款不释放时间槽：通过。
- 超额退款防护：本地单元测试通过。

仍未完成：

- 真实微信支付/退款接口和验签仍未接入。
- 退款回调仍为模拟同步成功模型，尚未形成真实异步回调链路。
- 小程序仍未做微信开发者工具编译和真机调试。
- 基础审计日志已补；生产级账号、密码和权限菜单仍需继续完善。
## 12. 退款回调幂等与服务器部署回归记录

测试时间：2026-06-28

本轮新增后端能力：

- 新增退款单号查询能力：`RefundOrderRepository.findByRefundNo`。
- 新增模拟退款成功回调接口：`POST /api/pay/mock/refund/success/{refundNo}`。
- 新增微信退款通知占位接口：`POST /api/pay/wechat/notify/refund`。
- 退款创建时会记录 `MOCK / REFUND_SUCCESS / SUCCESS` 通知日志。
- 同一退款单重复成功回调时只记录 `DUPLICATE`，不重复处理支付单、预约订单和时间槽。
- 微信退款通知占位入口当前只做模拟解析和幂等日志，不包含真实微信验签。

本地后端测试结果：

```text
Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

新增单元测试覆盖：

- 重复退款成功回调只写 `DUPLICATE` 通知日志。
- 重复退款成功回调不会重复保存支付单。
- 重复退款成功回调不会重复释放时间槽。

服务器部署结果：

```text
API: http://1.69.143.156:18200
管理后台: http://1.69.143.156:18201
容器: ball-saas-api / ball-saas-admin / ball-saas-postgres / ball-saas-redis 均运行中
健康接口: /actuator/health 返回 200, UP
管理后台首页: 返回 200
```

服务器业务冒烟结果：

```json
{
  "VenueId": 3,
  "CourtId": 3,
  "ConcurrentAttempts": 10,
  "ConcurrentSuccess": 1,
  "SixtyMinuteBookingId": 7,
  "NinetyMinuteBookingId": 8,
  "FullRefundId": 3,
  "PartialRefundId": 4,
  "VenueTodayOrders": 3,
  "VenueTodayAmountCent": 21000,
  "VenueTodayRefundCent": 9000
}
```

退款回调幂等服务器验证：

```text
退款单号: R1782613700758
POST /api/pay/mock/refund/success/R1782613700758 连续调用 2 次，均返回 REFUNDED。
POST /api/pay/wechat/notify/refund 携带该退款单号调用 1 次，返回 mock-refund-notify-accepted。
```

通知日志数据库验证：

```text
channel | notify_type    | process_status | count
MOCK    | REFUND_SUCCESS | SUCCESS        | 1
MOCK    | REFUND_SUCCESS | DUPLICATE      | 2
WECHAT  | REFUND_NOTIFY  | DUPLICATE      | 1
```

当前新增通过项：

- 退款创建成功日志：通过。
- 退款重复回调幂等：通过。
- 微信退款通知占位入口日志落库：通过。
- 部署后容器状态检查：通过。
- API 健康检查：通过。
- 管理后台页面访问检查：通过。
- 服务器并发预约冒烟：通过。
- 服务器全额退款释放时间槽、部分退款不释放时间槽冒烟：通过。

仍未完成：

- 真实微信支付/退款验签、证书、回调解密和商户配置尚未接入。
- 小程序尚未用微信开发者工具做真机预览和真机调试。
- 约战未成局自动取消、AA 支付退款链路仍需继续完善和验证。
- 基础审计日志已补；生产级账号密码和菜单权限仍需继续完善。
- 当前测试环境仍不是正式小程序上线环境；正式上线仍需要备案域名、HTTPS 入口、微信合法域名和支付/退款公网 HTTPS 回调地址。

## 13. 约战 P1 状态流转回归记录

测试时间：2026-06-28

本轮新增后端能力：

- 新增约战成员查询接口：`GET /api/app/matches/{matchId}/players`。
- 新增成员退出接口：`POST /api/app/matches/{matchId}/leave`。
- 新增未成局取消接口：`POST /api/app/matches/{matchId}/cancel-not-formed`。
- 新增约战 AA 退款记录查询接口：`GET /api/app/matches/{matchId}/share-refunds`。
- AA 支付单创建响应新增 `paymentNo`，便于小程序/测试端继续发起模拟支付。
- AA 支付成功后，成员状态会更新为 `PAID`。
- 约战成局人数计算包含已支付原预约的创建者。
- 未成局取消时，已支付 AA 单会转为 `REFUNDED`，并生成 `match_share_refund` 记录。
- 成员退出时，如存在已支付 AA 单，会生成 AA 退款记录并回退当前人数。

本地后端测试结果：

```text
Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

新增单元测试覆盖：

- 未成局取消会为已支付 AA 单生成退款记录。
- 成员退出会为已支付 AA 单生成退款记录。
- AA 支付成功后，当“创建者 + 已支付 AA 成员”达到最低人数，约战自动成局。

服务器部署结果：

```text
API 健康接口 /actuator/health 返回 200, UP
ball-saas-api / ball-saas-admin / ball-saas-postgres / ball-saas-redis 均已重新部署或保持运行
未使用 80/443
未停止或覆盖非本项目容器
```

服务器约战回归脚本：

```powershell
.\scripts\server-match-smoke.ps1
```

服务器约战回归结果：

```json
{
  "VenueId": 4,
  "CourtId": 4,
  "FormedMatchId": 1,
  "FormedMatchStatus": "FORMED",
  "PaidPlayerStatus": "PAID",
  "PaidShareStatus": "PAID",
  "CancelledMatchId": 2,
  "BeforeCancelStatus": "RECRUITING",
  "CancelledMatchStatus": "NOT_FORMED_CANCELLED",
  "CancelledShareStatus": "REFUNDED",
  "ShareRefundCount": 1
}
```

当前新增通过项：

- 基于已支付预约创建约战：通过。
- 成员加入约战：通过。
- AA 支付单创建并返回 `paymentNo`：通过。
- AA 支付成功后成员状态更新：通过。
- 达到最低人数自动成局：通过。
- 未成局取消：通过。
- 未成局自动生成 AA 退款记录：通过。
- AA 支付状态和退款记录可查询：通过。

仍未完成：

- 约战 AA 退款目前是系统内记录型模拟退款，尚未接入真实微信退款。
- 约战定时未成局自动取消任务已完成服务器回归；AA 退款仍为系统内模拟退款记录。
- 小程序约战创建、AA 支付、取消/退款记录已完成源码接入和结构检查；仍未做微信开发者工具预览和真机调试。
- 管理后台约战详情中的成员/AA 支付/退款记录展示仍需继续增强。

## 14. 约战未成局自动取消回归记录

测试时间：2026-06-28

本轮修正：

- 修正 `MatchLifecycleService.cancelUnformedMatches` 的成局人数判断：创建者已支付原预约，因此自动取消判断中计入创建者。
- 自动取消扫描范围从仅 `RECRUITING` 扩展为 `RECRUITING` 和 `FULL`。
- 自动取消生成的 `match_share_refund` 记录状态修正为 `REFUNDED`。
- 新增 `scripts/server-match-auto-cancel-smoke.ps1`，用于服务器验证定时自动取消。

本地后端测试结果：

```text
Tests run: 18, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

新增单元测试覆盖：

- 已过开场时间、未达到最低人数的约战会自动取消。
- 自动取消会把已支付 AA 单改为 `REFUNDED`。
- 自动取消会生成 `REFUNDED` 状态的 AA 退款记录。
- “创建者 + 已支付 AA 成员”达到最低人数时不会被自动取消。

服务器自动取消验证脚本：

```powershell
.\scripts\server-match-auto-cancel-smoke.ps1
```

服务器自动取消验证结果：

```json
{
  "VenueId": 7,
  "CourtId": 7,
  "MatchId": 5,
  "BeforeStatus": "RECRUITING",
  "AfterStatus": "NOT_FORMED_CANCELLED",
  "ShareStatus": "REFUNDED",
  "ShareRefundCount": 1,
  "WaitSeconds": 75
}
```

当前新增通过项：

- 约战未成局定时自动取消：通过。
- 自动取消后 AA 支付单退款状态：通过。
- 自动取消后 AA 退款记录生成：通过。
- 自动取消人数判断包含创建者：本地单元测试通过。

仍未完成：

- 自动取消生成的 AA 退款仍是系统内模拟退款记录，尚未调用真实微信退款。
- 管理后台已补充约战成员、AA 支付、AA 退款记录展示；小程序仍需继续完善。


## 15. 管理后台约战详情回归记录

测试时间：2026-06-28

本轮新增管理后台能力：

- 约战列表新增“详情”操作。
- 约战详情抽屉展示基础信息：约战号、状态、人数、AA 金额。
- 约战详情抽屉展示成员列表和成员状态。
- 约战详情抽屉展示 AA 支付单、支付单号、金额、状态。
- 约战详情抽屉展示 AA 退款记录、金额、状态。

本地管理后台构建结果：

```text
npm run build: 通过
vite build: 通过
```

服务器部署验证：

```text
管理后台：http://1.69.143.156:18201 返回 200
API 健康接口：http://1.69.143.156:18200/actuator/health 返回 200, UP
ball-saas-admin 已重新构建并启动
ball-saas-api 保持运行
```

说明：

- 本轮只增强管理后台展示能力，未新增数据库迁移。
- 构建过程中仍有 Element Plus/Vue 依赖包体积提示，不影响当前测试环境运行。
- 约战详情数据来自已通过服务器回归的接口：成员、AA 支付、AA 退款。

当前新增通过项：

- 管理后台约战详情构建：通过。
- 管理后台约战详情部署：通过。
- 管理后台页面访问：通过。

仍未完成：

- 管理后台还需要进一步增加约战筛选、详情操作确认、退款处理说明和生产级权限菜单。
- 小程序约战创建、AA 支付、取消/退款记录展示已完成源码接入和结构检查；仍未做微信开发者工具预览和真机调试。


## 16. 小程序约战与退款页面结构回归记录

测试时间：2026-06-28

本轮新增小程序能力：

- 约战列表页新增“发起约战”表单，可基于已支付预约订单 ID 创建约战。
- 订单详情页新增“发起约战”入口，可携带预约订单 ID 跳转到约战页。
- 约战详情页补齐成员列表展示。
- 约战详情页补齐 AA 支付单展示。
- 约战详情页补齐 AA 退款记录展示。
- 约战详情页新增加入约战、退出约战、创建 AA 支付单、模拟支付 AA、取消未成局操作。
- 退款申请页从占位改为可提交退款申请，支持输入支付单 ID、场馆 ID、退款金额和退款原因。
- 小程序请求封装支持业务失败消息透传，便于页面展示后端错误原因。

小程序结构检查结果：

```text
miniprogram json/js/page-structure check passed. json=12, js=12, pages=10
```

检查覆盖：

- `app.json` 可解析。
- 所有页面 JSON 可解析。
- 所有小程序 JS 文件通过 `node --check` 语法检查。
- `app.json` 中声明的 10 个页面均具备 `.js/.json/.wxml/.wxss` 文件。

已由服务器接口回归覆盖的小程序调用链路：

- 创建约战：`POST /api/app/matches`。
- 加入约战：`POST /api/app/matches/{matchId}/join`。
- 查询约战详情：`GET /api/app/matches/{matchId}`。
- 查询约战成员：`GET /api/app/matches/{matchId}/players`。
- 创建 AA 支付单：`POST /api/app/matches/{matchId}/share-payments`。
- 模拟 AA 支付：`POST /api/pay/mock/success/{paymentNo}`。
- 查询 AA 支付单：`GET /api/app/matches/{matchId}/share-payments`。
- 未成局取消：`POST /api/app/matches/{matchId}/cancel-not-formed`。
- 查询 AA 退款记录：`GET /api/app/matches/{matchId}/share-refunds`。
- 提交退款申请：`POST /api/app/refunds`。

说明：

- 本轮完成的是小程序源码层面的页面结构和接口接入，尚未使用微信开发者工具做预览、编译和真机调试。
- 当前小程序仍使用测试 API：`http://1.69.143.156:18200`。
- 正式版小程序仍必须切换到备案域名 + HTTPS，并配置微信合法域名。
- 退款申请页已支持从订单详情自动带入支付单 ID、场馆 ID 和金额；也保留手动输入能力用于测试。

当前新增通过项：

- 小程序约战创建页面结构：通过源码检查。
- 小程序约战详情页面结构：通过源码检查。
- 小程序 AA 支付与退款记录展示页面结构：通过源码检查。
- 小程序退款申请页面结构：通过源码检查。
- 小程序页面文件完整性：通过。

仍未完成：

- 微信开发者工具预览/编译未执行。
- 小程序真机调试未执行。
- 正式 HTTPS 域名、微信合法域名、真实微信支付/退款仍未配置。


## 17. 订单支付单关联与小程序退款入口回归记录

测试时间：2026-06-28

本轮新增能力：

- 后端订单响应新增支付单关联字段：`paymentOrderId`、`paymentNo`、`paymentStatus`。
- App 端订单列表/详情接口会返回最近一条预约支付单信息。
- 场馆后台订单列表/详情接口会返回最近一条预约支付单信息。
- 小程序订单详情页在已支付订单下自动携带支付单 ID、场馆 ID、订单金额进入退款页。
- 小程序退款页不再只能手动输入；从订单详情进入时可自动填充主要退款参数。
- 管理后台订单类型同步增加支付单字段定义。

本地验证结果：

```text
后端测试：Tests run: 18, Failures: 0, Errors: 0, Skipped: 0
管理后台构建：vite build 通过
小程序结构检查：miniprogram json/js/page-structure check passed. json=12, js=12, pages=10
```

服务器部署验证：

```text
ball-saas-api 已重新构建并启动
ball-saas-admin 已重新构建并启动
PostgreSQL/Redis 未暴露公网端口
未使用 80/443
未停止或覆盖非本项目容器
```

服务器订单支付单关联验证：

```json
{
  "VenueId": 8,
  "BookingId": 15,
  "PaymentOrderId": 19,
  "PaymentNo": "P1782617246054",
  "PaymentStatus": "PAID"
}
```

当前新增通过项：

- 订单详情返回支付单 ID：通过。
- 订单详情返回支付单号：通过。
- 订单详情返回支付状态：通过。
- 小程序退款入口自动带参源码检查：通过。
- 后端、后台、小程序结构回归：通过。

仍未完成：

- 微信开发者工具预览/编译未执行。
- 小程序真机调试未执行。
- 真实微信支付/退款、验签、HTTPS 回调仍未配置。


## 18. 支付成功与订单超时边界回归记录

测试时间：2026-06-28

本轮修正：

- 调整模拟支付成功处理顺序：预约类支付会先确认预约订单仍可支付，再将支付单标记为 `PAID`。
- 当预约订单已经超时、取消或处于其他不可确认支付状态时，模拟支付会失败返回，不会把支付单错误改为已支付。
- 保留重复支付成功回调幂等：已支付支付单重复通知仍记录 `DUPLICATE`，不会重复确认预约。

本地验证结果：

```text
后端测试：Tests run: 22, Failures: 0, Errors: 0, Skipped: 0
管理后台构建：vite build 通过
小程序结构检查：miniprogram json/js/page-structure check passed. json=12, js=12, pages=10
```

新增单元测试覆盖：

- 超时关闭任务只扫描 `PENDING_PAYMENT` 预约订单。
- 已超时预约调用 `confirmPaid` 会被拒绝，且不会写时间槽。
- 已预约订单重复 `confirmPaid` 保持幂等，不重复写时间槽。
- 预约确认失败时，模拟支付不会把支付单标记为 `PAID`。

服务器部署验证：

```text
ball-saas-api 已重新构建并启动
ball-saas-admin / ball-saas-postgres / ball-saas-redis 保持运行
API 健康接口 /actuator/health 返回 200, UP
管理后台首页返回 200
管理后台 /api/admin/dashboard 代理返回 200
未使用 80/443
未停止或覆盖非本项目容器
```

服务器核心冒烟结果：

```json
{
  "VenueId": 9,
  "CourtId": 9,
  "ConcurrentAttempts": 10,
  "ConcurrentSuccess": 1,
  "SixtyMinuteBookingId": 16,
  "NinetyMinuteBookingId": 17,
  "FullRefundId": 5,
  "PartialRefundId": 6,
  "VenueTodayOrders": 3,
  "VenueTodayAmountCent": 21000,
  "VenueTodayRefundCent": 9000
}
```

服务器支付超时边界验证：

```json
{
  "VenueId": 11,
  "CourtId": 11,
  "BookingId": 20,
  "PaymentNo": "P1782618938947",
  "MockPayRejected": true,
  "ErrorMessage": "当前订单状态不能确认支付",
  "BookingStatus": "PAYMENT_TIMEOUT",
  "PaymentStatus": "PENDING_PAYMENT"
}
```

服务器约战支付回归结果：

```json
{
  "VenueId": 12,
  "CourtId": 12,
  "FormedMatchId": 6,
  "FormedMatchStatus": "FORMED",
  "PaidPlayerStatus": "PAID",
  "PaidShareStatus": "PAID",
  "CancelledMatchId": 7,
  "BeforeCancelStatus": "RECRUITING",
  "CancelledMatchStatus": "NOT_FORMED_CANCELLED",
  "CancelledShareStatus": "REFUNDED",
  "ShareRefundCount": 1
}
```

当前新增通过项：

- 已支付订单不会被超时关闭任务扫描：单元测试通过。
- 已超时预约不能再被支付成功改为已预约：单元测试和服务器验证通过。
- 预约确认失败时支付单不误置为 `PAID`：单元测试和服务器验证通过。
- 支付服务调整后预约/退款核心冒烟：通过。
- 支付服务调整后约战 AA 支付和成局冒烟：通过。

仍未完成：

- 真实微信支付/退款、验签、证书、回调解密和商户配置尚未接入。
- 小程序微信开发者工具预览/编译未执行。
- 小程序真机调试未执行。
- 基础审计日志已补；生产级账号密码和菜单权限仍需继续完善。

## 19. App 用户身份隔离回归记录

测试时间：2026-06-28

本轮修正：

- 新增 `RequestUserContext`，用于解析 App 端当前用户身份。
- 有 mock token 时，后端会校验请求中的 `userId` 必须与 token 用户一致。
- 无 token 时保留开发/测试脚本传 `userId` 的兼容模式，但该模式不作为生产登录方案。
- 预约创建、订单列表、订单详情、订单取消接入用户身份解析。
- 约战创建、加入、退出、取消未成局接入用户身份解析；登录状态下只有创建者可以取消未成局约战。
- 约战 AA 支付创建接入用户身份解析。
- 预约预支付增加用户归属校验，登录用户不能支付他人预约订单。
- 退款申请增加用户归属校验，登录用户不能申请他人支付单退款。

本地验证结果：

```text
后端测试：Tests run: 29, Failures: 0, Errors: 0, Skipped: 0
管理后台构建：vite build 通过
小程序结构检查：miniprogram json/js/page-structure check passed. json=12, js=12, pages=10
```

新增单元测试覆盖：

- 无 token 开发模式允许使用请求中的 `userId`。
- 有 token 且请求用户一致时允许访问。
- 有 token 且请求用户不一致时拒绝，错误为“不能冒用其他用户身份”。
- 有 token 时可不传请求 `userId`，由 token 用户决定。
- 无 token 且未传 `userId` 时拒绝。
- 登录用户不能预支付他人预约订单。
- 登录用户不能申请他人支付单退款。

服务器部署验证：

```text
ball-saas-api 已重新构建并启动
ball-saas-admin / ball-saas-postgres / ball-saas-redis 保持运行
API 健康接口 /actuator/health 返回 200, UP
管理后台 /api/admin/dashboard 代理返回 200
未使用 80/443
未停止或覆盖非本项目容器
```

服务器用户冒用查询验证：

```json
{
  "TokenUserId": 1,
  "SpoofedUserId": 999,
  "Rejected": true,
  "Message": "不能冒用其他用户身份"
}
```

服务器预支付与退款用户隔离验证：

```json
{
  "TokenUserId": 1,
  "OtherUserId": 999,
  "VenueId": 15,
  "BookingId": 28,
  "PaymentNo": "P1782622349680",
  "PrepayRejected": true,
  "PrepayMessage": "不能支付他人预约订单",
  "RefundRejected": true,
  "RefundMessage": "不能申请他人支付单退款"
}
```

服务器核心冒烟结果：

```json
{
  "VenueId": 16,
  "CourtId": 17,
  "ConcurrentAttempts": 10,
  "ConcurrentSuccess": 1,
  "SixtyMinuteBookingId": 31,
  "NinetyMinuteBookingId": 32,
  "FullRefundId": 9,
  "PartialRefundId": 10,
  "VenueTodayOrders": 3,
  "VenueTodayAmountCent": 21000,
  "VenueTodayRefundCent": 9000
}
```

服务器约战回归结果：

```json
{
  "VenueId": 17,
  "CourtId": 16,
  "FormedMatchId": 10,
  "FormedMatchStatus": "FORMED",
  "PaidPlayerStatus": "PAID",
  "PaidShareStatus": "PAID",
  "CancelledMatchId": 11,
  "BeforeCancelStatus": "RECRUITING",
  "CancelledMatchStatus": "NOT_FORMED_CANCELLED",
  "CancelledShareStatus": "REFUNDED",
  "ShareRefundCount": 1
}
```

当前新增通过项：

- App 用户查询他人订单拦截：服务器验证通过。
- App 用户预支付他人预约订单拦截：服务器验证通过。
- App 用户申请他人支付单退款拦截：服务器验证通过。
- 用户身份解析单元测试：通过。
- 原有无 token 自动化冒烟兼容性：通过。

仍未完成：

- 当前仍是 mock token 与开发兼容模型，基础审计日志已完成；生产级账号密码、微信真实登录态和菜单权限尚未完成。
- 小程序微信开发者工具预览/编译未执行。
- 小程序真机调试未执行。
- 真实微信支付/退款、验签、证书、回调解密和商户配置尚未接入。

## 20. 小程序 mock 登录与 token 请求封装回归记录

测试时间：2026-06-28

本轮新增小程序能力：

- 小程序请求封装新增自动 mock 登录能力。
- 首次请求会调用 `POST /api/app/auth/wx-login`，使用测试 `mock-code` 获取 mock token。
- 后续请求自动携带 `Authorization: Bearer <token>`。
- 登录成功后会把后端返回的 `userId` 写回 `globalData.mockUserId`，避免前端固定用户 ID 与 token 用户不一致。
- `app.js` 保留测试 API 和未来正式 HTTPS API 切换位置。
- “我的”页从占位改为展示测试用户、环境、API 地址和登录态，便于测试人员确认当前身份。

小程序结构检查结果：

```text
miniprogram json/js/page-structure check passed. json=12, js=12, pages=10
```

说明：

- 本轮完成的是源码层面的 mock 登录和 token 请求封装。
- 后端服务器已验证 token 用户不能冒用其他 `userId` 查询、预支付或退款。
- 小程序仍未使用微信开发者工具做预览、编译和真机调试。
- 正式上线时 `prodApiBaseUrl` 必须替换为备案域名 + HTTPS，并配置微信合法域名。

当前新增通过项：

- 小程序自动 mock 登录源码检查：通过。
- 小程序请求自动带 token 源码检查：通过。
- 小程序“我的”页结构检查：通过。
- 小程序页面文件完整性：通过。

仍未完成：

- 微信开发者工具预览/编译未执行。
- 小程序真机调试未执行。
- 正式 HTTPS 域名、微信合法域名、真实微信支付/退款仍未配置。

## 21. 小程序场馆详情、预约与模拟支付主路径回归记录

测试时间：2026-06-28

本轮新增小程序能力：

- 场馆详情页从占位改为可加载场馆信息和可预约场地列表。
- 场馆详情页可选择场地并进入预约页。
- 预约页支持选择日期、开始时间、结束时间。
- 预约页可基于当前 token 用户创建预约订单，不再依赖前端传固定 `userId`。
- 预约页创建订单后可直接发起预支付并调用模拟支付成功。
- 订单列表和订单详情改为使用 token 用户查询，不再显式拼接 `userId`。
- 订单详情新增待支付订单的模拟支付入口。
- 约战创建、加入、退出、AA 支付创建去除显式用户 ID，由后端 token 解析当前用户。

小程序结构检查结果：

```text
miniprogram json/js/page-structure check passed. json=12, js=12, pages=10
```

服务器 API 主链路验证：

```json
{
  "UserId": 1,
  "VenueId": 18,
  "CourtCount": 1,
  "BookingId": 34,
  "BookingUserId": 1,
  "PaymentNo": "P1782623483856",
  "PaymentStatus": "PAID",
  "DetailStatus": "RESERVED",
  "OrdersReturned": 1
}
```

验证说明：

- 小程序同等调用链路已在服务器 API 层验证：mock 登录、场馆详情、场地列表、创建预约、预支付、模拟支付、订单详情、订单列表。
- 创建预约时请求体未传 `userId`，后端按 token 用户创建，返回 `BookingUserId = 1`。
- 支付成功后订单详情状态为 `RESERVED`。

当前新增通过项：

- 小程序场馆详情源码接入：通过。
- 小程序预约页源码接入：通过。
- 小程序订单详情模拟支付入口源码接入：通过。
- 小程序 token 用户预约/支付服务器 API 链路：通过。
- 小程序页面结构完整性：通过。

仍未完成：

- 微信开发者工具预览/编译未执行。
- 小程序真机调试未执行。
- 正式 HTTPS 域名、微信合法域名、真实微信支付/退款仍未配置。

## 22. 管理后台详情抽屉与部署回归记录

测试时间：2026-06-28

本轮新增管理后台能力：

- 场馆审核列表新增“详情”操作，可查看场馆 ID、名称、类型、地址、状态。
- 预约订单列表新增“详情”操作，可查看订单号、状态、场馆、场地、用户、金额、时间、支付单与支付状态。
- 订单详情抽屉中保留已预约订单核销操作。
- 退款列表新增“详情”操作，可查看退款单号、支付单、场馆、金额、状态、申请时间、原因。
- 修复约战详情抽屉相关状态变量缺失问题。
- 管理后台补充 scoped 样式，保持清爽的经营工作台布局。

本地验证结果：

```text
管理后台构建：vite build 通过
小程序结构检查：miniprogram json/js/page-structure check passed. json=12, js=12, pages=10
```

服务器部署验证：

```text
ball-saas-admin 已重新构建并启动
ball-saas-api / ball-saas-postgres / ball-saas-redis 保持运行
管理后台首页 http://1.69.143.156:18201/ 返回 200
管理后台 /api/admin/dashboard 代理返回 200
未使用 80/443
未停止或覆盖非本项目容器
```

后台代理验证返回：

```json
{
  "todayOrders": 34,
  "todayAmountCent": 201000,
  "todayRefundCent": 45000,
  "pendingRefunds": 0,
  "venues": 18
}
```

当前新增通过项：

- 管理后台场馆详情展示：构建通过。
- 管理后台订单详情展示与核销入口：构建通过。
- 管理后台退款详情展示：构建通过。
- 管理后台约战详情变量修复：构建通过。
- 管理后台服务器部署与访问：通过。

仍未完成：

- 管理后台已补充基础审计日志入口；生产级账号密码、菜单权限配置仍未完成。
- 管理后台暂未使用浏览器自动化逐按钮点击验证。
- 真实微信支付/退款、HTTPS 回调和正式小程序上线条件仍未具备。


## 23. 审计日志查询、部署与服务器回归记录

测试时间：2026-06-28

本轮新增能力：

- 新增 `audit_log` 审计日志查询接口。
- 平台接口：`GET /api/admin/audit-logs`，仅平台管理员 token 可查询最近 100 条审计日志。
- 场馆接口：`GET /api/venue/audit-logs`，按 `X-Venue-Id` 和登录 token 的场馆权限收窄数据范围。
- 管理后台新增“审计日志”菜单，可切换平台视角和场馆视角查看操作时间、操作类型、对象、操作人、角色、场馆和摘要。
- 当前已记录场馆入驻、场馆审核、场地创建、场地停用、价格规则保存、预约订单核销等关键后台操作。

本地验证结果：

```text
后端测试：Tests run: 33, Failures: 0, Errors: 0, Skipped: 0
管理后台构建：vite build 通过
小程序结构检查：miniprogram json/js/page-structure check passed. json=11, js=10, pages=10
```

服务器部署验证：

```text
ball-saas-api：running，端口 18200
ball-saas-admin：running，端口 18201
ball-saas-postgres：running，仅 Docker 内部 5432
ball-saas-redis：running，仅 Docker 内部 6379
API 健康检查：http://127.0.0.1:18200/actuator/health 返回 200 / UP
管理后台首页：http://127.0.0.1:18201/ 返回 200
管理后台 /api/admin/dashboard 代理返回 200
未使用 80/443
未停止或覆盖非本项目容器
```

Flyway 迁移验证：

```text
1|init|t
2|match share payment|t
3|audit log|t
```

审计日志服务器闭环验证：

```text
platform_api_count=2
venue_api_rows=[('VENUE_APPROVE', 19, 'PLATFORM_ADMIN'), ('VENUE_CREATE', 19, 'PLATFORM_ADMIN')]
venue_id=19
VENUE_CREATE|VENUE|19|PLATFORM_ADMIN|19
VENUE_APPROVE|VENUE|19|PLATFORM_ADMIN|19
```

服务器核心冒烟回归：

```json
{
  "VenueId": 20,
  "CourtId": 19,
  "ConcurrentAttempts": 10,
  "ConcurrentSuccess": 1,
  "SixtyMinuteBookingId": 37,
  "NinetyMinuteBookingId": 38,
  "FullRefundId": 11,
  "PartialRefundId": 12,
  "VenueTodayOrders": 3,
  "VenueTodayAmountCent": 21000,
  "VenueTodayRefundCent": 9000
}
```

服务器约战 P1 回归：

```json
{
  "VenueId": 21,
  "CourtId": 20,
  "FormedMatchId": 12,
  "FormedMatchStatus": "FORMED",
  "PaidPlayerStatus": "PAID",
  "PaidShareStatus": "PAID",
  "CancelledMatchId": 13,
  "BeforeCancelStatus": "RECRUITING",
  "CancelledMatchStatus": "NOT_FORMED_CANCELLED",
  "CancelledShareStatus": "REFUNDED",
  "ShareRefundCount": 1
}
```

服务器未成局自动取消回归：

```json
{
  "VenueId": 22,
  "CourtId": 21,
  "MatchId": 14,
  "BeforeStatus": "RECRUITING",
  "AfterStatus": "NOT_FORMED_CANCELLED",
  "ShareStatus": "REFUNDED",
  "ShareRefundCount": 1,
  "WaitSeconds": 75
}
```

当前新增通过项：

- Flyway V3 `audit_log` 迁移：服务器验证通过。
- 审计日志写入：服务器验证通过。
- 平台审计日志查询接口：服务器验证通过。
- 场馆范围审计日志查询接口：服务器验证通过。
- 管理后台审计日志入口：构建通过并已部署。
- 后端审计控制器与服务单元测试：通过。
- 最新部署后的核心预约、退款、并发锁、报表回归：通过。
- 最新部署后的约战成局、取消、AA 退款回归：通过。

仍未完成或受外部条件限制：

- 当前后台登录仍是 mock token，生产级账号密码、员工账号和菜单权限需要上线前补齐。
- 小程序微信开发者工具预览/编译未执行。
- 小程序真机调试未执行。
- 真实微信支付/退款、验签、证书、回调解密和商户配置未接入。
- 正式小程序上线所需备案域名、HTTPS、微信合法域名和公网 HTTPS 回调地址未配置。

## 24. 场馆员工角色与最终部署回归记录

测试时间：2026-06-28

本轮新增能力：

- 开发期场馆登录接口支持发放场馆员工 mock token。
- `POST /api/venue/auth/login` 使用 `staff/staff` 时返回 `VENUE_STAFF`、`userId=3`、`venueId=1`。
- 管理后台顶部新增“员工登录”按钮，便于测试平台、场馆管理员、场馆员工三种后台视角。
- 场馆员工沿用场馆角色数据隔离规则，不能跨场馆访问场馆接口。

本地验证结果：

```text
后端测试：Tests run: 35, Failures: 0, Errors: 0, Skipped: 0
管理后台构建：vite build 通过
小程序结构检查：miniprogram json/js/page-structure check passed. json=11, js=10, pages=10
```

服务器验证：

```text
API 健康检查：200 / UP
管理后台首页：200
员工 mock 登录：success=true, role=VENUE_STAFF, userId=3, venueId=1
员工跨场馆访问 /api/venue/audit-logs：返回 403
```

最终部署后的核心业务冒烟：

```json
{
  "VenueId": 24,
  "CourtId": 22,
  "ConcurrentAttempts": 10,
  "ConcurrentSuccess": 1,
  "SixtyMinuteBookingId": 43,
  "NinetyMinuteBookingId": 44,
  "FullRefundId": 13,
  "PartialRefundId": 14,
  "VenueTodayOrders": 3,
  "VenueTodayAmountCent": 21000,
  "VenueTodayRefundCent": 9000
}
```

最终部署后的约战 P1 冒烟：

```json
{
  "VenueId": 23,
  "CourtId": 23,
  "FormedMatchId": 15,
  "FormedMatchStatus": "FORMED",
  "PaidPlayerStatus": "PAID",
  "PaidShareStatus": "PAID",
  "CancelledMatchId": 16,
  "BeforeCancelStatus": "RECRUITING",
  "CancelledMatchStatus": "NOT_FORMED_CANCELLED",
  "CancelledShareStatus": "REFUNDED",
  "ShareRefundCount": 1
}
```

最终部署后的未成局自动取消回归：

```json
{
  "VenueId": 25,
  "CourtId": 24,
  "MatchId": 17,
  "BeforeStatus": "RECRUITING",
  "AfterStatus": "NOT_FORMED_CANCELLED",
  "ShareStatus": "REFUNDED",
  "ShareRefundCount": 1,
  "WaitSeconds": 75
}
```

当前新增通过项：

- 场馆员工 mock token 发放：服务器验证通过。
- 场馆员工跨场馆访问拦截：服务器验证通过。
- 管理后台员工登录入口：构建通过并已部署。
- 最终部署后核心预约、退款、并发、报表回归：通过。
- 最终部署后约战成局、取消、AA 退款回归：通过。
- 最终部署后未成局自动取消和自动退款记录回归：通过。

仍未完成或受外部条件限制：

- 当前仍是开发期 mock 登录，不是生产级账号密码和员工管理系统。
- 生产级菜单权限配置未完成。
- 微信开发者工具预览/编译和真机调试未执行。
- 真实微信支付/退款、验签、证书、回调解密和商户配置未接入。
- 正式小程序上线所需备案域名、HTTPS、微信合法域名和公网 HTTPS 回调地址未配置。



