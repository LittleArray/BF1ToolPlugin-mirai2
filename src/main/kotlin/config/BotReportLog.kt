package top.ffshaozi.config

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import top.ffshaozi.config.BotBlackList.provideDelegate

/**
 * @Description
 * @Author littleArray
 * @Date 2023/9/9
 */
object BotReportLog: AutoSavePluginData("BotReportLog") {
    @ValueDescription("机器人反馈Log,勿动")
    val botLog: MutableList<String> by value()
}