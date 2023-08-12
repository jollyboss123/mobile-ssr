package com.jolly.mobilessr

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.konform.validation.Invalid
import io.konform.validation.Validation
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

/**
 * @author jolly
 */
@RestController
@RequestMapping
class MainController {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @PostMapping
    suspend fun getFlavor(@RequestBody request: FlavorRequest): FlavorResponse {
        logger.info("get flavor start")
        try {
            val req = request.validate()

            return supervisorScope {
                try {
                    val dm = getDataModel(req, this)
                    getResponse(dm)
                } finally {
                    logger.info("get flavor end")
                    this.coroutineContext.cancelChildren()
                }
            }
        } catch (e: Throwable) {
            logger.info("get flavor error")
            throw getErrorStatus(e)
        }
    }

    suspend fun getDataModel(request: FlavorRequest, coroutineScope: CoroutineScope): AppFlavorDataModel {
        return coroutineScope.run {
            val colorSchemes = async(start = CoroutineStart.LAZY) {
                Result.runCatching {
                    //TODO: fetch color schemes from cache
                }
            }
            val titles = async(start = CoroutineStart.LAZY) {
                Result.runCatching {
                    //TODO: fetch titles from cache
                }
            }
            val assets = async(start = CoroutineStart.LAZY) {
                Result.runCatching {
                    //TODO: fetch assets from cache
                }
            }
            AppFlavorDataModel(colorSchemes, titles, assets)
        }
    }

    suspend fun getResponse(dataModel: AppFlavorDataModel): FlavorResponse {
        var colorSchemes: ColorScheme? = null
        var titles: Title? = null
        var assets : Asset? = null
        coroutineScope {
            launch {
                colorSchemes = dataModel.colorSchemes.awaitResult().getOrNull()
            }
            launch {
                titles = dataModel.titles.awaitResult().getOrNull()
            }
            launch {
                assets = dataModel.assets.awaitResult().getOrNull()
            }
        }
        return FlavorResponse(colorSchemes, titles, assets)
    }

    fun getErrorStatus(e: Throwable): ResponseStatusException {
        return when(e) {
            is IllegalArgumentException -> {
                ResponseStatusException(HttpStatus.BAD_REQUEST, e.message, e)
            }
            else -> {
                ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
            }
        }
    }
}

data class FlavorRequest @JsonCreator constructor(
    @JsonProperty("appId") val appId: String
) {
    fun validate(): FlavorRequest {
        Validation {
            FlavorRequest::appId required {}
        }.validateAndThrowOnFailure(this)
        return this
    }
}

data class FlavorResponse(
    val colorSchemes: ColorScheme?,
    val titles: Title?,
    val assets: Asset?
)

data class AppFlavorDataModel(
    val colorSchemes: Deferred<Result<ColorScheme>>,
    val titles: Deferred<Result<Title>>,
    val assets: Deferred<Result<Asset>>
)

data class ColorScheme(
    val primary: String?,
    val secondary: String?
)

data class Title(
    val main: String?,
    val subtitle: String?
)

data class Asset(
    val logo: String?
)

suspend fun <T> Deferred<Result<T>>.awaitResult(): Result<T> {
    return try {
        this.await()
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

fun <T> Validation<T>.validateAndThrowOnFailure(value: T) {
    val result = validate(value)
    if (result is Invalid<T>) {
        throw IllegalArgumentException(result.errors.toString())
    }
}
