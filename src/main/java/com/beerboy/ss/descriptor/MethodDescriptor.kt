package com.beerboy.ss.descriptor

import com.beerboy.ss.model.ExternalDocs
import com.beerboy.ss.model.HttpMethod
import com.beerboy.ss.model.Response
import com.beerboy.ss.rest.RestResponse
import java.util.*
import kotlin.reflect.KClass

/**
 * @author manusant
 */
class MethodDescriptor {

    var method: HttpMethod? = null
    var path: String? = null
    var description: String? = null
    var requestType: KClass<*>? = null
    var isRequestAsCollection: Boolean = false
    var responseType: KClass<*>? = null
    var isResponseAsCollection: Boolean = false
    var operationId: String? = null
    var consumes: List<String>? = null
    var produces: List<String>? = null
    var parameters: List<ParameterDescriptor> = ArrayList()
    var responses: Map<String, Response>? = null
    var externalDocs: ExternalDocs? = null
    var deprecated: Boolean? = null

    class Builder {
        private var method: HttpMethod? = null
        private var path: String? = null
        private var description: String? = null
        private var requestType: KClass<*>? = null
        private var requestAsCollection: Boolean = false
        private var responseType: KClass<*>? = null
        private var responseAsCollection: Boolean = false
        private var operationId: String? = null
        private var consumes: List<String>? = null
        private var produces: List<String>? = null
        private var parameters: MutableList<ParameterDescriptor> = ArrayList()
        private var responses: Map<String, Response>? = null
        private var externalDocs: ExternalDocs? = null
        private var deprecated: Boolean? = null

        fun withMethod(method: HttpMethod): Builder {
            this.method = method
            return this
        }

        fun withPath(path: String): Builder {
            this.path = path
            return this
        }

        fun withDescription(description: String): Builder {
            this.description = description
            return this
        }

        fun withRequestType(requestType: KClass<*>): Builder {
            this.requestType = requestType
            return this
        }

        fun withRequestAsCollection(itemType: KClass<*>): Builder {
            this.requestAsCollection = true
            this.requestType = itemType
            return this
        }

        fun withResponseType(responseType: KClass<*>): Builder {
            this.responseType = responseType
            return this
        }

        fun withGenericResponse(): Builder {
            this.responseType = RestResponse::class
            return this
        }

        fun withResponseAsCollection(itemType: KClass<*>): Builder {
            this.responseAsCollection = true
            this.responseType = itemType
            return this
        }

        fun withOperationId(operationId: String): Builder {
            this.operationId = operationId
            return this
        }

        fun withConsumes(consumes: List<String>): Builder {
            this.consumes = consumes
            return this
        }

        fun withProduces(produces: List<String>): Builder {
            this.produces = produces
            return this
        }

        fun withParams(parameters: MutableList<ParameterDescriptor>): Builder {
            this.parameters = parameters
            return this
        }

        fun withParam(parameter: ParameterDescriptor): Builder {
            this.parameters.add(parameter)
            return this
        }

        fun withPathParam(): ParameterDescriptor.Builder {
            return ParameterDescriptor.newBuilder(this).withType(ParameterDescriptor.ParameterType.PATH)
        }

        fun withPathParam(param: ParameterDescriptor): Builder {
            param.type = ParameterDescriptor.ParameterType.PATH
            this.parameters.add(param)
            return this
        }

        fun withQueryParam(): ParameterDescriptor.Builder {
            return ParameterDescriptor.newBuilder(this).withType(ParameterDescriptor.ParameterType.QUERY)
        }

        fun withQueryParam(param: ParameterDescriptor): Builder {
            param.type = ParameterDescriptor.ParameterType.QUERY
            this.parameters.add(param)
            return this
        }

        fun withHeaderParam(): ParameterDescriptor.Builder {
            return ParameterDescriptor.newBuilder(this).withType(ParameterDescriptor.ParameterType.HEADER)
        }

        fun withHeaderParam(param: ParameterDescriptor): Builder {
            param.type = ParameterDescriptor.ParameterType.HEADER
            this.parameters.add(param)
            return this
        }

        fun withResponses(responses: Map<String, Response>): Builder {
            this.responses = responses
            return this
        }

        fun withExternalDocs(externalDocs: ExternalDocs): Builder {
            this.externalDocs = externalDocs
            return this
        }

        fun withDeprecated(deprecated: Boolean?): Builder {
            this.deprecated = deprecated
            return this
        }

        fun build(): MethodDescriptor {
            val methodDescriptor = MethodDescriptor()
            methodDescriptor.method = method
            methodDescriptor.path = path
            methodDescriptor.description = description
            methodDescriptor.requestType = requestType
            methodDescriptor.isRequestAsCollection = requestAsCollection
            methodDescriptor.responseType = responseType
            methodDescriptor.isResponseAsCollection = responseAsCollection
            methodDescriptor.operationId = operationId
            methodDescriptor.consumes = consumes
            methodDescriptor.produces = produces
            methodDescriptor.parameters = parameters
            methodDescriptor.responses = responses
            methodDescriptor.externalDocs = externalDocs
            methodDescriptor.deprecated = deprecated
            return methodDescriptor
        }

        companion object {

            fun newBuilder(): Builder {
                return Builder()
            }
        }
    }

    companion object {

        fun path(path: String): Builder {
            return Builder().withPath(path)
        }
    }
}
