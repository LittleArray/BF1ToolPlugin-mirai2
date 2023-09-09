package top.ffshaozi

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.event.subscribeGroupTempMessages
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.info
import top.ffshaozi.command.BF1Cmd
import top.ffshaozi.config.CustomerCmd
import top.ffshaozi.config.CustomerLang
import top.ffshaozi.config.Setting
import top.ffshaozi.config.Setting.groupData
import top.ffshaozi.config.SettingController
import top.ffshaozi.intent.Cache.BotGroups
import top.ffshaozi.intent.Intent
import top.ffshaozi.utils.BF1Api
import top.ffshaozi.utils.test

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
        //test()
        Glogger.info { "战地一插件已启用,正在加载..." }
        Setting.reload()
        Glogger.info { "重新加载设置成功" }
        CustomerLang.reload()
        Glogger.info { "重新加载语言成功" }
        CustomerCmd.reload()
        Glogger.info { "重新加载命令成功" }
        BF1Cmd.register()
        Glogger.info { "基础命令加载成功" }
        SettingController.logerSetting()
        //登录事件
        globalEventChannel().subscribeOnce<BotOnlineEvent> { onlineEvent ->
            Glogger.info("重注册${onlineEvent.bot.id}登录事件响应 ")
            GlobalBots = Bot.instances
            GlobalBots.forEach { bot ->
                Glogger.info {
                    "检测到Bot:${bot.id}上线"
                }
                bot.bot.groups.forEach { group ->
                    BotGroups = BotGroups + group.name + "   " + group.id + ","
                }
                logger.info("获取到的群聊 $BotGroups")
                Glogger.info("重注册${bot.id}群临时会话事件响应 ")
                bot.eventChannel.subscribeGroupTempMessages {
                    startsWith("*") reply { s ->
                        var temp: Any? = null
                        groupData.forEach { (it, data) ->
                            temp = if (it == this.group.id) {
                                var isAdmin = false
                                this.group.members.forEach { normalMember ->
                                    if (normalMember.permission.level != 0 && normalMember.id == this.sender.id) isAdmin = true
                                }
                                data.operator.forEach {
                                    if (it == this.sender.id) isAdmin = true
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
                Glogger.info("重注册${bot.id}群会话事件响应 ")
                bot.eventChannel.subscribeGroupMessages {
                    groupData.forEach { (it, data) ->
                        sentFrom(it) reply { s ->
                            var isAdmin = false
                            this.group.members.forEach {
                                if (it.permission.level != 0 && it.id == this.sender.id) isAdmin = true
                            }
                            data.operator.forEach {
                                if (it == this.sender.id) isAdmin = true
                            }
                            Glogger.info("群消息处理...")
                            Intent.run(this, s, isAdmin)
                        }
                    }
                }
                Glogger.info("重注册${bot.id}入群判断事件响应 ")
                bot.eventChannel.subscribeAlways<MemberJoinRequestEvent>{
                    groupData.forEach { (groupId, _) ->
                        if (it.group?.id == groupId) {
                            val id = message.replace("\n","").replace("问题：EAID","").replace("答案：","")
                            Glogger.info("${fromId}请求入群:${groupId},入群消息:${id}")
                            val stats = BF1Api.getStats(id)
                            if (stats.isSuccessful){
                                accept()
                                this.group?.members?.forEach {
                                    if (fromId == it.id) it.nameCard = id
                                }
                                group?.id?.let { gr -> SettingController.addBinding(gr, fromId, id) }
                                this.group?.sendMessage("新成员进群,快欢迎他,EAID:${id},尝试改名")
                            }
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