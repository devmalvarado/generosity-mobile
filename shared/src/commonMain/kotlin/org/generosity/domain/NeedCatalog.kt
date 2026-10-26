package org.generosity.domain

/**
 * Needs live apart from the center catalog so a future backend can update a
 * need without republishing the whole center, and so a contribution can
 * change the remaining quantity in one place.
 */
interface NeedCatalog {
    suspend fun needById(id: String): CenterNeed?

    suspend fun needsForCenter(centerId: String): List<CenterNeed>

    suspend fun save(need: CenterNeed)
}

class InMemoryNeedCatalog(
    initialNeeds: List<CenterNeed> = emptyList()
) : NeedCatalog, NeedsRepository {
    private val needs = initialNeeds.associateBy { it.id }.toMutableMap()

    override suspend fun needById(id: String): CenterNeed? = needs[id]

    override suspend fun needsForCenter(centerId: String): List<CenterNeed> =
        needs.values.filter { it.centerId == centerId }

    override suspend fun save(need: CenterNeed) {
        needs[need.id] = need
    }
}
