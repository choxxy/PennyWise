package com.iogarage.ke.pennywise;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.iogarage.ke.pennywise.api.ContactManager;
import com.iogarage.ke.pennywise.entities.Debt;
import com.iogarage.ke.pennywise.util.DateUtil;
import com.iogarage.ke.pennywise.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Joshua on 11/1/2014.
 */
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> implements Filterable {
    private List<Debt> _dataset;
    private List<Debt> _original;



    private Context mContext;

    public TransactionAdapter(List<Debt> dataset, Context context) {
        _dataset = dataset;
        _original = new LinkedList<>(dataset);
        mContext = context;
    }

    @Override
    public Filter getFilter() {
        return new TransactionFilter(this, _original);
    }

    public void setData(List<Debt> dataset) {
        _dataset = dataset;
        _original = new LinkedList<>(dataset);
        notifyDataSetChanged();
    }

    public void remove(int position) {
        if (!_dataset.isEmpty()) {


            Debt t = _dataset.get(position);
            EventBus.getDefault().post(new DeleteTransaction(t));

            _dataset.remove(position);
            _original.remove(position);
            notifyDataSetChanged();


        }

    }

    public void selected(int position) {
        Debt transaction = _dataset.get(position);
        Intent intent = new Intent(mContext, PaymentView.class);
        intent.putExtra(PaymentView.TRANSACTION_ID, transaction.getId());
        mContext.startActivity(intent);
    }

    // Not use static
    public class ViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener{
        public TextView mTextView;
        public TextView mTrxDate;
        public TextView mAmount;
        public ImageView image;
        public TextView mCurrency;
        public TextView status;
        public TextView textSeparator;
        CardView transactionCard;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.txtname);
            mTrxDate = itemView.findViewById(R.id.txtenddate);
            mAmount = itemView.findViewById(R.id.txtamount);
            mCurrency = itemView.findViewById(R.id.txtcurrency);
            image = itemView.findViewById(R.id.imageView);
            status = itemView.findViewById(R.id.status);
            textSeparator  = itemView.findViewById(R.id.header_separator);
            transactionCard = itemView.findViewById(R.id.transaction_card);

            transactionCard.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            if(v.getId() == R.id.header_separator)
                return;

            int position  =   getAdapterPosition();

            selected(position);
        }
    }

    @Override
    public int getItemCount() {
        return _dataset.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder,  int position) {

        final Debt transaction = _dataset.get(position);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(transaction.getPaydate());

        // Show header for item if it is the first in date group
        if (position > 0 && transaction.getPaydate().equals(_dataset.get(position - 1).getPaydate()) ) {
            holder.textSeparator.setVisibility(View.GONE);
        } else {
            String appropriateDate = DateUtil.getAppropriateDateFormat(mContext, calendar);
            holder.textSeparator.setText(appropriateDate);
            holder.textSeparator.setVisibility(View.VISIBLE);
        }

        holder.mTextView.setText(transaction.getPersonname());

        if (transaction.getType() == TransactionView.LENDING) {
            holder.mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.md_red_500));
            holder.mAmount.setText(Util.formatCurrency(transaction.getBalance()));
        } else {
            holder.mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.md_green_500));
            holder.mAmount.setText(Util.formatCurrency(transaction.getBalance()));
        }
        holder.mCurrency.setText(transaction.getCurrency());
        holder.mTrxDate.setText("Due date: " + Util.formatDate(transaction.getPaydate()));

        Bitmap photo = null;

        try {
            photo = ContactManager.getPhoto(mContext, transaction.getPhonenumber());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (photo != null) {
            //Bitmap photo = BitmapFactory.decodeByteArray(transaction.getImage(), 0, transaction.getImage().length);
            holder.image.setImageBitmap(photo);
        }

        LocalDate today  = LocalDate.now();
        LocalDate toDate = DateUtil.date2LocalDate(transaction.getPaydate());

        long days = ChronoUnit.DAYS.between(today, toDate);

        if (days > 1)
            holder.status.setText(days + " days");
        else if (days == 1)
            holder.status.setText(days + " day");
        else if (days == 0) {
            holder.status.setText("due");
            holder.status.setTextColor(Color.RED);
        } else {
            holder.status.setText("overdue");
            holder.status.setTextColor(Color.RED);
        }

        if (transaction.getBalance() <= 0)
            holder.status.setText("paid");





    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_transactions_list, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    private static class TransactionFilter extends Filter {

        private final TransactionAdapter adapter;

        private final List<Debt> originalList;

        private final List<Debt> filteredList;

        private TransactionFilter(TransactionAdapter adapter, List<Debt> originalList) {
            super();
            this.adapter = adapter;
            this.originalList = new LinkedList<>(originalList);
            this.filteredList = new ArrayList<>();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            filteredList.clear();
            final FilterResults results = new FilterResults();

            if (constraint.length() == 0) {
                filteredList.addAll(originalList);
            } else {
                final String filterPattern = constraint.toString().toLowerCase().trim();

                for (final Debt transaction : originalList) {
                    String name = transaction.getPersonname().toLowerCase().trim();
                    String phone = transaction.getPhonenumber().toLowerCase().trim();

                    if (name.startsWith(filterPattern) || phone.startsWith(filterPattern)) {
                        filteredList.add(transaction);
                    }
                }
            }
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter._dataset.clear();
            adapter._dataset.addAll((ArrayList<Debt>) results.values);
            adapter.notifyDataSetChanged();
        }
    }
}
