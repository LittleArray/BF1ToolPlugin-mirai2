package top.ffshaozi.data

/**
 * @Description
 * @Author littleArray
 * @Date 2023/8/24
 */
data class Vehicles(
    var vehicleName: String? = null,
    var type: String? = null,
    var image: String? = null,
    var kills: Int = 0,
    var killsPerMinute: Double = 0.0,
    var destroyed: Int = 0,
    var timeIn: Int = 0,
)
