package org.nodocentral.miviaje.data.room;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity(tableName = "cards")
public class CardEntity {
    @PrimaryKey
    public long uid;
    public LocalDate productionDate;

    // Custom user defined fields
    public String alias;
    public LocalDateTime lastUpdated;
    public String artworkRef;

    // Emission related fields
    public String country;
    public int serialNumber;
    public LocalDate emissionExpiration;
    public int applicationNetworkId;
    public int applicationCompanyId;
    public int issuerNetworkId;
    public int issuerDistributorId;
    public long samUid;
    public int algorithmId;
    public byte keyVersion;

    // Environment fields
    public int environmentNetworkId;
    public int applicationVersion;
    public LocalDate environmentExpiration;

    // User fields
    public LocalDate userBirthDate;
    public int userProfileType;
    public LocalDate userProfileExpiration;
    public String userName;
    public String userCredential;

    // Application status
    public int applicationStatus;
    public int applicationEventCount;
    public int applicationActionsApplied;
}

