package org.jpb.numberms

import com.google.protobuf.GeneratedMessageV3
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.jpb.numberms.ConfigHelper.getMandatoryConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions
import reactor.kafka.sender.SenderRecord
import java.io.Closeable
import java.time.Duration
import java.util.UUID

typealias KafkaPayload = ByteArray
typealias KafkaKey = String

class CloseableWrapper(private val sender: KafkaSender<KafkaKey, KafkaPayload>) : Closeable {
    override fun close() =
        sender.close()
}

fun KafkaSender<KafkaKey, KafkaPayload>.asCloseable() = CloseableWrapper(this)

object KafkaHelper {
    object KafkaConfig {
        // opinionated on naming conventions
        val SERVERS: String = getMandatoryConfig("CFG_KAFKA_BOOTSTRAP")
        val CONSUMER: String = "${ConfigHelper.application}-${ConfigHelper.environment}"
        val GROUP: String = "${ConfigHelper.application}-${ConfigHelper.environment}-group"
        val IN_TOPIC: String = "${ConfigHelper.domain}-${ConfigHelper.environment}-${ConfigHelper.input}"
        val OUT_TOPIC: String = "${ConfigHelper.domain}-${ConfigHelper.environment}-${ConfigHelper.output}"
    }

    private val SERVERS: String = KafkaConfig.SERVERS
    private val KEY_SERIAL = StringSerializer::class.java
    private val KEY_DES = StringDeserializer::class.java
    private val VALUE_SERIAL = ByteArraySerializer::class.java
    private val VALUE_DES = ByteArrayDeserializer::class.java
    private val CLIENT_ID_PRODUCER: String = KafkaConfig.CONSUMER
    private val log: Logger = LoggerFactory.getLogger(KafkaHelper::class.java)

    private val writeProps: Map<String, Any> = mapOf(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to SERVERS,
        ProducerConfig.CLIENT_ID_CONFIG to CLIENT_ID_PRODUCER,
        ProducerConfig.ACKS_CONFIG to "1",
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to KEY_SERIAL,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to VALUE_SERIAL
    )

    private fun readOptions(consumerName: String, groupName: String): Map<String, Any> = mapOf(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to SERVERS,
        ConsumerConfig.CLIENT_ID_CONFIG to consumerName,
        ConsumerConfig.GROUP_ID_CONFIG to groupName,
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to KEY_DES,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to VALUE_DES
    )

    fun generateUUID(): String = UUID.randomUUID().toString()

    fun write(kv: Flux<Pair<KafkaKey?, GeneratedMessageV3>>) {
        log.info("Start kafka write...")
        val newFlux = kv.map {
            SenderRecord.create(
                KafkaConfig.OUT_TOPIC,
                0,
                System.currentTimeMillis(),
                it.first ?: generateUUID(),
                it.second.toByteArray(),
                it.first
            )
        }

        val sender = KafkaSender.create(SenderOptions.create<KafkaKey, KafkaPayload>(writeProps))
        sender.asCloseable().use {
            val ct = sender.send(newFlux).doOnError {
                log.error("Kafka send failed: {}", it)
            }.doOnComplete { log.info("${KafkaConfig.OUT_TOPIC} WRITE COMPLETE") }.count().block() ?: 0L
            log.info("******* Completed kafka write: $ct rows written *******")
        }
    }

    fun read(
        readEarliest: Boolean = false,
        keepListening: Boolean = true
    ): Flux<ConsumerRecord<KafkaKey /* = kotlin.String */, KafkaPayload /* = kotlin.ByteArray */>> =
        with(
            KafkaReceiver
                .create(ReceiverOptions.create<KafkaKey, KafkaPayload>(
                    readOptions(
                        KafkaConfig.CONSUMER,
                        KafkaConfig.GROUP
                    )
                )
                    .subscription(setOf(KafkaConfig.IN_TOPIC))
                    .addAssignListener { parts ->
                        if (readEarliest) parts.forEach { p -> p.seekToBeginning() }
                    }
                    .addRevokeListener { log.debug("partitions revoked {}", it) })
                .receiveAutoAck()
        ) {
            if (keepListening) this
            else this.timeout(Duration.ofSeconds(1L), Mono.empty())
        }
            .flatMap { it }//.log()
            .doOnComplete { log.info("${KafkaConfig.IN_TOPIC} READ COMPLETE (${KafkaConfig.CONSUMER})") }
}
