package top.ffshaozi.intent

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import top.ffshaozi.config.Bindings
import top.ffshaozi.config.CustomerLang
import top.ffshaozi.config.ServerInfos
import top.ffshaozi.config.Setting
import top.ffshaozi.data.ea.FullServerInfoJson
import top.ffshaozi.utils.BF1Api
import java.text.SimpleDateFormat
import java.util.*

/*** 服务器管理实现
 * @Description
 * @Author littleArray
 * @Date 2023/9/4
 */
object ServerManagement {

    //TODO 踢人实现
    fun kickPlayer(I: PullIntent): Message {
        if (!I.isAdmin) return CustomerLang.notAdminErr.toPlainText()
        if (I.cmdSize < 3) return CustomerLang.parameterErr.replace("//para//", "*k <ID> <Reason>").toPlainText()
        var res = "\n"
        val kickR = when (I.sp[2]) {
            "*tj" -> "禁止偷家"
            "*zz" -> "禁止蜘蛛人"
            "*ur" -> "違反規則"
            "*nf" -> "nuan 服战神是吧"
            else -> I.sp[2]
        }
        var player = 0
        Cache.PlayerListInfo.forEach { pldata ->
            if (pldata.id.indexOf(I.sp[1], 0, false) != -1) {
                player++
            }
        }
        if (player > 1) {
            var temp = "找到多个ID,无法确认\n"
            Cache.PlayerListInfo.forEach { pldata ->
                if (pldata.id.indexOf(I.sp[1], 0, false) != -1) {
                    temp += "ID:${pldata.id} 在 队伍${pldata.team}\n"
                }
            }
            res += temp
        } else if (player == 1) {
            var result = "找不到此玩家"
            Cache.PlayerListInfo.forEach { pldata ->
                if (pldata.id.indexOf(I.sp[1], 0, false) != -1) {
                    runBlocking {
                        result = if (pldata.kick(kickR).isSuccessful) {
                            "踢出成功 ${pldata.id}"
                        } else {
                            "踢出失败 ${pldata.id}"
                        }
                    }
                }
            }
            res += result+"\n"
        } else {
            res += "找不到此玩家"
        }
        return res.toPlainText()
    }

    //TODO ban人实现
    fun banPlayer(I: PullIntent): Message {
        if (!I.isAdmin) return CustomerLang.notAdminErr.toPlainText()
        if (I.cmdSize < 3) return CustomerLang.parameterErr.replace("//para//", "*b <ServerCount> <ID>").toPlainText()
        val name = I.sp[1]
        val server = ServerInfos.getServerByName(I.event.group.id,name) ?: return CustomerLang.nullServerErr.replace("//err//", name).toPlainText()
        val serverBan = BF1Api.addServerBan(server.sessionId!!, server.serverRspID, I.sp[2])
        return if (serverBan.isSuccessful) {
            CustomerLang.banSucc.replace("//id//", I.sp[2]).replace("//serverCount//", name).toPlainText()
        } else {
            CustomerLang.banErr.replace("//id//", I.sp[2]).replace("//err//", serverBan.reqBody).toPlainText()
        }
    }

    //TODO unban人实现
    fun unBanPlayer(I: PullIntent): Message {
        if (!I.isAdmin) return CustomerLang.notAdminErr.toPlainText()
        if (I.cmdSize < 3) return CustomerLang.parameterErr.replace("//para//", "*rb <ServerCount> <ID>").toPlainText()
        val name = I.sp[1]
        val server = ServerInfos.getServerByName(I.event.group.id,name) ?: return CustomerLang.nullServerErr.replace("//err//", name).toPlainText()
        val pid = BF1Api.getPersonaid(I.sp[2])
        val serverBan = BF1Api.removeServerBan(server.sessionId!!, server.serverRspID, pid.id.toString())
        return if (serverBan.isSuccessful) {
            CustomerLang.unBanSucc.replace("//serverCount//", name).replace("//id//", I.sp[2])
                .toPlainText()
        } else {
            CustomerLang.unBanErr.replace("//id//", I.sp[2]).replace("//err//", serverBan.reqBody).toPlainText()
        }
    }

    //TODO 查询服务器Ban实现
    fun getBanList(I: PullIntent, re: Boolean = false): Message {
        if (I.cmdSize < 2) return CustomerLang.parameterErr.replace("//para//", "*gb <ServerCount>").toPlainText()
        val name = I.sp[1]
        val server = ServerInfos.getServerByName(I.event.group.id,name) ?: return CustomerLang.nullServerErr.replace("//err//", name).toPlainText()
        val it: FullServerInfoJson = BF1Api.getFullServerDetails(server.sessionId.toString(), server.gameID.toString())
        return if (it.isSuccessful == true) {
            var banStr = ""
            it.result?.rspInfo?.bannedList?.sortedBy { bannedList -> bannedList.displayName }?.forEach {
                banStr += "ID:${it.displayName}\n"
            }
            "服务器${name}的Ban:\n$banStr".toPlainText()
        } else {
            CustomerLang.searchErr.replace("//action//", "封禁列表").toPlainText()
        }
    }

