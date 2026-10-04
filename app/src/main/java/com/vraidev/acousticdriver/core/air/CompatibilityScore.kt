package com.vraidev.acousticdriver.core.air

data class ParameterEvaluation(
    val parameterName: String,
    val sourceValue: String,
    val targetEffect: String,
    val status: ParameterClassification,
    val explanation: String
)

data class CompatibilityScore(
    val percentage: Int, // 0 - 100
    val evaluations: List<ParameterEvaluation>,
    val nativeCount: Int,
    val approximatedCount: Int,
    val unsupportedCount: Int
)
