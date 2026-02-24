# Peach Shop Backend

这是Peach Shop电商平台的Spring Boot后端服务，与v-shop前端实现前后端分离架构。

## 技术栈

- Java 17
- Spring Boot 3.2.2
- Spring Data JPA
- PostgreSQL
- Spring Security
- JWT

## 项目结构

```
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── peachshop/
│   │   │           ├── BackendApplication.java   # 应用主类
│   │   │           └── controller/
│   │   │               └── HelloController.java  # 测试控制器
│   │   └── resources/
│   │       └── application.properties           # 应用配置
│   └── test/
├── .mvn/
│   └── wrapper/
├── mvnw                                         # Maven wrapper脚本
├── pom.xml                                       # Maven配置
└── README.md                                     # 项目说明
```

## 配置说明

### 数据库配置

在`application.properties`中配置PostgreSQL数据库连接：

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/peach-shop
spring.datasource.username=postgres
spring.datasource.password=123456
spring.datasource.driver-class-name=org.postgresql.Driver
```

### 服务器配置

```properties
server.port=8080
server.servlet.context-path=/api
```

### CORS配置

```properties
spring.web.cors.allowed-origins=http://localhost:3000,http://localhost:5173
spring.web.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
spring.web.cors.allowed-headers=*
spring.web.cors.allow-credentials=true
```

## 运行说明

### 前置条件

- JDK 17或以上版本
- PostgreSQL数据库（已创建peach-shop数据库）

### 运行方式

#### 1. 使用Maven Wrapper运行

```bash
# 开发环境运行
./mvnw spring-boot:run

# 构建生产版本
./mvnw clean package -DskipTests

# 运行生产版本
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

#### 2. 使用IDE运行

- 在IntelliJ IDEA中打开项目
- 配置JDK为17
- 运行`BackendApplication.java`主类

## API接口

### 测试接口

- GET `/api/hello` - 测试接口
- GET `/api/hello/health` - 健康检查

## 前端配置

前端v-shop项目已配置API代理，指向本地后端服务。在`vite.config.ts`中配置：

```typescript
'/dev-api': {
  target: 'http://localhost:8080/api',
  changeOrigin: true,
  secure: false,
  rewrite: (path) => path.replace(/^\/dev-api/, ''),
}
```

## 开发流程

1. 启动PostgreSQL数据库
2. 运行后端服务
3. 运行前端服务
4. 在浏览器中访问前端应用

## 注意事项

1. 确保PostgreSQL数据库已启动，并且已创建`peach-shop`数据库
2. 后端服务默认运行在8080端口，前端服务默认运行在5173端口
3. 首次运行时，Spring Boot会自动创建数据库表
4. 如需修改数据库配置，请修改`application.properties`文件

## 许可证

MIT