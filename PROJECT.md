# 暖食记 · 家庭点餐小程序 — 项目总结

## 📋 项目概述

面向家庭内部协作的微信点餐小程序。核心场景：一家人共享菜单，谁想吃什么就下单，做饭的人接单完成。不是外卖，是家庭厨房的数字化。

**技术栈**：uni-app (Vue3) + Spring Boot + MySQL，独立全栈开发。

---

## 🛠️ 技术栈

| 层 | 技术 | 选型理由 |
|---|------|----------|
| 前端框架 | uni-app (Vue3 Composition API) | 一套代码编译微信小程序，学习成本低 |
| 状态管理 | Pinia | Vue3 官方推荐，比 Vuex 轻量 |
| 后端框架 | Spring Boot 3.2 | 生态成熟，MyBatis-Plus 一键 CRUD |
| ORM | MyBatis-Plus 3.5 | Lambda 查询，不用写 XML |
| 数据库 | MySQL 8.0 | 关系型，表关联查询方便 |
| 认证 | JWT (jjwt 0.12) | 无状态，适合移动端 |
| API 文档 | Knife4j | Swagger 增强，自动生成接口文档 |
| 部署 | 阿里云 ECS + Nginx + Let's Encrypt | 学生机 ¥9.9/月 |
| CDN/HTTPS | Cloudflare (计划中) | 免费 SSL + 全球加速 |

---

## 🏗️ 开发过程

### 第一阶段：需求设计（产品经理思维）

1. **明确用户角色**：家庭所有成员平等——都能发布菜单、点餐、接单
2. **MVP 范围**：家庭管理、菜品 CRUD、购物车、下单流程、基础看板
3. **排除项**：微信支付（家庭内部不需要）、配送（不是外卖）、评价系统（MVP 不做）
4. **数据库设计**：7 张核心表 → 最终扩展为 10 张

### 第二阶段：技术实现

```
client/                  server/
├── pages/               ├── controller/    6 个控制器
│   ├── index/   点餐首页  ├── service/      5 个业务层
│   ├── order/   确认下单  ├── entity/       10 张表
│   ├── order-list/ 订单   ├── config/       拦截器+CORS
│   ├── manage/  菜品管理   ├── security/     JWT 工具
│   ├── stats/   数据看板   └── common/      统一返回+异常
│   └── mine/   个人中心
├── api/request.js  统一请求
└── stores/cart.js  Pinia 购物车
```

### 第三阶段：迭代优化（产品经理 + 用户双视角）

作为产品经理不断追问"还缺什么"，作为用户不断测试"哪里不好用"，驱动了 4 轮迭代。

---

## 🐛 主要问题与解决方案

### 1. 微信小程序开发环境搭建

**问题**：uni-app 项目无法被 HBuilderX 识别为小程序项目，只显示"web 项目"

**原因**：项目中残留了 `router/`、`vite.config.js`、`index.html` 等 H5 模式文件

**解决**：删除 H5 残留文件，添加 `vueVersion: "3"` 到 manifest.json

### 2. 微信开发者工具 wx.login() 超时

**问题**：模拟器中 `wx.login()` 一直 timeout，无法获取微信 code

**原因**：Windows 版微信开发者工具的网络层与微信服务器通信不稳定

**解决**：添加 dev 模式登录降级——`wx.login()` 失败时自动使用本地 mock code，保证开发调试不受影响

### 3. 前端代码不兼容微信小程序

**问题**：使用 HTML 标签（`<div>`、`<span>`、`<img>`）和 Web API（`window.location`、`localStorage`），在小程序中全部失效

**解决**：全局替换为 uni-app 组件（`<view>`、`<text>`、`<image>`），用 `uni.getStorageSync()` 替代 `localStorage`，用 `onLoad()` 替代 `window.location.search`

### 4. 下单页菜品信息丢失

**问题**：通过 URL 参数传递菜品列表，param 只能传 dishId 和 quantity，丢失了菜名和价格

**解决**：改为直接从 Pinia 购物车 store 读取，不再依赖 URL 传参

