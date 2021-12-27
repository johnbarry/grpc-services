import com.google.protobuf.gradle.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
plugins {
    kotlin("jvm")
    id("com.google.protobuf")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn","-Xjsr305=strict")
        jvmTarget = "11"
    }
}

sourceSets {
    main {
        java {
            srcDir ("build/generated/source/proto/main/grpc")
            srcDir ("build/generated/source/proto/main/grpckt")
            srcDir ("build/generated/source/proto/main/java")
        }
    }
}


dependencies {
    api("io.grpc:grpc-protobuf:${rootProject.ext["grpcVersion"]}")
    api("com.google.protobuf:protobuf-java-util:${rootProject.ext["protobufVersion"]}")
    api("com.google.protobuf:protobuf-kotlin:${rootProject.ext["protobufVersion"]}")
    api("io.grpc:grpc-kotlin-stub:${rootProject.ext["grpcKotlinVersion"]}")

}
java {
    sourceCompatibility = JavaVersion.VERSION_11
}




protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${rootProject.ext["protobufVersion"]}"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${rootProject.ext["grpcVersion"]}"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:${rootProject.ext["grpcKotlinVersion"]}:jdk7@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
            it.builtins {
                id("kotlin")
            }
        }
    }
}

