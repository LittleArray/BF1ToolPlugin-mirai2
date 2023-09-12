package top.ffshaozi.intent

import data.MultiCheckPostJson
import data.MultiCheckResponse
import kotlinx.coroutines.*
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import top.ffshaozi.NeriQQBot
import top.ffshaozi.config.DataForGroup
import top.ffshaozi.config.Setting
import top.ffshaozi.config.Setting.groupData
import top.ffshaozi.intent.Intent.sendMsg
import top.ffshaozi.utils.BF1Api
import top.ffshaozi.utils.BF1Api.getPersonaid
import top.ffshaozi.utils.BF1Api.getStats
import top.ffshaozi.utils.BF1Api.kickPlayer
import top.ffshaozi.utils.BF1Api.recentlySearch
import top.ffshaozi.utils.BF1Api.searchBFEAC
import top.ffshaozi.utils.BF1Api.searchBFEACByPid

/**
 * @Description
 * @Author littleArray
 * @Date 2023/8/25
 */
object CycleTask {

    fun serverManageRefresh(I: PullIntent): Message {
        serverPlayerR(I)
        serverPlayerDataR(I)
        serverListR(I)
        return if (Cache.ServerManageThreadPool[I.event.group.id] == null || Cache.ServerManageThreadPool[I.event.group.id] == false) {
            Cache.ServerManageThreadPool[I.event.group.id] = true
            "启用玩家管理服务".toPlainText()
        } else {
            Cache.ServerManageThreadPool[I.event.group.id] = false
            "关闭玩家管理服务".toPlainText()
        }
    }

    //Thread - 服务器列表更新线程
    fun serverListR(I: PullIntent) {
        if (Cache.ListThreadPool[I.event.group.id]?.isAlive == null || Cache.ListThreadPool[I.event.group.id]?.isAlive == false) {
            Cache.ListThreadPool[I.event.group.id] = Thread {
                while (true) {
                    serverInfoIterator { groupID, data, index, serverInfoForSave ->
                        run a@{
                            if (groupID != I.event.group.id) return@a
                            if (serverInfoForSave.gameID.isNullOrEmpty()) return@a
                            Cache.refreshServerList(
                                serverInfoForSave.gameID!!,
                                data.botGroup,
                                data.botUrl,
                                data.isUseBot
                            )
                        }
                    }
                    serverEAC(I)
                    NeriQQBot.Glogger.info("服务器列表更新线程")
                    Thread.sleep(25 * 1000)
                }
            }
            Cache.ListThreadPool[I.event.group.id]?.start()
        } else {
            Cache.ListThreadPool[I.event.group.id]?.stop()
        }
    }

    //Thread - BFEAC检测(缓存算法)
    fun serverEAC(I: PullIntent) {
        NeriQQBot.Glogger.info("挂钩开踹")
        serverInfoIterator { groupID, data, index, serverInfoForSave ->
            run p@{
                if (groupID != I.event.group.id) return@p
                val cacheBan: MultiCheckResponse
                val cacheList = MultiCheckPostJson()
                Cache.PlayerListInfo[serverInfoForSave.gameID]?.forEach { (name, data) ->
                    var isKick = false
                    Cache.KickPlayers.forEach {
                        if (it == name) isKick = true
                    }
                    if (!isKick)
                        cacheList.pids.add(data.pid)
                }
                if (cacheList.pids.size == 0) return@p
                cacheBan = searchBFEAC(cacheList, false)
                cacheBan.data.forEach {
                    var name = ""
                    Cache.PlayerListInfo[serverInfoForSave.gameID]?.forEach { (id, data) ->
                        if (data.pid == it) name = id
                    }
                    val kickPlayer = kickPlayer(
                        serverInfoForSave.sessionId!!,
                        serverInfoForSave.gameID!!,
                        it.toString(),
                        "掛鉤給老子滾 BFEAC BAN"
                    )
                    if (!kickPlayer.isSuccessful) {
                        sendMsg(
                            I,
                            "挂钩${name}在${index}服上市了失败\n${kickPlayer.reqBody}"
                        )
                    } else {
                        val case_id = searchBFEACByPid(it.toString(), false).data?.case_id
                        sendMsg(
                            I,
                            "挂钩${name}在${index}服被狠狠上市了\nhttps://www.bfeac.com/?#/case/$case_id"
                        )
                        NeriQQBot.Glogger.error("挂钩${name}在${index}服被狠狠上市了 https://www.bfeac.com/?#/case/$case_id")
                        Cache.PlayerListInfo[serverInfoForSave.gameID!!]?.remove(name)
                        Cache.KickPlayers.add(name)
                    }
                }
            }
        }
    }