### 5. HTTP API 地址冲突

**问题**：Vite 代理 `/api` 路径拦截了 `api/request.js` 文件，返回空响应导致 App 白屏

**解决**：uni-app 不使用 Vite 代理，直接 `uni.request()` 发请求

### 6. 服务器部署 IPv6 绑定

**问题**：Spring Boot 在服务器上只监听 IPv6 `[::]:8080`，外部 IPv4 连接被拒绝

**解决**：添加 `-Djava.net.preferIPv4Stack=true` JVM 参数和 `--server.address=0.0.0.0`

### 7. 数据库新增表未同步到服务器

**问题**：本地开发中新增的 `notification`、`frequent_dish` 表和多个 ALTER 字段未在部署时创建

**解决**：部署脚本中补充所有 DDL，通过 SSH 远程执行

### 8. Nginx 宕机导致 HTTPS 不可用

**问题**：调试过程中 nginx 被意外停止，且未配置开机自启

**解决**：配置 systemd service，`systemctl enable nginx`

### 9. SSL 证书链不完整

**问题**：Let's Encrypt 证书在浏览器正常但移动端报证书错误

**原因**：最初用的是 `cert.pem` 而非 `fullchain.pem`

**解决**：使用 `fullchain.pem`（包含完整证书链），添加合适的 ssl_protocols 和 ssl_ciphers

### 10. 域名无 ICP 备案导致外网不通

**问题**：所有外部 HTTPS 请求都被阿里云拦截，返回"未备案"页面

**原因**：国内服务器上的域名必须完成 ICP 备案才能对外提供 Web 服务

**解决**：本地开发用 `127.0.0.1`；生产部署需完成 ICP 备案或使用海外服务器

---

## ✨ 功能清单

| 模块 | 功能 | 
|------|------|
| 家庭 | 创建/加入（6 位邀请码）、退出（创建者退出=解散） |
| 菜品 | 7 分类 × 28 默认菜（配真实图片）、CRUD、上下架、编辑、删除 |
| 点餐 | 左分类右网格、搜索、菜品详情弹窗、EMOJI 分类、常点快捷区 |
| 购物车 | 加减数量、弹动反馈、Pinia 持久化 |
| 订单 | 备注、下单人显示、接单/完成/取消、再来一单 |
| 看板 | 排行榜、主厨轮值、下单截止时间、常点统计 |
| 通知 | 有人下单→家人收到提醒（10 秒轮询）、未读计数、一键已读 |
| 推荐 | 「猜你喜欢」——家庭热门但个人没点过的菜 |
| 反馈 | 完成订单后 👍👎 评价每道菜 |
| 个人 | 微信原生头像/昵称、邀请码复制 |
| 引导 | 首次使用 3 页引导轮播 |

---

## 📊 数据设计

```
user ──┬── family_member ──┬── family ──┬── category ──┬── dish
       │                   │            │              └── frequent_dish
       │                   │            ├── notification
       │                   │            └── order ──┬── order_item
       └── (order.user_id) ┘                        └── feedback
```

10 张表，核心关系：**用户 → 加入家庭 → 家庭有分类和菜品 → 下单产生订单**

---

## 📝 可补充的内容

### 技术层面
- [ ] 单元测试（JUnit + MockMvc）
- [ ] 接口压力测试（JMeter）
- [ ] Redis 缓存热门菜品（目前每次查数据库）
- [ ] Docker 一键部署（docker-compose）
- [ ] CI/CD（GitHub Actions 自动部署）

### 产品层面
- [ ] ICP 备案（域名备案后可正式上线微信小程序）
- [ ] 菜品图片 AI 识别（上传照片自动识别菜名）
- [ ] 多人实时协作（WebSocket）
- [ ] 多家庭支持（一个人属于多个家庭）

### 面试准备
- [ ] 数据库 ER 图可视化
- [ ] 接口文档导出
- [ ] 准备"最大挑战""技术选型""如果重来会怎么改"等问题的回答
