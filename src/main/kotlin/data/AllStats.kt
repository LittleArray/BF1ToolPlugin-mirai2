package top.ffshaozi.data

/**
 * @Description
 * @Author littleArray
 * @Date 2023/9/8
 */
data class AllStats(
    val isSuccessful:Boolean = false,
    val accuracy: String? = null,
    val activePlatoon: ActivePlatoon? = null,
    val avatar: String? = null,
    val avengerKills: Int? = null,
    val awardScore: Int? = null,
    val bestClass: String? = null,
    val bonusScore: Int? = null,
    val classes: List<Classe>? = null,
    val currentRankProgress: Int? = null,
    val deaths: Int? = null,
    val dogtagsTaken: Int? = null,
    val gamemodes: List<Gamemode>? = null,
    val headShots: Int? = null,
    val headshots: String? = null,
    val heals: Int? = null,
    val highestKillStreak: Int? = null,
    val id: Long? = null,
    val infantryKillDeath: Double? = null,
    val infantryKillsPerMinute: Double? = null,
    val killAssists: Int? = null,
    val killDeath: Double? = null,
    val kills: Int? = null,
    val killsPerMinute: Double? = null,
    val longestHeadShot: Double? = null,
    val loses: Int? = null,
    val platoons: List<Platoon>? = null,
    val progress: List<Progres>? = null,
    val rank: Int? = null,
    val rankImg: String? = null,
    val rankName: String? = null,
    val repairs: Int? = null,
    val revives: Int? = null,
    val roundsPlayed: Int? = null,
    val saviorKills: Int? = null,
    val scorePerMinute: Double? = null,
    val secondsPlayed: Int? = null,
    val sessions: List<Any>? = null,
    val skill: Double? = null,
    val squadScore: Int? = null,
    val timePlayed: String? = null,
    val totalRankProgress: Int? = null,
    val userId: Long? = null,
    val userName: String? = null,
    val vehicles: List<Vehicle>? = null,
    val weapons: List<Weapon>? = null,
    val winPercent: String? = null,
    val wins: Int? = null
) {

    data class ActivePlatoon(
        val description: String?=null,
        val emblem: String?=null,
        val id: String?=null,
        val name: String?=null,
        val tag: String?=null,
        val type: String?=null,
        val url: String?=null
    )

    data class Classe(
        val altImage: String,
        val className: String,
        val image: String,
        val kills: Int,
        val kpm: Double,
        val score: Int,
        val secondsPlayed: Int,
        val timePlayed: String
    )

    data class Gamemode(
        val gamemodeName: String,
        val losses: Int,
        val score: Int,
        val winPercent: String,
        val wins: Int
    )

    data class Platoon(
        val description: String,
        val emblem: String,
        val id: String,
        val name: String,
        val tag: String,
        val type: String,
        val url: String
    )

    data class Progres(
        val current: Int,
        val progressName: String,
        val total: Int
    )

    data class Vehicle(
        val destroyed: Int,
        val image: String,
        val kills: Int,
        val killsPerMinute: Double,
        val timeIn: Int,
        val type: String,
        val vehicleName: String
    )

    data class Weapon(
        val accuracy: String,
        val headshotKills: Int,
        val headshots: String,
        val hitVKills: Double,
        val image: String,
        val kills: Int,
        val killsPerMinute: Double,
        val shotsFired: Int,
        val shotsHit: Int,
        val timeEquipped: Int,
        var type: String,
        val weaponName: String
    )
}