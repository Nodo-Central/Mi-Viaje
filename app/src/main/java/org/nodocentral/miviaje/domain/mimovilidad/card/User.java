package org.nodocentral.miviaje.domain.mimovilidad.card;

import androidx.annotation.NonNull;

import java.time.LocalDate;

public class User {
    /**
     * FechaNacimientoUsuario: BirthDate, encoded as BCD 0xYYYYMMDD
     */
    private final LocalDate birthDate;

    /**
     * Perfil: sequence containing the special‐profile code
     */
    private final Profile profile;

    public User(LocalDate birthDate,
                Profile profile) {
        this.birthDate = birthDate;
        this.profile = profile;
    }

    @NonNull
    @Override
    public String toString() {
        return "User[" +
                "birthDate=" + birthDate +
                ", profile=" + profile +
                ']';
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public Profile getProfile() {
        return profile;
    }

    public static class Profile {
        /** ProfileCodeIOP: 6-bit code for special user segment */
        private final Type type;

        /**
         * FechaFinPerfil: EndDate of profile validity (days since 1997-01-01 → LocalDate)
         */
        private final LocalDate expirationDate;

        /**
         * NombreUsuario: owner’s name in UTF-8
         */
        private final String name;

        /**
         * CredencialUsuario: holder ID (e.g., CURP) in UTF-8
         */
        private final String credential;

        public enum Type {
            GENERAL_FARE(0),             // TARIFA_GENERAL
            RFU_1(1),
            RFU_2(2),
            RFU_3(3),
            RFU_4(4),
            RFU_5(5),

            VISUALLY_IMPAIRED(6),        // DISCAPACITADO_VISUAL
            HEARING_IMPAIRED(7),         // DISCAPACITADO_AUDITIVO
            WOMEN_SUPPORT(8),            // MUJERES
            EMPLOYEE(9),                 // EMPLEADO
            SPEECH_IMPAIRED(10),         // DISCAPACITADO_ORAL
            CHILD_5_TO_12(11),           // NINO_5_12
            STUDENT(12),                 // ESTUDIANTE
            TEACHER(13),                 // MAESTRO
            SENIOR_CITIZEN(14),          // ADULTO_MAYOR
            DISABILITY(15),              // DISCAPACITADO
            SECURITY(16),                // SEGURIDAD
            SERVICE_ANIMAL(17),          // ANIMAL_ASISTENCIA

            RFU_18(18),

            DISABILITY_ASSISTED(19),     // DISCAPACIDAD_ASISTIDA
            MISSING_PERSON_RELATIVE(20), // FAM_PER_DESAPARECIDA
            POLICE(21),                  // POLICIA
            VISITOR_PROVIDER(22),        // VISITANTE_PROVEEDOR
            BUSINESS(23),                // NEGOCIO
            BICYCLE(24),                 // BICICLETA

            RFU_25(25),

            SOCIAL_SERVICE(26),          // SERVICIO_SOCIAL
            MENTALLY_IMPAIRED(27),       // DISCAPACITADO_MENTAL
            MOBILITY_IMPAIRED(28),       // DISCAPACITADO_MOTRIZ
            CHILD_UNDER_5(29),           // MENOR_0_5
            FAMILY_MEMBER(30),           // FAMILIAR
            CLEANING_STAFF(31),          // LIMPIEZA
            SUPERVISOR(32),              // SUPERVISOR
            ZAPOPAN(33),                 // ZAPOPAN
            RFU_34(34),
            SINGLE_CARD(35),             // UNICA
            SINGLE_CARD_CHILD(36);       // INFANTE_UNICA

            private final int value;
            Type(int value) {
                this.value = value;
            }

            public int getValue() {
                return value;
            }

            public static Type fromInt(int typeId) {
                for (Type type : Type.values()) {
                    if (type.value == typeId) {
                        return type;
                    }
                }
                throw new IllegalArgumentException("Invalid type: " + typeId);
            }
        }

        public Profile(Type type,
                       LocalDate expirationDate,
                       String name,
                       String credential) {
            this.type = type;
            this.expirationDate = expirationDate;
            this.name = name;
            this.credential = credential;
        }

        @NonNull
        @Override
        public String toString() {
            return "Profile[" +
                    "code=" + type +
                    ", expirationDate=" + expirationDate +
                    ", name=" + name +
                    ", credential=" + credential +
                    ']';
        }

        public Type getType() {
            return type;
        }

        public LocalDate getExpirationDate() {
            return expirationDate;
        }

        public String getName() {
            return name;
        }

        public String getCredential() {
            return credential;
        }
    }
}

