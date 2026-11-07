package com.example.myzum.model.statistics

enum class InsightPriority {
    HIGH, MEDIUM, LOW
}

data class BusinessInsight(
    val message: String,
    val priority: InsightPriority,
    val icon: String // Identifier for the icon to display
)
