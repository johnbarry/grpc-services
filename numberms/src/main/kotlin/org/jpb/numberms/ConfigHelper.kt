package org.jpb.numberms

object ConfigHelper {
    data class MandatoryConfigException(val key: String) : Exception() {
        override fun toString(): String = "Missing mandatory config key $key"
    }
    fun getOptionalConfig(key: String): String? = System.getenv(key)
    fun getMandatoryConfig(key: String): String = System.getenv(key) ?: throw MandatoryConfigException(key)
    fun getMandatoryIntConfig(key: String): Int = (System.getenv(key) ?: throw MandatoryConfigException(key)).toInt()
}