package top.ffshaozi.intent

import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import top.ffshaozi.config.CustomerLang
import top.ffshaozi.config.DataForGroup
import top.ffshaozi.config.Setting
import top.ffshaozi.config.SettingController
import top.ffshaozi.data.FullServerInfoJson
import top.ffshaozi.utils.BF1Api
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

/*** 服务器管理实现
 * @Description
 * @Author littleArray
 * @Date 2023/9/4
 */
object ServerManagement {

    //TODO 踢人实现
    fun kickPlayer(I: PullIntent, re: Boolean = false): Message {
        if (!I.isAdmin) return CustomerLang.notAdminErr.toPlainText()
        if (I.cmdSize < 3) return CustomerLang.parameterErr.replace("//para//", "*k <ID> <Reason>").toPlainText()
        if (SettingController.isNullServer(I.event.group.id)) return "不存在绑定的服务器".toPlainText()
        if (re) SettingController.refreshServerInfo(I.event.group.id)
        val pid = BF1Api.getPersonaid(I.sp[1])
        var res = "\n"
        Setting.groupData[I.event.group.id]?.server?.forEachIndexed { index, it ->
            if (it.gameID.isNullOrEmpty() || it.sessionId.isNullOrEmpty()) {
                if (re) CustomerLang.serverInfoRErr.toPlainText()
                kickPlayer(I, true)
                return CustomerLang.serverInfoRefreshing.toPlainText()
            }
            val kickR = when (I.sp[2]) {
                "*tj" -> "禁止偷家"
                "*zz" -> "禁止蜘蛛人"
                "*ur" -> "違反規則"
                "*nf" -> "nuan 服战神是吧"
                else -> I.sp[2]
            }
            val kickPlayer = BF1Api.kickPlayer(it.sessionId!!, it.gameID!!, pid.id.toString(), kickR)
            res += if (kickPlayer.isSuccessful) {
                CustomerLang.kickSucc.replace("//id//", I.sp[1]).replace("//serverCount//", "${index + 1}").replace("//res//", kickR)+"\n"
            } else {
                CustomerLang.kickErr.replace("//id//", I.sp[1]).replace("//err//", kickPlayer.reqBody)+"\n"
            }
        }
        return res.toPlainText()
    }

    //TODO ban人实现
    fun banPlayer(I: PullIntent, re: Boolean = false): Message {
        if (!I.isAdmin) return CustomerLang.notAdminErr.toPlainText()
        if (I.cmdSize < 3) return CustomerLang.parameterErr.replace("//para//", "*b <ServerCount> <ID>").toPlainText()
        if (SettingController.isNullServer(I.event.group.id)) return CustomerLang.nullServerErr.replace("//err//", "")
            .toPlainText()
        if (re) SettingController.refreshServerInfo(I.event.group.id)
        val serverCount = I.sp[1].toInt()
        Setting.groupData[I.event.group.id]?.server?.forEachIndexed { index, it ->
            if (index + 1 == serverCount) {
                if (it.serverRspID == 0 || it.sessionId.isNullOrEmpty()) {
                    if (re) CustomerLang.serverInfoRErr.toPlainText()
                    banPlayer(I, true)
                    return CustomerLang.serverInfoRefreshing.toPlainText()
                }
                val serverBan = BF1Api.addServerBan(it.sessionId!!, it.serverRspID, I.sp[2])
                return if (serverBan.isSuccessful) {
                    CustomerLang.banSucc.replace("//id//", I.sp[2]).replace("//serverCount//", "$serverCount")
                        .toPlainText()
                } else {
                    SettingController.refreshServerInfo(I.event.group.id)
                    CustomerLang.banErr.replace("//id//", I.sp[2]).replace("//err//", serverBan.reqBody).toPlainText()
                }
            }
        }
        return CustomerLang.nullServerErr.replace("//err//", "第${serverCount}个").toPlainText()
    }

