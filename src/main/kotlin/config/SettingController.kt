package top.ffshaozi.config

import top.ffshaozi.BF1ToolPlugin.Glogger
import top.ffshaozi.config.Setting.groupData
import top.ffshaozi.utils.BF1Api

/**
 * @Description
 * @Author littleArray
 * @Date 2023/8/26
 */
object SettingController {
    fun logerSetting(){
        groupData.forEach{(groupId,data) ->
            Glogger.info("群:${groupId}的设定")
            Glogger.info("-管理列表:")
            data.operator.forEach {
                Glogger.info("--${it}")
            }
            Glogger.info("-绑定唧唧群:${data.botGroup}")
            Glogger.info("-绑定唧唧Api地址:${data.botUrl}")
            Glogger.info("-是否使用唧唧检测:${data.isUseBot}")
            Glogger.info("-临时白列表:")
            data.recentlyTempWhitelist.forEach {
                Glogger.info("--${it}")
            }
            Glogger.info("-绑定的服务器:")
            data.server.forEach {
                Glogger.info("--服务器名:${it.serverName}")
                Glogger.info("---GameID:${it.gameID}")
                Glogger.info("---最大生涯KPM:${it.lifeMaxKPM}")
                Glogger.info("---最大生涯KD:${it.lifeMaxKD}")
                Glogger.info("---最大最近KPM:${it.recentlyMaxKPM}")
                Glogger.info("---最大最近KD:${it.recentlyMaxKD}")
                Glogger.info("---是否启用自动踢人:${it.isEnableAutoKick}")
                Glogger.info("---Vip列表")
                it.vipList.forEach{
                    Glogger.info("----ID:${it.key} 到期时间戳:${it.value}")
                }
            }
        }
    }
    fun refreshServerInfo(groupID:Long){
        groupData[groupID]?.server?.forEach {ctx ->
            //搜索服务器,判断serverGUID
            Glogger.info("搜索服务器,判断serverGUID")
            if (ctx.serverName.isNullOrBlank()) return@forEach
            val serverSearchJson = BF1Api.searchServer(ctx.serverName!!)
            //判断是否成功
            Glogger.info("判断是否成功")
            if (!serverSearchJson.isSuccessful) return@forEach
            serverSearchJson.servers?.forEach {
                if (it.serverId == ctx.serverGuid) {
                    ctx.gameID = it.gameId
                    ctx.serverName = it.prefix
                }
            }
            //判断有没有找到服务器
            Glogger.info("判断有没有找到服务器")
            if (ctx.gameID.isNullOrBlank()) return@forEach
            //ssid不为空
            Glogger.info("ssid不为空")
            if (ctx.sessionId.isNullOrBlank()) return@forEach
            val serverInfoJson = BF1Api.getFullServerDetails(ctx.sessionId!!, ctx.gameID!!)
            Glogger.info("serverInfoJson不为空")
            if (serverInfoJson.isSuccessful == false) return@forEach
            ctx.serverRspID = serverInfoJson.result?.rspInfo?.server?.serverId?.toInt()!!
        }
    }
    fun addBinding(groupID: Long,qq:Long,EAid:String) = groupData[groupID]?.bindingData?.put(qq,EAid)
    fun removeBinding(groupID: Long,qq: Long): String? = groupData[groupID]?.bindingData?.remove(qq)
    fun getBinding(groupID: Long,qq: Long):String{
        var idTemp = ""
        groupData[groupID]?.bindingData?.forEach { tqq, eaid ->
            if (qq == tqq) idTemp=eaid
        }
        return idTemp
    }

    fun isNullServer(groupID: Long):Boolean{
        val serverSize = groupData[groupID]?.server?.size
        return serverSize == null
    }
    fun addVip(groupID: Long,serverCount:Int,name:String,time:String):Boolean{
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
    fun removeVip(groupID: Long,serverCount:Int,name: String):Boolean{
        if (isNullServer(groupID)) return false
        groupData[groupID]?.server?.forEachIndexed { index, it ->
            if (index + 1 == serverCount) {
                it.vipList.remove(name)
                return true
            }
        }
        return false
    }

    fun setOperator(opId:Long,groupId:Long){
        val isRemove = groupData[groupId]?.operator?.removeIf {
            opId == it
        }
        if (isRemove == false)
            groupData[groupId]?.operator?.add(opId)
    }

    fun setBotGroup(groupId: Long,isUseBot:Boolean,botGroupId:String,botUrl:String){
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
        groupData[gid]?.recentlyTempWhitelist?.forEach {
            if (eaid == it) temp = it
        }
        return if (temp.isNotEmpty()) {
            groupData[gid]?.recentlyTempWhitelist?.removeIf {
                it == eaid
            }
            false
        } else {
            groupData[gid]?.recentlyTempWhitelist?.add(eaid)
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
        groupData[gid]?.server?.forEachIndexed { index, it ->
            if (serverCount == index + 1) {
                if (lifeMaxKD > 0)
                    it.lifeMaxKD = lifeMaxKD
                if (lifeMaxKPM > 0)
                    it.lifeMaxKPM = lifeMaxKPM
                if (recentlyMaxKPM > 0)
                    it.recentlyMaxKPM = recentlyMaxKPM
                if (recentlyMaxKD > 0)
                    it.lifeMaxKD = recentlyMaxKD
            }
        }
        return true
    }
}