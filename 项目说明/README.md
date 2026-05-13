# 智慧医院系统 / Smart Hospital System

<p align="center">
  <img src="docs/images/logo.png" alt="Smart Hospital System Logo" width="200"/>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/version-v1.0.0-blue.svg" alt="Version"/>
  <img src="https://img.shields.io/badge/JDK-17+-green.svg" alt="JDK"/>
  <img src="https://img.shields.io/badge/Vue-3.x-green.svg" alt="Vue"/>
  <img src="https://img.shields.io/badge/Spring-Boot3.x-green.svg" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/license-MIT-orange.svg" alt="License"/>
</p>

---

## 项目简介 / Introduction

智慧医院系统是一个基于人工智能和大数据技术的医疗辅助平台，旨在为学校、工厂、监狱等不方便前往医院的人群提供便捷、安全的医疗服务。系统整合了医生诊断案例，将其转化为"skills"并植入主流大模型中，实现精准的病情判断。

Smart Hospital System is an AI-powered medical assistance platform that provides convenient and secure medical services for people in schools, factories, prisons, and other locations where access to hospitals is limited.

## 核心特性 / Key Features

| 特性 | 说明 |
|------|------|
| 🤖 **AI智能诊断** | 基于大模型的智能诊断助手，支持多轮对话、诊断报告生成 |
| 📋 **健康档案** | 个人健康档案管理，记录病史、过敏史、用药记录 |
| 🔍 **症状自测** | 结构化问卷引导，智能评估健康状况 |
| 🏥 **医疗内容管理** | 疾病管理、药品管理、科研成果管理 |
| 🔒 **数据安全保障** | 数据脱敏、操作审计、合规认证 |
| 📊 **诊疗统计** | 诊疗数据统计分析，诊断准确率追踪 |

## 技术栈 / Tech Stack

### 后端技术

| 技术 | 说明 |
|------|------|
| Java 17+ | 编程语言 |
| Spring Boot 3.x | 应用框架 |
| Spring Cloud | 微服务架构 |
| Spring Security + JWT | 安全认证 |
| MySQL 8.0 | 主数据库 |
| Elasticsearch 8.x | 全文搜索引擎 |
| Redis 7.0 | 缓存服务 |
| MinIO | 对象存储 |

### 前端技术

| 技术 | 说明 |
|------|------|
| Vue 3.x | 前端框架 |
| TypeScript | 类型系统 |
| Pinia | 状态管理 |
| Element Plus | UI组件库 |
| Vite | 构建工具 |

### AI集成

| 技术 | 说明 |
|------|------|
| OpenAI GPT API | 大语言模型 |
| 通义千问 | 国内大模型 |
| 自定义Skills | 诊断技能库 |

## 系统架构 / Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Frontend (Vue.js)                     │
├─────────────────────────────────────────────────────────────┤
│                    API Gateway (Spring Cloud)                 │
├─────────────────────────────────────────────────────────────┤
│  ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌─────────┐│
│  │   User    │  │  Disease  │  │   Drug    │  │   AI    ││
│  │  Service  │  │  Service   │  │  Service  │  │ Service ││
│  └───────────┘  └───────────┘  └───────────┘  └─────────┘│
├─────────────────────────────────────────────────────────────┤
│   MySQL   │   Redis   │   Elasticsearch   │   MinIO    │
└─────────────────────────────────────────────────────────────┘
```

## 功能模块 / Modules

### 用户端功能

- ✅ 用户注册登录（邮箱验证）
- ✅ 个人资料管理
- ✅ 健康档案管理（病史、过敏史、用药记录）
- ✅ 症状自测工具
- ✅ AI医生多轮对话
- ✅ 历史诊断记录
- ✅ 诊断报告生成（PDF导出）
- ✅ 诊断反馈机制
- ✅ 用户收藏

### 医生端功能

- ✅ 医生认证注册
- ✅ 患者健康档案查看
- ✅ AI辅助诊断
- ✅ 疾病发布管理
- ✅ 药品发布管理
- ✅ 科研成果发布
- ✅ 内容审核
- ✅ 诊疗数据统计

### 管理员端功能

- ✅ 用户管理
- ✅ 疾病管理
- ✅ 药品管理
- ✅ Skills管理
- ✅ 内容审核
- ✅ 用户反馈处理
- ✅ 操作审计日志
- ✅ 系统配置

## 快速开始 / Quick Start

### 环境要求

| 软件 | 版本要求 |
|------|----------|
| JDK | 17+ |
| Node.js | 18+ |
| MySQL | 8.0+ |
| Redis | 6.0+ |
| Elasticsearch | 8.x |
| Maven | 3.8+ |
| Docker | 20.10+ |

### 安装部署

#### 方式一：Docker部署（推荐）

```bash
# 1. 克隆项目
git clone https://github.com/your-username/smart-hospital-system.git
cd smart-hospital-system

