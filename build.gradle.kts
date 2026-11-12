plugins {
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.spring") version "2.2.0"
    kotlin("plugin.jpa") version "2.2.0"
    kotlin("kapt") version "2.2.0"
}

group = "com.aifactory"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-authorization-server")

    // Kotlin 관련
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // QueryDSL
    implementation("io.github.openfeign.querydsl:querydsl-jpa:6.11")
    implementation("io.github.openfeign.querydsl:querydsl-core:6.11")
    kapt("io.github.openfeign.querydsl:querydsl-apt:6.11:jpa")

    // Database
    runtimeOnly("org.postgresql:postgresql")

    // Development
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Swagger/OpenAPI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:2.8.13")

    // Redis
    implementation("org.springframework.session:spring-session-data-redis")
    implementation("org.apache.commons:commons-pool2")
    implementation("io.lettuce:lettuce-core")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.5")

    implementation("net.logstash.logback:logstash-logback-encoder:7.4")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)

    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

// QueryDSL Q클래스 생성 경로 설정
kapt {
    generateStubs = true
}

// Generated 소스 경로 추가
sourceSets {
    main {
        kotlin.srcDirs("src/main/kotlin", "build/generated/source/kapt/main")
    }
}