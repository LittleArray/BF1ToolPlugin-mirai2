package top.ffshaozi.data

/**
 * @Description
 * @Author littleArray
 * @Date 2023/8/24
 */
data class StatsJson(
    var isSuccessful: Boolean = false,
    var userId: Long = 0,
    var avatar: String? = null,
    var userName: String? = null,
    var id: Long = 0,
    var rank: Int = 0,
    var rankImg: String? = null,
    var rankName: String? = null,
    var skill: Double = 0.0,
    var scorePerMinute: Double = 0.0,
    var killsPerMinute: Double = 0.0,
    var winPercent: String? = null,
    var bestClass: String? = null,
    var accuracy: String? = null,
    var headshots: String? = null,
    var timePlayed: String? = null,
    var secondsPlayed: Long = 0,
    var killDeath: Double = 0.0,
    var infantryKillDeath: Double = 0.0,
    var infantryKillsPerMinute: Double = 0.0,
    var kills: Int = 0,
    var deaths: Int = 0,
    var wins: Int = 0,
    var loses: Int = 0,
    var longestHeadShot: Double = 0.0,
    var revives: Int = 0,
    var dogtagsTaken: Int = 0,
    var highestKillStreak: Int = 0,
    var roundsPlayed: Int = 0,
    var awardScore: Long = 0,
    var bonusScore: Long = 0,
    var squadScore: Long = 0,
    var currentRankProgress: Long = 0,
    var totalRankProgress: Long = 0,
    var avengerKills: Int = 0,
    var saviorKills: Int = 0,
    var headShots: Int = 0,
    var heals: Int = 0,
    var repairs: Int = 0,
    var killAssists: Int = 0,
)
