package com.iogarage.ke.pennywise.views.home

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter
import com.ernestoyaquello.dragdropswiperecyclerview.util.DragDropSwipeDiffCallback
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
    private val items: List<Transaction> = emptyList(),
    private val onItemSelected: (Long) -> Unit
) : DragDropSwipeAdapter<Transaction, TransactionAdapter.ViewHolder>(items), Filterable {
    private var transactions: MutableList<Transaction> = items.toMutableList()

    override fun getFilter(): Filter {
        return TransactionFilter(this, transactions)
    }

    fun selected(position: Int) {
        val item = dataSet[position]
        onItemSelected(item.transactionId)
    }

    // Not use static
    inner class ViewHolder(itemView: View) : DragDropSwipeAdapter.ViewHolder(itemView),
        View.OnClickListener {
        var mTextView: TextView
        var mTrxDate: TextView
        var mAmount: TextView
        var image: ImageView
        var mCurrency: TextView
        var status: TextView
        var textSeparator: TextView
        val dragIcon: ImageView
        private val transactionCard: CardView

        init {
            mTextView = itemView.findViewById(R.id.txtname)
            mTrxDate = itemView.findViewById(R.id.txtenddate)
            mAmount = itemView.findViewById(R.id.txtamount)
            mCurrency = itemView.findViewById(R.id.txtcurrency)
            image = itemView.findViewById(R.id.imageView)
            status = itemView.findViewById(R.id.status)
            textSeparator = itemView.findViewById(R.id.header_separator)
            dragIcon = itemView.findViewById(R.id.drag_icon)
            transactionCard = itemView.findViewById(R.id.transaction_card)
            transactionCard.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            if (v.id == R.id.header_separator) return
            val position = adapterPosition
            selected(position)
        }
    }

    override fun getViewHolder(itemView: View): ViewHolder {
        return ViewHolder(itemView)
    }

    override fun createDiffUtil(
        oldList: List<Transaction>,
        newList: List<Transaction>
    ): DragDropSwipeDiffCallback<Transaction> {
        return TransactionDiff(oldList, newList)
    }

    override fun onBindViewHolder(transaction: Transaction, viewHolder: ViewHolder, position: Int) {
        val context = viewHolder.itemView.context
        val date = LocalDate.ofEpochDay(transaction.payDate)

        viewHolder.mTextView.text = transaction.personName

        if (transaction.type === TransactionType.LENDING) {
            viewHolder.mAmount.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.md_red_500
                )
            )
            viewHolder.mAmount.text = transaction.balance.toCurrency()
        } else {
            viewHolder.mAmount.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.md_green_500
                )
            )
            viewHolder.mAmount.text = transaction.balance.toCurrency()
        }

        viewHolder.mCurrency.text = transaction.currency

        val dueDate = date.asString(
            "E, dd MMM, yyyy",
            Locale.getDefault()
        )

        viewHolder.mTrxDate.text = "Due date: $dueDate"
        var photo: Bitmap? = null
        try {
            photo = ContactManager.getPhoto(context, transaction.phoneNumber)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        if (photo != null) {
            viewHolder.image.setImageBitmap(photo)
        }
        val today = LocalDate.now()
        val toDate = LocalDate.ofEpochDay(transaction.payDate)
        val days = ChronoUnit.DAYS.between(today, toDate)
        if (days > 1) viewHolder.status.text =
            "$days days" else if (days == 1L) viewHolder.status.text =
            "$days day" else if (days == 0L) {
            viewHolder.status.text = "due"
            viewHolder.status.setTextColor(Color.RED)
        } else {
            viewHolder.status.text = "overdue"
            viewHolder.status.setTextColor(Color.RED)
        }
        if (transaction.balance <= 0) viewHolder.status.text = "paid"
    }

    override fun getViewToTouchToStartDraggingItem(
        item: Transaction,
        viewHolder: ViewHolder,
        position: Int
    ): View? = viewHolder.dragIcon

    override fun onIsSwiping(
        item: Transaction?,
        viewHolder: ViewHolder,
        offsetX: Int,
        offsetY: Int,
        canvasUnder: Canvas?,
        canvasOver: Canvas?,
        isUserControlled: Boolean
    ) {
    }

    override fun onSwipeStarted(item: Transaction, viewHolder: ViewHolder) {
    }

    override fun onSwipeAnimationFinished(viewHolder: ViewHolder) {

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

class TransactionDiff(
    oldList: List<Transaction>,
    newList: List<Transaction>
) : DragDropSwipeDiffCallback<Transaction>(oldList, newList) {
    override fun isSameContent(oldItem: Transaction, newItem: Transaction): Boolean {
        return oldItem == newItem
    }

    override fun isSameItem(oldItem: Transaction, newItem: Transaction): Boolean {
        return oldItem.transactionId == newItem.transactionId
    }

}