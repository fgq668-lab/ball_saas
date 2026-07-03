# 场馆预约与球员约战 SaaS 系统架构文档

## 1. 架构目标

系统目标是支持多个球馆入驻平台，并为球员提供预约、约战、支付、退款等能力。架构设计必须满足：

- 多场馆 SaaS
- 微信小程序接入
- 正式支付优先采用微信支付服务商模式，MVP 可使用平台普通商户过渡
- 场地库存强一致约束
- 支付回调幂等
- 退款和分账可追踪
- 可在非 80/443 端口环境部署
- 不影响服务器已有产品

## 2. 总体架构

```text
微信小程序
  |
  | HTTPS/API
  v
Spring Boot API 服务
  |
  |-- PostgreSQL：业务数据、订单、支付、结算
  |-- Redis：登录态、缓存、场地临时锁、分布式锁
  |-- 微信支付：支付、退款、分账、回调
  |-- 对象存储：场馆图片、资质图片
  |
  v
管理后台 Vue 3
```

当前服务器没有 80 和 443 端口，因此测试环境可以先通过高位端口访问 API。生产环境如果需要微信小程序正式上线，接口域名必须使用备案域名和 HTTPS 入口，微信支付回调、退款回调、分账回调也必须使用公网可访问的 HTTPS 地址。`1.69.143.156:18200` 只能作为测试访问地址或 HTTPS 网关后的内部服务地址，不能作为正式小程序请求地址。仅申请域名不够，必须同时完成备案、HTTPS 证书、小程序合法域名配置和回调地址配置。

## 3. 部署架构

### 3.1 当前服务器

```text
服务器：1.69.143.156
SSH 端口：2218
用户：frank
限制：没有 80/443 端口
```

### 3.2 推荐部署方式

使用独立目录和独立容器：

```text
/home/frank/apps/ball-saas
  backend
  admin
  data
  logs
  deploy
```

容器建议：

```text
ball-saas-api
ball-saas-postgres
ball-saas-redis
ball-saas-admin
```

端口建议：

```text
API 测试端口：18200
Admin 测试端口：18201
PostgreSQL：不暴露公网
Redis：不暴露公网
正式 HTTPS 入口：由独立网关、API 网关、云托管或具备 443 的代理服务器提供
推荐转发链路：https://api.xxx.com:443 -> HTTPS 网关/代理 -> http://1.69.143.156:18200
```

正式上线域名要求：

- `api.xxx.com` 用作小程序 API 和微信支付回调入口。
- `admin.xxx.com` 可用作管理后台入口，后台也建议使用 HTTPS。
- 当前服务器无 80/443 不影响后端作为源站运行，但影响它直接对外承担正式 HTTPS 入口。
- HTTPS 证书建议使用 DNS 校验申请，避免依赖 80 端口 HTTP 校验。

Redis key 前缀：

```text
ballsaas:
```

数据库名：

```text
ball_saas
```

部署前检查：

```text
检查端口 18200、18201 是否占用
检查 /home/frank/apps/ball-saas 是否为本项目目录
检查 Docker 容器名是否冲突
检查已有进程和已有产品目录
```

## 4. 应用分层

后端采用单体模块化架构，第一版不拆微服务。

```text
Controller 层：HTTP API、参数校验、登录鉴权
Application 层：业务用例编排
Domain 层：核心业务规则
Repository 层：数据库访问
Integration 层：微信支付、对象存储、短信等外部系统
```

第一版保持单体，原因：

- 业务还在验证阶段。
- 订单、支付、预约之间事务关系强。
- 部署和排查成本更低。
- 后续可以按模块拆分服务。

## 5. 多租户架构

系统以场馆为主要业务租户边界。MVP 阶段建议使用 `venue_id` 作为数据隔离主键；如果后续支持连锁集团、多门店统一经营，再引入 `tenant_id` 表示集团或组织。

核心字段：

```text
tenant_id，可为空，二期用于集团/连锁租户
venue_id，MVP 必填，用于场馆数据隔离
```

建议：

- 平台级数据可以没有 `venue_id`。
- 场馆业务数据必须带 `venue_id`。
- 后台接口必须根据登录身份限制数据范围。
- 场馆管理员只能访问自己场馆的数据。
- 平台管理员可以访问全部数据。

需要带 `venue_id` 的表：

```text
venue_user
court
court_price_rule
booking_order
match_room
payment_order
refund_order
settlement_bill
member_card
coupon
checkin_record
```

## 6. 核心数据模型

### 6.1 场馆

```text
venue
- id
- tenant_id
- name
- sport_types
- address
- longitude
- latitude
- contact_name
- contact_phone
- status
- wx_sub_mch_id
- created_at
- updated_at
```

### 6.2 场地

```text
court
- id
- venue_id
- name
- sport_type
- indoor
- status
- sort_order
- created_at
- updated_at
```

### 6.3 价格规则

```text
court_price_rule
- id
- venue_id
- court_id
- day_type
- start_time
- end_time
- price_cent
- effective_start_date
- effective_end_date
- priority
- status
```

### 6.4 预约订单

```text
booking_order
- id
- order_no
- venue_id
- court_id
- user_id
- start_at
- end_at
- amount_cent
- payable_cent
- status
- lock_token
- paid_at
- cancelled_at
- checked_in_at
- created_at
- updated_at
```

### 6.5 约战局

```text
match_room
- id
- match_no
- venue_id
- court_id
- creator_user_id
- start_at
- end_at
- min_players
- max_players
- current_players
- pay_mode
- amount_cent
- per_user_amount_cent
- status
- created_at
- updated_at
```

