package com.example.myzum.ui.refund

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myzum.model.Payment
import com.example.myzum.util.CurrencyUtils
import com.example.myzum.viewmodel.RefundUiState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefundScreen(
    payment: Payment?,
    uiState: RefundUiState,
    onRefundConfirm: (Double, String?) -> Unit,
    onSuccessDismiss: () -> Unit,
    onBackClick: () -> Unit
) {
    if (payment == null) return

    var amountInput by remember { mutableStateOf(String.format("%.2f", payment.amount - payment.refundedAmount)) }
    var reason by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val availableToRefund = payment.amount - payment.refundedAmount

    val parsedInput = amountInput.replace(",", ".").toDoubleOrNull() ?: 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Issue Refund") },
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
                .padding(24.dp)
        ) {
    Text(text = "Original Payment", color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
            Text(
                text = CurrencyUtils.formatAmount(payment.amount),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Available to refund", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                    Text(
                        text = CurrencyUtils.formatAmount(availableToRefund),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = amountInput,
                onValueChange = { amountInput = it },
                label = { Text("Refund Amount") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Reason (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = uiState is RefundUiState.Idle && parsedInput > 0 && parsedInput <= availableToRefund
            ) {
                if (uiState is RefundUiState.Processing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Confirm Refund", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Refund") },
            text = { Text("Are you sure you want to refund ${amountInput} €? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    onRefundConfirm(parsedInput, if (reason.isEmpty()) null else reason)
                    showConfirmDialog = false
                }) {
                    Text("Refund", color = Color(0xFFFF3B30))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState is RefundUiState.Success) {
        Dialog(onDismissRequest = onSuccessDismiss) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Refund Successful",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "${CurrencyUtils.formatAmount(uiState.amount)} has been returned to the original payment method.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onSuccessDismiss,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}
