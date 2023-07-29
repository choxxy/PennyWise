package com.iogarage.ke.pennywise.views.home

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.iogarage.ke.pennywise.R
import com.iogarage.ke.pennywise.api.ContactManager
import com.iogarage.ke.pennywise.domain.entity.Transaction
import com.iogarage.ke.pennywise.domain.entity.TransactionType
import com.iogarage.ke.pennywise.util.asString
import com.iogarage.ke.pennywise.util.toCurrency
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.LinkedList
import java.util.Locale

/**
 * Created by Joshua on 11/1/2014.
 */
class TransactionAdapter(
    private var _dataset: List<Transaction>,
    private val onItemSelected: (Long) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>(), Filterable {
    private var transactions: MutableList<Transaction> = _dataset.toMutableList()

    override fun getFilter(): Filter {
        return TransactionFilter(this, transactions)
    }

    fun setData(dataset: List<Transaction>) {
        transactions = dataset.toMutableList()
        notifyDataSetChanged()
    }

    fun remove(position: Int) {
        if (_dataset.isNotEmpty()) {
            transactions.removeAt(position)
            notifyDataSetChanged()
        }
    }

    fun selected(position: Int) {
        val item = _dataset[position]
        onItemSelected(item.transactionId)
    }

    // Not use static
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var mTextView: TextView
        var mTrxDate: TextView
        var mAmount: TextView
        var image: ImageView
        var mCurrency: TextView
        var status: TextView
        var textSeparator: TextView
        private var transactionCard: CardView

        init {
            mTextView = itemView.findViewById(R.id.txtname)
            mTrxDate = itemView.findViewById(R.id.txtenddate)
            mAmount = itemView.findViewById(R.id.txtamount)
            mCurrency = itemView.findViewById(R.id.txtcurrency)
            image = itemView.findViewById(R.id.imageView)
            status = itemView.findViewById(R.id.status)
            textSeparator = itemView.findViewById(R.id.header_separator)
            transactionCard = itemView.findViewById(R.id.transaction_card)
            transactionCard.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            if (v.id == R.id.header_separator) return
            val position = adapterPosition
            selected(position)
        }
    }

    override fun getItemCount(): Int {
        return _dataset.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = _dataset[position]
        val date = LocalDate.ofEpochDay(transaction.payDate)
        val debug = date.asString("MMM dd", Locale.getDefault())
        Log.d("END_DATE", "$debug $transaction.payDate")

        // Show header for item if it is the first in date group
        if (position > 0 && transaction.payDate == _dataset[position - 1].payDate) {
            holder.textSeparator.visibility = View.GONE
        } else {
            val header = date.asString(
                "MMM dd",
                Locale.getDefault()
            )
            holder.textSeparator.text = header
            holder.textSeparator.visibility = View.VISIBLE
        }
        holder.mTextView.text = transaction.personName
        if (transaction.type === TransactionType.LENDING) {
            holder.mAmount.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.md_red_500
                )
            )
            holder.mAmount.text = transaction.balance.toCurrency()
        } else {
            holder.mAmount.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.md_green_500
                )
            )
            holder.mAmount.text = transaction.balance.toCurrency()
        }
        holder.mCurrency.text = transaction.currency
        val dueDate = date.asString(
            "E, dd MMM, yyyy",
            Locale.getDefault()
        )
        holder.mTrxDate.text = "Due date: $dueDate"
        var photo: Bitmap? = null
        try {
            photo = ContactManager.getPhoto(holder.itemView.context, transaction.phoneNumber)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        if (photo != null) {
            holder.image.setImageBitmap(photo)
        }
        val today = LocalDate.now()
        val toDate = LocalDate.ofEpochDay(transaction.payDate)
        val days = ChronoUnit.DAYS.between(today, toDate)
        if (days > 1) holder.status.text = "$days days" else if (days == 1L) holder.status.text =
            "$days day" else if (days == 0L) {
            holder.status.text = "due"
            holder.status.setTextColor(Color.RED)
        } else {
            holder.status.text = "overdue"
            holder.status.setTextColor(Color.RED)
        }
        if (transaction.balance <= 0) holder.status.text = "paid"
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_transactions_list, parent, false
        )
        return ViewHolder(view)
    }

    private class TransactionFilter constructor(
        private val adapter: TransactionAdapter,
        originalList: List<Transaction>
    ) : Filter() {
        private val _originalList: List<Transaction>
        private val filteredList: MutableList<Transaction>

        init {
            _originalList = LinkedList(originalList)
            filteredList = ArrayList()
        }

        override fun performFiltering(constraint: CharSequence): FilterResults {
            filteredList.clear()
            val results = FilterResults()
            if (constraint.isEmpty()) {
                filteredList.addAll(_originalList)
            } else {
                val filterPattern =
                    constraint.toString().lowercase(Locale.getDefault()).trim { it <= ' ' }
                for (transaction in _originalList) {
                    val name = transaction.personName.lowercase().trim();
                    val phone = transaction.phoneNumber.trim();

                    if (name.startsWith(filterPattern) || phone.startsWith(filterPattern)) {
                        filteredList.add(transaction);
                    }
                }
            }
            results.values = filteredList
            results.count = filteredList.size
            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            adapter.transactions.clear()
            adapter.transactions.addAll((results.values as ArrayList<Transaction>))
            adapter.notifyDataSetChanged()
        }
    }
}