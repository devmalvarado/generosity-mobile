# Expanded public-source center catalog

Classification correction: the 31 reviewed blood-service records now include
`blood_donation` alongside `health`. The original publication used only `health`;
its recorded digest remains historical. See the [catalog contract](../../docs/imported-catalog-format.md)
for legacy-client compatibility. Research sources and all other imported fields
are unchanged.

Researched on 2026-11-01: **33 requested zones**, **156 Google Maps searches**, covering all **132 zone/category combinations**. This is an initial selection, not an exhaustive census or an endorsement. Searches include municipal surroundings; the actual address determines the location, not the query text.

**132 place records: 120 published, 12 held for review.** The owner approved publication on 2026-11-01. Production now contains **157 centers**, including the unchanged previous 37-record public batch. The 120 were also imported into the local development backend. See [publication and acceptance evidence](../../docs/releases/catalog-expansion-2026-11-01.md).

## Files

- `research.json`: observed place links, unrounded place-pin coordinates, public contact details, supplementary sources, service distinctions, funding unknowns and exclusion reasons.
- `catalog.json`: 120 records in the existing backend import schema. Every record is `imported`, has an empty needs list, and grants no staff rights or verification.
- `coverage.json`: all 33 × 4 search combinations, selected/held IDs, and links to shared or neighboring matches.
- `searches.json`: queries and candidate listing facts. Search results are leads, not an approved import list.

## Selected records

| Category | Researched | Ready / loaded locally | Held |
| --- | ---: | ---: | ---: |
| Child/foster support | 32 | 31 | 1 |
| Blood donation | 33 | 31 | 2 |
| Older-adult care | 32 | 31 | 1 |
| Food assistance | 35 | 27 | 8 |

| Postal country | Ready / loaded locally |
| --- | ---: |
| Mexico | 64 |
| United States | 37 |
| Spain | 8 |
| Italy | 7 |
| United Kingdom | 4 |

Vatican City was searched in all four categories. The captured alternatives have **Roma, Italia** postal addresses in Maps, including the border-area Dono di Maria listing. No territorial boundary or Vatican-country classification was inferred. They remain explicitly identified as alternatives rather than four confirmed sites inside Vatican City.

## What the data does and does not establish

- Coordinates are the full `!3d` latitude and `!4d` longitude of the observed Google Maps place URL, never the `@` viewport. They reproduce the published pin; an entrance has not been surveyed. Hospital pins identify the hospital, with collection-unit details recorded separately.
- Where a place ID was visible, the public Maps URL combines it with the coordinate pair. Otherwise the observed place URL is retained. Tracking/account query parameters are removed; source links retain the exact pin.
- Phone numbers use international dialing format while `phoneAsListed` preserves the source. Unknown public contacts remain null. No calls, messages, beneficiary records, ratings, photos or reviewer identities were collected.
- `hoursAsListed` is only a search-time opening-status snapshot, not a weekly schedule. It is excluded from the imported data. Admission, fees, capacity and availability remain unknown unless subsequently confirmed.
- Child/foster support includes public contact offices (Treehouse, Olive Crest, St Christopher’s, The Eli Home), with descriptions that do not label those offices as residential homes. Confidential family/shelter addresses are not inferred.
- Food assistance includes two explicitly identified food pantries: The Stewpot’s Malcolm X location and Salvation Army Anaheim. Prepared meals at those two pins are **not** claimed. Dallas prepared meals have a separate record at The Bridge; shelter-resident meals and targeted women/children services are qualified in their descriptions.
- Public/private/mixed **funding origin remains `not_informed` for every record**. An organization’s name, religious affiliation, legal form or government operator does not establish its complete funding mix or permission to accept donations. The Lugo operator’s public-service agreement is preserved as partial evidence, not a current complete funding classification.
- Funding, source provenance, country and research categories are research metadata; this batch does not implement their full database/UI directory feature. Only supported import fields are loaded. The existing `health`, `children_support`, `older_adults` and `community_kitchen` codes are used without changing the strict mobile-v1 schema.

## Coverage and gaps

`Ready N` counts selected matches from that query area; `outside N` flags those whose address is elsewhere. `Related N` refers to a record discovered in another search (shared municipal coverage or a neighboring alternative). `Follow-up` means no eligible record was selected; it does not prove no service exists.

| Search zone | Child/foster support | Blood donation | Older-adult care | Food assistance |
| --- | --- | --- | --- | --- |
| tlalpan-centro | Ready 1 | Ready 1 | Ready 1 | Ready 1 (outside 1); held 1 |
| guadalajara | Ready 1 | Ready 1 | Ready 1 | Ready 1 |
| monterrey | Ready 1 | Ready 1 | Ready 1 | Ready 1 |
| san-pedro-garza-garcia | Ready 1 (outside 1) | Ready 1 | Ready 1 | Ready 1 (outside 1) |
| puebla | Ready 1 | Ready 1 | Ready 1 | Follow-up |
| cholula | Ready 1 | Related 1; held 1 | Ready 1 | Follow-up; held 1 |
| veracruz | Ready 1 | Ready 1 | Ready 1 | Ready 1 |
| boca-del-rio | Ready 1 | Ready 1 | Ready 1 | Follow-up; held 1 |
| ensenada | Ready 1 (outside 1) | Ready 1 | Ready 1 | Ready 1 |
| tijuana | Ready 1 | Ready 1 | Ready 1 | Ready 1 |
| xalapa | Ready 1 | Ready 1 | Ready 1 | Follow-up; held 1 |
| queretaro | Ready 1 | Ready 1 | Ready 1 | Ready 1 |
| san-luis-potosi | Ready 1 | Ready 1 | Ready 1 | Ready 1 |
| guanajuato | Follow-up; held 1 | Ready 1 | Follow-up; held 1 | Follow-up; held 1 |
| leon | Ready 1 | Ready 1 | Ready 1 | Ready 1 |
| manzanillo | Ready 1 | Ready 1 | Ready 1 | Ready 1 |
| puerto-vallarta | Related 1 | Ready 1 | Ready 1 | Ready 1 |
| nuevo-vallarta | Ready 1 (outside 1) | Ready 1 | Ready 1 | Related 1 |
| bahia-de-banderas | Ready 1 | Related 1; held 1 | Related 1 | Ready 1; held 1 |
| dallas | Ready 1 | Ready 1 | Ready 1 | Ready 2 |
| austin | Ready 1 | Ready 1 | Ready 1 | Ready 1 |
| el-paso | Ready 1 | Ready 1 | Ready 1 | Ready 1 |
| seattle | Ready 1 | Ready 1 | Ready 1 | Ready 1 |
| tacoma | Ready 1 | Ready 1 | Ready 1 | Ready 1; held 1 |
| washington-dc | Ready 1 (outside 1) | Ready 1 | Ready 1 | Ready 1 |
| baltimore | Ready 1 (outside 1) | Ready 1 | Ready 1 | Ready 1 |
| los-angeles | Ready 1 | Ready 1 | Ready 1 | Ready 1 |
| anaheim | Ready 1 | Ready 1 (outside 1) | Ready 1 | Ready 1 |
| madrid | Ready 1 | Ready 1 | Ready 1 | Ready 1 |
| lugo | Ready 1 | Ready 1 | Ready 1 | Ready 1 |
| roma | Ready 1 | Ready 1 | Ready 1 | Follow-up; held 1 |
| vaticano | Ready 1 (outside 1) | Ready 1 (outside 1) | Ready 1 (outside 1) | Ready 1 (outside 1) |
| londres | Ready 1 | Ready 1 | Ready 1 | Ready 1 |

Priority gaps: Guanajuato capital children’s homes, older-adult residences and community meals; Xalapa community-meal street address; Puebla/Cholula meal-service qualification; local alternatives to the neighboring San Pedro and Anaheim results. Closed CIANNA and Cosme Olivares listings are not substitutes for current services.

## Held records

| Organization | Search zone | Reason |
| --- | --- | --- |
| Comedor comunitario | tlalpan-centro | Incomplete street description; the listing is outside central Tlalpan. Confirm the service address before referral. |
| Centro Estatal de la Transfusión Sanguínea | cholula | Maps has an inconsistent postal address (11350). Official Puebla sources give Antiguo Camino a Guadalupe Hidalgo 11350, Agua Santa, 72490. Confirm the entrance/pin before loading. |
| Comedor comunitario | cholula | Restaurant listing in Puebla, not Cholula; social-meal service has not been corroborated. |
| Asociacion Asistencial Doña Nico A. C. | boca-del-rio | Address discrepancy: Maps gives Barranca del Muerto 20; independent reports place the community kitchen on Jose Hernandez Sierra. Confirm the current site and pin. |
| Comedor Comunitario "Libertad" | guanajuato | Located in Leon, not Guanajuato capital; restaurant listing does not establish a community-meal service. |
| IMSS Hospital General de Zona No. 33, Bahía Banderas, Nayarit | bahia-de-banderas | The hospital listing lacks a street address and a confirmed blood-donation service at this site. |
| Dives in Misericordia A. C. | bahia-de-banderas | Located in Puerto Vallarta, not Bahia de Banderas; the selected nonprofit listing does not establish a meal service. |
| Nativity House | tacoma | Public day-meal service was reported closed in 2025. Resident meals are a different service; confirm access before listing as a community kitchen. |
| Mensa Serale | roma | Maps gives Via Marsala 109; the current Caritas meal-service page gives 113. Confirm the correct meal entrance before loading. |
| Refugio Para Ancianos Cosme Olivares Torres | guanajuato | Maps marks this place permanently closed; a Guanajuato congressional record also describes its 2023 closure. |
| UV - Comedor Comunitario | xalapa | Maps provides only "cerca de" and a neighborhood. No precise street address; the separate UV university-meal address was not matched to this pin. |
| CIANNA | guanajuato | Maps marks CIANNA permanently closed and gives a different address from the official CAS directory. Do not infer a replacement pin. |

