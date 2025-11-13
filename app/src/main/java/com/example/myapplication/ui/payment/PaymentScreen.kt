package com.example.myapplication.ui.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.model.PaymentMethod
import com.example.myapplication.data.model.PaymentType
import com.example.myapplication.ui.components.CustomAppBar
import com.example.myapplication.ui.theme.MyApplicationTheme

@Composable
fun PaymentScreen(
    viewModel: PaymentViewModel = viewModel(),
    orphanageName: String = "Hope Children's Home",
    category: String = "Food",
    onNavigateBack: () -> Unit = {},
    onPaymentSuccess: (String, Double) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    val event by viewModel.events.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    LaunchedEffect(event) {
        when (event) {
            is PaymentEvent.ShowError -> {
                snackbarHostState.showSnackbar((event as PaymentEvent.ShowError).message)
                viewModel.onEventHandled()
            }
            is PaymentEvent.PaymentSuccess -> {
                val successEvent = event as PaymentEvent.PaymentSuccess
                onPaymentSuccess(successEvent.transactionId, successEvent.amount)
                viewModel.onEventHandled()
            }
            null -> {}
        }
    }

    Scaffold(
        topBar = {
            CustomAppBar(
                title = "Payment",
                onNavigationClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Donation Summary
            DonationSummaryCard(
                orphanageName = orphanageName,
                category = category
            )

            // Amount Input
            AmountSection(
                amount = uiState.amount,
                onAmountChange = viewModel::onAmountChange,
                enabled = !uiState.isProcessing
            )

            // Payment Methods
            PaymentMethodsSection(
                paymentMethods = uiState.paymentMethods,
                selectedMethod = uiState.selectedPaymentMethod,
                onMethodSelected = viewModel::onPaymentMethodSelected,
                enabled = !uiState.isProcessing
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Pay Button
            Button(
                onClick = viewModel::onProcessPayment,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !uiState.isProcessing
            ) {
                if (uiState.isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Processing...")
                } else {
                    Icon(Icons.Default.Payment, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pay Now",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Security Note
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Your payment is secure and encrypted",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DonationSummaryCard(
    orphanageName: String,
    category: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Donation To",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = orphanageName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Category,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun AmountSection(
    amount: String,
    onAmountChange: (String) -> Unit,
    enabled: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Donation Amount",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Amount") },
            placeholder = { Text("0.00") },
            leadingIcon = {
                Text(
                    text = "$",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            enabled = enabled
        )

        // Quick amount buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("10", "25", "50", "100").forEach { quickAmount ->
                OutlinedButton(
                    onClick = { onAmountChange(quickAmount) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    enabled = enabled
                ) {
                    Text("\$$quickAmount")
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodsSection(
    paymentMethods: List<PaymentMethod>,
    selectedMethod: PaymentMethod?,
    onMethodSelected: (PaymentMethod) -> Unit,
    enabled: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Payment Method",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        paymentMethods.forEach { method ->
            PaymentMethodItem(
                method = method,
                isSelected = method == selectedMethod,
                onClick = { onMethodSelected(method) },
                enabled = enabled
            )
        }
    }
}

@Composable
private fun PaymentMethodItem(
    method: PaymentMethod,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getPaymentIcon(method.type),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary 
                       else MaterialTheme.colorScheme.onSurface
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = method.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
                method.lastFourDigits?.let {
                    Text(
                        text = "•••• $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun getPaymentIcon(type: PaymentType) = when (type) {
    PaymentType.CREDIT_CARD, PaymentType.DEBIT_CARD -> Icons.Default.CreditCard
    PaymentType.MOBILE_MONEY -> Icons.Default.PhoneAndroid
    PaymentType.BANK_TRANSFER -> Icons.Default.AccountBalance
    PaymentType.PAYPAL -> Icons.Default.Payment
}

@Preview(showBackground = true)
@Composable
fun PaymentScreenPreview() {
    MyApplicationTheme {
        PaymentScreen()
    }
}
