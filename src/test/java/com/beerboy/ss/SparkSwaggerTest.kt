package com.beerboy.ss

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.beerboy.ss.descriptor.EndpointDescriptor
import com.beerboy.ss.descriptor.MethodDescriptor
import com.beerboy.ss.descriptor.ParameterDescriptor
import com.beerboy.ss.factory.DefinitionsFactoryTest
import com.beerboy.ss.factory.Description
import org.slf4j.LoggerFactory
import java.util.*

object SparkSwaggerTest {

    @JvmStatic
    fun main(args: Array<String>) {

        try {
            val service = spark.Service.ignite().port(3000)

            val logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
            logger.level = Level.INFO

            val swagger = SparkSwagger.of(service, Config(
                    description = "A test",
                    title = "Test",
                    host = "localhost:3000",
                    basePath = "",
                    serviceName = "barstool",
                    theme = Theme.MATERIAL,
                    defaultModelExpandDepth = 10,
                    logLevel = Level.INFO,
                    docPath = "/doc"
            ))

            swagger
                    .endpoint(EndpointDescriptor.endpointPath("/hello")) { _, _ -> }
                    .post(MethodDescriptor.path("/there")
                            .withQueryParam(ParameterDescriptor().apply {
                                name = "pathname"
                                description = "the description"
                            })
                            .withHeaderParam(ParameterDescriptor().apply {
                                name = "headername"
                                description = "the description"
                            })
                            .withRequestType(MyFoo::class)
                            .withResponseType(DefinitionsFactoryTest.ClassWithCollection::class)) { a, b -> "response" }

            service.get("/foo") { _, _ -> "hey" }
            swagger.generateDoc()
        } catch (e: Throwable) {
            e.printStackTrace()
        }

    }
}

sealed class Foo {
    data class Bar(val x: String): Foo()
    data class Yo(val y: Int): Foo()
}

data class MyFoo(
        val foo: Foo,
        val x: DefinitionsFactoryTest.SimpleClass,
        val afloat: Float,
        val abool: Boolean,
        val aDate: Date,
        val classWithList: DefinitionsFactoryTest.ClassWithCollection,
        @Description("The user") val uuid: UUID,
        @Description("The user") val long: Long,
        @Description("The user") val users: String,
        @Description("the count") val count: Int?)

