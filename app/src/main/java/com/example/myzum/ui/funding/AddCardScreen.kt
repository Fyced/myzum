package com.example.myzum.ui.funding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myzum.model.FundingSource
import com.example.myzum.model.FundingSourceType
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardScreen(
    ownerId: String,
    onCardAdded: (FundingSource) -> Unit,
    onBackClick: () -> Unit
) {
    var label by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Card") },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Card Label (e.g. Personal Visa)") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = brand,
                onValueChange = { brand = it },
                label = { Text("Card Brand (e.g. VISA, Mastercard)") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = cardNumber,
                onValueChange = { if (it.length <= 16) cardNumber = it },
                label = { Text("Card Number") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
            )

            Button(
                onClick = {
                    if (cardNumber.length >= 4) {
                        onCardAdded(
                            FundingSource(
                                id = UUID.randomUUID().toString(),
                                type = FundingSourceType.CARD,
                                label = label.ifEmpty { "New Card" },
                                last4 = cardNumber.takeLast(4),
                                brand = brand.ifEmpty { "Card" },
                                ownerId = ownerId
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = cardNumber.length >= 4
            ) {
                Text("Add Card", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddCardScreenPreview() {
    AddCardScreen(ownerId = "123", onCardAdded = {}, onBackClick = {})
}
