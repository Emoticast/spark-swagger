package com.beerboy.ss

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.beerboy.ss.descriptor.EndpointDescriptor
import com.beerboy.ss.descriptor.MethodDescriptor
import org.slf4j.LoggerFactory

object SparkSwaggerTest {

    @JvmStatic
    fun main(args: Array<String>) {

        try {
            val service = spark.Service.ignite().port(3000)

            val logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
            logger.level = Level.INFO

            val swagger = SparkSwagger.of(service, "conf/" + SparkSwagger.CONF_FILE_NAME)

            swagger
                    .endpoint(EndpointDescriptor.endpointPath("/hello")) { a, b -> }
                    .get(MethodDescriptor.path("/there")
//                            .withRequestType(MyFoo::class)
                            .withResponseType(MyFoo::class)) { a, b -> "response" }

            service.get("/foo") { a, b -> "hey" }
            swagger.generateDoc()
        } catch (e: Throwable) {
            e.printStackTrace()
        }

    }
}

data class MyFoo(val user: String, val count: Int?)

