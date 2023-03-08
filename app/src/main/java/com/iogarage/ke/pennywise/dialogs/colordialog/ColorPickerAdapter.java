package com.iogarage.ke.pennywise.dialogs.colordialog;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iogarage.ke.pennywise.R;

import io.multimoon.colorful.ColorfulKt;
import io.multimoon.colorful.ThemeColor;

class ColorPickerAdapter extends RecyclerView.Adapter<ColorPickerAdapter.ItemViewHolder> {
    private Context context;
    private OnItemClickListener listener;

    ColorPickerAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return ThemeColor.values().length;
    }

    @Override
    public void onBindViewHolder(ItemViewHolder ViewHolder, int i) {
        if (ColorfulKt.Colorful().getDarkTheme())
            ViewHolder.circle.setColor(ThemeColor.values()[i].getColorPack().dark().asInt());
        else
            ViewHolder.circle.setColor(ThemeColor.values()[i].getColorPack().normal().asInt());
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final ItemViewHolder holder = new ItemViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_coloritem, viewGroup, false));
        holder.circle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onItemClick(ThemeColor.values()[holder.getAdapterPosition()]);
                }
            }
        });
        return holder;
    }

    void setOnItemClickListener(OnItemClickListener l) {
        listener = l;
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        CircularView circle;

        ItemViewHolder(View v) {
            super(v);
            circle = ((CircularView) v);
        }
    }

    interface OnItemClickListener {
        void onItemClick(ThemeColor color);
    }
}
