package org.generosity.usecase

import java.text.Normalizer
import java.util.Locale

internal actual fun String.searchKey(): String =
    Normalizer.normalize(trim(), Normalizer.Form.NFD)
        .filterNot { Character.getType(it) == Character.NON_SPACING_MARK.toInt() }
        .lowercase(Locale.ROOT)
