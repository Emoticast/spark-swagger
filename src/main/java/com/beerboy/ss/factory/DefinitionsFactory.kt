package com.beerboy.ss.factory

import com.beerboy.spark.typify.spec.IgnoreSpec
import com.beerboy.ss.model.Model
import com.beerboy.ss.model.ModelImpl
import com.beerboy.ss.model.properties.*
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.reflect.ParameterizedType
import java.util.*
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.jvmErasure

/**
 * @author manusant
 */
object DefinitionsFactory {

    private val LOGGER = LoggerFactory.getLogger(DefinitionsFactory::class.java)

    var ignoreSpec: IgnoreSpec? = null

    fun create(type: KClass<*>): Map<String, Model> {
        println(type)
        val definitions = HashMap<String, Model>()

        if (isObject(type)) {
            val model = ModelImpl()
            model.type = ModelImpl.OBJECT
            definitions[type.simpleName!!] = model

            val refDefinitions = parseProperties(model, type.declaredMemberProperties)
            definitions.putAll(refDefinitions)
        }
        return definitions
    }

    private fun parseProperties(model: ModelImpl, fields: Collection<KCallable<*>>): Map<String, Model> {
        val refDefinitions = HashMap<String, Model>()

        for (field in fields) {
            if (DefinitionsFactory.ignoreSpec == null) {
                if (isViable(field)) {
                    val property = createProperty(field, field.returnType.jvmErasure.java)

                    val suffix = if (field.returnType.isMarkedNullable) "?" else ""
                    model.addProperty("${field.name}$suffix", property)

                    if (isRef(field.returnType.jvmErasure)) {
                        val definitions = create(field.returnType.jvmErasure)
                        refDefinitions.putAll(definitions)
                    } else if (field.returnType.javaClass.isArray || Collection::class.java.isAssignableFrom(field.returnType.javaClass)) {
                        val childType = getCollectionType(field)
                        if (isRef(childType)) {
                            val definitions = create(childType)
                            refDefinitions.putAll(definitions)
                        }
                    }
                }
            }
        }
        return refDefinitions
    }

    private fun isViable(field: KCallable<*>): Boolean {
        return field.visibility == KVisibility.PUBLIC
    }

    fun createProperty(field: KCallable<*>, fieldClass: Class<*>): Property {
        println("creating property")
        println("field = [${field}], fieldClass = [${fieldClass}]")
        return when {
            fieldClass.isEnum -> {
                val property = StringProperty()
                property._enum(fieldClass.enumConstants.map { o -> (o as Enum<*>).name })
            }
            fieldClass == Boolean::class.javaPrimitiveType || fieldClass == Boolean::class.java -> BooleanProperty()
            fieldClass == ByteArray::class.java -> ByteArrayProperty()
            fieldClass == Date::class.java || fieldClass == java.sql.Date::class.java -> DateProperty()
            fieldClass == Number::class.java -> DecimalProperty()
            fieldClass == Double::class.java || fieldClass == Double::class.javaPrimitiveType -> DoubleProperty()
            fieldClass == Float::class.java || fieldClass == Float::class.javaPrimitiveType -> FloatProperty()
            fieldClass == Int::class.java || fieldClass == Int::class.javaPrimitiveType || fieldClass == Integer::class.java -> IntegerProperty()
            fieldClass == Long::class.java || fieldClass == Long::class.javaPrimitiveType -> LongProperty()
            fieldClass == String::class.java -> StringProperty()
            fieldClass == UUID::class.java -> UUIDProperty()
            fieldClass.isArray || Collection::class.java.isAssignableFrom(fieldClass) -> {
                val property = ArrayProperty()
                // FIXME set actual items
                property.items = getCollectionProperty(field)
                property
            }
            File::class.java.isAssignableFrom(fieldClass) -> FileProperty()
            else -> {
                val property = RefProperty()
                property.`$ref` = "#/definitions/" + fieldClass.simpleName
                property
            }
        }
    }

    private fun isRef(fieldClass: KClass<*>): Boolean {
        return !(fieldClass.java.isEnum
                || fieldClass == Boolean::class
                || fieldClass == Boolean::class
                || fieldClass == ByteArray::class
                || fieldClass == Date::class
                || fieldClass == java.sql.Date::class
                || fieldClass == Number::class
                || fieldClass == Double::class
                || fieldClass == Double::class
                || fieldClass == Float::class
                || fieldClass == Float::class
                || fieldClass == Int::class
                || fieldClass == Int::class
                || fieldClass == Long::class
                || fieldClass == Long::class
                || fieldClass == String::class
                || fieldClass == UUID::class
                || fieldClass.java.isArray
                || Collection::class.java.isAssignableFrom(fieldClass.java)
                || File::class.java.isAssignableFrom(fieldClass.java)
                || fieldClass.java.canonicalName.contains("java"))
    }

    private fun isObject(fieldClass: KClass<*>): Boolean {
        return !(fieldClass.java.isEnum
                || fieldClass == Boolean::class.javaPrimitiveType
                || fieldClass == Boolean::class.java
                || fieldClass == ByteArray::class.java
                || fieldClass == Number::class.java
                || fieldClass == Double::class.java
                || fieldClass == Double::class.javaPrimitiveType
                || fieldClass == Float::class.java
                || fieldClass == Float::class.javaPrimitiveType
                || fieldClass == Int::class.java
                || fieldClass == Int::class.javaPrimitiveType
                || fieldClass == Long::class.java
                || fieldClass == Long::class.javaPrimitiveType
                || fieldClass == String::class.java)
    }

    private fun getCollectionProperty(collectionField: KCallable<*>): Property {
        val childType = getCollectionType(collectionField)
        return createProperty(collectionField, childType.java)
    }

    private fun getCollectionType(collectionField: KCallable<*>): KClass<*> {
        try {
            val actualType = collectionField.returnType.arguments[0].type
            if (actualType is KClass<*>) {
                return actualType
            } else if (actualType is ParameterizedType) {
                return actualType as KClass<*>
            }
        } catch (e: ClassCastException) {
            LOGGER.error("Field mapping not supported. ", e)
        }

        // FIXME resolve actual type in strange collection types
        return String::class
    }
}