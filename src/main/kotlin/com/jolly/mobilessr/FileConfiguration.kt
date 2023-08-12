package com.jolly.mobilessr

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.MessageChannels
import org.springframework.integration.dsl.integrationFlow
import org.springframework.integration.file.dsl.Files
import org.springframework.messaging.MessagingException
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

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
class FileConfiguration(private val channels: ChannelConfiguration, private val customCacheManager: CustomCacheManager) {
    private val input = File("${System.getenv("HOME")}/Desktop/in")
    private val output = File("${System.getenv("HOME")}/Desktop/out")
    private val mapper = jacksonObjectMapper()
    val fileVer: AtomicInteger = AtomicInteger()

    @Bean
    fun filesFlow(): IntegrationFlow = integrationFlow(
        Files.inboundAdapter(this.input).autoCreateDirectory(true),
        { poller { it.fixedDelay(500).maxMessagesPerPoll(1) } }
    ) {

        filter<File> { it.isFile && checkVersion(it) }
        route<File> {
            when (it.extension.lowercase()) {
                "json" -> channels.assets()
                else -> channels.errors()
            }
        }
    }

    @Bean
    fun assetsFlow(): IntegrationFlow = integrationFlow(channels.assets()) {
        handle { message ->
            val render: Render? = mapper.readValue(message.payload as String)
            render?.let { r ->
                r.appId.let { appId ->
                    customCacheManager.apply {
                        colorSchemeMap[appId] = ColorScheme(r.colorSchemes.primary, r.colorSchemes.secondary)
                        assetMap[appId] = Asset(r.assets.logo)
                        titleMap[appId] = Title(r.titles.main, r.titles.subtitle)
                    }
                }
            }
        }
    }

    @Bean
    fun errorFlow(): IntegrationFlow = integrationFlow(channels.errors()) {
        transform { it: MessagingException -> it.message + "\n" }
        handle(Files.outboundAdapter(output).autoCreateDirectory(true))
    }

    fun checkVersion(file: File): Boolean {
        val versionRegex = """.*__v(\d+)\.json""".toRegex()
        val fileName = file.name
        val matchResult = versionRegex.matchEntire(fileName)
        if (matchResult != null) {
            val version = matchResult.groupValues[1].toInt()
            return fileVer.compareAndSetIfGreaterThan(version)
        }
        return false
    }
}

data class Render(
    val appId: String,
    val titles: Title,
    val colorSchemes: ColorScheme,
    val assets: Asset
)

fun AtomicInteger.compareAndSetIfGreaterThan(newValue: Int): Boolean {
    do {
        val oldValue = get()
        if (newValue <= oldValue) {
            return false
        }
    } while (!compareAndSet(oldValue, newValue))
    return true
}
