# ---------- 构建阶段 ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
# 先拷贝 pom.xml 以利用依赖缓存
COPY EAVI_system/pom.xml .
RUN mvn dependency:go-offline -B
# 拷贝源码并打包（跳过测试）
COPY EAVI_system/src ./src
RUN mvn package -DskipTests -B

# ---------- 运行阶段 ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/springboot-headline-part-1.0-SNAPSHOT.jar ./app.jar
# 数据集等静态资源（约 6.8MB）
COPY eavi-static ./eavi-static
EXPOSE 8080
ENV TZ=Asia/Shanghai
ENTRYPOINT ["java", "-jar", "app.jar"]
