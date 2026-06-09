package org.nodocentral.miviaje.data.parsers;

import static org.nodocentral.miviaje.Helpers.unsign;
import static org.nodocentral.miviaje.Helpers.unsignLong;
import static org.nodocentral.miviaje.Helpers.unsignShort;

import android.nfc.tech.IsoDep;

import org.nodocentral.miviaje.Helpers;
import org.nodocentral.miviaje.data.nfc.desfire.DesfireManager;
import org.nodocentral.miviaje.data.nfc.responses.ApduResponse;
import org.nodocentral.miviaje.data.nfc.desfire.CardVersion;
import org.nodocentral.miviaje.domain.mimovilidad.card.ApplicationStatus;
import org.nodocentral.miviaje.domain.mimovilidad.card.Card;
import org.nodocentral.miviaje.domain.mimovilidad.card.Emission;
import org.nodocentral.miviaje.domain.mimovilidad.card.Environment;
import org.nodocentral.miviaje.domain.mimovilidad.card.Event;
import org.nodocentral.miviaje.domain.mimovilidad.card.Product;
import org.nodocentral.miviaje.domain.mimovilidad.card.ProductContract;
import org.nodocentral.miviaje.domain.mimovilidad.card.ProductService;
import org.nodocentral.miviaje.domain.mimovilidad.card.User;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

interface MiMovilidadProductReader {
    ProductContract readContract(int contractPointer);
    ProductService readService(int servicePointer);
    int getValue(int valuePointer);
}

public class MiMovilidadParser implements MiMovilidadProductReader {
    final IsoDep iso;
    final DesfireManager manager;
    public static final int JALISCO_DF = 0x484000;
    public static final int EMISION_EF = 0x01;
    public static final int EMISION_FILE_LENGTH_BYTES = 48;
    public static final int ENTORNO_EF = 0x02;
    public static final int ENTORNO_FILE_LENGTH_BYTES = 24;
    public static final int USUARIO_EF = 0x03;
    public static final int USUARIO_FILE_LENGTH_BYTES = 96;
    public static final int ESTADO_APLICACION_EF = 0x04;
    public static final int ESTADO_APLICACION_FILE_LENGTH_BYTES = 23;
    public static final int LISTA_PRODUCTOS_EF = 0x05;
    public static final int LISTA_PRODUCTOS_FILE_LENGTH_BYTES = 57;
    public static final int PRODUCTO_LENGTH_BYTES = 6;
    public static final int PRODUCTO_RFU_LENGTH_BYTES = 9;
    public static final int EVENTOS_EF = 0x06;
    public static final int EVENTOS_RECORD_LENGTH_BYTES = 64;
    public static final int EVENTOS_LENGTH_RECORDS = 9;
    public static final int CONTRATO_MONEDERO_EF = 0x07;
    public static final int SERVICIO_MONEDERO_EF = 0x08;
    public static final int VALOR_MONEDERO_EF = 0x09;
    public static final int CONTRATO_CREDITO_EF = 0x0A;
    public static final int SERVICIO_CREDITO_EF = 0x0B;
    public static final int VALOR_CREDITO_EF = 0x0C;
    public static final int CONTRATO_BPD_EF = 0x0D;
    public static final int SERVICIO_BPD_EF = 0x0E;
    public static final int VALOR_BPD_EF = 0x0F;
    public static final int CONTRATO_FILE_LENGTH_BYTES = 72;
    public static final int SERVICIO_FILE_LENGTH_BYTES = 23;
    public static final int FUNCIONARIO_EF = 0x10;
    public static final int FUNCIONARIO_FILE_LENGTH_BYTES = 23;
    public static final int CONTRATO_BPD2_EF = 0x12;
    public static final int SERVICIO_BPD2_EF = 0x13;
    public static final int VALOR_BPD2_EF = 0x14;

    public static final LocalDate EPOCH_CARD_EXPIRATION = LocalDate.of(1973, 5, 28);
    public static final LocalDate EPOCH_PROFILE_EXPIRATION = LocalDate.of(1975, 4, 9);
    public static final LocalDate EPOCH_SPEC = LocalDate.of(1997, 1, 1);
    public static final LocalDate EPOCH_DATE_TIME_COMPACT = LocalDate.of(1990, 1, 1);
    public static final LocalDate EPOCH_CONTRACT_EXPIRATION = LocalDate.of(1991, 1, 1);

    public MiMovilidadParser(IsoDep iso) throws IOException {
        this.iso = iso;
        this.iso.connect();
        this.manager = new DesfireManager(iso);
        selectApplication();
    }