    //TODO unban人实现
    fun unBanPlayer(I: PullIntent, re: Boolean = false): Message {
        if (!I.isAdmin) return CustomerLang.notAdminErr.toPlainText()
        if (I.cmdSize < 3) return CustomerLang.parameterErr.replace("//para//", "*rb <ServerCount> <ID>").toPlainText()
        if (SettingController.isNullServer(I.event.group.id)) return CustomerLang.nullServerErr.toPlainText()
        if (re) SettingController.refreshServerInfo(I.event.group.id)
        val serverCount = I.sp[1].toInt()
        val pid = BF1Api.getPersonaid(I.sp[2])
        Setting.groupData[I.event.group.id]?.server?.forEachIndexed { index, it ->
            if (index + 1 == serverCount) {
                if (it.serverRspID == 0 || it.sessionId.isNullOrEmpty()) {
                    if (re) CustomerLang.serverInfoRErr.toPlainText()
                    unBanPlayer(I, true)
                    return CustomerLang.serverInfoRefreshing.toPlainText()
                }
                val serverBan = BF1Api.removeServerBan(it.sessionId!!, it.serverRspID, pid.id.toString())
                return if (serverBan.isSuccessful) {
                    CustomerLang.unBanSucc.replace("//serverCount//", "$serverCount").replace("//id//", I.sp[2])
                        .toPlainText()
                } else {
                    SettingController.refreshServerInfo(I.event.group.id)
                    CustomerLang.unBanErr.replace("//id//", I.sp[2]).replace("//err//", serverBan.reqBody).toPlainText()
                }
            }
        }
        return CustomerLang.nullServerErr.replace("//err//", "第${serverCount}个").toPlainText()
    }

    //TODO 查询服务器Ban实现
    fun getBanList(I: PullIntent, re: Boolean = false): Message {
        if (I.cmdSize < 2) return CustomerLang.parameterErr.replace("//para//", "*gb <ServerCount>").toPlainText()
        if (SettingController.isNullServer(I.event.group.id)) return CustomerLang.nullServerErr.toPlainText()
        if (re) SettingController.refreshServerInfo(I.event.group.id)
        val serverCount = I.sp[1].toInt()
        Setting.groupData[I.event.group.id]?.server?.forEachIndexed { index, it ->
            if (index + 1 == serverCount) {
                if (it.gameID.isNullOrEmpty()) {
                    if (re) return CustomerLang.serverInfoRErr.toPlainText()
                    getBanList(I, re = true)
                    return CustomerLang.serverInfoRefreshing.toPlainText()
                }
                val it: FullServerInfoJson = BF1Api.getFullServerDetails(it.sessionId.toString(), it.gameID.toString())
                return if (it.isSuccessful == true) {
                    var banStr = ""
                    it.result?.rspInfo?.bannedList?.sortedBy { bannedList -> bannedList.displayName }?.forEach {
                        banStr += "ID:${it.displayName}\n"
                    }
                    "服务器${serverCount}的Ban:\n$banStr".toPlainText()
                } else {
                    SettingController.refreshServerInfo(I.event.group.id)
                    CustomerLang.searchErr.replace("//action//", "封禁列表").toPlainText()
                }
            }
        }
        return CustomerLang.nullServerErr.replace("//err//", "第${serverCount}个").toPlainText()
    }

