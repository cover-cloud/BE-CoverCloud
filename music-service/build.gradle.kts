plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")

    id("org.springframework.boot")
    id("io.spring.dependency-management")
}
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.4")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.0")



    implementation(project(":shared-library"))

    runtimeOnly("com.mysql:mysql-connector-j")
}