    /**
     * 玩家数量检测-线程
     * @param I PullIntent
     */
    fun serverPlayerR(I: PullIntent) {
        if (Cache.PlayerThreadPool[I.event.group.id]?.isAlive == null || Cache.PlayerThreadPool[I.event.group.id]?.isAlive == false) {
            Cache.PlayerThreadPool[I.event.group.id] = Thread {
                val players: MutableMap<String, Int> = mutableMapOf()
                val cd: MutableMap<String, Int> = mutableMapOf()
                while (true) {
                    serverInfoIterator { groupID, data, index, serverInfoForSave ->
                        run p@{
                            if (groupID != I.event.group.id) return@p
                            val gameID = serverInfoForSave.gameID!!
                            var (player, bot, loadingBots) = mutableListOf(0, 0, 0)
                            Cache.PlayerListInfo[serverInfoForSave.gameID]?.forEach { (name, data) ->
                                if (data.isBot) {
                                    bot++
                                    if (data.botState == "Loading") loadingBots++
                                } else {
                                    player++
                                }
                            }
                            if (players[gameID] == null) players[gameID] = 0
                            if (players[gameID] != player) {
                                if (player > 60) {
                                    players[gameID] = player
                                    return@p
                                }
                                if (player < 5) {
                                    players[gameID] = player
                                    return@p
                                }
                                if (player < 45) {
                                    if (player < players[gameID]!!) {
                                        sendMsg(
                                            I, """
                                            ${index}服人数快带完了,快救服
                                            ${players[gameID]} --> $player 
                                            """.trimIndent()
                                        )
                                        players[gameID] = player
                                        return@p
                                    }
                                }
                                if (cd[gameID] == null) cd[gameID] = 0
                                if (cd[gameID]!! > 2) {
                                    cd[gameID] = 0
                                    return@p
                                }
                                val botText = if (bot > 0) {
                                    "唧唧数量:${bot} 加载中:${loadingBots}"
                                } else {
                                    ""
                                }
                                sendMsg(
                                    I, """
                                            ${index}服人数变化
                                            ${players[gameID]} --> $player 
                                            $botText
                                            """.trimIndent()
                                )
                            }
                            if (cd[gameID] == null) cd[gameID] = 0
                            cd[gameID] = cd[gameID]!! + 1
                            players[gameID] = player
                        }
                    }
                    NeriQQBot.Glogger.info("玩家数量检测")
                    Thread.sleep(60 * 1000)
                }
            }
            Cache.PlayerThreadPool[I.event.group.id]?.start()
        } else {
            Cache.PlayerThreadPool[I.event.group.id]?.stop()
        }
    }


    /*    //Thread - 踢人线程
        fun serverRKDR(I: PullIntent) {
            if (Cache.RPDataThreadPool[I.event.group.id]?.isAlive == null || Cache.RPDataThreadPool[I.event.group.id]?.isAlive == false) {
                Cache.RPDataThreadPool[I.event.group.id] = Thread {
                    while (true) {

                        BF1ToolPlugin.Glogger.info("踢人线程")
                        Thread.sleep(30 * 1000)
                    }
                }
                Cache.RPDataThreadPool[I.event.group.id]?.start()
            } else {
                Cache.RPDataThreadPool[I.event.group.id]?.stop()
            }
        }*/

