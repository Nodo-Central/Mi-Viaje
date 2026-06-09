package org.nodocentral.miviaje.presentation.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.TextViewCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;

import org.nodocentral.miviaje.R;
import org.nodocentral.miviaje.domain.mimovilidad.Operator;
import org.nodocentral.miviaje.domain.mimovilidad.Route;
import org.nodocentral.miviaje.domain.mimovilidad.RouteMapper;
import org.nodocentral.miviaje.domain.mimovilidad.Station;
import org.nodocentral.miviaje.domain.mimovilidad.StationMapper;
import org.nodocentral.miviaje.domain.mimovilidad.Terminal;
import org.nodocentral.miviaje.domain.mimovilidad.card.Event;
import org.nodocentral.miviaje.domain.mimovilidad.card.Event.TransportType;
import org.nodocentral.miviaje.domain.mimovilidad.card.ProductContract;
import org.nodocentral.miviaje.presentation.TransitTextFormatter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private List<Event> eventList;
    boolean rebelMode;
    boolean showTechnicalData;
    boolean showDebugData;

    public EventAdapter() {
        this.eventList = new ArrayList<>();
    }

    @NonNull
    @Override
    public EventAdapter.EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventAdapter.EventViewHolder(v);
    }

    private String formatDate(LocalDateTime dateTime) {
        FormatStyle dateStyle = FormatStyle.MEDIUM;
        return dateTime.format(DateTimeFormatter.ofLocalizedDate(dateStyle));
    }

    private String formatTime(LocalDateTime dateTime) {
        FormatStyle timeStyle = FormatStyle.MEDIUM;
        return dateTime.format(DateTimeFormatter.ofLocalizedTime(timeStyle));
    }

    @Override
    public void onBindViewHolder(@NonNull EventAdapter.EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(holder.context);
        rebelMode = preferences.getBoolean("setting_rebel_mode", false);
        showTechnicalData = preferences.getBoolean("setting_show_technical_data", false);
        showDebugData = preferences.getBoolean("setting_show_debug_data", false);

        try {
            setEventIcon(event, holder);
            setEventTitle(event, holder);
            setEventDirection(event, holder);
            setEventAmount(event, holder);
            setEventDateTime(event, holder);
            setEventTransferInfo(event, holder);
            setEventInfo(event, holder);
            setEventExtraInfo(holder, event);
            setEventOperator(event, holder);
            setEventDevice(holder, event);
        } catch (Exception e) {
            holder.setEventIcon(R.drawable.ic_more_vert);
            holder.setEventIconTintColor(R.color.miviaje_danger);
            holder.eventTitle.setText(e.getClass().getName());
            holder.eventAmount.setVisibility(TextView.GONE);
            holder.eventDate.setVisibility(TextView.GONE);
            holder.eventInfo.setVisibility(TextView.VISIBLE);
            if (e.getMessage() != null) holder.eventInfo.setText(e.getMessage());
            else holder.eventInfo.setText(R.string.unknown);
            holder.eventDevice.setVisibility(TextView.GONE);
            holder.eventOperator.setVisibility(TextView.GONE);
            holder.eventExtraInfo.setVisibility(TextView.VISIBLE);
            holder.eventExtraInfo.setText("Event details:\n" + event.toString().replace("\n", ""));
            Log.e("EVENT_ADAPTER", event.toString().replace("\n", ""), e);
        }
    }

    void setEventIcon(Event event, EventViewHolder holder) {
        switch (event.getType()) {
            case PRODUCT_USE:
            case TRANSFER:
            case FARE_REFUND:
                switch (event.getTransportType()) {
                    case BUS:
                        holder.setEventIcon(R.drawable.transport_bus);
                        holder.setEventIconTintAttr(com.google.android.material.R.attr.colorPrimaryInverse);
                        break;

                    case BRT_FEEDER_BUS:
                        if (event.getOperator() == Operator.RUTA_LOPEZ_MATEOS) {
                            holder.setEventIcon(R.drawable.transport_brt);
                        } else holder.setEventIcon(R.drawable.transport_bus);
                        switch (event.getOperator()) {
                            case MI_MACRO_CALZADA:
                                holder.setEventIconTintColor(R.color.miviaje_mm_mc);
                                break;

                            case MI_MACRO_PERIFERICO_TRONCAL:
                            case MI_MACRO_PERIFERICO_COMPLEMENTARIO:
                            case BEA:
                            case UNSPECIFIED:
                                holder.setEventIconTintColor(R.color.miviaje_mm_mp);
                                break;

                            case RUTA_LOPEZ_MATEOS:
                                holder.setEventIconTintColor(R.color.miviaje_tp_lm);
                                break;

                            default:
                                holder.setEventIconTintColor(R.color.miviaje_mm_ma);
                                break;
                        }
                        break;

                    case TRAIN_FEEDER_BUS:
                        holder.setEventIcon(R.drawable.transport_bus);
                        holder.setEventIconTintColor(R.color.miviaje_mt_st);
                        break;

                    case BRT:
                    case TRAIN:
                        Route route = StationMapper.getRoute(event);
                        if (route == null) {
                            Log.w("EVENT_ADAPTER", "Route is null!");
                            break;
                        }
                        switch (route) {
                            case LINE_1:
                                holder.setEventIconTintColor(R.color.miviaje_mt_l1);
                                holder.setEventIconText("L1");
                                break;
                            case LINE_2:
                                holder.setEventIconTintColor(R.color.miviaje_mt_l2);
                                holder.setEventIconText("L2");
                                break;
                            case LINE_3:
                                holder.setEventIconTintColor(R.color.miviaje_mt_l3);
                                holder.setEventIconText("L3");
                                break;
                            case LINE_4:
                                holder.setEventIconTintColor(R.color.miviaje_mt_l4);
                                holder.setEventIconText("L4");
                                break;
                            case LINE_5:
                                if (rebelMode) holder.setEventIcon(R.drawable.transport_brt);
                                else holder.setEventIconText("L5");
                                holder.setEventIconTintColor(R.color.miviaje_mm_ma);
                                break;
                            case LINE_6:
                                if (rebelMode) holder.setEventIcon(R.drawable.transport_brt);
                                else holder.setEventIconText("L6");
                                holder.setEventIconTintColor(R.color.miviaje_mm_mc);
                                break;
                            case LINE_7:
                                if (rebelMode) holder.setEventIcon(R.drawable.transport_brt);
                                else holder.setEventIconText("L7");
                                holder.setEventIconTintColor(R.color.miviaje_mm_mp);
                                break;
                            default:
                                break;
                        }
                        break;
                }
                break;

            case PRODUCT_TOP_UP:
                holder.setEventIcon(R.drawable.ic_card_plus);
                holder.setEventIconTintAttr(com.google.android.material.R.attr.colorPrimaryInverse);
                break;

            default:
                holder.setEventIcon(R.drawable.ic_sim_card);
                holder.setEventIconTintAttr(com.google.android.material.R.attr.colorPrimaryInverse);
                holder.eventAmount.setVisibility(TextView.GONE);
                break;
        }
    }

    void setEventTitle(Event event, EventViewHolder holder) {
        String title;
        switch (event.getType()) {
            case PRODUCT_DISTRIBUTION:
            case PAYMENT_METHOD_EMISSION:
            case PRODUCT_TOP_UP:
                Station station = StationMapper.getLocation(event);
                if (station != null) {
                    title = getStationName(holder.context, station);
                } else {
                    title = getEventType(event, holder.context);
                }
                break;
            case PRODUCT_USE:
            case TRANSFER:
            case FARE_REFUND:
                title = getRouteOrStationName(holder, event);
                break;
            default:
                title = getEventType(event, holder.context);
        }
        holder.eventId.setText(holder.getString(R.string.numbered_event_format, event.getEventSequence()));
        holder.eventTitle.setText(title);
    }

    void setEventDirection(Event event, EventViewHolder holder) {
        String direction;
        Terminal terminal = StationMapper.getTerminal(event);
        if (terminal != null) {
            direction = holder.getString(R.string.transport_direction,
                    getRawStationName(holder.context, terminal.station));
            holder.eventDirection.setVisibility(TextView.VISIBLE);
            holder.eventDirection.setText(direction);
        } else {
            holder.eventDirection.setVisibility(TextView.GONE);
        }
    }

    void setEventAmount(Event event, EventViewHolder holder) {
        switch (event.getType()) {
            case PRODUCT_USE:
            case TRANSFER:
            case FARE_REFUND:
            case PRODUCT_TOP_UP:
            case PAYMENT_METHOD_EMISSION:
                holder.eventAmount.setVisibility(TextView.VISIBLE);
                int amount = event.getAmount();
                ProductContract.ValueUnit valueUnit = event.getValueUnit();
                String amountText;
                Integer amountIcon;
                if (amount > 0) {
                    int color;
                    if (event.isPayment())
                        color = holder.getAttrColor(com.google.android.material.R.attr.colorOnSurface);
                    else
                        color = holder.getAttrColor(com.google.android.material.R.attr.colorTertiary);
                    holder.eventAmount.setTextColor(color);
                    TextViewCompat.setCompoundDrawableTintList(holder.eventAmount, ColorStateList.valueOf(color));
                    if (valueUnit == ProductContract.ValueUnit.MXN_CENT) {
                        amountText = holder.context.getString(
                                (event.isPayment()) ? R.string.money_mxn_format :
                                        R.string.money_mxn_plus_format,
                                ((float) amount) / 100);
                        amountIcon = R.drawable.ic_coin;
                    } else if (valueUnit == ProductContract.ValueUnit.TICKET) {
                        amountText = holder.context.getResources().getQuantityString(
                                (event.isPayment()) ? R.plurals.ticket_count :
                                        R.plurals.ticket_count_plus,
                                amount,
                                amount);
                        amountIcon = R.drawable.ic_ticket;
                    } else {
                        amountText = String.valueOf(amount);
                        amountIcon = null;
                    }
                } else {
                    holder.eventAmount.setTextColor(holder.getAttrColor(com.google.android.material.R.attr.colorTertiary));
                    amountText = holder.context.getString(R.string.free_of_charge);
                    amountIcon = null;
                }
                holder.eventAmount.setText(amountText);
                Drawable drawable = amountIcon != null ? holder.getDrawable(amountIcon) : null;
                holder.eventAmount.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        null, null, drawable, null);
                break;
            default:
                holder.eventAmount.setVisibility(TextView.GONE);
                break;
        }
    }

    void setEventTransferInfo(Event event, EventViewHolder holder) {
        LocalDateTime transferOffset = event.getTransferLimit();
        if (transferOffset != null && transferOffset.isAfter(event.getEventDateTime())) {
            holder.eventTransferInfo.setVisibility(TextView.VISIBLE);
            holder.eventTransferInfo.setText(holder.getString(R.string.transfer_limit,
                    formatTime(transferOffset)));
        } else {
            holder.eventTransferInfo.setVisibility(TextView.GONE);
        }
    }

    void setEventDateTime(Event event, EventViewHolder holder) {
        String date, time;
        if (event.getEventDateTime() != null) {
            date = formatDate(event.getEventDateTime());
            time = formatTime(event.getEventDateTime());
        } else {
            date = holder.getString(R.string.unknown);
            time = null;
        }
        holder.eventDate.setText(date);
        if (time != null) {
            holder.eventTime.setVisibility(TextView.VISIBLE);
            holder.eventTime.setText(time);
        } else {
            holder.eventTime.setVisibility(TextView.GONE);
        }
    }

    void setEventInfo(Event event, EventViewHolder holder) {
        String eventTypeString = getEventType(event, holder.context);
        String deviceType;

        switch (event.getDeviceType()) {
            case CUSTOMER_SERVICE:
                deviceType = holder.getString(R.string.device_attention_module);
                break;
            case SMARTPHONE:
                deviceType = holder.getString(R.string.device_smartphone);
                break;
            case POS_MACHINE:
                deviceType = holder.getString(R.string.device_point_of_sale);
                break;
            case FARE_VALIDATOR:
                switch (event.getTransportType()) {
                    case TRAIN:
                    case BRT:
                        deviceType = holder.getString(R.string.device_turnstile);
                        break;
                    default:
                        deviceType = holder.getString(R.string.device_validator);
                        break;
                }
                break;
            case TICKET_MACHINE:
                deviceType = holder.getString(R.string.device_ticket_vending_machine);
                break;
            case UNSPECIFIED:
            default:
                deviceType = null;
                break;
        }

        StringBuilder builder = new StringBuilder();
        switch (event.getType()) {
            case PAYMENT_METHOD_EMISSION:
            case PRODUCT_DISTRIBUTION:
            case PRODUCT_TOP_UP:
                if (StationMapper.getLocation(event) != null) {
                    builder.append(eventTypeString);
                }
                if (deviceType != null) {
                    if (builder.length() > 0) builder.append("\n");
                    builder.append(deviceType);
                }
                if (builder.length() > 0) {
                    holder.eventInfo.setVisibility(TextView.VISIBLE);
                    holder.eventInfo.setText(builder.toString());
                } else {
                    holder.eventInfo.setVisibility(TextView.GONE);
                }
                break;

            default:
                if (event.getType() != Event.Type.PRODUCT_USE) {
                    builder.append(eventTypeString);
                    if (deviceType != null && event.getTransportType() != Event.TransportType.TRAIN && event.getTransportType() != Event.TransportType.BRT) {
                        builder.append("\n");
                        builder.append(deviceType);
                    }
                }
                if (builder.length() > 0) {
                    holder.eventInfo.setVisibility(TextView.VISIBLE);
                    holder.eventInfo.setText(builder.toString());
                } else {
                    holder.eventInfo.setVisibility(TextView.GONE);
                    holder.eventInfo.setText(null);
                }
                break;
        }
    }

    void setEventDevice(EventViewHolder holder, Event event) {
        String eventDevice;
        switch (event.getType()) {
            case PRODUCT_USE:
            case TRANSFER:
            case FARE_REFUND:
                switch (event.getTransportType()) {
                    case TRAIN:
                    case BRT:
                        if (showTechnicalData)
                            eventDevice = holder.context.getString(R.string.device_turnstile_numbered_debug, StationMapper.getValidator(event), event.getDeviceId());
                        else
                            eventDevice = holder.context.getString(R.string.device_turnstile_numbered, StationMapper.getValidator(event));
                        break;
                    default:
                        eventDevice = holder.context.getString(R.string.bus_unit_id_format, StationMapper.getValidator(event));
                        break;
                }
                break;

            case PAYMENT_METHOD_EMISSION:
            case PRODUCT_DISTRIBUTION:
            case PRODUCT_TOP_UP:
                if (event.getDeviceId() != 0) {
                    switch (event.getDeviceType()) {
                        case TICKET_MACHINE:
                            eventDevice = holder.context.getString(R.string.vrt_id_format, event.getDeviceId());
                            break;

                        case POS_MACHINE:
                        case SMARTPHONE:
                            switch (event.getDeviceId()) {
                                case 39321:
                                    eventDevice = holder.getString(R.string.company_sfinx);
                                    break;
                                default:
                                    eventDevice = holder.getString(R.string.event_location, event.getDeviceId());
                                    break;
                            }
                            break;

                        default:
                            eventDevice = holder.context.getString(R.string.device_id_format, String.valueOf(event.getDeviceId()));
                            break;
                    }
                } else eventDevice = null;
                break;

            default:
                if (event.getDeviceId() != 0)
                    eventDevice = holder.context.getString(R.string.device_id_format, String.valueOf(event.getDeviceId()));
                else eventDevice = null;
                break;
        }

        if (eventDevice != null) {
            holder.eventDevice.setVisibility(TextView.VISIBLE);
            holder.eventDevice.setText(eventDevice);
        } else {
            holder.eventDevice.setVisibility(TextView.GONE);
        }

        holder.techBlock.setVisibility(showTechnicalData ? View.VISIBLE : View.GONE);
    }

    void setEventOperator(Event event, EventViewHolder holder) {
        Operator operator = Operator.fromInt(event.getEntityId());
        String operatorName;

        if (operator != null && operator != Operator.UNSPECIFIED) {
            operatorName = operator.getName();
        } else if (event.getEntityId() != 0 && showTechnicalData) {
            operatorName = holder.getString(R.string.operator_id_format, event.getEntityId());
        } else {
            operatorName = null;
        }

        if (operatorName != null) {
            holder.eventOperator.setVisibility(TextView.VISIBLE);
            holder.eventOperator.setText(operatorName);
        } else {
            holder.eventOperator.setVisibility(TextView.GONE);
        }
    }

    public void setEventExtraInfo(EventViewHolder holder, Event event) {
        LocalDateTime transferOffsetObj = event.getTransferLimit();

        // TODO: Improve displaying of technical data
        if (showTechnicalData) {
            String[] data = new String[]{
                    "SamId: " + Long.toHexString(event.getSamId()).toUpperCase(),
                    "SamSeq: " + Long.toHexString(event.getSamSequence()).toUpperCase(),
                    "RoutID: " + event.getRouteId(),
                    "DevcID: " + event.getDeviceId(),
                    "Oprtr(Ent): " + event.getEntityId(),
                    "Location: " + event.getLocationId(),
                    "TsprtTyp: " + Objects.requireNonNullElse(event.getRawTransportType().name(), "NULL") + " (" + Objects.requireNonNullElse(event.getTransportType().name(), "NULL") + ")",
                    "TsfrCnt: " + event.getTransferCount(),
                    "TsfrLim: " + event.getTransferLimitTimestamp(),
                    "Amount: " + event.getAmount(),
                    "PassbkCnt: " + event.getPassbackCount(),
                    "PrdID: " + event.getProductId(),
                    "PrdPtr: " + event.getProductPointer(),
            };
            holder.techLine1.setText(String.join(" • ", data));
            holder.techBlock.setVisibility(View.VISIBLE);
        } else {
            holder.techBlock.setVisibility(View.GONE);
        }

        if (showDebugData) {
            holder.eventExtraInfo.setVisibility(TextView.VISIBLE);
            holder.eventExtraInfo.setText(event.toString());
        } else {
            holder.eventExtraInfo.setVisibility(TextView.GONE);
        }
    }

    String getEventType(Event event, Context context) {
        String eventTitle;
        switch (event.getType()) {
            case PRODUCT_DISTRIBUTION:
                eventTitle = context.getString(R.string.event_card_emission);
                break;
            case PRODUCT_USE:
                eventTitle = context.getString(R.string.event_fare_payment);
                break;
            case PRODUCT_TOP_UP:
                eventTitle = context.getString(R.string.event_top_up);
                break;
            case TRANSFER:
                eventTitle = context.getString(R.string.event_transfer);
                break;
            case FARE_REFUND:
                eventTitle = context.getString(R.string.event_refund);
                break;
            case PAYMENT_METHOD_EMISSION:
                eventTitle = context.getString(R.string.event_card_activation);
                break;
            case UNSPECIFIED:
            default:
                eventTitle = context.getString(R.string.unspecified);
                break;
        }
        return eventTitle;
    }

    String getRouteOrStationName(EventAdapter.EventViewHolder holder, Event event) {
        TransportType transportType = event.getTransportType();
        if (transportType == TransportType.TRAIN || transportType == TransportType.BRT) {
            return getStationName(holder.context, event);
        } else {
            return getRouteName(holder.context, event);
        }
    }

    String getRouteName(Context context, Event event) {
        Route route = RouteMapper.fromInt(event.getEntityId(), event.getRouteId(), event.getDeviceId(), event.getTransportType());
        if (route != null) {
            return getRouteName(context, route);
        } else {
            return context.getString(R.string.transport_complementary, event.getRouteId(), "");
        }
    }

    String getRouteName(Context context, Route route) {
        return TransitTextFormatter.getRouteName(context, route, rebelMode);
    }

    String getStationName(Context context, Event event) {
        Station station = StationMapper.getStation(event);
        Route route = StationMapper.getRoute(event);
        if (station != null) {
            return getStationName(context, station);
        } else if (route == Route.LINE_7) {
            String stationId = Integer.toHexString(StationMapper.getStationId(event)).toUpperCase();
            return context.getString(R.string.transport_station, stationId);
        } else if (route == Route.LINE_4) {
            return String.valueOf(StationMapper.getStationId(event));
        } else {
            return context.getString(R.string.transport_station, String.valueOf(event.getRouteId()));
        }
    }

    String getStationName(Context context, Station station) {
        return TransitTextFormatter.getStationName(context, station);
    }

    String getRawStationName(Context context, Station station) {
        return TransitTextFormatter.getRawStationName(context, station);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public void updateEvents(List<Event> eventList) {
        this.eventList = eventList;
        notifyDataSetChanged();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        public Context context;
        public ConstraintLayout eventIconLayout;
        public TextView eventIconText;
        public TextView eventId;
        public TextView eventTitle;
        public TextView eventAmount;
        public TextView eventDate;
        public TextView eventTime;
        public TextView eventDirection;
        public TextView eventTransferInfo;
        public TextView eventInfo;
        public TextView eventExtraInfo;
        public TextView eventOperator;
        public TextView eventDevice;

        public View techBlock;
        public TextView techLine1;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            eventId = itemView.findViewById(R.id.item_event_id);
            eventTitle = itemView.findViewById(R.id.item_event_title);
            eventAmount = itemView.findViewById(R.id.item_event_amount);
            eventDate = itemView.findViewById(R.id.item_event_date);
            eventTime = itemView.findViewById(R.id.item_event_time);
            eventDirection = itemView.findViewById(R.id.item_event_direction);
            eventTransferInfo = itemView.findViewById(R.id.item_event_transfer_limit);
            eventInfo = itemView.findViewById(R.id.item_event_info);
            eventExtraInfo = itemView.findViewById(R.id.item_event_extra_info);
            eventIconLayout = itemView.findViewById(R.id.item_event_transport_icon_layout);
            eventIconText = itemView.findViewById(R.id.item_event_transport_icon);
            eventOperator = itemView.findViewById(R.id.item_event_operator);
            eventDevice = itemView.findViewById(R.id.item_event_device);

            techBlock = itemView.findViewById(R.id.item_event_tech_block);
            techLine1 = itemView.findViewById(R.id.item_event_tech_line1);

        }

        public String getString(int id, Object... args) {
            return context.getString(id, args);
        }

        Drawable getDrawable(int id) {
            return AppCompatResources.getDrawable(context, id);
        }

        void setEventIcon(int drawableId) {
            eventIconText.setForeground(getDrawable(drawableId));
            eventIconText.setText(null);
        }

        int getAttrColor(int attr) {
            return MaterialColors.getColor(context, attr, 0);
        }

        void setEventIconTintAttr(Integer attr) {
            ColorStateList list = attr == null ? null :
                    ColorStateList.valueOf(getAttrColor(attr));
            eventIconLayout.setBackgroundTintList(list);
        }

        void setEventIconTintColor(Integer colorRes) {
            ColorStateList list = colorRes == null ? null :
                    AppCompatResources.getColorStateList(context, colorRes);
            eventIconLayout.setBackgroundTintList(list);
        }

        void setEventIconText(String text) {
            eventIconText.setForeground(null);
            eventIconText.setText(text);
        }

        void setEventIconText(int stringId) {
            setEventIconText(getString(stringId));
        }
    }
}