    //TODO 添加VIP实现
    fun addVipPlayer(I: PullIntent, re: Boolean = false): Message {
        if (!I.isAdmin) return CustomerLang.notAdminErr.toPlainText()
        if (I.cmdSize < 2) return CustomerLang.parameterErr.replace("//para//", "*av <ServerCount> <ID> <Time>")
            .toPlainText()
        if (SettingController.isNullServer(I.event.group.id)) return CustomerLang.nullServerErr.toPlainText()
        if (re) SettingController.refreshServerInfo(I.event.group.id)
        val serverCount = I.sp[1].toInt()
        Setting.groupData[I.event.group.id]?.server?.forEachIndexed { index, it ->
            if (index + 1 == serverCount) {
                if (it.gameID.isNullOrEmpty() || it.serverRspID == 0) {
                    if (re) return CustomerLang.serverInfoRErr.toPlainText()
                    addVipPlayer(I, re = true)
                    return CustomerLang.serverInfoRefreshing.toPlainText()
                }
                val serverVIP = BF1Api.addServerVIP(it.sessionId.toString(), it.serverRspID, I.sp[2])
                if (serverVIP.isSuccessful || serverVIP.reqBody.indexOf("RspErrUserIsAlreadyVip") != -1) {
                    if (I.cmdSize > 2) {
                        SettingController.addVip(I.event.group.id, serverCount, I.sp[2], I.sp[3])
                        return CustomerLang.addVIPSucc
                            .replace("//id//", I.sp[2])
                            .replace("//serverCount//", "$serverCount")
                            .replace("//Time//", "${I.sp[3]}天的")
                            .toPlainText()
                    }
                    return CustomerLang.addVIPSucc
                        .replace("//id//", I.sp[2])
                        .replace("//serverCount//", "$serverCount")
                        .replace("//Time//", "")
                        .toPlainText()
                } else {
                    SettingController.refreshServerInfo(I.event.group.id)
                    return CustomerLang.addVIPErr.replace("//id//", I.sp[2]).replace("//err//", serverVIP.reqBody)
                        .toPlainText()
                }
            }
        }
        return CustomerLang.nullServerErr.replace("//err//", "第${serverCount}个").toPlainText()
    }

    //TODO 移除VIP人实现
    fun unVipPlayer(I: PullIntent, re: Boolean = false): Message {
        if (!I.isAdmin) return CustomerLang.notAdminErr.toPlainText()
        if (I.cmdSize < 2) return CustomerLang.parameterErr.replace("//para//", "*rv <ServerCount> <ID>").toPlainText()
        if (SettingController.isNullServer(I.event.group.id)) return CustomerLang.nullServerErr.toPlainText()
        if (re) SettingController.refreshServerInfo(I.event.group.id)
        val serverCount = I.sp[1].toInt()
        Setting.groupData[I.event.group.id]?.server?.forEachIndexed { index, it ->
            if (index + 1 == serverCount) {
                if (it.gameID.isNullOrEmpty() || it.serverRspID == 0) {
                    if (re) return CustomerLang.serverInfoRErr.toPlainText()
                    unVipPlayer(I, re = true)
                    return CustomerLang.serverInfoRefreshing.toPlainText()
                }
                val pid = BF1Api.getPersonaid(I.sp[2])
                if (!pid.isSuccessful) {
                    if (re) return CustomerLang.serverInfoRErr.toPlainText()
                    unVipPlayer(I, true)
                    return CustomerLang.serverInfoRefreshing.toPlainText()
                }
                val serverVIP = BF1Api.removeServerVIP(it.sessionId.toString(), it.serverRspID, pid.id.toString())
                if (serverVIP.isSuccessful) {
                    SettingController.removeVip(I.event.group.id, serverCount, I.sp[2])
                    return CustomerLang.unVIPSucc.replace("//id//", I.sp[2]).replace("//serverCount//", "$serverCount")
                        .toPlainText()
                } else {
                    if (re) return CustomerLang.serverInfoRErr.toPlainText()
                    unVipPlayer(I, true)
                    return CustomerLang.unVIPErr.replace("//id//", I.sp[2]).replace("//err//", serverVIP.reqBody)
                        .toPlainText()
                }
            }
        }
        return CustomerLang.nullServerErr.replace("//err//", "第${serverCount}个").toPlainText()
    }

