package top.ffshaozi.data

/**
 * @Description
 * @Author littleArray
 * @Date 2023/8/24
 */
data class Weapons(
    var weaponName: String? = null,
    var type: String? = null,
    var image: String? = null,
    var timeEquipped: Int = 0,
    var kills: Int = 0,
    var killsPerMinute: Double = 0.0,
    var headshotKills: Int = 0,
    var headshots: String? = null,
    var shotsFired: Int = 0,
    var shotsHit: Int = 0,
    var accuracy: String? = null,
    var hitVKills: Float = 0f
)
