package top.ffshaozi.config

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import top.ffshaozi.NeriQQBot
import top.ffshaozi.utils.BF1Api

/**
 * @Description 服务器数据类
 * @Author littleArray
 * @Date 2023/9/16
 */
object ServerInfos : AutoSavePluginData("ServerInfo") {
    @ValueDescription("服务器数据类")
    var serverInfo: MutableList<_ServerInfo> by value()//服务器信息

    /**
     * 添加服务器
     * @param name String
     * @param gameID String
     */
    fun addServer(name: String, gameID: String) {
        serverInfo.removeIf {
            it.gameID == gameID
        }
        val temp = _ServerInfo(serverName = name, gameID = gameID)
        serverInfo.add(temp)
    }

    /**
     * 移除服务器
     * @param name String
     * @param gameID String
     */
    fun removeServer(gameID: String): Boolean {
        return serverInfo.removeIf {
            it.gameID == gameID
        }
    }

    /**
     * 设置唧唧数据的获取地址
     * @param gameID String
     * @param url String
     * @return Boolean
     */
    fun setBotUrl(gameID: String, url: String): Boolean {
        serverInfo.forEach {
            if (gameID == it.gameID) {
                it.botUrl = url
                return true
            }
        }
        return false
    }

    /**
     * 设置唧唧所在群号的获取地址
     * @param gameID String
     * @param url String
     * @return Boolean
     */
    fun setBotGroup(gameID: String, group: Long): Boolean {
        serverInfo.forEach {
            if (gameID == it.gameID) {
                it.botGroup = group
                return true
            }
        }
        return false
    }

    /**
     * 更新服务器sessionId
     * @param gameID String
     * @param sessionId String
     */
    fun updateServerSSID(gameID: String, sessionId: String, isAll: Boolean = false): Boolean {
        serverInfo.forEach {
            if (it.gameID == gameID || isAll) {
                it.sessionId = sessionId
                return true
            }
        }
        return false
    }

    /**
     * 更新服务器信息
     * @param gameID String
     */
    fun updateServer(gameID: String, isLog: Boolean = true): Boolean {
        serverInfo.forEach {
            if (it.gameID == gameID) {
                //搜索服务器,判断serverGUID
                if (isLog)
                    NeriQQBot.Glogger.info("搜索服务器,判断serverGUID")
                if (it.serverName.isNullOrBlank()) return false
                val serverSearchJson = BF1Api.searchServer(it.serverName!!)
                //判断是否成功
                if (isLog)
                    NeriQQBot.Glogger.info("判断是否成功")
                if (!serverSearchJson.isSuccessful) return false
                serverSearchJson.servers?.forEach { s ->
                    if (s.gameId == it.gameID) {
                        it.gameID = s.gameId
                        it.serverName = s.prefix
                    }
                }
                //判断有没有找到服务器
                if (isLog)
                    NeriQQBot.Glogger.info("判断有没有找到服务器")
                if (it.gameID.isNullOrBlank()) return false
                //ssid不为空
                if (isLog)
                    NeriQQBot.Glogger.info("ssid不为空")
                if (it.sessionId.isNullOrBlank()) return false
                val serverInfoJson = BF1Api.getFullServerDetails(it.sessionId!!, it.gameID!!)
                if (isLog)
                    NeriQQBot.Glogger.info("serverInfoJson不为空")
                if (serverInfoJson.isSuccessful == false) return false
                it.serverRspID = serverInfoJson.result?.rspInfo?.server?.serverId?.toInt()!!
                return true
            }
        }
        return false
    }

    /**
     * 设置服务器kd
     * @param gameID String
     * @param recentlyMaxKD Float
     * @param recentlyMaxKPM Float
     * @param lifeMaxKD Float
     * @param lifeMaxKPM Float
     * @return Boolean
     */
    fun setKD(
        gameID: String,
        recentlyMaxKD: Float = 0F,
        recentlyMaxKPM: Float = 0F,
        lifeMaxKD: Float = 0F,
        lifeMaxKPM: Float = 0F,
    ): Boolean {
        serverInfo.forEach {
            if (it.gameID == gameID) {
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
        return true
    }

    /**
     * 通过名字来找GameId
     * @param group Long
     * @param name String
     * @return String
     */
    fun getGameIDByName(group: Long,name: String):String{
        GroupSetting.groupSetting.forEach {
            if (it.groupID == group) {
                it.games.forEach {
                    if (it.name == name) return it.gameID
                }
            }
        }
        return ""
    }

    /**
     * 通过服务器名找到对应服务器信息
     * @param group Long
     * @param name String
     * @return _GroupSetting.Games
     */
    fun getServerByName(group: Long,name: String):_ServerInfo?{
        val gameID = getGameIDByName(group, name)
        serverInfo.forEach {
            if (it.gameID == gameID) {
                return it
            }
        }
        return null
    }

}

@Serializable
data class _ServerInfo(
    var serverName: String? = null,
    var gameID: String? = null,
    var serverRspID: Int = 0,
    var serverGuid: String? = null,
    var sessionId: String? = null,
    var isUseBot: Boolean = true,
    var botGroup: Long = 605712770L,
    var botUrl: String = "https://asoul.zj.cn/api/warm/status",
    var isEnableAutoKick: Boolean = true,
    var isEnableReEnterKick: Boolean = true,
    var ReEnterKickMsg: String = "伺服器禁止短時間內重進",
    var recentlyMaxKD: Float = 2.0F,
    var recentlyMaxKPM: Float = 1.5F,
    var lifeMaxKD: Float = 1.5F,
    var lifeMaxKPM: Float = 1.5F,
    var platoonLimited: MutableList<String> = mutableListOf(),
    var vipList: MutableMap<String, Float> = mutableMapOf()
)