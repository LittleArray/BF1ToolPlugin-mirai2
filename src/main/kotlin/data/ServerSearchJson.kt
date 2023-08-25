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
data class Teams(
    var teamOne: TeamOne? = null,
    var teamTwo: TeamTwo? = null,
)
data class TeamOne(
    var image: String? = null,
    var key: String? = null,
    var name: String? = null,
)
data class TeamTwo(
    var image: String? = null,
    var key: String? = null,
    var name: String? = null,
)