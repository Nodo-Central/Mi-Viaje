package org.nodocentral.miviaje.presentation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.core.util.Pair;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;

import org.nodocentral.miviaje.R;
import org.nodocentral.miviaje.data.repository.EventRepository;
import org.nodocentral.miviaje.data.room.MiViajeDatabase;
import org.nodocentral.miviaje.domain.mimovilidad.Operator;
import org.nodocentral.miviaje.domain.mimovilidad.Route;
import org.nodocentral.miviaje.domain.mimovilidad.card.Event;
import org.nodocentral.miviaje.domain.mimovilidad.filters.EventFilter;
import org.nodocentral.miviaje.domain.mimovilidad.filters.EventFilterCriteria;
import org.nodocentral.miviaje.presentation.adapters.EventAdapter;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventsActivity extends BaseActivity {
    private final EnumSet<Event.TransportType> activeTransportTypes = EnumSet.noneOf(Event.TransportType.class);
    private final EnumSet<Route> activeRoutes = EnumSet.noneOf(Route.class);
    private final EnumSet<Operator> activeOperators = EnumSet.noneOf(Operator.class);
    private final EnumSet<Event.Type> activeEventTypes = EnumSet.noneOf(Event.Type.class);
    private final ExecutorService eventExecutor = Executors.newSingleThreadExecutor();
    private List<Event> allEvents = new ArrayList<>();
    private EventAdapter eventAdapter;
    private int filterRequestId;
    private int activeDateFilterId = View.NO_ID;
    private LocalDate activeCustomDateStart;
    private LocalDate activeCustomDateEnd;
    private Chip allFilterChip;
    private Chip transportFilterChip;
    private Chip eventTypeFilterChip;
    private Chip dateFilterChip;
    private Chip moreFilterChip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);
        super.setToolbar(true);
        setupFilterToggles();
        configureInsets();

        long cardUid = getIntent().getLongExtra("CARD_UID", -1L);
        if (cardUid == -1L) {
            finish();
            return;
        }

        eventAdapter = new EventAdapter();

        RecyclerView cardRecycler = findViewById(R.id.events_recycler);
        cardRecycler.setLayoutManager(new LinearLayoutManager(this));
        cardRecycler.setAdapter(eventAdapter);
        MiViajeDatabase database = MiViajeDatabase.getInstance(getApplicationContext());
        EventRepository eventRepository = new EventRepository(database);
        eventExecutor.execute(() -> {
            List<Event> events = eventRepository.getEventsForCard(cardUid);
            List<Event> loadedEvents = events != null ? new ArrayList<>(events) : new ArrayList<>();
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                allEvents = loadedEvents;
                applyActiveFilters();
            });
        });
    }

    private void configureInsets() {
        int navAndCutoutTypes = WindowInsetsCompat.Type.navigationBars()
                | WindowInsetsCompat.Type.displayCutout();
        applyInsetsToPadding(findViewById(R.id.events_filter_scroll), navAndCutoutTypes, true, false, true, false);
        applyInsetsToPadding(findViewById(R.id.events_recycler), navAndCutoutTypes, true, false, true, true);
    }

    private void setupFilterToggles() {
        allFilterChip = findViewById(R.id.events_filter_all);
        if (allFilterChip != null) {
            allFilterChip.setOnClickListener(v -> clearActiveFilters());
        }
        transportFilterChip = findViewById(R.id.events_filter_transport);
        if (transportFilterChip != null) {
            transportFilterChip.setOnClickListener(v -> showEventFiltersBottomSheet());
        }
        eventTypeFilterChip = findViewById(R.id.events_filter_event_type);
        if (eventTypeFilterChip != null) {
            eventTypeFilterChip.setOnClickListener(v -> showEventFiltersBottomSheet());
        }
        dateFilterChip = findViewById(R.id.events_filter_date);
        if (dateFilterChip != null) {
            dateFilterChip.setOnClickListener(v -> showEventFiltersBottomSheet());
        }
        moreFilterChip = findViewById(R.id.events_filter_more);
        if (moreFilterChip != null) {
            moreFilterChip.setOnClickListener(v -> showEventFiltersBottomSheet());
        }
        updateTopFilterState();
    }

    private void showEventFiltersBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_event_filters, null, false);
        dialog.setContentView(sheetView);

        EnumSet<Event.TransportType> draftTransportTypes = copyOf(activeTransportTypes, Event.TransportType.class);
        EnumSet<Route> draftRoutes = copyOf(activeRoutes, Route.class);
        EnumSet<Operator> draftOperators = copyOf(activeOperators, Operator.class);
        EnumSet<Event.Type> draftEventTypes = copyOf(activeEventTypes, Event.Type.class);
        int[] draftDateFilterId = {activeDateFilterId};
        LocalDate[] draftCustomDateStart = {activeCustomDateStart};
        LocalDate[] draftCustomDateEnd = {activeCustomDateEnd};
        boolean[] suppressDateListener = {false};

        populateTransportChips(sheetView, draftTransportTypes);
        populateRouteChips(sheetView, draftRoutes);
        populateOperatorChips(sheetView, draftOperators);
        populateEventTypeChips(sheetView, draftEventTypes);
        setupDateChipState(
                sheetView,
                draftDateFilterId,
                draftCustomDateStart,
                draftCustomDateEnd,
                suppressDateListener
        );

        MaterialButton clearAllButton = sheetView.findViewById(R.id.event_filters_clear_all);
        if (clearAllButton != null) {
            clearAllButton.setOnClickListener(v -> {
                draftTransportTypes.clear();
                draftRoutes.clear();
                draftOperators.clear();
                draftEventTypes.clear();
                draftDateFilterId[0] = View.NO_ID;
                draftCustomDateStart[0] = null;
                draftCustomDateEnd[0] = null;
                refreshCheckedStates(
                        sheetView,
                        draftTransportTypes,
                        draftRoutes,
                        draftOperators,
                        draftEventTypes,
                        draftDateFilterId[0],
                        draftCustomDateStart,
                        draftCustomDateEnd
                );
            });
        }

        MaterialButton cancelButton = sheetView.findViewById(R.id.event_filters_cancel);
        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> dialog.dismiss());
        }

        MaterialButton applyButton = sheetView.findViewById(R.id.event_filters_apply);
        if (applyButton != null) {
            applyButton.setOnClickListener(v -> {
                activeTransportTypes.clear();
                activeTransportTypes.addAll(draftTransportTypes);
                activeRoutes.clear();
                activeRoutes.addAll(draftRoutes);
                activeOperators.clear();
                activeOperators.addAll(draftOperators);
                activeEventTypes.clear();
                activeEventTypes.addAll(draftEventTypes);
                activeDateFilterId = draftDateFilterId[0];
                activeCustomDateStart = draftCustomDateStart[0];
                activeCustomDateEnd = draftCustomDateEnd[0];
                updateTopFilterState();
                applyActiveFilters();
                dialog.dismiss();
            });
        }

        dialog.setOnDismissListener(d -> updateTopFilterState());
        dialog.show();
    }

    private void populateTransportChips(View sheetView, EnumSet<Event.TransportType> draftTransportTypes) {
        ChipGroup group = sheetView.findViewById(R.id.event_filters_transport_group);
        if (group == null) {
            return;
        }
        group.removeAllViews();
        for (Event.TransportType transportType : Event.TransportType.values()) {
            if (transportType == Event.TransportType.UNSPECIFIED) {
                continue;
            }
            Chip chip = createFilterChip(getTransportTypeLabel(transportType), draftTransportTypes.contains(transportType));
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> updateEnumSet(draftTransportTypes, transportType, isChecked));
            group.addView(chip);
        }
    }

    private void populateRouteChips(View sheetView, EnumSet<Route> draftRoutes) {
        ChipGroup group = sheetView.findViewById(R.id.event_filters_route_group);
        if (group == null) {
            return;
        }
        group.removeAllViews();
        for (Route route : Route.getRapidTransitLines()) {
            Chip chip = createFilterChip(getRouteLabel(route), draftRoutes.contains(route));
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> updateEnumSet(draftRoutes, route, isChecked));
            group.addView(chip);
        }
    }

    private void populateOperatorChips(View sheetView, EnumSet<Operator> draftOperators) {
        ChipGroup group = sheetView.findViewById(R.id.event_filters_operator_group);
        if (group == null) {
            return;
        }
        group.removeAllViews();
        for (Operator operator : Operator.values()) {
            if (operator == Operator.UNSPECIFIED) {
                continue;
            }
            Chip chip = createFilterChip(getOperatorLabel(operator), draftOperators.contains(operator));
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> updateEnumSet(draftOperators, operator, isChecked));
            group.addView(chip);
        }
    }

    private void populateEventTypeChips(View sheetView, EnumSet<Event.Type> draftEventTypes) {
        ChipGroup group = sheetView.findViewById(R.id.event_filters_event_type_group);
        if (group == null) {
            return;
        }
        group.removeAllViews();
        for (Event.Type eventType : Event.Type.values()) {
            if (!isDisplayableEventType(eventType)) {
                continue;
            }
            Chip chip = createFilterChip(getEventTypeLabel(eventType), draftEventTypes.contains(eventType));
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> updateEnumSet(draftEventTypes, eventType, isChecked));
            group.addView(chip);
        }
    }

    private void setupDateChipState(View sheetView,
                                    int[] draftDateFilterId,
                                    LocalDate[] draftCustomDateStart,
                                    LocalDate[] draftCustomDateEnd,
                                    boolean[] suppressDateListener) {
        ChipGroup dateGroup = sheetView.findViewById(R.id.event_filters_date_group);
        if (dateGroup == null) {
            return;
        }
        if (draftDateFilterId[0] != View.NO_ID) {
            dateGroup.check(draftDateFilterId[0]);
        } else {
            dateGroup.clearCheck();
        }
        updateDateRangeInput(sheetView, draftCustomDateStart[0], draftCustomDateEnd[0]);
        dateGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (suppressDateListener[0]) {
                return;
            }
            int selectedId = checkedIds.isEmpty() ? View.NO_ID : checkedIds.get(0);
            if (selectedId == R.id.event_filters_date_range) {
                showDateRangePicker(
                        sheetView,
                        dateGroup,
                        draftDateFilterId,
                        draftCustomDateStart,
                        draftCustomDateEnd,
                        suppressDateListener
                );
            } else {
                draftDateFilterId[0] = selectedId;
                draftCustomDateStart[0] = null;
                draftCustomDateEnd[0] = null;
                updateDateRangeInput(sheetView, null, null);
            }
        });

        View dateRangeInputLayout = sheetView.findViewById(R.id.event_filters_date_range_input_layout);
        View dateRangeInput = sheetView.findViewById(R.id.event_filters_date_range_input);
        View.OnClickListener openDatePicker = v -> {
            if (draftDateFilterId[0] == R.id.event_filters_date_range) {
                showDateRangePicker(
                        sheetView,
                        dateGroup,
                        draftDateFilterId,
                        draftCustomDateStart,
                        draftCustomDateEnd,
                        suppressDateListener
                );
            } else {
                dateGroup.check(R.id.event_filters_date_range);
            }
        };
        if (dateRangeInputLayout != null) {
            dateRangeInputLayout.setOnClickListener(openDatePicker);
        }
        if (dateRangeInput != null) {
            dateRangeInput.setOnClickListener(openDatePicker);
        }
    }

    private void refreshCheckedStates(View sheetView,
                                      EnumSet<Event.TransportType> transportTypes,
                                      EnumSet<Route> routes,
                                      EnumSet<Operator> operators,
                                      EnumSet<Event.Type> eventTypes,
                                      int dateFilterId,
                                      LocalDate[] draftCustomDateStart,
                                      LocalDate[] draftCustomDateEnd) {
        populateTransportChips(sheetView, transportTypes);
        populateRouteChips(sheetView, routes);
        populateOperatorChips(sheetView, operators);
        populateEventTypeChips(sheetView, eventTypes);
        ChipGroup dateGroup = sheetView.findViewById(R.id.event_filters_date_group);
        if (dateGroup != null) {
            if (dateFilterId == View.NO_ID) {
                dateGroup.clearCheck();
            } else {
                dateGroup.check(dateFilterId);
            }
        }
        updateDateRangeInput(sheetView, draftCustomDateStart[0], draftCustomDateEnd[0]);
    }

    private void showDateRangePicker(View sheetView,
                                     ChipGroup dateGroup,
                                     int[] draftDateFilterId,
                                     LocalDate[] draftCustomDateStart,
                                     LocalDate[] draftCustomDateEnd,
                                     boolean[] suppressDateListener) {
        int previousDateFilterId = draftDateFilterId[0];
        LocalDate previousStart = draftCustomDateStart[0];
        LocalDate previousEnd = draftCustomDateEnd[0];
        draftDateFilterId[0] = R.id.event_filters_date_range;

        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText(R.string.events_filter_date_range_picker_title);
        if (draftCustomDateStart[0] != null && draftCustomDateEnd[0] != null) {
            builder.setSelection(new Pair<>(
                    toPickerUtcMillis(draftCustomDateStart[0]),
                    toPickerUtcMillis(draftCustomDateEnd[0])
            ));
        }

        MaterialDatePicker<Pair<Long, Long>> picker = builder.build();
        boolean[] confirmed = {false};
        boolean[] restored = {false};
        Runnable restorePreviousSelection = () -> {
            if (confirmed[0] || restored[0]) {
                return;
            }
            restored[0] = true;
            draftDateFilterId[0] = previousDateFilterId;
            draftCustomDateStart[0] = previousStart;
            draftCustomDateEnd[0] = previousEnd;
            suppressDateListener[0] = true;
            if (previousDateFilterId == View.NO_ID) {
                dateGroup.clearCheck();
            } else {
                dateGroup.check(previousDateFilterId);
            }
            suppressDateListener[0] = false;
            updateDateRangeInput(sheetView, previousStart, previousEnd);
        };

        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection == null || selection.first == null || selection.second == null) {
                return;
            }
            confirmed[0] = true;
            draftDateFilterId[0] = R.id.event_filters_date_range;
            draftCustomDateStart[0] = fromPickerUtcMillis(selection.first);
            draftCustomDateEnd[0] = fromPickerUtcMillis(selection.second);
            suppressDateListener[0] = true;
            dateGroup.check(R.id.event_filters_date_range);
            suppressDateListener[0] = false;
            updateDateRangeInput(sheetView, draftCustomDateStart[0], draftCustomDateEnd[0]);
        });
        picker.addOnNegativeButtonClickListener(v -> restorePreviousSelection.run());
        picker.addOnCancelListener(v -> restorePreviousSelection.run());
        picker.addOnDismissListener(v -> restorePreviousSelection.run());
        picker.show(getSupportFragmentManager(), "event_date_range_picker");
    }

    private void updateDateRangeInput(View sheetView, LocalDate startDate, LocalDate endDate) {
        TextInputEditText dateRangeInput = sheetView.findViewById(R.id.event_filters_date_range_input);
        if (dateRangeInput == null) {
            return;
        }
        if (startDate != null && endDate != null) {
            dateRangeInput.setText(formatDateRange(startDate, endDate));
        } else {
            dateRangeInput.setText(null);
        }
    }

    private String formatDateRange(LocalDate startDate, LocalDate endDate) {
        DateTimeFormatter formatter = DateTimeFormatter
                .ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(Locale.getDefault());
        return startDate.format(formatter) + " — " + endDate.format(formatter);
    }

    private long toPickerUtcMillis(LocalDate date) {
        return date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
    }

    private LocalDate fromPickerUtcMillis(long millis) {
        return Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate();
    }

    private Chip createFilterChip(String label, boolean checked) {
        Chip chip = (Chip) LayoutInflater.from(this).inflate(R.layout.item_event_filter_chip, null, false);
        chip.setText(label);
        chip.setChecked(checked);
        return chip;
    }

    private <T extends Enum<T>> EnumSet<T> copyOf(EnumSet<T> source, Class<T> enumClass) {
        return source.isEmpty() ? EnumSet.noneOf(enumClass) : EnumSet.copyOf(source);
    }

    private <T extends Enum<T>> void updateEnumSet(EnumSet<T> values, T value, boolean enabled) {
        if (enabled) {
            values.add(value);
        } else {
            values.remove(value);
        }
    }

    private void updateTopFilterState() {
        boolean hasActiveFilters = !activeTransportTypes.isEmpty() || !activeRoutes.isEmpty() || !activeOperators.isEmpty() || !activeEventTypes.isEmpty() || activeDateFilterId != View.NO_ID;
        if (allFilterChip != null) {
            allFilterChip.setText(R.string.events_filter_all);
            allFilterChip.setChecked(!hasActiveFilters);
        }
        updateTopChip(transportFilterChip, R.string.events_filter_transport, activeTransportTypes, this::getTransportTypeLabel);
        updateTopChip(eventTypeFilterChip, R.string.events_filter_event_type, activeEventTypes, this::getEventTypeLabel);
        updateDateTopChip();
        updateMoreTopChip();
    }

    private <T extends Enum<T>> void updateTopChip(Chip chip, int defaultLabelResId, EnumSet<T> values, LabelProvider<T> labelProvider) {
        if (chip == null) {
            return;
        }
        chip.setChecked(!values.isEmpty());
        if (values.size() == 1) {
            chip.setText(labelProvider.getLabel(values.iterator().next()));
        } else {
            chip.setText(defaultLabelResId);
        }
    }

    private void updateDateTopChip() {
        if (dateFilterChip == null) {
            return;
        }
        boolean hasDateFilter = activeDateFilterId != View.NO_ID;
        dateFilterChip.setChecked(hasDateFilter);
        dateFilterChip.setText(hasDateFilter ? getDateFilterLabel(activeDateFilterId) : getString(R.string.events_filter_date));
    }

    private void updateMoreTopChip() {
        if (moreFilterChip != null) {
            boolean hasMoreFilters = !activeRoutes.isEmpty() || !activeOperators.isEmpty();
            moreFilterChip.setChecked(hasMoreFilters);
            if (activeRoutes.size() == 1 && activeOperators.isEmpty()) {
                moreFilterChip.setText(getRouteLabel(activeRoutes.iterator().next()));
            } else if (activeOperators.size() == 1 && activeRoutes.isEmpty()) {
                moreFilterChip.setText(getOperatorLabel(activeOperators.iterator().next()));
            } else {
                moreFilterChip.setText(R.string.events_filter_more);
            }
        }
    }

    private void clearActiveFilters() {
        activeTransportTypes.clear();
        activeRoutes.clear();
        activeOperators.clear();
        activeEventTypes.clear();
        activeDateFilterId = View.NO_ID;
        activeCustomDateStart = null;
        activeCustomDateEnd = null;
        updateTopFilterState();
        applyActiveFilters();
    }

    private void applyActiveFilters() {
        if (eventAdapter == null) {
            return;
        }
        EventFilterCriteria criteria = buildFilterCriteria();
        List<Event> sourceEvents = new ArrayList<>(allEvents);
        int requestId = ++filterRequestId;
        eventExecutor.execute(() -> {
            List<Event> filteredEvents = EventFilter.filter(sourceEvents, criteria);
            runOnUiThread(() -> {
                if (requestId != filterRequestId || isFinishing() || isDestroyed()) {
                    return;
                }
                eventAdapter.updateEvents(filteredEvents);
            });
        });
    }

    private EventFilterCriteria buildFilterCriteria() {
        EventFilterCriteria.Builder builder = EventFilterCriteria.builder()
                .transportTypes(activeTransportTypes)
                .routes(activeRoutes)
                .operators(activeOperators)
                .eventTypes(activeEventTypes);
        EventFilterCriteria.DateRange dateRange = getActiveDateRange();
        if (dateRange != null) {
            builder.dateRange(dateRange);
        }
        return builder.build();
    }

    private EventFilterCriteria.DateRange getActiveDateRange() {
        LocalDate today = LocalDate.now();
        if (activeDateFilterId == R.id.event_filters_date_today) {
            return EventFilterCriteria.DateRange.forDay(today);
        } else if (activeDateFilterId == R.id.event_filters_date_week) {
            DayOfWeek firstDayOfWeek = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
            return EventFilterCriteria.DateRange.forWeek(today, firstDayOfWeek);
        } else if (activeDateFilterId == R.id.event_filters_date_month) {
            return EventFilterCriteria.DateRange.forMonth(today);
        } else if (activeDateFilterId == R.id.event_filters_date_range
                && activeCustomDateStart != null
                && activeCustomDateEnd != null) {
            return EventFilterCriteria.DateRange.forDateSpan(activeCustomDateStart, activeCustomDateEnd);
        } else {
            return null;
        }
    }

    private String getTransportTypeLabel(Event.TransportType transportType) {
        switch (transportType) {
            case BUS:
                return getString(R.string.transport_bus);
            case TRAIN:
                return getString(R.string.transport_train);
            case TRAIN_FEEDER_BUS:
                return getString(R.string.transport_train_feeder);
            case BRT_FEEDER_BUS:
                return getString(R.string.transport_brt_feeder);
            case BRT:
                return getString(R.string.transport_brt);
            case UNSPECIFIED:
            default:
                return getString(R.string.unspecified);
        }
    }

    private String getRouteLabel(Route route) {
        return "L" + route.getId();
    }

    private String getDateFilterLabel(@IdRes int dateFilterId) {
        if (dateFilterId == R.id.event_filters_date_today) {
            return getString(R.string.events_filter_date_today);
        } else if (dateFilterId == R.id.event_filters_date_week) {
            return getString(R.string.events_filter_date_week);
        } else if (dateFilterId == R.id.event_filters_date_month) {
            return getString(R.string.events_filter_date_month);
        } else if (dateFilterId == R.id.event_filters_date_range) {
            return getString(R.string.events_filter_date_range);
        } else {
            return getString(R.string.events_filter_date);
        }
    }

    private String getOperatorLabel(Operator operator) {
        String name = operator.getName();
        return name != null ? name : operator.name().replace('_', ' ');
    }

    private boolean isDisplayableEventType(Event.Type eventType) {
        switch (eventType) {
            case PRODUCT_DISTRIBUTION:
            case PRODUCT_USE:
            case PRODUCT_TOP_UP:
            case TRANSFER:
            case REFUND:
            case FARE_REFUND:
            case PAYMENT_METHOD_EMISSION:
                return true;
            case UNSPECIFIED:
            default:
                return false;
        }
    }

    private String getEventTypeLabel(Event.Type eventType) {
        switch (eventType) {
            case PRODUCT_DISTRIBUTION:
                return getString(R.string.event_card_activation);
            case PRODUCT_USE:
                return getString(R.string.event_fare_payment);
            case PRODUCT_TOP_UP:
                return getString(R.string.event_top_up);
            case TRANSFER:
                return getString(R.string.event_transfer);
            case REFUND:
                return getString(R.string.event_refund);
            case FARE_REFUND:
                return getString(R.string.event_fare_refund);
            case PAYMENT_METHOD_EMISSION:
                return getString(R.string.event_card_emission);
            case UNSPECIFIED:
            default:
                return getString(R.string.unspecified);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        eventExecutor.shutdownNow();
        super.onDestroy();
    }

    private interface LabelProvider<T> {
        String getLabel(T value);
    }
}
