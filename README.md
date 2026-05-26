# VitaAI 智慧医院系统

<p align="center">
  <img src="https://img.shields.io/badge/version-v1.0.0-blue.svg" alt="Version"/>
  <img src="https://img.shields.io/badge/JDK-17-green.svg" alt="JDK"/>
  <img src="https://img.shields.io/badge/Vue-3.x-green.svg" alt="Vue"/>
  <img src="https://img.shields.io/badge/Spring_Boot-3.2.5-green.svg" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/license-MIT-orange.svg" alt="License"/>
</p>


---

## 项目简介

VitaAI 是一个基于人工智能的医疗辅助平台，旨在为学校、工厂、监狱等不便前往医院的人群提供便捷、安全的医疗服务。系统整合医生诊断案例，将其转化为"Skills"并植入大模型中，实现精准的病情判断。

## 核心特性

| 特性         | 说明                                                         |
| ------------ | ------------------------------------------------------------ |
| AI智能诊断   | 基于 DeepSeek 大模型的智能诊断助手，支持多轮对话、SSE流式响应、诊断报告生成 |
| 健康档案     | 个人健康档案管理，记录病史、过敏史、用药记录                 |
| 在线问诊     | 用户留言咨询，医生回复，支持状态跟踪                         |
| 医疗内容管理 | 疾病管理、药品管理，含内容审核流程                           |
| 数据安全保障 | 环境变量管理敏感配置、操作审计日志、角色权限控制             |
| 多角色支持   | 游客、用户、医生、管理员四种角色，各有专属工作台             |

## 技术栈

### 后端

| 技术                        | 说明                                         |
| --------------------------- | -------------------------------------------- |
| Java 17                     | 编程语言                                     |
| Spring Boot 3.2.5           | 应用框架                                     |
| Spring Security + JWT       | 安全认证（accessToken 2h + refreshToken 7d） |
| Spring Data JPA + Hibernate | 数据访问                                     |
| MySQL 8.0                   | 关系数据库                                   |
| Redis 7.0                   | Token 缓存与会话管理                         |
| Spring Boot Mail            | QQ邮箱 SMTP 邮件服务                         |

### 前端

| 技术         | 说明                                       |
| ------------ | ------------------------------------------ |
| Vue 3.x      | 前端框架（Composition API + script setup） |
| TypeScript   | 类型系统                                   |
| Pinia        | 状态管理                                   |
| Element Plus | UI 组件库                                  |
| Axios        | HTTP 客户端（JWT 拦截器）                  |
| Vite         | 构建工具                                   |
| ECharts      | 数据可视化                                 |

### AI 集成

| 技术              | 说明                                                |
| ----------------- | --------------------------------------------------- |
| DeepSeek V4 Flash | 大语言模型，通过 Anthropic 兼容 API 调用            |
| Vita-skills       | 诊断技能库，Markdown 文件系统存储，支持同步到数据库 |

## 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│              Frontend (Vue 3 + TypeScript + Element Plus)    │
│                         Vite + Axios                          │
├─────────────────────────────────────────────────────────────┤
│              Backend API (Spring Boot 3.2.5 Monolith)        │
│                      RESTful JSON API                        │
├─────────────────────────────────────────────────────────────┤
│  ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌───────────┐   │
│  │   Auth    │ │  Disease  │ │   Drug    │ │  Message  │   │
│  │  Service  │ │  Service  │ │  Service  │ │  Service  │   │
│  └───────────┘ └───────────┘ └───────────┘ └───────────┘   │
│  ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌───────────┐   │
│  │    AI     │ │  Health   │ │  Review   │ │  Audit    │   │
│  │  Service  │ │  Record   │ │  Service  │ │   Log     │   │
│  └───────────┘ └───────────┘ └───────────┘ └───────────┘   │
├─────────────────────────────────────────────────────────────┤
│              MySQL 8.0          Redis 7.0                    │
└─────────────────────────────────────────────────────────────┘
```

## 快速开始

### 环境要求

| 软件    | 版本要求       |
| ------- | -------------- |
| JDK     | 17+            |
| Node.js | 18+            |
| MySQL   | 8.0+           |
| Redis   | 6.0+           |
| Maven   | 3.8+           |
| Docker  | 20.10+（可选） |

### 本地开发

**1. 配置环境变量**

```bash
cp .env.example .env
# 编辑 .env 填入真实凭据
```

**2. 启动 MySQL 和 Redis**

确保 MySQL 和 Redis 服务已启动，数据库 `smart_hospital` 已创建。

**3. 后端启动**

```bash
cd backend
mvn clean compile
mvn spring-boot:run
# 运行在 http://localhost:8080
```

**4. 前端启动**

```bash
cd frontend
npm install
npm run dev
# 运行在 http://localhost:5173
```

### Docker 部署

```bash
# 配置 .env 文件
cp .env.example .env

# 启动所有服务（MySQL + Redis + Backend + Frontend）
docker-compose up -d

