package top.ffshaozi

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.utils.info

object BF1ToolPlugin : KotlinPlugin(
    JvmPluginDescription(
        id = "top.ffshaozi.bf1toolplugin",
        name = "BF1ToolPlugin",
        version = "0.1.0",
    ) {
        author("FFSHAOZI")
        info("""战地1QQ机器人""")
    }
) {

    override fun onEnable() {
        logger.info { "战地一插件已启用,正在加载..." }
        // 订阅所有来着 Bot 的消息
        globalEventChannel().subscribeGroupMessages {
            startsWith("*") quoteReply { cmd ->
                logger.warning(cmd)
                "测试回复 ${cmd}"
            }
        }
    }
}