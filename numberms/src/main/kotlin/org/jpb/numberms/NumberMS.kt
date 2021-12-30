package org.jpb.numberms

import com.google.protobuf.GeneratedMessageV3
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.asFlux
import org.jpb.grpcservice.proto.ANumber
import org.jpb.grpcservice.proto.CalcGrpcKt
import org.jpb.grpcservice.proto.Lineage
import org.jpb.grpcservice.proto.aNumber
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.stereotype.Component

@SpringBootApplication
@EnableAutoConfiguration
@Component
class NumberMS : ApplicationRunner {

    companion object {
        val log: Logger = LoggerFactory.getLogger(NumberMS::class.java)
    }

    private val channel = ManagedChannelBuilder.forAddress(ConfigHelper.gRPCHost, ConfigHelper.gRPCPort).usePlaintext().build()
    private val stub: CalcGrpcKt.CalcCoroutineStub = CalcGrpcKt.CalcCoroutineStub(channel)

    data class ParseException(val key: String, val offset: Long) : Exception() {
        override fun toString(): String = "parse error for key: $key, offset=$offset"
    }

    suspend fun applyFn(num: ANumber, stub: CalcGrpcKt.CalcCoroutineStub): ANumber = when (ConfigHelper.function) {
        "f1" -> stub.f1(num)
        "f2" -> stub.f2(num)
        "f3" -> stub.f3(num)
        else -> throw Exception("Unable to find function named ${ConfigHelper.function} to execute")
    }

    fun kafkaInputFlow(): Flow<ANumber> =
        KafkaHelper.read(readEarliest = false, keepListening = true).map {
            try {
                ANumber.parseFrom(it.value()).toBuilder().build().apply {
                    log.debug("friend command created: offset ${it.offset()}: key ${it.key()}")
                }
            } catch (ex: Exception) {
                throw ParseException(it.key(), it.offset())
            }
        }.onErrorContinue { t: Throwable, _: Any ->
            log.error(t.toString())
        }.asFlow()

    fun generatedInputFlow(): Flow<ANumber> =
        (1..ConfigHelper.generationSize)
            .asFlow()
            .map { aNumber {
                number = it.toLong()
                lineage = Lineage.newBuilder().setCorrelationId(KafkaHelper.generateUUID()).build()
            } }


    override fun run(args: ApplicationArguments?) {
        with(if (ConfigHelper.input == "generator") generatedInputFlow() else kafkaInputFlow()) {
            this.map {
                Pair(it.lineage.correlationId, applyFn(it, stub) as GeneratedMessageV3)
            }.asFlux().apply {
                KafkaHelper.write(this)
            }
        }
        channel.shutdown()
    }
}

fun main(args: Array<String>) {
    NumberMS.log.info("Starting up spring...")
    val app = SpringApplication(NumberMS::class.java)
    app.webApplicationType = WebApplicationType.NONE
    app.run(*args)
}
