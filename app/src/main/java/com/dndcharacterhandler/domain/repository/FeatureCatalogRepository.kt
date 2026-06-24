package com.dndcharacterhandler.domain.repository

import com.dndcharacterhandler.domain.model.FeatureCatalogItem

interface FeatureCatalogRepository {
    suspend fun getItems(): List<FeatureCatalogItem>
}
