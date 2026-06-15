package com.dndcharacterhandler.domain.repository

import com.dndcharacterhandler.domain.model.SpellCatalogItem

interface SpellCatalogRepository {
    suspend fun getItems(): List<SpellCatalogItem>
}
