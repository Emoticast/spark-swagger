package com.beerboy.ss

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.beerboy.ss.descriptor.EndpointDescriptor
import com.beerboy.ss.descriptor.MethodDescriptor
import com.beerboy.ss.descriptor.ParameterDescriptor
import com.beerboy.ss.factory.DefinitionsFactoryTest
import org.slf4j.LoggerFactory

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
                    basePath = "/test",
                    theme = Theme.MATERIAL,
                    defaultModelExpandDepth = 10,
                    docPath = "/doc"
            ))

            swagger
                    .endpoint(EndpointDescriptor.endpointPath("/hello")) { _, _ -> }
                    .get(MethodDescriptor.path("/there")
                            .withQueryParam(ParameterDescriptor().apply {
                                name = "pathname"
                                description = "the description"
                            })
                            .withHeaderParam(ParameterDescriptor().apply {
                                name = "headername"
                                description = "the description"
                            })
                            .withResponseType(DefinitionsFactoryTest.ClassWithCollection::class)) { a, b -> "response" }

            service.get("/foo") { _, _ -> "hey" }
            swagger.generateDoc()
        } catch (e: Throwable) {
            e.printStackTrace()
        }

    }
}

data class MyFoo(val user: String, val count: Int?)

