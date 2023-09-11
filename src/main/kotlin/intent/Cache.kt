package top.ffshaozi.intent

import com.google.gson.Gson
import data.BotsJson
import top.ffshaozi.BF1ToolPlugin
import top.ffshaozi.config.Setting
import top.ffshaozi.utils.BF1Api
import java.util.Date

/*** 全局数据的缓存
 * @Description
 * @Author littleArray
 * @Date 2023/9/4
 */
object Cache {
    //vip管理服务线程池子 群号
    var VipCThreadPool: HashMap<Long, Thread> = hashMapOf()

    //玩家列表管理服务线程池子 群号
    var ListThreadPool: HashMap<Long, Thread> = hashMapOf()

    //查询玩家EAC线程池
    var EACThreadPool: HashMap<Long, Thread> = hashMapOf()

    //服务器玩家数量检查线程池
    var PlayerThreadPool: HashMap<Long, Thread> = hashMapOf()

    //玩家生涯数据检测线程池
    var PlayerDataThreadPool: HashMap<Long, Thread> = hashMapOf()

    //玩家最近数据检测线程池
    var RPDataThreadPool: HashMap<Long, Thread> = hashMapOf()

    //服务器管理线程池
    var ServerManageThreadPool: HashMap<Long, Boolean> = hashMapOf()

    //临时服务器玩家数据
    var PlayerListInfo: HashMap<String, HashMap<String, ServerInfo.Player>> = hashMapOf()

    //临时服务器数据
    var ServerInfoList: HashMap<String, ServerInfo> = hashMapOf()

    //被踢出玩家列表
    var KickPlayers: MutableSet<String> = mutableSetOf()

    //生涯缓存
    var cacheLife: MutableSet<String> = mutableSetOf()

    //机器人所有群
    var BotGroups: String = ""

    var mapCache: HashMap<String, String> = hashMapOf(
        "MP_Desert" to "西奈沙漠",
        "MP_FaoFortress" to "法欧堡",
        "MP_Suez" to "苏伊士",
        "MP_Argonne" to "阿尔贡森林",
        "MP_ItalianCoast" to "意大利海岸",
        "MP_Amiens" to "亚眠",
        "MP_MountainFort" to "拉粑粑山",
        "MP_Graveyard" to "决裂",
        "MP_Forest" to "阿尔贡森林",
        "MP_Verdun" to "老毕登高地",
        "MP_Underworld" to "垃圾要塞",
        "MP_Fields" to "苏瓦松",
        "MP_Volga" to "伏尔加河",
        "MP_Tsaritsyn" to "察里津",
        "MP_Harbor" to "泽布吕赫",
        "MP_Naval" to "黑尔戈兰湾",
        "MP_Giant" to "庞然暗影",
        "MP_Ridge" to "迫击巴巴",
        "MP_Chateau" to "流血宴厅",
        "MP_Offensive" to "索姆河",
        "MP_ShovelTown" to "尼维尔之夜",
        "MP_Bridge" to "勃鲁西洛夫关口",
        "MP_Scar" to "圣康坦的伤痕",
    )

    /**
     *
     *
     *
     *
     *
     *
     *         "MP_Hell",
     *
     *         "MP_Trench",
     *         "MP_Beachhead"
     */
    var modeCache: HashMap<String, String> = hashMapOf(
        "Conquest" to "征服模式",
        "BreakthroughLarge" to "行动模式",
    )

    data class ServerInfo(
        var map: String = "",
        var mode: String = "",
        var perfix: String = "",
        var teamOneImgUrl: String = "",
        var teamTwoImgUrl: String = "",
        var bots: Int = 0,
        var oldPlayers: Int = 0,
        var players: Int = 0,
        var loadingPlayers: Int = 0,
        val cacheTime: Date,
        val zeroTime:Int = 0,
    ){
        data class Player(
            var team: String,
            var teamId: Int,
            var rank: Int,
            var pid: Long = 0L,
            var platoon: String = "",
            var join_time: Long,
            var latency: Int,
            var isBot: Boolean = false,
            var botState: String = "",
            var lkd: Float = 0f,
            var lkp: Float = 0f,
            var rkd: Float = 0f,
            var rkp: Float = 0f,
        )
    }