```text
match_player
- id
- match_room_id
- user_id
- status
- payment_order_id
- joined_at
- left_at
```

### 6.6 支付单

```text
payment_order
- id
- payment_no
- business_type
- business_id
- venue_id
- user_id
- amount_cent
- channel
- wx_prepay_id
- wx_transaction_id
- status
- paid_at
- created_at
- updated_at
```

### 6.7 退款单

```text
refund_order
- id
- refund_no
- payment_order_id
- venue_id
- amount_cent
- reason
- wx_refund_id
- status
- requested_at
- refunded_at
- created_at
- updated_at
```

## 7. 场地库存与锁定

场地预约必须避免同一时间段重复销售。

### 7.1 Redis 临时锁

锁 key：

```text
ballsaas:court-lock:{venueId}:{courtId}:{startAt}:{endAt}
```

锁定时间：

```text
5-10 分钟，仅用于普通订场支付倒计时
```

流程：

```text
用户选择时间
后端检查正式预约是否冲突
后端写入 Redis 临时锁
创建待支付订单
用户支付
支付成功后创建正式预约
删除临时锁
支付超时后释放临时锁并关闭订单
```

约战说明：

- 约战不能长期依赖 5-10 分钟临时锁占用场地。
- MVP 中，约战必须绑定一笔已支付成功的预约订单，由发起人全额支付或定金支付锁定场地。
- AA 分摊、未成局自动退款作为二期能力，不能阻塞普通订场 MVP。

### 7.2 数据库防重

仅有 Redis 锁不够，数据库也必须防止并发重复预约。

建议：

- 在创建正式预约时再次检查冲突。
- 对同一场地同一时间范围使用事务和行级锁。
- 可以维护 `court_time_slot` 表，将时间切成固定粒度，使用唯一索引防重。

MVP 推荐使用 30 分钟固定时间槽：

```text
court_time_slot
- venue_id
- court_id
- slot_start_at
- slot_end_at
- booking_order_id
唯一索引：venue_id + court_id + slot_start_at
```

规则：

- 一笔 60 分钟订单占用 2 个连续 30 分钟时间槽。
- 一笔 90 分钟订单占用 3 个连续 30 分钟时间槽。
- 创建正式预约时，在同一个数据库事务内批量写入所有时间槽。
- 任意一个时间槽写入失败，则整笔预约确认失败并进入人工处理或自动退款流程。

## 8. 支付架构

### 8.1 微信支付模式

正式推荐方案：

```text
微信支付服务商模式
场馆作为特约商户
平台作为服务商
订单支付到对应场馆特约商户
平台通过分账收取佣金
```

MVP 过渡方案：

```text
平台普通商户收款
系统记录订单归属场馆和应结算金额
平台按周期线下结算给场馆
```

说明：

- 正式商业化优先使用服务商模式，账务和合规边界更清晰。
- 如果服务商、特约商户进件和分账能力暂未就绪，可用平台普通商户收款作为短期过渡。
- 过渡方案必须在后台报表中明确标记资金归属、平台应付场馆金额和合规风险，不应长期使用。

### 8.2 支付回调

回调处理要求：

- 校验微信签名。
- 解密回调报文。
- 根据微信交易号和本地支付单号定位支付单。
- 支持重复回调。
- 使用事务更新支付单和业务单。
- 记录原始回调日志。

回调日志表：

```text
payment_notify_log
- id
- channel
- notify_type
- out_trade_no
- transaction_id
- raw_body
- headers
- process_status
- error_message
- created_at
```

### 8.3 分账

分账建议在订单完成或核销后执行，不建议支付成功立即分账。

流程：

```text
用户支付成功
订单进入已预约
用户到场核销
订单完成
生成分账单
调用微信分账
记录分账结果
```

## 9. 定时任务

需要的定时任务：

- 关闭超时未支付订单
- 释放过期临时锁
- P1：约战未成局自动取消
- P1：约战未成局自动退款
- 开场前提醒
- 订单完成状态流转
- 生成每日结算报表
- 对账文件拉取，P1 版本必须补齐

定时任务要求：

- 支持幂等。
- 支持失败重试。
- 记录执行日志。
- 同一任务不能并发重复执行。

## 10. 权限架构

权限模型：

```text
用户
角色
权限
场馆数据范围
```

角色：

```text
平台超级管理员
平台运营
平台财务
场馆老板
场馆店长
场馆员工
教练
普通球员
```

接口权限：

- 平台接口必须平台角色访问。
- 场馆接口必须校验用户是否属于该场馆。
- 小程序接口只能访问当前用户自己的数据。

## 11. 日志与监控

必须记录：

- 登录日志
- 订单状态变更日志
- 支付回调日志
- 退款操作日志
- 分账操作日志
- 场馆配置变更日志
- 管理员操作日志

日志目录：

```text
/home/frank/apps/ball-saas/logs
```

日志文件建议：

```text
app.log
payment.log
refund.log
job.log
error.log
```

## 12. 安全要求

- 管理后台必须登录后访问。
- 小程序接口必须校验登录态。
- 支付回调必须验签。
- 敏感配置通过环境变量注入。
- 微信支付证书不能提交到代码仓库。
- 数据库和 Redis 不暴露公网。
- 上传文件限制类型和大小。
- 管理后台操作保留审计日志。

## 13. 后续扩展

可扩展方向：

- 教练课程
- 会员卡
- 优惠券
- 联赛赛事
- 球员等级体系
- 球友关系链
- 多门店连锁
- 城市代理
- 商家营销工具
- 对账自动化