# 2. 配置环境变量
cp docker/.env.example docker/.env
# 编辑 .env 文件，填入实际配置

# 3. 启动所有服务
docker-compose up -d

# 4. 查看服务状态
docker-compose ps

# 5. 访问系统
# 前端: http://localhost
# API: http://localhost:8080
# MinIO Console: http://localhost:9001
```

#### 方式二：本地开发

**后端启动**

```bash
# 1. 进入后端目录
cd backend

# 2. 配置数据库
# 修改 src/main/resources/application.yml

# 3. 安装依赖
mvn clean install

# 4. 启动应用
mvn spring-boot:run
```

**前端启动**

```bash
# 1. 进入前端目录
cd frontend

# 2. 安装依赖
npm install

# 3. 启动开发服务器
npm run dev

# 4. 访问系统
# http://localhost:5173
```

### Kubernetes部署

```bash
# 添加Helm仓库
helm repo add bitnami https://charts.bitnami.com/bitnami

# 创建命名空间
kubectl create namespace smart-hospital

# 部署
helm upgrade --install smart-hospital ./smart-hospital \
  --namespace smart-hospital \
  --values ./values-prod.yaml
```

## 项目结构 / Structure

```
smart-hospital-system/
├── backend/                         # 后端项目
│   ├── src/main/java/
│   │   └── com/smarthospital/
│   │       ├── config/            # 配置类
│   │       ├── controller/         # 控制器
│   │       ├── service/            # 服务层
│   │       ├── repository/         # 数据访问层
│   │       ├── entity/             # 实体类
│   │       ├── dto/                # 数据传输对象
│   │       ├── security/           # 安全配置
│   │       ├── audit/              # 审计日志
│   │       └── exception/          # 异常处理
│   ├── src/main/resources/
│   │   ├── application.yml        # 应用配置
│   │   └── logback-spring.xml     # 日志配置
│   └── pom.xml
│
├── frontend/                        # 前端项目
│   ├── src/
│   │   ├── api/                   # API接口
│   │   ├── assets/                # 静态资源
│   │   ├── components/            # 组件
│   │   ├── router/               # 路由配置
│   │   ├── stores/                # 状态管理
│   │   ├── views/                 # 页面视图
│   │   ├── utils/                 # 工具函数
│   │   └── App.vue
│   ├── package.json
│   └── vite.config.ts
│
├── docs/                           # 项目文档
│   ├── 需求分析.md
│   ├── 数据库设计.md
│   ├── 接口文档.md
│   ├── 部署说明.md
│   └── images/
│
├── docker/                         # Docker配置
│   ├── docker-compose.yml
│   ├── .env
│   ├── mysql/
│   ├── redis/
│   ├── elasticsearch/
│   └── nginx/
│
└── README.md
```

## API接口 / API Reference

### 认证接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/auth/register` | POST | 用户注册 |
| `/api/auth/login` | POST | 用户登录 |
| `/api/auth/refresh` | POST | 刷新Token |
| `/api/auth/logout` | POST | 用户登出 |

### 用户接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/users/profile` | GET | 获取用户资料 |
| `/api/users/profile` | PUT | 更新用户资料 |

