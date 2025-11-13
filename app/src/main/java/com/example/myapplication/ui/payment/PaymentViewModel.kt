package com.example.myapplication.ui.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.PaymentMethod
import com.example.myapplication.data.model.PaymentType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PaymentViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<PaymentEvent?>(null)
    val events: StateFlow<PaymentEvent?> = _events.asStateFlow()

    init {
        loadPaymentMethods()
    }

    private fun loadPaymentMethods() {
        viewModelScope.launch {
            delay(500)
            
            // Mock payment methods
            val methods = listOf(
                PaymentMethod(
                    id = "1",
                    type = PaymentType.CREDIT_CARD,
                    displayName = "Visa ending in 4242",
                    lastFourDigits = "4242",
                    expiryDate = "12/25",
                    isDefault = true
                ),
                PaymentMethod(
                    id = "2",
                    type = PaymentType.MOBILE_MONEY,
                    displayName = "M-Pesa",
                    isDefault = false
                ),
                PaymentMethod(
                    id = "3",
                    type = PaymentType.PAYPAL,
                    displayName = "PayPal",
                    isDefault = false
                )
            )
            
            _uiState.update { it.copy(
                paymentMethods = methods,
                selectedPaymentMethod = methods.firstOrNull { it.isDefault }
            )}
        }
    }

    fun onPaymentMethodSelected(method: PaymentMethod) {
        _uiState.update { it.copy(selectedPaymentMethod = method) }
    }

    fun onAmountChange(amount: String) {
        _uiState.update { it.copy(amount = amount) }
    }

    fun onProcessPayment() {
        if (validatePayment()) {
            processPayment()
        }
    }

    private fun validatePayment(): Boolean {
        val amount = _uiState.value.amount.toDoubleOrNull()
        
        if (amount == null || amount <= 0) {
            _events.value = PaymentEvent.ShowError("Please enter a valid amount")
            return false
        }
        
        if (_uiState.value.selectedPaymentMethod == null) {
            _events.value = PaymentEvent.ShowError("Please select a payment method")
            return false
        }
        
        return true
    }

    private fun processPayment() {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            
            // Simulate payment processing
            delay(2000)
            
            _uiState.update { it.copy(isProcessing = false) }
            _events.value = PaymentEvent.PaymentSuccess(
                transactionId = "TXN${System.currentTimeMillis()}",
                amount = _uiState.value.amount.toDouble()
            )
        }
    }

    fun onEventHandled() {
        _events.value = null
    }
}

data class PaymentUiState(
    val amount: String = "",
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val selectedPaymentMethod: PaymentMethod? = null,
    val isProcessing: Boolean = false
)

sealed class PaymentEvent {
    data class ShowError(val message: String) : PaymentEvent()
    data class PaymentSuccess(val transactionId: String, val amount: Double) : PaymentEvent()
}
