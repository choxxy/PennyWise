package com.iogarage.ke.pennywise.views.payments

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.iogarage.ke.pennywise.R
import com.iogarage.ke.pennywise.domain.entity.Payment
import com.iogarage.ke.pennywise.util.asDateString
import com.iogarage.ke.pennywise.util.toCurrency

/**
 * Created by Joshua on 11/1/2014.
 */
class PaymentAdapter(
    private val mDataset: List<Payment>,
    private val onItemClicked: (Payment) -> Unit
) :
    RecyclerView.Adapter<PaymentAdapter.ViewHolder>() {
    // Not use static
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mTrxDate: TextView
        var mAmount: TextView
        var mDesc: TextView

        init {
            mTrxDate = itemView.findViewById(R.id.txtpaydate)
            mDesc = itemView.findViewById(R.id.txtdesc)
            mAmount = itemView.findViewById(R.id.txtamount)
        }
    }

    override fun getItemCount(): Int {
        return mDataset.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val payment = mDataset[position]
        if (payment.description == holder.itemView.context.getString(R.string.penalty)) {
            holder.mAmount.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.md_red_400
                )
            )
        } else if (payment.description == holder.itemView.context.getString(R.string.topup)) {
            holder.mAmount.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.md_blue_600
                )
            )
        } else holder.mAmount.setTextColor(
            ContextCompat.getColor(
                holder.itemView.context,
                R.color.primary_dark
            )
        )
        holder.mAmount.text = payment.amountPaid.toCurrency()
        holder.mTrxDate.text = payment.paymentDate.asDateString()
        holder.mDesc.text =
            if (TextUtils.isEmpty(payment.description)) "Payment" else payment.description
        holder.itemView.setOnClickListener {
            onItemClicked(payment)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.payment_list_item, parent, false
        )
        return ViewHolder(view)
    }
}