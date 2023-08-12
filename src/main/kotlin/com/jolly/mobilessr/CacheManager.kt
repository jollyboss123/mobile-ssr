package com.jolly.mobilessr

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * @author jolly
 */
@Component
class CacheManager {
    var colorSchemeMap: ConcurrentHashMap<String, ColorScheme> = ConcurrentHashMap<String, ColorScheme>()
    var titleMap: ConcurrentHashMap<String, Title> = ConcurrentHashMap<String, Title>()
    var assetMap: ConcurrentHashMap<String, Asset> = ConcurrentHashMap<String, Asset>()
}
