package org.nodocentral.miviaje.presentation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.core.util.Pair;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.FlexWrap;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import org.nodocentral.miviaje.R;
import org.nodocentral.miviaje.data.repository.CardRepository;
import org.nodocentral.miviaje.data.room.MiViajeDatabase;
import org.nodocentral.miviaje.domain.mimovilidad.Operator;
import org.nodocentral.miviaje.domain.mimovilidad.Route;
import org.nodocentral.miviaje.domain.mimovilidad.Station;
import org.nodocentral.miviaje.domain.mimovilidad.analytics.MobilityAnalyticsCalculator;
import org.nodocentral.miviaje.domain.mimovilidad.analytics.MobilityAnalyticsSummary;
import org.nodocentral.miviaje.domain.mimovilidad.analytics.MobilityAnalyticsSummary.Count;
import org.nodocentral.miviaje.domain.mimovilidad.analytics.MobilityAnalyticsSummary.DateRange;
import org.nodocentral.miviaje.domain.mimovilidad.analytics.MobilityAnalyticsSummary.HourBlockCount;
import org.nodocentral.miviaje.domain.mimovilidad.analytics.MobilityAnalyticsSummary.PaymentUnitFlow;
import org.nodocentral.miviaje.domain.mimovilidad.card.Card;
import org.nodocentral.miviaje.domain.mimovilidad.card.Event;

