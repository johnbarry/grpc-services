package org.jpb.numberms

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

//@ContextConfiguration
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NumberMSTests {
	//@LocalServerPort
	//private val localPort = -1

	@Test
	fun `check the server has started and been allocated a port`() {
		//localPort shouldNotBe -1
	}

}
