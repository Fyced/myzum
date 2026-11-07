package com.example.myzum.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myzum.R
import com.example.myzum.model.FundingSource
import com.example.myzum.model.FundingSourceType
import com.example.myzum.model.Payment
import com.example.myzum.util.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    userType: com.example.myzum.model.AccountType,
    fundingSources: List<FundingSource>,
    payments: List<Payment>,
    initialPageIndex: Int = 0,
    onDataphoneClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    onCatalogClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAddFundingSourceClick: () -> Unit,
    onFundingSourceSelected: (String?, Int) -> Unit,
    onRemoveFundingSource: (String) -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = initialPageIndex,
        pageCount = { fundingSources.size + 1 }
    )

    var showDeleteDialog by remember { mutableStateOf(false) }
    var sourceToDelete by remember { mutableStateOf<FundingSource?>(null) }

    if (showDeleteDialog && sourceToDelete != null) {
        Dialog(onDismissRequest = { showDeleteDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.85f) // Slightly narrower to feel more compact
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp), // Slightly tighter radius for smaller dialog
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(top = 24.dp, start = 20.dp, end = 20.dp, bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Remove Card?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "This will remove '${sourceToDelete!!.label}' from your settlement methods.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.secondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Buttons in a more compact vertical stack with dividers (Apple alert style)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    
                    TextButton(
                        onClick = {
                            onRemoveFundingSource(sourceToDelete!!.id)
                            showDeleteDialog = false
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF3B30))
                    ) {
                        Text("Remove", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    TextButton(
                        onClick = { showDeleteDialog = false },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(
                            "Cancel",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Simple minimalist "logo"
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "M",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.app_name),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Terminal Buttons
            val isAddCardSelected = pagerState.currentPage == fundingSources.size
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onDataphoneClick,
                    enabled = !isAddCardSelected,
                    modifier = Modifier
                        .weight(if (userType == com.example.myzum.model.AccountType.MERCHANT) 0.8f else 1f)
                        .height(56.dp),
                    shape = CircleShape,
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(text = stringResource(R.string.action_dataphone), fontWeight = FontWeight.SemiBold)
                }

                if (userType == com.example.myzum.model.AccountType.MERCHANT) {
                    Surface(
                        onClick = onCatalogClick,
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Catalog",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Surface(
                        onClick = onStatisticsClick,
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = "Statistics",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Mini History Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp) // Increased height to comfortably fit 2 items + header
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .clickable(onClick = onHistoryClick)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Activity",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "See All",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (payments.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No recent transactions",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                } else {
                    // Limit to maximum 2 items as requested
                    val recentPayments = payments.take(2)
                    recentPayments.forEachIndexed { index, payment ->
                        MiniPaymentItem(payment = payment)
                        if (index < recentPayments.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Funding Sources Carousel
            Text(
                text = "Settlement Method",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            )

            LaunchedEffect(pagerState.currentPage, fundingSources) {
                if (fundingSources.isNotEmpty()) {
                    val selectedId = if (pagerState.currentPage < fundingSources.size) {
                        fundingSources[pagerState.currentPage].id
                    } else {
                        null // "Add Card" selected
                    }
                    onFundingSourceSelected(selectedId, pagerState.currentPage)
                }
            }
            
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 24.dp),
                pageSpacing = 16.dp,
                modifier = Modifier.fillMaxWidth().height(180.dp)
            ) { page ->
                if (page < fundingSources.size) {
                    FundingSourceCard(
                        source = fundingSources[page],
                        onLongClick = {
                            sourceToDelete = fundingSources[page]
                            showDeleteDialog = true
                        }
                    )
                } else {
                    AddSourceCard(onClick = onAddFundingSourceClick)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun MiniPaymentItem(payment: Payment) {
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    val isRefundMovement = payment.amount < 0
    val isSuccessPayment = payment.amount > 0 && (payment.status == "APPROVED" || payment.status == "REFUNDED" || payment.status == "PARTIALLY_REFUNDED")
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = if (isRefundMovement) "Refund" else "Contactless",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = dateFormat.format(payment.date),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Text(
            text = String.format(Locale.getDefault(), "%s%s", if (isRefundMovement) "-" else "+", CurrencyUtils.formatAmount(kotlin.math.abs(payment.amount))),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (isRefundMovement) Color(0xFFFF3B30) else if (isSuccessPayment) Color(0xFF34C759) else Color(0xFFFF3B30)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FundingSourceCard(
    source: FundingSource,
    onLongClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "card_scale")

    val gradient = if (source.type == FundingSourceType.CARD) {
        Brush.linearGradient(listOf(Color(0xFF1A1A1A), Color(0xFF434343)))
    } else {
        Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer))
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .scale(scale)
            .clip(RoundedCornerShape(24.dp))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = {},
                onLongClick = onLongClick
            ),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (source.type == FundingSourceType.CARD) Icons.Default.CreditCard else Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = if (source.type == FundingSourceType.CARD) source.brand ?: "CARD" else "IBAN",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = source.label,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "**** ${source.last4}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun AddSourceCard(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add Card or Account",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        userType = com.example.myzum.model.AccountType.MERCHANT,
        fundingSources = listOf(
            FundingSource("1", FundingSourceType.CARD, "Personal Visa", "4242", "VISA", ownerId = "123"),
            FundingSource("2", FundingSourceType.ACCOUNT, "Business Savings", "8892", bankName = "Santander", ownerId = "123")
        ),
        payments = listOf(
            Payment("1", 12.50, "EUR", java.util.Date(), "APPROVED", ownerId = "123"),
            Payment("2", 5.00, "EUR", java.util.Date(), "REJECTED", ownerId = "123")
        ),
        onDataphoneClick = {},
        onHistoryClick = {},
        onStatisticsClick = {},
        onCatalogClick = {},
        onSettingsClick = {},
        onAddFundingSourceClick = {},
        onFundingSourceSelected = { _, _ -> },
        onRemoveFundingSource = {}
    )
}