import java.time.Instant;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MobilityAnalyticsActivity extends BaseActivity {
    private static final String EXTRA_CARD_UID = "CARD_UID";
    private static final String STATE_RANGE_PRESET = "analytics_range_preset";
    private static final String STATE_CUSTOM_START = "analytics_custom_start";
    private static final String STATE_CUSTOM_END = "analytics_custom_end";
    private static final long NO_CUSTOM_DATE = Long.MIN_VALUE;
    private static final String PREF_SHOW_ADVANCED_DATA = "setting_show_technical_data";
    private static final String PREF_SHOW_DEBUG_DATA = "setting_show_debug_data";
    private static final String PREF_REBEL_MODE = "setting_rebel_mode";

    private final ExecutorService analyticsExecutor = Executors.newSingleThreadExecutor();
    private RangePreset selectedRangePreset = RangePreset.NINETY_DAYS;
    private LocalDate customStartDate;
    private LocalDate customEndDate;
    private Card card;
    private boolean suppressRangeSelection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobility_analytics);
        setToolbar(true);
        configureInsets();
        restoreState(savedInstanceState);
        setupRangeSelector();
        updateRangeChipSelection();

        long cardUid = getIntent().getLongExtra(EXTRA_CARD_UID, -1L);
        if (cardUid == -1L) {
            finish();
            return;
        }
        loadCard(cardUid);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_RANGE_PRESET, selectedRangePreset.name());
        outState.putLong(STATE_CUSTOM_START, customStartDate != null ? customStartDate.toEpochDay() : NO_CUSTOM_DATE);
        outState.putLong(STATE_CUSTOM_END, customEndDate != null ? customEndDate.toEpochDay() : NO_CUSTOM_DATE);
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
        analyticsExecutor.shutdownNow();
        super.onDestroy();
    }

    private void configureInsets() {
        int navAndCutoutTypes = WindowInsetsCompat.Type.navigationBars()
                | WindowInsetsCompat.Type.displayCutout();
        applyInsetsToPadding(findViewById(R.id.analytics_range_scroll), navAndCutoutTypes, true, false, true, false);
        applyInsetsToPadding(findViewById(R.id.analytics_scroll), navAndCutoutTypes, true, false, true, true);
    }

    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        String presetName = savedInstanceState.getString(STATE_RANGE_PRESET);
        selectedRangePreset = RangePreset.fromName(presetName, RangePreset.NINETY_DAYS);
        long customStart = savedInstanceState.getLong(STATE_CUSTOM_START, NO_CUSTOM_DATE);
        long customEnd = savedInstanceState.getLong(STATE_CUSTOM_END, NO_CUSTOM_DATE);
        customStartDate = customStart == NO_CUSTOM_DATE ? null : LocalDate.ofEpochDay(customStart);
        customEndDate = customEnd == NO_CUSTOM_DATE ? null : LocalDate.ofEpochDay(customEnd);
        if (selectedRangePreset == RangePreset.CUSTOM && (customStartDate == null || customEndDate == null)) {
            selectedRangePreset = RangePreset.NINETY_DAYS;
        }
    }

    private void setupRangeSelector() {
        setupPresetChip(R.id.analytics_range_7, RangePreset.SEVEN_DAYS);
        setupPresetChip(R.id.analytics_range_30, RangePreset.THIRTY_DAYS);
        setupPresetChip(R.id.analytics_range_90, RangePreset.NINETY_DAYS);
        setupPresetChip(R.id.analytics_range_month, RangePreset.THIS_MONTH);
        setupPresetChip(R.id.analytics_range_all, RangePreset.ALL_TIME);
        View customChip = findViewById(R.id.analytics_range_custom);
        customChip.setOnClickListener(v -> {
            if (!suppressRangeSelection) {
                showCustomRangePicker();
            }
        });
    }

    private void setupPresetChip(int chipId, RangePreset preset) {
        View chip = findViewById(chipId);
        chip.setOnClickListener(v -> {
            if (suppressRangeSelection) {
                return;
            }
            selectedRangePreset = preset;
            if (preset != RangePreset.CUSTOM) {
                customStartDate = null;
                customEndDate = null;
            }
            updateRangeChipSelection();
            renderAnalytics();
        });
    }

    private void showCustomRangePicker() {
        RangePreset previousPreset = selectedRangePreset;
        LocalDate previousStart = customStartDate;
        LocalDate previousEnd = customEndDate;

        DateRange currentRange = buildDateRange();
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText(R.string.analytics_custom_range_picker_title);
        if (!currentRange.isAllTime()) {
            builder.setSelection(new Pair<>(
                    toPickerUtcMillis(currentRange.getStartDateInclusive()),
                    toPickerUtcMillis(currentRange.getEndDateInclusive())
            ));
        }

        MaterialDatePicker<Pair<Long, Long>> picker = builder.build();
        boolean[] confirmed = {false};
        Runnable restorePreviousSelection = () -> {
            if (confirmed[0]) {
                return;
            }
            selectedRangePreset = previousPreset;
            customStartDate = previousStart;
            customEndDate = previousEnd;
            updateRangeChipSelection();
        };
        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection == null || selection.first == null || selection.second == null) {
                restorePreviousSelection.run();
                return;
            }
            confirmed[0] = true;
            customStartDate = fromPickerUtcMillis(selection.first);
            customEndDate = fromPickerUtcMillis(selection.second);
            selectedRangePreset = RangePreset.CUSTOM;
            updateRangeChipSelection();
            renderAnalytics();
        });
        picker.addOnNegativeButtonClickListener(v -> restorePreviousSelection.run());
        picker.addOnCancelListener(v -> restorePreviousSelection.run());
        picker.addOnDismissListener(v -> restorePreviousSelection.run());
        picker.show(getSupportFragmentManager(), "analytics_date_range_picker");
    }

    private void updateRangeChipSelection() {
        ChipGroup group = findViewById(R.id.analytics_range_group);
        suppressRangeSelection = true;
        group.check(selectedRangePreset.chipId);
        suppressRangeSelection = false;
    }

    private void loadCard(long cardUid) {
        setLoadingVisible(true);
        MiViajeDatabase database = MiViajeDatabase.getInstance(getApplicationContext());
        CardRepository cardRepository = new CardRepository(database);
        analyticsExecutor.execute(() -> {
            Card loadedCard = cardRepository.getCard(cardUid, 0);
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                if (loadedCard == null) {
                    finish();
                    return;
                }
                card = loadedCard;
                updateToolbarTitle();
                renderAnalytics();
            });
        });
    }

    private void updateToolbarTitle() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null || card == null) {
            return;
        }
        String alias = card.getAlias();
        if (alias != null && !alias.isBlank()) {
            actionBar.setTitle(getString(R.string.analytics_title_for_card, alias));
        } else {
            actionBar.setTitle(getString(R.string.analytics_title_generic));
        }
    }

    private void renderAnalytics() {
        if (card == null) {
            return;
        }
        MobilityAnalyticsSummary summary = MobilityAnalyticsCalculator.calculate(card.getEvents(), buildDateRange());
        TextView subtitle = findViewById(R.id.analytics_range_subtitle);
        subtitle.setText(formatRangeSubtitle(summary.getDateRange()));
        setLoadingVisible(false);

        if (summary.getTotalSavedEvents() == 0) {
            showEmptyState(
                    getString(R.string.analytics_empty_no_events_title),
                    getString(R.string.analytics_empty_no_events_body)
            );
            return;
        }
        if (summary.getEventsInRange() == 0) {
            showEmptyState(
                    getString(R.string.analytics_empty_range_title),
                    getString(R.string.analytics_empty_range_body)
            );
            return;
        }

        findViewById(R.id.analytics_empty_panel).setVisibility(View.GONE);
        findViewById(R.id.analytics_summary_grid).setVisibility(View.VISIBLE);
        findViewById(R.id.analytics_sections).setVisibility(View.VISIBLE);
        bindSummaryGrid(summary);
        bindRhythm(summary);
        bindTransport(summary);
        bindSpending(summary);
        bindPatterns(summary);
        bindAdvanced(summary);
        bindDebug(summary);
    }

    private DateRange buildDateRange() {
        LocalDate today = LocalDate.now();
        switch (selectedRangePreset) {
            case SEVEN_DAYS:
                return DateRange.lastDays(today, 7);
            case THIRTY_DAYS:
                return DateRange.lastDays(today, 30);
            case THIS_MONTH:
                return DateRange.currentMonth(today);
            case ALL_TIME:
                return DateRange.allTime();
            case CUSTOM:
                if (customStartDate != null && customEndDate != null) {
                    return DateRange.custom(customStartDate, customEndDate);
                }
                selectedRangePreset = RangePreset.NINETY_DAYS;
                updateRangeChipSelection();
                return DateRange.lastDays(today, 90);
            case NINETY_DAYS:
            default:
                return DateRange.lastDays(today, 90);
        }
    }

    private void setLoadingVisible(boolean loading) {
        findViewById(R.id.analytics_loading_panel).setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading) {
            findViewById(R.id.analytics_empty_panel).setVisibility(View.GONE);
            findViewById(R.id.analytics_summary_grid).setVisibility(View.GONE);
            findViewById(R.id.analytics_sections).setVisibility(View.GONE);
            findViewById(R.id.analytics_advanced_section).setVisibility(View.GONE);
            findViewById(R.id.analytics_debug_section).setVisibility(View.GONE);
        }
    }

    private void showEmptyState(String title, String body) {
        TextView emptyTitle = findViewById(R.id.analytics_empty_title);
        TextView emptyBody = findViewById(R.id.analytics_empty_body);
        emptyTitle.setText(title);
        emptyBody.setText(body);
        findViewById(R.id.analytics_empty_panel).setVisibility(View.VISIBLE);
        findViewById(R.id.analytics_summary_grid).setVisibility(View.GONE);
        findViewById(R.id.analytics_sections).setVisibility(View.GONE);
        findViewById(R.id.analytics_advanced_section).setVisibility(View.GONE);
        findViewById(R.id.analytics_debug_section).setVisibility(View.GONE);
    }

    private void bindSummaryGrid(MobilityAnalyticsSummary summary) {
        FlexboxLayout grid = findViewById(R.id.analytics_summary_grid);
        grid.removeAllViews();
        addStatCard(grid, R.drawable.ic_walk, R.string.analytics_metric_trips,
                String.valueOf(summary.getTripCount()), formatTripCount(summary.getTripCount()));
        addStatCard(grid, R.drawable.ic_coin, R.string.analytics_metric_cash_spent,
                formatMoney(summary.getSpentCents()), getString(R.string.analytics_range_selected));
        addStatCard(grid, R.drawable.ic_ticket, R.string.analytics_metric_tickets_used,
                formatTicketCount(summary.getTicketFlow().getConsumed()), getString(R.string.analytics_range_selected));
        addStatCard(grid, R.drawable.ic_hub, R.string.analytics_metric_transfers,
                String.valueOf(summary.getTransferCount()), formatTripCount(summary.getTransferCount()));
        addStatCard(grid, R.drawable.ic_calendar_month, R.string.analytics_metric_active_days,
                String.valueOf(summary.getActiveDays()), getString(R.string.analytics_active_days_detail));
        addStatCard(grid, R.drawable.ic_graph, R.string.analytics_metric_average_day,
                formatDecimal(summary.getAverageTripsPerActiveDay()), getString(R.string.analytics_trips_per_active_day));
    }

    private void addStatCard(FlexboxLayout parent, int iconResId, int labelResId, String value, String detail) {
        addStatCard(parent, iconResId, getString(labelResId), value, detail);
    }

    private void addStatCard(FlexboxLayout parent, int iconResId, String labelText, String value, String detail) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_analytics_stat, parent, false);
        ImageView icon = view.findViewById(R.id.analytics_stat_icon);
        TextView label = view.findViewById(R.id.analytics_stat_label);
        TextView valueView = view.findViewById(R.id.analytics_stat_value);
        TextView detailView = view.findViewById(R.id.analytics_stat_detail);
        icon.setImageResource(iconResId);
        label.setText(labelText);
        valueView.setText(value);
        detailView.setText(detail);
        view.setContentDescription(getString(R.string.analytics_accessibility_metric_format, label.getText(), value, detail));
        parent.addView(view);
    }

    private void bindRhythm(MobilityAnalyticsSummary summary) {
        LinearLayout weekdayBars = findViewById(R.id.analytics_weekday_bars);
        weekdayBars.removeAllViews();
        int maxWeekdayCount = 0;
        for (Integer count : summary.getWeekdayTripCounts().values()) {
            maxWeekdayCount = Math.max(maxWeekdayCount, count);
        }
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            int count = summary.getWeekdayTripCounts().get(dayOfWeek);
            addBarRow(weekdayBars, formatDay(dayOfWeek), formatTripCount(count), count, maxWeekdayCount);
        }

        LinearLayout insights = findViewById(R.id.analytics_rhythm_insights);
        insights.removeAllViews();
        Count<DayOfWeek> activeDay = summary.getTripCount() == 0 ? null : summary.getMostActiveWeekday();
        HourBlockCount peak = summary.getTripCount() == 0 ? null : summary.getPeakHourBlock();
        addMetricRow(insights, getString(R.string.analytics_most_active_day),
                activeDay == null ? getString(R.string.none) : getString(
                        R.string.analytics_day_with_count_format,
                        formatDay(activeDay.getItem()),
                        activeDay.getCount()
                ));
        addMetricRow(insights, getString(R.string.analytics_peak_hour),
                peak == null ? getString(R.string.none) : getString(
                        R.string.analytics_hour_with_count_format,
                        formatHourBlock(peak),
                        peak.getCount()
                ));
    }

    private void bindTransport(MobilityAnalyticsSummary summary) {
        LinearLayout transportShare = findViewById(R.id.analytics_transport_share);
        transportShare.removeAllViews();
        for (Count<Event.TransportType> count : summary.getTransportCounts()) {
            addBarRow(
                    transportShare,
                    formatTransportType(count.getItem()),
                    formatPercentTripCount(count.getCount(), summary.getTripCount()),
                    count.getCount(),
                    summary.getTripCount()
            );
        }
        if (summary.getTransportCounts().isEmpty()) {
            addMetricRow(transportShare, getString(R.string.analytics_metric_top_mode), getString(R.string.none));
        }

        LinearLayout topRoutes = findViewById(R.id.analytics_top_routes);
        topRoutes.removeAllViews();
        addSmallHeader(topRoutes, getString(R.string.analytics_top_routes));
        int maxRouteCount = maxCount(summary.getRouteCounts());
        addRankingRows(topRoutes, summary.getRouteCounts(), 3, maxRouteCount, this::formatRoute);

        LinearLayout insights = findViewById(R.id.analytics_transport_insights);
        insights.removeAllViews();
        addMetricRow(insights, getString(R.string.analytics_top_station), formatStation(summary.getTopStation()));
        addMetricRow(insights, getString(R.string.analytics_top_operator), formatOperator(summary.getTopOperator()));
    }

    private void bindSpending(MobilityAnalyticsSummary summary) {
        LinearLayout rows = findViewById(R.id.analytics_spending_rows);
        rows.removeAllViews();
        PaymentUnitFlow cashFlow = summary.getCashFlow();
        PaymentUnitFlow ticketFlow = summary.getTicketFlow();
        boolean showCash = cashFlow.hasMovement();
        boolean showTickets = ticketFlow.hasMovement();
        if (!showCash && !showTickets) {
            addMetricRow(rows, getString(R.string.analytics_no_value_movements), getString(R.string.none));
            return;
        }
        if (showCash) {
            addPaymentFlowSection(
                    rows,
                    R.string.analytics_cash_movement,
                    R.drawable.ic_coin,
                    cashFlow,
                    true,
                    R.string.analytics_cash_spent,
                    R.string.analytics_cash_added,
                    R.string.analytics_cash_net,
                    R.string.analytics_added_minus_spent
            );
        }
        if (showTickets) {
            addPaymentFlowSection(
                    rows,
                    R.string.analytics_ticket_movement,
                    R.drawable.ic_ticket,
                    ticketFlow,
                    false,
                    R.string.analytics_tickets_used,
                    R.string.analytics_tickets_added,
                    R.string.analytics_tickets_net,
                    R.string.analytics_added_minus_used
            );
        }
    }

    private void bindPatterns(MobilityAnalyticsSummary summary) {
        LinearLayout rows = findViewById(R.id.analytics_patterns_rows);
        rows.removeAllViews();
        Count<DayOfWeek> activeDay = summary.getTripCount() == 0 ? null : summary.getMostActiveWeekday();
        HourBlockCount peak = summary.getTripCount() == 0 ? null : summary.getPeakHourBlock();
        addMetricRow(rows, getString(R.string.analytics_most_active_day),
                activeDay == null ? getString(R.string.none) : formatDay(activeDay.getItem()));
        addMetricRow(rows, getString(R.string.analytics_peak_hour),
                peak == null ? getString(R.string.none) : formatHourBlock(peak));
        addMetricRow(rows, getString(R.string.analytics_longest_streak),
                getString(R.string.analytics_days_count_format, summary.getLongestActivityStreakDays()));
        addMetricRow(rows, getString(R.string.analytics_common_route), formatRoute(summary.getTopRoute()));
        addMetricRow(rows, getString(R.string.analytics_common_station), formatStation(summary.getTopStation()));
    }

    private void bindAdvanced(MobilityAnalyticsSummary summary) {
        View advancedSection = findViewById(R.id.analytics_advanced_section);
        boolean showAdvanced = preferences.getBoolean(PREF_SHOW_ADVANCED_DATA, false);
        advancedSection.setVisibility(showAdvanced ? View.VISIBLE : View.GONE);
        if (!showAdvanced) {
            return;
        }

        LinearLayout rows = findViewById(R.id.analytics_advanced_rows);
        rows.removeAllViews();
        addSmallHeader(rows, getString(R.string.analytics_top_routes));
        addRankingRows(rows, summary.getRouteCounts(), 5, maxCount(summary.getRouteCounts()), this::formatRoute);
        addSmallHeader(rows, getString(R.string.analytics_top_stations));
        addRankingRows(rows, summary.getStationCounts(), 5, maxCount(summary.getStationCounts()), this::formatStation);
        addSmallHeader(rows, getString(R.string.analytics_top_operators));
        addRankingRows(rows, summary.getOperatorCounts(), 5, maxCount(summary.getOperatorCounts()), this::formatOperator);
        addPaymentAdvancedRows(rows, summary.getCashFlow(), true, R.string.analytics_cash_details);
        addPaymentAdvancedRows(rows, summary.getTicketFlow(), false, R.string.analytics_ticket_details);
        addSmallHeader(rows, getString(R.string.analytics_weekday_counts));
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            addMetricRow(rows, formatDay(dayOfWeek), formatTripCount(summary.getWeekdayTripCounts().get(dayOfWeek)));
        }
        addSmallHeader(rows, getString(R.string.analytics_hour_counts));
        int maxHourCount = 0;
        for (HourBlockCount count : summary.getHourBlockCounts()) {
            maxHourCount = Math.max(maxHourCount, count.getCount());
        }
        for (HourBlockCount count : summary.getHourBlockCounts()) {
            addBarRow(rows, formatHourBlock(count), formatTripCount(count.getCount()), count.getCount(), maxHourCount);
        }
    }

    private void bindDebug(MobilityAnalyticsSummary summary) {
        View debugSection = findViewById(R.id.analytics_debug_section);
        boolean showDebug = preferences.getBoolean(PREF_SHOW_DEBUG_DATA, false);
        debugSection.setVisibility(showDebug ? View.VISIBLE : View.GONE);
        if (!showDebug) {
            return;
        }

        LinearLayout rows = findViewById(R.id.analytics_debug_rows);
        rows.removeAllViews();
        addMetricRow(rows, getString(R.string.analytics_debug_total_events), String.valueOf(summary.getTotalSavedEvents()));
        addMetricRow(rows, getString(R.string.analytics_debug_events_in_range), String.valueOf(summary.getEventsInRange()));
        addMetricRow(rows, getString(R.string.analytics_debug_events_outside_range), String.valueOf(summary.getEventsOutsideRange()));
        addMetricRow(rows, getString(R.string.analytics_debug_missing_dates), String.valueOf(summary.getMissingDateEvents()));
        addMetricRow(rows, getString(R.string.analytics_debug_unknown_transport), String.valueOf(summary.getUnknownTransportCount()));
        addMetricRow(rows, getString(R.string.analytics_debug_unknown_routes), String.valueOf(summary.getUnknownRouteCount()));
        addMetricRow(rows, getString(R.string.analytics_debug_unknown_stations), String.valueOf(summary.getUnknownStationCount()));
        addMetricRow(rows, getString(R.string.analytics_debug_unknown_value_movements), String.valueOf(summary.getUnknownValueMovementCount()));
        addMetricRow(rows, getString(R.string.analytics_debug_invalid_events), String.valueOf(summary.getInvalidOrUnspecifiedEventTypeCount()));
        addMetricRow(rows, getString(R.string.analytics_debug_distinct_operators), String.valueOf(summary.getDistinctOperatorCount()));
        addMetricRow(rows, getString(R.string.analytics_debug_distinct_validators), String.valueOf(summary.getDistinctValidatorCount()));
        addMetricRow(rows, getString(R.string.analytics_debug_distinct_devices), String.valueOf(summary.getDistinctDeviceCount()));
        addMetricRow(rows, getString(R.string.analytics_debug_distinct_sams), String.valueOf(summary.getDistinctSamCount()));
    }

    private <T> void addRankingRows(LinearLayout parent,
                                    List<Count<T>> counts,
                                    int limit,
                                    int max,
                                    LabelFormatter<T> formatter) {
        if (counts.isEmpty()) {
            addMetricRow(parent, getString(R.string.analytics_no_rankings), getString(R.string.none));
            return;
        }
        int rows = Math.min(limit, counts.size());
        for (int i = 0; i < rows; i++) {
            Count<T> count = counts.get(i);
            addBarRow(parent, formatter.format(count.getItem()), formatTripCount(count.getCount()), count.getCount(), max);
        }
    }

    private void addBarRow(LinearLayout parent, String label, String value, int count, int max) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_analytics_bar_row, parent, false);
        TextView labelView = view.findViewById(R.id.analytics_bar_label);
        TextView valueView = view.findViewById(R.id.analytics_bar_value);
        LinearProgressIndicator progress = view.findViewById(R.id.analytics_bar_progress);
        int percent = max <= 0 ? 0 : Math.round((count * 100f) / max);
        labelView.setText(label);
        valueView.setText(value);
        progress.setProgressCompat(percent, false);
        view.setContentDescription(label + ": " + value);
        parent.addView(view);
    }

    private void addMetricRow(LinearLayout parent, String label, String value) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_analytics_metric_row, parent, false);
        TextView labelView = view.findViewById(R.id.analytics_metric_label);
        TextView valueView = view.findViewById(R.id.analytics_metric_value);
        labelView.setText(label);
        valueView.setText(value);
        view.setContentDescription(label + ": " + value);
        parent.addView(view);
    }

    private void addPaymentFlowSection(LinearLayout parent,
                                       int headerResId,
                                       int iconResId,
                                       PaymentUnitFlow flow,
                                       boolean money,
                                       int consumedLabelResId,
                                       int addedLabelResId,
                                       int netLabelResId,
                                       int netDetailResId) {
        addSmallHeader(parent, getString(headerResId));
        FlexboxLayout grid = new FlexboxLayout(this);
        grid.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        grid.setFlexWrap(FlexWrap.WRAP);
        addValueTile(grid, iconResId, getString(consumedLabelResId),
                formatPaymentValue(flow.getConsumed(), money, false),
                formatMovementCount(flow.getConsumedCount()));
        addValueTile(grid, R.drawable.ic_wallet, getString(addedLabelResId),
                formatPaymentValue(flow.getAdded(), money, false),
                formatMovementCount(flow.getAddedCount()));
        addValueTile(grid, R.drawable.ic_graph, getString(netLabelResId),
                formatPaymentValue(flow.getNetChange(), money, true),
                getString(netDetailResId));
        parent.addView(grid);
        if (flow.getAddedCount() > 0 && flow.getLastTopUpDateTime() != null) {
            addMetricRow(parent, getString(R.string.analytics_last_reload), formatLastTopUp(flow, money));
        }
    }

    private void addPaymentAdvancedRows(LinearLayout parent,
                                        PaymentUnitFlow flow,
                                        boolean money,
                                        int headerResId) {
        if (!flow.hasMovement()) {
            return;
        }
        addSmallHeader(parent, getString(headerResId));
        addMetricRow(parent, getString(R.string.analytics_average_reload),
                formatPaymentValue(flow.getAverageTopUp(), money, false));
        addMetricRow(parent, getString(R.string.analytics_highest_reload),
                formatPaymentValue(flow.getHighestTopUp(), money, false));
        addMetricRow(parent, getString(R.string.analytics_total_reloaded),
                formatPaymentValue(flow.getAdded(), money, false));
        addMetricRow(parent, getString(R.string.analytics_refunded),
                formatPaymentValue(flow.getRefunded(), money, false));
    }

    private void addValueTile(FlexboxLayout parent, int iconResId, String label, String value, String detail) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_analytics_value_tile, parent, false);
        ImageView icon = view.findViewById(R.id.analytics_value_icon);
        TextView labelView = view.findViewById(R.id.analytics_value_label);
        TextView valueView = view.findViewById(R.id.analytics_value_amount);
        TextView detailView = view.findViewById(R.id.analytics_value_detail);
        icon.setImageResource(iconResId);
        labelView.setText(label);
        valueView.setText(value);
        detailView.setText(detail);
        view.setContentDescription(getString(R.string.analytics_accessibility_metric_format, label, value, detail));
        parent.addView(view);
    }

    private void addSmallHeader(LinearLayout parent, String text) {
        TextView header = new TextView(this);
        header.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleSmall);
        header.setText(text);
        header.setTextColor(com.google.android.material.color.MaterialColors.getColor(
                parent,
                com.google.android.material.R.attr.colorOnSurface,
                0
        ));
        header.setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(4));
        header.setTypeface(header.getTypeface(), android.graphics.Typeface.BOLD);
        parent.addView(header);
    }

    private String formatRangeSubtitle(DateRange range) {
        if (range.isAllTime()) {
            return getString(R.string.analytics_range_subtitle_all);
        }
        return getString(
                R.string.analytics_range_subtitle,
                formatDate(range.getStartDateInclusive()),
                formatDate(range.getEndDateInclusive())
        );
    }

    private String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault()));
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                .withLocale(Locale.getDefault()));
    }

    private String formatMoney(int cents) {
        return getString(R.string.money_mxn_format, cents / 100f);
    }

    private String formatSignedMoney(int cents) {
        if (cents > 0) {
            return getString(R.string.money_mxn_plus_format, cents / 100f);
        }
        if (cents < 0) {
            return getString(R.string.money_mxn_negative_format, Math.abs(cents) / 100f);
        }
        return formatMoney(0);
    }

    private String formatTicketCount(int count) {
        return getResources().getQuantityString(R.plurals.ticket_count, count, count);
    }

    private String formatSignedTicketCount(int count) {
        if (count > 0) {
            return getResources().getQuantityString(R.plurals.ticket_count_plus, count, count);
        }
        if (count < 0) {
            int absoluteCount = Math.abs(count);
            return "-" + getResources().getQuantityString(R.plurals.ticket_count, absoluteCount, absoluteCount);
        }
        return formatTicketCount(0);
    }

    private String formatPaymentValue(int amount, boolean money, boolean signed) {
        if (money) {
            return signed ? formatSignedMoney(amount) : formatMoney(amount);
        }
        return signed ? formatSignedTicketCount(amount) : formatTicketCount(amount);
    }

    private String formatDecimal(double value) {
        return String.format(Locale.getDefault(), "%.1f", value);
    }

    private String formatTripCount(int count) {
        return getString(R.string.analytics_trip_count_format, count);
    }

    private String formatMovementCount(int count) {
        return getString(R.string.analytics_movement_count_format, count);
    }

    private String formatPercentTripCount(int count, int total) {
        int percent = total <= 0 ? 0 : Math.round((count * 100f) / total);
        return getString(R.string.analytics_percent_trip_count_format, percent, count);
    }

    private String formatDay(DayOfWeek dayOfWeek) {
        if (dayOfWeek == null) {
            return getString(R.string.none);
        }
        return dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault());
    }

    private String formatHourBlock(HourBlockCount hourBlock) {
        if (hourBlock == null) {
            return getString(R.string.none);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                .withLocale(Locale.getDefault());
        LocalTime start = LocalTime.of(hourBlock.getStartHour(), 0);
        LocalTime end = LocalTime.of(hourBlock.getEndHour() % 24, 0);
        return getString(R.string.analytics_hour_block_format, start.format(formatter), end.format(formatter));
    }

    private String formatTransportType(Event.TransportType transportType) {
        if (transportType == null) {
            return getString(R.string.none);
        }
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

    private String formatRoute(Route route) {
        if (route == null) {
            return getString(R.string.none);
        }
        if (Route.getRapidTransitLines().contains(route)) {
            return "L" + route.getId();
        }
        boolean rebelMode = preferences.getBoolean(PREF_REBEL_MODE, false);
        return TransitTextFormatter.getRouteName(this, route, rebelMode);
    }

    private String formatStation(Station station) {
        if (station == null) {
            return getString(R.string.none);
        }
        return TransitTextFormatter.getStationSuggestionName(this, station);
    }

    private String formatOperator(Operator operator) {
        if (operator == null) {
            return getString(R.string.none);
        }
        String name = operator.getName();
        return name != null ? name : getString(R.string.operator_id_format, operator.getValue());
    }

    private String formatLastTopUp(PaymentUnitFlow flow, boolean money) {
        if (flow.getAddedCount() == 0 || flow.getLastTopUpDateTime() == null) {
            return getString(R.string.none);
        }
        return getString(
                R.string.analytics_last_reload_format,
                formatPaymentValue(flow.getLastTopUpAmount(), money, false),
                formatDateTime(flow.getLastTopUpDateTime())
        );
    }

    private int maxCount(List<? extends Count<?>> counts) {
        int max = 0;
        for (Count<?> count : counts) {
            max = Math.max(max, count.getCount());
        }
        return max;
    }

    private long toPickerUtcMillis(LocalDate date) {
        return date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
    }

    private LocalDate fromPickerUtcMillis(long millis) {
        return Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate();
    }

    private interface LabelFormatter<T> {
        String format(T item);
    }

    private enum RangePreset {
        SEVEN_DAYS(R.id.analytics_range_7),
        THIRTY_DAYS(R.id.analytics_range_30),
        NINETY_DAYS(R.id.analytics_range_90),
        THIS_MONTH(R.id.analytics_range_month),
        ALL_TIME(R.id.analytics_range_all),
        CUSTOM(R.id.analytics_range_custom);

        final int chipId;

        RangePreset(int chipId) {
            this.chipId = chipId;
        }

        static RangePreset fromName(String name, RangePreset fallback) {
            if (name == null) {
                return fallback;
            }
            for (RangePreset preset : values()) {
                if (preset.name().equals(name)) {
                    return preset;
                }
            }
            return fallback;
        }
    }
}
