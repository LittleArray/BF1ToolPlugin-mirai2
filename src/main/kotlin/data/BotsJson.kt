package data


data class BotsJson(
    val code: Int,
    val data: Data,
    val message: String
){

    data class Data(
        val abnormalCount: Int,
        val bots: List<Bot>,
        val notStartedCount: Int,
        val offlineCount: Int,
        val serverWithBots: List<ServerWithBot>,
        val startingCount: Int,
        val totalCount: Int,
        val usableCount: Int,
        val usedCount: Int
    )
    data class Bot(
        val botNo: String,
        val clientNo: String,
        val gameId: String,
        val onlineState: Int,
        val path: String,
        val state: String,
        val time: Long,
        val user: String
    )
    data class ServerWithBot(
        val botList: List<Bot>,
        val platoonInfo: Any,
        val rspInfo: Any,
        val serverInfo: ServerInfo
    )
    data class ServerInfo(
        val country: String,
        val description: String,
        val gameId: String,
        val guid: String,
        val mapImageUrl: String,
        val mapMode: String,
        val mapModePretty: String,
        val mapName: String,
        val mapNamePretty: String,
        val name: String,
        val region: String,
        val rotation: Any,
        val serverBookmarkCount: String,
        val serverType: String,
        val slots: Slots
    )
    data class Slots(
        val queue: Queue,
        val soldier: Soldier,
        val spectator: Spectator
    )
    data class Queue(
        val current: Int,
        val max: Int
    )
    data class Soldier(
        val current: Int,
        val max: Int
    )
    data class Spectator(
        val current: Int,
        val max: Int
    )
}

