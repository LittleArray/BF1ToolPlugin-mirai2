package top.ffshaozi.intent

import com.google.gson.Gson
import data.BotsJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import top.ffshaozi.NeriQQBot
import top.ffshaozi.NeriQQBot.save
import top.ffshaozi.config.BotReportLog
import top.ffshaozi.utils.BF1Api
import top.ffshaozi.utils.PostResponse
import java.text.SimpleDateFormat
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

    //服务器管理线程池
    var ServerManageThreadPool: HashMap<Long, Boolean> = hashMapOf()

    //临时服务器玩家数据
    var PlayerListInfo: HashMap<String,  MutableList<PlayerCache>> = hashMapOf()

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
    )

    class PlayerCache(
        var id:String,
        var team: String,
        var teamId: Int,
        var rank: Int,
        var pid: Long = 0L,
        var platoon: String = "",
        var join_time: Long,
        var latency: Int,
        var isBot: Boolean = false,
        var botState: String = "",
        var gameID: String = "",
        var RSPid: String ="",
        var ssid:String =""
    ){
        var lkd: Float = 0f
            set(value) {
                field = value
            }
        var lkp: Float = 0f
            set(value) {
                field = value
            }
        var rkd: Float = 0f
            set(value) {
                field = value
            }
        var rkp: Float = 0f
            set(value) {
                field = value
            }

        private val coroutineScope = CoroutineScope(Dispatchers.IO)

        fun kick(reason:String): PostResponse {
            return BF1Api.kickPlayer(this.ssid,this.gameID,this.pid.toString(),reason)
        }
        fun ban():PostResponse{
            return BF1Api.addServerBan(this.ssid,this.RSPid.toInt(),this.id)
        }
        fun stats(){

        }
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
            val bot = BF1Api.getBots(botGroup, botUrl, isLog = false)
            if (!bot.isSuccessful) {
                NeriQQBot.Glogger.warning("唧唧数据获取失败!")
                NeriQQBot.Glogger.warning(bot.reqBody)
            } else {
                botsJson = Gson().fromJson(bot.reqBody, BotsJson::class.java)
                //NeriQQBot.Glogger.warning("唧唧总数:${botsJson?.data?.totalCount}")
            }
        }
        val temp: MutableList<PlayerCache> = mutableListOf()
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
                        temp.add(
                            PlayerCache(
                                team = team.name,
                                teamId = 1,
                                rank = player.rank,
                                platoon = player.platoon,
                                pid = player.player_id,
                                join_time = player.join_time,
                                latency = player.latency,
                                id = player.name
                            )
                        )
                        tempServerInfo = tempServerInfo?.copy(teamOneImgUrl = team.image)
                    }

                    "teamTwo" -> {
                        temp.add(
                            PlayerCache(
                                team = team.name,
                                teamId = 2,
                                rank = player.rank,
                                platoon = player.platoon,
                                pid = player.player_id,
                                join_time = player.join_time,
                                latency = player.latency,
                                id = player.name
                            )
                        )
                        tempServerInfo = tempServerInfo?.copy(teamTwoImgUrl = team.image)
                    }

                    else -> NeriQQBot.Glogger.warning(team.teamid)
                }
                if (isBot) {
                    bots++
                    temp.forEach {
                        if (it.id == player.name){
                            it.isBot = true
                            it.botState = botState
                        }
                    }
                } else {
                    players++
                }
                PlayerListInfo.forEach { (gameId, data) ->
                    if (gameId == gameID)
                        data.forEach { it ->
                            if (it.id == player.name) {
                                if (it.isBot) {
                                    temp.forEach {
                                        if (it.id == player.name){
                                            it.team = team.name
                                            it.teamId = if (team.teamid == "teamOne") 1 else 2
                                            it.isBot = true
                                        }
                                    }
                                } else {
                                    temp.forEach {
                                        if (it.id == player.name){
                                            it.team = team.name
                                            it.teamId = if (team.teamid == "teamOne") 1 else 2
                                            it.isBot = false
                                        }
                                    }
                                }
                            }
                        }
                }
            }
        }
        PlayerListInfo.forEach { (gameId, data) ->
            if (gameId == gameID){
                if (data.size == 0 ){
                    BotReportLog.botLog2 = mutableMapOf()
                }
                temp.forEach { t->
                    val temp2 = mutableSetOf<String>()
                    BotReportLog.botLog2.forEach { (gameid, data4) ->
                        if (gameId == gameid){
                            data4.forEach {
                                temp2.add(it)
                            }
                        }
                    }
                    temp2.add(t.id)
                    BotReportLog.botLog2[gameID] = temp2
                    var isOnServer = false
                    var join_time = 0L
                    data.forEach {
                        if (it.id == t.id) {
                            isOnServer = true
                        }else{
                            join_time = it.join_time
                        }
                    }
                    if (!isOnServer){
                        val time = if (join_time / 1000 > 0) join_time / 1000 else System.currentTimeMillis()
                        BotReportLog.botLog.add("[${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time)}] ID:${t.id}进入服务器 $gameID")
                    }
                }
                data.forEach { d ->
                    var isLeaveServer = true
                    temp.forEach {
                        if (it.id == d.id) isLeaveServer = false
                    }
                    if (isLeaveServer){
                        BotReportLog.botLog.add("[${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis())}±15s] ID:${d.id}退出服务器 $gameID")
                        BotReportLog.botLog2.forEach { (gameid, data4) ->
                            if (gameId == gameid){
                                data4.remove(d.id)
                            }
                        }
                    }
                }
            }
        }
        BotReportLog.save()
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