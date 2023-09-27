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
        var res = ""
        val kickR = when (I.sp[2]) {
            "*tj" -> "禁止偷家"
            "*zz" -> "禁止蜘蛛人"
            "*ur" -> "違反規則"
            "*nf" -> "nuan服战神滾"
            else -> I.sp[2]
        }
        var player = 0
        Cache.PlayerListInfo.forEach { pldata ->
            if (pldata.id.indexOf(I.sp[1], 0, true) != -1) {
                player++
            }
        }
        if (player > 1) {
            var temp = "找到多个ID,无法确认\n"
            Cache.PlayerListInfo.forEach { pldata ->
                if (pldata.id.indexOf(I.sp[1], 0, true) != -1) {
                    temp += "ID:${pldata.id} 在 队伍${pldata.team}\n"
                }
            }
            res += temp
        } else if (player == 1) {
            var result = "找不到此玩家"
            Cache.PlayerListInfo.forEach { pldata ->
                if (pldata.id.indexOf(I.sp[1], 0, true) != -1) {
                    runBlocking {
                        val k = pldata.kick(kickR)
                        result = if (k.isSuccessful) {
                            CustomerLang.kickSucc.replace("//id//",pldata.id).replace("//res//",kickR).replace("//serverCount//","")
                        } else {
                            CustomerLang.kickErr.replace("//id//",pldata.id).replace("//err//",k.reqBody)
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
        val time = try {
            I.sp[3].toFloat()
        }catch (e:Exception){
            return CustomerLang.parameterErr.replace("//para//", "*av <ServerCount> <ID> <Time>").toPlainText()
        }
        val server = ServerInfos.getServerByName(I.event.group.id,name) ?: return CustomerLang.nullServerErr.replace("//err//", name).toPlainText()
        val serverVIP = BF1Api.addServerVIP(server.sessionId.toString(), server.serverRspID, I.sp[2])
        if (serverVIP.isSuccessful || serverVIP.reqBody.indexOf("RspErrUserIsAlreadyVip") != -1) {
            if (I.cmdSize > 2) {
                ServerInfos.addVip(server.gameID!!,I.sp[2],time)
                return CustomerLang.addVIPSucc
                    .replace("//id//", I.sp[2])
                    .replace("//serverCount//", name)
                    .replace("//Time//", "${time}天的")
                    .toPlainText()
            }
            ServerInfos.addVip(server.gameID!!,I.sp[2],9999F)
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
            ServerInfos.removeVip(server.gameID!!,I.sp[2])
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
            if (pldata.id.indexOf(I.sp[1], 0, true) != -1) {
                player++
            }
        }
        if (player > 1) {
            var temp = "找到多个ID,无法确认\n"
            Cache.PlayerListInfo.forEach { pldata ->
                if (pldata.id.indexOf(I.sp[1], 0, true) != -1) {
                    temp += "ID:${pldata.id} 在 队伍${pldata.team}\n"
                }
            }
            return temp.toPlainText()
        } else if (player == 1) {
            var result = "换边失败"
            Cache.PlayerListInfo.forEach { pldata ->
                if (pldata.id.indexOf(I.sp[1], 0, true) != -1) {
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
        if (!I.isAdmin) return CustomerLang.notAdminErr.toPlainText()
        if (I.cmdSize < 2) return CustomerLang.parameterErr.replace("//para//", "*gv <ServerCount>").toPlainText()
        val name = I.sp[1]
        val server = ServerInfos.getServerByName(I.event.group.id,name) ?: return CustomerLang.nullServerErr.replace("//err//", name).toPlainText()
        var temp = "服务器$name 的vip列表如下\n"
        ServerInfos.serverInfo.forEach {
            if (server.gameID!! == it.gameID){
                it.vipList.forEach { id, endTime ->
                    temp+="ID:${id} 到期时间:${SimpleDateFormat("yyyy-MM-dd HH:mm").format(endTime)}\n"
                }
            }
        }
        return temp.toPlainText()
    }

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