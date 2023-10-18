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
import okhttp3.internal.wait
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
        version = "1.0.5",
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
        //登录事件
        ServerApi.run(Setting.port)
        CycleTask.serverManageRefresh()
        CycleTask.vipRefresh()
        /*globalEventChannel().subscribeAlways<BotOnlineEvent> { onlineEvent ->
            Glogger.info("重注册${onlineEvent.bot.id}登录事件响应 ")
            Glogger.info {
                "检测到Bot:${bot.id}上线"
            }
            bot.groups.forEach { group ->
                BotGroups = BotGroups + group.name + "   " + group.id + ","
            }
            logger.info("获取到的群聊 $BotGroups")
            Glogger.info("重注册${bot.id}群会话事件响应 ")
            bot.eventChannel.subscribeGroupMessages {
                GroupSetting.groupSetting.forEach {
                    sentFrom(it.groupID ?: 0L) quoteReply { s ->
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
            bot.eventChannel.subscribeAlways<MemberJoinRequestEvent> {
                GroupSetting.groupSetting.forEach { p ->
                    if (it.group?.id == p.groupID) {
                        val id = message.replace("\n", "").replace("问题：EAID", "").replace("答案：", "")
                        Glogger.info("${fromId}请求入群:${groupId},入群消息:${id}")

                        run p@{
                            if (BF1Api.searchBFEAC(id).data?.first()?.current_status == 1) {
                                this.group?.sendMessage("挂钩:$id 尝试进群,已拒绝并拉入黑名单")
                                reject(true)
                                return@p
                            }
                            ServerInfos.serverInfo.forEach {
                                if (it.riskBanList.any { it == id }) {
                                    this.group?.sendMessage(
                                        """
                                        ${fromNick}[${fromId}]由于风控进群
                                        EAID:${id}(有效ID),自动通过
                                    """.trimIndent()
                                    )
                                    accept()
                                    return@p
                                }
                            }
                            val stats = BF1Api.getStats(id)
                            if (stats.rank != 0 || stats.currentRankProgress != 0L) {
                                this.group?.sendMessage(
                                    """
                                    ${fromNick}[${fromId}]尝试进群
                                    EAID:${id}(有效ID),管理员请核对
                                    ${
                                        when (stats.killDeath) {
                                            in 0.0..1.0 -> "鉴定为大薯"
                                            in 1.0..2.0 -> "鉴定为中薯"
                                            in 2.0..3.0 -> "鉴定为噗肉哥"
                                            in 3.0..4.0 -> "鉴定为超级噗肉哥"
                                            else -> { "我超瓜!!!" }
                                        }
                                    }
                                    """.trimIndent()
                                )
                            } else {
                                this.group?.sendMessage(
                                    """
                                    ${fromNick}[${fromId}]尝试进群,EAID:${id}(无效ID),管理员请核对
                                    """.trimIndent()
                                )
                            }
                        }
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
        }*/
    }

    override fun onDisable() {
        super.onDisable()
        BF1Cmd.unregister()
    }
}