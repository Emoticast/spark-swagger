package com.beerboy.ss.model.utils

import com.beerboy.ss.model.*
import com.beerboy.ss.model.properties.*
import java.util.*

class PropertyModelConverter {

    fun modelToProperty(model: Model): Property? {

        if (model is ModelImpl) {
            if (model.additionalProperties != null) {
                val mapProperty = MapProperty()
                mapProperty.type = model.type
                mapProperty.allowEmptyValue = model.allowEmptyValue
                mapProperty.setDefault(model.defaultValue as String)
                mapProperty.description = model.description
                mapProperty.example = model.example
                mapProperty.format = model.format
                mapProperty.name = model.name
                mapProperty.title = model.title
                val required = model.required
                if (required != null) {
                    for (name in required) {
                        if (model.name == name) {
                            mapProperty.required = true
                        }
                    }
                }
                mapProperty.xml = model.xml
                mapProperty.vendorExtensions = model.vendorExtensions
                mapProperty.additionalProperties = model.additionalProperties
                return mapProperty
            }

            val property = propertyByType(model)

            if (property is ObjectProperty) {
                val objectProperty = property as ObjectProperty?
                objectProperty!!.properties = model.getProperties()
                objectProperty.example = model.getExample()
                return objectProperty
            }

            return property

        } else if (model is ArrayModel) {
            val property = ArrayProperty()
            val inner = model.items
            property.items = inner
            property.example = model.example
            property.maxItems = model.maxItems
            property.minItems = model.minItems
            property.description = model.description
            property.title = model.title
            property.uniqueItems = model.uniqueItems
            return property

        } else if (model is RefModel) {
            return RefProperty(model.`$ref`)

        } else if (model is ComposedModel) {
            val objectProperty = ObjectProperty()
            objectProperty.description = model.getDescription()
            objectProperty.title = model.getTitle()
            objectProperty.example = model.getExample()
            val requiredProperties = HashSet<String>()
            for (item in model.allOf) {
                val itemProperty = modelToProperty(item)
                if (itemProperty is RefProperty) {
                    val refProperty = itemProperty as RefProperty?
                    objectProperty.property(refProperty!!.simpleRef, itemProperty)

                } else if (itemProperty is ObjectProperty) {
                    val itemPropertyObject = itemProperty as ObjectProperty?
                    if (itemPropertyObject!!.properties != null) {
                        for (key in itemPropertyObject.properties.keys) {
                            objectProperty.property(key, itemPropertyObject.properties[key])
                        }
                    }
                    if (itemPropertyObject.requiredProperties != null) {
                        for (req in itemPropertyObject.requiredProperties!!) {
                            requiredProperties.add(req)
                        }
                    }
                }
            }
            if (requiredProperties.size > 0) {
                objectProperty.requiredProperties = ArrayList(requiredProperties)
            }
            if (model.vendorExtensions != null) {
                for (key in model.vendorExtensions.keys) {
                    objectProperty.vendorExtension(key, model.vendorExtensions[key])
                }
            }
            return objectProperty

        }
        return null
    }

    private fun propertyByType(model: ModelImpl): Property? {
        return PropertyBuilder.build(model.type, model.format, argsFromModel(model))
    }

    private fun argsFromModel(model: ModelImpl?): MutableMap<PropertyBuilder.PropertyId, Any> {
        if (model == null) return mutableMapOf<PropertyBuilder.PropertyId, Any>()
        val args = EnumMap<PropertyBuilder.PropertyId, Any>(PropertyBuilder.PropertyId::class.java)
        args[PropertyBuilder.PropertyId.DESCRIPTION] = model.description
        args[PropertyBuilder.PropertyId.EXAMPLE] = model.example
        args[PropertyBuilder.PropertyId.ENUM] = model.enum
        args[PropertyBuilder.PropertyId.TITLE] = model.title
        args[PropertyBuilder.PropertyId.DEFAULT] = model.defaultValue
        args[PropertyBuilder.PropertyId.DESCRIMINATOR] = model.discriminator
        args[PropertyBuilder.PropertyId.MINIMUM] = model.minimum
        args[PropertyBuilder.PropertyId.MAXIMUM] = model.maximum
        args[PropertyBuilder.PropertyId.UNIQUE_ITEMS] = model.uniqueItems
        args[PropertyBuilder.PropertyId.VENDOR_EXTENSIONS] = model.vendorExtensions
        return args
    }

    fun propertyToModel(property: Property): Model {

        val description = property.description
        val type = property.type
        val format = property.format
        val example: String? = null
        var properties: MutableMap<String, Property>? = null

        /*Object obj = property.getExample();
        if (obj != null) {
            example = obj.toString();
        }*/
        println(property)

        if (property is SealedProperty) {
            val sealedProperty = property
            properties = mutableMapOf<String, Property>()

            return SealedModel(properties!!)
        }

        val allowEmptyValue = property.allowEmptyValue

        if (property is RefProperty) {
            return RefModel(property.`$ref`)
        }

        val extensions = property.vendorExtensions

        var additionalProperties: Property? = null

        if (property is MapProperty) {
            additionalProperties = property.additionalProperties
        }

        val name = property.name
        val xml = property.xml


        if (property is ObjectProperty) {
            properties = property.properties
        }

        if (property is ArrayProperty) {
            val arrayModel = ArrayModel()
            arrayModel.items = property.items
            arrayModel.description = description
            arrayModel.example = example
            arrayModel.uniqueItems = property.uniqueItems

            if (extensions != null) {
                arrayModel.vendorExtensions = extensions
            }

            if (properties != null) {
                arrayModel.properties = properties
            }

            return arrayModel
        }

        val model = ModelImpl()

        model.description = description
        model.example = property.example//example
        model.name = name
        model.xml = xml
        model.type = type
        model.format = format
        model.allowEmptyValue = allowEmptyValue

        if (extensions != null) {
            model.vendorExtensions = extensions
        }

        if (additionalProperties != null) {
            model.additionalProperties = additionalProperties
        }

        if (properties != null) {
            model.properties = properties
        }



        return model
    }
}