    public Card readCard() {
        CardVersion version = readVersion();
        Emission emission = readEmission();
        Environment environment = readEnvironment();
        User user = readUser();
        ApplicationStatus status = readApplicationStatus();
        Map<Integer, Product> products = readProducts();
        List<Event> events = readEvents(products);

        return new Card(
                version.getUid(),
                emission,
                environment,
                user,
                status,
                products,
                events,
                null,
                null,
                LocalDateTime.now(),
                version.getProductionDate()
        );
    }

    public CardVersion readVersion() {
        try {
            ApduResponse response;
            response = manager.getVersion();
            return parseVersion(response.getBuffer());
        } catch (IOException e) {
            return null;
        }
    }

    public Emission readEmission() {
        try {
            ByteBuffer emissionBytes = readData(EMISION_EF, EMISION_FILE_LENGTH_BYTES);
            return parseEmission(emissionBytes);
        } catch (IOException e) {
            return null;
        }
    }

    public Environment readEnvironment() {
        try {
            return parseEnvironment(readData(ENTORNO_EF, ENTORNO_FILE_LENGTH_BYTES));
        } catch (IOException e) {
            return null;
        }
    }

    public User readUser() {
        try {
            return parseUser(readData(USUARIO_EF, USUARIO_FILE_LENGTH_BYTES));
        } catch (IOException e) {
            return null;
        }
    }

    public ApplicationStatus readApplicationStatus() {
        try {
            return parseApplicationStatus(readData(ESTADO_APLICACION_EF, ESTADO_APLICACION_FILE_LENGTH_BYTES));
        } catch (IOException e) {
            return null;
        }
    }

    public Map<Integer, Product> readProducts() {
        try {
            return parseProducts(readData(LISTA_PRODUCTOS_EF, LISTA_PRODUCTOS_FILE_LENGTH_BYTES), this);
        } catch (IOException e) {
            return null;
        }
    }

    public List<Event> readEvents(Map<Integer, Product> productList) {
        try {
            return parseEvents(readRecord(EVENTOS_EF, 0, EVENTOS_LENGTH_RECORDS), productList);
        } catch (IOException e) {
            return null;
        }
    }

    public ProductContract readContract(int contractPointer) {
        try {
            return parseProductContract(contractPointer, readData(contractPointer, CONTRATO_FILE_LENGTH_BYTES));
        } catch (IOException e) {
            return null;
        }
    }

    public ProductService readService(int servicePointer) {
        try {
            return parseProductService(readData(servicePointer, CONTRATO_FILE_LENGTH_BYTES));
        } catch (IOException e) {
            return null;
        }
    }

    public float readWalletValue() {
        return (float) getValue(VALOR_MONEDERO_EF) / 100;
    }

    public float readCreditValue() {
        return (float) -getValue(VALOR_CREDITO_EF) / 100;
    }

    public int readDiscountTicketsValue() {
        return getValue(VALOR_BPD_EF);
    }

    public int readDiscountTickets2Value() {
        return getValue(VALOR_BPD2_EF);
    }

    void selectApplication() throws IOException {
        manager.selectApplication(MiMovilidadParser.JALISCO_DF);
    }

    ByteBuffer readData(int file, int length) throws IOException {
        ApduResponse response;
        response = manager.readData(file, 0, length);
        return response.getBuffer();
    }

    public int getValue(int file) {
        try {
            ApduResponse response;
            response = manager.getValue(file);
            ByteBuffer buffer = ByteBuffer.wrap(response.getData()).order(ByteOrder.LITTLE_ENDIAN);
            return buffer.getInt();
        } catch (IOException ignored) {
            return 0;
        }
    }

    ByteBuffer readRecord(int file, int firstRecord, int numRecords) throws IOException {
        ApduResponse response;
        response = manager.readRecords(file, firstRecord, numRecords);
        return response.getBuffer();
    }

