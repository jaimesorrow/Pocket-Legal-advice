package com.pocketlawbook.alaska.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pocketlawbook.alaska.data.billing.BillingRepository
import com.pocketlawbook.alaska.data.billing.PurchaseState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Keeps Compose screens off BillingRepository directly. */
class SubscriptionViewModel(
    private val repository: BillingRepository
) : ViewModel() {

    val purchaseState: StateFlow<PurchaseState> = repository.purchaseState
    val formattedPrice: StateFlow<String?> = repository.formattedPrice

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch { repository.connect() }
    }

    fun purchase(activity: Activity) {
        viewModelScope.launch {
            repository.launchPurchaseFlow(activity)
                .onFailure { _errorMessage.value = it.message ?: "Couldn't start the purchase." }
        }
    }

    fun restore() {
        viewModelScope.launch {
            repository.restorePurchases()
                .onFailure { _errorMessage.value = it.message ?: "Couldn't restore purchases." }
        }
    }

    fun manageSubscriptionUrl(): String = repository.manageSubscriptionUrl()

    class Factory(private val repository: BillingRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SubscriptionViewModel(repository) as T
    }
}
