package org.jpb.grpcservice

import com.google.protobuf.Descriptors
import io.r2dbc.spi.*
import org.jpb.grpcservice.proto.Employee
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.lang.IllegalArgumentException
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap


class FieldLookup {
    val lookup =  ConcurrentHashMap<String, Descriptors.FieldDescriptor>()
    fun bootstrap(rowMeta: RowMetadata, desc: Descriptors.Descriptor) {
        if (lookup.isEmpty()) {
            val databaseFields: Map<String, String> = rowMeta.columnNames.associateBy { it.uppercase() }
            val protoFields: Map<String, Descriptors.FieldDescriptor> =
            desc.fields.associateBy { it.name .uppercase() }
            (databaseFields.keys.intersect(protoFields.keys.toSet())).forEach { lookup[databaseFields[it]!!] = protoFields[it]!! }
        }
    }
}

object FieldLookups {
    val employee = FieldLookup()
}

fun Row.parseAsEmployee(rowMeta: RowMetadata) : Employee =
    with (FieldLookups.employee) {
        val builder = Employee.newBuilder()
        bootstrap(rowMeta, Employee.getDescriptor())
        lookup.forEach { (name, desc) ->
            builder.setField(desc, getModified(name, desc))
        }
        builder.build()
    }

fun Row.getModified(name: String, protoType: Descriptors.FieldDescriptor ): Any =
    with(get(name)!!) {
        if (this is BigDecimal) {
            when (protoType.type.name) {
                "UINT32", "UINT64" ->
                    this.longValueExact()
                else ->
                    throw IllegalArgumentException("$name - ${protoType.type.name}")
            }
        } else this
    }

@Configuration
@EnableR2dbcRepositories
class OracleConfig : AbstractR2dbcConfiguration() {

    @Bean
    override fun connectionFactory(): ConnectionFactory =
        ConnectionFactories.get(
            ConnectionFactoryOptions
                .parse("r2dbc:oracle://localhost:1521/XEPDB1")
                .mutate()
                .option(ConnectionFactoryOptions.USER, "tester")
                .option(ConnectionFactoryOptions.PASSWORD, "tester")
                .build()
        )

}

object TestDatabase {
    fun testQuery(): Flux<Employee?> =
        Mono.from(OracleConfig().connectionFactory().create())
                .flatMapMany { conn ->
                    Flux
                        .from(conn.createStatement("SELECT id, name from emp").execute())
                        .flatMap{  it.map { row, meta -> row.parseAsEmployee(meta) } }
                        .doOnComplete { conn.close() }
                }
}