## Local import and validation

Executed using the existing audited `catalog:import` command. Development now has **160 centers**: its 40 previous records plus 120 new ones. The 40 previous records match their pre-import hash; all 120 new record values match the batch. The existing six needs and three staff memberships are unchanged. Re-imports reuse the stable IDs derived from observed Google feature IDs.

```sh
# From backend/; development only.
docker compose exec -T app php artisan catalog:import /catalog/catalog-expansion-2026-11-01/catalog.json --operator=YOUR_OPERATOR_LABEL

docker compose exec -T app composer test -- --filter='CatalogExpansionTest|InitialCatalogTest'
```

Validation: **4 tests, 3,375 assertions passed** on isolated `generosity_test`, including source-pin consistency, all search combinations, held-record exclusions, repeated import, unchanged previous batch and the strict mobile response contract. Additional checks found no place-ID or coordinate duplicates with the prior 40 researched records, validated international dialing formats, and checked coarse country coordinate bounds. Country bounds do not validate municipal boundaries or exact entrances.

No application code, schema, landing layout or dependencies changed. The separate reviewed release/import step completed successfully, preserving the previous production records and excluding all 12 held records. Do not overwrite a published record from a stale source: source ownership and reconciliation remain pending in foundation task 11.5.

## Reviewable location inventory

Coordinates are latitude, longitude. `Ready` is the review eligibility label: those records have been loaded locally and published; `Held` records have not. Funding is unknown for all rows. Detailed contacts, source findings and notes are in `research.json`.

