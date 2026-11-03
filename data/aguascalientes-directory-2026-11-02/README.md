# Aguascalientes workbook reconciliation — 2026-11-02

Source: `Directorio Aguascallientes.xlsx`, supplied by the owner. The original workbook was read without modification. Its SHA-256 is `1d1f017d34d1dca1e07bef1a6eab6627f4a21600483de9343504f85c8228fc1e`.

## Outcome

| Disposition | Workbook rows |
| --- | ---: |
| New public centers with corroborated place pins | 13 |
| Existing centers enriched without replacing their IDs/pins | 4 |
| Existing centers matched; current values retained | 2 |
| Pending, excluded from the public catalog | 112 |
| Total reconciled | 131 |

The sheets contain 110 organization rows, 20 kitchen rows and one medical dispensary. Blank/number-only rows, the empty DB sheet and the Ideas sheet are not center records. A row is not necessarily a distinct organization: AACAFIQ appears twice, and Asilo San Nicolás may be a department of Ciudad de los Niños. Neither ambiguity creates a public duplicate.

The pending count is a research queue, **not a finding that 112 organizations do not exist**. Of those rows, 102 organization entries provide no street address, Maps link or coordinates. Not every remaining organization has received an exhaustive independent search. Pending entries need further research/contact before publication.

## Published-data boundary

- `catalog.json`: 13 additions and four updates, empty needs, original existing IDs, `expectedVersion` concurrency checks. No official-verification badges are assigned by this import.
- `decisions.json`: reviewed public values and reasons. Existing kitchen/center contact details are not replaced by unconfirmed workbook alternatives.
- `reconciliation.json`: all 131 source positions, names, status, public references and review notes; no raw personal contacts or call notes.
- `reconcile.py`: read-only workbook extraction and assertions. It generates a **Git-ignored, mode-0600** batch at `backend/storage/app/private/aguascalientes-directory-import.json` with source cells for the private review table.
- Original representative contacts, tax claims and call notes stay in `catalog_import_rows.source_data`. This table has no public or center-staff route. Public profiles accept only the explicit field allowlist.

Reproduction: run `python reconcile.py /path/to/original.xlsx /path/to/current-centers-snapshot.json` with read-only `openpyxl` installed. After an import, take a fresh snapshot and reconcile decisions before retrying: `expectedVersion` intentionally rejects replay against changed records.

## Location and funding decisions

- San Marcos reused Constitución's coordinates in the workbook. Insurgentes also had a misplaced point. Both additions use the observed Google Maps **place** coordinates (`!3d`/`!4d`), not the map viewport.
- AMANC: the official network lists Monroy 109; the consulted Maps listing uses Mahatma Gandhi 347-B and another telephone. Pending operational address confirmation.
- AACAFIQ: the consulted Maps result says permanently closed. This is not sufficient to conclude the legal association is dissolved; both workbook rows remain pending.
- Casa de Jesús: the official and Maps street numbers differ, as does the workbook point. Pending.
- Constitución: the physical community center is mapped, but meal service at that branch was not corroborated. Obraje, Palomino Dena and Solidaridad IV searches returned Jesús Terán; that pin was rejected for those branches.
- CRIT: official and Maps street/number/telephone agree; neighborhood/postal-code labels differ. The uncertain labels are omitted.
- Mujeres Ilustres: the place pin and neighborhood are corroborated; the workbook street number is omitted.
- Public kitchen funding/administration refers to the documented SEDESO program and 2024 state food procurement. Evidence retains the historical date/year; this is not an audit of the current budget or a promise of available meals.
- Dulce Refugio declares private-company donations. Ciudad de los Niños declares donations and a contribution for older-adult care, but its overall public/private mix is unknown. An A.C. legal form does not establish funding composition or tax-deductible donation status.
- Public directories and websites establish source declarations, not a Generosity verification, current vacancies, free admission or permission to distribute medication.

