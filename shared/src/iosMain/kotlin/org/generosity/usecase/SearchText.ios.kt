package org.generosity.usecase

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDiacriticInsensitiveSearch
import platform.Foundation.NSLocale
import platform.Foundation.NSString
import platform.Foundation.stringByFoldingWithOptions

@OptIn(ExperimentalForeignApi::class)
@Suppress("CAST_NEVER_SUCCEEDS")
internal actual fun String.searchKey(): String =
    (trim() as NSString).stringByFoldingWithOptions(
        NSDiacriticInsensitiveSearch, NSLocale(localeIdentifier = "en_US_POSIX")
    ).lowercase()