    //TODO 添加VIP实现
    fun addVipPlayer(I: PullIntent): Message {
        if (!I.isAdmin) return CustomerLang.notAdminErr.toPlainText()
        if (I.cmdSize < 2) return CustomerLang.parameterErr.replace("//para//", "*av <ServerCount> <ID> <Time>").toPlainText()
        val name = I.sp[1]
        val server = ServerInfos.getServerByName(I.event.group.id,name) ?: return CustomerLang.nullServerErr.replace("//err//", name).toPlainText()
        val serverVIP = BF1Api.addServerVIP(server.sessionId.toString(), server.serverRspID, I.sp[2])
        if (serverVIP.isSuccessful || serverVIP.reqBody.indexOf("RspErrUserIsAlreadyVip") != -1) {
            /*if (I.cmdSize > 2) {
                Setting.addVip(I.event.group.id, serverCount, I.sp[2], I.sp[3])
                return CustomerLang.addVIPSucc
                    .replace("//id//", I.sp[2])
                    .replace("//serverCount//", "$serverCount")
                    .replace("//Time//", "${I.sp[3]}天的")
                    .toPlainText()
            }*/
            return CustomerLang.addVIPSucc
                .replace("//id//", I.sp[2])
                .replace("//serverCount//", name)
                .replace("//Time//", "")
                .toPlainText()
        } else {
            return CustomerLang.addVIPErr.replace("//id//", I.sp[2]).replace("//err//", serverVIP.reqBody)
                .toPlainText()
        }
    }

    //TODO 移除VIP人实现
    fun unVipPlayer(I: PullIntent): Message {
        if (!I.isAdmin) return CustomerLang.notAdminErr.toPlainText()
        if (I.cmdSize < 2) return CustomerLang.parameterErr.replace("//para//", "*rv <ServerCount> <ID>").toPlainText()
        val name = I.sp[1]
        val server = ServerInfos.getServerByName(I.event.group.id,name) ?: return CustomerLang.nullServerErr.replace("//err//", name).toPlainText()
        val pid = BF1Api.getPersonaid(I.sp[2])
        val serverVIP = BF1Api.removeServerVIP(server.sessionId.toString(), server.serverRspID, pid.id.toString())
        return if (serverVIP.isSuccessful) {
            CustomerLang.unVIPSucc.replace("//id//", I.sp[2]).replace("//serverCount//", name).toPlainText()
        } else {
            CustomerLang.unVIPErr.replace("//id//", I.sp[2]).replace("//err//", serverVIP.reqBody).toPlainText()
        }
    }

    //TODO 切图实现
    fun chooseLevel(I: PullIntent, re: Boolean = false): Message {
        if (!I.isAdmin) return CustomerLang.notAdminErr.toPlainText()
        if (I.cmdSize < 2) return CustomerLang.parameterErr.replace("//para//", "*qt <ServerCount> <level>").toPlainText()
        val name = I.sp[1]
        val server = ServerInfos.getServerByName(I.event.group.id,name) ?: return CustomerLang.nullServerErr.replace("//err//", name).toPlainText()
        val serverInfoJson = BF1Api.getFullServerDetails(server.sessionId!!, server.gameID!!)
        if (I.cmdSize < 3) {
            var temp = "服务器 $name 的地图池如下:\n"
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
                    server.sessionId!!,
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

    //TODO 换边实现
    fun movePlayer(I: PullIntent): Message {
        if (!I.isAdmin) return CustomerLang.notAdminErr.toPlainText()
        if (I.cmdSize < 2) return CustomerLang.parameterErr.replace("//para//", "*hb <ID>")
            .toPlainText()
        var player = 0
        Cache.PlayerListInfo.forEach { pldata ->
            if (pldata.id.indexOf(I.sp[1], 0, false) != -1) {
                player++
            }
        }
        if (player > 1) {
            var temp = "找到多个ID,无法确认\n"
            Cache.PlayerListInfo.forEach { pldata ->
                if (pldata.id.indexOf(I.sp[1], 0, false) != -1) {
                    temp += "ID:${pldata.id} 在 队伍${pldata.team}\n"
                }
            }
            return temp.toPlainText()
        } else if (player == 1) {
            var result = "换边失败"
            Cache.PlayerListInfo.forEach { pldata ->
                if (pldata.id.indexOf(I.sp[1], 0, false) != -1) {
                    runBlocking {
                        if (pldata.move().isSuccessful) {
                            result = "换边..成功了吗?如换 ${pldata.id}"
                        }
                    }
                }
            }
            return result.toPlainText()
        } else {
            return "找不到此玩家".toPlainText()
        }
    }

    //TODO 查询VIP实现
    fun getVipList(I: PullIntent): Message {
        return "正在重构".toPlainText()
    }

    /*//TODO 绑定服务器实现
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
*/
    //TODO 修改服务器kd实现
    fun setKDInfo(I: PullIntent): Message {
        if (!I.isAdmin) return CustomerLang.notAdminErr.toPlainText()
        if (I.cmdSize < 4) return CustomerLang.parameterErr.replace(
            "//para//",
            "*setkd <ServerCount> <lkd/lkp/rkd/rkp> <Float>"
        )
            .toPlainText()
        val name = I.sp[1]
        val server = ServerInfos.getServerByName(I.event.group.id,name) ?: return CustomerLang.nullServerErr.replace("//err//", name).toPlainText()
        when (I.sp[2]) {
            "lkd" -> ServerInfos.setKD(server.gameID?:"", lifeMaxKD = I.sp[3].toFloat())
            "lkp" -> ServerInfos.setKD(server.gameID?:"", lifeMaxKPM = I.sp[3].toFloat())
            "rkp" -> ServerInfos.setKD(server.gameID?:"", recentlyMaxKPM = I.sp[3].toFloat())
            "rkd" -> ServerInfos.setKD(server.gameID?:"", recentlyMaxKD = I.sp[3].toFloat())
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
        if (I.cmdSize < 2) return Bindings.recentlyTempWhitelist.toString().toPlainText()
        return if (Bindings.addWl(I.event.group.id, I.sp[1])) {
            "添加白名单成功".toPlainText()
        } else {
            "移除白名单成功".toPlainText()
        }
    }

}