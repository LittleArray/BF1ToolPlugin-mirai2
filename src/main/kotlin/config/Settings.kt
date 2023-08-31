package top.ffshaozi.config

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object Setting : AutoSavePluginData("Setting") {
    @ValueDescription("绑定的群数据(实验)")
    val groupData: MutableMap<Long, DataForGroup> by value()
}
@Serializable
data class DataForGroup(
    var server:MutableSet<ServerInfoForSave> = mutableSetOf(),
    var bindingData:MutableMap<Long,String> = mutableMapOf(),//key是qq号
    var recentlyTempWhitelist:MutableSet<String> = mutableSetOf()
)
@Serializable
data class ServerInfoForSave(
    var serverName:String?=null,
    var sessionId:String?=null,
    var gameID:String?=null,
    var serverRspID:Int = 0,
    var serverGuid:String?=null,
    var players:Int= 0,
    var isEnableRecentlyKick:Boolean= true,
    var recentlyMaxKD:Float= 2.0F,
    var recentlyMaxKPM:Float= 1.5F,
    var lifeMaxKD:Float= 1.5F,
    var lifeMaxKPM:Float= 1.5F,
    var vipList: MutableMap<String,Float> = mutableMapOf()
)
