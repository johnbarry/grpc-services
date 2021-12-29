
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

dependencies {
	implementation(project(":proto"))
}

tasks.bootBuildImage {
	imageName = "number-demo/number-ms:v1"
}


dependencies {
	implementation ("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("junit:junit:4.13.1")
	implementation( "io.projectreactor.kafka:reactor-kafka:1.3.7")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
	testImplementation("org.junit.jupiter:junit-jupiter-engine:5.8.1")
	testImplementation("io.kotest:kotest-assertions-core:5.0.0")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.2")
	runtimeOnly("io.grpc:grpc-netty:${rootProject.ext["grpcVersion"]}")

	api("io.grpc:grpc-protobuf:${rootProject.ext["grpcVersion"]}")
	api("com.google.protobuf:protobuf-java-util:${rootProject.ext["protobufVersion"]}")
	api("com.google.protobuf:protobuf-kotlin:${rootProject.ext["protobufVersion"]}")
	api("io.grpc:grpc-kotlin-stub:${rootProject.ext["grpcKotlinVersion"]}")
	api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${rootProject.ext["kotlinVersion"]}")
	api("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:${rootProject.ext["kotlinVersion"]}")

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
/*
sourceSets {
	main {
		java {
			srcDirs(listOf("grpc","grpckt","java","kotlin")
					.map {  "../proto/build/generated/source/proto/main/$it" })
		}
	}
}
*/
tasks.register("showSets") {
	doFirst {
		sourceSets.main{
			this.allSource.forEach(::println)
			//println("Sourceset: ${this.java.sourceDirectories.}")
		}
	}
}

