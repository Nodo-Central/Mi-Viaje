package org.nodocentral.miviaje.data.parsers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nodocentral.miviaje.Helpers;
import org.robolectric.RobolectricTestRunner;

import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.nodocentral.miviaje.domain.mimovilidad.card.ApplicationStatus;
import org.nodocentral.miviaje.domain.mimovilidad.card.Emission;
import org.nodocentral.miviaje.domain.mimovilidad.card.Environment;
import org.nodocentral.miviaje.domain.mimovilidad.card.Event;
import org.nodocentral.miviaje.domain.mimovilidad.card.Product;
import org.nodocentral.miviaje.domain.mimovilidad.card.ProductContract;
import org.nodocentral.miviaje.domain.mimovilidad.card.ProductService;
import org.nodocentral.miviaje.domain.mimovilidad.card.User;

@RunWith(RobolectricTestRunner.class)
public class MiMovilidadParserTest {
    @Test
    public void parseBcd_decodesValue() {
        assertEquals(42, Helpers.parseBcd(0x42));
    }

    @Test
    public void parseBcdDate_decodesDate() {
        assertEquals(LocalDate.of(2024, 12, 31), Helpers.parseBcdDate(encodeBcdDate(2024, 12, 31)));
    }

    @Test
    public void parseDateCompact_decodesDate() {
        LocalDate date = LocalDate.of(2026, 4, 24);

        assertEquals(date, Helpers.parseDateCompact(encodeDateCompact(date)));
    }