    /**
     * 刷新服务器玩家列表
     * @param gameID String
     * @param botGroup String
     * @param botUrl String
     * @param isUseBot Boolean
     */
    fun refreshServerList(gameID: String, botGroup: String, botUrl: String, isUseBot: Boolean) {
        val list = BF1Api.searchServerList(gameID)
        if (list.isSuccessful == false) return
        var botsJson: BotsJson? = null
        if (isUseBot) {
            val bot = BF1Api.postBot(botGroup, botUrl, isLog = false)
            if (!bot.isSuccessful) {
                BF1ToolPlugin.Glogger.warning("唧唧数据获取失败!")
                BF1ToolPlugin.Glogger.warning(bot.reqBody)
            } else {
                botsJson = Gson().fromJson(bot.reqBody, BotsJson::class.java)
            }
        }
        val temp: HashMap<String, ServerInfo.Player> = hashMapOf()
        val tempCacheLife: MutableSet<String> = mutableSetOf()
        var tempServerInfo: ServerInfo? =
            list.serverinfo?.let {
                ServerInfo(
                    map = it.level,
                    perfix = it.name,
                    mode = it.mode,
                    cacheTime = Date(System.currentTimeMillis()),
                )
            }
        var bots = 0
        var players = 0
        var oldPlayers = 0
        list.teams?.forEach { team ->
            team.players.forEach { player ->
                var isBot = false
                var botState = ""
                KickPlayers.removeIf {
                    it == player.name
                }
                cacheLife.forEach {
                    if (player.name == it) tempCacheLife.add(player.name)
                }
                botsJson?.data?.bots?.forEach {
                    if (it.user == player.name) {
                        isBot = true
                        botState = it.state
                    }
                }
                if (player.rank > 120) oldPlayers++
                when (team.teamid) {
                    "teamOne" -> {
                        temp[player.name] =
                            ServerInfo.Player(
                                team = team.name,
                                teamId = 1,
                                rank = player.rank,
                                platoon = player.platoon,
                                pid = player.player_id,
                                join_time = player.join_time,
                                latency = player.latency
                            )
                        tempServerInfo = tempServerInfo?.copy(teamOneImgUrl = team.image)
                    }

                    "teamTwo" -> {
                        temp[player.name] =
                            ServerInfo.Player(
                                team = team.name,
                                teamId = 2,
                                rank = player.rank,
                                platoon = player.platoon,
                                pid = player.player_id,
                                join_time = player.join_time,
                                latency = player.latency
                            )
                        tempServerInfo = tempServerInfo?.copy(teamTwoImgUrl = team.image)
                    }

                    else -> BF1ToolPlugin.Glogger.warning(team.teamid)
                }
                if (isBot) {
                    bots++
                    temp[player.name] = temp[player.name]!!.copy(isBot = true, botState = botState)
                } else {
                    players++
                }
                PlayerListInfo.forEach { (gameId, data) ->
                    if (gameId == gameID)
                        data.forEach { (name, players) ->
                            if (name == player.name) {
                                if (players.isBot) {
                                    temp[player.name] = data[name]!!.copy(
                                        team = team.name,
                                        isBot = true,
                                        teamId = if (team.teamid == "teamOne") 1 else 2
                                    )
                                } else {
                                    temp[player.name] = data[name]!!.copy(
                                        team = team.name,
                                        teamId = if (team.teamid == "teamOne") 1 else 2
                                    )
                                }
                            }
                        }
                }
            }
        }
        cacheLife = tempCacheLife
        if (PlayerListInfo[gameID] == null){
            PlayerListInfo[gameID] = temp
            tempServerInfo = tempServerInfo?.copy(bots = bots, players = players, oldPlayers = oldPlayers, zeroTime = 0)
            if (tempServerInfo != null)
                ServerInfoList[gameID] = tempServerInfo!!
        }else{
            if (temp.size > 0 || ServerInfoList[gameID]?.zeroTime == 1){
                PlayerListInfo[gameID] = temp
                tempServerInfo = tempServerInfo?.copy(bots = bots, players = players, oldPlayers = oldPlayers, zeroTime = 0)
                if (tempServerInfo != null)
                    ServerInfoList[gameID] = tempServerInfo!!
            }else{
                tempServerInfo = tempServerInfo?.copy(bots = bots, players = players, oldPlayers = oldPlayers, zeroTime = 1)
                if (tempServerInfo != null)
                    ServerInfoList[gameID] = tempServerInfo!!
            }
        }
    }
}