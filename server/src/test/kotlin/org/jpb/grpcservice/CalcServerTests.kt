package org.jpb.grpcservice

import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.runBlocking
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.jpb.grpcservice.proto.CalcGrpcKt
import org.jpb.grpcservice.proto.aNumber
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ContextConfiguration

@ContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CalcServerTests {
	@LocalServerPort
	private val localPort = -1

	@Test
	fun `check the server has started and been allocated a port`() {
		localPort shouldNotBe -1
	}
	@Test
	fun check_f1() {
		val channel = ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build()
		val stub = CalcGrpcKt.CalcCoroutineStub(channel)
		runBlocking {
			val response = stub.f1 (aNumber {
				number = 20
			})
			response.number shouldBe 40
			response.functionsApplied shouldBe 1
		}
		channel.shutdown()
	}

}