    //TODO 切图实现
    fun chooseLevel(I: PullIntent, re: Boolean = false): Message {
        if (!I.isAdmin) return CustomerLang.notAdminErr.toPlainText()
        if (I.cmdSize < 2) return CustomerLang.parameterErr.replace("//para//", "*qt <ServerCount> <level>")
            .toPlainText()
        if (SettingController.isNullServer(I.event.group.id)) return CustomerLang.nullServerErr.toPlainText()
        if (re) SettingController.refreshServerInfo(I.event.group.id)
        val serverCount = I.sp[1].toInt()
        Setting.groupData[I.event.group.id]?.server?.forEachIndexed { index, it ->
            if (index + 1 == serverCount) {
                if (it.gameID.isNullOrEmpty() || it.sessionId.isNullOrEmpty()) {
                    if (re) return CustomerLang.serverInfoRErr.toPlainText()
                    chooseLevel(I, re = true)
                    return CustomerLang.serverInfoRefreshing.toPlainText()
                }
                val serverInfoJson = BF1Api.getFullServerDetails(it.sessionId!!, it.gameID!!)
                if (serverInfoJson.isSuccessful == false) {
                    if (re) return CustomerLang.serverInfoRErr.toPlainText()
                    chooseLevel(I, re = true)
                    return CustomerLang.serverInfoRefreshing.toPlainText()
                }
                if (I.cmdSize < 3) {
                    var temp = "服务器${serverCount}的地图池如下:\n"
                    serverInfoJson.result?.serverInfo?.rotation?.forEachIndexed { index, it ->
                        temp += """
                            地图序号:${index + 1}
                            --地图名:${it.mapPrettyName}
                            --模式名:${it.modePrettyName}
                        """.trimIndent() + "\n"
                    }
                    return temp.toPlainText()
                } else {
                    if (serverInfoJson.result?.rspInfo?.server?.persistedGameId != null) {
                        val response = BF1Api.chooseServerVIP(
                            it.sessionId!!,
                            serverInfoJson.result.rspInfo.server.persistedGameId,
                            (I.sp[2].toInt() - 1).toString()
                        )
                        return if (response.isSuccessful)
                            "切图成功,地图名:${serverInfoJson.result.serverInfo.rotation[I.sp[2].toInt() - 1].mapPrettyName}".toPlainText()
                        else
                            "切图失败\n:${response.reqBody}".toPlainText()
                    } else {
                        return "切图失败,PGID为空".toPlainText()
                    }
                }
            }
        }
        return CustomerLang.nullServerErr.replace("//err//", "第${serverCount}个").toPlainText()
    }
    //TODO 换边实现
    fun movePlayer(I: PullIntent, re: Boolean = false): Message {
        if (!I.isAdmin) return CustomerLang.notAdminErr.toPlainText()
        if (I.cmdSize < 3) return CustomerLang.parameterErr.replace("//para//", "*hb <ServerCount> <ID>")
            .toPlainText()
        if (SettingController.isNullServer(I.event.group.id)) return CustomerLang.nullServerErr.toPlainText()
        if (re) SettingController.refreshServerInfo(I.event.group.id)
        val serverCount = I.sp[1].toInt()
        Setting.groupData[I.event.group.id]?.server?.forEachIndexed { index, it ->
            if (index + 1 == serverCount) {
                if (it.gameID.isNullOrEmpty() || it.sessionId.isNullOrEmpty()) {
                    if (re) return CustomerLang.serverInfoRErr.toPlainText()
                    chooseLevel(I, re = true)
                    return CustomerLang.serverInfoRefreshing.toPlainText()
                }
                val player:HashMap<String,Int> = hashMapOf()
                Cache.PlayerListInfo[it.gameID!!]?.forEach{id,pldata->
                    if (id.indexOf(I.sp[2],0,false) != -1)player[id] = pldata.teamId
                }
                if (player.size > 1){
                    var temp = "找到多个ID,无法确认\n"
                    player.forEach { id, team ->
                        temp += "ID:${id} 在 队伍${team}\n"
                    }
                    return temp.toPlainText()
                }else if (player.size == 1){
                    var result = "换边失败"
                    player.forEach { (id, team) ->
                        val pid = BF1Api.getPersonaid(id)
                        if (pid.isSuccessful) {
                            val movePlayer = BF1Api.movePlayer(it.sessionId!!, it.gameID!!, pid.id, if (team == 1) 2 else 1)
                            if (movePlayer.isSuccessful){
                                result = "换边成功 $id"
                            }
                        }
                    }
                    return result.toPlainText()
                }else {
                    return "找不到此玩家".toPlainText()
                }
            }
        }
        return CustomerLang.nullServerErr.replace("//err//", "第${serverCount}个").toPlainText()
    }

