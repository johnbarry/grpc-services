package org.jpb.numberms

object ConfigHelper {
    data class MandatoryConfigException(val key: String) : Exception() {
        override fun toString(): String = "Missing mandatory config key $key"
    }
    fun getOptionalIntConfig(key: String, default: Int): Int = (System.getenv(key) ?: default).toString().toInt()
    fun getOptionalConfig(key: String, default: String): String = System.getenv(key) ?: default
    fun getMandatoryConfig(key: String): String = System.getenv(key) ?: throw MandatoryConfigException(key)
    fun getMandatoryIntConfig(key: String): Int = (System.getenv(key) ?: throw MandatoryConfigException(key)).toInt()
    val environment = getOptionalConfig("CFG_ENV", "dev")
    val domain = getOptionalConfig("CFG_DOMAIN", "numbers")
    val application = getOptionalConfig("CFG_APPLICATION", "app")
    val function = getMandatoryConfig("CFG_FUNCTION")
    val input = getOptionalConfig("CFG_INPUT", "input")
    val output = getOptionalConfig("CFG_OUTPUT", "output")
    val gRPCHost = getMandatoryConfig("CFG_GRPC_HOST")
    val gRPCPort = getMandatoryIntConfig("CFG_GRPC_PORT")
    val generationSize = getOptionalIntConfig("CFG_GENERATION_SIZE", 1000)
}