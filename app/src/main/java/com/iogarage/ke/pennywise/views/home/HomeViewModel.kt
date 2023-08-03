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

    private var _homeUiState = MutableLiveData<HomeUiState>()
    val homeUiState: LiveData<HomeUiState> = _homeUiState
    var removedItemPosition = 0

    init {
        getTransactions()
    }

    private fun getTransactions() {
        viewModelScope.launch {
            transactionRepository.getTransactions().collect { list ->
                _homeUiState.value = HomeUiState(transactions = list)
            }
        }
    }

    fun removeItem(item: Transaction, position: Int) {
        val tempList = _homeUiState.value?.transactions?.toMutableList()
        tempList?.let {
            it.removeAt(position)
            _homeUiState.value = _homeUiState.value?.copy(transactions = it)
        }
    }

    fun restoreItem(item: Transaction, position: Int) {
        val tempList = _homeUiState.value?.transactions?.toMutableList()
        tempList?.let {
            if (!tempList.contains(item))
                it.add(position, item)
            _homeUiState.value =
                _homeUiState.value?.copy(transactions = it)
        }
    }

    fun deleteItem(item: Transaction) {
        viewModelScope.launch {
            transactionRepository.deleteDebt(item)
        }
    }
}

data class HomeUiState(
    val transactions: List<Transaction> = emptyList(),
)