# seeksome
二手交易平台

## 技术栈

| 技术 | 版本      | 说明 |
|---|---------|---|
| JDK | 17      | 开发与运行环境 |
| Spring Boot | 3.5.7   | Web、Validation 等基础框架 |
| MyBatis-Plus | 3.5.18  | 持久层框架 |
| MySQL | 8.4.11  | MySQL 驱动 |
| Redis | 8.4.x   | 缓存服务端 |
| Hutool | 5.8.48  | 工具类库 |
| Lombok | 1.18.42 | 编译期代码生成 |
| Jackson | 2.19.2  | JSON 序列化 |
| Knife4j | 4.5.0   | 接口文档 |
| springdoc-openapi | 2.8.17  | 兼容 Spring Boot 3.5 的版本覆盖 |
| Maven | 3.9.14  | 构建工具 |

## 项目结构
```
seeksome
│
├── bootstrap                                 # 单体应用：启动装配 + 业务
│   └── src/main/java/com/zyzylong/seeksome
│       ├── auth/                             # 认证域（controller/admin、service、dao）
│       ├── config/                           # 全局配置（Swagger）
│       └── SeekSomeApplication.java          # 唯一启动类
│
└── framework                                 # 技术基础：结果契约、异常、Web 机制
```
