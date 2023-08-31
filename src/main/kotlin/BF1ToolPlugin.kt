package top.ffshaozi

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.event.subscribeGroupTempMessages
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.info
import top.ffshaozi.command.BF1Cmd
import top.ffshaozi.config.CustomerLang
import top.ffshaozi.config.Setting
import top.ffshaozi.config.Setting.groupData
import top.ffshaozi.utils.Intent
import top.ffshaozi.utils.Value.groups

object BF1ToolPlugin : KotlinPlugin(
    JvmPluginDescription(
        id = "top.ffshaozi.bf1toolplugin",
        name = "BF1ToolPlugin",
        version = "1.0.0",
    ) {
        author("FFSHAOZI")
        info("""战地1QQ机器人""")
    }
) {

    lateinit var Glogger: MiraiLogger
    private lateinit var GlobalBots: List<Bot>

    override fun onEnable() {
        Glogger = logger
        Glogger.info { "战地一插件已启用,正在加载..." }
        Setting.reload()
        CustomerLang.reload()
        BF1Cmd.register()
        //登录事件
        globalEventChannel().subscribeOnce<BotOnlineEvent> {
            Glogger.info("重注册${it.bot.id}登录事件响应 ")
            GlobalBots = Bot.instances
            GlobalBots.forEach {
                Glogger.info {
                    "检测到Bot:${it.id}上线"
                }
                it.bot.groups.forEach {
                    groups = groups + it.name + "   " + it.id + ","
                }
                logger.info("获取到的群聊 $groups")
                Glogger.info("重注册${it.id}群临时会话事件响应 ")
                it.eventChannel.subscribeGroupTempMessages {
                    startsWith("*") reply { s ->
                        var temp: Any? = null
                        groupData.forEach { (it, _) ->
                            temp = if (it == this.group.id) {
                                var isAdmin = false
                                this.group.members.forEach {
                                    if (it.permission.level != 0 && it.id == this.sender.id) isAdmin = true
                                }
                                if (this.sender.id == 3354889203) {
                                    isAdmin = true
                                }
                                Glogger.info("临时消息处理...")
                                Intent.runTemp(this, s, isAdmin)
                            } else {
                                Unit
                            }
                        }
                        temp
                    }
                }
                Glogger.info("重注册${it.id}群会话事件响应 ")
                it.eventChannel.subscribeGroupMessages {
                    groupData.forEach { (it, _) ->
                        sentFrom(it) and startsWith("*") reply { s ->
                            var isAdmin = false
                            this.group.members.forEach {
                                if (it.permission.level != 0 && it.id == this.sender.id) isAdmin = true
                            }
                            if (this.sender.id == 3354889203) {
                                isAdmin = true
                            }
                            Glogger.info("群消息处理...")
                            Intent.run(this, s, isAdmin)
                        }
                    }
                }
            }
        }
    }

    override fun onDisable() {
        super.onDisable()
        BF1Cmd.unregister()
    }
}