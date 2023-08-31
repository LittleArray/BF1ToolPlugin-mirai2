package top.ffshaozi.utils

import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import top.ffshaozi.BF1ToolPlugin
import top.ffshaozi.config.Setting
import top.ffshaozi.data.ServerListJson
import top.ffshaozi.utils.BF1Api.getPersonaid
import top.ffshaozi.utils.BF1Api.getStats
import top.ffshaozi.utils.BF1Api.kickPlayer
import top.ffshaozi.utils.BF1Api.recentlySearch
import top.ffshaozi.utils.BF1Api.seaechBFEAC
import top.ffshaozi.utils.Intent.sendMsg

/**
 * @Description
 * @Author littleArray
 * @Date 2023/8/25
 */
object CycleTask {
    var isEnableVipC: HashMap<Long, Thread> = hashMapOf()
    var isEnableSC_List: HashMap<Long, Thread> = hashMapOf()
    var isEnableSC_EAC: HashMap<Long, Thread> = hashMapOf()
    var isEnableSC_Player: HashMap<Long, Thread> = hashMapOf()
    var isEnableSC_Player_Data: HashMap<Long, Thread> = hashMapOf()
    var isEnableSC_Rkdkp: HashMap<Long, Thread> = hashMapOf()
    var isEnableSC:HashMap<Long, Boolean> = hashMapOf()
    val serverInfo:HashMap<String, ServerListJson> = hashMapOf()

