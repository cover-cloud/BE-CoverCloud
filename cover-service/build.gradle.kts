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
//    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")

    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.4")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.0")

    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

//    implementation(project(":shared-library"))

    runtimeOnly("com.mysql:mysql-connector-j")

    implementation(project(":shared-library")) {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-security")
    }

    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.7.0")
    testRuntimeOnly("com.h2database:h2")
}

tasks.bootJar {
    mainClass.set("com.covercloud.cover.CoverServiceApplicationKt")
}

tasks.test {
    useJUnitPlatform()
}