package com.beerboy.ss

import com.beerboy.spark.typify.spec.IgnoreSpec
import com.beerboy.ss.factory.DefinitionsFactory
import com.beerboy.ss.factory.ParamsFactory
import com.beerboy.ss.model.*
import com.beerboy.ss.model.auth.SecuritySchemeDefinition
import com.beerboy.ss.model.parameters.BodyParameter
import com.beerboy.ss.model.parameters.Parameter
import com.beerboy.ss.model.properties.Property
import com.beerboy.ss.model.utils.PropertyModelConverter
import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import org.slf4j.LoggerFactory
import java.util.*

/**
 * @author manusant
 */
@JsonInclude(Include.NON_NULL)
class Swagger {

    // getter & setters
    var swagger: String? = "2.0"
    var info: Info? = null
    var host: String? = null
    public var basePath: String? = null
    private var tags: MutableList<Tag>? = null
    private var schemes: MutableList<Scheme>? = null
    private var consumes: MutableList<String>? = null
    private var produces: MutableList<String>? = null
    private var security: MutableList<SecurityRequirement>? = null
    private var paths: MutableMap<String, Path>? = null
    private var securityDefinitions: MutableMap<String, SecuritySchemeDefinition>? = null
    private var definitions: MutableMap<String, Model>? = null
    var externalDocs: ExternalDocs? = null
    private var parameters: MutableMap<String, Parameter>? = null
    private var responses: MutableMap<String, Response>? = null
    private var vendorExtensions: MutableMap<String, Any>? = null
    @JsonIgnore
    private var apiEndpoints: MutableList<ApiEndpoint>? = null
    @JsonIgnore
    private var ignoreSpec: IgnoreSpec? = null


    var securityRequirement: MutableList<SecurityRequirement>?
        @JsonIgnore
        @Deprecated("Use {@link #getSecurity()}.")
        get() = security
        @JsonIgnore
        @Deprecated("Use {@link #setSecurity(List)}.")
        set(securityRequirements) {
            this.security = securityRequirements
        }

    fun endpoints(apiEndpoints: MutableList<ApiEndpoint>): Swagger {
        this.apiEndpoints = apiEndpoints
        return this
    }

    fun info(info: Info): Swagger {
        this.info = info
        return this
    }

    fun host(host: String): Swagger {
        this.host = host
        return this
    }

    fun basePath(basePath: String): Swagger {
        this.basePath = basePath
        return this
    }

    fun externalDocs(value: ExternalDocs): Swagger {
        this.externalDocs = value
        return this
    }

    fun tags(tags: MutableList<Tag>): Swagger {
        this.setTags(tags)
        return this
    }

    fun tag(tag: Tag): Swagger {
        this.addTag(tag)
        return this
    }

    fun schemes(schemes: MutableList<Scheme>): Swagger {
        this.setSchemes(schemes)
        return this
    }

    fun scheme(scheme: Scheme): Swagger {
        this.addScheme(scheme)
        return this
    }

    fun consumes(consumes: MutableList<String>): Swagger {
        this.setConsumes(consumes)
        return this
    }

    fun consumes(consumes: String): Swagger {
        this.addConsumes(consumes)
        return this
    }

    fun produces(produces: MutableList<String>): Swagger {
        this.setProduces(produces)
        return this
    }

    fun produces(produces: String): Swagger {
        this.addProduces(produces)
        return this
    }

    fun paths(paths: MutableMap<String, Path>): Swagger {
        this.setPaths(paths)
        return this
    }

    fun path(key: String, path: Path): Swagger {
        if (this.paths == null) {
            this.paths = LinkedHashMap()
        }
        this.paths!![key] = path
        return this
    }

    fun responses(responses: MutableMap<String, Response>): Swagger {
        this.responses = responses
        return this
    }

    fun response(key: String, response: Response): Swagger {
        if (this.responses == null) {
            this.responses = LinkedHashMap()
        }
        this.responses!![key] = response
        return this
    }

    fun parameter(key: String, parameter: Parameter): Swagger {
        this.addParameter(key, parameter)
        return this
    }

    fun securityDefinition(name: String, securityDefinition: SecuritySchemeDefinition): Swagger {
        this.addSecurityDefinition(name, securityDefinition)
        return this
    }

    fun model(name: String, model: Model): Swagger {
        this.addDefinition(name, model)
        return this
    }

    fun security(securityRequirement: SecurityRequirement): Swagger {
        this.addSecurity(securityRequirement)
        return this
    }

