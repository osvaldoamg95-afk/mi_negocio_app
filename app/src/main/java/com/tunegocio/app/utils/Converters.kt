package com.tunegocio.app.data.utils

import androidx.room.TypeConverter
import com.tunegocio.app.data.entities.ProductType

class Converters {
    @TypeConverter
    fun fromProductType(value: ProductType): String = value.name

    @TypeConverter
    fun toProductType(value: String): ProductType = enumValueOf(value)
}
