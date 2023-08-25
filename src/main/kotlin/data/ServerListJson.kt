package top.ffshaozi.data

/**
 * @Description
 * @Author littleArray
 * @Date 2023/8/24
 */

data class ServerListJson(
    val isSuccessful:Boolean?=false,
    val loading: List<Player>?=null,
    val que: List<Player>?=null,
    val serverinfo: sin?=null,
    val teams: List<Team>?=null,
    val update_timestamp: Int?=null
)
data class sin(
    val country: String,
    val description: String,
    val level: String,
    val maps: List<String>,
    val mode: String,
    val name: String,
    val owner: String,
    val region: String,
    val servertype: String,
    val settings: List<Any>
)
data class Team(
    val faction: String,
    val image: String,
    val key: String,
    val name: String,
    val players: List<Player>,
    val teamid: String
)
data class Player(
    val join_time: Long,
    val latency: Int,
    val name: String,
    val platoon: String,
    val player_id: Long,
    val rank: Int,
    val slot: Int,
    val user_id: Long
)
