package com.example.myzum.ui.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myzum.model.statistics.*
import com.example.myzum.util.CurrencyUtils
import com.example.myzum.viewmodel.StatisticsUiState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessStatisticsScreen(
    uiState: StatisticsUiState,
    selectedPeriod: StatisticsPeriod,
    onPeriodSelected: (StatisticsPeriod) -> Unit,
    onBackClick: () -> Unit
) {
    val periods = StatisticsPeriod.entries
    val pagerState = rememberPagerState(
        initialPage = periods.indexOf(selectedPeriod),
        pageCount = { periods.size }
    )

    // Sync pager with external selection (and vice versa)
    LaunchedEffect(pagerState.currentPage) {
        onPeriodSelected(periods[pagerState.currentPage])
    }

    LaunchedEffect(selectedPeriod) {
        val targetPage = periods.indexOf(selectedPeriod)
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Business Insights") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Period Selector (Static indicator)
            PeriodSelector(selectedPeriod, onPeriodSelected)

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top
            ) { page ->
                val currentPeriodUiState = if (periods[page] == selectedPeriod) uiState else StatisticsUiState.Loading
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    when (currentPeriodUiState) {
                        is StatisticsUiState.Loading -> {
                            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        is StatisticsUiState.Error -> {
                            Text(text = currentPeriodUiState.message, color = MaterialTheme.colorScheme.error)
                        }
                        is StatisticsUiState.Success -> {
                            val stats = currentPeriodUiState.stats
                            
                            SalesSummary(stats.summary, stats.comparison)

                            if (periods[page] != StatisticsPeriod.TODAY) {
                                Spacer(modifier = Modifier.height(32.dp))
                                EvolutionChart(stats.evolution)
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            PaymentMethodsBreakdown(stats.paymentMethods)

                            Spacer(modifier = Modifier.height(32.dp))

                            InsightsList(currentPeriodUiState.insights)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PeriodSelector(selected: StatisticsPeriod, onSelected: (StatisticsPeriod) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatisticsPeriod.entries.forEach { period ->
            val isSelected = selected == period
            val label = when (period) {
                StatisticsPeriod.TODAY -> "Today"
                StatisticsPeriod.LAST_7_DAYS -> "7 Days"
                StatisticsPeriod.LAST_30_DAYS -> "30 Days"
            }
            
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { onSelected(period) },
                color = Color.Transparent
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
fun SalesSummary(summary: StatisticsSummary, comparison: ComparisonResult) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Gross Sales", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                Text(
                    text = CurrencyUtils.formatAmount(summary.totalSales),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            if (summary.totalRefunded > 0) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Refunds", fontSize = 14.sp, color = Color(0xFFFF3B30).copy(alpha = 0.7f))
                    Text(
                        text = String.format(Locale.getDefault(), "-%s", CurrencyUtils.formatAmount(summary.totalRefunded)),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFF3B30)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Net Sales", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = CurrencyUtils.formatAmount(summary.netSales),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(12.dp))
            comparison.percentageVariation?.let { variation ->
                val isUp = variation >= 0
                Text(
                    text = "${if (isUp) "↑" else "↓"} ${kotlin.math.abs(variation).toInt()}%",
                    color = if (isUp) Color(0xFF34C759) else Color(0xFFFF3B30),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
        Text(
            text = "${summary.transactionCount} operations",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Average Ticket", fontSize = 16.sp, color = MaterialTheme.colorScheme.secondary)
        Text(
            text = summary.averageTicket?.let { CurrencyUtils.formatAmount(it) } ?: "—",
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun EvolutionChart(points: List<SalesPoint>) {
    var selectedPoint by remember { mutableStateOf<SalesPoint?>(null) }
    val chartScrollState = rememberScrollState()
    
    // Auto-scroll to the end (most recent day) when data loads
    LaunchedEffect(points) {
        chartScrollState.scrollTo(chartScrollState.maxValue)
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "EVOLUTION",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            
            selectedPoint?.let {
                Text(
                    text = CurrencyUtils.formatAmount(it.sales),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (points.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                Text("Not enough data yet", color = MaterialTheme.colorScheme.secondary)
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (points.size > 7) Modifier.horizontalScroll(chartScrollState) 
                            else Modifier.padding(horizontal = 8.dp) // Add padding so edge labels aren't cut
                        ),
                    horizontalArrangement = if (points.size > 7) Arrangement.spacedBy(12.dp) else Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val maxSales = points.maxOf { it.sales }.coerceAtLeast(1.0)
                    
                    points.forEach { point ->
                        val heightFactor = (point.sales / maxSales).toFloat().coerceIn(0.05f, 1f)
                        val isSelected = selectedPoint == point
                        
                        Column(
                            modifier = Modifier
                                .then(if (points.size > 7) Modifier.width(36.dp) else Modifier.weight(1f))
                                .fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(if (points.size > 7) 1f else 0.6f) // Slightly thinner bars for 7-day view
                                    .fillMaxHeight(heightFactor)
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary 
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                    )
                                    .clickable { selectedPoint = if (isSelected) null else point }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = point.dateLabel,
                                fontSize = 9.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentMethodsBreakdown(methods: List<PaymentMethodStats>) {
    Column {
        Text(
            text = "PAYMENT METHODS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        methods.forEach { method ->
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = method.method, fontWeight = FontWeight.Medium)
                    Text(text = "${(method.percentage * 100).toInt()}%", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { method.percentage },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            }
        }
    }
}

@Composable
fun InsightsList(insights: List<BusinessInsight>) {
    Column {
        Text(
            text = "INSIGHTS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (insights.isEmpty()) {
            Text(
                text = "Keep collecting payments to get business insights.",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 14.sp
            )
        } else {
            insights.forEach { insight ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    val icon = when (insight.icon) {
                        "trending_up" -> Icons.Default.TrendingUp
                        "trending_down" -> Icons.Default.TrendingDown
                        "star" -> Icons.Default.Star
                        "event" -> Icons.Default.Event
                        "payments" -> Icons.Default.Payments
                        else -> Icons.Default.Info
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = when (insight.priority) {
                            InsightPriority.HIGH -> Color(0xFFFF3B30)
                            InsightPriority.MEDIUM -> MaterialTheme.colorScheme.primary
                            InsightPriority.LOW -> Color(0xFF34C759)
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = insight.message,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BusinessStatisticsScreenPreview() {
    BusinessStatisticsScreen(
        uiState = StatisticsUiState.Success(
            stats = BusinessStatistics(
                summary = StatisticsSummary(8420.50, 342, 24.62),
                evolution = listOf(
                    SalesPoint("01 Aug", 420.0),
                    SalesPoint("02 Aug", 510.0),
                    SalesPoint("03 Aug", 380.0),
                    SalesPoint("04 Aug", 620.0),
                    SalesPoint("05 Aug", 780.0)
                ),
                paymentMethods = listOf(
                    PaymentMethodStats("NFC", 246, 6320.0, 0.72f),
                    PaymentMethodStats("Myzum Pay", 96, 2100.0, 0.28f)
                ),
                comparison = ComparisonResult(7640.0, 10.2)
            ),
            insights = listOf(
                BusinessInsight("Your sales have increased by 10%.", InsightPriority.MEDIUM, "trending_up"),
                BusinessInsight("Saturday was your strongest day.", InsightPriority.LOW, "event")
            )
        ),
        selectedPeriod = StatisticsPeriod.LAST_7_DAYS,
        onPeriodSelected = {},
        onBackClick = {}
    )
}
