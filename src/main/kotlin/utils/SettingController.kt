package top.ffshaozi.utils

import net.mamoe.mirai.message.data.toPlainText
import top.ffshaozi.BF1ToolPlugin.Glogger
import top.ffshaozi.config.Setting.groupData

/**
 * @Description
 * @Author littleArray
 * @Date 2023/8/26
 */
object SettingController {
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
                it.vipList.put(
                    name,
                    System.currentTimeMillis() + (time.toFloat() * 24 * 60 * 60 * 1000)
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
}