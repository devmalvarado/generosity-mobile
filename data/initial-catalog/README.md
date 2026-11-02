# Initial public-source center catalog

Classification correction: the eight reviewed blood-service records now include
`blood_donation` alongside `health`. The original publication used only `health`;
its recorded digest remains historical. See the [catalog contract](../../docs/imported-catalog-format.md)
for legacy-client compatibility. Research sources and other imported fields are unchanged.

Researched on 2026-11-01 in Google Maps, with supplementary institutional sources where available. This is an initial selection, not a complete census or an endorsement. Search areas: Coapa, central Coyoacán, UNAM/Ciudad Universitaria, San Miguel Chapultepec and surrounding neighborhoods, and Aguascalientes capital. All four categories were searched in each area (20 searches plus one Coapa refinement).

## Files and coverage

- `research.json`: 40 public listings, sources, exact place-pin coordinates, Google place IDs, contact data, available hours and review notes. Unknown fees, admission availability, beneficiary counts and legal names remain null.
- `catalog.json`: 37 records ready for the backend import, all marked `imported`, with no invented needs, ratings, verification, membership, capacity or donation requests.
- Three listings are held for review: Graciela Zubirán (street name and current service discrepancy), Banco de Sangre HMG (entry/pin and address uncertainty), and Residencia Los Ángeles Coyoacán (postal address discrepancy).

| Research category | Researched | Included in local load |
| --- | ---: | ---: |
| Children's homes | 11 | 10 |
| Blood banks | 9 | 8 |
| Older-adult homes | 11 | 10 |
| Community kitchens | 9 | 9 |
| Total | 40 | 37 |

Search areas are discovery labels, not administrative boundaries. Nearby alternatives in Tlalpan, Xochimilco, Cuauhtémoc and Álvaro Obregón retain their actual addresses. Blood banks use the existing `health` app category; the research inventory retains the more precise `blood_bank` category. Commercial hospitals and residences are not assumed to offer free services or to be nonprofits.

## Location and source meaning

Coordinates are the full decimal `!3d` latitude and `!4d` longitude in each Google Maps place link, never the `@` map viewport center. They locate the published pin, not a field-surveyed or independently verified entrance. The Acoxpa pin identifies the hospital. The canonical Google Maps URLs combine that coordinate pair and the observed place ID using Google's [Maps URL format](https://developers.google.com/maps/documentation/urls/get-started). The Amparo URL was reopened and matched the name/address after normalization.

Maps-listed phone numbers are retained in `phoneAsListed`. Unambiguous ten-digit Mexican numbers are normalized to `+52` for dialing. Two ambiguous/local numbers remain in the research notes and are omitted from the imported dialable contact. Contact data and schedules are not guarantees of current operation; confirm admission and donor requirements before referring anyone. No calls, messages or beneficiary data collection were performed. Public reviews, reviewer identities, photographs and ratings were not copied.

The seven included listings without a public phone/email/website are discovery-only imported records supported by a Google Maps reference. Unknown contact is not replaced with an invented organization website. Imported-only/no-needs validation is enforced; the full directory's reviewed publication and staff assignment rules remain separate.

## Local import and release

From `backend/`:

```sh
docker compose exec -T app php artisan catalog:import /catalog/initial-catalog/catalog.json --operator=YOUR_OPERATOR_LABEL
```

Executed locally: 37 additions, leaving 3 pre-existing development centers and their 6 needs intact (40 local centers total). The batch itself adds no needs or staff rights. The operation is audited and repeat imports upsert the same stable IDs. Do not change IDs on later edits. Review updates before re-importing: general reconciliation with newer staff edits remains a foundation backlog item.

