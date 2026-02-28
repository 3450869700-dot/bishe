# 使用官方 Java 17 镜像作为基础镜像
FROM eclipse-temurin:17-jdk-alpine

# 设置工作目录
WORKDIR /app

# 复制 Maven wrapper 和 pom.xml
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# 下载依赖（利用 Docker 缓存层）
RUN ./mvnw dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建应用
RUN ./mvnw clean package -DskipTests -B

# 暴露端口
EXPOSE 8080

# 启动应用
CMD ["java", "-jar", "target/backend-0.0.1-SNAPSHOT.jar"]
