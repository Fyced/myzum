package com.example.myzum.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myzum.model.Payment
import com.example.myzum.util.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    payments: List<Payment>,
    onBackClick: () -> Unit,
    onPaymentClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Transaction History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        if (payments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No transactions yet",
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(payments) { payment ->
                    PaymentItem(payment = payment, onClick = { onPaymentClick(payment.id) })
                }
            }
        }
    }
}

@Composable
fun PaymentItem(
    payment: Payment,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    
    // A movement is a refund if its amount is negative
    val isRefundMovement = payment.amount < 0
    // A payment is successful if it was approved, even if it was later refunded
    val isSuccessPayment = payment.amount > 0 && (payment.status == "APPROVED" || payment.status == "REFUNDED" || payment.status == "PARTIALLY_REFUNDED")
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = if (isRefundMovement) "Refund Issued" else "Contactless Payment",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = dateFormat.format(payment.date),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            Text(
                text = String.format(Locale.getDefault(), "%s%s", if (isRefundMovement) "-" else "+", CurrencyUtils.formatAmount(kotlin.math.abs(payment.amount))),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isRefundMovement) Color(0xFFFF3B30) else if (isSuccessPayment) Color(0xFF34C759) else Color(0xFFFF3B30)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    HistoryScreen(
        payments = listOf(
            Payment("1", 12.50, "EUR", Date(), "APPROVED", ownerId = "123"),
            Payment("2", 5.00, "EUR", Date(), "REJECTED", ownerId = "123")
        ),
        onBackClick = {},
        onPaymentClick = {}
    )
}
