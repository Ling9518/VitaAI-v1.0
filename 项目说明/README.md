# VitaAI 智慧医院系统

<p align="center">
  <img src="https://img.shields.io/badge/version-v1.0.0-blue.svg" alt="Version"/>
  <img src="https://img.shields.io/badge/JDK-17+-green.svg" alt="JDK"/>
  <img src="https://img.shields.io/badge/Vue-3.x-green.svg" alt="Vue"/>
  <img src="https://img.shields.io/badge/Spring_Boot-3.2.5-green.svg" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/license-MIT-orange.svg" alt="License"/>
</p>

---

## 项目简介

VitaAI 是一个基于人工智能的医疗辅助平台，旨在为学校、工厂、监狱等不便前往医院的人群提供便捷、安全的医疗服务。系统整合医生诊断案例，将其转化为"Skills"并植入大模型中，实现精准的病情判断。

## 核心特性

| 特性 | 说明 |
|------|------|
| AI智能诊断 | 基于 DeepSeek 大模型的智能诊断助手，支持多轮对话、诊断报告生成 |
| 健康档案 | 个人健康档案管理，记录病史、过敏史、用药记录 |
| 在线问诊 | 用户留言咨询，医生回复，支持状态跟踪 |
| 医疗内容管理 | 疾病管理、药品管理，含内容审核流程 |
| 数据安全保障 | Jasypt 加密敏感配置、操作审计日志、角色权限控制 |
| 多角色支持 | 游客、用户、医生、管理员四种角色，各有专属工作台 |

## 技术栈

### 后端

| 技术 | 说明 |
|------|------|
| Java 21 | 编程语言 |
| Spring Boot 3.2.5 | 应用框架 |
| Spring Security + JWT | 安全认证（accessToken 2h + refreshToken 7d） |
| Spring Data JPA + Hibernate | 数据访问 |
| MySQL 8.0 | 关系数据库 |
| Redis 7.0 | Token 缓存与黑名单 |
| Jasypt | 配置文件加密 |

### 前端

| 技术 | 说明 |
|------|------|
| Vue 3.x | 前端框架 |
| TypeScript | 类型系统 |
| Pinia | 状态管理 |
| Element Plus | UI 组件库 |
| Axios | HTTP 客户端 |
| Vite | 构建工具 |

### AI 集成

| 技术 | 说明 |
|------|------|
| DeepSeek (astron-code-latest) | 大语言模型，通过 Spark 代理调用 |
| 自定义 Skills | 诊断技能库，文件系统存储 |

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

## 功能模块

### 用户端
- 用户注册登录（邮箱验证）
- 个人资料管理
- 健康档案管理（病史、过敏史、用药记录、家族病史）
- AI 医生多轮对话诊断
- 历史诊断记录查看
- 在线问诊（留言/医生回复）
- 疾病/药品浏览与搜索
- 用户收藏

### 医生端
- 仪表盘统计（患者数、诊断数、待审核数）
- 患者列表与健康档案查看
- 诊断记录查看
- 在线问诊回复（回复/编辑/撤回/状态标记）
- 疾病反馈（新增/修改建议，待管理员审核）
- 药品反馈（新增/修改建议，待管理员审核）
- AI 辅助诊断

### 管理员端
- 仪表盘统计（用户/医生/疾病/药品/诊断/待审核数量）
- 用户管理（角色筛选、启用/禁用、角色变更）
- 内容审核（疾病/药品 APPROVED/REJECTED）
- 疾病/药品完整 CRUD
- 在线问诊管理（编辑双方内容）
- Skills 管理
- 操作审计日志
- 用户反馈处理

## 快速开始

### 环境要求

| 软件 | 版本要求 |
|------|----------|
| JDK | 17+ |
| Node.js | 18+ |
| MySQL | 8.0+ |
| Redis | 6.0+ |
| Maven | 3.8+ |
| Docker | 20.10+（可选） |

### Docker 部署（推荐）

```bash
# 1. 克隆项目
git clone https://github.com/your-username/vitaai.git
cd vitaai

# 2. 配置环境变量（设置 JASYPT_MASTER_PASSWORD 等）
cp docker/.env.example docker/.env

# 3. 启动所有服务（MySQL + Redis + Backend + Frontend）
docker-compose up -d

# 4. 访问系统
# 前端: http://localhost
# 后端 API: http://localhost:8080
```

