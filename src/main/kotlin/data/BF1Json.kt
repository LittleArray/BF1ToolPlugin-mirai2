package top.ffshaozi.data

/**
 * @Description
 * @Author littleArray
 * @Date 2023/9/14
 */
data class BF1Json(
    var isSuccessful:Boolean = false,
    val regions: MutableMap<String,Regions>?=null
)

data class Regions(
    val amounts: Amounts,
    val mapPlayers: MutableMap<String,String>,
    val maps: MutableMap<String,String>,
    val modePlayers: MutableMap<String,String>,
    val modes: MutableMap<String,String>,
    val region: String,
    val regionName: String
)

data class Amounts(
    val communityQueueAmount: Int,
    val communityServerAmount: Int,
    val communitySoldierAmount: Int,
    val communitySpectatorAmount: Int,
    val diceQueueAmount: Int,
    val diceServerAmount: Int,
    val diceSoldierAmount: Int,
    val diceSpectatorAmount: Int,
    val queueAmount: Int,
    val serverAmount: Int,
    val soldierAmount: Int,
    val spectatorAmount: Int
)

