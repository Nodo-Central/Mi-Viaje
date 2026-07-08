package org.nodocentral.miviaje.presentation.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import org.nodocentral.miviaje.R;
import org.nodocentral.miviaje.presentation.CardUidFormatter;
import org.nodocentral.miviaje.presentation.CardActivity;
import org.nodocentral.miviaje.data.artwork.CardArtworkResolver;
import org.nodocentral.miviaje.domain.artwork.Artwork;
import org.nodocentral.miviaje.domain.mimovilidad.card.Card;
import org.nodocentral.miviaje.domain.mimovilidad.card.Product.State;
import org.nodocentral.miviaje.domain.mimovilidad.card.User.Profile.Type;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {
    public interface OnCardLongClickListener {
        boolean onCardLongClick(Card card, MenuItem item);
    }

    private List<Card> cardList;
    private Map<String, Artwork> artworksById;
    private final OnCardLongClickListener longClickListener;
    private boolean hideCardUid;

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView cardView;
        final ConstraintLayout cardLayout;
        final TextView cardTicketBalance;
        final TextView cardCashBalance;
        final TextView cardExpiry;
        final TextView cardUid;
        final TextView cardStatus;
        final TextView cardAlias;
        final Context context;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            cardView = itemView.findViewById(R.id.card_item);
            cardLayout = itemView.findViewById(R.id.card_item_layout);
            cardTicketBalance = itemView.findViewById(R.id.card_item_tickets);
            cardCashBalance = itemView.findViewById(R.id.card_item_cash);
            cardExpiry = itemView.findViewById(R.id.card_item_expiry);
            cardUid = itemView.findViewById(R.id.card_item_uid);
            cardStatus = itemView.findViewById(R.id.card_item_status);
            cardAlias = itemView.findViewById(R.id.card_item_alias);
        }
    }

    public CardAdapter(OnCardLongClickListener longClickListener, boolean hideCardUid) {
        this.cardList = new ArrayList<>();
        this.artworksById = new HashMap<>();
        this.longClickListener = longClickListener;
        this.hideCardUid = hideCardUid;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new CardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Card card = cardList.get(position);
        String transitionName = buildTransitionName(card);

        holder.cardCashBalance.setText(holder.context.getString(R.string.money_mxn_format, (float) card.getWalletValue() / 100));
        holder.cardExpiry.setText(holder.context.getString(
                R.string.card_expiration,
                card.getUser().getProfile().getExpirationDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)))
        );

        bindAlias(holder.cardAlias, card);
        holder.cardUid.setText(holder.context.getString(
                R.string.uid_format,
                CardUidFormatter.formatUid(card.getUidString(), hideCardUid)
        ));
        setCardStatus(card, holder.cardStatus, holder.context);
        holder.cardTicketBalance.setVisibility(TextView.GONE);

        Artwork artwork = resolveArtwork(card);
        setCardBackground(holder.context, holder.cardLayout, holder.cardTicketBalance, card, artwork);
        ViewCompat.setTransitionName(holder.cardView, transitionName);

        holder.cardLayout.setOnLongClickListener(v -> {
            showCardPopupMenu(v, card);
            return true;
        });
        holder.cardLayout.setOnClickListener(v ->
                openCardDetails(v, holder.cardView, card, transitionName)
        );
    }

    public static void setCardStatus(Card card, TextView cardStatus, Context context) {
        switch (card.getApplicationStatus().getState()) {
            case INITIALIZED:
                cardStatus.setText(context.getString(R.string.card_status_initialized));
                cardStatus.setBackgroundTintList(context.getColorStateList(R.color.miviaje_warning));
                break;

            case ACTIVATED:
                cardStatus.setText(context.getString(R.string.card_status_active));
                cardStatus.setBackgroundTintList(context.getColorStateList(R.color.miviaje_success));
                break;

            case BLOCKED:
                cardStatus.setText(context.getString(R.string.card_status_blocked));
                cardStatus.setBackgroundTintList(context.getColorStateList(R.color.miviaje_danger));
                break;

            case DEACTIVATED:
                cardStatus.setText(context.getString(R.string.card_status_deactivated));
                cardStatus.setBackgroundTintList(context.getColorStateList(R.color.miviaje_attention));
                break;
        }
    }

    private String buildTransitionName(Card card) {
        return "card_" + card.getUidString();
    }

    private void bindAlias(TextView aliasView, Card card) {
        if (card.getAlias() != null && !card.getAlias().isBlank()) {
            aliasView.setText(card.getAlias());
            aliasView.setVisibility(TextView.VISIBLE);
            return;
        }
        aliasView.setVisibility(TextView.GONE);
    }

    private Artwork resolveArtwork(Card card) {
        String artworkId = CardArtworkResolver.getArtworkId(card.getArtworkRef());
        return artworkId == null ? null : artworksById.get(artworkId);
    }

    private void openCardDetails(View view,
                                 MaterialCardView sharedCardView,
                                 Card card,
                                 String transitionName) {
        Intent intent = new Intent(view.getContext(), CardActivity.class);
        intent.putExtra("CARD_UID", card.getUid());
        intent.putExtra("CARD_TN", transitionName);

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                (Activity) view.getContext(),
                sharedCardView,
                transitionName
        );

        view.getContext().startActivity(intent, options.toBundle());
    }

    private void showCardPopupMenu(View anchor, Card card) {
        PopupMenu popupMenu = new PopupMenu(anchor.getContext(), anchor);
        popupMenu.inflate(R.menu.popup_card);
        popupMenu.setForceShowIcon(true);
        popupMenu.setOnMenuItemClickListener(item ->
                longClickListener != null && longClickListener.onCardLongClick(card, item)
        );
        popupMenu.show();
    }

    public static void setCardBackground(Context context,
                                         ConstraintLayout layout,
                                         TextView ticketBalance,
                                         Card card,
                                         Artwork artwork) {
        updateTicketBalanceView(context, ticketBalance, card);

        if (CardArtworkResolver.applyCustomBackground(context, card.getArtworkRef(), artwork, layout)) {
            return;
        }

        layout.setBackgroundResource(CardArtworkResolver.resolveAutomaticBackground(card));
    }

    private static void updateTicketBalanceView(Context context, TextView ticketBalance, Card card) {
        if (!shouldShowTicketBalance(card)) {
            ticketBalance.setVisibility(TextView.GONE);
            return;
        }

        ticketBalance.setVisibility(TextView.VISIBLE);
        ticketBalance.setText(context.getResources().getQuantityString(
                R.plurals.ticket_count, card.getTicketValue(), card.getTicketValue()));
    }

    private static boolean shouldShowTicketBalance(Card card) {
        Type profileType = card.getUser().getProfile().getType();
        return profileType != Type.GENERAL_FARE
                && profileType != Type.SINGLE_CARD
                && card.getBPDState() == State.ACTIVE;
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    public void updateCards(List<Card> cardList) {
        this.cardList = cardList;
        // TODO: Restore the empty-state affordance if the main screen brings back a dedicated placeholder view.
        notifyDataSetChanged();
    }

    public void updateArtworks(Map<String, Artwork> artworksById) {
        this.artworksById = artworksById == null ? new HashMap<>() : new HashMap<>(artworksById);
    }

    public void setHideCardUid(boolean hideCardUid) {
        if (this.hideCardUid == hideCardUid) {
            return;
        }
        this.hideCardUid = hideCardUid;
        notifyDataSetChanged();
    }
}
