package com.tunegocio.app.data.entities

import androidx.room.Embedded
import androidx.room.Relation

data class SaleWithDetails(
    @Embedded val sale: Sale,
    @Relation(
        parentColumn = "id",
        entityColumn = "saleId"
    )
    val details: List<SaleDetail>
)
