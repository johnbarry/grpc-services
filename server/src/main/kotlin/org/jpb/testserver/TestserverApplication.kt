package org.jpb.testserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TestserverApplication

fun main(args: Array<String>) {
	runApplication<TestserverApplication>(*args)
}
