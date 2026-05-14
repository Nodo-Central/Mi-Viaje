package org.nodocentral.miviaje.presentation;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.nodocentral.miviaje.R;

final class CardListUiStateController {
    private final RecyclerView cardRecycler;
    private final View loadingContainer;
    private final TextView emptyTipText;

    CardListUiStateController(View rootView) {
        cardRecycler = rootView.findViewById(R.id.main_card_recycler);
        loadingContainer = rootView.findViewById(R.id.main_loading_container);
        emptyTipText = rootView.findViewById(R.id.main_empty_tip_text);
    }

    RecyclerView getCardRecycler() {
        return cardRecycler;
    }

    void showLoading() {
        loadingContainer.setVisibility(View.VISIBLE);
        cardRecycler.setVisibility(View.GONE);
        emptyTipText.setVisibility(View.GONE);
    }

    void showCards() {
        loadingContainer.setVisibility(View.GONE);
        cardRecycler.setVisibility(View.VISIBLE);
        emptyTipText.setVisibility(View.GONE);
    }

    void showEmptyTip() {
        loadingContainer.setVisibility(View.GONE);
        cardRecycler.setVisibility(View.GONE);
        emptyTipText.setVisibility(View.VISIBLE);
    }
}
