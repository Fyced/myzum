package com.example.myzum.statistics

import com.example.myzum.model.statistics.BusinessInsight
import com.example.myzum.model.statistics.BusinessStatistics
import com.example.myzum.model.statistics.InsightPriority
import com.example.myzum.util.CurrencyUtils

object BusinessInsightEngine {

    fun generateInsights(stats: BusinessStatistics): List<BusinessInsight> {
        val insights = mutableListOf<BusinessInsight>()

        // Growth Insight
        stats.comparison.percentageVariation?.let { variation ->
            if (variation > 10) {
                insights.add(BusinessInsight(
                    message = "Your sales have increased by ${variation.toInt()}% compared to the previous period.",
                    priority = InsightPriority.MEDIUM,
                    icon = "trending_up"
                ))
            } else if (variation < -20) {
                insights.add(BusinessInsight(
                    message = "Warning: Sales have decreased by ${variation.toInt().coerceAtMost(-1)}%.",
                    priority = InsightPriority.HIGH,
                    icon = "trending_down"
                ))
            }
        }

        // Average Ticket Insight
        stats.summary.averageTicket?.let { ticket ->
            if (ticket > 50) {
                insights.add(BusinessInsight(
                    message = "You have a high average ticket (${CurrencyUtils.formatAmount(ticket)}).",
                    priority = InsightPriority.LOW,
                    icon = "star"
                ))
            }
        }

        // Best Day Insight
        if (stats.evolution.isNotEmpty()) {
            val bestDay = stats.evolution.maxByOrNull { it.sales }
            bestDay?.let {
                insights.add(BusinessInsight(
                    message = "Your strongest day was ${it.dateLabel} with ${CurrencyUtils.formatAmount(it.sales)} in sales.",
                    priority = InsightPriority.LOW,
                    icon = "event"
                ))
            }
        }

        // Payment Method Insight
        stats.paymentMethods.maxByOrNull { it.percentage }?.let { dominant ->
            if (dominant.percentage > 0.7f) {
                insights.add(BusinessInsight(
                    message = "${(dominant.percentage * 100).toInt()}% of your operations are via ${dominant.method}.",
                    priority = InsightPriority.LOW,
                    icon = "payments"
                ))
            }
        }

        return insights.sortedBy { it.priority }.take(3)
    }
}
