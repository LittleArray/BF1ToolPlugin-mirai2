package top.ffshaozi.data.ea

/**
 * @Description
 * @Author littleArray
 * @Date 2023/9/18
 */
data class Stats(
    val id: String,
    val jsonrpc: String,
    val result: Result
){
    data class Result(
        val accuracyRatio: Double,
        val avengerKills: Int,
        val awardScore: Double,
        val basicStats: BasicStats,
        val bonusScore: Double,
        val detailedStatType: String,
        val dogtagsTaken: Int,
        val favoriteClass: String,
        val flagsCaptured: Int,
        val flagsDefended: Int,
        val gameModeStats: List<GameModeStat>,
        val headShots: Int,
        val heals: Double,
        val highestKillStreak: Int,
        val kdr: Double,
        val killAssists: Double,
        val kitStats: List<KitStat>,
        val longestHeadShot: Double,
        val nemesisKillStreak: Double,
        val nemesisKills: Double,
        val repairs: Double,
        val revives: Double,
        val roundsPlayed: Int,
        val saviorKills: Int,
        val squadScore: Double,
        val suppressionAssist: Double,
        val vehicleStats: List<VehicleStat>
    )
    data class BasicStats(
        val completion: List<Any>,
        val deaths: Int,
        val equippedDogtags: Any,
        val freemiumRank: Any,
        val highlights: Any,
        val highlightsByType: Any,
        val kills: Int,
        val kpm: Double,
        val losses: Int,
        val rank: Any,
        val rankProgress: Any,
        val skill: Double,
        val soldierImageUrl: String,
        val spm: Double,
        val timePlayed: Int,
        val wins: Int
    )
    data class GameModeStat(
        val losses: Int,
        val name: String,
        val prettyName: String,
        val score: Double,
        val winLossRatio: Double,
        val wins: Int
    )
    data class KitStat(
        val kills: Double,
        val kitType: KitType,
        val name: String,
        val prettyName: String,
        val score: Double,
        val secondsAs: Double
    )
    data class VehicleStat(
        val killsAs: Double,
        val name: String,
        val prettyName: String,
        val timeSpent: Double,
        val vehiclesDestroyed: Double
    )
    class KitType
}
