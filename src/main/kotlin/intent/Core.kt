package top.ffshaozi.intent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.GroupTempMessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import top.ffshaozi.NeriQQBot
import top.ffshaozi.NeriQQBot.logger
import top.ffshaozi.config.CustomerCmd
import top.ffshaozi.config.CustomerLang
import java.util.*




object Intent {
    fun runTemp(groupTempMessageEvent: GroupTempMessageEvent, msg: String, isAdmin: Boolean): Any {
        val sp = msg.split(" ")
        val cmdSize = sp.size
        val pullIntent = PullIntentTemp(
            event = groupTempMessageEvent,
            sp = sp,
            isAdmin = isAdmin,
            cmdSize = cmdSize
        )
        var result: Message? = null
        val cmdList = hashMapOf(
            listOf("*bdssid", "*绑定服务器ssid") to { ServerManagement.bindingServerSessionId(pullIntent) },
        )
        cmdList.forEach { v ->
            v.key.forEach {
                if (it == sp[0].lowercase(Locale.getDefault())) {
                    logger.warning(it)
                    logger.warning(sp.toString())
                    result = v.value()
                }
            }
        }
        return if (result is Message) {
            NeriQQBot.Glogger.info("临时消息处理...")
            result as Message
        } else {
            PlainText(CustomerLang.errCommand.replace("//err//", msg))
        }
    }

    fun run(event: GroupMessageEvent, msg: String, isAdmin: Boolean): Any {
        val sp = msg.split(" ")
        val cmdSize = sp.size
        val pullIntent = PullIntent(
            event = event,
            sp = sp,
            isAdmin = isAdmin,
            cmdSize = cmdSize
        )
        val vp =
            listOf("*冲锋枪", "*霰弹枪", "*轻机枪", "*配备", "*半自动步枪", "*配枪", "*近战武器", "*步枪", "*制式步枪")
        var result: Message? = null
        val cmdList = hashMapOf(
            CustomerCmd.vips to { CycleTask.vipRefresh(pullIntent) },
            CustomerCmd.sls to { CycleTask.serverManageRefresh(pullIntent) },
            CustomerCmd.help to { help(pullIntent) },
            CustomerCmd.binding to { EnquiryService.bindingUser(pullIntent) },
            CustomerCmd.ss to { EnquiryService.searchServer(pullIntent) },
            CustomerCmd.ssi to { EnquiryService.searchServerListPlayer(pullIntent) },
            CustomerCmd.stats to { EnquiryService.searchMe(pullIntent) },
            CustomerCmd.vehicle to { EnquiryService.searchVehicle(pullIntent) },
            CustomerCmd.weapon to { EnquiryService.searchWp(pullIntent) },
            CustomerCmd.recently to { EnquiryService.searchRecently(pullIntent) },
            CustomerCmd.playerList to { EnquiryService.searchServerList(pullIntent) },
            CustomerCmd.bindingServer to { ServerManagement.bindingServer(pullIntent) },
            CustomerCmd.setkd to { ServerManagement.setKDInfo(pullIntent) },
            CustomerCmd.ky to { ServerManagement.wl(pullIntent) },
            CustomerCmd.kick to { ServerManagement.kickPlayer(pullIntent) },
            CustomerCmd.ban to { ServerManagement.banPlayer(pullIntent) },
            CustomerCmd.chooseMap to { ServerManagement.chooseLevel(pullIntent) },
            CustomerCmd.movePlayer to { ServerManagement.movePlayer(pullIntent) },
            CustomerCmd.searchEac to { EnquiryService.searchEACBan(pullIntent) },
            CustomerCmd.removeBan to { ServerManagement.unBanPlayer(pullIntent) },
            CustomerCmd.addVip to { ServerManagement.addVipPlayer(pullIntent) },
            CustomerCmd.removeVip to { ServerManagement.unVipPlayer(pullIntent) },
            CustomerCmd.searchVip to { ServerManagement.getVipList(pullIntent) },
            CustomerCmd.searchBan to { ServerManagement.getBanList(pullIntent) },
            vp to { vpType: String? -> EnquiryService.searchWp(pullIntent, vpType) },
        )
        cmdList.forEach { v ->
            v.key.forEach {
                if (it == sp[0].lowercase(Locale.getDefault())) {
                    result = v.value(it)
                }
            }
        }
        return if (result is Message) {
            NeriQQBot.Glogger.info("群消息处理...")
            result as Message
        } else {
            if (msg.indexOf("*") == 0){
                PlainText(CustomerLang.errCommand.replace("//err//", msg))
            }else{
                Unit
            }
        }
    }


    //TODO 帮助实现
    private fun help(I: PullIntent): Message {
        return if (I.isAdmin) {
            PlainText(CustomerLang.help + "\n\n" + CustomerLang.helpAdmin)
        } else {
            PlainText(CustomerLang.help)
        }
    }


    //TODO 发送信息
    fun sendMsg(I: PullIntent, msg: Any) {
        CoroutineScope(Dispatchers.IO).launch {
            if (msg is Message) {
                I.event.subject.sendMessage(msg)
            } else {
                I.event.subject.sendMessage(msg.toString())
            }
        }
    }
}
