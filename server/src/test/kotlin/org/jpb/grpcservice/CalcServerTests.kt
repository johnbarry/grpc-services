package org.jpb.grpcservice

import io.grpc.ManagedChannelBuilder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import org.jpb.grpcservice.proto.CalcGrpcKt
import org.jpb.grpcservice.proto.aNumber
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
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
    fun check_unary() {
        val channel = ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build()
        val stub = CalcGrpcKt.CalcCoroutineStub(channel)
        runBlocking {
            // f1(x) = x + 1
            val response = assertDoesNotThrow {
                stub.f1(aNumber {
                    number = 20
                })
            }
            response.number shouldBe 21
            response.lineage.lineageCount shouldBe 1
            response.lineage.getLineage(0).service shouldBe "grpcservice.Calc"
            response.lineage.getLineage(0).procedure shouldBe "f1"

            // f2(x) = x + 1000
            val response2 = stub.f2(response)
            response2.number shouldBe 1_021
            response2.lineage.lineageCount shouldBe 2
            response2.lineage.getLineage(1).service shouldBe "grpcservice.Calc"
            response2.lineage.getLineage(1).procedure shouldBe "f2"

            // f3(x) = x + 1000000
            val response3 = stub.f3(response2)
            response3.number shouldBe 1_001_021
            response3.lineage.lineageCount shouldBe 3
            response3.lineage.getLineage(2).service shouldBe "grpcservice.Calc"
            response3.lineage.getLineage(2).procedure shouldBe "f3"
        }
    }

    @Test
    fun check_stream() {
        val channel = ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build()
        val stub = CalcGrpcKt.CalcCoroutineStub(channel)
        runBlocking {
            // f1(x) = x + 1
            val repeat = 10
            val num = aNumber {
                number = 20
            }
            val response =
                assertDoesNotThrow {
                    stub.streamF1(generateSequence { num }.asFlow().take(repeat))
                }
            var sum = 0L
            var ct = 0
            response
                .map { it.number }
                .collect { sum += it; ct++ }
            ct shouldBe repeat
            sum shouldBe repeat * (num .number + 1)
        }


        channel.shutdown()
    }
}

