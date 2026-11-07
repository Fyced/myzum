package com.example.myzum.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myzum.payment.RefundResult
import com.example.myzum.repository.RefundRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class RefundUiState {
    object Idle : RefundUiState()
    object Processing : RefundUiState()
    data class Success(val amount: Double) : RefundUiState()
    data class Error(val message: String) : RefundUiState()
}

class RefundViewModel(
    private val repository: RefundRepository,
    private val activeUserId: StateFlow<String?>
) : ViewModel() {

    private val _uiState = MutableStateFlow<RefundUiState>(RefundUiState.Idle)
    val uiState: StateFlow<RefundUiState> = _uiState.asStateFlow()

    fun refund(paymentId: String, amount: Double, reason: String?) {
        val userId = activeUserId.value ?: run {
            _uiState.value = RefundUiState.Error("No user session found")
            return
        }

        viewModelScope.launch {
            _uiState.value = RefundUiState.Processing
            when (val result = repository.processRefund(paymentId, userId, amount, reason)) {
                is RefundResult.Success -> {
                    _uiState.value = RefundUiState.Success(amount)
                }
                is RefundResult.Pending -> {
                    // For now handle pending as success or separate state
                    _uiState.value = RefundUiState.Success(amount)
                }
                is RefundResult.Failed -> {
                    _uiState.value = RefundUiState.Error(result.message)
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = RefundUiState.Idle
    }
}
