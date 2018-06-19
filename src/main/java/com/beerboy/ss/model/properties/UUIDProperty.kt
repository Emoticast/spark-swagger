package com.beerboy.ss.model.properties

data class UUIDProperty(
        var _enum: MutableList<String> = mutableListOf(),
        val minLength: Int? = null,
        val maxLength: Int? = null,
        val pattern: String? = null,
        val default: String? = null,
        val enum: MutableList<String>? = null) : AbstractProperty("uuid", "string"), Property {

    fun withEnum(value: String) {
        if (!_enum.contains(value)) {
            _enum.add(value)
        }
    }

    companion object {
        fun isType(type: String, format: String): Boolean = "string" == type && "uuid" == format
    }
}