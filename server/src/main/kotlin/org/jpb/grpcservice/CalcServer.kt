package org.jpb.grpcservice


import com.google.protobuf.timestamp
import io.grpc.MethodDescriptor
import io.grpc.Server
import io.grpc.ServerBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jpb.grpcservice.proto.*

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.*
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.stereotype.Component
import java.time.Instant

fun ANumber.updatedLineage(method: MethodDescriptor<ANumber, ANumber>): Lineage =
	Lineage.newBuilder()
		.setCorrelationId (lineage.correlationId)
		.addAllLineage(lineage.lineageList)
		.addLineage (callInstance {
			service = method.serviceName ?: ""
			procedure = method.bareMethodName ?: ""
			timestamp = timestamp { seconds = Instant.now().epochSecond }
		})
		.build()

open class CalcService : CalcGrpcKt.CalcCoroutineImplBase() {

	// f1(x) = x + 1
	override suspend fun f1(request: ANumber): ANumber = aNumber {
		lineage = request.updatedLineage(CalcGrpcKt.f1Method)
		number = request.number + 1
	}

	override fun streamF1(requests: Flow<ANumber>): Flow<ANumber> =
		requests.map { f1(it) }


	// f2(x) = x + 1000
	override suspend fun f2(request: ANumber): ANumber = aNumber {
		lineage = request.updatedLineage(CalcGrpcKt.f2Method)
		number = request.number + 1000
	}

	// f3(x) = x + 1000000
	override suspend fun f3(request: ANumber): ANumber = aNumber {
		lineage = request.updatedLineage(CalcGrpcKt.f3Method)
		number = request.number + 1000000
	}
}

@SpringBootApplication
@EnableAutoConfiguration
@Component
class CalcServer : ApplicationRunner {
	private val port = 50051
	private val server: Server = ServerBuilder
		.forPort(port)
		.addService(CalcService())
		.build()

	private fun start() {
		server.start()
		log.info("gRPC server started, listening on $port")
		Runtime.getRuntime().addShutdownHook(
			Thread {
				log.info("*** shutting down gRPC server since JVM is shutting down")
				server.shutdown()
				log.info("*** server shut down")
			}
		)
	}

	companion object {
		val log: Logger = LoggerFactory.getLogger(CalcServer::class.java)
	}

	override fun run(args: ApplicationArguments?) {
		log.info("Starting grpc Server")
		val server = CalcServer()
		server.start()
	}
}


fun main(args: Array<String>) {
	CalcServer.log.info("Starting up spring...")
	val app = SpringApplication(CalcServer::class.java)
	app.webApplicationType = WebApplicationType.REACTIVE
	app.run(*args)
}
