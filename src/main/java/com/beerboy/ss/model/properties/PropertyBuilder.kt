package com.beerboy.ss.model.properties

import com.beerboy.ss.model.ArrayModel
import com.beerboy.ss.model.Model
import com.beerboy.ss.model.ModelImpl
import com.beerboy.ss.model.RefModel
import com.emoticast.extensions.print
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.util.*

object PropertyBuilder {
    internal val LOGGER = LoggerFactory.getLogger(PropertyBuilder::class.java)

    /**
     * Creates new property on the passed arguments.
     *
     * @param type   property type
     * @param format property format
     * @param args   mapping of argument identifier to value
     * @return new property instance or `null` for unknown types
     */
    fun build(type: String, format: String?, args: MutableMap<PropertyId, Any>?): Property? {
        val processor = Processor.fromType(type, format) ?: return null
        val safeArgs = args ?: mutableMapOf()
        val fixedArgs: MutableMap<PropertyId, Any>
        if (format != null) {
            fixedArgs = EnumMap(PropertyId::class.java)
            fixedArgs.putAll(safeArgs)
            fixedArgs[PropertyId.FORMAT] = format
        } else {
            fixedArgs = safeArgs
        }
        return processor.build(fixedArgs)
    }

    /**
     * Merges passed arguments into an existing property instance.
     *
     * @param property property to be updated
     * @param args     mapping of argument identifier to value. `null`s
     * will replace existing values
     * @return updated property instance
     */
    fun merge(property: Property, args: Map<PropertyId, Any>?): Property {
        if (args != null && !args.isEmpty()) {
            val processor = Processor.fromProperty(property)
            processor?.merge(property, args)
        }
        return property
    }

    /**
     * Converts passed property into a model.
     *
     * @param property property to be converted
     * @return model instance or `null` for unknown types
     */
    fun toModel(property: Property): Model? {
        val processor = Processor.fromProperty(property)
        return processor?.toModel(property)
    }

    enum class PropertyId private constructor(val propertyName: String) {
        ENUM("enum"),
        TITLE("title"),
        DESCRIPTION("description"),
        DEFAULT("default"),
        PATTERN("pattern"),
        DESCRIMINATOR("discriminator"),
        MIN_ITEMS("minItems"),
        MAX_ITEMS("maxItems"),
        MIN_PROPERTIES("minProperties"),
        MAX_PROPERTIES("maxProperties"),
        MIN_LENGTH("minLength"),
        MAX_LENGTH("maxLength"),
        MINIMUM("minimum"),
        MAXIMUM("maximum"),
        EXCLUSIVE_MINIMUM("exclusiveMinimum"),
        EXCLUSIVE_MAXIMUM("exclusiveMaximum"),
        UNIQUE_ITEMS("uniqueItems"),
        EXAMPLE("example"),
        TYPE("type"),
        FORMAT("format"),
        READ_ONLY("readOnly"),
        REQUIRED("required"),
        VENDOR_EXTENSIONS("vendorExtensions"),
        ALLOW_EMPTY_VALUE("allowEmptyValue"),
        MULTIPLE_OF("multipleOf");

        fun <T> findValue(args: Map<PropertyId, Any>): T? {
            return args[this] as T
        }
    }

