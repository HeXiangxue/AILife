# AI 生活优选

基于大模型的本地生活平台：提供智能问答 Agent（商户搜索 / 评价查询 / 地理定位）与内容审核 Agent（敏感词 / 图像 / 信誉 / LLM 语义审核 + 人工复审）。

**技术栈**：Spring Boot 3.3 · Java 17 · MyBatis · MySQL · Redis · JWT · LangChain4j 1.0（通义千问 DashScope）

## 功能特性

- **智能问答 Agent**
  - 基于 LangChain4j `AiServices` 构建，商家查询 / 评价查询 / 地理位置计算封装为 Tool；
  - Agent 按问题自主选择工具并汇总结果，支持分类、关键词、评分、人均价格等多条件商家推荐；
  - 支持按经纬度计算距离（Haversine），推荐附近商家。
- **内容审核 Agent**
  - 文本敏感词（DFA 字典）→ 图像审核（阿里云内容安全，真实 API）→ 用户信誉校验 → LLM 语义审核，串联为审核流水线；
  - 对模型输出做结果校验：命中敏感词 / 图像违规直接拒绝，图像疑似或 LLM 置信度低 / 解析失败转**人工复审**，低信誉用户自动升级复审；
  - 审核结果落库（audit_record），同步更新内容状态；管理员工作台支持人工复审（通过 / 拒绝）。
- **认证**：注册 / 登录 / 退出，JWT + Redis 吊销与单点控制，BCrypt 加密。
- **前端**：登录、智能问答、社区内容（发布 / 评价 / 审核状态）、审核工作台四个页面。

## 快速开始

### 1. 准备环境

| 依赖 | 说明 |
|------|------|
| JDK 17+ | 编译与运行 |
| Maven 3.6+ | 构建 |
| MySQL 8.x | 数据存储，端口 3306 |
| Redis | token 缓存，端口 6379 |

### 2. 初始化数据库

```bash
mysql -uroot -p你的密码 < sql/init.sql
```

脚本会创建 `ai_life` 库、全部表结构、10 家演示商家。

### 3. 配置

编辑 `src/main/resources/application.yml`（或设置环境变量）：

| 配置项 | 环境变量 | 默认值 |
|--------|----------|--------|
| MySQL 密码 | `MYSQL_PASSWORD` | `Jojo20030106` |
| DashScope API Key | `DASHSCOPE_API_KEY` | 空（为空时使用本地 Mock 模型） |
| 阿里云图像审核 AK/SK | `ALIYUN_AK` / `ALIYUN_SK` | 空（未配置时带图内容转人工复审） |

> DashScope 使用 OpenAI 兼容模式（`https://dashscope.aliyuncs.com/compatible-mode/v1`，模型 `qwen-plus`）。
> 不配置 API Key 也能跑通全流程：内置 Mock 模型会模拟工具选择与审核结果。

### 4. 启动

```bash
mvn spring-boot:run
# 或
mvn -DskipTests package && java -jar target/ai-life-platform.jar
```

访问 `http://localhost:8080/login.html`。

### 5. 演示账号

| 账号 | 密码 | 角色 | 说明 |
|------|------|------|------|
| admin | admin123 | 管理员 | 可访问审核工作台 |
| user1 | user123 | 普通用户 | 信誉分 90 |
| user2 | user123 | 普通用户 | 信誉分 50（发布内容自动升级人工复审，用于演示） |

## 页面入口

| 页面 | 地址 | 说明 |
|------|------|------|
| 登录 | `/login.html` | 登录 / 注册 |
| 智能问答 | `/chat.html` | 与 Agent 对话，支持商家推荐 / 评价查询 / 附近推荐 |
| 社区内容 | `/content.html` | 发布动态、提交评价、查看审核状态 |
| 审核工作台 | `/admin.html` | 人工复审（仅 admin） |

## 核心 API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 登录 |
| POST | `/api/auth/register` | 注册 |
| POST | `/api/agent/chat` | 智能问答（body：`question` + 可选 `latitude/longitude`） |
| GET | `/api/merchants` | 多条件查询商家（支持经纬度距离排序） |
| POST | `/api/posts` | 发布动态（自动审核） |
| POST | `/api/reviews` | 提交评价（自动审核） |
| GET | `/api/admin/audits?status=MANUAL` | 审核记录列表 |
| POST | `/api/admin/audits/{id}/review` | 人工复审（`decision: APPROVED/REJECTED`） |

## 项目结构

```
src/main/java/com/ailife/
├── config/          # Web/数据初始化/LangChain4j 配置
├── security/        # JWT、拦截器、用户上下文
├── common/          # 统一返回、异常、常量
├── entity/ dto/     # 实体与出入参
├── mapper/          # MyBatis（含 XML 动态 SQL）
├── service/agent/   # 问答 Agent（tools/）、审核 Agent（review/、mock/）
└── controller/      # Auth/Merchant/Review/Post/Agent/Admin
```

## 冒烟测试

```bash
PYTHONIOENCODING=utf-8 python smoke_test.py   # 需先启动应用
```
