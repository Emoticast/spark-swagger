package com.beerboy.ss.model.properties

data class UUIDProperty(
        val minLength: Int? = null,
        val maxLength: Int? = null,
        val pattern: String? = null,
        val default: String? = null,
        val enum: MutableList<String>? = null) : AbstractProperty("uuid", "string"), Property {

    companion object {
        fun isType(type: String, format: String?): Boolean = "string" == type && "uuid" == format
    }
}