    private enum class Processor private constructor(private val type: Class<out Property>) {
        BOOLEAN(BooleanProperty::class.java) {
            override fun isType(type: String, format: String?): Boolean {
                return BooleanProperty.isType(type, format)
            }

            override fun create(): BooleanProperty {
                return BooleanProperty()
            }

            override fun merge(property: Property, args: Map<PropertyId, Any>): Property {
                super.merge(property, args)
                if (property is BooleanProperty) {
                    if (args.containsKey(PropertyId.DEFAULT)) {
                        val value = PropertyId.DEFAULT.findValue<String>(args)
                        if (value != null) {
                            property.setDefault(value)
                        } else {
                            property.default = null
                        }
                    }
                }

                return property
            }
        },
        BYTE_ARRAY(ByteArrayProperty::class.java) {
            override fun isType(type: String, format: String?): Boolean {
                return ByteArrayProperty.isType(type, format)
            }

            override fun create(): ByteArrayProperty {
                return ByteArrayProperty()
            }

            override fun merge(property: Property, args: Map<PropertyId, Any>): Property {
                super.merge(property, args)
                if (property is ByteArrayProperty) {
                    mergeString(property, args)
                    // the string properties for pattern and enum will be ignored, they doesn't make sense for
                    // base64 encoded strings - instead an appropriate base64 pattern is set
                    property.enum = null
                    property.setPattern("^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$")
                }

                return property
            }

            override fun toModel(property: Property): Model? {
                return if (isType(property)) {
                    createStringModel(property as StringProperty)
                } else null

            }
        },
        BINARY(BinaryProperty::class.java) {
            override fun isType(type: String, format: String?): Boolean {
                return BinaryProperty.isType(type, format)
            }

            override fun create(): BinaryProperty {
                return BinaryProperty()
            }
        },
        DATE(DateProperty::class.java) {
            override fun isType(type: String, format: String?): Boolean {
                return DateProperty.isType(type, format)
            }

            override fun create(): DateProperty {
                return DateProperty()
            }
        },
        DATE_TIME(DateTimeProperty::class.java) {
            override fun isType(type: String, format: String?): Boolean {
                return DateTimeProperty.isType(type, format)
            }

            override fun create(): DateTimeProperty {
                return DateTimeProperty()
            }
        },
        INT(IntegerProperty::class.java) {
            override fun isType(type: String, format: String?): Boolean {
                return IntegerProperty.isType(type, format)
            }

            override fun create(): IntegerProperty {
                return IntegerProperty()
            }

            override fun merge(property: Property, args: Map<PropertyId, Any>): Property {
                super.merge(property, args)
                if (property is IntegerProperty) {
                    mergeNumeric(property, args)
                    if (args.containsKey(PropertyId.DEFAULT)) {
                        val value = PropertyId.DEFAULT.findValue<String>(args)
                        if (value != null) {
                            property.setDefault(value)
                        } else {
                            property.default = null!!.toInt()
                        }
                    }
                }
                return property
            }

            override fun toModel(property: Property): Model? {
                if (isType(property)) {
                    val resolved = property as IntegerProperty
                    val model = createModel(resolved)
                    val defaultValue = resolved.default
                    if (defaultValue != null) {
                        model.setDefaultValue(defaultValue.toString())
                    }
                    return model
                }
                return null
            }
        },
        LONG(LongProperty::class.java) {
            override fun isType(type: String, format: String?): Boolean {
                return LongProperty.isType(type, format)
            }

            override fun create(): LongProperty {
                return LongProperty()
            }

            override fun merge(property: Property, args: Map<PropertyId, Any>): Property {
                super.merge(property, args)
                if (property is LongProperty) {
                    mergeNumeric(property, args)
                    if (args.containsKey(PropertyId.DEFAULT)) {
                        val value = PropertyId.DEFAULT.findValue<String>(args)
                        if (value != null) {
                            property.setDefault(value)
                        } else {
                            property.default = null!!.toLong()
                        }
                    }
                }
                return property
            }

            override fun toModel(property: Property): Model? {
                if (isType(property)) {
                    val resolved = property as LongProperty
                    val model = createModel(resolved)
                    val defaultValue = resolved.default
                    if (defaultValue != null) {
                        model.setDefaultValue(defaultValue.toString())
                    }
                    return model
                }
                return null
            }
        },
        FLOAT(FloatProperty::class.java) {
            override fun isType(type: String, format: String?): Boolean {
                return FloatProperty.isType(type, format)
            }

            override fun create(): FloatProperty {
                return FloatProperty()
            }

            override fun merge(property: Property, args: Map<PropertyId, Any>): Property {
                super.merge(property, args)
                if (property is FloatProperty) {
                    mergeNumeric(property, args)
                    if (args.containsKey(PropertyId.DEFAULT)) {
                        val value = PropertyId.DEFAULT.findValue<String>(args)
                        if (value != null) {
                            property.setDefault(value)
                        } else {
                            property.default = null!!.toFloat()
                        }
                    }
                }
                return property
            }

            override fun toModel(property: Property): Model? {
                if (isType(property)) {
                    val resolved = property as FloatProperty
                    val model = createModel(resolved)
                    val defaultValue = resolved.default
                    if (defaultValue != null) {
                        model.setDefaultValue(defaultValue.toString())
                    }
                    return model
                }
                return null
            }
        },
        DOUBLE(DoubleProperty::class.java) {
            override fun isType(type: String, format: String?): Boolean {
                return DoubleProperty.isType(type, format)
            }

            override fun create(): DoubleProperty {
                return DoubleProperty()
            }

            override fun merge(property: Property, args: Map<PropertyId, Any>): Property {
                super.merge(property, args)
                if (property is DoubleProperty) {
                    mergeNumeric(property, args)
                    if (args.containsKey(PropertyId.DEFAULT)) {
                        val value = PropertyId.DEFAULT.findValue<String>(args)
                        if (value != null) {
                            property.setDefault(value)
                        } else {
                            property.default = null!!.toDouble()
                        }
                    }
                }
                return property
            }

            override fun toModel(property: Property): Model? {
                if (isType(property)) {
                    val resolved = property as DoubleProperty
                    val model = createModel(resolved)
                    val defaultValue = resolved.default
                    if (defaultValue != null) {
                        model.setDefaultValue(defaultValue.toString())
                    }
                    return model
                }
                return null
            }
        },

