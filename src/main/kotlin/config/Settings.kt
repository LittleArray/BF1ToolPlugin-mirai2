package top.ffshaozi.config

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import top.ffshaozi.NeriQQBot
import top.ffshaozi.utils.BF1Api

object Setting : AutoSavePluginData("Setting") {
    @ValueDescription("服务器Api端口")
    val port:Int by value(2086)
    @ValueDescription("绑定的群数据(实验)")
    val groupData: MutableMap<Long, DataForGroup> by value()

    fun logerSetting() {
        groupData.forEach { (groupId, data) ->
            NeriQQBot.Glogger.info("群:${groupId}的设定")
            NeriQQBot.Glogger.info("-管理列表:")
            data.operator.forEach {
                NeriQQBot.Glogger.info("--${it}")
            }
            NeriQQBot.Glogger.info("-绑定唧唧群:${data.botGroup}")
            NeriQQBot.Glogger.info("-绑定唧唧Api地址:${data.botUrl}")
            NeriQQBot.Glogger.info("-是否使用唧唧检测:${data.isUseBot}")
            NeriQQBot.Glogger.info("-绑定的服务器:")
            data.server.forEach {
                NeriQQBot.Glogger.info("--服务器名:${it.serverName}")
                NeriQQBot.Glogger.info("---GameID:${it.gameID}")
                NeriQQBot.Glogger.info("---最大生涯KPM:${it.lifeMaxKPM}")
                NeriQQBot.Glogger.info("---最大生涯KD:${it.lifeMaxKD}")
                NeriQQBot.Glogger.info("---最大最近KPM:${it.recentlyMaxKPM}")
                NeriQQBot.Glogger.info("---最大最近KD:${it.recentlyMaxKD}")
                NeriQQBot.Glogger.info("---是否启用自动踢人:${it.isEnableAutoKick}")
                NeriQQBot.Glogger.info("---Vip列表")
                it.vipList.forEach {
                    NeriQQBot.Glogger.info("----ID:${it.key} 到期时间戳:${it.value}")
                }
            }
        }
    }

    fun refreshServerInfo(groupID: Long, isLog: Boolean = true) {
        groupData[groupID]?.server?.forEach { ctx ->
            //搜索服务器,判断serverGUID
            if (isLog)
                NeriQQBot.Glogger.info("搜索服务器,判断serverGUID")
            if (ctx.serverName.isNullOrBlank()) return@forEach
            val serverSearchJson = BF1Api.searchServer(ctx.serverName!!)
            //判断是否成功
            if (isLog)
                NeriQQBot.Glogger.info("判断是否成功")
            if (!serverSearchJson.isSuccessful) return@forEach
            serverSearchJson.servers?.forEach {
                if (it.serverId == ctx.serverGuid) {
                    ctx.gameID = it.gameId
                    ctx.serverName = it.prefix
                }
            }
            //判断有没有找到服务器
            if (isLog)
                NeriQQBot.Glogger.info("判断有没有找到服务器")
            if (ctx.gameID.isNullOrBlank()) return@forEach
            //ssid不为空
            if (isLog)
                NeriQQBot.Glogger.info("ssid不为空")
            if (ctx.sessionId.isNullOrBlank()) return@forEach
            val serverInfoJson = BF1Api.getFullServerDetails(ctx.sessionId!!, ctx.gameID!!)
            if (isLog)
                NeriQQBot.Glogger.info("serverInfoJson不为空")
            if (serverInfoJson.isSuccessful == false) return@forEach
            ctx.serverRspID = serverInfoJson.result?.rspInfo?.server?.serverId?.toInt()!!
        }
    }

    fun addBinding(groupID: Long, qq: Long, EAid: String) = Bindings.bindingData.put(qq, EAid)
    fun removeBinding(groupID: Long, qq: Long): String? = Bindings.bindingData.remove(qq)
    fun getBinding(groupID: Long, qq: Long): String {
        var idTemp = ""
        Bindings.bindingData.forEach { tqq, eaid ->
            if (qq == tqq) idTemp = eaid
        }
        return idTemp
    }

    fun isNullServer(groupID: Long): Boolean {
        val serverSize = groupData[groupID]?.server?.size
        return serverSize == null
    }

