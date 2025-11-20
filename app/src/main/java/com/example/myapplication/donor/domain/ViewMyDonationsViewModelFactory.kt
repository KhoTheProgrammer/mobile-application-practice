package com.example.myapplication.donor.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewMyDonationsViewModelFactory(
    private val donorId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ViewMyDonationsViewModel::class.java)) {
            return ViewMyDonationsViewModel(donorId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}