        // note: this must be in the enum order after both INT and LONG
        // (and any integer types added in the future), so the more specific
        // ones will be found first.
        INTEGER(BaseIntegerProperty::class.java) {
            override fun isType(type: String, format: String?): Boolean {
                return BaseIntegerProperty.isType(type, format)
            }

            override fun create(): BaseIntegerProperty {
                return BaseIntegerProperty()
            }

            override fun merge(property: Property, args: Map<PropertyId, Any>): Property {
                super.merge(property, args)
                if (property is BaseIntegerProperty) {
                    mergeNumeric(property, args)
                }
                return property
            }
        },

        // note: this must be in the enum order after both DOUBLE and FLOAT
        // (and any number types added in the future), so the more specific
        // ones will be found first.
        DECIMAL(DecimalProperty::class.java) {
            override fun isType(type: String, format: String?): Boolean {
                return DecimalProperty.isType(type, format)
            }

            override fun create(): DecimalProperty {
                return DecimalProperty()
            }

            override fun merge(property: Property, args: Map<PropertyId, Any>): Property {
                super.merge(property, args)
                if (property is DecimalProperty) {
                    mergeNumeric(property, args)
                }
                return property
            }
        },
        FILE(FileProperty::class.java) {
            override fun isType(type: String, format: String?): Boolean {
                return FileProperty.isType(type, format)
            }

            override fun create(): FileProperty {
                return FileProperty()
            }
        },
        REFERENCE(RefProperty::class.java) {
            override fun isType(type: String, format: String?): Boolean {
                return RefProperty.isType(type, format)
            }

            override fun create(): RefProperty {
                return RefProperty()
            }

            override fun toModel(property: Property): Model? {
                if (property is RefProperty) {
                    val model = RefModel(property.`$ref`)
                    model.description = property.getDescription()
                    return model
                }
                return null
            }
        },
        E_MAIL(EmailProperty::class.java) {
            override fun isType(type: String, format: String?): Boolean {
                return EmailProperty.isType(type, format)
            }

            override fun create(): EmailProperty {
                return EmailProperty()
            }

            override fun merge(property: Property, args: Map<PropertyId, Any>): Property {
                super.merge(property, args)
                if (property is EmailProperty) {
                    mergeString(property, args)
                }
                return property
            }

            override fun toModel(property: Property): Model? {
                return if (isType(property)) {
                    createStringModel(property as StringProperty)
                } else null
            }
        },
        UUID(UUIDProperty::class.java) {
            override fun isType(type: String, format: String?): Boolean {
                return UUIDProperty.isType(type, format!!)
            }

            override fun create(): UUIDProperty {
                return UUIDProperty()
            }

            override fun merge(property: Property, args: Map<PropertyId, Any>): Property {
                super.merge(property, args)
                if (property is UUIDProperty) {
                    if (args.containsKey(PropertyId.DEFAULT)) {
                        val value = PropertyId.DEFAULT.findValue<String>(args)
                        property.setDefault(value)
                    }
                    if (args.containsKey(PropertyId.MIN_LENGTH)) {
                        val value = PropertyId.MIN_LENGTH.findValue<Int>(args)
                        property.copy(minLength = value)
                    }
                    if (args.containsKey(PropertyId.MAX_LENGTH)) {
                        val value = PropertyId.MAX_LENGTH.findValue<Int>(args)
                        property.copy(maxLength = value)
                    }
                    if (args.containsKey(PropertyId.PATTERN)) {
                        val value = PropertyId.PATTERN.findValue<String>(args)
                        property.copy(pattern = value)
                    }
                }
                return property
            }

            override fun toModel(property: Property): Model? {
                if (isType(property)) {
                    val resolved = property as UUIDProperty
                    val model = createModel(resolved)
                    model.setDefaultValue(resolved.default)
                    return model
                }
                return null
            }
        },
        OBJECT(ObjectProperty::class.java) {
            override fun isType(type: String, format: String?): Boolean {
                return ObjectProperty.isType(type, format)
            }

            override fun create(): ObjectProperty {
                return ObjectProperty()
            }
        },
        UNTYPED(UntypedProperty::class.java) {
            override fun isType(type: String, format: String?): Boolean {
                return UntypedProperty.isType(type, format)
            }

            override fun create(): UntypedProperty {
                return UntypedProperty()
            }
        },
        ARRAY(ArrayProperty::class.java) {
            override fun isType(type: String, format: String?): Boolean {
                return ArrayProperty.isType(type)
            }

            override fun create(): ArrayProperty {
                return ArrayProperty()
            }

            override fun toModel(property: Property): Model? {
                return if (property is ArrayProperty) {
                    ArrayModel().items(property.getItems()).description(property.getDescription())
                } else null
            }

            override fun merge(property: Property, args: Map<PropertyId, Any>): Property {
                super.merge(property, args)
                if (property is ArrayProperty) {
                    if (args.containsKey(PropertyId.MIN_ITEMS)) {
                        val value = PropertyId.MIN_ITEMS.findValue<Int>(args)
                        property.minItems = value
                    }
                    if (args.containsKey(PropertyId.MAX_ITEMS)) {
                        val value = PropertyId.MAX_ITEMS.findValue<Int>(args)
                        property.maxItems = value
                    }
                    if (args.containsKey(PropertyId.UNIQUE_ITEMS)) {
                        val value = PropertyId.UNIQUE_ITEMS.findValue<Boolean>(args)
                        property.setUniqueItems(value)
                    }
                }

                return property
            }
        },
        MAP(MapProperty::class.java) {
            override fun isType(type: String, format: String?): Boolean {
                // Note: It's impossible to distinct MAP and OBJECT as they use the same
                // set of values for type and format
                return MapProperty.isType(type, format)
            }

            override fun create(): MapProperty {
                return MapProperty()
            }

            override fun toModel(property: Property): Model? {
                return if (property is MapProperty) {
                    createModel(property).additionalProperties(property.additionalProperties)
                } else null
            }
        },

