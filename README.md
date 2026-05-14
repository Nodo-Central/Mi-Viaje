# Mi Viaje

Mi Viaje es una app Android comunitaria para leer tarjetas **Mi Movilidad** por NFC, consultar lo que la tarjeta expone localmente y guardar un historial personal en el dispositivo.

> Proyecto de Nodo Central. No es una aplicación oficial ni está afiliada a Mi Movilidad, SITEUR, Gobierno de Jalisco, operadores o instituciones relacionadas.

## Qué Hace

| Área | Detalle |
| --- | --- |
| Lectura NFC | Usa `IsoDep` y comandos DESFire/APDU para seleccionar la aplicación Jalisco y leer archivos de tarjeta. |
| Tarjetas | Muestra UID, emisión, entorno, usuario, estado de aplicación y fecha de producción cuando están disponibles. |
| Productos | Lee monedero, crédito, boletos y contratos/servicios asociados. |
| Movimientos | Presenta eventos recientes con filtros y datos de transporte cuando el parser puede resolverlos. |
| Guardado local | Persiste tarjetas, productos, eventos y arte en Room. |
| Respaldos | Exporta/importa JSON con esquema versionado, incluyendo tarjetas individuales o toda la base local. |
| Personalización | Alias de tarjetas, fondos integrados, imágenes importadas y temas de color inspirados en rutas. |
| Idiomas | Español base, inglés, japonés, coreano y chino. |

## Privacidad y datos

Mi Viaje no declara permiso de internet. La lectura NFC, el parsing y el historial funcionan en el dispositivo, sin analíticas, Crashlytics, telemetría remota ni llamadas a servidores del proyecto.

La app permite exportar respaldos JSON y logs para que la persona usuaria controle sus datos. Android también puede incluir datos de la app en sus mecanismos normales de respaldo según la configuración del dispositivo, la cuenta y el sistema operativo. Eso no lo inicia Mi Viaje: es comportamiento de plataforma o una acción de la persona usuaria.

Ten cuidado al compartir capturas, respaldos o logs: pueden contener identificadores de tarjeta, datos personales o movimientos. La app no intenta anonimizar lo que decides exportar.

## Estado del proyecto

Mi Viaje está pensada para consulta y experimentación cívica, no para recargar, alterar saldos, autenticar usuarios ni reemplazar canales oficiales. La compatibilidad depende de lo que cada tarjeta permita leer por NFC.

## Requisitos

| Herramienta | Versión |
| --- | --- |
| Android Studio | Reciente, con soporte para Android Gradle Plugin 9 |
| JDK | 17 recomendado para el toolchain de Android |
| Android | `minSdk 26`, `targetSdk 36`, `compileSdk 36` |
| Dispositivo de prueba | Android con NFC |

## Compilar

```bash
./gradlew assembleDebug
```

Instalar en un dispositivo conectado:

```bash
./gradlew installDebug
```

Ejecutar pruebas locales:

```bash
./gradlew testDebugUnitTest
```

Ejecutar pruebas instrumentadas:

```bash
./gradlew connectedDebugAndroidTest
```

Generar APK de release sin firmar:

```bash
./gradlew assembleRelease
```

La firma de release se hace fuera de este repositorio con la llave privada del proyecto.

## Estructura

```text
app/src/main/java/org/nodocentral/miviaje/
├── data/
│   ├── nfc/          # comandos APDU, DESFire y respuestas
│   ├── parsers/      # parser Mi Movilidad e ISO/códigos auxiliares
│   ├── room/         # entidades, DAOs, migraciones y conversores
│   ├── repository/   # acceso a datos persistidos
│   ├── backup/       # exportación, importación y mapeo de esquemas JSON
│   ├── artwork/      # resolución de fondos integrados/importados
│   └── files/        # selector, logs e imágenes locales
├── domain/
│   └── mimovilidad/  # modelos de tarjeta, productos, eventos, rutas y estaciones
└── presentation/
    ├── *Activity     # pantallas principales
    ├── about/        # créditos, licencias y bibliotecas
    └── adapters/     # listas de tarjetas, eventos, productos y licencias
```

Recursos relevantes:

```text
app/src/main/res/
├── layout/       # activities, dialogs, bottom sheets e items
├── drawable*/    # iconos, tarjetas y fondos integrados
├── values*/      # strings, temas, colores, dimensiones y traducciones
├── navigation/   # navegación de la sección Acerca de
├── raw/          # textos de licencias mostrados en la app
└── xml/          # reglas de respaldo del sistema Android
```

## Implementación

| Capa | Piezas principales |
| --- | --- |
| NFC | `MiMovilidadParser`, `DesfireManager`, `ApduCommand`, `DesfireCommand` |
| Dominio | `Card`, `Product`, `Event`, `Route`, `Station`, filtros de eventos |
| Persistencia | Room database `miviaje_db`, versión 6, con migraciones manuales |
| Esquemas Room | Exportados en `app/schemas/` para revisar cambios de base de datos y probar migraciones. |
| Respaldos | JSON `schemaVersion: 3`, validación de campos y compatibilidad con esquemas previos |
| UI | Material Components, AppCompat, Navigation, RecyclerView y ViewBinding |
| Temas | Modo claro/oscuro/sistema, negro puro y paletas configurables |

Dependencias principales:

- AndroidX AppCompat, Activity, ConstraintLayout, Lifecycle, Navigation, RecyclerView y Preference
- Material Components
- Room
- Gson
- Glide
- Flexbox
- RecyclerView FastScroll
- JUnit, Mockito, Robolectric, AndroidX Test y Espresso

## Contribuir

Las contribuciones son bienvenidas. Para cambios grandes, abre primero un issue con contexto; para arreglos pequeños, un pull request directo está bien.

Antes de enviar cambios:

- No incluyas datos reales de tarjetas, respaldos privados, capturas sensibles ni logs con identificadores.
- Mantén separadas las capas de NFC/parsing, persistencia y UI.
- Agrega o actualiza pruebas cuando cambies parsing, migraciones, respaldos o filtros.
- Si cambias entidades o migraciones de Room, revisa y confirma el cambio generado en `app/schemas/`.
- Ejecuta `./gradlew assembleDebug` como verificación mínima.

Consulta [CONTRIBUTING.md](CONTRIBUTING.md).

## Licencia y marca

El código fuente está publicado bajo **Apache License 2.0** (`Apache-2.0`). Consulta [LICENSE](LICENSE).

La identidad del proyecto se maneja aparte: nombre, logos, iconos, capturas, arte oficial y presentación pública están delimitados en [BRANDING.md](BRANDING.md).

Las marcas, nombres, logos, diseños de tarjetas y referencias visuales de terceros no se relicencian bajo Apache-2.0. Consulta [ASSET_NOTICES.md](ASSET_NOTICES.md).

---

Desarrollado por Nodo Central desde Jalisco.