    static CardVersion parseVersion(ByteBuffer buffer) {
        CardVersion.Version hardware = new CardVersion.Version(
                unsignShort(buffer.get()),
                unsignShort(buffer.get()),
                unsignShort(buffer.get()),
                unsignShort(buffer.get()),
                unsignShort(buffer.get()),
                unsignShort(buffer.get()),
                unsignShort(buffer.get())
        );
        CardVersion.Version software = new CardVersion.Version(
                unsignShort(buffer.get()),
                unsignShort(buffer.get()),
                unsignShort(buffer.get()),
                unsignShort(buffer.get()),
                unsignShort(buffer.get()),
                unsignShort(buffer.get()),
                unsignShort(buffer.get())
        );
        long uid = (unsignLong(buffer.getInt()) << 8 * 3) |
                (unsignLong(buffer.get()) << 8 * 2) |
                (unsignLong(buffer.get()) << 8) |
                unsignLong(buffer.get());
        long batch = (unsignLong(buffer.getInt()) << 8) | unsignLong(buffer.get());
        int week = Helpers.parseBcd(buffer.get() & 0x7F);
        int year = Helpers.parseBcd(buffer.get());
        return new CardVersion(
                hardware,
                software,
                batch,
                uid,
                week,
                year
        );
    }

    /**
     * Parses a 48-byte Emisión_EF record into an Emission object.
     *
     * @param buffer 48-byte buffer read directly from Emisión_EF.
     * @return Emission instance populated with all constructor fields.
     * @throws IllegalArgumentException if data is null or not 48 bytes long.
     */
    static Emission parseEmission(ByteBuffer buffer) {
        if (buffer == null || buffer.limit() != EMISION_FILE_LENGTH_BYTES) {
            throw new IllegalArgumentException(
                    "Emisión_EF record must be exactly "
                            + EMISION_FILE_LENGTH_BYTES + " bytes long"
            );
        }

        /* Bytes 0–1: CountryNumeric (10 bits valid, stored as 16-bit)
         * We treat both bytes as an unsigned 16-bit integer (0..65535),
         * though only the lower 10 bits truly encode the country code. */
        String countryCode = IsoCountryParser.getShortCode(unsign(buffer.getShort()));

        /* Bytes 2–5: SerialMedioPago, 32-bit unsigned int */
        int serialNumber = buffer.getInt();

        /* Bytes 6–7: EndDate (days since 1990-01-01) as unsigned 16-bit */
        LocalDate expirationDate = Helpers.parseDateCompact(unsign(buffer.getShort()));

        /* Bytes 8–10: ApplicationOwner NetworkId (24-bit unsigned) */
        int ownerNetworkId = ((buffer.get() & 0xFF) << 16)
                | ((buffer.get() & 0xFF) << 8)
                | (buffer.get() & 0xFF);

        /* Bytes 11–12: ApplicationOwner CompanyId (16-bit unsigned) */
        int ownerCompanyId = unsign(buffer.getShort());

        Emission.ApplicationOwner owner = new Emission.ApplicationOwner(ownerNetworkId, ownerCompanyId);

        /* Bytes 13–15: Issuer NetworkId (24-bit unsigned) */
        int issuerNetworkId = ((buffer.get() & 0xFF) << 16)
                | ((buffer.get() & 0xFF) << 8)
                | (buffer.get() & 0xFF);

        /* Bytes 16–17: Issuer DistributorId (16-bit unsigned) */
        int issuerDistributorId = unsign(buffer.getShort());

        Emission.Issuer issuer = new Emission.Issuer(issuerNetworkId, issuerDistributorId);

        /* Bytes 18–24: SecurityVersion.IdSAM (56-bit unsigned)
         * We read seven bytes, pad into a 64-bit long. */
        long securitySamUid = 0L;
        for (int i = 0; i < 7; i++) {
            securitySamUid = (securitySamUid << 8) | unsignLong(buffer.get());
        }

        /* Bytes 25–26: SecurityVersion.IdAlgSeguridad (12 bits valid, stored as 16-bit) */
        int securityAlgorithmId = unsign(buffer.getShort());

        /* Byte 27: SecurityVersion.KeyVersionNumber (8-bit signed, but treat as unsigned if needed) */
        byte securityKeyVersion = buffer.get();

        Emission.SecurityVersion securityVersion = new Emission.SecurityVersion(
                securitySamUid, securityAlgorithmId, securityKeyVersion
        );

        // Bytes 28–47 are RFU (reserved); ignored

        return new Emission(
                countryCode,
                serialNumber,
                expirationDate,
                owner,
                issuer,
                securityVersion
        );
    }

