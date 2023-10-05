package top.ffshaozi.config

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
/**
 * @Description 全局可用的绑定数据
 * @Author littleArray
 * @Date 2023/9/16
 */
object Bindings : AutoSavePluginData("Bindings"){
    @ValueDescription("绑定的qq号")
    var bindingData:MutableMap<Long,String> by value()//key是qq号
    @ValueDescription("临时抗压白")
    var recentlyTempWhitelist:MutableSet<String> by value()
    //TODO 修改服务器抗压白名单
    fun addWl(
        gid: Long,
        eaid: String,
    ): Boolean {
        var temp = ""
        recentlyTempWhitelist.forEach {
            if (eaid == it) temp = it
        }
        return if (temp.isNotEmpty()) {
            recentlyTempWhitelist.removeIf {
                it == eaid
            }
            false
        } else {
            recentlyTempWhitelist.add(eaid)
            true
        }
    }
    fun addBinding(groupID: Long, qq: Long, EAid: String) {
        bindingData.put(qq, EAid)
        ServerInfos.serverInfo.forEach {
            it.riskBanList.remove(EAid)
        }
    }
    fun removeBinding(groupID: Long, qq: Long): String? = bindingData.remove(qq)
    fun getBinding(groupID: Long, qq: Long): String {
        var idTemp = ""
        bindingData.forEach { tqq, eaid ->
            if (qq == tqq) idTemp = eaid
        }
        return idTemp
    }
}

