package top.ffshaozi.intent

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import data.BotsJson
import top.ffshaozi.data.eac.MultiCheckPostJson
import top.ffshaozi.data.eac.MultiCheckResponse
import kotlinx.coroutines.*
import net.mamoe.mirai.message.data.toPlainText
import top.ffshaozi.NeriQQBot.GlobalBots
import top.ffshaozi.NeriQQBot.Glogger
import top.ffshaozi.config.*
import top.ffshaozi.data.ea.Stats
import top.ffshaozi.utils.BF1Api
import top.ffshaozi.utils.BF1Api.getPlayerListBy22
import top.ffshaozi.utils.BF1Api.searchBFEAC
import top.ffshaozi.utils.PostResponse
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date

/*** 全局数据的缓存
 * @Description
 * @Author littleArray
 * @Date 2023/9/4
 */
object Cache {
    //vip管理服务线程池子 群号
    lateinit var VipCThreadPool: Thread
    var VipAlive = false

    //服务器管理线程池
    lateinit var ServerManageThreadPool: Thread
    var ServerManageAlive = false

    //临时服务器玩家数据
    var PlayerListInfo: MutableList<PlayerCache> = mutableListOf()

    //临时服务器数据
    var ServerInfoList: HashMap<String, ServerInfo> = hashMapOf()

    //机器人所有群
    var BotGroups: String = ""

