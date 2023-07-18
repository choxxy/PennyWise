package com.iogarage.ke.pennywise.bindings

import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.google.android.material.chip.ChipGroup
import com.iogarage.ke.pennywise.R
import com.iogarage.ke.pennywise.domain.entity.TransactionType

object TransactionTypeAdapter {
    @BindingAdapter("transactionType")
    @JvmStatic
    fun ChipGroup.bindTransactionType(transactionType: TransactionType?) =
        transactionType?.let { filter ->
            when (filter) {
                TransactionType.BORROWING -> check(R.id.borrowing)
                TransactionType.LENDING -> check(R.id.lending)
            }
        }

    @InverseBindingAdapter(attribute = "transactionType")
    @JvmStatic
    fun ChipGroup.convertToTransactionType(): TransactionType = when (checkedChipId) {
        R.id.borrowing -> TransactionType.BORROWING
        R.id.lending -> TransactionType.LENDING
        else -> {
            TransactionType.BORROWING
        }
    }

    @BindingAdapter("transactionTypeAttrChanged")
    @JvmStatic
    fun ChipGroup.setListeners(attrChange: InverseBindingListener?) =
        setOnCheckedStateChangeListener { _, _ -> attrChange?.onChange() }
}