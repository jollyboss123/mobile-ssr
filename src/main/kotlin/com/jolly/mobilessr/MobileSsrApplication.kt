package com.jolly.mobilessr

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.MessageChannels
import org.springframework.integration.dsl.integrationFlow
import org.springframework.integration.file.dsl.Files
import org.springframework.messaging.MessageChannel
import java.io.File

@SpringBootApplication
class MobileSsrApplication

fun main(args: Array<String>) {
    runApplication<MobileSsrApplication>(*args)
}