Published with explicit owner approval on 2026-11-01: [API and 37-center import](https://github.com/devmalvarado/generosity-mobile/actions/runs/41367588487), followed by [landing publication](https://github.com/devmalvarado/generosity-mobile/actions/runs/41367713476) from main commit `8213725`. The backend was published first; the subsequent automatic API run did not repeat the import. The static landing deployment does not deploy PHP. Review the three held records with the organizations before changing their load status; add them deliberately to `catalog.json` only after resolving their discrepancies. The release was integrated onto current main, preserving its layout, wordmark, example needs and separate legal pages.

`googleMapsUrl` is backend import/directory metadata. The `/api/v1/centers` endpoints retain it, while `/api/v1/sync/catalog` deliberately omits it to preserve strict mobile schema-v1 compatibility. Native maps continue to consume the coordinate pair without provider-specific domain types. Do not feed this backend import file directly to the strict bundled mobile reader.

## Validation

- Backend: `ApiTest`, `InitialCatalogTest`, `CenterApplicationTest`: 34 passing tests; catalog source/pin consistency, atomic rejection, repeated imports, missing-contact guardrails, private intake and old pending-retry compatibility.
- Portal: `tests/center-applications.spec.js`: 16 passing Chromium/WebKit checks, ES/EN, 320/390/768/1280 CSS pixels and 200% text, coordinate pair/range validation and private pending submission.
- Production: all 37 IDs, coordinate pairs, contact values and stored Maps URLs match this batch; the public mobile snapshot remains compatible and has no invented needs. Public health is ready. The form was visually inspected live and its HTML/JS/CSS and four legal pages match the release. Two publication-script checks and generated PHP lint also passed.
- In-person entrance confirmation, phone availability, real-device screen readers and a new authenticated production form submission remain unverified.

## Reviewable inventory

Coordinates below are latitude, longitude. `Held` rows are retained only in the research file.

| Category | Organization | Search area | Public address | Latitude, longitude | Google Maps | Load |
| --- | --- | --- | --- | --- | --- | --- |
| Casa hogar | Casa Hogar Amparo I.A.P. | coyoacan-centro | Ignacio Allende 177, Del Carmen, Coyoacán, 04100 Ciudad de México, CDMX | 19.3562351, -99.1627481 | [Place](https://www.google.com/maps/search/?api=1&query=19.3562351%2C-99.1627481&query_place_id=ChIJG5-pT8H_0YURRoOVOsm2q_w) | Imported locally |
| Casa hogar | CASA HOGAR SANTA INÉS A.C. | coyoacan-centro | Av. Real San Lucas 55, San Lucas, Coyoacán, 04030 Ciudad de México, CDMX | 19.3487145, -99.1539795 | [Place](https://www.google.com/maps/search/?api=1&query=19.3487145%2C-99.1539795&query_place_id=ChIJZVmlNND_0YUROEQRPhBp7CM) | Imported locally |
| Casa hogar | Casa Cuna Tlalpan | coyoacan-centro | Calz. de Tlalpan 1677, San Diego Churubusco, Coyoacán, 04120 Ciudad de México, CDMX | 19.356457, -99.144475 | [Place](https://www.google.com/maps/search/?api=1&query=19.356457%2C-99.144475&query_place_id=ChIJ23jqoDT-0YURNtBk0VXLxsY) | Imported locally |
| Casa hogar | Casa Hogar Graciela Zubirán Villareal | coyoacan-unam | Av. Insurgentes Centro 3700A, Insurgentes Cuicuilco, Coyoacán, 04530 Ciudad de México, CDMX | 19.307556, -99.1862184 | [Place](https://www.google.com/maps/search/?api=1&query=19.307556%2C-99.1862184&query_place_id=ChIJ36DGvHYAzoURt7JRfYutmR8) | Held |
| Casa hogar | Casa Hogar Para Varones CAHOVA DIF Nacional | coyoacan-unam | Cjon. del Río 33, Santa Catarina, Coyoacán, 04010 Ciudad de México, CDMX | 19.3508282, -99.1779068 | [Place](https://www.google.com/maps/search/?api=1&query=19.3508282%2C-99.1779068&query_place_id=ChIJYTySxe__0YURNV_CkjqNiTA) | Imported locally |
| Casa hogar | Albergue Infantil Inés María Gasca A | miguel-hidalgo-san-miguel-chapultepec | Gobernador José Ceballos 16, San Miguel Chapultepec I Sección, Miguel Hidalgo, 11850 Ciudad de México, CDMX | 19.410128, -99.186807 | [Place](https://www.google.com/maps/search/?api=1&query=19.410128%2C-99.186807&query_place_id=ChIJ4VIMVl7_0YURpuVMBt2Kczo) | Imported locally |
| Casa hogar | CASA CUNA "LA PAZ" | miguel-hidalgo-san-miguel-chapultepec | Zamora 53, Colonia Condesa, Cuauhtémoc, 06140 Ciudad de México, CDMX | 19.4177516, -99.1774237 | [Place](https://www.google.com/maps/search/?api=1&query=19.4177516%2C-99.1774237&query_place_id=ChIJuYeFFUX_0YURG6nyX0Ujn-I) | Imported locally |
| Casa hogar | Casa Hogar Dulce Refugio | aguascalientes-capital | El Mocho 511, Ojocaliente III, 20196 Aguascalientes, Ags. | 21.876702, -102.24588 | [Place](https://www.google.com/maps/search/?api=1&query=21.876702%2C-102.24588&query_place_id=ChIJ5dR7U_jxKYQRizNMZHErsEk) | Imported locally |
| Casa hogar | Hogar de la niña Casimira Arteaga | aguascalientes-capital | Av Arqueros 202, Gómez Portugal, 20250 Aguascalientes, Ags. | 21.8707213, -102.270018 | [Place](https://www.google.com/maps/search/?api=1&query=21.8707213%2C-102.270018&query_place_id=ChIJNTIDQuLtKYQRVxasXlWlgT0) | Imported locally |
| Casa hogar | Casa del Niño Villa de La Asunción | aguascalientes-capital | Dr Salvador Quezada Limon 501, Curtidores, 20040 Aguascalientes, Ags. | 21.8865954, -102.3208647 | [Place](https://www.google.com/maps/search/?api=1&query=21.8865954%2C-102.3208647&query_place_id=ChIJG-2D0ZXuKYQRMH9sn8aQkHc) | Imported locally |
| Banco de sangre | Hospital Angeles Acoxpa | coyoacan-coapa | Calz Acoxpa 430, Coapa, Ex-Hacienda Coapa, Tlalpan, 14308 Ciudad de México, CDMX | 19.2999185, -99.1351929 | [Place](https://www.google.com/maps/search/?api=1&query=19.2999185%2C-99.1351929&query_place_id=ChIJe8ykdK8BzoURm1qTr4fQAB4) | Imported locally |
| Banco de sangre | Banco de Sangre HMG | coyoacan-coapa | Calle Árbol del Fuego, El Rosario, Coyoacán, 04380 Ciudad de México, CDMX | 19.3287405, -99.1449497 | [Place](https://www.google.com/maps/search/?api=1&query=19.3287405%2C-99.1449497&query_place_id=ChIJu3_kHQABzoURoayo2bhETBw) | Held |
| Banco de sangre | Servicios Hematológicos | coyoacan-centro | Vito Alessio Robles 23, Agrícola, Florida, Álvaro Obregón, 01030 Ciudad de México, CDMX | 19.3505155, -99.1856241 | [Place](https://www.google.com/maps/search/?api=1&query=19.3505155%2C-99.1856241&query_place_id=ChIJywSpCXT_0YURPy09SxsAu88) | Imported locally |
| Banco de sangre | Banco de sangre Instituto Nacional de Pediatría | coyoacan-unam | Av. del Imán 1261, Insurgentes Cuicuilco, Coyoacán, 04530 Ciudad de México, CDMX | 19.3053431, -99.1852912 | [Place](https://www.google.com/maps/search/?api=1&query=19.3053431%2C-99.1852912&query_place_id=ChIJHUEEAgABzoURRH9WFyUKBDA) | Imported locally |
| Banco de sangre | Banco de sangre perinatologia | miguel-hidalgo-san-miguel-chapultepec | C. Montes Urales 800, Lomas - Virreyes, Lomas de Chapultepec IV Secc, Miguel Hidalgo, 11000 Ciudad de México, CDMX | 19.4231684, -99.2054048 | [Place](https://www.google.com/maps/search/?api=1&query=19.4231684%2C-99.2054048&query_place_id=ChIJAagtT_EB0oURWEsn7SZ9GuQ) | Imported locally |
| Banco de sangre | Banco de Sangre Cruz Roja CDMX | miguel-hidalgo-san-miguel-chapultepec | Benito Pérez Galdós 149, Polanco, Col. Irrigación, Miguel Hidalgo, 11510 Ciudad de México, CDMX | 19.4371121, -99.2087891 | [Place](https://www.google.com/maps/search/?api=1&query=19.4371121%2C-99.2087891&query_place_id=ChIJQW_5GBkC0oURQ2xGwUlM3eM) | Imported locally |
| Banco de sangre | Hemocel | aguascalientes-capital | República de Ecuador 808, Santa Elena, 20230 Aguascalientes, Ags. | 21.870492, -102.3041367 | [Place](https://www.google.com/maps/search/?api=1&query=21.870492%2C-102.3041367&query_place_id=ChIJy-W9z4HuKYQRXbkZG4VTzuA) | Imported locally |
| Banco de sangre | Centro Estatal De Transfusión Sanguinea (CETS) | aguascalientes-capital | Artillero Mier 903, Morelos, Desarrollo Especial Centro de Morelos, 20270 Aguascalientes, Ags. | 21.8574227, -102.25496 | [Place](https://www.google.com/maps/search/?api=1&query=21.8574227%2C-102.25496&query_place_id=ChIJRxacivbtKYQRxMs9u23lL2g) | Imported locally |
| Banco de sangre | Banco de Sangre Vida SC | aguascalientes-capital | Talamantes 410-interior 37, Barrio de San Marcos, 20070 Aguascalientes, Ags. | 21.8795847, -102.3078706 | [Place](https://www.google.com/maps/search/?api=1&query=21.8795847%2C-102.3078706&query_place_id=ChIJVe7IAqbvKYQR6C4wNv_-FTM) | Imported locally |
| Personas mayores | CASA HOGAR JARDIN DE VIDA | coyoacan-coapa | 28 de Diciembre 67 COL, Coapa, Emiliano Zapata, Coyoacán, 04815 Ciudad de México, CDMX | 19.3261168, -99.1372012 | [Place](https://www.google.com/maps/search/?api=1&query=19.3261168%2C-99.1372012&query_place_id=ChIJyeK9GGMBzoUR4UqK3FTgeXo) | Imported locally |
| Personas mayores | Asilo De Ancianos San Martín De Porres | coyoacan-coapa | Canal de Miramontes 2258, Coapa, Avante, Coyoacán, 04460 Ciudad de México, CDMX | 19.3291416, -99.1366 | [Place](https://www.google.com/maps/search/?api=1&query=19.3291416%2C-99.1366&query_place_id=ChIJ-TltVHIBzoURu91pdT3Lwu8) | Imported locally |
| Personas mayores | Residencia Fátima | coyoacan-centro | Av. Miguel Ángel de Quevedo 566-B, Santa Catarina, Coyoacán, 04000 Ciudad de México, CDMX | 19.3453226, -99.1672165 | [Place](https://www.google.com/maps/search/?api=1&query=19.3453226%2C-99.1672165&query_place_id=ChIJCTxhxvr_0YURFj4xpuCo27g) | Imported locally |
| Personas mayores | Residencia Los Ángeles Coyoacan | coyoacan-centro | Cda. Pedregal 82-Bis, Santa Catarina, Coyoacán, 03100 Ciudad de México, CDMX | 19.347611, -99.1757253 | [Place](https://www.google.com/maps/search/?api=1&query=19.347611%2C-99.1757253&query_place_id=ChIJRyk5n-X_0YURQDm1A-jTrnI) | Held |
| Personas mayores | Casa Hogar Nuestra Señora del Camino | coyoacan-unam | Blvd. Adolfo López Mateos 190, Lomas de San Ángel Inn, Álvaro Obregón, 01060 Ciudad de México, CDMX | 19.3457065, -99.201992 | [Place](https://www.google.com/maps/search/?api=1&query=19.3457065%2C-99.201992&query_place_id=ChIJrfIXlA8A0oUR6D0dcN268yM) | Imported locally |
| Personas mayores | Residencia San Francisco | miguel-hidalgo-san-miguel-chapultepec | C. Gobernador Gregorio V. Gelati 20, San Miguel Chapultepec I Sección, Miguel Hidalgo, 11850 Ciudad de México, CDMX | 19.4112751, -99.1848171 | [Place](https://www.google.com/maps/search/?api=1&query=19.4112751%2C-99.1848171&query_place_id=ChIJkZ9Hml7_0YUR05iC_uBVKIE) | Imported locally |
| Personas mayores | Residencial del Bosque | miguel-hidalgo-san-miguel-chapultepec | Gobernador José Ceballos 25, San Miguel Chapultepec I Sección, Miguel Hidalgo, 11850 Ciudad de México, CDMX | 19.409844, -99.187284 | [Place](https://www.google.com/maps/search/?api=1&query=19.409844%2C-99.187284&query_place_id=ChIJSTMEq1__0YUR8UuuTTSpocs) | Imported locally |
| Personas mayores | Asilo Hogar Cosio Ducoing I.A.P. | miguel-hidalgo-san-miguel-chapultepec | Av. Montes Pirineos 750, Lomas de Chapultepec, Miguel Hidalgo, 11000 Ciudad de México, CDMX | 19.4207871, -99.2156699 | [Place](https://www.google.com/maps/search/?api=1&query=19.4207871%2C-99.2156699&query_place_id=ChIJhZ15TowB0oURO54Lhsl4qAQ) | Imported locally |
| Personas mayores | Casa Hogar Cuicacalli | aguascalientes-capital | República de Ecuador 309, Las Américas, 20230 Aguascalientes, Ags. | 21.8697512, -102.2985703 | [Place](https://www.google.com/maps/search/?api=1&query=21.8697512%2C-102.2985703&query_place_id=ChIJkah6Y9XtKYQRMhnBOZBHdiE) | Imported locally |
| Personas mayores | ESTANCIA DE VIDA NUESTRA SEÑORA DE GUADALUPE A.C. | aguascalientes-capital | La Soledad 207, El Llanito, 20240 Aguascalientes, Ags. | 21.8771583, -102.2877286 | [Place](https://www.google.com/maps/search/?api=1&query=21.8771583%2C-102.2877286&query_place_id=ChIJ9bSyNHfuKYQRfvhBAoiQXVI) | Imported locally |
| Personas mayores | Casa hogar divino maestro | aguascalientes-capital | C. Juan de Montoro 406, Zona Centro, 20000 Aguascalientes, Ags. | 21.8819255, -102.2897626 | [Place](https://www.google.com/maps/search/?api=1&query=21.8819255%2C-102.2897626&query_place_id=ChIJrzhkImPvKYQRvZrAuWUxzOQ) | Imported locally |
| Comedor | Comedor Comunitario "Aliméntate" | coyoacan-coapa | And. Rafaela López Aguado Mz 42 Lt 4, Coapa, Carmen Serdán, Coyoacán, 04480 Ciudad de México, CDMX | 19.3198541, -99.1098616 | [Place](https://www.google.com/maps/search/?api=1&query=19.3198541%2C-99.1098616&query_place_id=ChIJT7NSVPIBzoURukteyfZeMV8) | Imported locally |
| Comedor | Nuestro Comedor Santa Ursula | coyoacan-coapa | San León 126-136, Pedregal de Sta Úrsula, Coyoacán, 04600 Ciudad de México, CDMX | 19.3131867, -99.148225 | [Place](https://www.google.com/maps/search/?api=1&query=19.3131867%2C-99.148225&query_place_id=ChIJR9wD9UoAzoURfXenmwTq-vg) | Imported locally |
| Comedor | Comedor Comunitario Belmont | coyoacan-centro | Cjon. San Miguel 29, San Diego Churubusco, Coyoacán, 04120 Ciudad de México, CDMX | 19.3532767, -99.149511 | [Place](https://www.google.com/maps/search/?api=1&query=19.3532767%2C-99.149511&query_place_id=ChIJv6GZSsz_0YURZCULhByiYlM) | Imported locally |
| Comedor | Comedor Popular Las Reynas Del Sur | coyoacan-unam | C. Ilama 56, Pedregal de Santo Domingo, Coyoacán, 04369 Ciudad de México, CDMX | 19.3269906, -99.1714215 | [Place](https://www.google.com/maps/search/?api=1&query=19.3269906%2C-99.1714215&query_place_id=ChIJfX2NuBkAzoURsag7aKl09QM) | Imported locally |
| Comedor | Comedor Popular Como Agua para Chocolate | coyoacan-unam | Canacuate 4, Pedregal de Santo Domingo, Coyoacán, 04369 Ciudad de México, CDMX | 19.3260296, -99.1650711 | [Place](https://www.google.com/maps/search/?api=1&query=19.3260296%2C-99.1650711&query_place_id=ChIJWY96KiMAzoURBGKgMVVYlgo) | Imported locally |
| Comedor | Comedor Comunitario 401 | miguel-hidalgo-alrededores | C. Canario 109, Bellavista, Álvaro Obregón, 01140 Ciudad de México, CDMX | 19.3979365, -99.1927405 | [Place](https://www.google.com/maps/search/?api=1&query=19.3979365%2C-99.1927405&query_place_id=ChIJ10mRqtkB0oURX133kGCEJcI) | Imported locally |
| Comedor | Manos a la olla | miguel-hidalgo-alrededores | Lago Onega 94, Ahuehuetes Anáhuac, Miguel Hidalgo, 11450 Ciudad de México, CDMX | 19.449925, -99.1884883 | [Place](https://www.google.com/maps/search/?api=1&query=19.449925%2C-99.1884883&query_place_id=ChIJuZZSYsn50YURGiYd3LlLKgw) | Imported locally |
| Comedor | Comedor Comunitario El Abuelo | aguascalientes-capital | Prol. Libertad 607, Centro, 20000 Aguascalientes, Ags. | 21.8877771, -102.2995644 | [Place](https://www.google.com/maps/search/?api=1&query=21.8877771%2C-102.2995644&query_place_id=ChIJBzH29mHuKYQR6rYNPb5eIiE) | Imported locally |
| Comedor | Comedor Comunitario San Martin de Tours | aguascalientes-capital | 2a. Privada Prol. Delicias, Ojo de Agua, 20260 Aguascalientes, Ags. | 21.8651372, -102.2769265 | [Place](https://www.google.com/maps/search/?api=1&query=21.8651372%2C-102.2769265&query_place_id=ChIJp1g_amztKYQRStuRgWjuGNw) | Imported locally |
| Casa hogar | Casa Hogar Sumando por Ti | coapa-alrededores | Nicolás Bravo 4, Santa María Tepepan, Xochimilco, 16020 Ciudad de México, CDMX | 19.2779809, -99.1338603 | [Place](https://www.google.com/maps/search/?api=1&query=19.2779809%2C-99.1338603&query_place_id=ChIJ16tGcBABzoUR9wVY2YLWXh0) | Imported locally |
