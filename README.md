一个简洁美观的图片管理平台，支持私有空间和团队空间管理，集成 AI 扩图功能。

## 功能特性

### 基础功能

- **用户系统** - 注册、登录、用户管理，支持普通用户、管理员、VIP 角色
- **图片上传** - 支持本地上传和 URL 批量导入，自动生成缩略图
- **图片管理** - 搜索、分类、标签管理，支持 WebP 格式压缩
- **空间管理** - 私有空间和团队空间，支持空间容量和数量限制

### 团队协作

- **团队空间** - 创建团队空间，邀请成员加入
- **权限控制** - 三种角色（浏览者/编辑者/管理员），细粒度权限控制
- **空间分析** - 图片分类统计、标签分析、空间使用分析

### AI 功能

- **智能扩图** - 基于阿里云通义万相的 AI 扩图能力
- **图片剪裁** - 灵活的图片剪裁工具

### 技术亮点

- 分库分表设计（ShardingSphere）
- Redis 会话管理
- Sa-Token 权限认证
- 腾讯云 COS 对象存储
- 实时图片编辑（WebSocket + Disruptor）

## 技术栈

### 后端

- Java 17
- Spring Boot 2.7
- MyBatis-Plus 3.5
- ShardingSphere 5.2（分库分表）
- Redis + Spring Session
- Sa-Token（权限认证）
- 腾讯云 COS（图片存储）
- 阿里云 DashScope（AI 扩图）

### 前端

- Vue 3 + Composition API
- TypeScript
- Vite
- Ant Design Vue
- Pinia（状态管理）

## 项目结构

```
huan-picture/
├── huan-picture-backend/        # 后端服务
│   ├── src/main/java/
│   │   └── com/huan/huanpicture/
│   │       ├── api/             # 第三方 API 调用
│   │       ├── config/          # 配置类
│   │       ├── controller/      # 控制器
│   │       ├── manager/         # 业务管理器
│   │       ├── mapper/          # 数据层
│   │       ├── model/           # 数据模型
│   │       ├── service/         # 服务层
│   │       └── ...
│   └── src/main/resources/
│       └── application.yml      # 配置文件
│
├── huan-picture-frontend/       # 前端应用
│   ├── src/
│   │   ├── api/                # API 接口
│   │   ├── components/         # 公共组件
│   │   ├── pages/              # 页面组件
│   │   ├── router/             # 路由配置
│   │   └── stores/             # 状态管理
│   └── ...
│
└── sql/                         # 数据库脚本
    └── create_table.sql
```

## 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+

### 后端启动

1. 创建数据库

```sql
mysql -u root -p < sql/create_table.sql
```

2. 配置环境变量

```bash
cd huan-picture-backend
cp .env.example .env
# 编辑 .env 填入数据库、Redis、云存储等配置
```

3. 启动服务

```bash
# 使用 Maven 启动
mvn spring-boot:run

# 或打包后运行
mvn package
java -jar target/huan-picture-0.0.1-SNAPSHOT.jar
```

### 前端启动

```bash
cd huan-picture-frontend
npm install
npm run dev
```

访问 http://localhost:5173

### 环境变量说明

| 变量名         | 说明             | 示例                                     |
| -------------- | ---------------- | ---------------------------------------- |
| DB_URL         | 数据库连接       | jdbc:mysql://localhost:3306/huan_picture |
| DB_USERNAME    | 数据库用户名     | root                                     |
| DB_PASSWORD    | 数据库密码       | your_password                            |
| REDIS_HOST     | Redis 地址       | localhost                                |
| REDIS_PASSWORD | Redis 密码       | your_password                            |
| COS_SECRET_ID  | 腾讯云 SecretId  | your_secret_id                           |
| COS_SECRET_KEY | 腾讯云 SecretKey | your_secret_key                          |
| COS_REGION     | COS 区域         | ap-guangzhou                             |
| COS_BUCKET     | COS 存储桶       | your-bucket                              |
| ALIYUN_APIKEY  | 阿里云 API Key   | your_apikey                              |

## 接口文档

启动后端服务后访问：http://localhost:8848/api/doc.html

## 数据库设计

- **user** - 用户表
- **picture** - 图片表
- **space** - 空间表
- **space_user** - 空间成员关联表

## License

[MIT License](LICENSE)