    //Thread - 玩家数据检测(缓存算法)
    fun serverPlayerDataR(I: PullIntent) {
        if (Cache.PlayerDataThreadPool[I.event.group.id]?.isAlive == null || Cache.PlayerDataThreadPool[I.event.group.id]?.isAlive == false) {
            Cache.PlayerDataThreadPool[I.event.group.id] = Thread {
                while (true) {
                    var btrIsErr = false
                    val tempCo = mutableListOf<Job>()
                    CoroutineScope(Dispatchers.IO).launch {
                        serverInfoIterator { groupID, data, serverCount, serverInfoForSave ->
                            run p@{
                                if (groupID != I.event.group.id) return@p
                                if (Cache.cacheLife.size > 300) Cache.cacheLife = mutableSetOf()
                                Cache.PlayerListInfo[serverInfoForSave.gameID]?.forEach { (name, plData) ->
                                    tempCo.add(launch {
                                        var rkd = 0f
                                        var rkpm = 0f
                                        var lkd: Float
                                        var lkpm: Float
                                        run c@{
                                            //生涯检测
                                            val stats = getStats(name, false)
                                            lkd = stats.killDeath.toFloat()
                                            lkpm = stats.killsPerMinute.toFloat()
                                            if (plData.rank == 0) plData.rank = stats.rank
                                            if (plData.pid == 0L) plData.pid = stats.id
                                            if (lkd > 0 && lkpm > 0 && plData.pid > 0 && plData.rank != 0) {
                                                Cache.cacheLife.add(name)
                                            }
                                            //最近数据检测
                                            val recentlyJson = recentlySearch(name, false)
                                            if (recentlyJson.isNotEmpty()) {
                                                run fe@{
                                                    recentlyJson.forEachIndexed { index2, it ->
                                                        if (index2 > 2) return@fe
                                                        if (it.isSuccessful) {
                                                            if (it.tp.isNotEmpty()) {
                                                                val sp = it.tp.split(" ")
                                                                if (sp.size > 1 || sp[0].replace("m", "").toInt() > 30
                                                                ) {
                                                                    rkd = it.kd.toFloat()
                                                                    rkpm = it.kpm.toFloat()
                                                                    return@fe
                                                                }
                                                            } else {
                                                                btrIsErr = true
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            if (lkd > 0 && lkpm > 0) {
                                                Cache.PlayerListInfo[serverInfoForSave.gameID]!![name] = plData.copy(lkd = lkd, lkp = lkpm, rkd = rkd, rkp = rkpm)
                                                if (lkd > serverInfoForSave.lifeMaxKD ||
                                                    lkpm > serverInfoForSave.lifeMaxKPM ||
                                                    rkpm > serverInfoForSave.recentlyMaxKPM ||
                                                    rkd > serverInfoForSave.recentlyMaxKD
                                                ) {
                                                    if (!plData.isBot){
                                                        if (serverInfoForSave.isEnableAutoKick) {
                                                            var isW = false
                                                            groupData[I.event.group.id]?.recentlyTempWhitelist?.forEach {
                                                                if (name == it) isW=true
                                                            }
                                                            if (!isW){
                                                                val pid = getPersonaid(name)
                                                                val kickPlayer = kickPlayer(
                                                                    serverInfoForSave.sessionId!!,
                                                                    serverInfoForSave.gameID!!,
                                                                    pid.id.toString(),
                                                                    "KD/KPM Limited"
                                                                )
                                                                if (!kickPlayer.isSuccessful) {
                                                                    NeriQQBot.Glogger.error("踹出老毕登${name}最近KD:${rkd} 最近KPM:${rkpm} 生涯KD:${lkd} 生涯KPM:${rkpm}失败")
                                                                    sendMsg(
                                                                        I,
                                                                        "踹出老毕登${name}最近KD:${rkd}失败\n最近KD:${rkd} 最近KPM:${rkpm} 生涯KD:${lkd} 生涯KPM:${rkpm}"
                                                                    )
                                                                } else {
                                                                    NeriQQBot.Glogger.warning("踹出老毕登${name}成功\n最近KD:${rkd} 最近KPM:${rkpm} 生涯KD:${lkd} 生涯KPM:${rkpm}")
                                                                    Cache.PlayerListInfo[serverInfoForSave.gameID!!]?.remove(name)
                                                                    Cache.KickPlayers.add(name)
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    })
                                }
                            }
                        }
                        tempCo.joinAll()
                        if (btrIsErr) {
                            NeriQQBot.Glogger.info("最近数据检测失败,Btr寄了")
                        }
                        NeriQQBot.Glogger.info("玩家数据检测")
                    }
                    Thread.sleep(30 * 1000)
                }
            }
            Cache.PlayerDataThreadPool[I.event.group.id]?.start()
        } else {
            Cache.PlayerDataThreadPool[I.event.group.id]?.stop()
        }
    }

    fun vipRefresh(I: PullIntent): Message {
        if (Cache.VipCThreadPool[I.event.group.id]?.isAlive == false || Cache.VipCThreadPool[I.event.group.id]?.isAlive == null) {
            Cache.VipCThreadPool[I.event.group.id] = Thread {
                while (true) {
                    //移除VIP
                    Setting.groupData.forEach { groupID, Data ->
                        if (groupID == I.event.group.id) {
                            Data.server.forEach {
                                var removeID = ""
                                it.vipList.forEach { (id, endTime) ->
                                    if (System.currentTimeMillis() > endTime) {
                                        removeID = id
                                    }
                                }
                                if (removeID.isNotEmpty()) {
                                    val pid = BF1Api.getPersonaid(removeID)
                                    if (BF1Api.removeServerVIP(
                                            it.sessionId.toString(),
                                            it.serverRspID,
                                            pid.id.toString()
                                        ).isSuccessful
                                    ) {
                                        it.vipList.remove(removeID)
                                        sendMsg(I, "已移除${removeID}的Vip,原因:过期了")
                                    }
                                }
                            }
                        }
                    }
                    NeriQQBot.Glogger.info("VIP管理服务")
                    Thread.sleep(10000)
                }
            }
            Cache.VipCThreadPool[I.event.group.id]?.start()
            return "启用VIP管理服务".toPlainText()
        } else {
            Cache.VipCThreadPool[I.event.group.id]?.stop()
            return "关闭VIP管理服务".toPlainText()
        }
    }

    //数据迭代器
    fun serverInfoIterator(action: (groupId: Long, groupData: DataForGroup, serverCount: Int, serverInfoForSave: DataForGroup.ServerInfoForSave) -> Unit) {
        Setting.groupData.forEach { (groupID, data) ->
            data.server.forEachIndexed { index, serverInfoForSave ->
                action(groupID, data, index + 1, serverInfoForSave)
            }
        }
    }
}