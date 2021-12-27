import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.6.2"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.6.10"
	kotlin("plugin.spring") version "1.6.10"

}

group = "org.jpb"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
}

tasks.bootBuildImage {
	imageName = "jpb/testserver"
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	testImplementation("org.springframework.boot:spring-boot-starter-test")

	//api(kotlin("stdlib"))
	api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${rootProject.ext["kotlinVersion"]}")
	api("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:${rootProject.ext["kotlinVersion"]}")

	api("io.grpc:grpc-protobuf:${rootProject.ext["grpcVersion"]}")
	api("com.google.protobuf:protobuf-java-util:${rootProject.ext["protobufVersion"]}")
	api("com.google.protobuf:protobuf-kotlin:${rootProject.ext["protobufVersion"]}")
	api("io.grpc:grpc-kotlin-stub:${rootProject.ext["grpcKotlinVersion"]}")

}
java {
	sourceCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
