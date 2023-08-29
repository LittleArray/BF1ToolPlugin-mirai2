package top.ffshaozi.utils

import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import top.ffshaozi.BF1ToolPlugin
import top.ffshaozi.config.Setting
import top.ffshaozi.utils.BF1Api.kickPlayer
import top.ffshaozi.utils.BF1Api.seaechBFEAC
import top.ffshaozi.utils.Intent.sendMsg

/**
 * @Description
 * @Author littleArray
 * @Date 2023/8/25
 */
object CycleTask {
    var isEnableVipC: HashMap<Long, Boolean> = hashMapOf()
    var isEnableSC: HashMap<Long, Boolean> = hashMapOf()
    val players: HashMap<String, Int> = hashMapOf()

    init {
        Setting.groupData.forEach { (groupID, Data) ->
            isEnableVipC[groupID] = false
            isEnableSC[groupID] = false
            Data.server.forEach {
                if (!it.gameID.isNullOrEmpty())
                    players[it.gameID!!] = 0
            }
        }
    }

    fun serverPlayerListRefresh(I: PullIntent): Message {
        if (isEnableSC[I.event.group.id] == false) {
            isEnableSC[I.event.group.id] = true
            Thread {
                while (true) {
                    Setting.groupData.forEach { (groupID, data) ->
                        if (groupID == I.event.group.id) {
                            data.server.forEachIndexed a@{ index, it ->
                                if (it.gameID.isNullOrEmpty() || it.sessionId.isNullOrEmpty()) return@a
                                val serverListJson = BF1Api.searchServerList(it.gameID!!, false)
                                if (serverListJson.isSuccessful == false) return@a
                                val eacBanList = hashMapOf<String, Long>()
                                var player = 0
                                serverListJson.teams?.forEach {
                                    it.players.forEach {
                                        val searchBFEAC = seaechBFEAC(it.name)
                                        if (searchBFEAC.error_code == 0) {
                                            if (!searchBFEAC.data.isNullOrEmpty()) {
                                                if (searchBFEAC.data[0].current_status == 1) {
                                                    eacBanList[searchBFEAC.data[0].current_name] = searchBFEAC.data[0].personaId
                                                }
                                            }
                                        }
                                        player++
                                    }
                                }
                                players[it.gameID!!] = player
                                eacBanList.forEach { (id, pid) ->
                                    sendMsg(I, "BFEAC石锤挂钩${id}进入服务器${index + 1},尝试踢出")
                                    BF1ToolPlugin.Glogger.error("尝试踢出挂钩$id")
                                    kickPlayer(it.sessionId!!, it.gameID!!, pid.toString(), "BFEAC BAN")
                                }
                            }
                        }
                    }
                    BF1ToolPlugin.Glogger.info("玩家管理服务心跳-主服务")
                    Thread.sleep(60 * 1000)
                }
            }.start()
            Thread {
                while (true) {
                    Setting.groupData.forEach { (groupID, data) ->
                        if (groupID == I.event.group.id) {
                            data.server.forEachIndexed a@{ index, it ->
                                if (it.players != players[it.gameID]) {
                                    sendMsg(
                                        I,
                                        "服务器${index + 1}发生人数变化\n${it.players} --> ${players[it.gameID]}"
                                    )
                                }
                                it.players = players[it.gameID] ?: 0
                            }
                        }
                    }
                    BF1ToolPlugin.Glogger.info("玩家管理服务心跳-数量检测")
                    Thread.sleep(3 * 60 * 1000)
                }
            }.start()
            return "启用玩家管理服务".toPlainText()
        } else {
            return "服务在运行".toPlainText()
        }
    }

    fun vipRefresh(I: PullIntent): Message {
        if (isEnableVipC[I.event.group.id] == false) {
            isEnableVipC[I.event.group.id] = true
            Thread {
                while (true) {
                    //BF1ToolPlugin.Glogger.info("VIP管理周期事件 响应")
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
            }.start()
            return "启用VIP管理服务".toPlainText()
        } else {
            return "服务在运行".toPlainText()
        }
        //周期性任务
    }
}