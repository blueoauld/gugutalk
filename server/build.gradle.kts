plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jetbrains.kotlin.plugin.jpa") version "2.2.21"
}

group = "com.blueoauld"
version = "0.0.1-SNAPSHOT"
description = "server"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    runtimeOnly("org.postgresql:postgresql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // bcrypt
    implementation("org.springframework.security:spring-security-crypto")
    implementation("org.bouncycastle:bcprov-jdk18on:1.84")

    // jwt
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")

    // kotlin-log
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")

    // kotlin-jdsl
    implementation("com.linecorp.kotlin-jdsl:jpql-dsl:3.9.0")
    implementation("com.linecorp.kotlin-jdsl:jpql-render:3.9.0")
    implementation("com.linecorp.kotlin-jdsl:spring-data-jpa-support:3.9.0")

    // s3
    implementation("software.amazon.awssdk:s3:2.44.12")

    // coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")

    // jackson
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // jda
    implementation("net.dv8tion:JDA:6.4.1") {
        exclude(module = "opus-java")
        exclude(module = "tink")
    }

    // pushy
    implementation("com.eatthepath:pushy:0.15.6")

    // prometheus
    implementation("io.micrometer:micrometer-registry-prometheus")

    // logstash-logback-encoder
    implementation("net.logstash.logback:logstash-logback-encoder:9.0")

    // solapi
    implementation("com.solapi:sdk:1.0.3")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
