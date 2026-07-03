# 场馆预约与球员约战 SaaS 系统需求编程文档

## 1. 文档目的

本文档用于把产品需求转化为可开发、可测试、可验收的编程任务。后续开发应优先参考本文档拆分接口、页面、数据表和业务流程。

技术路线：

```text
后端：Java Spring Boot + PostgreSQL + Redis
用户端：微信小程序
管理端：Vue 3 + Element Plus
部署：独立目录、独立端口、独立数据库、独立 Redis 前缀
```

服务器约束：

```text
服务器：1.69.143.156
SSH 端口：2218
用户：frank
禁止使用：80、443
要求：不得影响服务器已有产品
```

小程序正式环境约束：

```text
当前服务器可以运行后端服务，但不能直接作为正式小程序接口地址
正式小程序必须使用备案域名和 HTTPS 入口
仅申请域名不够，还需要备案、HTTPS 证书、小程序合法域名配置和公网 HTTPS 回调地址
微信支付、退款、分账回调必须使用公网 HTTPS 地址
如当前服务器无法提供 443，需要增加 HTTPS 网关、API 网关、云托管或代理服务器
推荐转发链路：https://api.xxx.com:443 -> HTTPS 网关/代理 -> http://1.69.143.156:18200
证书申请建议使用 DNS 校验，避免依赖当前服务器 80 端口
```

## 2. 用户角色

### 2.1 普通球员

可执行操作：

- 微信登录
- 查看附近场馆
- 查看场地可预约时间
- 预约场地
- 微信支付
- 发起约战
- 加入约战
- 申请退款
- 查看订单
- 查看约战记录

### 2.2 场馆管理员

可执行操作：

- 登录场馆后台
- 管理本场馆信息
- 管理场地
- 配置营业时间
- 配置价格
- 查看预约订单
- 核销订单
- 处理退款，按权限
- 查看本场馆经营报表

### 2.3 平台管理员

可执行操作：

- 登录平台后台
- 审核场馆入驻
- 管理全部场馆
- 查看全部订单
- 配置平台佣金
- 查看支付、退款、分账记录
- 查看平台经营报表
- 管理后台账号和权限

## 3. MVP 页面清单

### 3.1 微信小程序页面

```text
首页
场馆列表页
场馆详情页
场地预约页
订单确认页
微信支付页，使用微信原生支付能力
预约订单列表页
预约订单详情页
约战列表页
约战详情页
创建约战页
我的页面
退款申请页
```

### 3.2 平台后台页面

```text
登录页
平台首页/数据概览
场馆入驻审核页
场馆管理页
订单管理页
约战管理页
退款管理页
支付流水页
分账/结算页
管理员账号页
系统配置页
```

### 3.3 场馆后台页面

```text
登录页
场馆首页/经营概览
场地管理页
价格日历页
营业时间页
预约订单页
核销页
退款处理页
约战管理页
场馆报表页
员工账号页
```

## 4. 核心业务流程

### 4.1 普通订场流程

```text
用户进入场馆详情
选择日期、场地、时间段
后端计算价格
后端检查场地是否可预约
Redis 写入临时锁
创建待支付预约订单
小程序调起微信支付
微信支付成功
微信回调通知后端
后端验签并更新支付单
后端确认预约订单
写入正式场地时间槽
用户获得预约成功结果
```

开发要求：

- 价格必须以后端计算为准。
- 创建订单时必须锁定场地。
- 支付成功后必须再次检查并写入正式预约。
- 微信回调必须支持重复通知。
- 支付超时必须自动关闭订单并释放锁。

### 4.2 MVP 约战流程

```text
发起人先完成普通场地预约并支付成功
发起人基于已预约订单创建公开约战
填写人数、等级要求、说明
其他用户加入约战
达到最大人数后停止加入
开场后约战状态进入已开打/已结束
```

开发要求：

- MVP 约战必须绑定一笔已支付成功的预约订单。
- MVP 不使用 5-10 分钟临时锁长期占用场地。
- MVP 不做成员 AA 支付和未成局自动退款。
- 同一用户不能重复加入同一约战。
- 加入人数不能超过最大人数。

