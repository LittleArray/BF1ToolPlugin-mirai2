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
}