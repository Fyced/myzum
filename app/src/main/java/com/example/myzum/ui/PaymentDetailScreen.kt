package com.example.myzum.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myzum.model.FundingSource
import com.example.myzum.model.FundingSourceType
import com.example.myzum.model.Payment
import com.example.myzum.util.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentDetailScreen(
    payment: Payment?,
    fundingSources: List<FundingSource>,
    onRefundClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction Details") },
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
        if (payment == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Transaction not found")
            }
        } else {
            val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val isRefundMovement = payment.amount < 0
            val isSuccessPayment = payment.amount > 0 && (payment.status == "APPROVED" || payment.status == "REFUNDED" || payment.status == "PARTIALLY_REFUNDED")

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = if (isRefundMovement) Color(0xFFFF3B30) else if (isSuccessPayment) Color(0xFF34C759) else Color(0xFFFF3B30)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isSuccessPayment && !isRefundMovement) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = String.format(Locale.getDefault(), "%s%s", if (isRefundMovement) "-" else "", CurrencyUtils.formatAmount(kotlin.math.abs(payment.amount))),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = if (isRefundMovement) "REFUND ISSUED" else payment.status,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isRefundMovement) Color(0xFFFF3B30) else if (isSuccessPayment) Color(0xFF34C759) else Color(0xFFFF3B30)
                )

                Spacer(modifier = Modifier.height(48.dp))

                DetailRow(label = "Date", value = dateFormat.format(payment.date))
                DetailRow(label = "Time", value = timeFormat.format(payment.date))
                
                val source = fundingSources.find { it.id == payment.fundingSourceId }
                if (source != null) {
                    val receptorType = if (source.type == FundingSourceType.CARD) "Card" else "Account"
                    val receptorValue = if (source.type == FundingSourceType.CARD) {
                        "${source.brand ?: "Card"} **** ${source.last4}"
                    } else {
                        "IBAN **** ${source.last4}"
                    }
                    DetailRow(label = "Receptor ($receptorType)", value = receptorValue)
                }

                DetailRow(label = "Transaction ID", value = payment.id)
                DetailRow(label = "Payment Method", value = "Contactless (NFC)")
                DetailRow(label = "Merchant", value = "Myzum Terminal")

                if (payment.refundedAmount > 0) {
                    DetailRow(
                        label = "Refunded Amount",
                        value = CurrencyUtils.formatAmount(payment.refundedAmount)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                if (payment.status == "APPROVED" || payment.status == "PARTIALLY_REFUNDED") {
                    Button(
                        onClick = { onRefundClick(payment.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF3B30).copy(alpha = 0.1f),
                            contentColor = Color(0xFFFF3B30)
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text("Process Refund", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Button(
                    onClick = onBackClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Close", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.secondary, fontSize = 16.sp)
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
}

@Preview(showBackground = true)
@Composable
fun PaymentDetailScreenPreview() {
    PaymentDetailScreen(
        payment = Payment("TRX-123456", 24.99, "EUR", Date(), "APPROVED", fundingSourceId = "1", ownerId = "123"),
        fundingSources = listOf(
            FundingSource("1", FundingSourceType.CARD, "Personal Visa", "4242", "VISA", ownerId = "123")
        ),
        onRefundClick = {},
        onBackClick = {}
    )
}
