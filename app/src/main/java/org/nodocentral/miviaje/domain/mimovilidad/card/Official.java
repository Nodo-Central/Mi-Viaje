package org.nodocentral.miviaje.domain.mimovilidad.card;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class Official {
    /**
     * profileData: discretionary field for issuer-specific officer info.
     * OCTET STRING, 23 bytes (144 bits)
     */
    private final byte[] profileData;

    /**
     * @param profileData a 23-byte array containing officer-specific data
     * @throws IllegalArgumentException if profileData is null or not length 23
     */
    public Official(byte[] profileData) {
        if (profileData == null || profileData.length != 23) {
            throw new IllegalArgumentException("profileData must be exactly 23 bytes");
        }
        this.profileData = profileData.clone();
    }

    @NonNull
    @Override
    public String toString() {
        return "Official[" +
                "profileData=" + Arrays.toString(profileData) +
                "]";
    }
}
