# Mobile UI Checks

The initial UI checks focus on the mobile foundation contract:

- Anonymous users can open discovery without registration.
- Users can switch between English and Spanish.
- Category/search-filtered centers remain visible in map and list flows.
- Users can open a Kindness Center detail view.
- Current needs, urgent markers, and money donation boundaries are visible.
- Profile empty states are visible before contribution history exists.
- Notification settings expose nearby-center, urgent-need, and reminder controls.
- Visit-signal copy states that official verification happens through a future backend/backoffice workflow.

Android checks live in `androidApp/src/androidTest/java/org/generosity/app/GenerosityUiTest.kt`.

iOS checks live in `iosApp/GenerosityAppTests/GenerosityUiChecklistTests.swift`.

