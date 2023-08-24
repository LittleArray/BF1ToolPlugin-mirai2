package top.ffshaozi.data

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