    /**
     * Parses a 24-byte Entorno_EF record and returns an Environment instance.
     *
     * @param buffer 24-byte buffer read directly from the Entorno_EF file.
     * @return an Environment object populated with (applicationVersion, networkId, expirationDate).
     * @throws IllegalArgumentException if data is null or not exactly 24 bytes long.
     */
    static Environment parseEnvironment(ByteBuffer buffer) {
        if (buffer == null || buffer.limit() != ENTORNO_FILE_LENGTH_BYTES) {
            throw new IllegalArgumentException(
                    "Entorno_EF record must be exactly "
                            + ENTORNO_FILE_LENGTH_BYTES + " bytes long"
            );
        }

        // Byte 0: VersionNumber (8 bits = major<<4 | minor)
        int applicationVersion = unsign(buffer.get());

        // Bytes 1–3: NetworkId (24-bit big-endian)
        int networkId = (unsign(buffer.get()) << 16)
                | (unsign(buffer.get()) << 8)
                | unsign(buffer.get());

        // Bytes 4–5: EndDate as "days since 1997-01-01" (16-bit big-endian)
        int daysSinceEpoch = unsign(buffer.getShort());
        LocalDate expirationDate = Helpers.parseDateCompact(daysSinceEpoch);

        return new Environment(applicationVersion, networkId, expirationDate);
    }

    /**
     * Parses a 96-byte Usuario_EF record into a User object.
     *
     * @param buffer Exactly 96 bytes read from Usuario_EF.
     * @return a User instance with (birthDate, profile, profileExpirationDate, name, credential).
     * @throws IllegalArgumentException if data is null or not 96 bytes long.
     */
    static User parseUser(ByteBuffer buffer) {
        if (buffer == null || buffer.limit() != USUARIO_FILE_LENGTH_BYTES) {
            throw new IllegalArgumentException(
                    "Usuario_EF record must be exactly " + USUARIO_FILE_LENGTH_BYTES + " bytes long"
            );
        }

        // ── 1) birthDate (bytes 0–3, BCD “YYYYMMDD”) ────────────────────────────────────
        LocalDate birthDate = Helpers.parseBcdDate(buffer.getInt());

        // ── 2) profileCode (byte 4, lower 6 bits valid) ─────────────────────────────────
        // “CódigoPerfil”: 6-bit value stored in the low bits of data[4].
        int profileCode = (buffer.get() & 0x3F);

        // ── 3) profileExpirationDate (bytes 5–6, 16-bit days since EPOCH) ───────────────
        // “FechaFinPerfil”: unsigned 16-bit big-endian count of days
        int daysSinceEpoch = unsign(buffer.getShort());
        LocalDate profileExpirationDate = Helpers.parseDateCompact(daysSinceEpoch);

        // ── 4) name (bytes 7–45, 39 bytes UTF-8) ────────────────────────────────────────
        //   Indice inicial de name = 7, longitud = 39 bytes
        byte[] nameBytes = new byte[39];
        buffer.get(nameBytes);  // consumes bytes indices 7..45
        String profileName = new String(nameBytes, StandardCharsets.UTF_8)
                .replaceAll("\u0000+$", "")
                .trim();
        profileName = profileName.isBlank() ? null : profileName;

        // ── 5) credential (bytes 46–69, 24 bytes UTF-8) ─────────────────────────────────
        byte[] credBytes = new byte[24];
        buffer.get(credBytes);  // consumes bytes indices 46..69
        String profileCredential = new String(credBytes, StandardCharsets.UTF_8)
                .replaceAll("\u0000+$", "")
                .trim();
        profileCredential = profileCredential.isBlank() ? null : profileCredential;

        User.Profile profile = new User.Profile(
                User.Profile.Type.fromInt(profileCode),
                profileExpirationDate,
                profileName,
                profileCredential
        );

        // ── 6) bytes 70–95 (26 bytes RFU) are ignored ───────────────────────────────────

        return new User(birthDate, profile);
    }

    /**
     * Parses a 23-byte EstadoAplicación_EF record into an ApplicationStatus object.
     * <p>
     * File layout (all fields are big-endian):
     * • Byte 0   (1 byte):   EstadoAplicación (2-bit value, valid range 0..3; stored in a full byte)
     * • Bytes 1–3 (3 bytes): ConsecutivoAplicación (24-bit unsigned integer: number of events)
     * • Byte 4   (1 byte):   NúmeroAcciónAplicada (8-bit unsigned integer)
     * • Bytes 5–22 (18 bytes): RFU (ignored)
     *
     * @param buffer A 23-byte array read from EstadoAplicación_EF.
     * @return An ApplicationStatus(state, eventCount, actionsApplied).
     * @throws IllegalArgumentException if data is null or not exactly 23 bytes long.
     */
    static ApplicationStatus parseApplicationStatus(ByteBuffer buffer) {
        if (buffer == null || buffer.limit() != ESTADO_APLICACION_FILE_LENGTH_BYTES) {
            throw new IllegalArgumentException(
                    "EstadoAplicación_EF record must be exactly "
                            + ESTADO_APLICACION_FILE_LENGTH_BYTES + " bytes long"
            );
        }

        // Byte 0: EstadoAplicación (only lower 2 bits are meaningful: 0..3)
        int state = unsign(buffer.get()) & 0x03;

        // Bytes 1–3: ConsecutivoAplicación (24-bit unsigned big-endian)
        int eventCount = ((unsign(buffer.get()) << 16)
                | unsign(buffer.get()) << 8)
                | unsign(buffer.get());

        // Byte 4: NúmeroAcciónAplicada (0..255)
        int actionsApplied = unsign(buffer.get());

        // Bytes 5–22 (18 bytes) are RFU (ignore)

        return new ApplicationStatus(ApplicationStatus.State.fromInt(state), eventCount, actionsApplied);
    }

