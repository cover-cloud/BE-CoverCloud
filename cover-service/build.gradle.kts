plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")

    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

extra["springCloudVersion"] = "2023.0.1"

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.4")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.0")

    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

    implementation(project(":shared-library"))

    runtimeOnly("com.mysql:mysql-connector-j")
}

tasks.bootJar {
    mainClass.set("com.covercloud.cover.CoverServiceApplication")
}