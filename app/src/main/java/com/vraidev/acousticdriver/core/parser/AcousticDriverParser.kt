package com.vraidev.acousticdriver.core.parser

import com.vraidev.acousticdriver.core.air.AcousticIntermediateRepresentation

interface AcousticDriverParser {
    fun canParse(content: String): Boolean
    fun parse(content: String): AcousticIntermediateRepresentation
}