### AI诊断接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/ai/chat` | POST | 发送诊断消息 |
| `/api/ai/chat/stream` | POST | 流式对话 |
| `/api/ai/diagnoses` | GET | 获取诊断记录 |
| `/api/ai/diagnoses/{id}/report` | POST | 生成诊断报告 |
| `/api/ai/diagnoses/{id}/feedback` | POST | 提交诊断反馈 |

详细API文档请参考 [接口文档](docs/接口文档.md)

## 配置说明 / Configuration

### 环境变量

```bash
# 数据库配置
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=smart_hospital
MYSQL_USERNAME=root
MYSQL_PASSWORD=your_password

# Redis配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_password

# Elasticsearch配置
ES_HOSTS=localhost:9200

# JWT配置
JWT_SECRET=your-256-bit-secret
JWT_EXPIRATION=7200000

# AI服务配置
AI_API_KEY=your-api-key
AI_BASE_URL=https://api.openai.com
```

### 配置文件

| 环境 | 配置文件 |
|------|----------|
| 开发环境 | `application-dev.yml` |
| 测试环境 | `application-test.yml` |
| 生产环境 | `application-prod.yml` |

## 开发指南 / Development

### 代码规范

- 遵循 [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- 使用有意义的变量和方法命名
- 添加必要的注释
- 提交前进行代码审查

### Git提交规范

```
<type>(<scope>): <subject>

# 类型
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 重构
test: 测试相关
chore: 构建/工具相关

# 示例
feat(ai): 添加多轮对话功能
fix(user): 修复登录问题
docs(api): 更新接口文档
```

### 测试

```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn verify

# 生成测试覆盖率报告
mvn test jacoco:report
```

## 监控运维 / Operations

### 健康检查

```bash
# API健康检查
curl http://localhost:8080/actuator/health

# 详细健康信息
curl http://localhost:8080/actuator/health/details
```

### 监控指标

系统集成了以下监控：

- **Prometheus**: 指标采集
- **Grafana**: 可视化展示
- **ELK**: 日志收集和分析
- **Sentinel**: 流量控制

### 日志查看

```bash
# 查看应用日志
kubectl logs -f deployment/smart-hospital-backend

# 查看MySQL慢查询
kubectl exec -it mysql-master -- mysql -e "SHOW FULL PROCESSLIST;"
```

## 常见问题 / FAQ

### Q1: 如何申请AI服务API Key？

A: 请访问 [OpenAI官网](https://platform.openai.com/) 或 [阿里云DashScope](https://dashscope.console.aliyun.com/) 申请API Key。

### Q2: 如何配置多个AI模型？

A: 在 `application.yml` 中配置 `ai.models` 列表，系统会根据请求类型自动选择合适的模型。

### Q3: 如何导出诊断报告？

A: 调用 `/api/ai/diagnoses/{id}/report` 接口，系统会生成PDF格式的诊断报告。

### Q4: 如何进行数据备份？

A: 请参考 [部署说明文档](docs/部署说明.md#备份恢复)

## 贡献指南 / Contributing

欢迎提交Pull Request或Issue！

1. Fork本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建Pull Request

## 版本历史 / Changelog

| 版本 | 日期 | 说明 |
|------|------|------|
| v1.0.0 | 2026-05-13 | 正式版本发布 |

## 许可证 / License

本项目采用 [MIT License](LICENSE) 开源许可。

## 联系方式 / Contact

- **项目地址**: [GitHub Repository](https://github.com/your-username/smart-hospital-system)
- **问题反馈**: [GitHub Issues](https://github.com/your-username/smart-hospital-system/issues)
- **邮箱**: 2045562382@qq.com

## 致谢 / Acknowledgments

- 感谢所有为项目做出贡献的开发者
- 感谢使用和测试本系统的用户
- 感谢以下开源项目：
  - [Spring Boot](https://spring.io/projects/spring-boot)
  - [Vue.js](https://vuejs.org/)
  - [Element Plus](https://element-plus.org/)
  - [Elasticsearch](https://www.elastic.co/)
  - [MySQL](https://www.mysql.com/)

---

<p align="center">
  <strong>智慧医院系统</strong> - 让医疗服务更智能、更便捷、更安全
</p>

<p align="center">
  Made with ❤️ by 夏保霖
</p>