    /**
     * Parses 6-byte products into a list of Product objects.
     *
     * @param buffer A 57-byte buffer divided into 6-byte records containing:
     *               [0–1]   = productId (16-bit unsigned),
     *               [2]     = contractPointer (8-bit unsigned),
     *               [3]     = servicePointer (8-bit unsigned),
     *               [4]     = valuePointer   (8-bit unsigned),
     *               [5]     = priority       (8-bit unsigned).
     * @return List<Product>(productId, contractPointer, servicePointer, valuePointer, priority).
     * @throws IllegalArgumentException if data is null or of wrong size.
     */
    static Map<Integer, Product> parseProducts(ByteBuffer buffer, MiMovilidadProductReader reader) {
        if (buffer == null || buffer.limit() != LISTA_PRODUCTOS_FILE_LENGTH_BYTES) {
            throw new IllegalArgumentException(
                    "THe product list must be exactly "
                            + LISTA_PRODUCTOS_FILE_LENGTH_BYTES + " bytes long"
            );
        }
        Map<Integer, Product> products = new HashMap<>();

        for (int i = 0; i < buffer.limit() - PRODUCTO_RFU_LENGTH_BYTES; i += PRODUCTO_LENGTH_BYTES) {
            ByteBuffer productBuffer = ByteBuffer.wrap(buffer.array(), i, PRODUCTO_LENGTH_BYTES);

            // Bytes 0–1: ProductId (16-bit unsigned big-endian)
            short productId = productBuffer.getShort();
            int productIdUnsigned = unsign(productId);

            // Byte 2: ContractPointer (8-bit unsigned)
            int contractPointer = unsign(productBuffer.get());
            ProductContract contract = reader.readContract(contractPointer);

            // Byte 3: ServicePointer (8-bit unsigned)
            int servicePointer = unsign(productBuffer.get());
            ProductService service = reader.readService(servicePointer);

            // Byte 4: ValuePointer (8-bit unsigned)
            int valuePointer = unsign(productBuffer.get());
            int value = reader.getValue(valuePointer);

            // Byte 5: Priority (8-bit unsigned)
            int priority = unsign(productBuffer.get());

            Product product = new Product(
                    productId,
                    value,
                    valuePointer,
                    priority,
                    contract,
                    service
            );

            if (!product.isEmpty())
                products.put(productIdUnsigned, product);
        }

        return products;
    }

    static List<Event> parseEvents(ByteBuffer buffer, Map<Integer, Product> productList) {
        final int SAM_UID_BYTES = 7;
        List<Event> events = new ArrayList<>();

        for (int i = 0; i < buffer.limit() / EVENTOS_RECORD_LENGTH_BYTES; i++) {
            byte[] rec = new byte[EVENTOS_RECORD_LENGTH_BYTES];
            buffer.get(rec);

            ByteBuffer r = ByteBuffer.wrap(rec).order(ByteOrder.BIG_ENDIAN);
            int productId = unsign(r.getShort());
            int productPointer = r.get() & 0x0F;           // low‐nibble, kept for compatibility reasons
            int entityId = unsign(r.getShort());
            LocalDateTime eventDateTime = Helpers.parseDateTimeCompact(r.getInt());
            int eventType = unsign(r.get());
            int amount = unsign(r.getShort());
            int appSeq = (unsign(r.get()) << 16) | (unsign(r.get()) << 8) | unsign(r.get());
            long samUid = 0;
            for (int b = 0; b < SAM_UID_BYTES; b++) {
                samUid = (samUid << 8) | unsignLong(r.get());
            }
            long samTxNumber = r.getLong();
            int deviceId = unsign(r.getShort());
            int locationId = unsign(r.getShort());
            int transportType = r.get() & 0x1F;            // low‐5 bits
            int routeStationId = unsign(r.getShort());
            int journeyLegs = r.get() & 0x0F;            // low‐4 bits
            int timeLimit = r.getInt();
            int passbackCount = r.get() & 0x0F;            // low‐4 bits
            int returnReason = unsign(r.get());
            int deviceTypeId = unsign(r.getShort());

            Product product = productList.get(productId);

            // TODO: Replace all domain objects with room entities to insert straight into the db
            events.add(new Event(
                    productId,
                    productPointer,
                    product,
                    entityId,
                    eventDateTime,
                    Event.Type.fromInt(eventType),
                    amount,
                    appSeq,
                    samUid,
                    samTxNumber,
                    deviceId,
                    locationId,
                    Event.TransportType.fromInt(transportType),
                    routeStationId,
                    journeyLegs,
                    timeLimit,
                    passbackCount,
                    Event.RefundReason.fromInt(returnReason),
                    Event.DeviceType.fromInt(deviceTypeId)
            ));
        }
        return events;
    }