    @Test
    public void parseDateTimeCompact_decodesDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 4, 24, 13, 18, 20);

        assertEquals(dateTime, Helpers.parseDateTimeCompact(encodeDateTimeCompact(dateTime)));
    }

    @Test
    public void parseCompactValues_returnNullForInvalidInput() {
        assertNull(Helpers.parseDateCompact(encodeInvalidDateCompact(2026, 13, 1)));
        assertNull(Helpers.parseDateTimeCompact(encodeInvalidDateTimeCompact(2026, 12, 10, 25, 0, 0)));
        assertNull(Helpers.parseTimeCompact((25 << 11)));
    }

    @Test
    public void parseEmission_mapsRepresentativeRecord() {
        Emission emission = MiMovilidadParser.parseEmission(buildEmissionBuffer());

        assertNotNull(emission);
        assertEquals("MX", emission.getCountry().getCountry());
        assertEquals(123456789, emission.getSerialNumber());
        assertEquals(LocalDate.of(2026, 4, 24), emission.getExpirationDate());
        assertEquals(0x010203, emission.getApplicationOwner().getNetworkId());
        assertEquals(0x0405, emission.getApplicationOwner().getCompanyId());
        assertEquals(0x060708, emission.getIssuer().getNetworkId());
        assertEquals(0x090A, emission.getIssuer().getDistributorId());
        assertEquals(0x01020304050607L, emission.getSecurityVersion().getSamUid());
        assertEquals(0x0B0C, emission.getSecurityVersion().getAlgorithmId());
        assertEquals(0x0D, emission.getSecurityVersion().getKeyVersion());
    }

    @Test
    public void parseEnvironment_mapsRepresentativeRecord() {
        Environment environment = MiMovilidadParser.parseEnvironment(buildEnvironmentBuffer());

        assertNotNull(environment);
        assertEquals(0x21, environment.getApplicationVersion());
        assertEquals(0x010203, environment.getNetworkId());
        assertEquals(LocalDate.of(2026, 4, 24), environment.getExpirationDate());
    }

    @Test
    public void parseApplicationStatus_mapsRepresentativeRecord() {
        ApplicationStatus status = MiMovilidadParser.parseApplicationStatus(buildApplicationStatusBuffer());

        assertNotNull(status);
        assertEquals(ApplicationStatus.State.ACTIVATED, status.getState());
        assertEquals(0x000102, status.getEventCount());
        assertEquals(7, status.getActionsApplied());
    }

    @Test
    public void parseUser_mapsRepresentativeRecord() {
        User user = MiMovilidadParser.parseUser(buildUserBuffer());

        assertNotNull(user);
        assertEquals(LocalDate.of(2001, 2, 3), user.getBirthDate());
        assertEquals(User.Profile.Type.STUDENT, user.getProfile().getType());
        assertEquals(LocalDate.of(2026, 4, 24), user.getProfile().getExpirationDate());
        assertEquals("Ada Lovelace", user.getProfile().getName());
        assertEquals("CURP123", user.getProfile().getCredential());
    }

    @Test
    public void parseProductsAndEvents_linkEventsToProducts() {
        ProductContract contract = buildContract((short) 0x1234, 7);
        ProductService service = buildService(LocalDateTime.of(2026, 4, 24, 11, 22, 10));

        MiMovilidadProductReader reader = new MiMovilidadProductReader() {
            @Override
            public ProductContract readContract(int contractPointer) {
                if (contractPointer == 0) {
                    return null;
                }
                assertEquals(7, contractPointer);
                return contract;
            }

            @Override
            public ProductService readService(int servicePointer) {
                if (servicePointer == 0) {
                    return null;
                }
                assertEquals(8, servicePointer);
                return service;
            }

            @Override
            public int getValue(int valuePointer) {
                if (valuePointer == 0) {
                    return 0;
                }
                assertEquals(9, valuePointer);
                return 1234;
            }
        };

        Map<Integer, Product> products = MiMovilidadParser.parseProducts(buildProductsBuffer(), reader);
        Product product = products.get(0x1234);
        List<Event> events = MiMovilidadParser.parseEvents(buildEventsBuffer(product.getId()), products);

        assertEquals(1, products.size());
        assertNotNull(product);
        assertEquals(1234, product.getValue());
        assertSame(contract, product.getContract());
        assertSame(service, product.getService());

        assertEquals(1, events.size());
        Event event = events.get(0);
        assertEquals(product.getId(), event.getProductId());
        assertSame(product, event.getProduct());
        assertEquals(LocalDateTime.of(2026, 4, 24, 13, 18, 20), event.getEventDateTime());
        assertEquals(Event.Type.PRODUCT_USE, event.getType());
    }

    private static ByteBuffer buildEmissionBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(MiMovilidadParser.EMISION_FILE_LENGTH_BYTES);
        buffer.putShort((short) 484);
        buffer.putInt(123456789);
        buffer.putShort((short) encodeDateCompact(LocalDate.of(2026, 4, 24)));
        buffer.put((byte) 0x01);
        buffer.put((byte) 0x02);
        buffer.put((byte) 0x03);
        buffer.putShort((short) 0x0405);
        buffer.put((byte) 0x06);
        buffer.put((byte) 0x07);
        buffer.put((byte) 0x08);
        buffer.putShort((short) 0x090A);
        buffer.put(new byte[]{1, 2, 3, 4, 5, 6, 7});
        buffer.putShort((short) 0x0B0C);
        buffer.put((byte) 0x0D);
        buffer.position(0);
        return buffer;
    }

    private static ByteBuffer buildEnvironmentBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(MiMovilidadParser.ENTORNO_FILE_LENGTH_BYTES);
        buffer.put((byte) 0x21);
        buffer.put((byte) 0x01);
        buffer.put((byte) 0x02);
        buffer.put((byte) 0x03);
        buffer.putShort((short) encodeDateCompact(LocalDate.of(2026, 4, 24)));
        buffer.position(0);
        return buffer;
    }

    private static ByteBuffer buildApplicationStatusBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(MiMovilidadParser.ESTADO_APLICACION_FILE_LENGTH_BYTES);
        buffer.put((byte) ApplicationStatus.State.ACTIVATED.getValue());
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x01);
        buffer.put((byte) 0x02);
        buffer.put((byte) 7);
        buffer.position(0);
        return buffer;
    }

    private static ByteBuffer buildUserBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(MiMovilidadParser.USUARIO_FILE_LENGTH_BYTES);
        buffer.putInt(encodeBcdDate(2001, 2, 3));
        buffer.put((byte) (User.Profile.Type.STUDENT.getValue()));
        buffer.putShort((short) encodeDateCompact(LocalDate.of(2026, 4, 24)));
        putFixedString(buffer, "Ada Lovelace", 39);
        putFixedString(buffer, "CURP123", 24);
        buffer.position(0);
        return buffer;
    }

    private static ByteBuffer buildProductsBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(MiMovilidadParser.LISTA_PRODUCTOS_FILE_LENGTH_BYTES);
        buffer.putShort((short) 0x1234);
        buffer.put((byte) 7);
        buffer.put((byte) 8);
        buffer.put((byte) 9);
        buffer.put((byte) 5);
        buffer.position(0);
        return buffer;
    }

    private static ByteBuffer buildEventsBuffer(int productId) {
        ByteBuffer buffer = ByteBuffer.allocate(MiMovilidadParser.EVENTOS_RECORD_LENGTH_BYTES);
        buffer.putShort((short) productId);
        buffer.put((byte) 0x05);
        buffer.putShort((short) 99);
        buffer.putInt(encodeDateTimeCompact(LocalDateTime.of(2026, 4, 24, 13, 18, 20)));
        buffer.put((byte) Event.Type.PRODUCT_USE.getValue());
        buffer.putShort((short) 250);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x09);
        buffer.put(new byte[]{1, 2, 3, 4, 5, 6, 7});
        buffer.putLong(55L);
        buffer.putShort((short) 12);
        buffer.putShort((short) 13);
        buffer.put((byte) Event.TransportType.BUS.getValue());
        buffer.putShort((short) 14);
        buffer.put((byte) 2);
        buffer.putInt(3600);
        buffer.put((byte) 1);
        buffer.put((byte) Event.RefundReason.NO_REFUND.getValue());
        buffer.putShort((short) Event.DeviceType.FARE_VALIDATOR.getValue());
        buffer.position(0);
        return buffer;
    }

    private static ProductContract buildContract(short productId, int productPointer) {
        return new ProductContract(
                productId,
                200,
                productPointer,
                (short) 1,
                (short) 2,
                (short) 300,
                ProductContract.ValueUnit.MXN_CENT,
                10,
                1000,
                new ProductContract.Retailer(50, (short) 60),
                new ProductContract.DistributionInfo(LocalDateTime.of(2026, 4, 20, 9, 30), 70, (short) 80),
                new ProductContract.Validity(
                        LocalDateTime.of(2026, 4, 21, 0, 0),
                        LocalDateTime.of(2026, 5, 21, 23, 59),
                        LocalTime.of(5, 0),
                        LocalTime.of(23, 0)
                ),
                new ProductContract.Restrictions((byte) 0, 20, (short) 30, (byte) 2, (short) 45, (byte) 1)
        );
    }

    private static ProductService buildService(LocalDateTime lastDebitDateTime) {
        return new ProductService(
                Product.State.ACTIVE,
                17,
                5,
                9,
                lastDebitDateTime,
                10,
                11,
                12
        );
    }

    private static void putFixedString(ByteBuffer buffer, String value, int size) {
        byte[] bytes = value.getBytes();
        buffer.put(bytes, 0, Math.min(bytes.length, size));
        for (int i = bytes.length; i < size; i++) {
            buffer.put((byte) 0);
        }
    }

    private static int encodeDateCompact(LocalDate date) {
        return ((date.getYear() - 1990) << 9) | (date.getMonthValue() << 5) | date.getDayOfMonth();
    }

    private static int encodeInvalidDateCompact(int year, int month, int day) {
        return ((year - 1990) << 9) | (month << 5) | day;
    }

    private static int encodeDateTimeCompact(LocalDateTime dateTime) {
        return ((dateTime.getYear() - 1990) << 25)
                | (dateTime.getMonthValue() << 21)
                | (dateTime.getDayOfMonth() << 16)
                | (dateTime.getHour() << 11)
                | (dateTime.getMinute() << 5)
                | (dateTime.getSecond() / 2);
    }

    private static int encodeInvalidDateTimeCompact(int year, int month, int day, int hour, int minute, int second) {
        return ((year - 1990) << 25)
                | (month << 21)
                | (day << 16)
                | (hour << 11)
                | (minute << 5)
                | (second / 2);
    }

    private static int encodeBcdDate(int year, int month, int day) {
        return (toBcd(year / 100) << 24)
                | (toBcd(year % 100) << 16)
                | (toBcd(month) << 8)
                | toBcd(day);
    }

    private static int toBcd(int value) {
        return ((value / 10) << 4) | (value % 10);
    }
}