### 4.3 P1 约战 AA 支付流程

```text
发起人完成场地预约并支付成功
发起人创建约战局并设置 AA 分摊规则
其他用户加入约战并支付份额
系统记录成员支付状态和应分摊金额
如约战取消，按规则退款给已支付成员
```

开发要求：

- AA 分摊不改变场馆预约订单的支付事实。
- 场馆侧只认原始预约订单，成员分摊属于平台内费用分担。
- 未成局退款仅作用于成员分摊支付，不应误释放已预约场地，除非原始预约订单也被全额退款取消。

### 4.4 退款流程

```text
用户进入订单详情
发起退款申请
后端读取场馆退款规则
计算可退金额
生成退款单
调用微信退款接口
接收微信退款回调
更新退款单状态
更新订单/约战状态
按退款类型决定是否释放资源
```

开发要求：

- 退款金额必须由后端计算。
- 同一支付单不能重复超额退款。
- 微信退款回调必须幂等。
- 未开始且全额退款成功，才释放场地时间槽。
- 部分退款、已核销订单退款、已完成订单售后退款不释放场地。
- 退款后要写入操作日志。

### 4.5 场馆入驻流程

```text
场馆提交入驻资料
平台管理员审核
审核通过后创建场馆租户数据
绑定场馆管理员
配置收款商户号
场馆管理员配置场地和价格
场馆上线
```

开发要求：

- 未审核通过的场馆不能被用户预约。
- 场馆必须配置至少一个启用场地。
- 场馆必须配置有效价格后才能开放预约。

## 5. 后端接口规划

接口统一前缀建议：

```text
/api/app      微信小程序接口
/api/admin    平台后台接口
/api/venue    场馆后台接口
/api/pay      支付回调接口
```

### 5.1 小程序接口

```text
POST /api/app/auth/wx-login
GET  /api/app/venues
GET  /api/app/venues/{venueId}
GET  /api/app/venues/{venueId}/courts/availability
POST /api/app/bookings
GET  /api/app/bookings
GET  /api/app/bookings/{bookingId}
POST /api/app/bookings/{bookingId}/cancel
POST /api/app/bookings/{bookingId}/refunds
POST /api/app/payments/prepay
GET  /api/app/matches
POST /api/app/matches
GET  /api/app/matches/{matchId}
POST /api/app/matches/{matchId}/join
POST /api/app/matches/{matchId}/leave
POST /api/app/matches/{matchId}/share-payments，P1 AA 分摊支付
GET  /api/app/me
```

### 5.2 平台后台接口

```text
POST /api/admin/auth/login
GET  /api/admin/dashboard
GET  /api/admin/venues
GET  /api/admin/venues/{venueId}
POST /api/admin/venues/{venueId}/approve
POST /api/admin/venues/{venueId}/reject
POST /api/admin/venues/{venueId}/disable
GET  /api/admin/orders
GET  /api/admin/payments
GET  /api/admin/refunds
GET  /api/admin/settlements
POST /api/admin/commission-rules
GET  /api/admin/users
POST /api/admin/users
```

### 5.3 场馆后台接口

```text
POST /api/venue/auth/login
GET  /api/venue/dashboard
GET  /api/venue/profile
PUT  /api/venue/profile
GET  /api/venue/courts
POST /api/venue/courts
PUT  /api/venue/courts/{courtId}
POST /api/venue/courts/{courtId}/disable
GET  /api/venue/price-rules
POST /api/venue/price-rules
PUT  /api/venue/price-rules/{ruleId}
GET  /api/venue/bookings
POST /api/venue/bookings/{bookingId}/checkin
GET  /api/venue/refunds
POST /api/venue/refunds/{refundId}/approve
POST /api/venue/refunds/{refundId}/reject
GET  /api/venue/reports/orders
```

### 5.4 支付回调接口

