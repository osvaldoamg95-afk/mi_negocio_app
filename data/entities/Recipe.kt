@Entity(
    tableName = "recipes",
    primaryKeys = ["productId", "rawMaterialId"]
)
data class Recipe(
    val productId: Int,
    val rawMaterialId: Int,
    val quantityRequired: Double
)
