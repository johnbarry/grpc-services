package org.jpb.grpcservice

import io.grpc.Server
import io.grpc.ServerBuilder
import org.jpb.grpcservice.proto.ANumber
import org.jpb.grpcservice.proto.CalcGrpcKt
import org.jpb.grpcservice.proto.aNumber
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.*
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.stereotype.Component

open class CalcService : CalcGrpcKt.CalcCoroutineImplBase() {
	// f1(x) = x * 2
	override suspend fun f1(request: ANumber): ANumber = aNumber {
		functionsApplied = request.functionsApplied+1
		number = request.number * 2
	}

	// f2(x) = x + 100
	override suspend fun f2(request: ANumber): ANumber = aNumber {
		functionsApplied = request.functionsApplied+1
		number = request.number + 100
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
				stop()
				log.info("*** server shut down")
			}
		)
	}

	private fun stop() {
		server.shutdown()
	}

	private fun blockUntilShutdown() {
		server.awaitTermination()
	}
	companion object {
		val log: Logger = LoggerFactory.getLogger(CalcServer::class.java)
	}

	override fun run(args: ApplicationArguments?) {
		log.info("Starting grpc Server")
		val server = CalcServer()
		server.start()
		//server.blockUntilShutdown()
	}
}


fun main(args: Array<String>) {
	CalcServer.log.info("Starting up spring...")
	val app = SpringApplication(CalcServer::class.java)
	app.webApplicationType = WebApplicationType.REACTIVE
	app.run(*args)
}