```text
POST /api/pay/wechat/notify/payment
POST /api/pay/wechat/notify/refund
POST /api/pay/wechat/notify/profit-sharing
```

要求：

- 回调接口不走普通登录鉴权。
- 必须校验微信支付签名。
- 必须保存原始回调日志。
- 必须幂等处理。

## 6. 数据表开发清单

第一版建议建立以下表：

```text
app_user
admin_user
role
permission
user_role
venue
venue_user
court
court_business_hour
court_maintenance
court_price_rule
court_time_slot
booking_order
match_room
match_player
payment_order
payment_notify_log
refund_order
settlement_bill
commission_rule
operation_log
job_log
```

P1 约战 AA 支付建议增加：

```text
match_share_payment
match_share_refund
```

### 6.1 通用字段

核心业务表建议包含：

```text
id
created_at
updated_at
created_by
updated_by
deleted
version
```

并发敏感表建议使用：

```text
version 乐观锁字段
```

### 6.2 金额字段

MVP 预约时间槽统一按 30 分钟粒度拆分，一笔订单可占用多个连续时间槽。

所有金额字段统一使用整数分：

```text
amount_cent
payable_cent
refund_cent
commission_cent
settlement_cent
```

禁止使用浮点数保存金额。

### 6.3 状态字段

状态字段统一使用字符串或枚举映射：

```text
status
```

禁止在业务代码里散落魔法数字。

## 7. Redis Key 规划

Redis 前缀：

```text
ballsaas:
```

Key 规划：

```text
ballsaas:login:app:{token}
ballsaas:login:admin:{token}
ballsaas:court-lock:{venueId}:{courtId}:{startAt}:{endAt}
ballsaas:pay-lock:{paymentNo}
ballsaas:refund-lock:{refundNo}
ballsaas:job-lock:{jobName}
ballsaas:captcha:{scene}:{mobile}
```

要求：

- 所有 key 必须带 `ballsaas:` 前缀。
- 锁类 key 必须设置过期时间。
- 业务代码不能使用没有前缀的 Redis key。

## 8. 订单状态机

### 8.1 预约订单状态

```text
待支付 -> 已预约
待支付 -> 支付超时
已预约 -> 已核销
已核销 -> 已完成
已预约 -> 退款中
退款中 -> 已退款
退款中 -> 部分退款
已预约 -> 已取消
```

说明：

- 预约订单不设置“已支付”状态。
- 支付结果由 `payment_order` 维护。
- 支付成功后，预约订单直接从 `待支付` 流转为 `已预约`。

禁止状态：

```text
已退款 -> 已预约
已完成 -> 待支付
支付超时 -> 已预约，除非支付回调先于超时任务真实成功，需通过支付时间和事务判定
```

### 8.2 支付单状态

```text
待支付 -> 支付成功
待支付 -> 支付失败
待支付 -> 已关闭
支付成功 -> 已退款
支付成功 -> 部分退款
```

### 8.3 约战状态

MVP 状态：

```text
招募中 -> 人数已满
招募中 -> 已取消
人数已满 -> 已开打
招募中 -> 已开打
已开打 -> 已结束
```

P1 状态：

```text
招募中 -> 已成局
招募中 -> 未成局取消
已成局 -> 已开打
已成局 -> 已取消，需按退款规则处理
```

## 9. 后端开发任务拆分

### 9.1 基础工程

- 创建 Spring Boot 项目
- 配置 PostgreSQL
- 配置 Redis
- 配置统一返回结构
- 配置统一异常处理
- 配置参数校验
- 配置日志
- 配置 OpenAPI/Swagger
- 配置数据库迁移工具，建议 Flyway

### 9.2 权限与登录

- 小程序微信登录
- 后台账号密码登录
- JWT 或 Session Token
- 角色权限模型
- 场馆数据权限校验

### 9.3 场馆模块

- 场馆入驻申请
- 场馆审核
- 场馆启停
- 场馆资料维护
- 收款商户号配置

### 9.4 场地模块

- 场地增删改查
- 营业时间配置
- 维护时间配置
- 场地启停
- 可预约时间查询