    fun ignores(ignoreConf: IgnoreSpec): Swagger {
        this.ignoreSpec = ignoreConf
        return this
    }

    fun vendorExtension(key: String, extension: Any): Swagger {
        if (this.vendorExtensions == null) {
            this.vendorExtensions = LinkedHashMap()
        }
        this.vendorExtensions!![key] = extension
        return this
    }

    fun getApiEndpoints(): List<ApiEndpoint>? {
        return apiEndpoints
    }

    fun addApiEndpoint(endpoint: ApiEndpoint) {
        if (apiEndpoints == null) {
            apiEndpoints = ArrayList()
        }
        apiEndpoints!!.add(endpoint)
    }

    fun getSchemes(): List<Scheme>? {
        return schemes
    }

    fun setSchemes(schemes: MutableList<Scheme>) {
        this.schemes = schemes
    }

    fun addScheme(scheme: Scheme) {
        if (schemes == null) {
            schemes = ArrayList()
        }
        if (!schemes!!.contains(scheme)) {
            schemes!!.add(scheme)
        }
    }

    fun getTags(): List<Tag>? {
        return tags
    }

    fun setTags(tags: MutableList<Tag>) {
        this.tags = tags
    }

    fun getTag(tagName: String?): Tag? {
        var tag: Tag? = null
        if (this.tags != null && tagName != null) {
            for (existing in this.tags!!) {
                if (existing.name == tagName) {
                    tag = existing
                    break
                }
            }
        }
        return tag
    }

    fun addTag(tag: Tag?) {
        if (this.tags == null) {
            this.tags = ArrayList()
        }
        if (tag != null && tag.name != null) {
            if (getTag(tag.name) == null) {
                this.tags!!.add(tag)
            }
        }
    }

    fun getConsumes(): List<String>? {
        return consumes
    }

    fun setConsumes(consumes: MutableList<String>) {
        this.consumes = consumes
    }

    fun addConsumes(consumes: String) {
        if (this.consumes == null) {
            this.consumes = ArrayList()
        }

        if (!this.consumes!!.contains(consumes)) {
            this.consumes!!.add(consumes)
        }
    }

    fun getProduces(): List<String>? {
        return produces
    }

    fun setProduces(produces: MutableList<String>) {
        this.produces = produces
    }

    fun addProduces(produces: String) {
        if (this.produces == null) {
            this.produces = ArrayList()
        }

        if (!this.produces!!.contains(produces)) {
            this.produces!!.add(produces)
        }
    }

    fun getPaths(): Map<String, Path>? {
        return paths
    }

    fun setPaths(paths: MutableMap<String, Path>) {
        this.paths = paths
    }

    fun getPath(path: String): Path? {
        return if (this.paths == null) {
            null
        } else this.paths!![path]
    }

    fun getSecurityDefinitions(): Map<String, SecuritySchemeDefinition>? {
        return securityDefinitions
    }

    fun setSecurityDefinitions(securityDefinitions: MutableMap<String, SecuritySchemeDefinition>) {
        this.securityDefinitions = securityDefinitions
    }

    fun addSecurityDefinition(name: String, securityDefinition: SecuritySchemeDefinition) {
        if (this.securityDefinitions == null) {
            this.securityDefinitions = LinkedHashMap()
        }
        this.securityDefinitions!![name] = securityDefinition
    }


    @JsonIgnore
    @Deprecated("Use {@link #addSecurity(SecurityRequirement)}.")
    fun addSecurityDefinition(securityRequirement: SecurityRequirement) {
        this.addSecurity(securityRequirement)
    }

    fun getSecurity(): List<SecurityRequirement>? {
        return security
    }

    fun setSecurity(securityRequirements: MutableList<SecurityRequirement>) {
        this.security = securityRequirements
    }

    fun addSecurity(securityRequirement: SecurityRequirement) {
        if (this.security == null) {
            this.security = ArrayList()
        }
        this.security!!.add(securityRequirement)
    }

    fun getDefinitions(): Map<String, Model>? {
        return definitions
    }

    fun setDefinitions(definitions: MutableMap<String, Model>) {
        this.definitions = definitions
    }

    fun addDefinition(key: String, model: Model) {
        if (this.definitions == null) {
            this.definitions = LinkedHashMap()
        }
        this.definitions!![key] = model
    }

    fun hasDefinition(key: String): Boolean {
        return if (this.definitions == null) {
            false
        } else this.definitions!!.keys.contains(key)
    }