    //TODO 查询VIP实现
    fun getVipList(I: PullIntent): Message {
        /*var serverName = ""
        ServerNameData.forEach { (t, u) ->
            if (t == I.event.group.id) {
                serverName = u
            }
        }
        if (serverName.isEmpty()) return "未定义默认服务器名称".toPlainText()
        val serverID = BF1Api.searchServer(serverName)
        if (!serverID.isSuccessful) return "查找服务器失败".toPlainText()
        val exc: HashMap<String, String> = getGameIDList(I.event.group.id, serverID)
        val fullServerInfoJson: MutableList<FullServerInfoJson> = mutableListOf()
        exc.forEach { (t, u) ->
            fullServerInfoJson.add(getFullServerDetails(u, t))
        }*/
        if (I.cmdSize < 2) return CustomerLang.parameterErr.replace("//para//", "*gv <ServerCount>").toPlainText()
        val serverCount = I.sp[1].toInt()
        val serverSize = Setting.groupData[I.event.group.id]?.server?.size
        var p = "服务器${serverCount}的临时VIP:\n"
        if (serverSize != null) {
            if (serverSize >= serverCount) {
                Setting.groupData[I.event.group.id]!!.server.forEachIndexed { index, it ->
                    if (index + 1 == serverCount) {
                        it.vipList.forEach { id, endTime ->
                            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            val sd = sdf.format(Date(endTime.toLong()))
                            p += "ID:${id} 到期时间:${sd}\n"
                        }
                    }
                }
            }
        }
        return p.toPlainText()
    }

    //TODO 绑定服务器实现
    fun bindingServer(I: PullIntent): Message {
        if (I.isAdmin) {
            when (I.cmdSize) {
                in 1..2 -> {
                    var temp = "绑定的服务器\n"
                    Setting.groupData[I.event.group.id]?.server?.forEachIndexed { index, it ->
                        temp += """
                            Server:${index + 1}:
                            ServerName:${it.serverName}
                            ServerGuid:${it.serverGuid}
                            GameID:${it.gameID}
                        """.trimIndent() + "\n"
                    }
                    return temp.toPlainText()
                }

                in 3..5 -> {
                    when (I.sp[1]) {
                        "add" -> {
                            return if (I.sp[2].isNotEmpty() && I.sp[3].isNotEmpty()) {
                                if (Setting.groupData[I.event.group.id]?.server?.add(
                                        DataForGroup.ServerInfoForSave(
                                            serverGuid = I.sp[2],
                                            serverName = I.sp[3]
                                        )
                                    ) == true
                                ) {
                                    "成功".toPlainText()
                                } else {
                                    "已存在该绑定".toPlainText()
                                }
                            } else {
                                CustomerLang.parameterErr.replace("//para//", "*bds add <ServerID> <ServerName>")
                                    .toPlainText()
                            }
                        }

                        "remove" -> {
                            return if (I.sp[2].isNotEmpty()) {
                                Setting.groupData[I.event.group.id]?.server?.removeIf {
                                    it.serverGuid == I.sp[2]
                                }
                                "成功".toPlainText()
                            } else {
                                CustomerLang.parameterErr.replace("//para//", "*bds remove <ServerID>").toPlainText()
                            }
                        }

                        else -> return CustomerLang.parameterErr.replace("//para//", "*bds <add/remove/list>")
                            .toPlainText()
                    }
                }

                else -> {
                    return CustomerLang.errCommand.replace("//err//", "").toPlainText()
                }
            }
        } else {
            return CustomerLang.notAdminErr.toPlainText()
        }
    }

