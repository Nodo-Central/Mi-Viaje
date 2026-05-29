package org.nodocentral.miviaje.presentation;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;

import org.nodocentral.miviaje.Helpers;
import org.nodocentral.miviaje.R;
import org.nodocentral.miviaje.data.repository.ArtworkRepository;
import org.nodocentral.miviaje.data.repository.CardRepository;
import org.nodocentral.miviaje.data.room.MiViajeDatabase;
import org.nodocentral.miviaje.domain.artwork.Artwork;
import org.nodocentral.miviaje.domain.mimovilidad.card.ApplicationStatus;
import org.nodocentral.miviaje.domain.mimovilidad.card.Card;
import org.nodocentral.miviaje.domain.mimovilidad.card.Emission;
import org.nodocentral.miviaje.domain.mimovilidad.card.Environment;
import org.nodocentral.miviaje.domain.mimovilidad.card.Event;
import org.nodocentral.miviaje.domain.mimovilidad.card.Product;
import org.nodocentral.miviaje.domain.mimovilidad.card.ProductContract;
import org.nodocentral.miviaje.domain.mimovilidad.card.ProductService;
import org.nodocentral.miviaje.domain.mimovilidad.card.User;
import org.nodocentral.miviaje.presentation.adapters.CardAdapter;
import org.nodocentral.miviaje.presentation.adapters.EventAdapter;
import org.nodocentral.miviaje.presentation.adapters.ProductAdapter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;