    fun getParameters(): Map<String, Parameter>? {
        return parameters
    }

    fun setParameters(parameters: MutableMap<String, Parameter>) {
        this.parameters = parameters
    }

    fun getParameter(parameter: String): Parameter? {
        return if (this.parameters == null) {
            null
        } else this.parameters!![parameter]
    }

    fun addParameter(key: String, parameter: Parameter) {
        if (this.parameters == null) {
            this.parameters = LinkedHashMap()
        }
        this.parameters!![key] = parameter
    }

    fun getResponses(): Map<String, Response>? {
        return responses
    }

    fun setResponses(responses: MutableMap<String, Response>) {
        this.responses = responses
    }

    @JsonAnyGetter
    fun getVendorExtensions(): Map<String, Any>? {
        return vendorExtensions
    }

    @JsonAnySetter
    fun setVendorExtension(name: String, value: Any) {
        if (name.startsWith("x-")) {
            vendorExtension(name, value)
        }
    }

    fun parse() {
        LOGGER.debug("Spark-Swagger: Start parsing metadata")
        if (apiEndpoints != null) {
            apiEndpoints!!.forEach { endpoint ->
                if (ignoreSpec == null || !ignoreSpec!!.ignored(endpoint.endpointDescriptor.path)) {

                    tag(endpoint.endpointDescriptor.tag)
                    endpoint.methodDescriptors.forEach { methodDescriptor ->

                        val op = Operation()
                        op.tag(endpoint.endpointDescriptor.tag.name)
                        op.description(methodDescriptor.description)

                        val parameters = ParamsFactory.create(methodDescriptor.path, methodDescriptor.parameters)
                        op.parameters = parameters

                        // Supply Ignore configurations
                        DefinitionsFactory.ignoreSpec = this.ignoreSpec

                        if (methodDescriptor.requestType != null) {
                            // Process fields
                            val definitions = DefinitionsFactory.create(methodDescriptor.requestType!!)
                            for (key in definitions.keys) {
                                if (!hasDefinition(key)) {
                                    addDefinition(key, definitions[key]!!)
                                }
                            }

                            val model: Model
                            if (definitions.isEmpty()) {
                                val property = DefinitionsFactory.createProperty(null!!, methodDescriptor.requestType!!.java)
                                model = PropertyModelConverter().propertyToModel(property)
                            } else {
                                val refModel = RefModel()
                                refModel.`$ref` = methodDescriptor.requestType!!.simpleName
                                model = refModel
                            }

                            val requestBody = BodyParameter()
                            requestBody.description("Body object description")
                            requestBody.required = true
                            requestBody.schema = model
                            op.addParameter(requestBody)
                        }

                        if (methodDescriptor.responseType != null) {
                            // Process fields
                            val definitions = DefinitionsFactory.create(methodDescriptor.responseType!!)
                            for (key in definitions.keys) {
                                if (!hasDefinition(key)) {
                                    addDefinition(key, definitions[key]!!)
                                }
                            }

                            val property: Property?
                            if (definitions.isEmpty()) {
                                property = DefinitionsFactory.createProperty(null!!, methodDescriptor.responseType!!.java)
                            } else {
                                val refModel = RefModel()
                                refModel.`$ref` = methodDescriptor.responseType!!.simpleName
                                property = PropertyModelConverter().modelToProperty(refModel)
                            }

                            val responseBody = Response()
                            responseBody.description("successful operation")
                            responseBody.schema = property
                            op.addResponse("200", responseBody)

                        } else {
                            val responseBody = Response()
                            responseBody.description("successful operation")
                            op.addResponse("200", responseBody)
                        }

                        if (methodDescriptor.produces != null) {
                            op.produces(methodDescriptor.produces)
                        }
                        if (methodDescriptor.consumes != null) {
                            op.consumes(methodDescriptor.consumes)
                        }

                        addOperation(methodDescriptor.path, methodDescriptor.method, op)
                    }
                }
            }
            LOGGER.debug("Spark-Swagger: metadata successfully parsed")
        } else {
            LOGGER.debug("Spark-Swagger: No metadata to parse. Please check your SparkSwagger configurations and Endpoints Resolver")
        }
    }