| Country | Search zone | Category | Organization | Public address | Latitude, longitude | Maps | Load |
| --- | --- | --- | --- | --- | --- | --- | --- |
| MX | tlalpan-centro | Child/foster support | Casa Hogar de la Santisima Trinidad IAP | Cjon. San Marcos 92, Tlalpan Centro I, Tlalpan, 14000 Ciudad de México, CDMX | 19.2839106, -99.1719939 | [Place](https://www.google.com/maps/search/?api=1&query=19.2839106%2C-99.1719939&query_place_id=ChIJ46Q-C4wAzoUREIxYBofPzuU) | Ready |
| MX | tlalpan-centro | Blood donation | Banco de Sangre, Instituto Nacional de Cancerología | Av. San Fernando 22, Belisario Domínguez Secc 16, Tlalpan, 14080 Ciudad de México, CDMX | 19.2892422, -99.1603864 | [Place](https://www.google.com/maps/search/?api=1&query=19.2892422%2C-99.1603864&query_place_id=ChIJZ0qwYgABzoURg_YJhZJXjKs) | Ready |
| MX | tlalpan-centro | Food assistance | Comedor comunitario | Cedral casi frente a esquina con, Emiliano Zapata, Tlalpan, CDMX | 19.264827, -99.1633363 | [Place](https://www.google.com/maps/search/?api=1&query=19.264827%2C-99.1633363&query_place_id=ChIJN_u_RJQBzoUR4qKC2f1lmtg) | Held |
| MX | tlalpan-centro | Food assistance | Comedor Comunitario | Corregidora 115A, Miguel Hidalgo 1ra Secc, Tlalpan, 14267 Ciudad de México, CDMX | 19.2904139, -99.1826129 | [Place](https://www.google.com/maps/search/?api=1&query=19.2904139%2C-99.1826129&query_place_id=ChIJwf9ZkX0AzoURW-CAU1hwT1A) | Ready |
| MX | tlalpan-centro | Older-adult care | Casa Hogar Para Ancianos Isabel La Católica | Av. San Fernando 104, Tlalpan Centro I, Tlalpan, 14000 Ciudad de México, CDMX | 19.293195, -99.1705737 | [Place](https://www.google.com/maps/search/?api=1&query=19.293195%2C-99.1705737&query_place_id=ChIJpTZHAGEAzoURRWYvqKenubY) | Ready |
| MX | guadalajara | Child/foster support | Casa Hogar Amigos del Niño de Madre Felicitas | Calle 5 de Mayo 424, Analco, 44450 Guadalajara, Jal. | 20.6676952, -103.3416825 | [Place](https://www.google.com/maps/search/?api=1&query=20.6676952%2C-103.3416825&query_place_id=ChIJn4BSdPaxKIQR-X7yt7WfdO0) | Ready |
| MX | guadalajara | Blood donation | Centro De Transfusiones Y Banco De Sangre | C. Juan Álvarez 945, Artesanos, 44200 Guadalajara, Jal. | 20.68445, -103.357124 | [Place](https://www.google.com/maps/search/?api=1&query=20.68445%2C-103.357124&query_place_id=ChIJ9-Mj8TuxKIQRrfbfmrY0WqQ) | Ready |
| MX | guadalajara | Older-adult care | Asilo Ancianos María Auxiliadora | Centro, C. Mexicaltzingo 1228, Mexicaltzingo, 44100 Guadalajara, Jal. | 20.6671709, -103.3537201 | [Place](https://www.google.com/maps/search/?api=1&query=20.6671709%2C-103.3537201&query_place_id=ChIJ0bo9Ov-xKIQRWpyTE8-b254) | Ready |
| MX | guadalajara | Food assistance | DIF Guadalajara Comedor Comunitario División del Norte | C. Gilberto Ruvalcaba 1505, Santa Elena de La Cruz, 44230 Guadalajara, Jal. | 20.7208438, -103.3381342 | [Place](https://www.google.com/maps/search/?api=1&query=20.7208438%2C-103.3381342&query_place_id=ChIJWwp6e3exKIQRbH-CIfnREWc) | Ready |
| MX | monterrey | Child/foster support | Hogar Ortigosa | Fray Servando Padre Mier 1423, Maria Luisa, Centro, 64060 Monterrey, N.L. | 25.6719388, -100.3350828 | [Place](https://www.google.com/maps/search/?api=1&query=25.6719388%2C-100.3350828&query_place_id=ChIJgzXKmbu_YoYRJgYyucDvQuM) | Ready |
| MX | monterrey | Blood donation | Centro Estatal De la Transfusión Sanguínea de Nuevo León | Avenida Hermosillo 3363, Mitras Centro, 64460 Monterrey, N.L. | 25.6955021, -100.3448287 | [Place](https://www.google.com/maps/search/?api=1&query=25.6955021%2C-100.3448287&query_place_id=ChIJxSV9uuCVYoYR338ql6Bk6Zc) | Ready |
| MX | monterrey | Older-adult care | Casa Hogar Maria Magdalena | Lic. José Benítez 2222, Deportivo Obispado, 64040 Monterrey, N.L. | 25.6784355, -100.3445019 | [Place](https://www.google.com/maps/search/?api=1&query=25.6784355%2C-100.3445019&query_place_id=ChIJm9-wHPuVYoYRpr1ohH_OYqQ) | Ready |
| MX | monterrey | Food assistance | Centro Misionero y comedor gratuito María Siempre Virgen | Escobedo, Mariano Escobedo 1316, Treviño, 64570 Monterrey, N.L. | 25.6865964, -100.3091558 | [Place](https://www.google.com/maps/search/?api=1&query=25.6865964%2C-100.3091558&query_place_id=ChIJm0FsNoOVYoYR82j_eDAphj8) | Ready |
| MX | san-pedro-garza-garcia | Child/foster support | Hogar de los Niños Monterrey Manos que Sanan | Laredo 604, Mitras Nte., 64320 Monterrey, N.L. | 25.7113288, -100.3471761 | [Place](https://www.google.com/maps/search/?api=1&query=25.7113288%2C-100.3471761&query_place_id=ChIJUzk3cLuVYoYRVIr2h8h2AIA) | Ready |
| MX | san-pedro-garza-garcia | Blood donation | Hospital Zambrano Hellion TecSalud | Av. Batallón de San Patricio 112, Real San Agustín, 66278 San Pedro Garza García, N.L. | 25.6470541, -100.3344061 | [Place](https://www.google.com/maps/search/?api=1&query=25.6470541%2C-100.3344061&query_place_id=ChIJuZgZ9Gy-YoYRPM86LZHk0YU) | Ready |
| MX | san-pedro-garza-garcia | Older-adult care | Casa San José | Río Moctezuma 225-S, Del Valle, 66220 San Pedro Garza García, N.L. | 25.6562795, -100.362716 | [Place](https://www.google.com/maps/search/?api=1&query=25.6562795%2C-100.362716&query_place_id=ChIJXVVVVey9YoYRW7BLelfdqnk) | Ready |
| MX | san-pedro-garza-garcia | Food assistance | Comedor San Vicente de Paul | Benito Juárez, La Fama, 66100 Cdad. Santa Catarina, N.L. | 25.6717211, -100.4230062 | [Place](https://www.google.com/maps/search/?api=1&query=25.6717211%2C-100.4230062&query_place_id=ChIJi6QT3uOXYoYR_oN-aoWX1eo) | Ready |
| MX | puebla | Child/foster support | Casa de Asís A.C. - Hogar para niñas | C. Orquídeas 6321, Bugambilias, 72580 Heroica Puebla de Zaragoza, Pue. | 19.007734, -98.2200548 | [Place](https://www.google.com/maps/search/?api=1&query=19.007734%2C-98.2200548&query_place_id=ChIJpc7c36rAz4URucjWmS1MMeI) | Ready |
| MX | puebla | Blood donation | Banco de Sangre Puebla | C. 7 Sur 1510, Centro, 72000 Heroica Puebla de Zaragoza, Pue. | 19.040752, -98.2065981 | [Place](https://www.google.com/maps/search/?api=1&query=19.040752%2C-98.2065981&query_place_id=ChIJf1yJcPLAz4UR57FclLMvajM) | Ready |
| MX | puebla | Older-adult care | Asilos Particular de Caridad para Ancinos San José María de Yermo y Parres | Av 9 Pte 309, Centro histórico de Puebla, 72000 Heroica Puebla de Zaragoza, Pue. | 19.042488, -98.2034225 | [Place](https://www.google.com/maps/search/?api=1&query=19.042488%2C-98.2034225&query_place_id=ChIJ6bNq3dzAz4URCwdUknU1eII) | Ready |
| MX | cholula | Child/foster support | Hogar del Niño Campesino | Calle 2 Nte. 402, Centro, 72760 San Pedro Cholula, Pue. | 19.0634763, -98.3048248 | [Place](https://www.google.com/maps/search/?api=1&query=19.0634763%2C-98.3048248&query_place_id=ChIJsd-V9SbGz4UR-4O0YPR8XgA) | Ready |
| MX | cholula | Blood donation | Centro Estatal de la Transfusión Sanguínea | Colonia Agua Santa, Antiguo Camino, A, Guadalupe Hidalgo, 11350 Heroica Puebla de Zaragoza, Pue. | 18.985027, -98.2411861 | [Place](https://www.google.com/maps/search/?api=1&query=18.985027%2C-98.2411861&query_place_id=ChIJESqZZr64z4URq4NKWPv_Vv0) | Held |
| MX | cholula | Older-adult care | Asilo Vivir de Amor | C. 8 Nte. 3201, Residencial Los Pinos, Barrio de Jesús Tlatempa, 72760 San Pedro Cholula, Pue. | 19.0731545, -98.2944098 | [Place](https://www.google.com/maps/search/?api=1&query=19.0731545%2C-98.2944098&query_place_id=ChIJ_81fOxbGz4URF102jGYTnTQ) | Ready |
| MX | cholula | Food assistance | Comedor comunitario | Romero Rubio 22, San Jerónimo Caleras, 72100 Heroica Puebla de Zaragoza, Pue. | 19.0940662, -98.214666 | [Place](https://www.google.com/maps/search/?api=1&query=19.0940662%2C-98.214666&query_place_id=ChIJ-fqD6gzBz4URO0YxaPrRt78) | Held |
| MX | veracruz | Child/foster support | Ejército De Salvación Hogar de Niños Veracruz | Av Revillagigedo 1507, México, 91756 Veracruz, Ver. | 19.1980917, -96.1511065 | [Place](https://www.google.com/maps/search/?api=1&query=19.1980917%2C-96.1511065&query_place_id=ChIJ5Qtfy6BGw4URXJuLxRDDYKw) | Ready |
| MX | veracruz | Blood donation | Banco de Sangre Clínica Sangre Vital | Av. Cuauhtémoc 623, entre Arrillaga y JF Molina, Pinos, 91870 Veracruz, Ver. | 19.2078947, -96.1616391 | [Place](https://www.google.com/maps/search/?api=1&query=19.2078947%2C-96.1616391&query_place_id=ChIJGZnzTWVFw4UR1ApHnCHiQCU) | Ready |
| MX | veracruz | Older-adult care | Asilo Cogra | C. Cultura 341, Miguel Hidalgo, 91712 Veracruz, Ver. | 19.1933265, -96.1588409 | [Place](https://www.google.com/maps/search/?api=1&query=19.1933265%2C-96.1588409&query_place_id=ChIJBU1SXRtEw4UR86uaH0W6Un0) | Ready |
| MX | veracruz | Food assistance | Hogares por el Amor de Dios, A. C., Divina Providencia | C. Agustín de Iturbide 631, Ricardo Flores Magón, 91700 Veracruz, Ver. | 19.1879071, -96.1295051 | [Place](https://www.google.com/maps/search/?api=1&query=19.1879071%2C-96.1295051&query_place_id=ChIJvb5IpShHw4URL1cFgfe18jc) | Ready |
| MX | boca-del-rio | Child/foster support | Casa hogar niños de fe por su gracia AC | Adalberto Tejeda 32, Lázaro Cárdenas, 94298 Boca del Río, Ver. | 19.1262002, -96.1106128 | [Place](https://www.google.com/maps/search/?api=1&query=19.1262002%2C-96.1106128&query_place_id=ChIJAb41zKxBw4URnjRgPBTqvu4) | Ready |
| MX | boca-del-rio | Blood donation | Centro Estatal de la Transfusión Sanguínea Veracruz | Blvd. Adolfo Ruiz Cortines S/N, Costa Verde, 94294 Boca del Río, Ver. | 19.1648047, -96.1109851 | [Place](https://www.google.com/maps/search/?api=1&query=19.1648047%2C-96.1109851&query_place_id=ChIJKzlyRSdBw4UR5EoYwORTyPs) | Ready |
| MX | boca-del-rio | Older-adult care | La Rueda de la Vida A.C. | 5, Melchor Ocampo, Primero de Mayo Sur, 94297 Boca del Río, Ver. | 19.1374827, -96.1191523 | [Place](https://www.google.com/maps/search/?api=1&query=19.1374827%2C-96.1191523&query_place_id=ChIJIwLgTgZBw4URwOWDkKr6Lrk) | Ready |
| MX | boca-del-rio | Food assistance | Asociacion Asistencial Doña Nico A. C. | Barranca del Muerto 20, Rica, 94298 Boca del Río, Ver. | 19.1261248, -96.1137258 | [Place](https://www.google.com/maps/search/?api=1&query=19.1261248%2C-96.1137258&query_place_id=ChIJ6c6noq1Bw4URmaWIppvUx6A) | Held |
| MX | ensenada | Child/foster support | Casa Gabriel (orfanatorio niños con discapacidad) | Luis Echeverría Álvarez 292, El Maneadero, 22790 Rodolfo Sánchez Taboada, B.C. | 31.7114372, -116.5726839 | [Place](https://www.google.com/maps/search/?api=1&query=31.7114372%2C-116.5726839&query_place_id=ChIJi-9fPf2E2IAR1iUEYE_Drko) | Ready |
| MX | ensenada | Blood donation | UNHE Banco de Sangre | C. Segunda 1765-int. 2A y, Obrera, 22830 Ensenada, B.C. | 31.85896, -116.6119059 | [Place](https://www.google.com/maps/search/?api=1&query=31.85896%2C-116.6119059&query_place_id=ChIJCwcZu66T2IARMwBkdKRhnfY) | Ready |
| MX | ensenada | Older-adult care | Casa Hogar del Anciano Ensenada A.C. | C. 15 131, Zona Centro, 22800 Ensenada, B.C. | 31.8792919, -116.6239734 | [Place](https://www.google.com/maps/search/?api=1&query=31.8792919%2C-116.6239734&query_place_id=ChIJExdZAnWN2IARQ6zSsEXyKrQ) | Ready |
| MX | ensenada | Food assistance | Comedor Caritas Felices | Punta, Prta Palmilla 26, Popular 1989, 22810 Ensenada, B.C. | 31.8889669, -116.5708905 | [Place](https://www.google.com/maps/search/?api=1&query=31.8889669%2C-116.5708905&query_place_id=ChIJWyNF8LuN2IAR94zSojpkUw0) | Ready |
| MX | tijuana | Child/foster support | Hogar de Los Niños | José María Morelos y Pavón 6215, Pedregal de Santa Julia, 22430 Tijuana, B.C. | 32.4966753, -117.06159 | [Place](https://www.google.com/maps/search/?api=1&query=32.4966753%2C-117.06159&query_place_id=ChIJh2Xjv7pJ2YAR11KGNMBvBFo) | Ready |
| MX | tijuana | Blood donation | Banco de Sangre BS San Agustín | Gonzalez ortega, C. Benito Juárez 2da, 22000 Tijuana, B.C. | 32.5347473, -117.0474385 | [Place](https://www.google.com/maps/search/?api=1&query=32.5347473%2C-117.0474385&query_place_id=ChIJaRwqwPhJ2YARbjVyVh1SIxU) | Ready |
| MX | tijuana | Older-adult care | Asilo El Refugio | Cam. del Rancho 4, San Antonio, 22606 Tijuana, B.C. | 32.481449, -117.0779494 | [Place](https://www.google.com/maps/search/?api=1&query=32.481449%2C-117.0779494&query_place_id=ChIJiZB-agA22YARfJM3i9QcznA) | Ready |
| MX | tijuana | Food assistance | Desayunador Salesiano Padre Chava | Av Melchor Ocampo 700, Zona Centro, 22000 Tijuana, B.C. | 32.5367559, -117.032126 | [Place](https://www.google.com/maps/search/?api=1&query=32.5367559%2C-117.032126&query_place_id=ChIJ5SIq3v9I2YARfSni0k-x6oI) | Ready |
| MX | queretaro | Child/foster support | Casa Hogar Rosario de Guzmán | C. Tarahumaras 500, Cerrito Colorado, 76116 Santiago de Querétaro, Qro. | 20.6358063, -100.4647594 | [Place](https://www.google.com/maps/search/?api=1&query=20.6358063%2C-100.4647594&query_place_id=ChIJW_x3WPRQ04URGgfxNsK5OWs) | Ready |
| MX | queretaro | Blood donation | Centro Estatal de Transfusion Sanguinea | José Antonio García Jimeno 1000, Jardines de la Hacienda, 76188 Santiago de Querétaro, Qro. | 20.5746008, -100.4170274 | [Place](https://www.google.com/maps/search/?api=1&query=20.5746008%2C-100.4170274&query_place_id=ChIJ37lz4zFF04URmKMTNo_yXpk) | Ready |
| MX | queretaro | Older-adult care | Asilo Divina Providencia | Calle J. de la Barrera 10 A, Niños Heroes, 76010 Santiago de Querétaro, Qro. | 20.5864691, -100.4039949 | [Place](https://www.google.com/maps/search/?api=1&query=20.5864691%2C-100.4039949&query_place_id=ChIJF2LkSy1F04UR7s_EcMpdTsY) | Ready |
| MX | queretaro | Food assistance | Ayudando | C/ 15 de Mayo 52, Centro, 76029 Santiago de Querétaro, Qro. | 20.596652, -100.3915838 | [Place](https://www.google.com/maps/search/?api=1&query=20.596652%2C-100.3915838&query_place_id=ChIJY4yECylb04URLuesl5XCXZQ) | Ready |
| MX | san-luis-potosi | Child/foster support | Casa Hogar Infantil S.L.P., A.C. | Coronel Ontañón 910, Independencia, 78330 San Luis Potosí, S.L.P. | 22.1411892, -100.9815726 | [Place](https://www.google.com/maps/search/?api=1&query=22.1411892%2C-100.9815726&query_place_id=ChIJO1nUGqiYKoQRsYAL6IddbRo) | Ready |
| MX | san-luis-potosi | Blood donation | Centro Estatal de la Transfusión Sanguínea | C. Rosa Blanca 120, El Rosedal, 78120 San Luis Potosí, S.L.P. | 22.191245, -101.0103329 | [Place](https://www.google.com/maps/search/?api=1&query=22.191245%2C-101.0103329&query_place_id=ChIJ8TSotg6fKoQRj13zXGyC-SE) | Ready |
| MX | san-luis-potosi | Older-adult care | Asilo de Ancianos Santa Martha A.C. | Calle Francisco Javier Mina 225, Centro Historico, 78000 San Luis Potosí, S.L.P. | 22.147789, -100.9734344 | [Place](https://www.google.com/maps/search/?api=1&query=22.147789%2C-100.9734344&query_place_id=ChIJOWd4jQOiKoQRScmaTDGcy34) | Ready |
| MX | san-luis-potosi | Food assistance | Comedor de la Esperanza | C. Aurelio Manrique 113, Centro Historico, 78000 San Luis Potosí, S.L.P. | 22.1541996, -100.9813576 | [Place](https://www.google.com/maps/search/?api=1&query=22.1541996%2C-100.9813576&query_place_id=ChIJTS-LYlWfKoQRoGzIUOwSdBQ) | Ready |
| MX | guanajuato | Food assistance | Comedor Comunitario "Libertad" | Libertad, 37548 León de los Aldama, Gto. | 21.0616402, -101.620719 | [Place](https://www.google.com/maps/search/?api=1&query=21.0616402%2C-101.620719&query_place_id=ChIJexrvT-OVK4QRbPtt5LQKJOM) | Held |
| MX | manzanillo | Food assistance | COMEDOR COMUNITARIO | Rio Lerma, Punta Chica, 28220 Manzanillo, Col. | 19.0480624, -104.3013265 | [Place](https://google.com/maps/place/COMEDOR+COMUNITARIO/@19.0480624,-104.3013265,17z/data=!3m1!4b1!4m6!3m5!1s0x8424d5cf00e16e85:0xb9419270610dfd6b!8m2!3d19.0480624!4d-104.3013265!16s%2Fg%2F11sjdy9zbc) | Ready |
| MX | leon | Child/foster support | Casa Hogar San Antonio | Av, C. Guadalupe Oriental 502 A, Oriental, 37510 León de los Aldama, Gto. | 21.1045849, -101.6558182 | [Place](https://www.google.com/maps/search/?api=1&query=21.1045849%2C-101.6558182&query_place_id=ChIJr_MzN1m-K4QRiTo3C18ZBMA) | Ready |
| MX | leon | Blood donation | Centro Estatal de Medicina Transfusional | Blvd. Juan Alonso de Torres 4725 - A, San José del Potrero, Residencial el Carmen, 37296 León de los Aldama, Gto. | 21.1253445, -101.5960852 | [Place](https://www.google.com/maps/search/?api=1&query=21.1253445%2C-101.5960852&query_place_id=ChIJPWqZCIS_K4QR_8whhfaGhMQ) | Ready |
| MX | leon | Older-adult care | Asilo de Ancianos "El Angel Guardián A.C." | C. Artes 146, Col de Santiago, 37000 León de los Aldama, Gto. | 21.1241267, -101.6771888 | [Place](https://www.google.com/maps/search/?api=1&query=21.1241267%2C-101.6771888&query_place_id=ChIJHYN5_Qu_K4QRma4hab55N7w) | Ready |
| MX | leon | Food assistance | DIF Comedor Comunitario | Madre Selva 201, Diez de Mayo, 37549 León de los Aldama, Gto. | 21.068528, -101.6311344 | [Place](https://www.google.com/maps/search/?api=1&query=21.068528%2C-101.6311344&query_place_id=ChIJCXPfw_aVK4QRQvDdOc6Dhxo) | Ready |
| MX | manzanillo | Child/foster support | Casa Hogar Los Angelitos | C. 1 Pedro Flores 16, Las Palmas Salagua, 28869 Manzanillo, Col. | 19.112423, -104.3315932 | [Place](https://www.google.com/maps/search/?api=1&query=19.112423%2C-104.3315932&query_place_id=ChIJJ97_bmLWJIQRvVNuBXay4Wg) | Ready |
| MX | manzanillo | Blood donation | Hospital General Manzanillo | Ejido, Av Elías Zamora S/N, Nuevo Salahua, 28869 Manzanillo, Col. | 19.1165028, -104.3272471 | [Place](https://www.google.com/maps/search/?api=1&query=19.1165028%2C-104.3272471&query_place_id=ChIJY2IiSGHWJIQRY-oiu6_OzIA) | Ready |
| MX | puerto-vallarta | Food assistance | Comedor Comunitario Magisterio - Club Rotario Puerto Vallarta Sur | Sor Juana Inés de la Cruz 558A, El Magisterio, 48290 Puerto Vallarta, Jal. | 20.6722927, -105.2086795 | [Place](https://google.com/maps/place/Comedor+Comunitario+Magisterio+-+Club+Rotario+Puerto+Vallarta+Sur/@20.6722927,-105.2086795,17z/data=!3m1!4b1!4m6!3m5!1s0x84214f2a833fe533:0x58f3347e2795ab2!8m2!3d20.6722927!4d-105.2086795!16s%2Fg%2F11l1xzgmby) | Ready |
| MX | puerto-vallarta | Blood donation | Banco de Sangre Central de Puerto Vallarta | Av Los Tules 140, Díaz Ordaz, 48310 Puerto Vallarta, Jal. | 20.6405496, -105.2318815 | [Place](https://www.google.com/maps/search/?api=1&query=20.6405496%2C-105.2318815&query_place_id=ChIJLffY049FIYQRHKTABm2tJaA) | Ready |
| MX | puerto-vallarta | Older-adult care | Asilo Padre Aldana | C. Paricutín 593, Lomas de San Nicolás, 48290 Puerto Vallarta, Jal. | 20.6738906, -105.1853627 | [Place](https://www.google.com/maps/search/?api=1&query=20.6738906%2C-105.1853627&query_place_id=ChIJ__9L5BdPIYQRYyqWNqf5wes) | Ready |
| MX | nuevo-vallarta | Child/foster support | Casa Hogar Maximo Cornejo Quiroz A.C. | Carretera Tepic 1100, Av. Francisco Medina Ascencio 4764, Guadalupe Victoria, 48317 Puerto Vallarta, Jal. | 20.6877044, -105.235911 | [Place](https://www.google.com/maps/search/?api=1&query=20.6877044%2C-105.235911&query_place_id=ChIJq6qq6uVFIYQRIe_Jkd6jk24) | Ready |
| MX | bahia-de-banderas | Child/foster support | Manos De Amor Por Bahia | Río Balsas #15, Esq, Río Compostela, El Tule Dorado, Flamingos, 63732 Bucerías, Nay. | 20.7570505, -105.3225789 | [Place](https://www.google.com/maps/search/?api=1&query=20.7570505%2C-105.3225789&query_place_id=ChIJT7LvpLdAIYQRKAOzb6Ts6yM) | Ready |
| MX | nuevo-vallarta | Blood donation | Hospital Joya Riviera Nayarit | Paseo de los Cocoteros 55, Nautico Turistico, 63735 Nuevo Nayarit, Nay. | 20.6976746, -105.2957404 | [Place](https://www.google.com/maps/search/?api=1&query=20.6976746%2C-105.2957404&query_place_id=ChIJTRYTn6RGIYQRQTRDA6XV0Fg) | Ready |
| MX | bahia-de-banderas | Blood donation | IMSS Hospital General de Zona No. 33, Bahía Banderas, Nayarit | 63735 San Clemente de Lima, Nay. | 20.7470115, -105.2684589 | [Place](https://www.google.com/maps/search/?api=1&query=20.7470115%2C-105.2684589&query_place_id=ChIJa0QQwQdHIYQRG50KkoLvc6g) | Held |
| MX | nuevo-vallarta | Older-adult care | La Casa de las Lunas Nuevo Nayarit | C. Punta de Mita 108, Fraccionamiento Rinconada de Banderas, 63735 Nuevo Nayarit, Nay. | 20.7122401, -105.2901111 | [Place](https://www.google.com/maps/search/?api=1&query=20.7122401%2C-105.2901111&query_place_id=ChIJUdp17iZHIYQR7Idjjuqh_SU) | Ready |
| MX | bahia-de-banderas | Food assistance | Dives in Misericordia A. C. | Calle Gaviota 162, 24 de Febrero, 48280 Puerto Vallarta, Jal. | 20.711892, -105.213539 | [Place](https://www.google.com/maps/search/?api=1&query=20.711892%2C-105.213539&query_place_id=ChIJmxvnHaRIIYQRke06W1XQZLM) | Held |
| US | dallas | Child/foster support | Buckner Baptist Childrens Home | 5200 Buckner Blvd, Dallas, TX 75227, Estados Unidos | 32.7902697, -96.6831723 | [Place](https://www.google.com/maps/search/?api=1&query=32.7902697%2C-96.6831723&query_place_id=ChIJ0fOCmOqjToYRfl2XshpxMJc) | Ready |
| US | dallas | Blood donation | Carter BloodCare: Baylor Donor Center | 4201 Gaston Ave #110, Dallas, TX 75246, Estados Unidos | 32.7952231, -96.7763712 | [Place](https://www.google.com/maps/search/?api=1&query=32.7952231%2C-96.7763712&query_place_id=ChIJ7XSXfMOYToYRxump9MRI6co) | Ready |
| US | dallas | Older-adult care | Golden Acres Living and Rehabilitation | 2525 Centerville Rd, Dallas, TX 75228, Estados Unidos | 32.8431981, -96.6692393 | [Place](https://www.google.com/maps/search/?api=1&query=32.8431981%2C-96.6692393&query_place_id=ChIJ07POEzKhToYRsfjNH4FGCuM) | Ready |
| US | dallas | Food assistance | The Stewpot | 1610 S Malcolm X Blvd, Dallas, TX 75226, Estados Unidos | 32.7790397, -96.7789507 | [Place](https://www.google.com/maps/search/?api=1&query=32.7790397%2C-96.7789507&query_place_id=ChIJnWtLQR6ZToYRZhOnGgd3KJM) | Ready |
| US | austin | Child/foster support | Helping Hand Home for Children | 3804 Avenue B, Austin, TX 78751, Estados Unidos | 30.3021999, -97.7364751 | [Place](https://www.google.com/maps/search/?api=1&query=30.3021999%2C-97.7364751&query_place_id=ChIJre021X7KRIYRyL5Kx1mC4BA) | Ready |
| US | austin | Blood donation | We Are Blood | 4300 N Lamar Blvd, Austin, TX 78756, Estados Unidos | 30.311847, -97.739286 | [Place](https://www.google.com/maps/search/?api=1&query=30.311847%2C-97.739286&query_place_id=ChIJWSGX3mLKRIYRoL1Onob3b68) | Ready |
| US | austin | Older-adult care | Barton Valley Rehabilitation and Healthcare Center | 4501 Dudmar Dr, Austin, TX 78735, Estados Unidos | 30.2355992, -97.8048887 | [Place](https://www.google.com/maps/search/?api=1&query=30.2355992%2C-97.8048887&query_place_id=ChIJrWPLVIlLW4YRUxXliK7Q6qo) | Ready |
| US | austin | Food assistance | Austin Baptist Chapel- Angel House Soup Kitchen | 908 E Cesar Chavez St, Austin, TX 78702, Estados Unidos | 30.2607946, -97.7348556 | [Place](https://www.google.com/maps/search/?api=1&query=30.2607946%2C-97.7348556&query_place_id=ChIJq7-K6a61RIYROMgtBRNeEAE) | Ready |
| US | el-paso | Child/foster support | Lee & Beulah Moor Children's Home | 1100 E Cliff Dr, El Paso, TX 79902, Estados Unidos | 31.774765, -106.4866485 | [Place](https://www.google.com/maps/search/?api=1&query=31.774765%2C-106.4866485&query_place_id=ChIJyVcXv6xZ54YROWEpMgqXKn0) | Ready |
| US | el-paso | Older-adult care | Nazareth Living Care Center | 1475 Raynolds St, El Paso, TX 79903, Estados Unidos | 31.7881504, -106.433566 | [Place](https://www.google.com/maps/search/?api=1&query=31.7881504%2C-106.433566&query_place_id=ChIJmUUeJiRa54YRz59BJ4EdfjE) | Ready |
| US | el-paso | Blood donation | Vitalant Blood Donation- El Paso | 424 S Mesa Hills Dr, El Paso, TX 79912, Estados Unidos | 31.8176445, -106.5287313 | [Place](https://www.google.com/maps/search/?api=1&query=31.8176445%2C-106.5287313&query_place_id=ChIJm1eop-RX54YRslS1CJuGtak) | Ready |
| US | el-paso | Food assistance | Rescue Mission of EL Paso | 221 N Lee St, El Paso, TX 79901, Estados Unidos | 31.7653859, -106.4713947 | [Place](https://www.google.com/maps/search/?api=1&query=31.7653859%2C-106.4713947&query_place_id=ChIJb-XjbAlZ54YREiWHyDosIM8) | Ready |
| US | seattle | Child/foster support | Treehouse | 2100 24th Ave S #200, Seattle, WA 98144, Estados Unidos | 47.5842222, -122.3012671 | [Place](https://www.google.com/maps/search/?api=1&query=47.5842222%2C-122.3012671&query_place_id=ChIJb5IR6YtqkFQRvEyWqIpdwRE) | Ready |
| US | seattle | Blood donation | Bloodworks Northwest Seattle Central Donor Center | 921 Terry Ave, Seattle, WA 98104, Estados Unidos | 47.6086866, -122.3263551 | [Place](https://www.google.com/maps/search/?api=1&query=47.6086866%2C-122.3263551&query_place_id=ChIJg1NRPLZqkFQRaqiJuPUAR8M) | Ready |
| US | seattle | Older-adult care | Washington Care Center | 2821 S Walden St, Seattle, WA 98144, Estados Unidos | 47.5718893, -122.2956194 | [Place](https://www.google.com/maps/search/?api=1&query=47.5718893%2C-122.2956194&query_place_id=ChIJu1hST2VqkFQRtxCl3XmsH9Y) | Ready |
| US | seattle | Food assistance | St. James Cathedral Kitchen | 901 Terry Ave, Seattle, WA 98104, Estados Unidos | 47.607395, -122.3253782 | [Place](https://www.google.com/maps/search/?api=1&query=47.607395%2C-122.3253782&query_place_id=ChIJyVWy_OprkFQRaGNMkjEEBQQ) | Ready |
| US | tacoma | Child/foster support | Olive Crest | 3516 S 47th St #203, Tacoma, WA 98409, Estados Unidos | 47.2142669, -122.4831973 | [Place](https://www.google.com/maps/search/?api=1&query=47.2142669%2C-122.4831973&query_place_id=ChIJ60J9wun_kFQR3vpbZsSKgMw) | Ready |
| US | tacoma | Blood donation | Bloodworks Northwest Tacoma Donor Center | 2207 N Pearl St, Tacoma, WA 98407, Estados Unidos | 47.2678099, -122.5137153 | [Place](https://www.google.com/maps/search/?api=1&query=47.2678099%2C-122.5137153&query_place_id=ChIJhzr2YABVkFQRDIPs-7xS3W8) | Ready |
| US | tacoma | Older-adult care | Tacoma Nursing & Rehabilitation Center | 2102 96th St S, Tacoma, WA 98444, Estados Unidos | 47.1696011, -122.4651324 | [Place](https://www.google.com/maps/search/?api=1&query=47.1696011%2C-122.4651324&query_place_id=ChIJgy914K8BkVQRGa7ruITiFkU) | Ready |
| US | tacoma | Food assistance | Nativity House | 702 S 14th St, Tacoma, WA 98405, Estados Unidos | 47.2493436, -122.4459621 | [Place](https://www.google.com/maps/search/?api=1&query=47.2493436%2C-122.4459621&query_place_id=ChIJ-UP6RnNVkFQR6Ewb1lag-jk) | Held |
| US | washington-dc | Child/foster support | St Ann's Center for Children, Youth & Families | 4901 Eastern Ave NE, Hyattsville, MD 20782, Estados Unidos | 38.9510925, -76.9820491 | [Place](https://www.google.com/maps/search/?api=1&query=38.9510925%2C-76.9820491&query_place_id=ChIJh2-n-7nHt4kRd7p-QBlAf8s) | Ready |
| US | washington-dc | Blood donation | American Red Cross Blood Donation Center | 1730 E St NW, Washington, DC 20006, Estados Unidos | 38.8951314, -77.0407497 | [Place](https://www.google.com/maps/search/?api=1&query=38.8951314%2C-77.0407497&query_place_id=ChIJte8il6S3t4kRPtXXH_GkRAc) | Ready |
| US | washington-dc | Older-adult care | Stoddard Baptist Nursing Home | 1818 Newton St NW, Washington, DC 20010, Estados Unidos | 38.9339015, -77.0424454 | [Place](https://www.google.com/maps/search/?api=1&query=38.9339015%2C-77.0424454&query_place_id=ChIJv5LWtSXIt4kRbilHv1qskG8) | Ready |
| US | washington-dc | Food assistance | Miriam's Kitchen | 2401 Virginia Ave NW, Washington, DC 20037, Estados Unidos | 38.8985152, -77.0517192 | [Place](https://www.google.com/maps/search/?api=1&query=38.8985152%2C-77.0517192&query_place_id=ChIJYe6BbbK3t4kRyuWW7wnE7d4) | Ready |
| US | baltimore | Child/foster support | The Children's Home | 205 Bloomsbury Ave, Catonsville, MD 21228, Estados Unidos | 39.2666123, -76.7271601 | [Place](https://www.google.com/maps/search/?api=1&query=39.2666123%2C-76.7271601&query_place_id=ChIJVa5HzC4cyIkRiZmrNLh8B2A) | Ready |
| US | baltimore | Blood donation | American Red Cross Blood Donation Center | 4700 Mt Hope Dr, Baltimore, MD 21215, Estados Unidos | 39.3474663, -76.7069152 | [Place](https://www.google.com/maps/search/?api=1&query=39.3474663%2C-76.7069152&query_place_id=ChIJF5Dc3UgayIkRKg26gM8Fmro) | Ready |
| US | baltimore | Older-adult care | St Elizabeth Rehabilitation and Nursing Center | 3320 Benson Ave, Baltimore, MD 21227, Estados Unidos | 39.2700592, -76.6715595 | [Place](https://www.google.com/maps/search/?api=1&query=39.2700592%2C-76.6715595&query_place_id=ChIJaR525uscyIkRcWl7tgIVafI) | Ready |
| US | baltimore | Food assistance | Paul's Place, Inc | 1118 Ward St, Baltimore, MD 21230, Estados Unidos | 39.2820421, -76.6328611 | [Place](https://www.google.com/maps/search/?api=1&query=39.2820421%2C-76.6328611&query_place_id=ChIJCVdNrFEDyIkRtPjHlS-fU2c) | Ready |
| US | los-angeles | Child/foster support | Optimist Youth Homes & Family Services | 6957 N Figueroa St, Los Angeles, CA 90041, Estados Unidos | 34.1284232, -118.1882895 | [Place](https://www.google.com/maps/search/?api=1&query=34.1284232%2C-118.1882895&query_place_id=ChIJJ5Yo-_PDwoARG9NZ04uExSI) | Ready |
| US | los-angeles | Blood donation | Children's Hospital Los Angeles: Blood Donor Center | 4650 Sunset Blvd, Los Angeles, CA 90027, Estados Unidos | 34.0974752, -118.2906156 | [Place](https://www.google.com/maps/search/?api=1&query=34.0974752%2C-118.2906156&query_place_id=ChIJGaRjMK3AwoARP58oo0-MmIc) | Ready |
| US | los-angeles | Older-adult care | Sakura Gardens of Los Angeles | 325 S Boyle Ave, Los Angeles, CA 90033, Estados Unidos | 34.0444936, -118.2199732 | [Place](https://www.google.com/maps/search/?api=1&query=34.0444936%2C-118.2199732&query_place_id=ChIJreKQZRDGwoARkkEIV46xx8I) | Ready |
| US | los-angeles | Food assistance | Hospitality Kitchen | 821 E 6th St, Los Angeles, CA 90021, Estados Unidos | 34.040543, -118.2422664 | [Place](https://www.google.com/maps/search/?api=1&query=34.040543%2C-118.2422664&query_place_id=ChIJ0QyWizDGwoAROnUE7_c4GQo) | Ready |
| US | anaheim | Child/foster support | The Eli Home | 1175 N East St, Anaheim, CA 92805, Estados Unidos | 33.8506765, -117.9072005 | [Place](https://www.google.com/maps/search/?api=1&query=33.8506765%2C-117.9072005&query_place_id=ChIJsb8_1BPW3IARwhMZ1NcPJV0) | Ready |
| US | anaheim | Blood donation | American Red Cross Blood Donation Center | 1379 S Harbor Blvd, Fullerton, CA 92832, Estados Unidos | 33.85775, -117.9252304 | [Place](https://www.google.com/maps/search/?api=1&query=33.85775%2C-117.9252304&query_place_id=ChIJCxAaaWAq3YARFrEhK82oFF4) | Ready |
| US | anaheim | Older-adult care | Buena Vista Care Center | 1440 S Euclid St, Anaheim, CA 92802, Estados Unidos | 33.81161, -117.9405877 | [Place](https://www.google.com/maps/search/?api=1&query=33.81161%2C-117.9405877&query_place_id=ChIJDQrmoDMo3YARYOiHkpE6YAA) | Ready |
| US | anaheim | Food assistance | Salvation Army - Red Shield - Food Pantry-Soup Kitchen | 1515 W North St, Anaheim, CA 92801, Estados Unidos | 33.8414505, -117.9367297 | [Place](https://www.google.com/maps/search/?api=1&query=33.8414505%2C-117.9367297&query_place_id=ChIJAQBAVu8p3YARl1l3tDY175s) | Ready |
| ES | madrid | Child/foster support | Residencia Infantil "Vallehermoso" | C. de Fernández de los Ríos, 42, Chamberí, 28015 Madrid, España | 40.4354487, -3.7095712 | [Place](https://www.google.com/maps/search/?api=1&query=40.4354487%2C-3.7095712&query_place_id=ChIJqV_Dzl0oQg0RbGXjWMvwLRo) | Ready |
| ES | madrid | Blood donation | Centro de Transfusión de la Comunidad de Madrid | esquina con Avenida de las Comunidades, Av. de la Democracia, s/n, Vicálvaro, 28032 Madrid, España | 40.3964689, -3.6155502 | [Place](https://www.google.com/maps/search/?api=1&query=40.3964689%2C-3.6155502&query_place_id=ChIJrXr1pKclQg0Rde_jmWzTiwE) | Ready |
| ES | madrid | Older-adult care | Residencia de Mayores en Madrid Centro / Emera Juan Bravo | Calle de Juan Bravo, 38, Salamanca, 28006 Madrid, España | 40.4323907, -3.6784495 | [Place](https://www.google.com/maps/search/?api=1&query=40.4323907%2C-3.6784495&query_place_id=ChIJi7SXUwIpQg0RTGKaBD_xHr8) | Ready |
| ES | madrid | Food assistance | Comedor Social - San Juan Bautista - Orden de Malta España | C. de la Costa Verde, 15, Tetuán, 28029 Madrid, España | 40.4685657, -3.6957455 | [Place](https://www.google.com/maps/search/?api=1&query=40.4685657%2C-3.6957455&query_place_id=ChIJ03IBhg4pQg0R-7XOuPWYwPA) | Ready |
| ES | lugo | Food assistance | Cáritas Diocesana de Lugo | Rúa Luis Peña Novo, 6, 27001 Lugo, España | 43.011128, -7.5578648 | [Place](https://google.com/maps/place/C%C3%A1ritas+Diocesana+de+Lugo/@43.011128,-7.5578648,17z/data=!3m1!4b1!4m6!3m5!1s0xd31ce852b11a137:0x9ffa5cbd337623e5!8m2!3d43.011128!4d-7.5578648!16s%2Fg%2F1tff49st) | Ready |
| ES | lugo | Child/foster support | Hogar Madre Encarnación | Rúa Músico, Rúa Falla, 16, 27004 Lugo, España | 43.0081061, -7.5715826 | [Place](https://www.google.com/maps/search/?api=1&query=43.0081061%2C-7.5715826&query_place_id=ChIJbVipr33OMQ0Rej_rZFgrB1g) | Ready |
| ES | lugo | Older-adult care | Religiosas Hermanitas de los Ancianos Desamparados | Rúa San Roque, 54, 27002 Lugo, España | 43.008018, -7.5518231 | [Place](https://www.google.com/maps/search/?api=1&query=43.008018%2C-7.5518231&query_place_id=ChIJu7xtVpnOMQ0RgR87Dn8d83w) | Ready |
| IT | roma | Child/foster support | Associazione Piccoli Passi Onlus Casa D'Accoglienza Per Minori | Via Panerai Ruggero, 5, 00125 Roma RM, Italia | 41.7793911, 12.3848463 | [Place](https://www.google.com/maps/search/?api=1&query=41.7793911%2C12.3848463&query_place_id=ChIJnfx9oaTzJRMREmeURebgNXU) | Ready |
| IT | roma | Blood donation | Centro Donatori Sangue - Croce Rossa Italiana | Via Bernardino Ramazzini, 15, 00151 Roma RM, Italia | 41.8631498, 12.4509776 | [Place](https://www.google.com/maps/search/?api=1&query=41.8631498%2C12.4509776&query_place_id=ChIJR5qCTqWKJRMRayaKY0NHExY) | Ready |
| IT | roma | Older-adult care | Casa Di Riposo Villa Vittorio | Via Silvestro II, 15, 00167 Roma RM, Italia | 41.9057431, 12.423028 | [Place](https://www.google.com/maps/search/?api=1&query=41.9057431%2C12.423028&query_place_id=ChIJKRy1c89fLxMRu99lszyrqvE) | Ready |
| IT | roma | Food assistance | Mensa Serale | Via Marsala, 109, 00100 Roma RM, Italia | 41.898183, 12.5088413 | [Place](https://www.google.com/maps/search/?api=1&query=41.898183%2C12.5088413&query_place_id=ChIJmWPg8JhhLxMRLbz2ZFwZ_HA) | Held |
| IT | vaticano | Child/foster support | L'Accoglienza Onlus | Via delle Calasanziane, 12, 00167 Roma RM, Italia | 41.9182372, 12.4200404 | [Place](https://www.google.com/maps/search/?api=1&query=41.9182372%2C12.4200404&query_place_id=ChIJM26rRDReLxMRsfziO_rz7N4) | Ready |
| IT | vaticano | Blood donation | Centro Trasfusionale (SIMT) Santo Spirito | Lungotevere in Sassia, 1, 00186 Roma RM, Italia | 41.900521, 12.4625567 | [Place](https://www.google.com/maps/search/?api=1&query=41.900521%2C12.4625567&query_place_id=ChIJ0cErUMxhLxMR5pRrC-gF_ek) | Ready |
| IT | vaticano | Older-adult care | R.S.A. St. Joseph | Via Bernardino Telesio, 4, 00195 Roma RM, Italia | 41.9117689, 12.4525062 | [Place](https://www.google.com/maps/search/?api=1&query=41.9117689%2C12.4525062&query_place_id=ChIJix0b04lgLxMRCw9VcHwlWXQ) | Ready |
| GB | londres | Child/foster support | St Christopher's Fellowship | 1 Putney High St, London SW15 1SZ, Reino Unido | 51.4654659, -0.2132824 | [Place](https://www.google.com/maps/search/?api=1&query=51.4654659%2C-0.2132824&query_place_id=ChIJO7XKVQwPdkgRLIbvXmwulaU) | Ready |
| GB | londres | Blood donation | London West End Blood Donor Centre | 26 Margaret St, London W1W 8NB, Reino Unido | 51.5166811, -0.1416796 | [Place](https://www.google.com/maps/search/?api=1&query=51.5166811%2C-0.1416796&query_place_id=ChIJ1a6Cb9UadkgRMHXYEjh36WE) | Ready |
| GB | londres | Older-adult care | St Vincent's House Care Home - Care UK | 49 Queen Caroline St, London W6 9QH, Reino Unido | 51.4905947, -0.2252356 | [Place](https://www.google.com/maps/search/?api=1&query=51.4905947%2C-0.2252356&query_place_id=ChIJPa9KDrcPdkgR4SZ-J7Bo6L0) | Ready |
| GB | londres | Food assistance | Soup Kitchen London | 79A Tottenham Ct Rd, London W1T 4TD, Reino Unido | 51.5210763, -0.1353196 | [Place](https://www.google.com/maps/search/?api=1&query=51.5210763%2C-0.1353196&query_place_id=ChIJk5teTCkbdkgR4TM5baYpUsE) | Ready |
| MX | xalapa | Child/foster support | Casa del Niño Jalapeño | Fidencio Ocaña 46, Francisco Ferrer Guardia, 91050 Xalapa-Enríquez, Ver. | 19.5385827, -96.936849 | [Place](https://google.com/maps/place/Casa+del+Ni%C3%B1o+Jalape%C3%B1o/@19.5385827,-96.936849,17z/data=!3m1!4b1!4m6!3m5!1s0x85db2e0c1980ba0b:0x5e95f9294b18e558!8m2!3d19.5385827!4d-96.936849!16s%2Fg%2F1tgq6_rc) | Ready |
| MX | xalapa | Older-adult care | Casa Hogar del Adulto Mayor "Mariana Sayago" | Xalapeños Ilustres 90, Zona Centro, Centro, 91000 Xalapa-Enríquez, Ver. | 19.5291625, -96.9178907 | [Place](https://www.google.com/maps/search/?api=1&query=19.5291625%2C-96.9178907&query_place_id=ChIJHQKhrgEy24URiL1f8rOjQR4) | Ready |
| MX | xalapa | Blood donation | Valerio Laboratorios y Banco de Sangre | Av Miguel Alemán 103, Federal, 91170 Xalapa-Enríquez, Ver. | 19.5453414, -96.9171941 | [Place](https://google.com/maps/place/Valerio+Laboratorios+y+Banco+de+Sangre/@19.5453414,-96.9171941,17z/data=!3m1!4b1!4m6!3m5!1s0x85db31e3a19ef24f:0x4d01183e427e9111!8m2!3d19.5453414!4d-96.9171941!16s%2Fg%2F1v96m470) | Ready |
| MX | guanajuato | Older-adult care | Refugio Para Ancianos Cosme Olivares Torres | C. 5 de Mayo 32, Zona Centro, 36000 Guanajuato, Gto. | 21.0192899, -101.259025 | [Place](https://google.com/maps/place/Refugio+Para+Ancianos+Cosme+Olivares+Torres/@21.0192899,-101.259025,17z/data=!3m1!4b1!4m6!3m5!1s0x842b273bbd97643f:0x6e2afe5c7d7c80c5!8m2!3d21.0192899!4d-101.259025!16s%2Fg%2F1w6_p449) | Held |
| MX | xalapa | Food assistance | UV - Comedor Comunitario | cerca de, Francisco Ferrer Guardia, 91026 Xalapa-Enríquez, Ver. | 19.5387925, -96.934322 | [Place](https://www.google.com/maps/search/?api=1&query=19.5387925%2C-96.934322&query_place_id=ChIJXZmHwfYv24URkycB09fnoBU) | Held |
| MX | guanajuato | Child/foster support | CIANNA | s/n, Cam. al Carcamo, Yerbabuena, Gto. | 20.9514749, -101.2752691 | [Place](https://google.com/maps/place/CIANNA/@20.9514749,-101.2752691,17z/data=!3m1!4b1!4m6!3m5!1s0x842b77e09fde86b3:0x72d3c28e0764b01e!8m2!3d20.9514749!4d-101.2752691!16s%2Fg%2F11gk8zmwxj) | Held |
| MX | manzanillo | Older-adult care | Asilo de Ancianos de Manzanillo, I.A.P. | Alfonso Reyes 519, Nuevo Salahua, 28869 Manzanillo, Col. | 19.1139915, -104.3268005 | [Place](https://google.com/maps/place/Asilo+de+Ancianos+de+Manzanillo,+I.A.P./@19.1139915,-104.3268005,17z/data=!3m1!4b1!4m6!3m5!1s0x8424d66407fa7bf3:0xf9fc075ec26c436f!8m2!3d19.1139915!4d-104.3268005!16s%2Fg%2F1pxq33fjx) | Ready |
| MX | guanajuato | Blood donation | Hospital General de Guanajuato | Carretera Guanajuato - Silao Kilómetro 6.5, Los Alcaldes, 36250 Guanajuato, Gto. | 20.9807693, -101.2891448 | [Place](https://www.google.com/maps/search/?api=1&query=20.9807693%2C-101.2891448&query_place_id=ChIJQ_9tHo12K4QRNN7sVf5QipQ) | Ready |
| ES | lugo | Blood donation | [Hospital Público] Hospital Universitario Lucus Augusti ~ HULA (Lugo) | Rúa Dr. Ulises Romero, 1, 27003 Lugo, España | 43.0202244, -7.535217 | [Place](https://google.com/maps/place/%5BHospital+P%C3%BAblico%5D+Hospital+Universitario+Lucus+Augusti+~+HULA+(Lugo)/@43.0202244,-7.535217,17z/data=!3m1!4b1!4m6!3m5!1s0xd31ceb71b599fcf:0x363d1304bf9c0677!8m2!3d43.0202244!4d-7.535217!16s%2Fg%2F11clsyyqkv) | Ready |
| MX | bahia-de-banderas | Food assistance | Refuge of Hope Bucerias | Miguel Hidalgo, Colonia Francisco Javier Ovando, 63732 Bucerías, Nay. | 20.7630077, -105.3363924 | [Place](https://www.google.com/maps/search/?api=1&query=20.7630077%2C-105.3363924&query_place_id=ChIJ-T6RZtNBIYQRdoE9o15r4PQ) | Ready |
| IT | vaticano | Food assistance | Il dono di Maria (Missionaries of Charity) | Piazza del Sant'uffizio, 9a, 00165 Roma RM, Italia | 41.9004903, 12.4562796 | [Place](https://www.google.com/maps/search/?api=1&query=41.9004903%2C12.4562796&query_place_id=ChIJI6ahNwBhLxMRWN-jDGMDRVg) | Ready |
| US | tacoma | Food assistance | The Tacoma Rescue Mission | 425 S Tacoma Way, Tacoma, WA 98402, Estados Unidos | 47.2357864, -122.4389801 | [Place](https://google.com/maps/place/The+Tacoma+Rescue+Mission/@47.2357864,-122.4389801,17z/data=!3m1!4b1!4m6!3m5!1s0x5490556360dc5887:0xac0929b70226e61b!8m2!3d47.2357864!4d-122.4389801!16s%2Fg%2F1tf86sq2) | Ready |
| US | dallas | Food assistance | The Bridge Homeless Recovery Center | 1818 Corsicana St, Dallas, TX 75201, Estados Unidos | 32.7755726, -96.7913313 | [Place](https://google.com/maps/place/The+Bridge+Homeless+Recovery+Center/@32.7755726,-96.7913313,17z/data=!3m1!4b1!4m6!3m5!1s0x864e98e20fb80721:0x2f14c75645a405ce!8m2!3d32.7755726!4d-96.7913313!16s%2Fg%2F11b88ldx7g) | Ready |

## Supplementary source findings

### Hospital Zambrano Hellion TecSalud

- Institutional source establishes a blood bank at Hospital Zambrano Hellion. The captured pin is the hospital, not a separately surveyed donor entrance. [Source](https://conecta.tec.mx/es/noticias/monterrey/salud/premian-excelencia-de-banco-de-sangre-del-hospital-zambrano-hellion).

### Centro Estatal de la Transfusión Sanguínea

- Official 2025 document supplies the full CETS street address and postal code; conflicts with Maps formatting. [Source](https://licitaciones.puebla.gob.mx/images/BASES2025/ABRIL/GEP-SPFA-LPN-078-047-2025.pdf).

### Asociacion Asistencial Doña Nico A. C.

- 2024 report locates the kitchen on Jose Hernandez Sierra, conflicting with Maps. [Source](https://www.meganoticias.mx/veracruz/noticia/albergue-dona-nico-solicita-apoyo-para-ampliar-comedor-comunitario/526301).

### Hospital General Manzanillo

- Official directory identifies Hospital General Manzanillo as a blood collection point at Elias Zamora Verduzco, Nuevo Salahua, 28869. [Source](https://saludcolima.gob.mx/adquisiciones/licitaciones/2024/008-24/008-24-BASES.pdf).

### Hospital Joya Riviera Nayarit

- Hospital document lists an affiliated/concessioned blood-bank service. Confirm donor arrangements with the hospital. [Source](https://grupomedicojoya.com/wp-content/uploads/HJ-CURRICULUM-RIVIERA.pdf).

### The Stewpot

- 1610 S Malcolm X is the main service/pantry site. Prepared meals are served at The Bridge, 1818 Corsicana; keep the sites separate. [Source](https://www.thestewpot.org/i-need-help).
- Institution confirms the phased move from Young Street and food-pantry service at 1610 S Malcolm X. [Source](https://www.thestewpot.org/stewpot-services-updates).

### Rescue Mission of EL Paso

- Organization confirms meals for shelter residents at 221 N Lee. This is not a guarantee of unrestricted walk-in meal access. [Source](https://rmelp.org/).

### Treehouse

- Public office/store supporting youth in foster care at 2100 24th Avenue S. It is not a residential children's home; office and store access differ. [Source](https://www.treehouseforkids.org/contact-us/).

### Olive Crest

- Organization describes its Tacoma foster-care support program. Maps supplies the public office location; residential admission is not established. [Source](https://www.olivecrest.org/careers/).

### Nativity House

- 2025 report describes closure of the public day shelter and meal program. [Source](https://www.thenewstribune.com/news/local/article311722698.html).
- Later provider-posted volunteer listing concerns meals for shelter residents, not proof of restored public walk-in meals. [Source](https://www.idealist.org/en/volunteer-opportunity/cbd0e842ec974cd397f25dc9a7209cfc-join-our-kitchen-crew-catholic-community-volunteer-services-of-sw-washington-tacoma).

### St Ann's Center for Children, Youth & Families

- Housing and support for pregnant/parenting women and their children, with intake; public address is Hyattsville, Maryland, near Washington DC. [Source](https://www.stanns.org/).

### Paul's Place, Inc

- Maryland service directory identifies the soup kitchen at 1118 Ward Street. [Source](https://search.marylandaccesspoint.211md.org/search/50ff7781-ed0c-5e73-acca-6588f531e3f3).

### The Eli Home

- Organization publishes 1175 N East Street as its contact address. It is a public contact office, not a disclosed confidential shelter location. [Source](https://www.elihome.org/about-us).

### Salvation Army - Red Shield - Food Pantry-Soup Kitchen

- Food-bank partner directory lists groceries/food distribution at 1515 W North Street. Prepared-meal service at this pin is unconfirmed. [Source](https://feedoc.org/wp-content/uploads/2026/06/SHFB-Partner-List-06-18-26.pdf).

### Cáritas Diocesana de Lugo

- Diocese confirms the Caritas office and San Froilan meal service share the building; meal access is on the ground floor. [Source](https://www.diocesisdelugo.org/inauguracion-sede-caritas-lugo/).
- Current resource guide gives Rua Armanya for the meal entrance and Luis Pena Novo 6 for Caritas; contact first for entrance instructions. [Source](https://www.caritaslugo.es/wp-content/uploads/2026/04/Diptico-Sin-Hogar.pdf).

### Hogar Madre Encarnación

- Operator confirms the children's service at Musico Falla 16 and describes a public-service agreement; the page does not establish its current funding mix. [Source](https://www.rmaria.org/hogares.html).

### Mensa Serale

- Current operator page gives Via Marsala 113 for the evening meal service; Maps lists 109. [Source](https://www.caritasroma.it/cosa-facciamo/mense/).

### L'Accoglienza Onlus

- Operator publishes its contact and community site at Via delle Calasanziane 12 in Rome. No private child or household locations are inferred. [Source](https://www.coopaccoglienza.it/).

### St Christopher's Fellowship

- Registered public contact office at 1 Putney High Street for fostering and placement enquiries; not a residential home. [Source](https://www.stchris.org.uk/get-in-touch/).

### Refugio Para Ancianos Cosme Olivares Torres

- Congressional record describes the planned permanent closure on 31 October 2023, consistent with Maps closure status. [Source](https://congresogto.gob.mx/expedientes_legislativos_digitales/iniciativas/6115).

### UV - Comedor Comunitario

- UV gives Adalberto Lara Hernandez 28 for a university meal program. This is not sufficient to assign that address to the captured unnamed pin. [Source](https://www.uv.mx/voluntariado/ejestematicos/comedor-rincon-de-los-universitarios/).

### CIANNA

- Official CAS directory gives Lucas Alaman 191, Puentecillas; this differs from the closed Maps listing. [Source](https://pepnna.guanajuato.gob.mx/cas-guanajuato/).

### Hospital General de Guanajuato

- Blood-collection campaign organizer lists the hospital at Guanajuato-Silao km 6.5. This establishes a collection location, not permanent donation hours. [Source](https://gciamt.org/luds-mexico-2025/).

### [Hospital Público] Hospital Universitario Lucus Augusti ~ HULA (Lugo)

- Official health service places the donation room on the fourth floor of HULA. The captured pin is the hospital. [Source](https://lugomarinamonforte.sergas.es/Paxinas/web.aspx?idContido=2&idLista=6&idTax=6200&migtab=2&tipo=paxtxt).
- ADOS lists the permanent donation point and donor contact 982221064. [Source](https://ados.sergas.gal/cartafol/ados-locais?pdf=1).

### Refuge of Hope Bucerias

- Organization confirms food and nutrition support focused on vulnerable children and women in Bucerias; access and meal schedule must be confirmed. [Source](https://refugeofhopebucerias.com/).

### Il dono di Maria (Missionaries of Charity)

- Vatican publication lists Dono di Maria at Sant Uffizio 9/a and its contact. Maps labels the street address Roma, Italia; territorial status is not inferred. [Source](https://media.osservatoreromano.va/media/osservatoreromano/pdf/ods/2025/02/ODS_2025_028_0202.pdf).

### The Tacoma Rescue Mission

- Operator confirms public meals at its downtown campus, 425 S Tacoma Way. [Source](https://www.trm.org/new-emergency-services/).

### The Bridge Homeless Recovery Center

- Stewpot identifies The Bridge, 1818 Corsicana, as its prepared-meal location; separate from the Malcolm X pantry. [Source](https://www.thestewpot.org/i-need-help).

