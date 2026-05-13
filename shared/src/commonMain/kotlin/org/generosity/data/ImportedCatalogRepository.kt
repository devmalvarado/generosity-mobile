package org.generosity.data

import org.generosity.domain.CenterNeed
import org.generosity.domain.KindnessCenter
import org.generosity.domain.KindnessCenterRepository
import org.generosity.domain.NeedsRepository

interface ImportedCatalogDataSource {
    fun loadCenters(): List<KindnessCenter>
}

class InMemoryImportedCatalogDataSource(
    private val centers: List<KindnessCenter>
) : ImportedCatalogDataSource {
    override fun loadCenters(): List<KindnessCenter> = centers
}

class ImportedCatalogRepository(
    private val dataSource: ImportedCatalogDataSource
) : KindnessCenterRepository, NeedsRepository {
    override suspend fun centers(): List<KindnessCenter> = try {
        dataSource.loadCenters()
    } catch (_: RuntimeException) {
        emptyList()
    }

    override suspend fun needsForCenter(centerId: String): List<CenterNeed> =
        centers().firstOrNull { it.id == centerId }?.needs.orEmpty()
}

