package top.ffshaozi.data

/**
 * @Description
 * @Author littleArray
 * @Date 2023/8/25
 */
data class FullServerInfoJson(
    val isSuccessful:Boolean?=false,
    val id: String?=null,
    val jsonrpc: String?=null,
    val result: Result?=null
)

data class Result(
    val platoonInfo: Any,
    val rspInfo: RspInfo,
    val serverInfo: ServerInfo
)

data class RspInfo(
    val adminList: List<Admin>,
    val bannedList: List<Banned>,
    val mapRotations: List<MapRotation>,
    val owner: Owner,
    val server: Server,
    val serverSettings: ServerSettings,
    val vipList: List<Vip>
)

data class ServerInfo(
    val country: String,
    val custom: Boolean,
    val description: String,
    val expansions: List<Expansion>,
    val experience: String,
    val fairfightEnabled: Boolean,
    val game: String,
    val gameId: String,
    val guid: String,
    val ip: String,
    val isFavorite: Boolean,
    val mapExpansion: MapExpansion,
    val mapImageUrl: String,
    val mapMode: String,
    val mapModePretty: String,
    val mapName: String,
    val mapNamePretty: String,
    val mapRotation: List<Any>,
    val mixId: Any,
    val name: String,
    val officialExperienceId: String,
    val operationIndex: Int,
    val passwordProtected: Boolean,
    val pingSiteAlias: String,
    val platform: String,
    val preset: String,
    val protocolVersion: String,
    val punkbusterEnabled: Boolean,
    val ranked: Boolean,
    val region: String,
    val rotation: List<Rotation>,
    val secret: String,
    val serverBookmarkCount: String,
    val serverMode: Any,
    val serverType: String,
    val settings: Settings,
    val slots: Slots,
    val tickRate: Int
)

data class Admin(
    val accountId: String,
    val avatar: String,
    val displayName: String,
    val nucleusId: String,
    val personaId: String,
    val platform: String,
    val platformId: String
)

data class Banned(
    val accountId: String,
    val avatar: String,
    val displayName: String,
    val nucleusId: String,
    val personaId: String,
    val platform: String,
    val platformId: String
)

data class MapRotation(
    val description: String,
    val mapRotationId: String,
    val maps: List<Map>,
    val mod: String,
    val name: String,
    val rotationType: String
)

data class Owner(
    val accountId: String,
    val avatar: String,
    val displayName: String,
    val nucleusId: String,
    val personaId: String,
    val platform: String,
    val platformId: String
)

data class Server(
    val bannerUrl: String,
    val createdDate: String,
    val expirationDate: String,
    val gameProtocolVersionString: String,
    val isFree: Boolean,
    val name: String,
    val ownerId: String,
    val persistedGameId: String,
    val pingSiteAlias: String,
    val serverId: String,
    val status: Status,
    val updatedBy: String,
    val updatedDate: String
)

data class ServerSettings(
    val bannerUrl: String,
    val customGameSettings: String,
    val description: String,
    val mapRotationId: String,
    val message: String,
    val name: String,
    val password: String
)

data class Vip(
    val accountId: String,
    val avatar: String,
    val displayName: String,
    val nucleusId: String,
    val personaId: String,
    val platform: String,
    val platformId: String
)

data class Map(
    val gameMode: String,
    val mapName: String
)

data class Status(
    val name: String,
    val originalName: String,
    val value: Int
)

data class Expansion(
    val license: String,
    val mask: Int,
    val name: String,
    val prettyName: String
)

data class MapExpansion(
    val license: String,
    val mask: Int,
    val name: String,
    val prettyName: String
)

data class Rotation(
    val mapImage: String,
    val mapPrettyName: String,
    val modePrettyName: String
)

data class Settings(
    val Kits: kotlin.collections.Map<String,String>,
    val Misc: kotlin.collections.Map<String,String>,
    val Scales: kotlin.collections.Map<String,String>,
    val Vehicles: kotlin.collections.Map<String,String>,
    val Weapons: kotlin.collections.Map<String,String>
)

data class Slots(
    val Queue: Queue,
    val Soldier: Soldier,
    val Spectator: Spectator
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