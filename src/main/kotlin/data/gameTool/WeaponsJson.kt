package top.ffshaozi.data.gameTool

/**
 * @Description
 * @Author littleArray
 * @Date 2023/8/24
 */
data class WeaponsJson(
    var isSuccessful: Boolean = false,
    var userId: Long = 0,
    var avatar: String? = null,
    var userName: String? = null,
    var id: Long = 0,
    var weapons: List<Weapons>? = null
)
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

