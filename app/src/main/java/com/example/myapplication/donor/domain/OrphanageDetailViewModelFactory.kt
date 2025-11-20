package com.example.myapplication.donor.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class OrphanageDetailViewModelFactory(
    private val orphanageId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrphanageDetailViewModel::class.java)) {
            return OrphanageDetailViewModel(orphanageId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}