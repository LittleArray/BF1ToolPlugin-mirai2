package top.ffshaozi.intent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.GroupTempMessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.toPlainText
import top.ffshaozi.NeriQQBot
import top.ffshaozi.NeriQQBot.logger
import top.ffshaozi.config.CustomerCmd
import top.ffshaozi.config.CustomerLang
import java.util.*


object Intent {

    suspend fun run(event: GroupMessageEvent, msg: String, isAdmin: Boolean): Any {
        val sp = msg.split(" ")
        val cmdSize = sp.size
        val pullIntent = PullIntent(
            event = event,
            sp = sp,
            isAdmin = isAdmin,
            cmdSize = cmdSize
        )
        val vp =
            listOf(
                "冲锋枪",
                "霰弹枪",
                "轻机枪",
                "配备",
                "半自动步枪",
                "配枪",
                "近战武器",
                "步枪",
                "制式步枪",
                "手枪",
                "副武器",
                "佩枪",
                "机枪"
            )
        var result: Any? = null
        val cmdList = hashMapOf(
            CustomerCmd.help to { runBlocking { help(pullIntent) } },
            CustomerCmd.binding to { runBlocking { EnquiryService.bindingUser(pullIntent) } },
            CustomerCmd.bf1 to { runBlocking { EnquiryService.bf1(pullIntent) } },
            CustomerCmd.searchBlackTeam to { runBlocking { EnquiryService.searchBlackTeam(pullIntent) } },
            CustomerCmd.ss to { runBlocking { EnquiryService.searchServer(pullIntent) } },
            //CustomerCmd.searchHistory to { runBlocking { EnquiryService.searchHistory(pullIntent) } },
            //CustomerCmd.ssi to { runBlocking { EnquiryService.searchServerListPlayer(pullIntent) } },
            //CustomerCmd.stats to { runBlocking { EnquiryService.searchMe(pullIntent) } },
            //CustomerCmd.vehicle to { runBlocking { EnquiryService.searchVehicle(pullIntent) } },
            //CustomerCmd.weapon to { runBlocking { EnquiryService.searchWp(pullIntent) } },
            //CustomerCmd.recently to { runBlocking { EnquiryService.searchRecently(pullIntent) } },
            CustomerCmd.playerList to { runBlocking { EnquiryService.searchServerList(pullIntent) } },
            CustomerCmd.setkd to { runBlocking { ServerManagement.setKDInfo(pullIntent) } },
            CustomerCmd.ky to { runBlocking { ServerManagement.wl(pullIntent) } },
            CustomerCmd.kick to { runBlocking { ServerManagement.kickPlayer(pullIntent) } },
            CustomerCmd.ban to { runBlocking { ServerManagement.banPlayer(pullIntent) } },
            CustomerCmd.vBan to { runBlocking { ServerManagement.addVban(pullIntent) } },
            CustomerCmd.chooseMap to { runBlocking { ServerManagement.chooseLevel(pullIntent) } },
            CustomerCmd.movePlayer to { runBlocking { ServerManagement.movePlayer(pullIntent) } },
            CustomerCmd.searchEac to { runBlocking { EnquiryService.searchEACBan(pullIntent) } },
            CustomerCmd.removeBan to { runBlocking { ServerManagement.unBanPlayer(pullIntent) } },
            CustomerCmd.removeVBan to { runBlocking { ServerManagement.unVban(pullIntent) } },
            CustomerCmd.addVip to { runBlocking { ServerManagement.addVipPlayer(pullIntent) } },
            CustomerCmd.removeVip to { runBlocking { ServerManagement.unVipPlayer(pullIntent) } },
            CustomerCmd.searchVip to { runBlocking { ServerManagement.getVipList(pullIntent) } },
            CustomerCmd.searchBan to { runBlocking { ServerManagement.getBanList(pullIntent) } },
            CustomerCmd.warmProgress to { runBlocking { EnquiryService.warmProgress(pullIntent) } },
            vp to { vpType: String? -> runBlocking { EnquiryService.searchWp(pullIntent, vpType) } },
        )
        cmdList.forEach { v ->
            v.key.forEach {
                if (CustomerCmd.prefix.any { prefix -> msg.lowercase(Locale.getDefault()).indexOf(prefix) == 0 }) {
                    if (it == sp[0].lowercase(Locale.getDefault()).substring(1)) {
                        result = v.value(it)
                    }
                } else {
                    if (it == sp[0].lowercase(Locale.getDefault())) {
                        result = v.value(it)
                    }
                }
            }
        }
        return if (result is Message) {
            NeriQQBot.Glogger.info("群消息处理...")
            result as Message
        } else {
            if (CustomerCmd.prefix.any { msg.indexOf(it) == 0 }) {
                PlainText(CustomerLang.errCommand.replace("//err//", msg))
            } else {
                Unit
            }
        }
    }


    //TODO 帮助实现
    private fun help(I: PullIntent): Message {
        return if (I.isAdmin) {
            PlainText(Cache.help + "\n\n" + Cache.helpAdmin)
        } else {
            PlainText(Cache.help)
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
