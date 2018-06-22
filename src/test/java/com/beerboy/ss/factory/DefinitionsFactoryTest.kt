package com.beerboy.ss.factory

import com.google.gson.Gson
import org.junit.Assert
import org.junit.Test
import kotlin.reflect.full.createType

class DefinitionsFactoryTest {

    data class SimpleClass(val data: String)
    data class ClassWithCollection(val data: List<SimpleClass>)

    @Test
    fun `creates a definition for a simple object`() {
        val map = DefinitionsFactory.create(SimpleClass::class.createType())

        Assert.assertEquals(
                """{"SimpleClass":{"type":"object","properties":{"data":{"type":"string","required":true,"vendorExtensions":{}}},"isSimple":false,"vendorExtensions":{}}}""",
                Gson().toJson(map))
    }

    @Test
    fun `creates a definition for a class with collection`() {
        val map = DefinitionsFactory.create(ClassWithCollection::class.createType())

        println(Gson().toJson(map))
    }
}