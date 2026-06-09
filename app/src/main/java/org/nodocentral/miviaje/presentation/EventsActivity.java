package org.nodocentral.miviaje.presentation;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

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
import org.nodocentral.miviaje.domain.mimovilidad.RouteMapper;
import org.nodocentral.miviaje.domain.mimovilidad.Station;
import org.nodocentral.miviaje.domain.mimovilidad.StationMapper;
import org.nodocentral.miviaje.domain.mimovilidad.card.Event;
import org.nodocentral.miviaje.domain.mimovilidad.filters.EventFilter;
import org.nodocentral.miviaje.domain.mimovilidad.filters.EventFilterCriteria;
import org.nodocentral.miviaje.domain.mimovilidad.filters.EventFilterToken;
import org.nodocentral.miviaje.presentation.adapters.EventAdapter;

import java.sql.Array;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventsActivity extends BaseActivity {
    private final EnumSet<Event.TransportType> activeTransportTypes = EnumSet.noneOf(Event.TransportType.class);
    private final LinkedHashSet<EventFilterToken> activeRouteTokens = new LinkedHashSet<>();
    private final LinkedHashSet<EventFilterToken> activeStationValidatorTokens = new LinkedHashSet<>();
    private final LinkedHashSet<EventFilterToken> activeOperatorTokens = new LinkedHashSet<>();
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
        LinkedHashSet<EventFilterToken> draftRouteTokens = new LinkedHashSet<>(activeRouteTokens);
        LinkedHashSet<EventFilterToken> draftStationValidatorTokens = new LinkedHashSet<>(activeStationValidatorTokens);
        LinkedHashSet<EventFilterToken> draftOperatorTokens = new LinkedHashSet<>(activeOperatorTokens);
        EnumSet<Event.Type> draftEventTypes = copyOf(activeEventTypes, Event.Type.class);
        int[] draftDateFilterId = {activeDateFilterId};
        LocalDate[] draftCustomDateStart = {activeCustomDateStart};
        LocalDate[] draftCustomDateEnd = {activeCustomDateEnd};
        boolean[] suppressDateListener = {false};
        List<EventFilterSuggestion> routeSuggestions = buildRouteSuggestions();
        List<EventFilterSuggestion> stationValidatorSuggestions = buildStationValidatorSuggestions();
        List<EventFilterSuggestion> operatorSuggestions = buildOperatorSuggestions();

        populateTransportChips(sheetView, draftTransportTypes);
        populateEventTypeChips(sheetView, draftEventTypes);
        setupTokenAutocomplete(
                sheetView,
                R.id.event_filters_route_input,
                R.id.event_filters_route_token_group,
                EventFilterToken.Category.ROUTE,
                draftRouteTokens,
                routeSuggestions
        );
        setupTokenAutocomplete(
                sheetView,
                R.id.event_filters_station_input,
                R.id.event_filters_station_token_group,
                EventFilterToken.Category.STATION_VALIDATOR,
                draftStationValidatorTokens,
                stationValidatorSuggestions
        );
        setupTokenAutocomplete(
                sheetView,
                R.id.event_filters_operator_input,
                R.id.event_filters_operator_token_group,
                EventFilterToken.Category.OPERATOR,
                draftOperatorTokens,
                operatorSuggestions
        );
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
                draftRouteTokens.clear();
                draftStationValidatorTokens.clear();
                draftOperatorTokens.clear();
                draftEventTypes.clear();
                draftDateFilterId[0] = View.NO_ID;
                draftCustomDateStart[0] = null;
                draftCustomDateEnd[0] = null;
                refreshCheckedStates(
                        sheetView,
                        draftTransportTypes,
                        draftRouteTokens,
                        draftStationValidatorTokens,
                        draftOperatorTokens,
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
                addPendingTypedToken(
                        sheetView,
                        R.id.event_filters_route_input,
                        EventFilterToken.Category.ROUTE,
                        draftRouteTokens,
                        routeSuggestions
                );
                addPendingTypedToken(
                        sheetView,
                        R.id.event_filters_station_input,
                        EventFilterToken.Category.STATION_VALIDATOR,
                        draftStationValidatorTokens,
                        stationValidatorSuggestions
                );
                addPendingTypedToken(
                        sheetView,
                        R.id.event_filters_operator_input,
                        EventFilterToken.Category.OPERATOR,
                        draftOperatorTokens,
                        operatorSuggestions
                );
                activeTransportTypes.clear();
                activeTransportTypes.addAll(draftTransportTypes);
                activeRouteTokens.clear();
                activeRouteTokens.addAll(draftRouteTokens);
                activeStationValidatorTokens.clear();
                activeStationValidatorTokens.addAll(draftStationValidatorTokens);
                activeOperatorTokens.clear();
                activeOperatorTokens.addAll(draftOperatorTokens);
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
        for (Event.TransportType transportType : getTransportTypes()) {
            Chip chip = createFilterChip(getTransportTypeLabel(transportType), draftTransportTypes.contains(transportType));
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> updateEnumSet(draftTransportTypes, transportType, isChecked));
            group.addView(chip);
        }
    }

    private void populateEventTypeChips(View sheetView, EnumSet<Event.Type> draftEventTypes) {
        ChipGroup group = sheetView.findViewById(R.id.event_filters_event_type_group);
        if (group == null) {
            return;
        }
        group.removeAllViews();
        for (Event.Type eventType : getEventTypes()) {
            Chip chip = createFilterChip(getEventTypeLabel(eventType), draftEventTypes.contains(eventType));
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> updateEnumSet(draftEventTypes, eventType, isChecked));
            group.addView(chip);
        }
    }

    private void setupTokenAutocomplete(View sheetView,
                                        int inputId,
                                        int tokenGroupId,
                                        EventFilterToken.Category category,
                                        LinkedHashSet<EventFilterToken> draftTokens,
                                        List<EventFilterSuggestion> suggestions) {
        AutoCompleteTextView input = sheetView.findViewById(inputId);
        ChipGroup tokenGroup = sheetView.findViewById(tokenGroupId);
        if (tokenGroup != null) {
            renderTokenChips(tokenGroup, draftTokens);
        }
        if (input == null) {
            return;
        }

        ArrayAdapter<EventFilterSuggestion> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                suggestions
        );
        input.setAdapter(adapter);
        input.setThreshold(1);
        input.setOnItemClickListener((parent, view, position, id) -> {
            EventFilterSuggestion suggestion = adapter.getItem(position);
            if (suggestion != null && draftTokens.add(suggestion.token) && tokenGroup != null) {
                renderTokenChips(tokenGroup, draftTokens);
            }
            input.setText(null);
            input.dismissDropDown();
        });
        input.setOnEditorActionListener((v, actionId, event) -> {
            boolean isEnter = event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_UP;
            boolean isSearch = actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || actionId == EditorInfo.IME_ACTION_GO;
            if (!isEnter && !isSearch) {
                return false;
            }
            addTypedToken(input, category, draftTokens, suggestions, tokenGroup);
            return true;
        });
    }

    private void addTypedToken(AutoCompleteTextView input,
                               EventFilterToken.Category category,
                               LinkedHashSet<EventFilterToken> draftTokens,
                               List<EventFilterSuggestion> suggestions,
                               ChipGroup tokenGroup) {
        String typedValue = input.getText() != null ? input.getText().toString().trim() : "";
        if (typedValue.isEmpty()) {
            return;
        }
        EventFilterToken token = findSuggestionToken(typedValue, suggestions);
        if (token == null) {
            token = EventFilterToken.text(category, typedValue);
        }
        if (draftTokens.add(token) && tokenGroup != null) {
            renderTokenChips(tokenGroup, draftTokens);
        }
        input.setText(null);
        input.dismissDropDown();
    }

    private void addPendingTypedToken(View sheetView,
                                      int inputId,
                                      EventFilterToken.Category category,
                                      LinkedHashSet<EventFilterToken> draftTokens,
                                      List<EventFilterSuggestion> suggestions) {
        AutoCompleteTextView input = sheetView.findViewById(inputId);
        if (input != null) {
            addTypedToken(input, category, draftTokens, suggestions, null);
        }
    }

    private EventFilterToken findSuggestionToken(String typedValue, List<EventFilterSuggestion> suggestions) {
        String normalizedTypedValue = normalizeSearchText(typedValue);
        for (EventFilterSuggestion suggestion : suggestions) {
            if (normalizeSearchText(suggestion.label).equals(normalizedTypedValue)
                    || normalizeSearchText(suggestion.token.getLabel()).equals(normalizedTypedValue)
                    || normalizeSearchText(suggestion.token.getText()).equals(normalizedTypedValue)) {
                return suggestion.token;
            }
        }

        EventFilterToken fuzzyMatch = null;
        for (EventFilterSuggestion suggestion : suggestions) {
            if (!normalizedTypedValue.isEmpty()
                    && normalizeSearchText(suggestion.label).contains(normalizedTypedValue)) {
                if (fuzzyMatch != null) {
                    return null;
                }
                fuzzyMatch = suggestion.token;
            }
        }
        return fuzzyMatch;
    }

    private void renderTokenChips(ChipGroup group, LinkedHashSet<EventFilterToken> tokens) {
        group.removeAllViews();
        for (EventFilterToken token : tokens) {
            Chip chip = createTokenChip(token.getLabel());
            chip.setOnCloseIconClickListener(v -> {
                tokens.remove(token);
                renderTokenChips(group, tokens);
            });
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
                                      LinkedHashSet<EventFilterToken> routeTokens,
                                      LinkedHashSet<EventFilterToken> stationValidatorTokens,
                                      LinkedHashSet<EventFilterToken> operatorTokens,
                                      EnumSet<Event.Type> eventTypes,
                                      int dateFilterId,
                                      LocalDate[] draftCustomDateStart,
                                      LocalDate[] draftCustomDateEnd) {
        populateTransportChips(sheetView, transportTypes);
        populateEventTypeChips(sheetView, eventTypes);
        ChipGroup routeTokenGroup = sheetView.findViewById(R.id.event_filters_route_token_group);
        if (routeTokenGroup != null) {
            renderTokenChips(routeTokenGroup, routeTokens);
        }
        ChipGroup stationTokenGroup = sheetView.findViewById(R.id.event_filters_station_token_group);
        if (stationTokenGroup != null) {
            renderTokenChips(stationTokenGroup, stationValidatorTokens);
        }
        ChipGroup operatorTokenGroup = sheetView.findViewById(R.id.event_filters_operator_token_group);
        if (operatorTokenGroup != null) {
            renderTokenChips(operatorTokenGroup, operatorTokens);
        }
        clearTokenInput(sheetView, R.id.event_filters_route_input);
        clearTokenInput(sheetView, R.id.event_filters_station_input);
        clearTokenInput(sheetView, R.id.event_filters_operator_input);
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

    private void clearTokenInput(View sheetView, int inputId) {
        AutoCompleteTextView input = sheetView.findViewById(inputId);
        if (input != null) {
            input.setText(null);
        }
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

    private Chip createTokenChip(String label) {
        Chip chip = (Chip) LayoutInflater.from(this).inflate(R.layout.item_event_filter_chip, null, false);
        chip.setText(label);
        chip.setCheckable(false);
        chip.setChecked(false);
        chip.setCloseIconVisible(true);
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
        boolean hasActiveFilters = !activeTransportTypes.isEmpty()
                || !activeRouteTokens.isEmpty()
                || !activeStationValidatorTokens.isEmpty()
                || !activeOperatorTokens.isEmpty()
                || !activeEventTypes.isEmpty()
                || activeDateFilterId != View.NO_ID;
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
            int activeTokenCount = activeRouteTokens.size()
                    + activeStationValidatorTokens.size()
                    + activeOperatorTokens.size();
            boolean hasMoreFilters = activeTokenCount > 0;
            moreFilterChip.setChecked(hasMoreFilters);
            if (activeTokenCount == 1) {
                moreFilterChip.setText(getOnlyActiveMoreToken().getLabel());
            } else {
                moreFilterChip.setText(R.string.events_filter_more);
            }
        }
    }

    private EventFilterToken getOnlyActiveMoreToken() {
        if (!activeRouteTokens.isEmpty()) {
            return activeRouteTokens.iterator().next();
        } else if (!activeStationValidatorTokens.isEmpty()) {
            return activeStationValidatorTokens.iterator().next();
        } else {
            return activeOperatorTokens.iterator().next();
        }
    }

    private void clearActiveFilters() {
        activeTransportTypes.clear();
        activeRouteTokens.clear();
        activeStationValidatorTokens.clear();
        activeOperatorTokens.clear();
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
                .routeTokens(activeRouteTokens)
                .stationValidatorTokens(activeStationValidatorTokens)
                .operatorTokens(activeOperatorTokens)
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

    private List<EventFilterSuggestion> buildRouteSuggestions() {
        LinkedHashMap<EventFilterToken, EventFilterSuggestion> suggestions = new LinkedHashMap<>();
        for (Event event : allEvents) {
            Route route = resolveRoute(event);
            if (route != null) {
                addSuggestion(suggestions, EventFilterToken.route(route, getRouteLabel(route)));
            }
            if (event.getRouteId() > 0) {
                addSuggestion(suggestions, EventFilterToken.text(
                        EventFilterToken.Category.ROUTE,
                        String.valueOf(event.getRouteId())
                ));
            }
        }
        for (Route route : Route.values()) {
            if (route != Route.NONE) {
                addSuggestion(suggestions, EventFilterToken.route(route, getRouteLabel(route)));
            }
        }
        List<EventFilterSuggestion> sortedSuggestions = new ArrayList<>(suggestions.values());
        sortedSuggestions.sort(this::compareRouteSuggestions);
        return sortedSuggestions;
    }

    private List<EventFilterSuggestion> buildStationValidatorSuggestions() {
        LinkedHashMap<EventFilterToken, EventFilterSuggestion> suggestions = new LinkedHashMap<>();
        for (Event event : allEvents) {
            Station station = StationMapper.getStation(event);
            if (station != null) {
                addSuggestion(suggestions, EventFilterToken.station(station, getStationLabel(station)));
            }
            Station location = StationMapper.getLocation(event);
            if (location != null) {
                addSuggestion(suggestions, EventFilterToken.station(location, getStationLabel(location)));
            }
            addPositiveNumberSuggestion(suggestions, EventFilterToken.Category.STATION_VALIDATOR, StationMapper.getStationId(event));
            addPositiveNumberSuggestion(suggestions, EventFilterToken.Category.STATION_VALIDATOR, StationMapper.getValidator(event));
            addPositiveNumberSuggestion(suggestions, EventFilterToken.Category.STATION_VALIDATOR, event.getDeviceId());
            addPositiveNumberSuggestion(suggestions, EventFilterToken.Category.STATION_VALIDATOR, event.getLocationId());
        }
        for (Station station : Station.values()) {
            addSuggestion(suggestions, EventFilterToken.station(station, getStationLabel(station)));
        }
        List<EventFilterSuggestion> sortedSuggestions = new ArrayList<>(suggestions.values());
        sortedSuggestions.sort(this::compareTextSuggestions);
        return sortedSuggestions;
    }

    private List<EventFilterSuggestion> buildOperatorSuggestions() {
        LinkedHashMap<EventFilterToken, EventFilterSuggestion> suggestions = new LinkedHashMap<>();
        for (Event event : allEvents) {
            Operator operator = event.getOperator();
            if (operator != null && operator != Operator.UNSPECIFIED) {
                addSuggestion(suggestions, EventFilterToken.operator(operator, getOperatorLabel(operator)));
            }
            addPositiveNumberSuggestion(suggestions, EventFilterToken.Category.OPERATOR, event.getEntityId());
        }
        for (Operator operator : Operator.values()) {
            if (operator != Operator.UNSPECIFIED) {
                addSuggestion(suggestions, EventFilterToken.operator(operator, getOperatorLabel(operator)));
            }
        }
        List<EventFilterSuggestion> sortedSuggestions = new ArrayList<>(suggestions.values());
        sortedSuggestions.sort(this::compareTextSuggestions);
        return sortedSuggestions;
    }

    private int compareRouteSuggestions(EventFilterSuggestion first, EventFilterSuggestion second) {
        EventFilterToken firstToken = first.token;
        EventFilterToken secondToken = second.token;
        if (firstToken.getKind() == EventFilterToken.Kind.ROUTE
                && secondToken.getKind() == EventFilterToken.Kind.ROUTE) {
            return Integer.compare(firstToken.getRoute().ordinal(), secondToken.getRoute().ordinal());
        } else if (firstToken.getKind() == EventFilterToken.Kind.ROUTE) {
            return -1;
        } else if (secondToken.getKind() == EventFilterToken.Kind.ROUTE) {
            return 1;
        }

        Integer firstNumber = parsePositiveInt(firstToken.getText());
        Integer secondNumber = parsePositiveInt(secondToken.getText());
        if (firstNumber != null && secondNumber != null) {
            return firstNumber.compareTo(secondNumber);
        } else if (firstNumber != null) {
            return -1;
        } else if (secondNumber != null) {
            return 1;
        }
        return compareTextSuggestions(first, second);
    }

    private int compareTextSuggestions(EventFilterSuggestion first, EventFilterSuggestion second) {
        return normalizeSearchText(first.label).compareTo(normalizeSearchText(second.label));
    }

    private Integer parsePositiveInt(String value) {
        if (value == null || !value.matches("\\d+")) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void addPositiveNumberSuggestion(Map<EventFilterToken, EventFilterSuggestion> suggestions,
                                             EventFilterToken.Category category,
                                             int value) {
        if (value > 0) {
            addSuggestion(suggestions, EventFilterToken.text(category, String.valueOf(value)));
        }
    }

    private void addSuggestion(Map<EventFilterToken, EventFilterSuggestion> suggestions, EventFilterToken token) {
        if (token != null && token.getLabel() != null && !token.getLabel().trim().isEmpty()) {
            suggestions.putIfAbsent(token, new EventFilterSuggestion(token));
        }
    }

    private Route resolveRoute(Event event) {
        Route displayedRoute = StationMapper.getRoute(event);
        if (displayedRoute != null) {
            return displayedRoute;
        }
        return RouteMapper.fromId(
                event.getEntityId(),
                event.getRouteId(),
                event.getDeviceId(),
                event.getTransportType()
        );
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
        if (Route.getRapidTransitLines().contains(route)) {
            return "L" + route.getId();
        }
        return TransitTextFormatter.getRouteName(this, route, false);
    }

    private String getStationLabel(Station station) {
        return TransitTextFormatter.getStationSuggestionName(this, station);
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

    private String normalizeSearchText(String value) {
        return TransitTextFormatter.normalize(value);
    }

    private static Event.TransportType[] getTransportTypes() {
        return new Event.TransportType[]{
                Event.TransportType.BUS,
                Event.TransportType.TRAIN,
                Event.TransportType.TRAIN_FEEDER_BUS,
                Event.TransportType.BRT,
                Event.TransportType.BRT_FEEDER_BUS,
        };
    }

    private static Event.Type[] getEventTypes() {
        return new Event.Type[]{
                Event.Type.PRODUCT_USE,
                Event.Type.PRODUCT_TOP_UP,
                Event.Type.TRANSFER,
                Event.Type.REFUND,
                Event.Type.FARE_REFUND,
                Event.Type.PRODUCT_DISTRIBUTION,
                Event.Type.PAYMENT_METHOD_EMISSION,
        };
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

    private static final class EventFilterSuggestion {
        final EventFilterToken token;
        final String label;

        EventFilterSuggestion(EventFilterToken token) {
            this.token = token;
            this.label = token.getLabel();
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
