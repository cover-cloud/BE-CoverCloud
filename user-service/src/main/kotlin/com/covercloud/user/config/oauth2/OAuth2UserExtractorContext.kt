package com.covercloud.user.config.oauth2

import org.springframework.stereotype.Component

@Component
class OAuth2UserExtractorContext(
    private val extractors: List<OAuth2UserExtractor>
) {
    fun extract(attributes: Map<*, *>): OAuth2UserInfo {
        val extractor = extractors.find { it.supports(attributes) }
            ?: throw IllegalArgumentException("Unsupported OAuth2 provider")
        return extractor.extract(attributes)
    }
}