    /**
     * Parses a 72-byte Contrato(Producto)_EF record into a ProductContract object.
     * <p>
     * Offsets (all multi-byte values are Big-Endian):
     * • Bytes  0–2  (3 bytes):  IdRedProducto (distributorNetworkId)
     * • Bytes  3–4  (2 bytes):  IdDistribuidorProducto (distributorCompanyId)
     * • Bytes  5–6  (2 bytes):  IdProducto (productId)
     * • Bytes  7–10 (4 bytes):  SerialProducto (productSerial)
     * • Bytes 11–12 (2 bytes):  PrecioProducto (price, in cents MXN)
     * • Byte   13   (1 byte):   UnidadValorProducto (valueUnit)
     * • Bytes 14–17 (4 bytes):  MínimoValor (minAmountLimit)
     * • Bytes 18–21 (4 bytes):  MáximoValor (maxAmountLimit)
     * • Bytes 22–23 (2 bytes):  NúmeroReactivaciónProducto (reactivationCount)
     * • Bytes 24–25 (2 bytes):  NúmeroAcciónAplicadaProducto (lastAppliedActionNumber)
     * <p>
     * • InformaciónDistribución SEQUENCE (bytes 26–35):
     * – Bytes 26–29 (4 bytes): FechaDistribución (DateTimeCompact)
     * – Bytes 30–33 (4 bytes): IdSAMDistribución (samIdDistribution)
     * – Bytes 34–35 (2 bytes): IdDispositivoDistribución (deviceIdDistribution)
     * <p>
     * • ValidezProducto SEQUENCE (bytes 36–47):
     * – Bytes 36–39 (4 bytes): InicioValidezProducto (DateTimeCompact validFrom)
     * – Bytes 40–43 (4 bytes): FinValidezProducto (DateTimeCompact validTo)
     * – Bytes 44–45 (2 bytes): InicioValidezDía (StartTimeStamp, 11 bits of minutes since midnight)
     * – Bytes 46–47 (2 bytes): FinValidezDía (EndTimeStamp, 11 bits of minutes since midnight)
     * <p>
     * • RestriccionesProducto SEQUENCE (bytes 48–71):
     * – Byte   48   (1 byte):   DíasRestringidos (restrictedDays)
     * – Bytes 49–52 (4 bytes):  MaxViajesDíaSemana (maxTripsPerDayOfWeek)
     * – Bytes 53–54 (2 bytes):  TiempoPassback (passbackTime, minutes)
     * – Byte   55   (1 byte):   PassbacksPermitidos (allowedPassbacks)
     * – Bytes 56–57 (2 bytes):  TiempoTransbordo (transferTimeLimit, minutes)
     * – Byte   58   (1 byte):   TransbordosPermitidos (allowedInterchanges, low 4 bits)
     * – Bytes 59–71 (13 bytes): RFU (ignored)
     *
     * @param contractPointer The pointer to the file on the card.
     * @param buffer            Exactly 72 bytes read from Contrato(Producto)_EF.
     * @return A fully populated ProductContract instance.
     * @throws IllegalArgumentException if data is null or not exactly 72 bytes long.
     */
    static ProductContract parseProductContract(int contractPointer, ByteBuffer buffer) {
        if (buffer == null || buffer.limit() != CONTRATO_FILE_LENGTH_BYTES) {
            throw new IllegalArgumentException(
                    "Contrato(Producto)_EF record must be exactly "
                            + CONTRATO_FILE_LENGTH_BYTES + " bytes long"
            );
        }
        // ── 1) distributorNetworkId (bytes 0–2, 24-bit unsigned) ────────────────────────────
        // We read three bytes, left-pad into an int:
        int distributorNetworkId = (unsign(buffer.get()) << 16)
                | (unsign(buffer.get()) << 8)
                | unsign(buffer.get());

        // ── 2) distributorCompanyId (bytes 3–4, 16-bit unsigned) ───────────────────────────
        short distributorCompanyId = buffer.getShort();

        // ── 3) productId (bytes 5–6, 16-bit unsigned) ───────────────────────────────────────
        short productId = buffer.getShort();

        // ── 4) productSerial (bytes 7–10, 32-bit unsigned) ──────────────────────────────────
        int productSerial = buffer.getInt();

        // ── 5) price (bytes 11–12, 16-bit unsigned) ─────────────────────────────────────────
        short price = buffer.getShort();

        // ── 6) valueUnit (byte 13, 8-bit unsigned) ──────────────────────────────────────────
        int valueUnit = unsign(buffer.get());

        // ── 7) minAmountLimit (bytes 14–17, 32-bit signed-two’s-complement) ─────────────────
        int minAmountLimit = buffer.getInt();

        // ── 8) maxAmountLimit (bytes 18–21, 32-bit signed-two’s-complement) ─────────────────
        int maxAmountLimit = buffer.getInt();

        // ── 9) reactivationCount (bytes 22–23, 16-bit unsigned) ──────────────────────────────
        short reactivationCount = buffer.getShort();

        // ── 10) lastAppliedActionNumber (bytes 24–25, 16-bit unsigned) ──────────────────────
        short lastAppliedActionNumber = buffer.getShort();

        // ── 11) distributionInfo (bytes 26–35) ─────────────────────────────────────────────
        //     FechaDistribución    (bytes 26–29, DateTimeCompact)
        int raw = buffer.getInt();
        LocalDateTime distributionDateTime = Helpers.parseDateTimeCompact(raw);

        //     IdSAMDistribución    (bytes 30–36, 32-bit unsigned)
        long samIdDistribution = getUInt56BE(buffer);

        //     IdDispositivoDistribución (bytes 34–35, 16-bit unsigned)
        short deviceIdDistribution = buffer.getShort();

        ProductContract.Retailer retailer = new ProductContract.Retailer(
                distributorNetworkId,
                distributorCompanyId
        );

        ProductContract.DistributionInfo distributionInfo = new ProductContract.DistributionInfo(
                distributionDateTime,
                samIdDistribution,
                deviceIdDistribution
        );

        // ── 12) validity (bytes 36–47) ──────────────────────────────────────────────────────
        //     validFrom            (bytes 36–39, DateTimeCompact)
        LocalDateTime validFrom = Helpers.parseDateTimeCompact(buffer.getInt());

        //     validTo              (bytes 40–43, DateTimeCompact)
        LocalDateTime validTo;
        validTo = Helpers.parseDateTimeCompact(buffer.getInt());

        //     dailyStartTime       (bytes 44–45, 11-bit StartTimeStamp in minutes since midnight)
        LocalTime dailyStartTime = Helpers.parseTimeCompact(unsign(buffer.getShort()));
        //     dailyEndTime         (bytes 46–47, 11-bit EndTimeStamp in minutes since midnight)
        LocalTime dailyEndTime = Helpers.parseTimeCompact(unsign(buffer.getShort()));
        ProductContract.Validity validity = new ProductContract.Validity(
                validFrom,
                validTo,
                dailyStartTime,
                dailyEndTime
        );

        // ── 13) restrictions (bytes 48–71) ─────────────────────────────────────────────────
        //     restrictedDays       (byte 48, 8 bits; typically a bitmask for days of week)
        byte restrictedDays = buffer.get();
        //     maxTripsPerDayOfWeek (bytes 49–52, 32-bit unsigned)
        int maxTripsPerDayOfWeek = buffer.getInt();
        //     passbackTimeMinutes         (bytes 53–54, 16-bit unsigned, minutes)
        short passbackTimeMinutes = buffer.getShort();
        //     allowedPassbacks     (byte 55, 8-bit unsigned)
        byte allowedPassbacks = buffer.get();
        //     transferTimeLimit    (bytes 56–57, 16-bit unsigned, minutes)
        short transferTimeLimit = buffer.getShort();
        //     allowedInterchanges  (byte 58, lower 4 bits valid)
        byte allowedInterchanges = (byte) (unsign(buffer.get()) & 0x0F);
        ProductContract.Restrictions restrictions = new ProductContract.Restrictions(
                restrictedDays,
                maxTripsPerDayOfWeek,
                passbackTimeMinutes,
                allowedPassbacks,
                transferTimeLimit,
                allowedInterchanges
        );
        // bytes 59–71 are RFU; ignored.

        // ── 14) Construct the ProductContract ───────────────────────────────────────────────
        return new ProductContract(
                productId,
                productSerial,
                contractPointer,
                reactivationCount,
                lastAppliedActionNumber,
                price,
                ProductContract.ValueUnit.fromInt(valueUnit),
                minAmountLimit,
                maxAmountLimit,
                retailer,
                distributionInfo,
                validity,
                restrictions
        );
    }

