package com.jolly.mobilessr

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.MessageChannels
import org.springframework.integration.dsl.integrationFlow
import org.springframework.integration.file.dsl.Files
import java.io.File

/**
 * @author jolly
 */
@Configuration
class ChannelConfiguration {
    @Bean
    fun assets() = MessageChannels.direct().`object`

    @Bean
    fun errors() = MessageChannels.direct().`object`
}

@Configuration
class FileConfiguration(private val channels: ChannelConfiguration) {
    private val input = File("${System.getenv("HOME")}/Desktop/in")
    private val output = File("${System.getenv("HOME")}/Desktop/out")

    @Bean
    fun filesFlow(): IntegrationFlow = integrationFlow(
        Files.inboundAdapter(this.input).autoCreateDirectory(true),
        { poller { it.fixedDelay(500).maxMessagesPerPoll(1) } }
    ) {

        filter<File> { it.isFile } //TODO: check version here
        route<File> {
            when (it.extension.lowercase()) {
                "json" -> channels.assets()
                else -> channels.errors()
            }
        }
    }

    @Bean
    fun assetsFlow(): IntegrationFlow = integrationFlow(channels.assets()) {
        //TODO: split json & save to according cache
    }

    //TODO: log error to output for user to check
//    @Bean
//    fun errorFlow(): IntegrationFlow = integrationFlow(channels.errors()) {
//        handle(Files.outboundAdapter(output).autoCreateDirectory(true))
//    }
}
