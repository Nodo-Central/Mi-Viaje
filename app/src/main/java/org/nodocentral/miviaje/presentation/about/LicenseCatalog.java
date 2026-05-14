package org.nodocentral.miviaje.presentation.about;

import android.content.res.Resources;

import androidx.annotation.RawRes;

import org.nodocentral.miviaje.R;
import org.nodocentral.miviaje.presentation.adapters.LicensesAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class LicenseCatalog {

    private LicenseCatalog() {
    }

    static List<LicensesAdapter.LicenseItem> createLicenses(Resources resources) {
        List<LicensesAdapter.LicenseItem> licenses = new ArrayList<>();
        licenses.add(new LicensesAdapter.LicenseItem(
                "Apache License 2.0",
                "Licensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License...",
                rawText(resources, R.raw.apache_license_2_0),
                "Mi Viaje, AndroidX, FlexboxLayout, Gson, Material Components, RecyclerView FastScroll, Glide, uCrop",
                "apache 2.0 mi viaje app application source appcompat activity constraintlayout lifecycle livedata viewmodel navigation preference recyclerview room room compiler room testing material flexbox gson fastscroll glide ucrop androidx test junit espresso"
        ));
        licenses.add(new LicensesAdapter.LicenseItem(
                "BSD License",
                "Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met...",
                rawText(resources, R.raw.bsd_license),
                "Glide",
                "bsd glide bumptech image loading"
        ));
        licenses.add(new LicensesAdapter.LicenseItem(
                "MIT License",
                "Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files...",
                rawText(resources, R.raw.mit_license),
                "Glide, Mockito Inline, Robolectric",
                "mit glide bumptech gif decoder mockito inline robolectric test mocks shadows"
        ));
        licenses.add(new LicensesAdapter.LicenseItem(
                "Eclipse Public License 1.0",
                "This Agreement is intended to make the Program available under the terms of this Agreement...",
                rawText(resources, R.raw.epl_license_1_0),
                "JUnit",
                "epl eclipse public license junit test unit assertions runner"
        ));
        return licenses;
    }

    static LicensesAdapter.LicenseItem createLicenseForLibrary(Resources resources,
                                                               String libraryName,
                                                               String licenseName) {
        List<LicensesAdapter.LicenseItem> matchingLicenses = matchingLicenses(resources, licenseName);
        if (matchingLicenses.isEmpty()) {
            return new LicensesAdapter.LicenseItem(
                    licenseName,
                    licenseName,
                    "",
                    libraryName,
                    licenseName
            );
        }

        if (matchingLicenses.size() == 1) {
            LicensesAdapter.LicenseItem license = matchingLicenses.get(0);
            return new LicensesAdapter.LicenseItem(
                    license.getName(),
                    license.getPreview(),
                    license.getFullText(),
                    libraryName,
                    license.getSearchKeywords()
            );
        }

        StringBuilder fullText = new StringBuilder();
        for (LicensesAdapter.LicenseItem license : matchingLicenses) {
            if (fullText.length() > 0) {
                fullText.append("\n\n");
            }
            fullText.append(license.getName())
                    .append("\n\n")
                    .append(license.getFullText());
        }

        return new LicensesAdapter.LicenseItem(
                licenseName,
                licenseName,
                fullText.toString(),
                libraryName,
                licenseName
        );
    }

    private static List<LicensesAdapter.LicenseItem> matchingLicenses(Resources resources, String licenseName) {
        String normalizedLicenseName = licenseName.toLowerCase(Locale.ROOT);
        List<LicensesAdapter.LicenseItem> licenses = createLicenses(resources);
        List<LicensesAdapter.LicenseItem> matchingLicenses = new ArrayList<>();

        addIfContains(matchingLicenses, licenses, normalizedLicenseName, "bsd", "BSD License");
        addIfContains(matchingLicenses, licenses, normalizedLicenseName, "mit", "MIT License");
        addIfContains(matchingLicenses, licenses, normalizedLicenseName, "apache 2.0", "Apache License 2.0");
        addIfContains(matchingLicenses, licenses, normalizedLicenseName, "eclipse public license 1.0", "Eclipse Public License 1.0");

        return matchingLicenses;
    }

    private static void addIfContains(List<LicensesAdapter.LicenseItem> target,
                                      List<LicensesAdapter.LicenseItem> licenses,
                                      String normalizedLicenseName,
                                      String token,
                                      String catalogName) {
        if (!normalizedLicenseName.contains(token)) {
            return;
        }

        for (LicensesAdapter.LicenseItem license : licenses) {
            if (catalogName.equals(license.getName())) {
                target.add(license);
                return;
            }
        }
    }

    private static String rawText(Resources resources, @RawRes int resId) {
        StringBuilder result = new StringBuilder();
        try (InputStream inputStream = resources.openRawResource(resId);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append('\n');
            }
        } catch (IOException exception) {
            return "";
        }
        return result.toString().trim();
    }
}
