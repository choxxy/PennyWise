package com.iogarage.ke.pennywise.tabs;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iogarage.ke.pennywise.R;
import com.iogarage.ke.pennywise.util.Util;

import java.util.List;

/**
 * Created by Joshua on 11/1/2014.
 */
public class SummaryAdapter extends RecyclerView.Adapter<SummaryAdapter.ViewHolder> {
    private List<Summary> mDataset;
    private Context mContext;

    public SummaryAdapter(List<Summary> dataset, Context context) {
        mDataset = dataset;
        mContext = context;
    }

    // Not use static
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mNo;
        public TextView mAmount;
        public TextView mDesc;

        public ViewHolder(View itemView) {
            super(itemView);
            mNo = itemView.findViewById(R.id.no);
            mDesc = itemView.findViewById(R.id.desc);
            mAmount = itemView.findViewById(R.id.amount);
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final Summary item = mDataset.get(position);
        holder.mAmount.setText(Util.formatCurrency(item.getAmount()));
        holder.mDesc.setText(item.getDesc());
        holder.mNo.setText((position + 1) + ".");


    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }
}