    private fun addOperation(pathStr: String?, method: HttpMethod?, op: Operation) {
        val formattedPath = ParamsFactory.formatPath(pathStr)
        if (paths != null && paths!!.containsKey(formattedPath)) {
            val path = paths!![formattedPath]
            path!!.set(method, op)
        } else {
            val path = Path()
            path.set(method, op)
            path(formattedPath, path)
        }
        LOGGER.debug("Spark-Swagger: " + method!!.name + " " + formattedPath + " parsed")
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + if (basePath == null) 0 else basePath!!.hashCode()
        result = prime * result + if (consumes == null) 0 else consumes!!.hashCode()
        result = prime * result + if (definitions == null) 0 else definitions!!.hashCode()
        result = prime * result + if (externalDocs == null) 0 else externalDocs!!.hashCode()
        result = prime * result + if (host == null) 0 else host!!.hashCode()
        result = prime * result + if (info == null) 0 else info!!.hashCode()
        result = prime * result + if (parameters == null) 0 else parameters!!.hashCode()
        result = prime * result + if (paths == null) 0 else paths!!.hashCode()
        result = prime * result + if (produces == null) 0 else produces!!.hashCode()
        result = prime * result + if (responses == null) 0 else responses!!.hashCode()
        result = prime * result + if (schemes == null) 0 else schemes!!.hashCode()
        result = prime * result + if (security == null) 0 else security!!.hashCode()
        result = prime * result + if (securityDefinitions == null) 0 else securityDefinitions!!.hashCode()
        result = prime * result + if (swagger == null) 0 else swagger!!.hashCode()
        result = prime * result + if (tags == null) 0 else tags!!.hashCode()
        result = prime * result + if (vendorExtensions == null) 0 else vendorExtensions!!.hashCode()
        return result
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }
        val other = obj as Swagger?
        if (basePath == null) {
            if (other!!.basePath != null) {
                return false
            }
        } else if (basePath != other!!.basePath) {
            return false
        }
        if (consumes == null) {
            if (other.consumes != null) {
                return false
            }
        } else if (consumes != other.consumes) {
            return false
        }
        if (definitions == null) {
            if (other.definitions != null) {
                return false
            }
        } else if (definitions != other.definitions) {
            return false
        }
        if (externalDocs == null) {
            if (other.externalDocs != null) {
                return false
            }
        } else if (externalDocs != other.externalDocs) {
            return false
        }
        if (host == null) {
            if (other.host != null) {
                return false
            }
        } else if (host != other.host) {
            return false
        }
        if (info == null) {
            if (other.info != null) {
                return false
            }
        } else if (info != other.info) {
            return false
        }
        if (parameters == null) {
            if (other.parameters != null) {
                return false
            }
        } else if (parameters != other.parameters) {
            return false
        }
        if (paths == null) {
            if (other.paths != null) {
                return false
            }
        } else if (paths != other.paths) {
            return false
        }
        if (produces == null) {
            if (other.produces != null) {
                return false
            }
        } else if (produces != other.produces) {
            return false
        }
        if (responses == null) {
            if (other.responses != null) {
                return false
            }
        } else if (responses != other.responses) {
            return false
        }
        if (schemes == null) {
            if (other.schemes != null) {
                return false
            }
        } else if (schemes != other.schemes) {
            return false
        }
        if (security == null) {
            if (other.security != null) {
                return false
            }
        } else if (security != other.security) {
            return false
        }
        if (securityDefinitions == null) {
            if (other.securityDefinitions != null) {
                return false
            }
        } else if (securityDefinitions != other.securityDefinitions) {
            return false
        }
        if (swagger == null) {
            if (other.swagger != null) {
                return false
            }
        } else if (swagger != other.swagger) {
            return false
        }
        if (tags == null) {
            if (other.tags != null) {
                return false
            }
        } else if (tags != other.tags) {
            return false
        }
        if (vendorExtensions == null) {
            if (other.vendorExtensions != null) {
                return false
            }
        } else if (vendorExtensions != other.vendorExtensions) {
            return false
        }
        return true
    }

    fun vendorExtensions(vendorExtensions: Map<String, Any>?): Swagger {
        if (vendorExtensions == null) {
            return this
        }

        if (this.vendorExtensions == null) {
            this.vendorExtensions = LinkedHashMap()
        }

        this.vendorExtensions!!.putAll(vendorExtensions)
        return this
    }

    fun setVendorExtensions(vendorExtensions: MutableMap<String, Any>) {
        this.vendorExtensions = vendorExtensions
    }

    companion object {

        @JsonIgnore
        private val LOGGER = LoggerFactory.getLogger(Swagger::class.java)
    }
}