### 本地开发

**后端启动**

```bash
cd backend

# 配置 application.yml 中的数据库连接信息
# 敏感配置已使用 Jasypt 加密，通过环境变量 JASYPT_MASTER_PASSWORD 解密

mvn clean compile
mvn spring-boot:run
# 运行在 http://localhost:8080
```

**前端启动**

```bash
cd frontend

npm install
npm run dev
# 运行在 http://localhost:5173
```

## 项目结构

```
VitaAI4/
├── backend/                                    # 后端 (Spring Boot)
│   ├── src/main/java/com/vitaai/
│   │   ├── config/                             # 安全配置、CORS 配置
│   │   ├── controller/                         # 控制器 (14个)
│   │   │   ├── AuthController.java             #   认证（登录/注册/Token 刷新）
│   │   │   ├── AdminController.java            #   管理员（用户管理/审核/疾病CRUD/药品CRUD）
│   │   │   ├── DoctorController.java           #   医生（患者/诊断/疾病反馈/药品反馈）
│   │   │   ├── MessageController.java          #   在线问诊
│   │   │   ├── DiseaseController.java          #   疾病公开接口
│   │   │   ├── DrugController.java             #   药品公开接口
│   │   │   ├── UserController.java             #   用户资料
│   │   │   ├── HealthRecordController.java     #   健康档案
│   │   │   ├── FavoriteController.java         #   收藏夹
│   │   │   ├── AIController.java               #   AI 诊断对话 & Skills 管理
│   │   │   ├── SymptomController.java          #   症状自测
│   │   │   ├── ReviewController.java           #   用户反馈
│   │   │   ├── SystemController.java           #   系统配置
│   │   │   └── ContactMessageController.java   #   联系留言
│   │   ├── service/                            # 服务层 (8个)
│   │   ├── repository/                         # 数据访问层 (Spring Data JPA)
│   │   ├── entity/                             # 实体类 (20个)
│   │   ├── dto/                                # 数据传输对象
│   │   ├── ai/                                 # AI 客户端 (DeepSeekClient)
│   │   ├── security/                           # JWT 过滤器与工具
│   │   └── audit/                              # 审计日志
│   ├── src/main/resources/
│   │   ├── application.yml                     # 应用配置（Jasypt 加密）
│   │   └── schema.sql                          # 数据库建表脚本
│   └── pom.xml
│
├── frontend/                                   # 前端 (Vue 3)
│   ├── src/
│   │   ├── api/                                # API 接口封装 & Axios 实例
│   │   ├── components/layout/                  # 布局组件
│   │   ├── router/                             # 路由配置（含权限守卫）
│   │   ├── stores/                             # Pinia 状态管理
│   │   ├── views/
│   │   │   ├── home/                           # 主页、联系留言、404
│   │   │   ├── auth/                           # 登录、注册
│   │   │   ├── ai/                             # AI 诊断对话、历史记录
│   │   │   ├── disease/                        # 疾病列表、疾病详情
│   │   │   ├── drug/                           # 药品列表、药品详情
│   │   │   ├── health/                         # 健康档案
│   │   │   ├── user/                           # 个人中心、收藏、在线问诊
│   │   │   ├── admin/                          # 管理员后台
│   │   │   └── doctor/                         # 医生后台
│   │   ├── utils/                              # 工具函数
│   │   └── main.ts                             # 入口文件
│   ├── package.json
│   └── vite.config.ts
│
├── docker/                                     # Docker 配置
│   ├── docker-compose.yml
│   ├── .env.example
│   └── nginx/
│
├── vita-skills/                                # AI 诊断 Skills 库
├── 项目说明/                                   # 项目文档
└── README.md
```

## API 接口

### 认证接口 — `/api/auth`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/login` | 用户登录（返回 JWT） |
| POST | `/api/auth/refresh` | 刷新 Token |
| POST | `/api/auth/logout` | 用户登出 |

