# Memory — 情侣回忆共享

情侣间的回忆共享应用，支持日常记录、重要日子倒计时、心愿清单、回忆时间线、相册管理、足迹地图、日历心情追踪等功能。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 3.5 + Maven + JDBC (JdbcTemplate) |
| 数据库 | MySQL 8.0 |
| Web 前端 | Vue 3 + Vite + Axios |
| Android | 原生 Android (Java 17) + MVVM |

## 功能模块

- **首页** — 情侣信息、头像上传、重要日子倒计时
- **日常记录** — 每日日记，支持多图上传、心情标记、年月筛选
- **日历** — 自定义日历网格，每日心情记录，备注标记，自定义表情
- **心愿清单** — 心愿 CRUD，按状态/分类/发起人筛选，完成标记
- **回忆** — 三个子模块：足迹地图、记忆时间线、相册管理

## 快速开始

### 1. 数据库

创建 MySQL 数据库并导入表结构：

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS memory_db DEFAULT CHARSET utf8mb4"
mysql -u root -p memory_db < src/main/resources/memory_db.sql
```

### 2. 后端

```bash
# 复制配置文件并填入实际值
cp src/main/resources/application-sample.yml src/main/resources/application.yml

# 启动（默认端口 8081）
./mvnw spring-boot:run
```

### 3. Web 前端

```bash
cd frontend
npm install
npm run dev        # 开发服务器 http://localhost:5174
```

### 4. Android

用 Android Studio 打开 `memory/` 目录，或命令行构建：

```bash
cd memory
./gradlew assembleDebug
```

## 项目结构

```
├── src/main/java/com/niit/memory/   # Spring Boot 后端
│   ├── controller/                  # REST 控制器
│   ├── service/                     # 业务逻辑层
│   ├── repository/                  # 数据访问层（原生 JDBC）
│   ├── entity/                      # 数据实体
│   └── config/                      # JWT、CORS、文件上传等配置
├── src/main/resources/
│   ├── memory_db.sql                # 数据库建表脚本
│   ├── update_*.sql                 # 增量迁移脚本
│   └── application-sample.yml       # 配置模板
├── frontend/                        # Vue 3 Web 前端
├── memory/                          # Android 原生应用
└── CLAUDE.md                        # AI 辅助开发指南
```

## 配置说明

复制 `application-sample.yml` 为 `application.yml` 并修改：

- `spring.datasource.*` — MySQL 连接信息
- `app.jwt.secret` — JWT 签名密钥（务必修改）
- `app.qiniu.*` — 七牛云存储配置（用于图片上传）
- `server.port` — 后端端口，默认 8081

## API 概览

| 路径 | 说明 |
|------|------|
| `/api/auth/*` | 登录认证 |
| `/api/couple` | 情侣信息 |
| `/api/daily-records` | 日常记录 |
| `/api/calendar/*` | 日历心情与备注 |
| `/api/wishes` | 心愿清单 |
| `/api/albums` `/api/moments` `/api/locations` | 回忆（相册/瞬间/足迹） |
| `/api/important-dates` | 重要日子 |
| `/api/custom-emojis` | 自定义表情 |
| `/api/qiniu/*` | 图片上传 Token |

所有 API 返回统一格式：`{ code, message, data }`

## License

MIT
