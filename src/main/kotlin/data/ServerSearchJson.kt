package top.ffshaozi.data

/**
 * @Description
 * @Author littleArray
 * @Date 2023/8/24
 */
data class ServerSearchJson(
    var isSuccessful: Boolean = false,
    var servers: List<Servers>? = null
)
