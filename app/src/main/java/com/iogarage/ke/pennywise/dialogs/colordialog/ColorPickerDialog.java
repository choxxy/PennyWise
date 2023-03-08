package com.iogarage.ke.pennywise.dialogs.colordialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;

import com.iogarage.ke.pennywise.R;

import io.multimoon.colorful.ThemeColor;

import static io.multimoon.colorful.ColorfulKt.Colorful;

public class ColorPickerDialog extends Dialog implements View.OnClickListener, ColorPickerAdapter.OnItemClickListener {
    private RecyclerView recycler;
    private Toolbar toolbar;
    private OnColorSelectedListener listener;

    public ColorPickerDialog(Context context) {
        super(context);
    }

    @Override
    public void onClick(View view) {
        dismiss();
    }

    @Override
    public void onItemClick(ThemeColor color) {
        dismiss();
        if (listener != null) {
            listener.onColorSelected(color);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_colorpicker);

        recycler = findViewById(R.id.colorful_color_picker_recycler);
        toolbar = findViewById(R.id.colorful_color_picker_toolbar);
        toolbar.setNavigationOnClickListener(this);

        if (Colorful().getDarkTheme())
            toolbar.setBackgroundColor(Colorful().getPrimaryColor().getColorPack().dark().asInt());
        else
            toolbar.setBackgroundColor(Colorful().getPrimaryColor().getColorPack().normal().asInt());

        toolbar.setTitle(R.string.select_color);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_48px);
        recycler.setLayoutManager(new GridLayoutManager(getContext(), 4));
        ColorPickerAdapter adapter = new ColorPickerAdapter(getContext());
        adapter.setOnItemClickListener(this);
        recycler.setAdapter(adapter);
    }

    public interface OnColorSelectedListener {
        void onColorSelected(ThemeColor color);
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        this.listener = listener;
    }
}
