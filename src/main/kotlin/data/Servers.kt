package top.ffshaozi.data

/**
 * @Description
 * @Author littleArray
 * @Date 2023/8/24
 */
data class Servers(
    var prefix: String? = null,
    var description: String? = null,
    var playerAmount: Int = 0,
    var maxPlayers: Int = 0,
    var inSpectator: Int = 0,
    var inQue: Int = 0,
    var serverInfo: String? = null,
    var url: String? = null,
    var mode: String? = null,
    var currentMap: String? = null,
    var ownerId: String? = null,
    var country: String? = null,
    var region: String? = null,
    var platform: String? = null,
    var serverId: String? = null,
    var isCustom: Boolean = false,
    var smallMode: String? = null,
    var teams: Teams? = null,
    var official: Boolean = false,
    var gameId: String? = null,
)
