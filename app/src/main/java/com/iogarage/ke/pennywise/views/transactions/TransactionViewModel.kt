package com.iogarage.ke.pennywise.views.transactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iogarage.ke.pennywise.bindings.dto.TransactionDto
import com.iogarage.ke.pennywise.bindings.dto.fromEntity
import com.iogarage.ke.pennywise.bindings.dto.toEntity
import com.iogarage.ke.pennywise.domain.TransactionRepository
import com.iogarage.ke.pennywise.domain.entity.Transaction
import com.iogarage.ke.pennywise.domain.entity.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.util.EnumMap
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    private var _transaction = MutableLiveData<Transaction>()
    val transaction: LiveData<Transaction> = _transaction

    var transactionDto: TransactionDto = TransactionDto()

    private val _nameError = MutableLiveData("")
    val nameError: LiveData<String> = _nameError
    private val _amountError = MutableLiveData("")
    val amountError: LiveData<String> = _amountError
    private val _transactionTypeError = MutableLiveData("")
    val transactionTypeError: LiveData<String> = _transactionTypeError

    val transactionType = MutableLiveData<EnumMap<TransactionType, Boolean>>(
        EnumMap(TransactionType::class.java)
    ).apply {
        TransactionType.values().forEach { value?.put(it, false) }
    }

    init {
        getTransaction(0)
        // transactionType.value?.set(TransactionType.BORROWING, true)
    }

    private fun getTransaction(id: Long) {
        viewModelScope.launch {
            val transaction = Transaction(
                personName = "Peter Njoroge",
                phoneNumber = "0734454544",
                amount = 500.0,

                )// transactionRepository.getTransaction(id)
            transactionDto.fromEntity(transaction)
        }
    }

    fun update(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.updateDebt(transaction)
        }
    }

    fun setReminderDate(timeInMillis: Long) {
        val localDate =
            Instant.ofEpochMilli(timeInMillis).atZone(ZoneId.systemDefault()).toLocalDate();
        transactionDto.reminderDate = localDate.toEpochDay()
    }

    private fun validateInput(): Boolean {
        var isValid = true
        _nameError.value = ""
        _transactionTypeError.value = ""
        _amountError.value = ""

        val selectedTransactionType =
            transactionType.value?.filter { e -> e.value }?.keys
        if (selectedTransactionType?.isEmpty() == true) {
            _transactionTypeError.value = "Transaction type required"
            isValid = false
        } else {
            val type = selectedTransactionType?.first()
            transactionDto.type = type!!
        }

        if (transactionDto.personName.isEmpty()) {
            _nameError.value = "Name is required"
            isValid = false
        }

        if (transactionDto.amount <= 0.0) {
            _amountError.value = "Must be more than 0"
            isValid = false
        }
        return isValid

    }
    fun save() {
        viewModelScope.launch {
            if (validateInput()) {
                transactionRepository.insertTransaction(transactionDto.toEntity())
            }
        }

    }

    fun setEndDate(timeInMillis: Long) {
        val localDate =
            Instant.ofEpochMilli(timeInMillis).atZone(ZoneId.systemDefault()).toLocalDate();
        transactionDto.payDate = localDate.toEpochDay()
    }

    fun setStartDate(timeInMillis: Long) {
        val localDate =
            Instant.ofEpochMilli(timeInMillis).atZone(ZoneId.systemDefault()).toLocalDate();
        transactionDto.transactionDate = localDate.toEpochDay()
    }

}