# 前端: http://localhost:80
# 后端 API: http://localhost:8080
```

## 项目结构

```
VitaAI-v1.0/
├── backend/                                    # 后端 (Spring Boot)
│   ├── src/main/java/com/vitaai/
│   │   ├── ai/                                 # AI 客户端 (DeepSeekClient + MedicalSkillService)
│   │   ├── controller/                         # 控制器 (14个)
│   │   ├── service/                            # 服务层 (8个)
│   │   ├── repository/                         # 数据访问层 (Spring Data JPA)
│   │   ├── entity/                             # 实体类 (20个)
│   │   ├── dto/                                # 数据传输对象
│   │   ├── security/                           # JWT 过滤器与配置
│   │   └── exception/                          # 全局异常处理
│   ├── src/main/resources/
│   │   ├── application.yml                     # 应用配置
│   │   └── application-docker.yml              # Docker 环境配置
│   └── pom.xml
│
├── frontend/                                   # 前端 (Vue 3)
│   ├── src/
│   │   ├── api/                                # API 接口封装 & Axios 实例
│   │   ├── components/layout/                  # 布局组件
│   │   ├── router/                             # 路由配置（含权限守卫）
│   │   ├── stores/                             # Pinia 状态管理
│   │   ├── views/                              # 页面组件
│   │   └── utils/                              # 工具函数
│   ├── package.json
│   └── vite.config.ts
│
├── database/                                   # 数据库脚本
│   ├── schema.sql                              # 建表脚本
│   └── seed.sql                                # 种子数据
│
├── docker/                                     # Docker 配置
│   ├── docker-compose.yml
│   └── app/
│       ├── Dockerfile.backend
│       └── Dockerfile.frontend
│
├── Vita-skills/                                # AI 诊断 Skills 知识库
├── Drug-Disease/                               # 疾病药品数据
├── scripts/                                    # 工具脚本
├── 项目说明/                                   # 项目文档
├── .env.example                                # 环境变量模板
└── docker-compose.yml                          # Docker 编排文件
```

## API 接口概览

### 认证接口 — `/api/auth`

| 方法 | 路径                 | 说明                     |
| ---- | -------------------- | ------------------------ |
| POST | `/api/auth/register` | 用户注册（需邮箱验证码） |
| POST | `/api/auth/login`    | 用户登录（返回 JWT）     |
| POST | `/api/auth/refresh`  | 刷新 Token               |
| POST | `/api/auth/logout`   | 用户登出                 |

### AI 诊断接口 — `/api/ai`

| 方法   | 路径                              | 说明                    |
| ------ | --------------------------------- | ----------------------- |
| POST   | `/api/ai/chat`                    | 发送诊断消息            |
| POST   | `/api/ai/chat/stream`             | 流式对话 (SSE)          |
| GET    | `/api/ai/diagnoses`               | 诊断记录列表            |
| GET    | `/api/ai/diagnoses/{id}`          | 诊断详情                |
| DELETE | `/api/ai/diagnoses/{id}`          | 删除诊断记录            |
| POST   | `/api/ai/diagnoses/{id}/feedback` | 提交诊断反馈            |
| GET    | `/api/ai/skills`                  | Skills 列表             |
| POST   | `/api/ai/skills`                  | 新增 Skill              |
| PUT    | `/api/ai/skills/{id}`             | 更新 Skill              |
| DELETE | `/api/ai/skills/{id}`             | 删除 Skill              |
| POST   | `/api/ai/skills/upload`           | 上传 Skill 文件         |
| POST   | `/api/ai/skills/sync`             | 从 Vita-skills 目录同步 |

### 管理员接口 — `/api/admin`

| 方法                | 路径                                      | 说明                    |
| ------------------- | ----------------------------------------- | ----------------------- |
| GET                 | `/api/admin/stats`                        | 仪表盘统计              |
| GET                 | `/api/admin/users`                        | 用户列表                |
| PUT                 | `/api/admin/users/{id}`                   | 更新用户（禁用/改角色） |
| GET                 | `/api/admin/content/pending`              | 待审核内容              |
| PUT                 | `/api/admin/content/diseases/{id}/review` | 审核疾病                |
| PUT                 | `/api/admin/content/drugs/{id}/review`    | 审核药品                |
| GET/POST/PUT/DELETE | `/api/admin/diseases`                     | 疾病 CRUD               |
| GET/POST/PUT/DELETE | `/api/admin/drugs`                        | 药品 CRUD               |
| GET                 | `/api/admin/audit-logs`                   | 审计日志                |

### 其他公开接口

| 方法            | 路径                       | 说明                  |
| --------------- | -------------------------- | --------------------- |
| GET             | `/api/diseases`            | 疾病列表（搜索/分页） |
| GET             | `/api/diseases/{id}`       | 疾病详情              |
| GET             | `/api/drugs`               | 药品列表（搜索/分页） |
| GET             | `/api/drugs/{id}`          | 药品详情              |
| GET/PUT         | `/api/users/profile`       | 查看/更新个人资料     |
| GET/PUT         | `/api/health-records`      | 查看/更新健康档案     |
| GET/POST/DELETE | `/api/favorites`           | 收藏列表/添加/取消    |
| GET/POST        | `/api/contact`             | 联系留言 列表/提交    |
| GET             | `/api/symptoms/categories` | 症状分类列表          |

## 配置说明

所有敏感凭据通过环境变量管理，`.env` 文件存放实际值（已加入 `.gitignore`）。

```bash
# 数据库
SPRING_DATASOURCE_PASSWORD=your_db_password

# Redis
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379

# JWT
JWT_SECRET=your-256-bit-secret

# AI 服务 (DeepSeek Anthropic API)
AI_API_KEY=sk-your-api-key
AI_BASE_URL=https://api.deepseek.com/anthropic
AI_MODEL=deepseek-v4-flash

# 邮件（QQ邮箱 SMTP）
MAIL_USERNAME=your_email@qq.com
MAIL_PASSWORD=your_smtp_authorization_code
```

## 测试账户

| 角色   | 用户名     | 密码     |
| ------ | ---------- | -------- |
| 管理员 | admin      | admin123 |
| 医生   | testdoctor | 123456   |
| 用户   | testuser   | 123456   |

## 许可证

本项目采用 [MIT License](../LICENSE) 开源许可。

---

<p align="center">
  <strong>VitaAI 智慧医院系统</strong> — 让医疗服务更智能、更便捷、更安全
</p>