### 9.5 价格模块

- 价格规则 CRUD
- 工作日/周末/节假日价格
- 特殊日期价格
- 价格计算服务
- 价格优先级测试

### 9.6 预约模块

- 创建预约订单
- Redis 临时锁场
- 支付超时关闭
- 正式时间槽写入
- 订单查询
- 到场核销
- 取消预约

### 9.7 支付模块

- 微信预支付
- 支付单创建
- 支持正式服务商模式和 MVP 平台普通商户过渡模式
- 支付回调验签
- 支付回调幂等处理
- 支付日志记录
- 支付异常处理

### 9.8 退款模块

- 退款规则判断
- 退款单创建
- 微信退款调用
- 退款回调处理
- 按退款类型处理资源释放

### 9.9 约战模块

- 基于已支付预约订单创建约战
- 加入约战
- 退出约战
- 约战成员管理
- 约战状态流转
- P1：约战 AA 支付
- P1：未成局自动取消
- P1：未成局自动退款

### 9.10 报表模块

- 平台订单概览
- 场馆订单概览
- 场地利用率
- 支付金额统计
- 退款金额统计
- 结算金额统计

## 10. 前端开发任务拆分

### 10.1 微信小程序

- 登录与用户信息
- 场馆列表
- 场馆详情
- 场地时间选择
- 订单确认
- 微信支付调起
- 订单列表与详情
- 退款申请
- 约战列表
- 创建约战
- 加入约战
- 我的页面

### 10.2 管理后台

- 登录
- 首页数据看板
- 场馆审核
- 场馆管理
- 场地管理
- 价格配置
- 预约订单
- 退款管理
- 约战管理
- 结算报表
- 账号权限
- 审计日志

## 11. 验收规则

每个模块完成时必须满足：

- 接口可通过 Swagger 或接口文档查看。
- 核心接口有参数校验。
- 关键业务有单元测试。
- 支付、退款、锁场、状态机必须有集成测试。
- 后台接口必须校验权限。
- 场馆数据必须隔离。
- 日志能定位关键状态变化。

MVP 总体验收：

```text
平台能审核场馆
场馆能配置场地和价格
用户能预约并支付
同一场地同一时间不能重复预约
用户能基于已支付预约创建约战
用户能加入约战
P1：用户能加入约战并支付
P1：未成局能自动退款
用户能申请退款
场馆能核销订单
平台能查看订单、支付、退款、结算数据和审计日志
部署不会影响服务器其他产品
```

## 12. 开发优先级

P0 必须优先完成：

```text
基础工程
登录权限
场馆入驻
场地管理
价格计算
预约订单
Redis 锁场
微信支付
支付回调
退款
```

P1 第二阶段完成：

```text
约战 AA 支付
未成局自动退款
场馆后台报表
平台结算报表
订阅消息
```

P2 后续增强：

```text
会员卡
优惠券
教练课程
联赛赛事
球员等级体系
营销工具
```

## 13. 开发注意事项

- 不要把支付逻辑写散在预约模块里，支付必须独立成模块。
- 不要让前端决定最终价格，价格以后端为准。
- 不要只依赖 Redis 锁，正式预约必须有数据库防重。
- 不要在代码仓库提交微信支付证书和密钥。
- 不要暴露 PostgreSQL 和 Redis 到公网。
- 不要使用服务器 80 和 443 端口。
- 不要把 `1.69.143.156:18200` 写成正式小程序 API 地址，正式环境必须走 HTTPS 域名入口。
- 不要修改服务器已有产品的目录、进程、容器和配置。

### 13.1 审计日志补充要求

- 后台关键操作必须写入审计日志。
- 审计日志必须记录操作类型、对象类型、对象 ID、操作人 ID、角色、场馆 ID、摘要和创建时间。
- 平台管理员可查看平台最近审计日志。
- 场馆管理员只能查看本场馆审计日志。
- 审计日志必须按平台/场馆视角隔离，不能跨场馆泄露。

