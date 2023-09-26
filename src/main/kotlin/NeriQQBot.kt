package top.ffshaozi

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.command.descriptor.DoubleValueArgumentParser.findGroupOrFail
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.containsGroup
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import net.mamoe.mirai.event.events.MemberLeaveEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.event.subscribeGroupTempMessages
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.info
import top.ffshaozi.command.BF1Cmd
import top.ffshaozi.config.*
import top.ffshaozi.intent.Cache.BotGroups
import top.ffshaozi.intent.CycleTask
import top.ffshaozi.intent.Intent
import top.ffshaozi.intent.ServerApi
import top.ffshaozi.utils.BF1Api
import top.ffshaozi.utils.test

object NeriQQBot : KotlinPlugin(
    JvmPluginDescription(
        id = "top.ffshaozi.NeriQQBot",
        name = "NeriQQBot",
        version = "1.0.4",
    ) {
        author("FFSHAOZI")
        info("""Neri家的战地1QQ机器人""")
    }
) {

    lateinit var Glogger: MiraiLogger
    lateinit var GlobalBots: List<Bot>

    override fun onEnable() {

        Glogger = logger
        test()
        Glogger.info { "战地一插件已启用,正在加载..." }
        Setting.reload()
        Glogger.info { "重新加载设置成功" }
        CustomerLang.reload()
        Glogger.info { "重新加载语言成功" }
        CustomerCmd.reload()
        Glogger.info { "重新加载命令成功" }
        BF1Cmd.register()
        Glogger.info { "基础命令加载成功" }
        BotLog.reload()
        HistoryLog.reload()
        Glogger.info { "Log加载成功" }
        Bindings.reload()
        BackgroundImgData.reload()
        Glogger.info { "绑定数据加载成功" }
        ServerInfos.reload()
        Glogger.info { "服务器数据加载成功" }
        GroupSetting.reload()
        Glogger.info { "群组数据加载成功" }

        //Setting.logerSetting()
        ServerApi.run(Setting.port)
        CycleTask.serverManageRefresh()
        CycleTask.vipRefresh()
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
                        GroupSetting.groupSetting.forEach {
                            temp = if (it.groupID == this.group.id) {
                                var isAdmin = false
                                this.group.members.forEach { normalMember ->
                                    if (normalMember.permission.level != 0 && normalMember.id == this.sender.id) isAdmin = true
                                }
                                it.operator.forEach {
                                    if (it == this.sender.id) isAdmin = true
                                }
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
                    GroupSetting.groupSetting.forEach {
                        sentFrom(it.groupID ?:0L) quoteReply  { s ->
                            var isAdmin = false
                            this.group.members.forEach {
                                if (it.permission.level != 0 && it.id == this.sender.id) isAdmin = true
                            }
                            it.operator.forEach {
                                if (it == this.sender.id) isAdmin = true
                            }
                            Intent.run(this, s, isAdmin)
                        }
                    }
                }
                Glogger.info("重注册${bot.id}入群判断事件响应 ")
                bot.eventChannel.subscribeAlways<MemberJoinRequestEvent>{
                    GroupSetting.groupSetting.forEach {p->
                        if (it.group?.id == p.groupID) {
                            val id = message.replace("\n","").replace("问题：EAID","").replace("答案：","")
                            Glogger.info("${fromId}请求入群:${groupId},入群消息:${id}")
                            this.group?.sendMessage("${fromNick}[${fromId}]尝试进群,EAID:${id},管理员请核对")
                        }
                    }
                }
                Glogger.info("重注册${bot.id}离开群判断事件响应 ")
                bot.eventChannel.subscribeAlways<MemberLeaveEvent> {
                    GroupSetting.groupSetting.forEach { p ->
                        if (it.group.id == p.groupID) {
                            this.group.sendMessage("${member.nick}[${member.id}]受不了群里的南通氛围,离开了")
                            Bindings.bindingData.remove(member.id)
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