    fun addVip(groupID: Long, serverCount: Int, name: String, time: String): Boolean {
        if (isNullServer(groupID)) return false
        groupData[groupID]?.server?.forEachIndexed { index, it ->
            if (index + 1 == serverCount) {
                var times = System.currentTimeMillis()
                it.vipList.forEach { id, time ->
                    if (id == name) times = time.toLong()
                }
                it.vipList.put(
                    name,
                    times + (time.toFloat() * 24 * 60 * 60 * 1000)
                )
                return true
            }
        }
        return false
    }

    fun removeVip(groupID: Long, serverCount: Int, name: String): Boolean {
        if (isNullServer(groupID)) return false
        groupData[groupID]?.server?.forEachIndexed { index, it ->
            if (index + 1 == serverCount) {
                it.vipList.remove(name)
                return true
            }
        }
        return false
    }

    fun setOperator(opId: Long, groupId: Long) {
        val isRemove = groupData[groupId]?.operator?.removeIf {
            opId == it
        }
        if (isRemove == false)
            groupData[groupId]?.operator?.add(opId)
    }

    fun setBotGroup(groupId: Long, isUseBot: Boolean, botGroupId: String, botUrl: String) {
        groupData[groupId]?.isUseBot = isUseBot
        groupData[groupId]?.botGroup = botGroupId
        groupData[groupId]?.botUrl = botUrl
    }

    //TODO 修改服务器抗压白名单
    fun addWl(
        gid: Long,
        eaid: String,
    ): Boolean {
        var temp = ""
        Bindings.recentlyTempWhitelist.forEach {
            if (eaid == it) temp = it
        }
        return if (temp.isNotEmpty()) {
            Bindings.recentlyTempWhitelist.removeIf {
                it == eaid
            }
            false
        } else {
            Bindings.recentlyTempWhitelist.add(eaid)
            true
        }
    }

    //修改服务器kd
    fun setKD(
        gid: Long,
        serverCount: Int,
        recentlyMaxKD: Float = 0F,
        recentlyMaxKPM: Float = 0F,
        lifeMaxKD: Float = 0F,
        lifeMaxKPM: Float = 0F,
    ): Boolean {
        var gameID = ""
        groupData[gid]?.server?.forEachIndexed { index, it ->
            if (serverCount == index + 1) {
                gameID = it.gameID.toString()
            }
        }
        groupData.forEach { group, data ->
            data.server.forEach {
                if (it.gameID == gameID){
                    if (lifeMaxKD > 0)
                        it.lifeMaxKD = lifeMaxKD
                    if (lifeMaxKPM > 0)
                        it.lifeMaxKPM = lifeMaxKPM
                    if (recentlyMaxKPM > 0)
                        it.recentlyMaxKPM = recentlyMaxKPM
                    if (recentlyMaxKD > 0)
                        it.recentlyMaxKD = recentlyMaxKD
                }
            }
        }
        return true
    }
}

@Serializable
data class DataForGroup(
    var server:MutableSet<ServerInfoForSave> = mutableSetOf(),
    //var bindingData:MutableMap<Long,String> = mutableMapOf(),//key是qq号
    //var recentlyTempWhitelist:MutableSet<String> = mutableSetOf(),
    var isUseBot:Boolean = true,
    var botGroup:String = "605712770",
    var botUrl:String = "https://asoul.zj.cn/api/warm/status",
    var operator:MutableSet<Long> = mutableSetOf(),
    var eacApiKey:String = ""
){
    @Serializable
    data class ServerInfoForSave(
        var serverName:String?=null,
        var sessionId:String?=null,
        var gameID:String?=null,
        var serverRspID:Int = 0,
        var serverGuid:String?=null,
        var isEnableAutoKick:Boolean = true,
        var recentlyMaxKD:Float= 2.0F,
        var recentlyMaxKPM:Float= 1.5F,
        var lifeMaxKD:Float= 1.5F,
        var lifeMaxKPM:Float= 1.5F,
        var platoonLimited:MutableList<String> = mutableListOf(),
        var vipList: MutableMap<String,Float> = mutableMapOf()
    )
}
