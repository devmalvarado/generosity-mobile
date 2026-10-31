package org.generosity.usecase

// Fold only search keys; never alter displayed names, addresses or user-entered text.
internal expect fun String.searchKey(): String