public class CardActivity extends BaseActivity {
    private static final String TAG = "CardActivity";
    private Card card;
    private Artwork cardArtwork;
    EventAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);
        super.setToolbar(true);
        configureActivityInsets();

        long cardUid = getIntent().getLongExtra("CARD_UID", -1L);
        String cardTn = getIntent().getStringExtra("CARD_TN");
        if (cardUid == -1L || cardTn == null) {
            finish();
            return;
        }
        configureCardLayout(cardTn);
        fetchCard(cardUid);
    }

    private void configureActivityInsets() {
        applyInsetsToPadding(
                findViewById(R.id.card_layout),
                WindowInsetsCompat.Type.navigationBars() | WindowInsetsCompat.Type.displayCutout(),
                true,
                false,
                true,
                true
        );
    }

    private void configureCardLayout(String cardTn) {
        MaterialCardView cardLayout = findViewById(R.id.card_item);
        ViewGroup.MarginLayoutParams cardLayoutParams = (ViewGroup.MarginLayoutParams) cardLayout.getLayoutParams();
        cardLayoutParams.setMargins(0, 0, 0, 0);
        cardLayout.setLayoutParams(cardLayoutParams);
        ViewCompat.setTransitionName(cardLayout, cardTn);
    }

    private void fetchCard(long cardUid) {
        MiViajeDatabase database = MiViajeDatabase.getInstance(getApplicationContext());
        CardRepository cardRepository = new CardRepository(database);
        ArtworkRepository artworkRepository = new ArtworkRepository(this, database);
        new Thread(() -> {
            card = cardRepository.getCard(cardUid, 3);
            if (card == null) {
                runOnUiThread(this::finish);
                return;
            }
            cardArtwork = artworkRepository.getByRef(card.getArtworkRef());
            runOnUiThread(this::loadCardData);
        }).start();
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)) : "Desconocido";
    }

    private String formatTime(LocalTime time) {
        return time != null ? time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)) : "Desconocido";
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.MEDIUM)) : "Desconocido";
    }

    @SuppressLint("DefaultLocale")
    private void loadCardData() {
        setView();
        setCard();
        setUser();
        setNetwork();
        setCardStatus();
        setProducts();
        setEvents();
    }

    void setView() {
        MaterialCardView cardLayoutContainer = findViewById(R.id.card_item);
        cardLayoutContainer.setOnClickListener(null);
        cardLayoutContainer.setClickable(false);
        cardLayoutContainer.setFocusable(false);

        View analyticsLayout = findViewById(R.id.card_analytics_layout);
        analyticsLayout.setOnClickListener(v -> {
            Intent analytics = new Intent(this, MobilityAnalyticsActivity.class);
            analytics.putExtra("CARD_UID", card.getUid());
            startActivity(analytics);
        });
        analyticsLayout.setContentDescription(getString(R.string.analytics_open_card_content_description));

        ConstraintLayout cardView = findViewById(R.id.card_item_layout);
        TextView tickets = findViewById(R.id.card_item_tickets);
        tickets.setVisibility(TextView.GONE);
        CardAdapter.setCardBackground(this, cardView, tickets, card, cardArtwork);

        TextView alias = findViewById(R.id.card_item_alias);
        if (card.getAlias() != null && !card.getAlias().isBlank()) {
            alias.setText(card.getAlias());
            alias.setVisibility(TextView.VISIBLE);
        } else {
            alias.setVisibility(TextView.GONE);
        }
        TextView uid = findViewById(R.id.card_item_uid);
        uid.setText(getString(
                R.string.uid_format,
                CardUidFormatter.formatUid(card.getUidString(), CardUidFormatter.shouldHideCardUid(preferences))
        ));
        TextView cash = findViewById(R.id.card_item_cash);
        cash.setText(getString(R.string.money_mxn_format, (float) card.getWalletValue() / 100));
        TextView expiry = findViewById(R.id.card_item_expiry);
        LocalDate expirationDate = card.getUser().getProfile().getExpirationDate() != null ?
                card.getUser().getProfile().getExpirationDate() :
                card.getEmission().getExpirationDate();
        expiry.setText(getString(
                R.string.card_expiration,
                expirationDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
        ));
    }


    void setCard() {
        Emission emission = card.getEmission();

        TextView productionDate = findViewById(R.id.card_production_date);
        TextView country        = findViewById(R.id.card_country);
        TextView systemSn       = findViewById(R.id.system_sn);
        TextView expirationDate = findViewById(R.id.card_expiration_date);
        TextView networkOwner   = findViewById(R.id.network_owner);
        TextView networkCompany = findViewById(R.id.network_company);
        TextView networkIssuer  = findViewById(R.id.network_issuer_id);
        TextView distributorId  = findViewById(R.id.network_distributor_id);
        TextView samId          = findViewById(R.id.sam_id);
        TextView samAlgId       = findViewById(R.id.sam_alg_id);
        TextView writeKeysId    = findViewById(R.id.write_keys_id);

        String prodDateStr = (card.getProductionDate() != null)
                ? card.getProductionDate().format(DateTimeFormatter.ISO_WEEK_DATE)
                : getString(R.string.unknown);

        productionDate.setText(prodDateStr);

        country.setText(emission.getCountry().getDisplayCountry(Locale.getDefault()));

        systemSn.setText(String.valueOf(emission.getSerialNumber()));

        expirationDate.setText(formatDate(emission.getExpirationDate()));

        networkOwner.setText(String.valueOf(emission.getApplicationOwner().getNetworkId()));

        networkCompany.setText(String.valueOf(emission.getApplicationOwner().getCompanyId()));

        networkIssuer.setText(String.valueOf(emission.getIssuer().getNetworkId()));

        distributorId.setText(String.valueOf(emission.getIssuer().getDistributorId()));

        samId.setText(String.format(Locale.ROOT, "%014X", emission.getSecurityVersion().getSamUid()));

        samAlgId.setText(String.valueOf(emission.getSecurityVersion().getAlgorithmId()));

        writeKeysId.setText(String.valueOf(emission.getSecurityVersion().getKeyVersion()));
    }

    void setUser() {
        User user = card.getUser();
        View userNameField = findViewById(R.id.user_name_field);
        View userHolderIdField = findViewById(R.id.user_holder_id_field);
        View userBirthDateField = findViewById(R.id.user_birth_date_field);
        View userProfileTypeField = findViewById(R.id.user_profile_type_field);
        View userProfileExpirationField = findViewById(R.id.user_profile_expiration_field);
        TextView userName = findViewById(R.id.user_name);
        TextView userHolderId = findViewById(R.id.user_holder_id);
        TextView userBirthDate = findViewById(R.id.user_birth_date);
        TextView userProfileType = findViewById(R.id.user_profile_type);
        TextView userProfileExpiration = findViewById(R.id.user_profile_expiration);

        setDashboardField(userNameField, userName, user.getProfile().getName());
        setDashboardField(userHolderIdField, userHolderId, user.getProfile().getCredential());
        setDashboardField(userBirthDateField, userBirthDate,
                user.getBirthDate() == null ? null : formatDate(user.getBirthDate()));
        setDashboardField(userProfileExpirationField, userProfileExpiration,
                user.getProfile().getExpirationDate() == null
                        ? null
                        : formatDate(user.getProfile().getExpirationDate()));
        String userProfileTypeString = getProfileTypeString(user.getProfile().getType());
        setDashboardField(userProfileTypeField, userProfileType, userProfileTypeString);
    }

    private void setDashboardField(View field, TextView valueView, String value) {
        if (value == null || value.isBlank() || value.equals(getString(R.string.unknown))) {
            field.setVisibility(View.GONE);
            return;
        }
        valueView.setText(value);
        field.setVisibility(View.VISIBLE);
    }

    private String getProfileTypeString(User.Profile.Type type) {
        if (type == null) {
            return getString(R.string.unknown);
        }
        switch (type) {
            case GENERAL_FARE:
                return getString(R.string.profile_general_fare);
            case VISUALLY_IMPAIRED:
                return getString(R.string.profile_visually_impaired);
            case HEARING_IMPAIRED:
                return getString(R.string.profile_hearing_impaired);
            case EMPLOYEE:
                return getString(R.string.profile_employee);
            case SPEECH_IMPAIRED:
                return getString(R.string.profile_speech_impaired);
            case CHILD_5_TO_12:
                return getString(R.string.profile_child_5_to_12);
            case STUDENT:
                return getString(R.string.profile_student);
            case DISABILITY:
                return getString(R.string.profile_disability);
            case SENIOR_CITIZEN:
                return getString(R.string.profile_senior_citizen);
            case TEACHER:
                return getString(R.string.profile_teacher);
            case SECURITY:
                return getString(R.string.profile_security);
            case SERVICE_ANIMAL:
                return getString(R.string.profile_service_animal);
            case POLICE:
                return getString(R.string.profile_police);
            case VISITOR_PROVIDER:
                return getString(R.string.profile_visitor_provider);
            case BUSINESS:
                return getString(R.string.profile_business);
            case BICYCLE:
                return getString(R.string.profile_bicycle);
            case SOCIAL_SERVICE:
                return getString(R.string.profile_social_service);
            case MENTALLY_IMPAIRED:
                return getString(R.string.profile_mentally_impaired);
            case MOBILITY_IMPAIRED:
                return getString(R.string.profile_mobility_impaired);
            case CHILD_UNDER_5:
                return getString(R.string.profile_child_under_5);
            case FAMILY_MEMBER:
                return getString(R.string.profile_family_member);
            case CLEANING_STAFF:
                return getString(R.string.profile_cleaning_staff);
            case SUPERVISOR:
                return getString(R.string.profile_supervisor);
            case DISABILITY_ASSISTED:
                return getString(R.string.profile_disability_assisted);

            case MISSING_PERSON_RELATIVE:
                return getString(R.string.profile_missing_person_relative);

            case WOMEN_SUPPORT:
                return getString(R.string.profile_women_support);

            case ZAPOPAN:
                return getString(R.string.profile_zapopan);

            case SINGLE_CARD:
                return getString(R.string.profile_single_card);

            case SINGLE_CARD_CHILD:
                return getString(R.string.profile_single_card);
            default:
                return getString(R.string.unknown);
        }
    }

    void setNetwork() {
        Environment environment = card.getEnvironment();
        TextView appVersion = findViewById(R.id.network_app_version);
        appVersion.setText("v" + environment.getApplicationVersion());
        TextView appExpiration = findViewById(R.id.network_app_expiration);
        appExpiration.setText(String.valueOf(environment.getExpirationDate()));
        TextView networkId = findViewById(R.id.network_id);
        networkId.setText(String.valueOf(environment.getNetworkId()));
    }

    void setCardStatus() {
        ApplicationStatus status = card.getApplicationStatus();
        TextView eventCountBadge = findViewById(R.id.card_event_count_badge);
        TextView actionsAppliedView = findViewById(R.id.card_actions_applied);
        TextView cardStatus = findViewById(R.id.card_item_status);

        CardAdapter.setCardStatus(card, cardStatus, this);
        eventCountBadge.setText(getString(R.string.count_badge_format, status.getEventCount()));
        actionsAppliedView.setText(String.valueOf(status.getActionsApplied()));
    }

    void setProducts() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean showDebug = preferences.getBoolean("setting_show_debug_data", false);
        boolean showTechnical = preferences.getBoolean("setting_show_technical_data", false);
        RecyclerView products = findViewById(R.id.card_products_list);
        products.setLayoutManager(new LinearLayoutManager(this));
        products.setVerticalScrollBarEnabled(false);
        products.setAdapter(new ProductAdapter(
                card.getProductList(),
                product -> showPaymentMethodSheet(product, showTechnical, showDebug)
        ));
    }

    private void showPaymentMethodSheet(Product product, boolean showAdvanced, boolean showTechnical) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_payment_method, null, false);
        bindPaymentMethodSheet(sheetView, product, showAdvanced, showTechnical);
        dialog.setContentView(sheetView);
        dialog.show();
    }

    private void bindPaymentMethodSheet(View sheetView, Product product, boolean showAdvanced, boolean showTechnical) {
        TextView title = sheetView.findViewById(R.id.item_pm_title_basic);
        TextView status = sheetView.findViewById(R.id.item_pm_status);
        TextView value = sheetView.findViewById(R.id.item_pm_value);
        TextView validFrom = sheetView.findViewById(R.id.item_pm_valid_from);
        TextView validUntil = sheetView.findViewById(R.id.item_pm_valid_until);
        TextView validTime = sheetView.findViewById(R.id.item_pm_valid_time);

        TextView tripsPerDayOfWeek = sheetView.findViewById(R.id.item_pm_daily_trips_by_week);
        TextView maxTripsPerWeek = sheetView.findViewById(R.id.item_pm_max_trips_per_week);
        TextView allowedTransfers = sheetView.findViewById(R.id.item_pm_allowed_transfers);
        TextView transferTimeLimit = sheetView.findViewById(R.id.item_pm_transfer_time_limit);
        TextView allowedPassbacks = sheetView.findViewById(R.id.item_pm_max_passbacks);
        TextView passbackMinTime = sheetView.findViewById(R.id.item_pm_passback_min_time);
        TextView restrictedDays = sheetView.findViewById(R.id.item_pm_restricted_days);
        TextView restrictionsEmpty = sheetView.findViewById(R.id.item_pm_restrictions_empty);

        View sectionAdvanced = sheetView.findViewById(R.id.item_pm_section_advanced);
        TextView weekNumber = sheetView.findViewById(R.id.item_pm_week_number);
        TextView totalUses = sheetView.findViewById(R.id.item_pm_total_uses);
        TextView lastValidation = sheetView.findViewById(R.id.item_pm_last_validation);
        TextView lastOperator = sheetView.findViewById(R.id.item_pm_last_operator);
        TextView lastRouteOrStation = sheetView.findViewById(R.id.item_pm_last_route_or_station);
        TextView lastDevice = sheetView.findViewById(R.id.item_pm_last_device);
        TextView priority = sheetView.findViewById(R.id.item_pm_priority);
        TextView advancedEmpty = sheetView.findViewById(R.id.item_pm_advanced_empty);

        View sectionTechnical = sheetView.findViewById(R.id.item_pm_section_technical);
        TextView id = sheetView.findViewById(R.id.item_pm_id);
        TextView distributorNetworkId = sheetView.findViewById(R.id.item_pm_distributor_network_id);
        TextView distributorCompanyId = sheetView.findViewById(R.id.item_pm_distributor_company_id);
        TextView distributionDate = sheetView.findViewById(R.id.item_pm_distribution_date);
        TextView samId = sheetView.findViewById(R.id.item_pm_sam_id);
        TextView distributorDeviceId = sheetView.findViewById(R.id.item_pm_distributor_device_id);
        TextView technicalEmpty = sheetView.findViewById(R.id.item_pm_technical_empty);

        ProductContract contract = product.getContract();
        ProductService service = product.getService();
        ProductContract.Validity validity = contract == null ? null : contract.getValidity();
        ProductContract.Restrictions restrictions = contract == null ? null : contract.getRestrictions();

        ProductService.State state = service == null ? null : service.getState();
        title.setText(formatProductTitle(product));
        status.setText(formatPaymentMethodState(state));
        tintPaymentMethodStatus(status, state);
        value.setText(formatPaymentMethodRow(R.string.payment_method_value, ProductAdapter.formatMethodValue(sheetView, product)));

        setOptionalPaymentMethodRow(validFrom, R.string.payment_method_valid_from,
                validity == null ? null : validity.getValidFrom());
        setOptionalPaymentMethodRow(validUntil, R.string.payment_method_valid_until,
                validity == null ? null : validity.getValidTo());
        boolean hasDailyStart = validity != null && validity.getDailyStartTime() != null;
        boolean hasDailyEnd = validity != null && validity.getDailyEndTime() != null;
        setOptionalPaymentMethodRow(validTime, R.string.payment_method_valid_time,
                hasDailyStart && hasDailyEnd
                        ? validity.getDailyStartTime() + " - " + validity.getDailyEndTime()
                        : null);

        boolean hasRestrictions = false;
        hasRestrictions |= setPositivePaymentMethodIntRow(tripsPerDayOfWeek,
                R.string.payment_method_daily_trips_by_week,
                service == null ? 0 : service.getTripsPerDayOfWeek());
        hasRestrictions |= setPositivePaymentMethodIntRow(maxTripsPerWeek,
                R.string.payment_method_max_trips_per_week,
                restrictions == null ? 0 : restrictions.getMaxTripsPerDayOfWeek());
        hasRestrictions |= setPositivePaymentMethodIntRow(allowedTransfers,
                R.string.payment_method_allowed_transfers,
                restrictions == null ? 0 : restrictions.getAllowedInterchanges());
        hasRestrictions |= setPositivePaymentMethodMinutesRow(transferTimeLimit,
                R.string.payment_method_transfer_time_limit,
                restrictions == null ? 0 : restrictions.getTransferTimeLimitMinutes());
        hasRestrictions |= setPositivePaymentMethodIntRow(allowedPassbacks,
                R.string.payment_method_allowed_passbacks,
                restrictions == null ? 0 : restrictions.getAllowedPassbacks());
        hasRestrictions |= setPositivePaymentMethodMinutesRow(passbackMinTime,
                R.string.payment_method_passback_min_time,
                restrictions == null ? 0 : restrictions.getPassbackTimeMinutes());
        hasRestrictions |= setRestrictedDaysRow(restrictedDays,
                restrictions == null ? 0 : restrictions.getRestrictedDays());
        restrictionsEmpty.setVisibility(hasRestrictions ? View.GONE : View.VISIBLE);

        sectionAdvanced.setVisibility(showAdvanced ? View.VISIBLE : View.GONE);
        if (showAdvanced) {
            boolean hasAdvanced = false;
            hasAdvanced |= setPositivePaymentMethodIntRow(weekNumber,
                    R.string.payment_method_week_number,
                    service == null ? 0 : service.getWeekOfYear());
            hasAdvanced |= setPositivePaymentMethodIntRow(totalUses,
                    R.string.payment_method_total_uses,
                    service == null ? 0 : service.getTotalUsages());
            hasAdvanced |= setOptionalPaymentMethodRow(lastValidation,
                    R.string.payment_method_last_validation,
                    service == null ? null : service.getLastDebitDateTime());
            hasAdvanced |= setNonZeroPaymentMethodIntRow(lastOperator,
                    R.string.payment_method_last_operator,
                    service == null ? 0 : service.getLastDebitEntityId());
            hasAdvanced |= setNonZeroPaymentMethodIntRow(lastRouteOrStation,
                    R.string.payment_method_last_route_or_station,
                    service == null ? 0 : service.getLastDebitRouteStationId());
            hasAdvanced |= setNonZeroPaymentMethodIntRow(lastDevice,
                    R.string.payment_method_last_device,
                    service == null ? 0 : service.getLastDebitDeviceId());
            hasAdvanced |= setPositivePaymentMethodIntRow(priority,
                    R.string.payment_method_priority,
                    product.getPriority());
            advancedEmpty.setVisibility(hasAdvanced ? View.GONE : View.VISIBLE);
        }

        sectionTechnical.setVisibility(showTechnical ? View.VISIBLE : View.GONE);
        if (showTechnical) {
            boolean hasTechnical = false;
            hasTechnical |= setOptionalPaymentMethodRow(id,
                    R.string.payment_method_id,
                    contract == null || contract.getProductId() == 0 ? null : Helpers.longToHex(contract.getProductId(), 4));
            ProductContract.Retailer retailer = contract == null ? null : contract.getRetailer();
            hasTechnical |= setPositivePaymentMethodIntRow(distributorNetworkId,
                    R.string.payment_method_distributor_network_id,
                    retailer == null ? 0 : retailer.getDistributorNetworkId());
            hasTechnical |= setNonZeroPaymentMethodIntRow(distributorCompanyId,
                    R.string.payment_method_distributor_company_id,
                    retailer == null ? 0 : retailer.getDistributorCompanyId());
            ProductContract.DistributionInfo distributionInfo = contract == null ? null : contract.getDistributionInfo();
            hasTechnical |= setOptionalPaymentMethodRow(distributionDate,
                    R.string.payment_method_distribution_date,
                    distributionInfo == null ? null : distributionInfo.getDistributionDateTime());
            hasTechnical |= setOptionalPaymentMethodRow(samId,
                    R.string.payment_method_sam_id,
                    distributionInfo == null || distributionInfo.getSamId() == 0
                            ? null
                            : Helpers.longToHex(distributionInfo.getSamId()));
            hasTechnical |= setNonZeroPaymentMethodIntRow(distributorDeviceId,
                    R.string.payment_method_distributor_device_id,
                    distributionInfo == null ? 0 : distributionInfo.getDistributingDeviceId());
            technicalEmpty.setVisibility(hasTechnical ? View.GONE : View.VISIBLE);
        }
    }

    private String formatProductTitle(Product product) {
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

    private String formatPaymentMethodState(ProductService.State state) {
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

    private void tintPaymentMethodStatus(TextView view, ProductService.State state) {
        int color;
        if (state == ProductService.State.ACTIVATED) {
            color = ContextCompat.getColor(this, R.color.miviaje_success);
        } else if (state == ProductService.State.SUSPENDED) {
            color = ContextCompat.getColor(this, R.color.miviaje_danger);
        } else if (state == ProductService.State.INITIALIZED) {
            color = ContextCompat.getColor(this, R.color.miviaje_warning);
        } else {
            color = MaterialColors.getColor(view, com.google.android.material.R.attr.colorOutlineVariant, 0);
        }
        view.setTextColor(color);
    }

    private String formatPaymentMethodRow(int labelResId, Object value) {
        return getString(R.string.payment_method_row_format, getString(labelResId), value);
    }

    private boolean setOptionalPaymentMethodRow(TextView row, int labelResId, Object value) {
        if (value == null) {
            row.setVisibility(View.GONE);
            return false;
        }
        String text = String.valueOf(value);
        if (text.isEmpty() || text.equals(getString(R.string.unknown))) {
            row.setVisibility(View.GONE);
            return false;
        }
        row.setText(formatPaymentMethodRow(labelResId, text));
        row.setVisibility(View.VISIBLE);
        return true;
    }

    private boolean setPositivePaymentMethodIntRow(TextView row, int labelResId, int value) {
        if (value <= 0) {
            row.setVisibility(View.GONE);
            return false;
        }
        return setOptionalPaymentMethodRow(row, labelResId, value);
    }

    private boolean setNonZeroPaymentMethodIntRow(TextView row, int labelResId, int value) {
        if (value == 0) {
            row.setVisibility(View.GONE);
            return false;
        }
        return setOptionalPaymentMethodRow(row, labelResId, value);
    }

    private boolean setPositivePaymentMethodMinutesRow(TextView row, int labelResId, int minutes) {
        if (minutes <= 0) {
            row.setVisibility(View.GONE);
            return false;
        }
        return setOptionalPaymentMethodRow(row, labelResId, minutes + " min");
    }

    private boolean setRestrictedDaysRow(TextView row, int mask) {
        String days = formatRestrictedDays(mask);
        if (days.equals(getString(R.string.none))) {
            row.setVisibility(View.GONE);
            return false;
        }
        row.setText(formatPaymentMethodRow(R.string.payment_method_restricted_days, days));
        row.setVisibility(View.VISIBLE);
        return true;
    }

    private String formatRestrictedDays(int mask) {
        String[] names = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            if (((mask >> i) & 1) == 1) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(names[i]);
            }
        }
        return sb.length() == 0 ? getString(R.string.none) : sb.toString();
    }

    void setEvents() {
        View eventsLayout = findViewById(R.id.card_events_layout);
        eventsLayout.setOnClickListener(v -> {
            Intent viewAllEvents = new Intent(this, EventsActivity.class);
            viewAllEvents.putExtra("CARD_UID", card.getUid());
            startActivity(viewAllEvents);
        });

        List<Event> eventList = card.getEvents();
        adapter = new EventAdapter();
        RecyclerView eventsRecycler = findViewById(R.id.card_events_recycler);
        eventsRecycler.setLayoutManager(new LinearLayoutManager(this));
        eventsRecycler.setVerticalScrollBarEnabled(false);
        eventsRecycler.setAdapter(adapter);
        adapter.updateEvents(eventList);
    }
}
