package com.dndcharacterhandler.domain.repository

import com.dndcharacterhandler.domain.model.InventoryCatalogItem

interface InventoryCatalogRepository {
    suspend fun getItems(): List<InventoryCatalogItem>
}
