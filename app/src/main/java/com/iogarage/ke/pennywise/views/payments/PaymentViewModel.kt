package com.iogarage.ke.pennywise.views.payments

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iogarage.ke.pennywise.domain.PaymentRepository
import com.iogarage.ke.pennywise.domain.TransactionRepository
import com.iogarage.ke.pennywise.domain.entity.Payment
import com.iogarage.ke.pennywise.domain.entity.Transaction
import com.iogarage.ke.pennywise.domain.entity.TransactionWithPayments
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val transactionRepository: TransactionRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    private var _paymentUiState = MutableStateFlow<PaymentUiState>(PaymentUiState.Loading)
    val payments: StateFlow<PaymentUiState> = _paymentUiState

    init {
        val transactionId = state.get<Long>("transactionId")
        transactionId?.let {
            if (it != 0L)
                transactionWithPayments(it)
        }
    }

    private fun transactionWithPayments(transactionId: Long) {
        viewModelScope.launch {
            transactionRepository.getDebtWithPayments(transactionId)
                .collect { item ->
                    _paymentUiState.value = PaymentUiState.TransactionWithPaymentList(item)
                }
        }
    }

    fun deletePayment(payment: Payment) {
        viewModelScope.launch {
            paymentRepository.deletePayment(payment)
        }
    }

    fun updatePayment(payment: Payment) {
        viewModelScope.launch {
            paymentRepository.updatePayment(payment)
        }
    }

    fun addPayment(payment: Payment) {
        viewModelScope.launch {
            paymentRepository.addPayment(payment)
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.updateDebt(transaction)
        }
    }


}

sealed class PaymentUiState {
    data class TransactionWithPaymentList(val transaction: TransactionWithPayments) :
        PaymentUiState()

    data object Loading : PaymentUiState()
}