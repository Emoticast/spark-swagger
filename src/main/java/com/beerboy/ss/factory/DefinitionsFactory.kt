package com.beerboy.ss.factory

import com.beerboy.spark.typify.spec.IgnoreSpec
import com.beerboy.ss.model.Model
import com.beerboy.ss.model.ModelImpl
import com.beerboy.ss.model.properties.*
import com.beerboy.ss.model.utils.PropertyModelConverter
import java.io.File
import java.util.*
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure

/**
 * @author manusant
 */
object DefinitionsFactory {

    var ignoreSpec: IgnoreSpec? = null

    fun create(type: KType): Map<String, Model> {
        val definitions = HashMap<String, Model>()

        if (isObject(type)) {
            val model = ModelImpl()
            model.type = ModelImpl.OBJECT
            definitions[type.jvmErasure.simpleName!!] = model

            val refDefinitions = parseProperties(model, type.jvmErasure)
            definitions.putAll(refDefinitions)
        }
        return definitions
    }

    fun parseProperties(model: ModelImpl, klass: KClass<*>): Map<String, Model> {
        val refDefinitions = HashMap<String, Model>()

        val fields: Collection<KCallable<*>> = klass.declaredMemberProperties

        for (field in fields) {
            if (isViable(field)) {
                val property = createProperty(field.returnType)

                klass.primaryConstructor?.parameters?.forEach {
                    if (it.name == field.name) {
                        it.annotations.forEach {
                            if (it is Description) {
                                property.description = it.value
                            }
                        }

                    }
                }

                model.addProperty(field.name, property)

                if (isRef(field.returnType)) {
                    val definitions = create(field.returnType)
                    refDefinitions.putAll(definitions)
                } else if (field.returnType.javaClass.isArray || Collection::class.java.isAssignableFrom(field.returnType.jvmErasure.java)) {
                    val childType = getCollectionType(field.returnType)
                    if (isRef(childType)) {
                        val definitions = create(childType)
                        refDefinitions.putAll(definitions)
                    }
                }
            }
        }
        return refDefinitions
    }

    private fun isViable(field: KCallable<*>): Boolean {
        return field.visibility == KVisibility.PUBLIC
    }

    fun createProperty(fieldType: KType): Property {
        val fieldClass = fieldType.jvmErasure
        return when {
            fieldType.jvmErasure.java.isEnum -> {
                val property = StringProperty()
                property._enum(fieldType.jvmErasure.java.enumConstants.map { o -> (o as Enum<*>).name })
            }
            fieldClass == Boolean::class -> BooleanProperty()
            fieldClass == ByteArray::class -> ByteArrayProperty()
            fieldClass == Date::class -> DateProperty()
            fieldClass == Number::class -> DecimalProperty()
            fieldClass == Double::class -> DoubleProperty()
            fieldClass == Float::class -> FloatProperty()
            fieldClass == Int::class -> IntegerProperty()
            fieldClass == Long::class -> LongProperty()
            fieldClass == String::class -> StringProperty()
            fieldClass == UUID::class -> UUIDProperty()
            fieldClass.isSealed -> {
                SealedProperty(fieldClass.simpleName!!, fieldClass)
            }
            fieldClass.java.isArray || Collection::class.java.isAssignableFrom(fieldClass.java) -> {
                val property = ArrayProperty()
                property.items = getCollectionProperty(fieldType)
                property
            }
            File::class.java.isAssignableFrom(fieldType.jvmErasure.java) -> FileProperty()
            else -> {
                val property = ObjectProperty()
                val model = ModelImpl()
                model.type = ModelImpl.OBJECT
                parseProperties(model, fieldType.jvmErasure)
                PropertyModelConverter().modelToProperty(model)!!
            }
        }.apply {
            required = !fieldType.isMarkedNullable
        }
    }

    private fun isRef(type: KType): Boolean = !(type.jvmErasure.java.isEnum
            || type.jvmErasure == Boolean::class
            || type.jvmErasure == Boolean::class
            || type.jvmErasure == ByteArray::class
            || type.jvmErasure == Date::class
            || type.jvmErasure == java.sql.Date::class
            || type.jvmErasure == Number::class
            || type.jvmErasure == Double::class
            || type.jvmErasure == Double::class
            || type.jvmErasure == Float::class
            || type.jvmErasure == Float::class
            || type.jvmErasure == Int::class
            || type.jvmErasure == Int::class
            || type.jvmErasure == Long::class
            || type.jvmErasure == Long::class
            || type.jvmErasure == String::class
            || type.jvmErasure == UUID::class
            || type.jvmErasure.java.isArray
            || Collection::class.java.isAssignableFrom(type.jvmErasure.java)
            || File::class.java.isAssignableFrom(type.jvmErasure.java)
            || type.jvmErasure.java.canonicalName.contains("java"))

    private fun isObject(type: KType): Boolean = !(type.jvmErasure.java.isEnum
            || type.jvmErasure == Boolean::class
            || type.jvmErasure == ByteArray::class
            || type.jvmErasure == Number::class
            || type.jvmErasure == Double::class
            || type.jvmErasure == Float::class
            || type.jvmErasure == Int::class
            || type.jvmErasure == Long::class
            || type.jvmErasure == String::class)

    private fun getCollectionProperty(collectionField: KType?): Property = createProperty(getCollectionType(collectionField))

    private fun getCollectionType(collectionField: KType?): KType = collectionField?.arguments?.get(0)?.type!!
}

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Description(val value: String)
