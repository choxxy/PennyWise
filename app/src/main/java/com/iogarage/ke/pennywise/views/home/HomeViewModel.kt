package com.iogarage.ke.pennywise.views.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iogarage.ke.pennywise.domain.TransactionRepository
import com.iogarage.ke.pennywise.domain.entity.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private var _transactions = MutableLiveData<List<Transaction>>()
    val transaction: LiveData<List<Transaction>> = _transactions

    init {
        getTransactions()
    }

    private fun getTransactions() {
        viewModelScope.launch {
            transactionRepository.getTransactions().collect{ list ->
                _transactions.value = list
            }
        }
    }

}