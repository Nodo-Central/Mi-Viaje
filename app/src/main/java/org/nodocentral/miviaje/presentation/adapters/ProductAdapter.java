package org.nodocentral.miviaje.presentation.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;

import org.nodocentral.miviaje.R;
import org.nodocentral.miviaje.domain.mimovilidad.card.Product;
import org.nodocentral.miviaje.domain.mimovilidad.card.ProductContract;
import org.nodocentral.miviaje.domain.mimovilidad.card.ProductService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

public final class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.VH> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    private final List<Product> items;
    private final OnProductClickListener listener;

    public ProductAdapter(List<Product> products, OnProductClickListener listener) {
        this.items = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pm_preview, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Product product = items.get(position);
        holder.bind(product);
        holder.itemView.setOnClickListener(v -> listener.onProductClick(product));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class VH extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView status;
        final TextView value;
        final TextView expiration;

        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.item_pm_preview_title);
            status = itemView.findViewById(R.id.item_pm_preview_status);
            value = itemView.findViewById(R.id.item_pm_preview_value);
            expiration = itemView.findViewById(R.id.item_pm_preview_expiration);
        }

        void bind(Product product) {
            title.setText(formatProductTitle(product));
            value.setText(formatMethodValue(itemView, product));

            ProductService service = product.getService();
            ProductService.State state = service == null ? null : service.getState();
            status.setText(formatState(state));
            tintStatus(status, state);

            ProductContract contract = product.getContract();
            ProductContract.Validity validity = contract == null ? null : contract.getValidity();
            LocalDateTime validTo = validity == null ? null : validity.getValidTo();
            if (validTo == null) {
                expiration.setVisibility(View.GONE);
            } else {
                expiration.setText(getString(R.string.payment_method_row_format,
                        getString(R.string.payment_method_valid_until),
                        formatDate(validTo)));
                expiration.setVisibility(View.VISIBLE);
            }
        }

        String formatProductTitle(Product product) {
            if (product.getType() == null) {
                return String.valueOf(product.getId());
            }
            switch (product.getType()) {
                case CREDIT:
                    return getString(R.string.payment_method_type_credit);
                case WALLET:
                    return getString(R.string.payment_method_type_wallet);
                case DISCOUNT_TICKETS_1:
                case DISCOUNT_TICKETS_2:
                    return getString(R.string.payment_method_type_discount_ticket);
                default:
                    return String.valueOf(product.getId());
            }
        }

        String formatState(ProductService.State state) {
            if (state == null) {
                return getString(R.string.unknown);
            }
            switch (state) {
                case INITIALIZED:
                    return getString(R.string.payment_method_status_initialized);
                case ACTIVATED:
                    return getString(R.string.payment_method_status_active);
                case SUSPENDED:
                    return getString(R.string.payment_method_status_suspended);
                default:
                    return String.valueOf(state);
            }
        }

        void tintStatus(TextView view, ProductService.State state) {
            int color;
            if (state == ProductService.State.ACTIVATED) {
                color = ContextCompat.getColor(itemView.getContext(), R.color.miviaje_success);
            } else if (state == ProductService.State.SUSPENDED) {
                color = ContextCompat.getColor(itemView.getContext(), R.color.miviaje_danger);
            } else if (state == ProductService.State.INITIALIZED) {
                color = ContextCompat.getColor(itemView.getContext(), R.color.miviaje_warning);
            } else {
                color = MaterialColors.getColor(view, com.google.android.material.R.attr.colorOutlineVariant, 0);
            }
            view.setBackgroundTintList(ColorStateList.valueOf(color));
        }

        String getString(int resId, Object... formatArgs) {
            return itemView.getContext().getString(resId, formatArgs);
        }

        String formatDate(LocalDateTime date) {
            return date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG));
        }
    }

    public static String formatMethodValue(View itemView, Product product) {
        int raw = product.getValue();

        if (product.getType() == null) {
            return String.valueOf(raw);
        }
        switch (product.getType()) {
            case WALLET:
                return itemView.getContext().getString(
                        R.string.money_mxn_format,
                        (float) raw / 100);
            case CREDIT:
                return itemView.getContext().getString(
                        R.string.money_mxn_format,
                        (float) -raw / 100);
            case DISCOUNT_TICKETS_1:
            case DISCOUNT_TICKETS_2:
                return itemView.getContext().getResources().getQuantityString(
                        R.plurals.ticket_count, raw, raw);
            default:
                return String.valueOf(raw);
        }
    }
}
