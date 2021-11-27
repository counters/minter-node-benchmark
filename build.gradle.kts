import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.0"
    application
}

group = "su.update"
version = "1.0-SNAPSHOT"

val grpcKotlinVersion = "1.2.0"


repositories {
//    mavenLocal()
    mavenCentral()
    jcenter()
    maven(url = "https://jitpack.io") {}
    }

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.slf4j", "slf4j-simple", "1.7.26")
    implementation("org.slf4j", "slf4j-simple", "1.7.30")

    implementation(fileTree("libs"))
    implementation ("joda-time:joda-time:2.10.13")
    implementation("khttp:khttp:1.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.2")


    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")

    implementation("io.github.microutils:kotlin-logging-jvm:2.0.11")

    implementation("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")
    implementation ("io.grpc:grpc-stub:1.42.1")
    implementation("com.google.protobuf:protobuf-kotlin:3.19.1")
    implementation ("io.grpc:grpc-netty-shaded:1.42.1")
    implementation ("io.grpc:grpc-protobuf:1.42.1")


    implementation("com.github.uchuhimo:konf:master-SNAPSHOT")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.3")


}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}