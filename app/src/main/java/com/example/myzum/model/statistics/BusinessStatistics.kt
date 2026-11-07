package com.example.myzum.model.statistics

data class BusinessStatistics(
    val summary: StatisticsSummary,
    val evolution: List<SalesPoint>,
    val paymentMethods: List<PaymentMethodStats>,
    val comparison: ComparisonResult
)

data class StatisticsSummary(
    val totalSales: Double,
    val transactionCount: Int,
    val averageTicket: Double?,
    val totalRefunded: Double = 0.0,
    val netSales: Double = totalSales - totalRefunded
)

data class SalesPoint(
    val dateLabel: String,
    val sales: Double
)

data class PaymentMethodStats(
    val method: String,
    val count: Int,
    val totalAmount: Double,
    val percentage: Float
)

data class ComparisonResult(
    val previousPeriodSales: Double,
    val percentageVariation: Double? // null if no previous data
)