        // String is intentionally last, so it is found after the more specific property
        // types which also use the "string" type.
        STRING(StringProperty::class.java) {
            override fun isType(type: String, format: String?): Boolean {
                return StringProperty.isType(type, format)
            }

            override fun create(): StringProperty {
                return StringProperty()
            }

            override fun merge(property: Property, args: Map<PropertyId, Any>): Property {
                super.merge(property, args)
                if (property is StringProperty) {
                    mergeString(property, args)
                }

                return property
            }

            override fun toModel(property: Property): Model? {
                return if (isType(property)) {
                    createStringModel(property as StringProperty)
                } else null

            }

        };

        protected abstract fun isType(type: String, format: String?): Boolean

        protected fun isType(property: Property?): Boolean {
            return type.isInstance(property)
        }

        protected abstract fun create(): Property

        protected fun <N : AbstractNumericProperty> mergeNumeric(property: N, args: Map<PropertyId, Any>): N {
            if (args.containsKey(PropertyId.MINIMUM)) {
                val value = PropertyId.MINIMUM.findValue<BigDecimal>(args)
                if (value != null) {
                    property.setMinimum(value)
                }
            }
            if (args.containsKey(PropertyId.MAXIMUM)) {
                val value = PropertyId.MAXIMUM.findValue<BigDecimal>(args)
                if (value != null) {
                    property.setMaximum(value)
                }
            }
            if (args.containsKey(PropertyId.EXCLUSIVE_MINIMUM)) {
                val value = PropertyId.EXCLUSIVE_MINIMUM.findValue<Boolean>(args)
                property.setExclusiveMinimum(value)
            }
            if (args.containsKey(PropertyId.EXCLUSIVE_MAXIMUM)) {
                val value = PropertyId.EXCLUSIVE_MAXIMUM.findValue<Boolean>(args)
                property.setExclusiveMaximum(value)
            }
            if (args.containsKey(PropertyId.MULTIPLE_OF)) {
                val value = PropertyId.MULTIPLE_OF.findValue<BigDecimal>(args)
                if (value != null) {
                    property.setMultipleOf(value)
                }
            }
            return property
        }

