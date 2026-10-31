package org.generosity.data

import org.generosity.domain.KindnessCenter

/**
 * The labeled demonstration catalog. [BundledKindnessCatalog] is the only copy;
 * this name remains for callers that already read the sample through the repository.
 */
object SampleImportedCatalog {
    fun centers(): List<KindnessCenter> = BundledKindnessCatalog.centers()
}