    /**
     * Parses exactly 23 bytes from the given array into a ProductService.
     * <p>
     * Field layout (all BIG_ENDIAN):
     * Byte 0   : EstadoProducto (2 bits used; stored in a full byte: values 0–3)
     * Byte 1   : NúmeroSemanaAño (6 bits used; stored in a full byte: values 0–63)
     * Bytes 2‒3: NúmeroViajesDíaSemana (signed/unsigned 16-bit)
     * Bytes 4‒5: NúmeroActualUsos (12 bits used; stored in a full 16-bit; mask with 0x0FFF)
     * Bytes 6‒9: FechaUltimoDebito (32-bit compact format; use parseDateTimeCompact)
     * Bytes 10‒11: IdEntidadUltimoDebito (16-bit unsigned)
     * Bytes 12‒13: IdRutaEstaciónUltimoDebito (16-bit unsigned)
     * Bytes 14‒15: IdDispositivoUltimDebito (16-bit unsigned)
     * Bytes 16‒22: RFU (7 bytes, skip)
     *
     * @param buffer Exactly 23 bytes in the layout above.
     * @return a new ProductService instance populated from 'data'.
     * @throws IllegalArgumentException if data is null, length != 23, or state out of [0–3].
     */
    static ProductService parseProductService(ByteBuffer buffer) {
        if (buffer == null || buffer.limit() != 23) {
            throw new IllegalArgumentException("Expected exactly 23 bytes for Servicio(Producto)_EF");
        }

        buffer.order(ByteOrder.BIG_ENDIAN);

        // Byte 0: EstadoProducto (2 bits valid, but occupies one whole byte)
        int state = unsign(buffer.get()) & 0x03;

        // Byte 1: NúmeroSemanaAño (6 bits valid, but occupies one whole byte)
        int weekOfYear = unsign(buffer.get()) & 0x3F;
        // (Optionally you can validate: if (weekOfYear < 1 || weekOfYear > 53) …)

        // Bytes 2‒3: NúmeroViajesDíaSemana (16 bits)
        int tripsPerDayOfWeek = unsign(buffer.getShort());

        // Bytes 4‒5: NúmeroActualUsos (12 bits used; mask out any upper bits)
        int totalUsages = unsign(buffer.getShort()) & 0x0FFF;

        // Bytes 6‒9: FechaUltimoDebito (32-bit compact representation)
        int compactDate = buffer.getInt();
        LocalDateTime lastDebitDateTime = Helpers.parseDateTimeCompact(compactDate);

        // Bytes 10‒11: IdEntidadUltimoDebito (16-bit unsigned)
        int lastDebitEntityId = unsign(buffer.getShort());

        // Bytes 12‒13: IdRutaEstaciónUltimoDebito (16-bit unsigned)
        int lastDebitRouteStationId = unsign(buffer.getShort());

        // Bytes 14‒15: IdDispositivoUltimDebito (16-bit unsigned)
        int lastDebitDeviceId = unsign(buffer.getShort());

        // Bytes 16‒22: RFU (7 bytes) – we simply skip over them
        // ByteBuffer's position is already at 16; advance to 23:
        buffer.position(23);

        return new ProductService(
                ProductService.State.fromInt(state),
                weekOfYear,
                tripsPerDayOfWeek,
                totalUsages,
                lastDebitDateTime,
                lastDebitEntityId,
                lastDebitRouteStationId,
                lastDebitDeviceId
        );
    }

    static long getUInt56BE(ByteBuffer buffer) {
        long value = 0;

        for (int i = 0; i < 7; i++) {
            value = (value << 8) | Byte.toUnsignedLong(buffer.get());
        }

        return value;
    }
}