    fun serverPlayerListRefresh(I: PullIntent): Message {
        serverEACR(I)
        serverPlayerR(I)
        serverRKDR(I)
        serverPlayerDataR(I)
        serverListR(I)
        return if(isEnableSC[I.event.group.id] == null || isEnableSC[I.event.group.id] == false) {
            isEnableSC[I.event.group.id] = true
            "启用玩家管理服务".toPlainText()
        }else{
            "关闭玩家管理服务".toPlainText()
        }
    }
    //Thread - 服务器列表更新线程
    fun serverListR(I: PullIntent){
        if (isEnableSC_List[I.event.group.id]?.isAlive == null || isEnableSC_List[I.event.group.id]?.isAlive == false) {
            isEnableSC_List[I.event.group.id] =  Thread {
                while (true) {
                    Setting.groupData.forEach { (groupID, data) ->
                        if (groupID == I.event.group.id) {
                            data.server.forEachIndexed a@{ index, serverInfoForSave ->
                                if (serverInfoForSave.gameID.isNullOrEmpty() || serverInfoForSave.sessionId.isNullOrEmpty()) return@a
                                val list = BF1Api.searchServerList(serverInfoForSave.gameID!!)
                                if (list.isSuccessful == true)
                                    serverInfo[serverInfoForSave.gameID!!] = list
                            }
                        }
                    }
                    BF1ToolPlugin.Glogger.info("玩家管理服务心跳-服务器列表更新")
                    Thread.sleep(30 * 1000)
                }
            }
            isEnableSC_List[I.event.group.id]?.start()
        }else{
            isEnableSC_List[I.event.group.id]?.stop()
        }
    }
    //Thread - BFEAC检测
    fun serverEACR(I: PullIntent){
        if (isEnableSC_EAC[I.event.group.id]?.isAlive == null || isEnableSC_EAC[I.event.group.id]?.isAlive == false) {
            isEnableSC_EAC[I.event.group.id] =  Thread {
                while (true) {
                    var temp:MutableSet<String> = mutableSetOf()
                    Setting.groupData.forEach { (groupID, data) ->
                        if (groupID == I.event.group.id) {
                            data.server.forEachIndexed a@{ index, serverInfoForSave ->
                                if (serverInfo[serverInfoForSave.gameID]?.teams?.size != null && serverInfo[serverInfoForSave.gameID]?.teams?.size!! > 0) {
                                    serverInfo[serverInfoForSave.gameID]?.teams?.forEach {
                                        val c:MutableSet<String> = mutableSetOf()
                                        it.players.forEach b@{ player ->
                                            var isCache = false
                                            temp.forEach {
                                                if (player.name == it) isCache = true
                                            }
                                            if (isCache) return@b
                                            val searchBFEAC = seaechBFEAC(player.name)
                                            if (searchBFEAC.error_code != 0) {
                                                c.add(player.name)
                                                return@b
                                            }
                                            if (searchBFEAC.data.isNullOrEmpty()) return@b
                                            if (searchBFEAC.data[0].current_status != 1) return@b
                                            val id = searchBFEAC.data[0].current_name
                                            val pid = searchBFEAC.data[0].personaId
                                            sendMsg(I, "BFEAC石锤挂钩${id}进入服务器${index + 1},尝试踢出")
                                            BF1ToolPlugin.Glogger.error("尝试踢出挂钩$id")
                                            val kickPlayer = kickPlayer(
                                                serverInfoForSave.sessionId!!,
                                                serverInfoForSave.gameID!!,
                                                pid.toString(),
                                                "BFEAC BAN"
                                            )
                                            if (!kickPlayer.isSuccessful)
                                                sendMsg(
                                                    I,
                                                    "BFEAC石锤挂钩${id}进入服务器${index + 1},踢出失败\n${kickPlayer.reqBody}"
                                                )
                                        }
                                        temp = c
                                    }
                                }
                            }
                        }
                    }
                    BF1ToolPlugin.Glogger.info("玩家管理服务心跳-BFEAC检测")
                    Thread.sleep(30 * 1000)
                }
            }
            isEnableSC_EAC[I.event.group.id]?.start()
        }else{
            isEnableSC_EAC[I.event.group.id]?.stop()
        }
    }
    //Thread - 玩家数量检测
    fun serverPlayerR(I: PullIntent){
        if (isEnableSC_Player[I.event.group.id]?.isAlive == null || isEnableSC_Player[I.event.group.id]?.isAlive == false) {
            isEnableSC_Player[I.event.group.id] =  Thread {
                while (true) {
                    Setting.groupData.forEach { (groupID, data) ->
                        if (groupID == I.event.group.id) {
                            data.server.forEachIndexed a@{ index, it ->
                                if (serverInfo[it.gameID]?.teams?.size != null && serverInfo[it.gameID]?.teams?.size!! > 0) {
                                    var players = 0
                                    serverInfo[it.gameID]?.teams?.forEach {
                                        it.players.forEach {
                                            players++
                                        }
                                    }
                                    if (it.players != players) {
                                        sendMsg(
                                            I,
                                            "服务器${index + 1}发生人数变化\n${it.players} --> $players"
                                        )
                                        it.players = players
                                    }
                                }
                            }
                        }
                    }
                    BF1ToolPlugin.Glogger.info("玩家管理服务心跳-数量检测")
                    Thread.sleep(3 * 60 * 1000)
                }
            }
            isEnableSC_Player[I.event.group.id]?.start()
        }else{
            isEnableSC_Player[I.event.group.id]?.stop()
        }
    }
    //Thread - 最近kd/kpm检测
    fun serverRKDR(I: PullIntent){
        if (isEnableSC_Rkdkp[I.event.group.id]?.isAlive == null || isEnableSC_Rkdkp[I.event.group.id]?.isAlive == false) {
            isEnableSC_Rkdkp[I.event.group.id] =  Thread {
                while (true) {
                    Setting.groupData.forEach { (groupID, data) ->
                        if (groupID == I.event.group.id) {
                            data.server.forEachIndexed a@{ index, serverInfoForSave ->
                                if (serverInfo[serverInfoForSave.gameID]?.teams?.size != null && serverInfo[serverInfoForSave.gameID]?.teams?.size!! > 0) {
                                    serverInfo[serverInfoForSave.gameID]?.teams?.forEach {
                                        it.players.forEach b@{ player ->
                                            if (!serverInfoForSave.isEnableRecentlyKick) return@b
                                            var isWhite = false
                                            data.recentlyTempWhitelist.forEach {
                                                if (it == player.name) isWhite = true
                                            }
                                            if (isWhite) return@b
                                            val recentlyJsons = recentlySearch(player.name)
                                            if (recentlyJsons.isEmpty()) return@b
                                            if (recentlyJsons[0].isSuccessful) {
                                                var kd = recentlyJsons[0].kd.toFloat()
                                                var kpm = recentlyJsons[0].kpm.toFloat()
                                                if (kd == 0.0f || kpm == 0.0f) {
                                                    if (recentlyJsons[1].isSuccessful && recentlyJsons.size > 1) {
                                                        kd = recentlyJsons[1].kd.toFloat()
                                                        kpm = recentlyJsons[1].kpm.toFloat()
                                                    }
                                                }
                                                if (kd > serverInfoForSave.recentlyMaxKD || kpm > serverInfoForSave.recentlyMaxKPM) {
                                                    sendMsg(
                                                        I,
                                                        "${player.name}这个老逼登想在${index + 1}服偷吃薯条,尝试踢出\n最近KD:$kd 最近KPM:$kpm"
                                                    )
                                                    val pid = getPersonaid(player.name)
                                                    val kickPlayer = kickPlayer(
                                                        serverInfoForSave.sessionId!!,
                                                        serverInfoForSave.gameID!!,
                                                        pid.id.toString(),
                                                        "Recently KD/KPM Limited ${serverInfoForSave.recentlyMaxKD}/${serverInfoForSave.recentlyMaxKPM}"
                                                    )
                                                    if (!kickPlayer.isSuccessful)
                                                        sendMsg(
                                                            I,
                                                            "${player.name}还在${index + 1}服偷吃薯条,踢出失败\n${kickPlayer.reqBody}"
                                                        )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    BF1ToolPlugin.Glogger.info("玩家管理服务心跳-最近kd/kpm检测")
                    Thread.sleep(60 * 1000)
                }
            }
            isEnableSC_Rkdkp[I.event.group.id]?.start()
        }else{
            isEnableSC_Rkdkp[I.event.group.id]?.stop()
        }
    }
    //Thread - 玩家数据检测
    fun serverPlayerDataR(I: PullIntent){
        if (isEnableSC_Player_Data[I.event.group.id]?.isAlive == null || isEnableSC_Player_Data[I.event.group.id]?.isAlive == false) {
            isEnableSC_Player_Data[I.event.group.id] =  Thread {
                while (true) {
                    var temp:MutableSet<String> = mutableSetOf()
                    Setting.groupData.forEach { (groupID, data) ->
                        if (groupID == I.event.group.id) {
                            data.server.forEachIndexed a@{ index, serverInfoForSave ->
                                if (serverInfo[serverInfoForSave.gameID]?.teams?.size != null && serverInfo[serverInfoForSave.gameID]?.teams?.size!! > 0) {
                                    serverInfo[serverInfoForSave.gameID]?.teams?.forEach {
                                        val c:MutableSet<String> = mutableSetOf()
                                        it.players.forEach b@{ player ->
                                            var isWhite = false
                                            var isCache = false
                                            data.recentlyTempWhitelist.forEach {
                                                if (it == player.name) isWhite = true
                                            }
                                            temp.forEach {
                                                if (it == player.name) isCache = true
                                            }
                                            if (isWhite) return@b
                                            if (isCache) return@b
                                            c.add(player.name)
                                            val stats = getStats(player.name)
                                            if (
                                                stats.killDeath > serverInfoForSave.lifeMaxKD ||
                                                stats.killsPerMinute > serverInfoForSave.lifeMaxKPM
                                            ) {
                                                sendMsg(
                                                    I,
                                                    "${player.name}尝试在${index + 1}服偷吃薯条,尝试踢出\nLKD:${stats.killDeath} LKPM:${stats.killsPerMinute}"
                                                )
                                                val pid = getPersonaid(player.name)
                                                val kickPlayer = kickPlayer(
                                                    serverInfoForSave.sessionId!!,
                                                    serverInfoForSave.gameID!!,
                                                    pid.id.toString(),
                                                    "Life KD/KPM Limited ${serverInfoForSave.lifeMaxKD}/${serverInfoForSave.lifeMaxKPM}"
                                                )
                                                if (!kickPlayer.isSuccessful)
                                                    sendMsg(
                                                        I,
                                                        "${player.name}尝试在${index + 1}服偷吃薯条,踢出失败\n${kickPlayer.reqBody}"
                                                    )
                                            }
                                        }
                                        temp = c
                                    }
                                }
                            }
                        }
                    }
                    BF1ToolPlugin.Glogger.info("玩家管理服务心跳-玩家数据检测")
                    Thread.sleep(60 * 1000)
                }
            }
            isEnableSC_Player_Data[I.event.group.id]?.start()
        }else{
            isEnableSC_Player_Data[I.event.group.id]?.stop()
        }
    }
    fun vipRefresh(I: PullIntent): Message {
        if (isEnableVipC[I.event.group.id]?.isAlive == false || isEnableVipC[I.event.group.id]?.isAlive == null) {
            isEnableVipC[I.event.group.id] = Thread {
                while (true) {
                    //移除VIP
                    Setting.groupData.forEach { groupID, Data ->
                        if (groupID == I.event.group.id) {
                            Data.server.forEach {
                                var removeID = ""
                                it.vipList.forEach { id, endTime ->
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
                    BF1ToolPlugin.Glogger.info("VIP管理服务心跳")
                    Thread.sleep(10000)
                }
            }
            isEnableVipC[I.event.group.id]?.start()
            return "启用VIP管理服务".toPlainText()
        } else {
            isEnableVipC[I.event.group.id]?.stop()
            return "关闭VIP管理服务".toPlainText()
        }
        //周期性任务
    }
}