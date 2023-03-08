package com.iogarage.ke.pennywise.tabs;

import androidx.cardview.widget.CardView;

/**
 * Created by choxxy on 21/11/2016.
 */

public interface CardAdapter {

    int MAX_ELEVATION_FACTOR = 8;

    float getBaseElevation();

    CardView getCardViewAt(int position);

    int getCount();
}
