# RBAC Admin 权限管理系统

一个基于 Spring Boot + Vue 3 的现代化 RBAC 权限管理系统，集成 AI 智能助手功能。

## 项目简介

本项目是一个功能完善的通用权限管理系统，采用前后端分离架构，支持细粒度的权限控制，适用于企业级后台管理系统开发。

## 技术栈

### 后端

- **框架**: Spring Boot 3.2.5
- **安全**: Spring Security + JWT
- **数据库**: PostgreSQL + MyBatis-Plus
- **缓存**: Redis
- **API 文档**: Knife4j (Swagger)
- **AI 集成**: SiliconFlow API (DeepSeek R1)

### 前端

- **框架**: Vue 3
- **状态管理**: Pinia
- **UI 组件**: Element Plus
- **路由**: Vue Router 4
- **构建工具**: Vite

## 核心功能

### 用户权限管理

- 🔐 用户管理（CRUD、状态控制、批量操作）
- 👥 角色管理（角色权限分配、数据权限）
- 📋 菜单管理（动态路由、按钮级别权限）
- 🏢 部门管理（树形结构、组织架构）
- 📊 字典管理（系统字典、动态数据）

### 系统监控

- 💓 健康检查（数据库、Redis 连接状态）
- 📈 性能指标（内存、线程、JVM 信息）
- 📝 操作日志（完整记录用户操作）
- 🔐 登录日志（登录历史、IP 追踪）
- ⚠️ 风险告警（异常访问检测）

### AI 智能助手

- 🤖 AI 对话（支持多轮对话）
- 📝 Markdown 渲染（代码高亮、表格、列表）
- ⏳ 流式响应（打字机效果）
- 💾 对话持久化（历史记录保存）

### 安全特性

- 🔑 JWT 无状态认证
- 🚫 Token 黑名单机制
- 📊 API 敏感度检测
- ⏱️ 接口频率限制
- 🛡️ 敏感数据加密（AES）
- 📝 操作日志脱敏

### 性能优化

- 📇 数据库索引优化
- 💾 Redis 多级缓存（字典、用户、路由）
- 🔄 Token 滑动窗口续期
- ⚡ 异步任务处理

## 项目结构

```
rbac-admin-system/
├── rbac-admin/                    # 后端项目
│   └── src/main/java/com/admin/
│       ├── annotation/            # 自定义注解
│       ├── aspect/                # AOP 切面
│       ├── common/                # 通用类
│       │   ├── constant/          # 常量定义
│       │   ├── controller/        # 控制器基类
│       │   ├── exception/         # 异常处理
│       │   └── result/            # 响应封装
│       ├── config/                # 配置类
│       ├── controller/            # 控制器
│       ├── dto/                   # 数据传输对象
│       ├── entity/                # 实体类
│       ├── handler/               # 处理器
│       ├── mapper/                # 数据库映射
│       ├── security/              # 安全相关
│       ├── service/               # 业务层
│       └── util/                  # 工具类
│
├── rbac-ui/                       # 前端项目
│   └── src/
│       ├── api/                   # API 接口
│       ├── layout/                # 布局组件
│       ├── router/                # 路由配置
│       ├── stores/                # 状态管理
│       ├── utils/                # 工具函数
│       └── views/                 # 页面组件
│
└── docs/                          # 文档
    └── sql/                       # SQL 脚本
```

## 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- PostgreSQL 14+
- Redis 6+

### 后端启动

```bash
# 1. 创建数据库
psql -U postgres -c "CREATE DATABASE admin_system;"

# 2. 修改数据库配置
# 编辑 rbac-admin/src/main/resources/application.yml

# 3. 运行 SQL 脚本
psql -U postgres -d admin_system -f docs/sql/*.sql

# 4. 启动后端
cd rbac-admin
mvn spring-boot:run
```

### 前端启动

```bash
cd rbac-ui
npm install
npm run dev
```

### 默认账号

```
用户名: admin
密码: 123456
```

## API 文档

启动后访问: http://localhost:8080/doc.html

### 主要接口

| 模块 | 路径 | 说明 |
|------|------|------|
| 认证 | `/api/auth/*` | 登录、刷新 Token |
| 用户 | `/api/user/*` | 用户管理 |
| 角色 | `/api/role/*` | 角色管理 |
| 菜单 | `/api/menu/*` | 菜单管理 |
| 部门 | `/api/dept/*` | 部门管理 |
| 字典 | `/api/dict/*` | 字典管理 |
| AI | `/api/ai/*` | AI 智能助手 |
| 监控 | `/api/monitor/*` | 系统监控 |

## 系统截图

### 登录页面
![登录](docs/images/login.png)

### 工作台
![工作台](docs/images/dashboard.png)

### AI 助手
![AI 助手](docs/images/ai-chat.png)

## 改进记录

本项目经过多次迭代，持续优化中：

- ✅ AI 聊天打字机效果
- ✅ BaseController 抽取
- ✅ Markdown 渲染优化
- ✅ AI 多轮对话支持
- ✅ DTO 参数校验
- ✅ 操作日志脱敏
- ✅ 接口频率限制
- ✅ 敏感数据加密
- ✅ Token 黑名单机制
- ✅ Redis 缓存策略
- ✅ 数据库索引优化
- ✅ 全局异常处理
- ✅ 健康检查接口

详细改进内容请查看 [改进记录](docs/改进记录.md)

## 安全建议

生产环境部署时请注意：

1. **修改 JWT 密钥**
   ```bash
   export JWT_SECRET=your-secure-secret-key
   ```

2. **配置数据库密码**
   ```bash
   export DB_PASSWORD=your-db-password
   ```

3. **配置 Redis 密码**
   ```bash
   export REDIS_PASSWORD=your-redis-password
   ```

4. **配置 AI API Key**
   ```bash
   export AI_API_KEY=your-siliconflow-api-key
   ```

5. **启用 Knife4j 文档**（仅开发环境）
   ```yaml
   knife4j:
     enable: true
   ```

## License

MIT License

## 联系方式

- GitHub: https://github.com/YZW-nb/rbac-admin-system
- Issue: https://github.com/YZW-nb/rbac-admin-system/issues

---

⭐ 如果这个项目对你有帮助，请 star 支持一下！
