package com.vraidev.acousticdriver.core.air

enum class ParameterClassification(val label: String, val weight: Float) {
    EXACT("Exact Match", 1.0f),
    NATIVE("Native DSP", 1.0f),
    APPROXIMATED("Approximated", 0.75f),
    PARTIAL("Partial Support", 0.5f),
    UNSUPPORTED("Unsupported", 0.0f),
    UNKNOWN("Unknown Parameter", 0.1f)
}
