package com.beerboy.ss.model.properties

import com.beerboy.ss.factory.DefinitionsFactory
import com.beerboy.ss.model.ExternalDocs
import com.beerboy.ss.model.Model
import com.beerboy.ss.model.ModelImpl
import kotlin.reflect.KClass

class SealedProperty(name: String, private val clazz: KClass<*>) : AbstractProperty(name, "object") {
    //    val anyOf = listOf<AnonType>(AnonType("object", clazz))
    val anyOf = clazz.nestedClasses.filter { it.isFinal }.map {
        val model = ModelImpl()
        model.type = ModelImpl.OBJECT
        DefinitionsFactory.parseProperties(model, it)
        val stringProperty = StringProperty()
        stringProperty.enum = listOf(it.simpleName)
        model.addProperty("type", stringProperty)
        model
    }
}

class AnonType(val type: String, val properties: MutableMap<String, Property>?)
class SealedModel(properties: MutableMap<String, Property>) : Model {
    val anyOf = listOf<AnonType>(AnonType("object", properties))

    override fun getTitle(): String? {
        return null
    }

    override fun setTitle(title: String?) {
    }

    override fun getDescription(): String? {
        return null
    }

    override fun setDescription(description: String?) {
    }

    override fun getProperties(): MutableMap<String, Property>? {
        return null
    }

    override fun setProperties(properties: MutableMap<String, Property>?) {
    }

    override fun getExample(): Any? {
        return null
    }

    override fun setExample(example: Any?) {
    }

    override fun getExternalDocs(): ExternalDocs? {
        return null
    }

    override fun getReference(): String? {
        return null
    }

    override fun setReference(reference: String?) {
    }

    override fun clone(): Any? {
        return null
    }

    override fun getVendorExtensions(): MutableMap<String, Any>? {
        return null
    }
}