        protected fun <N : StringProperty> mergeString(property: N, args: Map<PropertyId, Any>): N {
            if (args.containsKey(PropertyId.DEFAULT)) {
                val value = PropertyId.DEFAULT.findValue<String>(args)
                property.default = value
            }
            if (args.containsKey(PropertyId.MIN_LENGTH)) {
                val value = PropertyId.MIN_LENGTH.findValue<Int>(args)
                property.setMinLength(value)
            }
            if (args.containsKey(PropertyId.MAX_LENGTH)) {
                val value = PropertyId.MAX_LENGTH.findValue<Int>(args)
                property.setMaxLength(value)
            }
            if (args.containsKey(PropertyId.PATTERN)) {
                val value = PropertyId.PATTERN.findValue<String>(args)
                property.setPattern(value)
            }
            if (args.containsKey(PropertyId.ENUM)) {
                val value = PropertyId.ENUM.findValue<List<String>>(args)
                property.enum = value
            }
            return property
        }

        protected fun createModel(property: Property): ModelImpl {
            return ModelImpl().type(property.type).format(property.format)
                    .description(property.description)
        }

        protected fun createStringModel(property: StringProperty): ModelImpl {
            val model = createModel(property)
            model.setDefaultValue(property.default)
            return model
        }

        /**
         * Creates new property on the passed arguments.
         *
         * @param args mapping of argument identifier to value
         * @return new property instance
         */
        fun build(args: Map<PropertyId, Any>): Property {
            return merge(create(), args)
        }

