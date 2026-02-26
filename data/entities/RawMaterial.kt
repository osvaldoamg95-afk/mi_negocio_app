@Entity(tableName = "raw_materials")
data class RawMaterial(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)
