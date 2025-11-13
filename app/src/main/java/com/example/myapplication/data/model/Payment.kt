package com.example.myapplication.data.model

data class PaymentMethod(
    val id: String,
    val type: PaymentType,
    val displayName: String,
    val lastFourDigits: String? = null,
    val expiryDate: String? = null,
    val isDefault: Boolean = false
)

enum class PaymentType {
    CREDIT_CARD,
    DEBIT_CARD,
    MOBILE_MONEY,
    BANK_TRANSFER,
    PAYPAL
}

data class PaymentDetails(
    val amount: Double,
    val currency: String = "USD",
    val paymentMethod: PaymentMethod,
    val donationId: String,
    val orphanageId: String,
    val orphanageName: String,
    val category: String,
    val note: String = ""
)
