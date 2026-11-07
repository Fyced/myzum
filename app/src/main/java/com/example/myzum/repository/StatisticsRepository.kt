package com.example.myzum.repository

import com.example.myzum.model.statistics.*
import com.example.myzum.repository.local.StatisticsDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.*

class StatisticsRepository(private val dao: StatisticsDao) {

    fun getStatistics(userId: String, start: Date, end: Date, previousStart: Date, previousEnd: Date): Flow<BusinessStatistics> {
        val currentPaymentsFlow = dao.getApprovedPaymentsInRange(userId, start, end)
        val previousPaymentsFlow = dao.getApprovedPaymentsInRange(userId, previousStart, previousEnd)

        return combine(currentPaymentsFlow, previousPaymentsFlow) { current, previous ->
            val grossSales = current.filter { it.amount > 0 }.sumOf { it.amount }
            val totalRefunded = current.filter { it.amount < 0 }.sumOf { kotlin.math.abs(it.amount) }
            val netSales = grossSales - totalRefunded
            val transactionCount = current.count { it.amount > 0 }
            
            val previousGrossSales = previous.filter { it.amount > 0 }.sumOf { it.amount }
            val previousRefunded = previous.filter { it.amount < 0 }.sumOf { kotlin.math.abs(it.amount) }
            val previousNetSales = previousGrossSales - previousRefunded
            
            BusinessStatistics(
                summary = StatisticsSummary(
                    totalSales = grossSales,
                    transactionCount = transactionCount,
                    averageTicket = if (transactionCount > 0) netSales / transactionCount else null,
                    totalRefunded = totalRefunded
                ),
                evolution = groupPaymentsByDate(current, start, end),
                paymentMethods = current.filter { it.amount > 0 }
                    .groupBy { it.paymentMethod }
                    .map { (method, items) ->
                        val methodTotal = items.sumOf { it.amount }
                        PaymentMethodStats(
                            method = method,
                            count = items.size,
                            totalAmount = methodTotal,
                            percentage = if (grossSales > 0) (methodTotal / grossSales).toFloat() else 0f
                        )
                    },
                comparison = ComparisonResult(
                    previousPeriodSales = previousNetSales,
                    percentageVariation = if (previousNetSales > 0) ((netSales - previousNetSales) / previousNetSales) * 100 else null
                )
            )
        }
    }

    private fun groupPaymentsByDate(payments: List<com.example.myzum.repository.local.PaymentEntity>, startDate: Date, endDate: Date): List<SalesPoint> {
        val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
        val todayStr = sdf.format(Date())
        
        // Group and sum net amounts (positives - negatives)
        val grouped = payments.groupBy { sdf.format(it.date) }
            .mapValues { it.value.sumOf { p -> p.amount } }
            
        val result = mutableListOf<SalesPoint>()
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        val endCal = Calendar.getInstance()
        endCal.time = endDate
        endCal.set(Calendar.HOUR_OF_DAY, 0)
        endCal.set(Calendar.MINUTE, 0)
        endCal.set(Calendar.SECOND, 0)
        endCal.set(Calendar.MILLISECOND, 0)
        
        while (!calendar.after(endCal)) {
            val dateLabel = sdf.format(calendar.time)
            val label = if (dateLabel == todayStr) "Today" else dateLabel
            result.add(SalesPoint(label, grouped[dateLabel] ?: 0.0))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            
            if (result.size > 40) break
        }
        
        return result
    }
}