Primary evidence includes the [state kitchen program](https://informacion.aguascalientes.gob.mx/news/invitan-a-comedores-comunitarios;-funcionan-en-todo-el-estado), [2024 state procurement](https://eservicios2.aguascalientes.gob.mx/transparencia/SAE/DGAD/LP/DGAD-LEA-N003-2024-C.pdf), [Ciudad de los Niños](https://ciudaddelosninosags.com/), [Dulce Refugio](https://dulcerefugio.mx/) and [CRIT branch directory](https://teleton.org/voluntariado). Each decision includes its applicable public references and exact Maps place URL.

## Schema and compatibility

`centers.funding_origin` and `centers.administration_type` separately support `public`, `private`, `mixed`, `not_informed`; existing unknown values are not inferred. Reviewed legal/branch name, activities, hours, ways to contribute, social URLs, resource types and dated evidence use `centers.details.directoryProfile`. Unconfirmed fields remain absent.

`catalog_import_rows` preserves file hash, sheet, row, linked center, status, original cells, notes and sources. Import is transactional; repeated review rows upsert once, stale center versions roll back, and existing needs/reservations/publication states are preserved.

Directory endpoints expose the reviewed fields. Mobile sync retains its strict existing contract; new centers and existing descriptions continue to sync. **Displaying/editing the new funding/profile fields in native screens is still pending** in OpenSpec section 8.5.

## Validation and release

- Full backend suite before the real-data fixture: 89 tests, 5,316 assertions, passed on isolated `generosity_test`.
- The focused enrichment suite passed: 4 tests, 207 assertions. It additionally exercises the reviewed 17-center fixture, private-row isolation, stale versions, legacy imports, invalid evidence and location pin precision.
- Production migration/import and post-release checks: pending below until executed.

## Row ledger

| Sheet | Row | Name | Disposition |
| --- | ---: | --- | --- |
| Organizaciones | 2 | Orfanatorio Casimira Arteaga Hogar de la Niña, API Ags., A.C. | applied |
| Organizaciones | 3 | Hogar Ahaba. A.C. | pending |
| Organizaciones | 4 | Hogar del Niño María Auxiliadora, A.C. | pending |
| Organizaciones | 5 | Foria Centro de Desarrollo, I.B.P. | pending |
| Organizaciones | 6 | Ciudad de los Niños de Ags., A. C. | applied |
| Organizaciones | 7 | Centro de Promoción Femenina "Casa de Jesús", A.C. | pending |
| Organizaciones | 8 | Casa Hogar Dulce Refugio, A.C. | applied |
| Organizaciones | 9 | Casa Hogar Creación y Vida, A.C. | pending |
| Organizaciones | 10 | Casa del Niño Villa de la Asunción, A.C. | matched |
| Organizaciones | 11 | Asilo Rosa María, A.C. | pending |
| Organizaciones | 12 | Alianza para los Desamparados, A. C. | pending |
| Organizaciones | 13 | Casa de Descanso para Ancianos "Matilde Roubroy de Villanueva", I.B.P. | pending |
| Organizaciones | 14 | Asilo de la Sagrada Familia, A.C. | pending |
| Organizaciones | 15 | Patronato de Asistencia Social de Calvillo, A.C. | pending |
| Organizaciones | 16 | Estancia de Vida Nuestra Señora de Guadalupe, A.C. | applied |
| Organizaciones | 17 | Casa de Descanso San Nicolás Tolentino Fundación Nerón López | pending |
| Organizaciones | 18 | Casa de Descanso y Asilo para Ancianos San José, A.C. | pending |
| Organizaciones | 19 | Casa de Retiro de Ancianitas San Vicente, A.C, | pending |
| Organizaciones | 20 | Asilo Nuestra Señora de Guadalupe | pending |
| Organizaciones | 21 | Asilo de Ancianos de Pabellón de Arteaga, Ags., A.C. | pending |
| Organizaciones | 22 | Asilo de Ancianos José Guadalupe Alcalá. A.C. | pending |
| Organizaciones | 23 | Plenitud y Dignidad, A.C. | pending |
| Organizaciones | 24 | Casa Hogar de Aguascalientes Colibrí, A.C. | pending |
| Organizaciones | 25 | Casa para la promoción Integral del Adulto Mayor, A.C. | pending |
| Organizaciones | 26 | Ciegos Progresistas de Aguascalientes, A. C. | pending |
| Organizaciones | 27 | Consejo Estatal de Personas con Discapacidad en el Estado de Ags. | pending |
| Organizaciones | 28 | Comunidad Agazzi, A. C. | pending |
| Organizaciones | 29 | Diagnóstico y Asesoría en Problemas de Desarrollo Infantil, A. C. (DAPDI) | pending |
| Organizaciones | 30 | Unión de Invidentes de Ags., A. C. | pending |
| Organizaciones | 31 | Benefactores del Grupo Integración, A.C. | pending |
| Organizaciones | 32 | Asociación de Hemofilia de Ags., AC | pending |
| Organizaciones | 33 | Padres de Personas con Discapacidad Intelectual de Ags, A.C. | pending |
| Organizaciones | 34 | Centro de Educación Diferenciada Especializada para Sordos, AC | pending |
| Organizaciones | 35 | Asociación al Rescate por amigos de Esclerosis Múltiple de Ags., A.C. | pending |
| Organizaciones | 36 | Personas Productivas, A. C. | pending |
| Organizaciones | 37 | Comunidad Educativa Asunción, A. C. | pending |
| Organizaciones | 38 | Fray Escoba A. C | pending |
| Organizaciones | 39 | Intégrate al Deporte, A.C. | pending |
| Organizaciones | 40 | Niños por Siempre. A.C | pending |
| Organizaciones | 41 | De mi Mano a tu Mano, A.C. | pending |
| Organizaciones | 42 | Fundación Caballo Amigo, A.C. | pending |
| Organizaciones | 43 | Mujeres Turner y Discapacidad México. A.C. | pending |
| Organizaciones | 44 | FUNDOWN .A.C. | pending |
| Organizaciones | 45 | Paso a Paso con el, AC | pending |
| Organizaciones | 46 | Kiimakool, A.C. | pending |
| Organizaciones | 47 | Ángeles de Corazón Down | pending |
| Organizaciones | 48 | Autismo Aguascalientes A.C | pending |
| Organizaciones | 49 | Fundación Teletón Aquascalientes | applied |
| Organizaciones | 50 | Centro de Estudios Diferenciados | pending |
| Organizaciones | 51 | Ángeles Unidos contra la Esclerosis Multiple de Aguascalientes, A.C. | pending |
| Organizaciones | 52 | Alas de Colores | pending |
| Organizaciones | 53 | Confia en Mi. si puedo. A.C | pending |
| Organizaciones | 54 | Patronato Aguascalientes contra la Sordera. A C | pending |
| Organizaciones | 55 | Fundación Tres Legados Centro contra las Adicciones, A. C. | pending |
| Organizaciones | 56 | Centros de Integración Juvenil Aguascalientes, A. C | pending |
| Organizaciones | 57 | Casas y albergues de Rehabilitación Agua Viva, A.C. | pending |
| Organizaciones | 58 | Amigos Para Siempre para Alcohólicos y Drogadictos, I.A.P. | pending |
| Organizaciones | 59 | Los Arcos Enlace a la Vida, A.C. | pending |
| Organizaciones | 60 | Centro de Recuperación y Rehabilitación para enfermos de Alcoholismo y Drogadicción Zona Uno A.C. | pending |
| Organizaciones | 61 | Centro de Rehabilitación Siete de Junio, A.C. | pending |
| Organizaciones | 62 | CIDA Aguascalientes, A.C | pending |
| Organizaciones | 63 | Grupo Plenitud 24 Horas. A.C | pending |
| Organizaciones | 64 | Grupo Despertar de Ags., A.C. | pending |
| Organizaciones | 65 | FARERSO. A.C | pending |
| Organizaciones | 66 | Mi Único Camino, A.C. | pending |
| Organizaciones | 67 | Comunidad Terapéutica Especializada en Adicciones Sana tu ser, Cuerpo, Espiritu y Mente, A.C. | pending |
| Organizaciones | 68 | Prisionero Liberado, A.C | pending |
| Organizaciones | 69 | Mesón del Buen Samaritano, A.C | pending |
| Organizaciones | 70 | Centro de Recuperación Filadelfia, A.C. | pending |
| Organizaciones | 71 | Instituto de Servicios Integrales para el Desarrollo Humano y las Conductas Adictivas, A.C. | pending |
| Organizaciones | 72 | CETTAP Ags., A.C. | pending |
| Organizaciones | 73 | Sanación del Alma, A.C. | pending |
| Organizaciones | 74 | Centro de Rehabilitación Vida de Hechos, A.C. | pending |
| Organizaciones | 75 | Asociación Mexicana de Ayuda a Niños con Cáncer de Ags., A. C | pending |
| Organizaciones | 76 | Grupo de Enfermos con Problemas Renales, A. C. | pending |
| Organizaciones | 77 | Juntos en el Camino de la Esperanza. A.C | pending |
| Organizaciones | 78 | Grupo de Auto Ayuda de Epilepsia, A.C. | pending |
| Organizaciones | 79 | Patronato Banco de Ojos y Tejidos de Aguascalientes, A. C | pending |
| Organizaciones | 80 | Asociación de Enfermeras Prácticas Gratuitas a Domicilio de Aguascalientes, A.C. | pending |
| Organizaciones | 81 | Dulce Esperanza, A.C. | pending |
| Organizaciones | 82 | Cambiando una Vida, A.C | pending |
| Organizaciones | 83 | Centro de Diagnóstico Oportuno del Cáncer, A.C | pending |
| Organizaciones | 84 | Asociación de Familiares y Amigos de Pacientes Esquizofrénicos, AFAPE de Aguascalientes, A.C. | pending |
| Organizaciones | 85 | Fundación de Familiares de Alzheimer de Aguascalientes, A.C | pending |
| Organizaciones | 86 | Salva a Una Mujer, A.C. | pending |
| Organizaciones | 87 | Fundación Intervive,A.C. | pending |
| Organizaciones | 88 | Asociación Mexicana de Fibrosis Quística de Aguascalientes, A.C | pending |
| Organizaciones | 89 | Aacafiq, A.C | pending |
| Organizaciones | 90 | Chikil. A.C | pending |
| Organizaciones | 91 | CANICA Aguascalientes Centro de Apoyo a Niños con Cáncer. I.B.P. | pending |
| Organizaciones | 92 | Erradicando la Discriminación a Personas con Enfermedades Crónicas, A.C. | pending |
| Organizaciones | 93 | Alcanza Salud. Valores I.A.P. | pending |
| Organizaciones | 94 | Eli Lozano Yo Lucho contra el Cáncer, A.C | pending |
| Organizaciones | 95 | Patronato el Centro de Prevención de Quemaduras, A.C. | pending |
| Organizaciones | 96 | Cruz Roja Delegación Aguascalientes.I.A.P | pending |
| Organizaciones | 97 | Cáritas de Aguascalientes, A. C | pending |
| Organizaciones | 98 | Centro Social REFFO, A. C. | pending |
| Organizaciones | 99 | Librada Orozco. A. C. | pending |
| Organizaciones | 100 | La Rosaleda de Aguascalientes, A. C. | pending |
| Organizaciones | 101 | Casa de Asistencia Infantil Nuestra Señora de la Asunción. A. C | pending |
| Organizaciones | 102 | VIFAC Aguascalientes, A. C | pending |
| Organizaciones | 103 | Alianza Cultural. A. C. | pending |
| Organizaciones | 104 | Formación y Cultura de Ags., A. C. | pending |
| Organizaciones | 105 | Bodega de Alimentos de Cáritas de Aguascalientes, A. C. | pending |
| Organizaciones | 106 | Asociación Nacional Pro Superación Personal, A. C. | pending |
| Organizaciones | 107 | Voluntarias Vicentinas de Ags., A. C. | pending |
| Organizaciones | 108 | Club Rotario San Marcos. A. C | pending |
| Organizaciones | 109 | Mujer Contemporánea, A.C. | pending |
| Organizaciones | 110 | Club Rotario Bosques, A. C | pending |
| Organizaciones | 111 | Protegidos de Cebras, A. C | pending |
| Comedores | 2 | Comedor Comunitario El Abuelo | applied |
| Comedores | 3 | Comedor Comunitario San Martin de Tours | matched |
| Comedores | 4 | Aacafiq A.C. Albergue y Comedor | pending |
| Comedores | 8 | Buen Provecho — Casa del Bien Común Constitución | pending |
| Comedores | 9 | Buen Provecho — OLIVARES SANTANA | pending |
| Comedores | 10 | Buen Provecho — POCITOS | applied |
| Comedores | 11 | Buen Provecho — Casa del Bien Común Insurgentes | applied |
| Comedores | 12 | Buen Provecho — Casa del Bien Común San Marcos | applied |
| Comedores | 13 | Buen Provecho — MIRAVALLE | applied |
| Comedores | 14 | Buen Provecho — PIRULES | pending |
| Comedores | 15 | Buen Provecho — LA SALUD | applied |
| Comedores | 16 | Buen Provecho — PALOMINO DENA | pending |
| Comedores | 17 | Buen Provecho — LA ESTRELLA | pending |
| Comedores | 18 | Buen Provecho — NAZARIO ORTIZ GARZA | applied |
| Comedores | 19 | Buen Provecho — JESUS TERAN | applied |
| Comedores | 20 | Buen Provecho — GOMEZ PORTUGAL | applied |
| Comedores | 21 | Buen Provecho — SOLIDARIDAD IV | pending |
| Comedores | 22 | Buen Provecho — GUADALUPE PERALTA | applied |
| Comedores | 23 | Buen Provecho — OBRAJE | pending |
| Comedores | 24 | Buen Provecho — MUJERES ILUSTRES | applied |
| Dispensario Médico | 2 | Casa de San Vicente de Paúl, dispensario médico | applied |
