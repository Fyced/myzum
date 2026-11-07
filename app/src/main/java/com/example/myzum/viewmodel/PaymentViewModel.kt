package com.example.myzum.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myzum.model.Payment
import com.example.myzum.nfc.NfcRepository
import com.example.myzum.payment.PaymentProvider
import com.example.myzum.payment.PaymentState
import com.example.myzum.repository.PaymentRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

class PaymentViewModel(
    private val paymentProvider: PaymentProvider,
    private val paymentRepository: PaymentRepository,
    private val nfcRepository: NfcRepository,
    private val activeUserId: StateFlow<String?>
) : ViewModel() {

    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState.asStateFlow()

    private val _amount = MutableStateFlow(0.0)
    val amount: StateFlow<Double> = _amount.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _selectedFundingSourceId = MutableStateFlow<String?>(null)
    val selectedFundingSourceId: StateFlow<String?> = _selectedFundingSourceId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val paymentHistory: StateFlow<List<Payment>> = activeUserId.flatMapLatest { userId ->
        if (userId == null) flowOf(emptyList())
        else combine(
            paymentRepository.getPayments(userId),
            _selectedFundingSourceId
        ) { payments, sourceId ->
            if (sourceId == null) payments
            else payments.filter { it.fundingSourceId == sourceId }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            nfcRepository.tagDetected.collect {
                if (_isListening.value && (_paymentState.value is PaymentState.Idle || _paymentState.value is PaymentState.Processing)) {
                    processPayment()
                }
            }
        }
    }

    fun setAmount(newAmount: Double) {
        _amount.value = newAmount
    }

    fun setSelectedFundingSourceId(id: String?) {
        _selectedFundingSourceId.value = id
    }

    fun startPayment() {
        // Transition to a state that indicates we are waiting for NFC tap
        // In this case, we'll keep it simple and just stay in Idle but the UI will know
        // it's waiting for NFC because of the screen it's on.
        _isListening.value = true
        nfcRepository.startListening()
    }

    fun stopPayment() {
        _isListening.value = false
        nfcRepository.stopListening()
        _paymentState.value = PaymentState.Idle
    }

    private fun processPayment() {
        val userId = activeUserId.value ?: return
        viewModelScope.launch {
            _paymentState.value = PaymentState.Processing
            val result = paymentProvider.processPayment(_amount.value, "EUR")
            
            if (result.success) {
                val payment = Payment(
                    id = result.transactionId ?: UUID.randomUUID().toString(),
                    amount = _amount.value,
                    currency = "EUR",
                    date = Date(),
                    status = "APPROVED",
                    fundingSourceId = _selectedFundingSourceId.value,
                    ownerId = userId
                )
                paymentRepository.savePayment(payment)
                _paymentState.value = PaymentState.Approved
            } else {
                _paymentState.value = PaymentState.Rejected
            }
        }
    }
    
    fun reset() {
        _paymentState.value = PaymentState.Idle
        _amount.value = 0.0
        // We no longer reset _selectedFundingSourceId here to preserve selection
    }
}
