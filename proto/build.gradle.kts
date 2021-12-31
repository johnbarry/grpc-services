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

dependencies {
    api("io.grpc:grpc-protobuf:${rootProject.ext["grpcVersion"]}")
    api("com.google.protobuf:protobuf-java-util:${rootProject.ext["protobufVersion"]}")
    api("com.google.protobuf:protobuf-kotlin:${rootProject.ext["protobufVersion"]}")
    api("io.grpc:grpc-kotlin-stub:${rootProject.ext["grpcKotlinVersion"]}")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${rootProject.ext["kotlinVersion"]}")


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
        all().forEach { task ->
            task.plugins {
                id("grpc")
                id("grpckt")
            }
            task.builtins {
                id("kotlin")
            }
            // If true, will generate a descriptor_set.desc file under
            // $generatedFilesBaseDir/$sourceSet. Default is false.
            // See --descriptor_set_out in protoc documentation about what it is.
            task.generateDescriptorSet = true

            // Allows to override the default for the descriptor set location
            task.descriptorSetOptions.path =
                "${projectDir}/build/descriptors/${task.sourceSet.name}.dsc"

            // If true, the descriptor set will contain line number information
            // and comments. Default is false.
            task.descriptorSetOptions.includeSourceInfo = true

            // If true, the descriptor set will contain all transitive imports and
            // is therefore self-contained. Default is false.
            task.descriptorSetOptions.includeImports = true
        }
    }
}

sourceSets {
    main {
        java {
            srcDirs(listOf("grpc","grpckt","java","kotlin")
                .map {  "build/generated/source/proto/main/$it" })
        }
    }
}