        /**
         * Merges passed arguments into an existing property instance.
         *
         * @param property property to be updated
         * @param args     mapping of argument identifier to value. `null`s
         * will replace existing values
         * @return updated property instance
         */
        open fun merge(property: Property, args: Map<PropertyId, Any>): Property {
            if (args.containsKey(PropertyId.READ_ONLY)) {
                property.readOnly = PropertyId.READ_ONLY.findValue(args)
            }
            if (property is AbstractProperty) {
                if (property.getFormat() == null) {
                    property.setFormat(PropertyId.FORMAT.findValue(args))
                }
                if (args.containsKey(PropertyId.ALLOW_EMPTY_VALUE)) {
                    val value = PropertyId.ALLOW_EMPTY_VALUE.findValue<Boolean>(args)
                    property.setAllowEmptyValue(value)
                }
                if (args.containsKey(PropertyId.TITLE)) {
                    val value = PropertyId.TITLE.findValue<String>(args)
                    property.setTitle(value)
                }
                if (args.containsKey(PropertyId.DESCRIPTION)) {
                    val value = PropertyId.DESCRIPTION.findValue<String>(args)
                    property.setDescription(value)
                }
                if (args.containsKey(PropertyId.EXAMPLE)) {
                    val value = PropertyId.EXAMPLE.findValue<Any>(args)
                    property.setExample(value)
                }
                if (args.containsKey(PropertyId.VENDOR_EXTENSIONS)) {
                    val value = PropertyId.VENDOR_EXTENSIONS.findValue<Map<String, Any>>(args)
                    property.setVendorExtensionMap(value)
                }
                if (args.containsKey(PropertyId.ENUM)) {
                    val values = PropertyId.ENUM.findValue<List<String>>(args)
                    if (values != null) {
                        if (property is BooleanProperty) {
                            for (value in values) {
                                try {
                                    property._enum(java.lang.Boolean.parseBoolean(value))
                                } catch (e: Exception) {
                                    // continue
                                }

                            }
                        }
                        if (property is IntegerProperty) {
                            for (value in values) {
                                try {
                                    property._enum(Integer.parseInt(value))
                                } catch (e: Exception) {
                                    // continue
                                }

                            }
                        }
                        if (property is LongProperty) {
                            for (value in values) {
                                try {
                                    property._enum(java.lang.Long.parseLong(value))
                                } catch (e: Exception) {
                                    // continue
                                }

                            }
                        }
                        if (property is DoubleProperty) {
                            for (value in values) {
                                try {
                                    property._enum(java.lang.Double.parseDouble(value))
                                } catch (e: Exception) {
                                    // continue
                                }

                            }
                        }
                        if (property is FloatProperty) {
                            for (value in values) {
                                try {
                                    property._enum(java.lang.Float.parseFloat(value))
                                } catch (e: Exception) {
                                    // continue
                                }

                            }
                        }
                        if (property is DateProperty) {
                            for (value in values) {
                                try {
                                    property._enum(value)
                                } catch (e: Exception) {
                                    // continue
                                }

                            }
                        }
                        if (property is DateTimeProperty) {
                            for (value in values) {
                                try {
                                    property._enum(value)
                                } catch (e: Exception) {
                                    // continue
                                }

                            }
                        }
                        if (property is UUIDProperty) {
                            val (_enum) = property
                            for (value in values) {
                                try {
                                    property.withEnum(value)
                                } catch (e: Exception) {
                                    // continue
                                }

                            }
                        }
                    }
                }
            }
            return property
        }

        /**
         * Converts passed property into a model.
         *
         * @param property property to be converted
         * @return model instance or `null` for unknown types
         */
        open fun toModel(property: Property): Model? {
            return createModel(property)
        }

        companion object {

            fun fromType(type: String, format: String?): Processor? {
                for (item in values()) {
                    if (item.isType(type, format)) {
                        return item
                    }
                }
                LOGGER.debug("no property for $type, $format")
                return null
            }

            fun fromProperty(property: Property?): Processor? {
                for (item in values()) {
                    if (item.isType(property)) {
                        return item
                    }
                }
                LOGGER.error("no property for " + if (property == null) "null" else property.javaClass.name)
                return null
            }
        }
    }
}