    //上一条消息缓存
    var lastMsg: String = ""
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
        var opPlayers: String = "",
        var players: Int = 0,
        var oldPlayers: Int = 0,
        var loadingPlayers: Int = 0,
        var spectatorPlayers: Int = 0,
        var isYaoSaiXunHuan: Boolean = false,
        val cacheTime: Date,
        val zeroTime: Int = 0,
    )

    /**
     * 机器人所有保存的玩家数据类
     * @property id EAID
     * @property team 队伍名
     * @property teamId 队伍id
     * @property rank 等级
     * @property pid PID
     * @property platoon 战队
     * @property join_time 加入时间
     * @property latency 延迟
     * @property isBot 是不是唧唧
     * @property botState 唧唧的状态
     * @property gameID 所属gameid
     * @property RSPid 服务器rspid
     * @property ssid 服务器ssid
     * @property lkd 生涯kd
     * @property lkp 生涯kp
     * @property rkd 最近kd
     * @property rkp 最近kp
     * @constructor
     */
    class PlayerCache(
        var id: String,
        var team: String,
        var rank: Int,
        var pid: Long = 0L,
        var platoon: String = "",
        var latency: Int,
        var langLong: Long,
        var isBot: Boolean = false,
        var botState: String = "",
        var gameID: String = "",
        var initTeamID: Int = -1,
        var initRole: String = "-1",
        var lifeMaxKD: Float = 99f,
        var lifeMaxKPM: Float = 99f,
        var recentlyMaxKPM: Float = 99f,
        var recentlyMaxKD: Float = 99f,
    ) {
        /**
         * 当以下数据被改变是将触发自动踢人流程
         */
        var join_time: Long = 0L
            set(value) {
                val old = field
                field = value
                if (old != value) {
                    if (player.initRole == "") {
                        BotLog.spectatorServerLog(Date(join_time / 1000), player.id, player.gameID)
                    }
                    if (!isBot) {
                        BotLog.enterServerLog(Date(join_time / 1000), id, gameID)
                        HistoryLog.addHistoryLog(id, Date(join_time / 1000), gameID)
                        ifReEnter()
                    }
                }
            }
        var teamId: Int = initTeamID
            set(value) {
                val old = field
                field = value
                if (old != value) {
                    if (!isAddPlayers) {
                        playerSizeUpdate()
                    }
                    teamChange()
                }
            }
        var role: String = initRole
            set(value) {
                val old = field
                field = value
                if (old != value) {
                    roleChange()
                }
            }
        var lkd: Float = 0f
            set(value) {
                field = value
                kickByKD()
            }
        var lkp: Float = 0f
            set(value) {
                field = value
                kickByKD()
            }
        var rkd: Float = 0f
            set(value) {
                field = value
                kickByKD()
            }
        var rkp: Float = 0f
            set(value) {
                field = value
                kickByKD()
            }
        var platoonList: MutableList<String> = mutableListOf()
            set(value) {
                field = value
                platoonLimited()
            }
        var platoonTagList: MutableList<String> = mutableListOf()

        private val coroutineScope = CoroutineScope(Dispatchers.IO)
        private val player = this
        private var isDead = false
        private var isKicking = false
        private var isAddPlayers = false
        private var isStandby  = false
        private var isMapChange  = false
        private var messageCD = 0
        private var ssid = ""
        private var RSPid = ""
        private var isEnableAutoKick = false
        private var isEnableReEnterKick = false
        private var isEnableSpKick = false
        private var reEnterKickMsg = ""

        init {
            //更新Player的数据
            limitedUpdate()
            statsUpdate()
            recentlyUpdate()
            playerSizeUpdate()
            ifReEnter()
//            //Log
//            if (!isBot) {
//                if (join_time != 0L && join_time != -1L) {
//                    BotLog.enterServerLog(Date(join_time / 1000), id, gameID)
//                }
//            }
        }

        /**
         * 踢人
         * @param reason 踢人原因
         * @return PostResponse 返回请求
         */
        suspend fun kick(reason: String): PostResponse {
            //log
            BotLog.kickLog(id, gameID, reason)
            return BF1Api.kickPlayer(this.ssid, this.gameID, this.pid.toString(), reason)
        }

        /**
         * ban人
         * @return PostResponse
         */
        suspend fun ban(): PostResponse {
            return BF1Api.addServerBan(this.ssid, this.RSPid.toInt(), this.id)
        }

        /**
         * 换边
         * @return PostResponse
         */
        suspend fun move(): PostResponse {
            return BF1Api.movePlayer(this.ssid, this.gameID, this.pid, if (this.teamId == 1) 2 else 1)
        }

        /**
         * 更新地图
         */
        fun mapChange(){
            isMapChange = true
        }
        /**
         * 角色变更
         */
        private fun roleChange() {
            if (ServerInfoList[gameID] != null) {
                if (player.role == "") {
                    val sp = ServerInfoList[player.gameID]?.opPlayers?.split(";")
                    var isOp = false
                    sp?.forEach {
                        if (player.pid == it.toLong()) isOp = true
                    }
                    if (!isOp && !isKicking) {
                        isKicking = true
                        coroutineScope.launch {
                            val kick = player.kick("禁止觀戰 NoWatching")
                            if (!kick.isSuccessful) {
                                messageCD++
                                if (messageCD % 2 == 0) {
                                    sendMessage(
                                        player.gameID,
                                        "${player.id}在//SC//服偷偷观战没被踹出去"
                                    )
                                }
                            }
                            isKicking = false
                        }
                    }
                    BotLog.spectatorServerLog(Date(join_time), player.id, player.gameID)
                }
            }
        }

        /**
         * 更新服务器的数据
         */
        private fun playerSizeUpdate() {

        }

        /**
         * 账号数据检测
         */
        fun statsUpdate() {
            if (player.lkd > 0 && player.lkp > 0 && !isStandby || isBot) return
            coroutineScope.launch {
                val stats = BF1Api.getAllStats(player.id, false)
                if (stats.isSuccessful) {
                    isStandby = false
                    if (stats.killDeath != null && stats.killDeath > 0) player.lkd = stats.killDeath.toFloat()
                    if (stats.killsPerMinute != null && stats.killsPerMinute > 0) player.lkp =
                        stats.killsPerMinute.toFloat()
                    if (player.rank == 0) player.rank = stats.rank ?: 0
                } else {
                    Glogger.warning("采用备用方案查询 ${player.id} 的生涯数据")
                    val response = BF1Api.getStatsByPID(player.pid.toString(), player.ssid, false)
                    if (response.isSuccessful) {
                        val statsP = Gson().fromJson(response.reqBody, Stats::class.java)
                        if (statsP.result.basicStats.kpm > 0){
                            player.lkp = statsP.result.basicStats.kpm.toFloat()
                        }
                        if (statsP.result.basicStats.kills > 0 &&
                            statsP.result.basicStats.deaths > 0
                        ) {
                            player.lkd = DecimalFormat("#.00")
                                .format(statsP.result.basicStats.kills.toFloat() / statsP.result.basicStats.deaths.toFloat())
                                .toFloat()
                        }
                        isStandby = true
                    }
                }
                if (player.rank > 120) {
                    if (ServerInfoList[gameID] != null) {
                        if (player.role != "") {
                            if (!isBot) {
                                val players = ServerInfoList[gameID]!!.oldPlayers + 1
                                ServerInfoList[gameID] = ServerInfoList[gameID]!!.copy(oldPlayers = players)
                            }
                        }
                    }
                }
                if (player.pid == 0L) player.pid = stats.id ?: 0L
                val _platoonList: MutableList<String> = mutableListOf()
                if (stats.platoons != null) {
                    stats.platoons.forEach {
                        _platoonList.add(it.name)
                        platoonTagList.add(it.tag)
                    }
                }
                if (stats.activePlatoon?.tag != null) {
                    player.platoon = stats.activePlatoon.tag
                    _platoonList.add(stats.activePlatoon.tag)
                    platoonTagList.add(stats.activePlatoon.tag)
                }
                player.platoonList = _platoonList
            }
        }

        /**
         * 最近检测
         */
        fun recentlyUpdate() {
            if (player.rkd > 0 && player.rkp > 0 || isBot) return
            coroutineScope.launch {
                val recentlyJson = BF1Api.recentlySearch(player.id, false)
                run p@{
                    recentlyJson.forEach {
                        if (!it.isSuccessful) return@launch
                        if (it.kd.toFloat() > 0) player.rkd = it.kd.toFloat()
                        if (it.kpm.toFloat() > 0) player.rkp = it.kpm.toFloat()
                        if (it.kpm.toFloat() > 0 && it.kd.toFloat() > 0)
                            return@p
                    }
                }
            }
        }

        /**
         * 更新限制数据
         */
        fun limitedUpdate() {
            ServerInfos.serverInfo.forEach {
                if (it.gameID == player.gameID) {
                    ssid = it.sessionId.toString()
                    RSPid = it.serverRspID.toString()
                    isEnableAutoKick = it.isEnableAutoKick
                    isEnableSpKick = it.isEnableSpKick
                    lifeMaxKD = it.lifeMaxKD
                    lifeMaxKPM = it.lifeMaxKPM
                    recentlyMaxKPM = it.recentlyMaxKPM
                    recentlyMaxKD = it.recentlyMaxKD
                    isEnableReEnterKick = it.isEnableReEnterKick
                    if (isEnableReEnterKick) {
                        reEnterKickMsg = it.ReEnterKickMsg
                    }
                }
            }
        }

        /**
         * 限制战队
         */
        private fun platoonLimited() {
            //该服务器没有启用自动踢人
            if (!isEnableAutoKick) return
            val isW = Bindings.recentlyTempWhitelist.any { player.id == it }
            if (isW) return
            player.platoonList.forEach { pp ->
                if (isKicking) return
                var isLimited = false
                ServerInfos.serverInfo.forEach {
                    if (it.gameID == player.gameID) {
                        it.platoonLimited.forEach {
                            if (pp == it) isLimited = true
                        }
                    }
                }
                if (isLimited && !isKicking) {
                    isKicking = true
                    coroutineScope.launch {
                        val kick = player.kick("Platoon Limited")
                        if (!kick.isSuccessful) {
                            messageCD++
                            if (messageCD % 2 == 0) {
                                sendMessage(
                                    player.gameID,
                                    "${player.id}在//SC//服没被踹出去\n战队:${player.platoonList}"
                                )
                            }
                        }
                        isKicking = false
                    }
                }
            }
        }

        /**
         * 因为超过KD被踢
         */
        private fun kickByKD() {
            //Glogger.info("整理${player.id}的数据 生涯KD:${player.lkd} KPM:${player.lkp} 最近KD:${player.rkd} KPM:${player.rkp}")
            //跳过唧唧检查
            if (player.isBot) return
            //跳过正在踢人
            if (isKicking) return
            //该服务器没有启用自动踢人
            if (!isEnableAutoKick) return
            val isW = Bindings.recentlyTempWhitelist.any { player.id == it }
            if (isW) return
            if (
                player.lkd > player.lifeMaxKD ||
                player.lkp > player.lifeMaxKPM ||
                player.rkd > player.recentlyMaxKD ||
                player.rkp > player.recentlyMaxKPM
            ) {
                if (!isKicking) {
                    isKicking = true
                    coroutineScope.launch {
                        val kick = player.kick("KD Limited")
                        if (!kick.isSuccessful) {
                            messageCD++
                            if (messageCD % 2 == 0) {
                                sendMessage(
                                    player.gameID,
                                    "臭捞逼${player.id}在//SC//服没被踹出去\n生涯KD:${player.lkd} KPM:${player.lkp} 最近KD:${player.rkd} KPM:${player.rkp}"
                                )
                            }
                        }
                        isKicking = false
                    }
                }
            }
        }

        /**
         * 更换队伍事件
         */
        private fun teamChange() {
            if (!isMapChange){
                Glogger.info("玩家${id}在${gameID}服换边 -> $teamId")
                //log
                if (!isBot)
                    BotLog.teamChangeLog(id, gameID, teamId.toString())

            }else{
                isMapChange = false
            }
        }

        /**
         * 重进事件
         */
        private fun ifReEnter() {
            if (!isBot) {
                BotLog.exitServerLog.forEach { (time, data) ->
                    val exitTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(time)
                    val cd = 10 * 60 * 1000
                    if (join_time / 1000 - exitTime.time < cd) {
                        val sp = data.split(" ")
                        if (sp[0] == player.id && sp[1] == player.gameID) {
                            BotLog.enterServerLog(Date(join_time / 1000), id, gameID)
                            BotLog.reEnterServerLog(Date(player.join_time / 1000), player.id, player.gameID)
                            Bindings.bindingData.forEach { (qq, id) ->
                                if (id == player.id) {
                                    isEnableReEnterKick = false
                                }
                            }
                            if (isEnableReEnterKick) {
                                if (!isKicking) {
                                    isKicking = true
                                    coroutineScope.launch {
                                        val kick = player.kick(reEnterKickMsg)
                                        if (!kick.isSuccessful) {
                                            messageCD++
                                            if (messageCD % 2 == 0) {
                                                sendMessage(
                                                    player.gameID,
                                                    "玩家${player.id}在//SC//服短时间内重进没被踹出去"
                                                )
                                            }
                                        }
                                        isKicking = false
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        /**
         * 善后工作
         */
        fun dead() {
            isDead = true
            coroutineScope.cancel()
            if (ServerInfoList[gameID] != null) {
                if (player.role == "") {
                    val players = ServerInfoList[gameID]!!.spectatorPlayers - 1
                    ServerInfoList[gameID] = ServerInfoList[gameID]!!.copy(spectatorPlayers = players)
                }
                if (player.rank > 120) {
                    val oldPlayers = ServerInfoList[gameID]!!.oldPlayers - 1
                    ServerInfoList[gameID] = ServerInfoList[gameID]!!.copy(oldPlayers = oldPlayers)
                }
            }
            //Log
            if (!isBot)
                BotLog.exitServerLog(Date(System.currentTimeMillis()), id, gameID)
        }
    }


    /**
     * 刷新服务器玩家列表
     * @param gameID String
     * @param botGroup String
     * @param botUrl String
     * @param isUseBot Boolean
     */
    fun refreshServerList() {
        //缓存已经查了的GameID
        val gameIDCache = mutableListOf<String>()
        ServerInfos.serverInfo.forEach {
            run p@{
                val oldGameID = it.gameID ?: return@p
                //判断缓存
                if (gameIDCache.any { it == oldGameID }) return@p
                //val list = BF1Api.searchServerList(oldGameID)
                var list = getPlayerListBy22(oldGameID)
                //防止数据有误
                var size = 0
                list.GDAT?.get(0)?.ROST?.forEach { p ->
                    size++
                }
                if (size == 0) {
                    runBlocking c@{
                        //重试3次
                        repeat(2) {
                            //Glogger.warning("$oldGameID 玩家列表为空,正在纠正")
                            list = getPlayerListBy22(oldGameID)
                            var _size = 0
                            list.GDAT?.get(0)?.ROST?.forEach { p ->
                                _size++
                            }
                            if (_size > 0) return@c
                            delay(3000)
                        }
                    }
                }
                if (!list.isSuccessful) return@p
                //添加缓存
                gameIDCache.add(oldGameID)
                var botsJson: BotsJson? = null
                if (it.isUseBot) {
                    val bot = BF1Api.getBots(it.botGroup.toString(), it.botUrl, isLog = false)
                    if (!bot.isSuccessful) {
                        Glogger.warning("唧唧数据获取失败!")
                        Glogger.warning(bot.reqBody)
                    } else {
                        try {
                            botsJson = Gson().fromJson(bot.reqBody, BotsJson::class.java)
                        } catch (e: JsonSyntaxException) {
                            Glogger.error(e.stackTraceToString())
                        }
                        //记录可用唧唧数量
                        val remain = (botsJson?.data?.totalCount ?: 0) -
                                (botsJson?.data?.abnormalCount ?: 0) -
                                (botsJson?.data?.notStartedCount ?: 0) -
                                (botsJson?.data?.usedCount ?: 0) -
                                (botsJson?.data?.offlineCount ?: 0) -
                                (botsJson?.data?.startingCount ?: 0)

                        if (remain > 50) {
                            sendMessage(oldGameID, "唧唧可用数大于50")
                            Glogger.warning("唧唧可用数:$remain")
                        }

                    }
                }
                val admin = (list.GDAT?.get(0)?.ATTR?.admins1 ?: "") +
                        (list.GDAT?.get(0)?.ATTR?.admins2 ?: "") +
                        (list.GDAT?.get(0)?.ATTR?.admins3 ?: "") +
                        (list.GDAT?.get(0)?.ATTR?.admins4 ?: "")
                //判断缓存里有没有这个玩家
                val oldPlayers = mutableSetOf<String>()
                val newPlayers = mutableSetOf<String>()
                val leavePlayers = mutableSetOf<String>()
                //玩家数量
                var players = 0
                var loadingPlayers = 0
                var spectatorPlayers = 0
                var bots = 0
                list.GDAT?.get(0)?.ROST?.forEach { p ->
                    //真实玩家
                    if (botsJson?.data?.bots?.any { it.user == p.NAME } != null && !botsJson.data.bots.any { it.user == p.NAME } && p.ROLE != "" && p.TIDX.toInt() != 65535) {
                        players++
                    }
                    //加载中玩家
                    if (p.TIDX.toInt() != 0 && p.TIDX.toInt() != 1) loadingPlayers++
                    //观战玩家
                    if (p.ROLE == "") spectatorPlayers++
                    //机器人
                    if (botsJson?.data?.bots?.any { it.user == p.NAME } == true) bots++
                    //新玩家
                    if (PlayerListInfo.none { it.id == p.NAME })
                        newPlayers.add(p.NAME)
                    //老玩家
                    if (PlayerListInfo.any { it.id == p.NAME })
                        oldPlayers.add(p.NAME)
                }
                /*if (ServerInfoList[oldGameID] != null) {
                    //防止数据有误
                    if (players == 0) {
                        ServerInfoList[oldGameID] =
                            ServerInfoList[oldGameID]!!.copy(zeroTime = ServerInfoList[oldGameID]!!.zeroTime + 1)
                        //1次都是空的话就一定是空的
                        if (ServerInfoList[oldGameID]!!.zeroTime < 1) {
                            Glogger.warning("请求玩家列表可能为空,终止此次执行")
                            return@p
                        } else {
                            ServerInfoList[oldGameID] =
                                ServerInfoList[oldGameID]!!
                                    .copy(zeroTime = 0)
                        }

                    }
                }*/
                //掉人提醒
                if ((ServerInfoList[oldGameID]?.players ?: 0) > players) {
                    if (players in 30..47) {//剩余玩家30到47
                        sendMessage(
                            oldGameID,
                            "快救//SC//服,要寄了 剩余:$players 人"
                        )
                    }
                    if (players == 0) {
                        if ((ServerInfoList[oldGameID]?.players ?: 0) != 1)
                            sendMessage(
                                oldGameID,
                                "//SC//服,死完了"
                            )

                    }
                }
                //进人提醒
                if ((ServerInfoList[oldGameID]?.players ?: 0) < players) {
                    if (players > 60 && (ServerInfoList[oldGameID]?.players ?: 0) < 60) {
                        sendMessage(
                            oldGameID,
                            "//SC//服暖好了 活人:$players"
                        )
                    }
                }
                //离开的玩家
                PlayerListInfo.forEach { old ->
                    var isLeave = true
                    list.GDAT?.get(0)?.ROST?.forEach { p ->
                        if (old.id == p.NAME) isLeave = false
                    }
                    if (isLeave && oldGameID == old.gameID) leavePlayers.add(old.id)
                }
                //屁都没了
                if (list.GDAT?.get(0)?.ROST?.size == 0) {
                    PlayerListInfo.forEach { old ->
                        leavePlayers.add(old.id)
                    }
                }
                //存储上一把的map
                val oldMap = ServerInfoList[oldGameID]?.map ?:""
                //加入服务器数据
                ServerInfoList[oldGameID] = ServerInfoList[oldGameID]
                    ?.copy(
                        map = list.GDAT?.get(0)?.ATTR?.level ?: "",
                        mode = list.GDAT?.get(0)?.ATTR?.mode ?: "",
                        perfix = list.GDAT?.get(0)?.GNAM ?: "",
                        zeroTime = ServerInfoList[oldGameID]?.zeroTime ?: 0,
                        cacheTime = Date(System.currentTimeMillis()),
                        opPlayers = admin,
                        players = players,
                        loadingPlayers = loadingPlayers,
                        spectatorPlayers = spectatorPlayers,
                        bots = bots
                    ) ?: ServerInfo(
                    map = list.GDAT?.get(0)?.ATTR?.level ?: "",
                    mode = list.GDAT?.get(0)?.ATTR?.mode ?: "",
                    perfix = list.GDAT?.get(0)?.GNAM ?: "",
                    zeroTime = ServerInfoList[oldGameID]?.zeroTime ?: 0,
                    cacheTime = Date(System.currentTimeMillis()),
                    opPlayers = admin,
                    players = players,
                    loadingPlayers = loadingPlayers,
                    spectatorPlayers = spectatorPlayers,
                    bots = bots
                )
                //要塞循环实现
                if (ServerInfoList[oldGameID] != null) {
                    if (ServerInfoList[oldGameID]!!.isYaoSaiXunHuan) {
                        if (ServerInfoList[oldGameID]!!.map != "MP_Underworld") {
                            ServerInfos.serverInfo.forEach {
                                if (it.gameID == oldGameID) {
                                    val serverInfoJson = BF1Api.getFullServerDetails(it.sessionId ?: "", oldGameID)
                                    serverInfoJson.result?.serverInfo?.rotation?.forEachIndexed { index, ctx ->
                                        if (ctx.mapPrettyName == "法烏克斯要塞") {
                                            BF1Api.chooseServerVIP(
                                                it.sessionId ?: "",
                                                serverInfoJson.result.rspInfo.server.persistedGameId,
                                                index.toString()
                                            )
                                            Glogger.warning("已启用要塞循环,强制切图")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                //离开的玩家整理
                leavePlayers.forEach { lp ->
                    PlayerListInfo.removeIf {
                        if (lp == it.id) {
                            it.dead()
                            // Glogger.info("离开玩家整理中 :${lp}")
                            true
                        } else {
                            false
                        }
                    }
                }
                /*//进人提醒
                if (ServerInfoList[oldGameID]!!.players < players) {
                    if (players - ServerInfoList[oldGameID]!!.players > 3) {//进人数大于3
                        sendMessage(oldGameID, "//SC//服进人啦 ${ServerInfoList[oldGameID]!!.players} -> $players")
                    }
                }*/
                list.GDAT?.get(0)?.ROST?.forEach { p ->
                    //旗帜数据整理
                    /*if (p.teamid == "teamOne")
                        ServerInfoList[oldGameID] =
                            ServerInfoList[oldGameID]!!.copy(teamOneImgUrl = newTeam.image)
                    else
                        ServerInfoList[oldGameID] =
                            ServerInfoList[oldGameID]!!.copy(teamTwoImgUrl = newTeam.image)*/
                    //新玩家整理
                    if (newPlayers.any { p.NAME == it }) {
                        //Glogger.info("新玩家整理中 :${p.NAME}")
                        val temp = PlayerCache(
                            id = p.NAME,
                            team = "",
                            rank = (p.PATT?.rank ?: "0").toInt(),
                            latency = (p.PATT?.latency ?: "0").toInt(),
                            pid = p.PID,
                            platoon = "",
                            isBot = botsJson?.data?.bots?.any { it.user == p.NAME } ?: false,
                            gameID = oldGameID,
                            initTeamID = p.TIDX.toInt(),
                            initRole = p.ROLE,
                            langLong = p.LOC
                        )
                        if (p.JGTS != 0L && p.JGTS != -1L) {
                            temp.join_time = p.JGTS
                        }
                        PlayerListInfo.add(temp)
                    }
                    //老玩家整理
                    if (oldPlayers.any { p.NAME == it }) {
                        //Glogger.info("老玩家整理中 :${p.NAME}")
                        //更新原数据
                        PlayerListInfo.forEach {
                            if (p.NAME == it.id) {
                                if (botsJson != null) {
                                    it.isBot = botsJson.data.bots.any { it.user == p.NAME }
                                }
                                it.gameID = oldGameID.toString()
                                it.team = ""
                                it.latency = (p.PATT?.latency ?: "0").toInt()
                                if (p.JGTS != 0L && p.JGTS != -1L) {
                                    it.join_time = p.JGTS
                                }
                                if (oldMap == ServerInfoList[oldGameID]?.map){
                                    it.teamId = p.TIDX.toInt()
                                }else{
                                    it.mapChange()
                                    it.teamId = p.TIDX.toInt()
                                }
                                it.role = p.ROLE
                                it.langLong = p.LOC
                                it.limitedUpdate()
                                it.recentlyUpdate()
                                it.statsUpdate()
                            }
                        }
                    }
                }
                //BFEAC检测
                //Glogger.info("BFEAC批量检查")
                val cacheBan: MultiCheckResponse
                val cacheList = MultiCheckPostJson()
                PlayerListInfo.forEach { data ->
                    cacheList.pids.add(data.pid)
                }
                if (cacheList.pids.size == 0) return@p
                cacheBan = searchBFEAC(cacheList, false)
                cacheBan.data.forEach {
                    PlayerListInfo.forEach { data ->
                        if (data.pid == it) {
                            CoroutineScope(Dispatchers.IO).launch {
                                val kick = data.kick("掛鉤滾 BFEAC BAN")
                                if (kick.isSuccessful)
                                    sendMessage(data.gameID, "挂钩${data.id}在//SC//服被狠狠上市了")
                                else {
                                    Glogger.error("挂钩${data.id}踢不动,他在//SC//服")
                                    sendMessage(data.gameID, "挂钩${data.id}踢不动,他在//SC//服")
                                }
                            }
                        }
                    }


                }

            }
        }
        //Glogger.info("玩家整理完毕 现存数量:${PlayerListInfo.size}")
    }

    /**
     * 通过GameID发送消息到指定群聊
     * @param gameID String
     * @param msg Any
     */
    fun sendMessage(gameID: String, msg: String) {
        GroupSetting.groupSetting.forEach { ctx ->
            CoroutineScope(Dispatchers.IO).launch {
                if (lastMsg != msg) {
                    ctx.games.forEach {
                        if (it.gameID == gameID && it.isEnableBroadcast) {
                            val _msg = msg.replace("//SC//", it.name)
                            GlobalBots.forEach { bot ->
                                ctx.groupID?.let { it1 -> bot.getGroup(it1)?.sendMessage(_msg.toPlainText()) }
                                delay(5000)
                            }
                        }
                    }
                    lastMsg = msg
                }
            }
        }
    }
}