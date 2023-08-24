package top.ffshaozi.config

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object Setting : AutoSavePluginData("Setting") {
    @ValueDescription("群ID,{serverID,sessionId}")
    var GroupID:MutableMap<Long,MutableSet<MutableMap<String,String>>> by value()
    @ValueDescription("服务器名称")
    var ServerNameData:MutableMap<Long,String> by value()
    @ValueDescription("绑定的用户数据")
    var bindingData:MutableMap<Long,String> by value()
}
