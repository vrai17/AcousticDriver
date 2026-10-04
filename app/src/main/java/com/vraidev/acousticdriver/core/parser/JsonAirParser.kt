package com.vraidev.acousticdriver.core.parser

import com.vraidev.acousticdriver.core.air.AcousticIntermediateRepresentation
import kotlinx.serialization.json.Json

class JsonAirParser : AcousticDriverParser {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }

    override fun canParse(content: String): Boolean {
        val trimmed = content.trim()
        return (trimmed.startsWith("{") && trimmed.endsWith("}")) && trimmed.contains("\"metadata\"")
    }

    override fun parse(content: String): AcousticIntermediateRepresentation {
        return json.decodeFromString(content)
    }

    fun serialize(air: AcousticIntermediateRepresentation): String {
        return json.encodeToString(AcousticIntermediateRepresentation.serializer(), air)
    }
}
