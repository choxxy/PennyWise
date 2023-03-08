package com.iogarage.ke.pennywise;

import android.content.Context;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iogarage.ke.pennywise.entities.Payment;
import com.iogarage.ke.pennywise.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by Joshua on 11/1/2014.
 */
public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.ViewHolder> {
    private List<Payment> mDataset;
    private Context mContext;

    public PaymentAdapter(List<Payment> dataset, Context context) {
        mDataset = dataset;
        mContext = context;
    }

    // Not use static
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTrxDate;
        public TextView mAmount;
        public TextView mDesc;


        public ViewHolder(View itemView) {
            super(itemView);
            mTrxDate = itemView.findViewById(R.id.txtpaydate);
            mDesc = itemView.findViewById(R.id.txtdesc);
            mAmount = itemView.findViewById(R.id.txtamount);

        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final Payment payment = mDataset.get(position);

        if (payment.getDescription().equals(mContext.getString(R.string.penalty))) {
            holder.mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.md_red_400));
        } else if (payment.getDescription().equals(mContext.getString(R.string.topup))) {
            holder.mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.md_blue_600));
        } else
            holder.mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.primary_dark));

        holder.mAmount.setText(Util.formatCurrency(payment.getAmountpaid()));
        holder.mTrxDate.setText(Util.formatDate(payment.getPaymentdate()));
        holder.mDesc.setText(TextUtils.isEmpty(payment.getDescription()) ? "Payment" : payment.getDescription());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new PaymentSelected(payment));
            }
        });


    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.payment_list_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }
}