    //TODO 绑定服务器ssid
    fun bindingServerSessionId(I: PullIntentTemp): Message {
        if (I.isAdmin) {
            if (I.cmdSize > 2) {
                val apiLocale = BF1Api.setAPILocale(I.sp[2])
                if (!apiLocale.isSuccessful) return "失败请重试\n${apiLocale.reqBody}".toPlainText()
                val ssid = BF1Api.getWelcomeMessage(I.sp[2])
                if (!ssid.isSuccessful) return "失败请重试\n${apiLocale.reqBody}".toPlainText()
                return if (I.sp[1] == "All") {
                    setBindServer(I.event.group.id, "All", I.sp[2])
                    "绑定成功\n${ssid.firstMessage}".toPlainText()
                } else {
                    setBindServer(I.event.group.id, I.sp[1], I.sp[2])
                    "绑定成功\n${ssid.firstMessage}".toPlainText()
                }
            } else {
                return CustomerLang.parameterErr.replace("//para//", "*bdssid <ServerID> <SessionID>").toPlainText()
            }
        } else {
            return CustomerLang.notAdminErr.toPlainText()
        }
    }

    //TODO 修改绑定服务器
    fun setBindServer(gid: Long, serverID: String, sessionId: String = ""): Boolean {
        Setting.groupData[gid]?.server?.forEach {
            if (serverID == "All") {
                it.sessionId = sessionId
            }
            if (it.serverGuid == serverID) {
                it.sessionId = sessionId
            }
        }
        return true
    }

    //TODO 修改服务器kd实现
    fun setKDInfo(I: PullIntent): Message {
        if (!I.isAdmin) return CustomerLang.notAdminErr.toPlainText()
        if (I.cmdSize < 4) return CustomerLang.parameterErr.replace(
            "//para//",
            "*setkd <ServerCount> <lkd/lkp/rkd/rkp> <Float>"
        )
            .toPlainText()
        if (SettingController.isNullServer(I.event.group.id)) return CustomerLang.nullServerErr.replace("//err//", "")
            .toPlainText()
        val serverCount = I.sp[1].toInt()
        when (I.sp[2]) {
            "lkd" -> SettingController.setKD(I.event.group.id, serverCount, lifeMaxKD = I.sp[3].toFloat())
            "lkp" -> SettingController.setKD(I.event.group.id, serverCount, lifeMaxKPM = I.sp[3].toFloat())
            "rkp" -> SettingController.setKD(I.event.group.id, serverCount, recentlyMaxKPM = I.sp[3].toFloat())
            "rkd" -> SettingController.setKD(I.event.group.id, serverCount, recentlyMaxKD = I.sp[3].toFloat())
            else -> return CustomerLang.parameterErr.replace(
                "//para//",
                "*setkd <ServerCount> <lkd/lkp/rkd/rkp> <Float>"
            )
                .toPlainText()
        }
        return "成功".toPlainText()
    }


    //TODO 修改服务器抗压白名单实现
    fun wl(I: PullIntent): Message {
        if (!I.isAdmin) return CustomerLang.notAdminErr.toPlainText()
        if (SettingController.isNullServer(I.event.group.id)) return CustomerLang.nullServerErr.replace("//err//", "")
            .toPlainText()
        if (I.cmdSize < 2) return Setting.groupData[I.event.group.id]?.recentlyTempWhitelist.toString().toPlainText()
        return if (SettingController.addWl(I.event.group.id, I.sp[1])) {
            "添加白名单成功".toPlainText()
        } else {
            "移除白名单成功".toPlainText()
        }
    }

}