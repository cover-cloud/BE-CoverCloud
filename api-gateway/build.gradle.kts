plugins {
    kotlin("jvm")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("plugin.spring")

}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.4")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.0")


    implementation(project(":shared-library"))

}
