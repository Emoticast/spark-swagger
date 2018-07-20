package com.beerboy.ss.factory

import com.beerboy.ss.Foo
import com.beerboy.ss.model.utils.PropertyModelConverter
import com.google.gson.Gson
import org.junit.Assert
import org.junit.Test
import kotlin.reflect.full.createType

class DefinitionsFactoryTest {

    data class SimpleClass(@Description("barfoo") val data: String)
    data class ClassWithCollection(
            @Description("barfoo")
            val data: List<SimpleClass>)

    @Test
    fun `creates a definition for a simple object`() {
        val map = DefinitionsFactory.create(SimpleClass::class.createType())

        Assert.assertEquals(
                """{"SimpleClass":{"type":"object","properties":{"data":{"type":"string","required":true,"description":"barfoo","vendorExtensions":{}}},"isSimple":false,"vendorExtensions":{}}}""",
                Gson().toJson(map))
    }

    @Test
    fun `creates a definition for a class with collection`() {
        val map = PropertyModelConverter().propertyToModel(DefinitionsFactory.createProperty(Foo::class.createType()))

        println(Gson().toJson(map))
    }
}