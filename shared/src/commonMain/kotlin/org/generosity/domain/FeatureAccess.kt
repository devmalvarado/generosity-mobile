package org.generosity.domain

/** Effective server decisions for the current account; never an authorization grant. */
data class FeatureAccess(
    val createContributions: Boolean = false,
    val publishNeeds: Boolean = false,
    val showNeedProgress: Boolean = false,
    val previewGroups: List<String> = emptyList()
)
