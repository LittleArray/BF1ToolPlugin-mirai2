package top.ffshaozi.config

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import top.ffshaozi.config.Setting.provideDelegate

/**
 * @Description
 * @Author littleArray
 * @Date 2023/9/9
 */
object BotBlackList: AutoSavePluginData("BotBlackList") {
    @ValueDescription("机器人黑名单")
    val qqList: MutableList<String> by value()
}