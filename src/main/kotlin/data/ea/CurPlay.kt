package top.ffshaozi.data.ea

import kotlin.collections.Map

/**
 * @Description
 * @Author littleArray
 * @Date 2023/9/27
 */
data class CurPlay(
    val id: String,
    val jsonrpc: String,
    val result: Map<Long,MapValue?>
){

    data class MapValue(
        val country: String,
        val custom: Boolean,
        val description: String,
        val expansions: List<Expansion>,
        val experience: String,
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
        val operationIndex: Any,
        val overallGameMode: Any,
        val ownerId: Any,
        val passwordProtected: Boolean,
        val pingSiteAlias: String,
        val platform: String,
        val playgroundId: Any,
        val preset: String,
        val protocolVersion: String,
        val ranked: Boolean,
        val region: String,
        val secret: String,
        val serverMode: Any,
        val serverType: String,
        val settings: Settings,
        val slots: Slots,
        val tickRate: Int
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
    class Settings
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
}