### AI 诊断接口 — `/api/ai`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/ai/chat` | 发送诊断消息 |
| POST | `/api/ai/chat/stream` | 流式对话 (SSE) |
| GET | `/api/ai/diagnoses` | 诊断记录列表 |
| GET | `/api/ai/diagnoses/{id}` | 诊断详情 |
| DELETE | `/api/ai/diagnoses/{id}` | 删除诊断记录 |
| POST | `/api/ai/diagnoses/{id}/feedback` | 提交诊断反馈 |
| GET | `/api/ai/skills` | Skills 列表 |
| POST | `/api/ai/skills` | 新增 Skill |
| PUT | `/api/ai/skills/{id}` | 更新 Skill |
| DELETE | `/api/ai/skills/{id}` | 删除 Skill |
| POST | `/api/ai/skills/upload` | 上传 Skill 文件 |
| POST | `/api/ai/skills/sync` | 同步 Skills |

### 管理员接口 — `/api/admin`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/stats` | 仪表盘统计 |
| GET | `/api/admin/users` | 用户列表 |
| PUT | `/api/admin/users/{id}` | 更新用户（禁用/改角色） |
| GET | `/api/admin/content/pending` | 待审核内容 |
| PUT | `/api/admin/content/diseases/{id}/review` | 审核疾病 |
| PUT | `/api/admin/content/drugs/{id}/review` | 审核药品 |
| GET/POST/PUT/DELETE | `/api/admin/diseases` | 疾病 CRUD |
| GET/POST/PUT/DELETE | `/api/admin/drugs` | 药品 CRUD |
| GET | `/api/admin/audit-logs` | 审计日志 |

### 医生接口 — `/api/doctor`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/doctor/stats` | 医生仪表盘 |
| GET | `/api/doctor/patients` | 患者列表 |
| GET | `/api/doctor/diagnoses` | 诊断记录 |
| GET/POST/PUT | `/api/doctor/diseases` | 疾病反馈（建议审核） |
| GET/POST/PUT | `/api/doctor/drugs` | 药品反馈（建议审核） |

### 在线问诊接口 — `/api/messages`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/messages/stats` | 留言统计 |
| GET/POST | `/api/messages` | 留言列表 / 提交留言 |
| PUT/DELETE | `/api/messages/{id}` | 编辑 / 删除留言 |
| PUT | `/api/messages/{id}/reply` | 医生回复 |
| DELETE | `/api/messages/{id}/reply` | 撤回回复 |
| PUT | `/api/messages/{id}/status` | 切换已解决/未解决 |
| PUT | `/api/messages/admin/{id}` | 管理员编辑留言和回复 |

### 其他公开接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/diseases` | 疾病列表（搜索/分页） |
| GET | `/api/diseases/{id}` | 疾病详情 |
| GET | `/api/drugs` | 药品列表（搜索/分页） |
| GET | `/api/drugs/{id}` | 药品详情 |
| GET/PUT | `/api/users/profile` | 查看/更新个人资料 |
| GET/PUT | `/api/health-records` | 查看/更新健康档案 |
| GET/POST/DELETE | `/api/favorites` | 收藏列表/添加/取消 |

详细 API 文档请参考 [接口文档](接口文档.md)

## 配置说明

### 环境变量

```bash
# 数据库配置
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=vitaai
MYSQL_USERNAME=root
MYSQL_PASSWORD=your_password

# Redis 配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_password

# JWT 配置
JWT_SECRET=your-256-bit-secret
JWT_ACCESS_EXPIRATION=7200000
JWT_REFRESH_EXPIRATION=604800000

# Jasypt 加密主密码
JASYPT_MASTER_PASSWORD=your-master-password

# AI 服务配置
DEEPSEEK_API_URL=https://spark-api.example.com
DEEPSEEK_API_KEY=your-api-key
```

敏感配置（数据库密码、API Key 等）在 `application.yml` 中使用 Jasypt `ENC()` 加密存储，通过环境变量 `JASYPT_MASTER_PASSWORD` 解密。

## 测试账户

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 医生 | testdoctor | 123456 |
| 医生 | drzhang | 123456 |
| 用户 | testuser | 123456 |

## 许可证

本项目采用 [MIT License](LICENSE) 开源许可。

---

<p align="center">
  <strong>VitaAI 智慧医院系统</strong> — 让医疗服务更智能、更便捷、更安全
</p>
