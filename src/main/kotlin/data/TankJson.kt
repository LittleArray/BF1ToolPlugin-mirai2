package top.ffshaozi.data

/**
 * @Description
 * @Author littleArray
 * @Date 2023/8/24
 */
data class TankJson(
    var isSuccessful: Boolean = false,
    var userId: Long = 0,
    var avatar: String? = null,
    var userName: String? = null,
    var id: Long = 0,
    var vehicles: List<Vehicles>? = null,
)
data class Vehicles(
    var vehicleName: String? = null,
    var type: String? = null,
    var image: String? = null,
    var kills: Int = 0,
    var killsPerMinute: Double = 0.0,
    var destroyed: Int = 0,
    var timeIn: Int = 0,
)