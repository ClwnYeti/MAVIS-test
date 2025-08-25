plugins {
    id("org.springframework.boot") version "3.1.2"
    id("io.spring.dependency-management") version "1.1.2"
    id("org.jetbrains.kotlin.jvm") version "1.8.0"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    implementation("ch.qos.logback:logback-classic:1.4.5")

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.7.3")

    implementation("com.fasterxml.jackson.core:jackson-core:2.17.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.3")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.3")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.14.5")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    reports {
        junitXml.isOutputPerTestCase = true
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "8.3"
    distributionType = Wrapper.DistributionType.ALL
}