package top.ffshaozi.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.GroupTempMessageEvent
import net.mamoe.mirai.message.data.*
import top.ffshaozi.BF1ToolPlugin.logger
import top.ffshaozi.config.Setting.GroupID
import top.ffshaozi.config.Setting.ServerNameData
import top.ffshaozi.config.Setting.bindingData
import top.ffshaozi.utils.BF1Api.RSPKickPlayer
import top.ffshaozi.utils.BF1Api.getPersonaid
import top.ffshaozi.utils.BF1Api.getStats
import top.ffshaozi.utils.BF1Api.getWeapon
import top.ffshaozi.utils.BF1Api.getWelcomeMessage
import top.ffshaozi.utils.BF1Api.setAPILocale
import top.ffshaozi.utils.Value.helpText
import top.ffshaozi.utils.Value.helpTextAdmin
import java.util.*
import kotlin.collections.HashMap

data class PullIntent(
    val event: GroupMessageEvent,
    val sp: List<String>,
    val isAdmin: Boolean,
    val cmdSize: Int,
)
data class PullIntentTemp(
    val event: GroupTempMessageEvent,
    val sp: List<String>,
    val isAdmin: Boolean,
    val cmdSize: Int,
)

object Intent {
    fun runTemp(groupTempMessageEvent: GroupTempMessageEvent, msg: String, isAdmin: Boolean):Any{
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
            listOf("*bdssid","*绑定服务器ssid") to { bindingServerSessionId(pullIntent) },
        )
        cmdList.forEach {v ->
            v.key.forEach {
                if (it == sp[0].lowercase(Locale.getDefault())) {
                    logger.warning(it.toString())
                    logger.warning(sp.toString())
                    result = v.value()
                }
            }
        }
        return if (result is Message) {
            result as Message
        } else {
            PlainText("未知命令")
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
        val vp = listOf("*冲锋枪","*霰弹枪","*轻机枪","*配备","*半自动步枪","*配枪","*近战武器","*武器")
        var result: Message? = null
        val cmdList = hashMapOf(
            listOf("*help","*帮助","*?") to { help(pullIntent) },
            listOf("*bd","*绑定") to { bindingUser(pullIntent) },
            listOf("*ss","*f") to { searchServer(pullIntent) },
            listOf("*c","*查询") to { searchMe(pullIntent) },
            listOf("*vp","*载具") to { searchVehicle(pullIntent) },
            listOf("*wp","*武器") to { searchWp(pullIntent) },
            listOf("*bds","*绑服") to { bindingServer(pullIntent) },
            listOf("*sn","*默认服务器") to { bindingServerName(pullIntent) },
            listOf("*k","*kick") to { kickPlayer(pullIntent) },
            vp to {vpType:String? -> searchWp(pullIntent, vpType) },
        )
        cmdList.forEach {v ->
            v.key.forEach {
                if (it == sp[0].lowercase(Locale.getDefault())) {
                    logger.warning(it.toString())
                    logger.warning(sp.toString())
                    result = v.value(it)
                }
            }
        }
        return if (result is Message) {
            result as Message
        } else {
            PlainText("未知命令")
        }
    }


    //TODO 帮助实现
    private fun help(I: PullIntent): Message {
        return if (I.isAdmin) {
            PlainText(helpText + "\n" + helpTextAdmin)
        } else {
            PlainText(helpText)
        }
    }
    //TODO 绑定实现
    private fun bindingUser(I: PullIntent): Message {
        return if (I.cmdSize > 1) {//绑定操作
            bindingData.put(I.event.sender.id, I.sp[1])
            PlainText("绑定成功")
        } else {
            //解绑操作
            val temp = bindingData.remove(I.event.sender.id)
            if (temp != null) {
                PlainText("解绑成功")
            } else {
                PlainText("未绑定,请先绑定EAID")
            }
        }
    }
    //TODO 查询自己实现
    private fun searchMe(I: PullIntent): Message {
        val id = getEAID(I)
        if (id.isEmpty()) return PlainText("未绑定EAID")
        //查询
        sendMsg(I, "查询${id}的基础数据中...")
        val tempStatsJson = getStats(id)
        return if (tempStatsJson.isSuccessful) {
            PlainText(
                """
                            ID:${tempStatsJson.userName} Lv.${tempStatsJson.rank} 
                            PID:${tempStatsJson.id}
                            KPM:${tempStatsJson.killsPerMinute} SPM:${tempStatsJson.scorePerMinute} 
                            LifeKD:${tempStatsJson.killDeath} 胜率:${tempStatsJson.winPercent}
                            死亡:${tempStatsJson.deaths} 击杀:${tempStatsJson.kills}
                            命中率:${tempStatsJson.accuracy} 爆头率:${tempStatsJson.headshots}
                            游玩时长:${tempStatsJson.secondsPlayed.toInt() / 60 / 60}h
                            最佳兵种:${tempStatsJson.bestClass} 
                            
                        """.trimIndent()
            )

        } else {
            PlainText("基础数据查询失败")
        }
    }
    //TODO 查询武器实现
    private fun searchWp(I: PullIntent, type: String? = null): Message {
        val typeI = when(type){
            "*霰弹枪" ->  "霰彈槍"
            "*轻机枪" ->  "輕機槍"
            "*配备" -> "配備"
            "*半自动步枪" -> "半自動步槍"
            "*配枪" -> "佩槍"
            "*近战武器" -> "近戰武器"
            "*手榴弹" -> "手榴彈"
            "*步枪" -> "步槍"
            "*战场装备" -> "戰場裝備"
            "*驾驶员" -> "坦克/駕駛員"
            "*制式步枪" -> "制式步槍"
            else -> null
        }
        val id = getEAID(I)
        if (id.isEmpty()) return PlainText("未绑定EAID")
        //查询
        sendMsg(I, "查询${id}的武器数据中...")
        val tempWeaponsJson = getWeapon(id)
        val message = I.event.buildForwardMessage {
            add(I.event.bot.id, I.event.bot.nick, PlainText("回复:${id}"))
            if (tempWeaponsJson.isSuccessful) {
                val newWp = tempWeaponsJson.weapons!!.sortedByDescending { weapons -> weapons.kills }
                var index = 0
                newWp.forEach {
                    if (typeI != null) {
                        if (it.type == typeI) {
                            if (index < 60) {
                                add(
                                    I.event.bot.id, I.event.bot.nick,
                                    PlainText(
                                        """
                            武器名:${it.weaponName} 
                            武器击杀数:${it.kills}
                            爆头率:${it.headshots}
                            命中率:${it.accuracy}
                            枪械类型:${it.type}
                                            """.trimIndent()
                                    )
                                )
                                index++
                            }
                        }
                    } else {
                        if (index < 60) {
                            add(
                                I.event.bot.id, I.event.bot.nick,
                                PlainText(
                                    """
                            武器名:${it.weaponName} 
                            武器击杀数:${it.kills}
                            爆头率:${it.headshots}
                            命中率:${it.accuracy}
                            枪械类型:${it.type}
                                            """.trimIndent()
                                )
                            )
                            index++
                        }
                    }
                }
            } else {
                add(I.event.bot.id, I.event.bot.nick, PlainText("武器数据查询失败"))
            }
        }
        return message
    }
    //TODO 查询载具实现
    private fun searchVehicle(I: PullIntent): Message {
        val id = getEAID(I)
        if (id.isEmpty()) return PlainText("未绑定EAID")
        //查询
        sendMsg(I, "查询${id}的载具数据中...")
        val tankJson = BF1Api.getVehicles(id)
        val message = I.event.buildForwardMessage {
            add(I.event.bot.id, I.event.bot.nick, PlainText("回复:${id}"))
            if (tankJson.isSuccessful) {
                tankJson.vehicles?.sortedByDescending { vehicles -> vehicles.kills }?.forEach {
                    add(
                        I.event.bot.id, I.event.bot.nick,
                        """
                            名称:${it.vehicleName}
                            类型:${it.type}
                            星数:${it.kills / 100}
                            摧毁数:${it.destroyed}
                            KPM:${it.killsPerMinute}
                        """.trimIndent().toPlainText()
                    )
                }
            } else {
                add(I.event.bot.id, I.event.bot.nick, "查询失败,请重试".toPlainText())
            }
        }
        return message
    }
    //TODO 查询服务器实现
    private fun searchServer(I: PullIntent): Message {
        return if (I.cmdSize > 1) {
            sendMsg(I, "查询${I.sp[1]}中")
            val serverSearchJson = BF1Api.searchServer(I.sp[1])
            return if (serverSearchJson.isSuccessful) {
                var temp = ""
                serverSearchJson.servers?.forEachIndexed { index, it ->
                    temp += """
                                    服务器${index + 1}==>
                                    名称:${it.prefix!!.substring(0, 30)}
                                    局内人数:${it.serverInfo} 观战人数${it.inSpectator}
                                    当前地图:${it.currentMap}
                                    GameID:
                                    ${it.gameId}
                                    ServerID:
                                    ${it.serverId}
                                """.trimIndent() + "\n\n"
                }
                temp.toPlainText()
            } else {
                "查询失败喵~".toPlainText()
            }
        } else {
            "请输入服务器名称".toPlainText()
        }
    }
    //TODO 踢人实现
    private fun kickPlayer(I: PullIntent):Message{
        if (I.isAdmin){
            if (I.cmdSize>2){
                var serverName = ""
                ServerNameData.forEach { (t, u) ->
                    if (t == I.event.group.id){
                        serverName = u
                    }
                }
                if (serverName.isEmpty()) return "未定义默认服务器名称".toPlainText()
                //sendMsg(I,"查找此人PID")
                val pid = getPersonaid(I.sp[1])
                if (!pid.isSuccessful) return "查找PID失败,踢人失败".toPlainText()
                //sendMsg(I,"查找该服务器")
                val serverID = BF1Api.searchServer(serverName)
                if (!serverID.isSuccessful) return "查找服务器失败,踢人失败".toPlainText()
                val exc: HashMap<String, String> = hashMapOf()
                GroupID.forEach { (t, u) ->
                    //是否此群
                    if (t == I.event.group.id){
                        //群绑定服务器遍历
                        u.forEach {
                            it.forEach { (s, r) ->
                                serverID.servers?.forEach {
                                    if (it.serverId == s) {
                                        //gameid,ssid
                                        it.gameId?.let { it1 -> exc.put(it1,r) }
                                    }
                                }
                            }
                        }
                    }
                }
                exc.forEach { (t, u) ->
                    val rspKickPlayer = RSPKickPlayer(u, t, pid.id.toString(), I.sp[2])
                }
                return "踢完了".toPlainText()
            }else{
                return "参数不足".toPlainText()
            }
        }else{
            return "非管理员".toPlainText()
        }
    }
    //TODO 绑定服务器实现
    private fun bindingServer(I: PullIntent): Message {
        if (I.isAdmin) {
            when (I.cmdSize) {
                in 1..2 -> return GroupID.toString().toPlainText()
                in 3..4 -> {
                    when (I.sp[1]) {
                        "add" -> {
                            return if (I.sp[2].isNotEmpty()) {
                                if (addBindServer(I.event.group.id, I.sp[2])) {
                                    "成功".toPlainText()
                                } else {
                                    "已存在该绑定".toPlainText()
                                }
                            } else {
                                "参数不足".toPlainText()
                            }
                        }

                        "remove" -> {
                            return if (I.sp[2].isNotEmpty()) {
                                removeBindServer(I.event.group.id, I.sp[2])
                                "成功".toPlainText()
                            }else{
                                "参数不足".toPlainText()
                            }
                        }

                        else -> return "无效参数".toPlainText()
                    }
                }

                else -> {
                    return "未知命令".toPlainText()
                }
            }
        } else {
            return "非管理员,无法执行".toPlainText()
        }
    }
    //TODO 绑定服务器名称
    private fun bindingServerName(I: PullIntent):Message{
        return if (I.isAdmin){
            if (I.cmdSize>1){
                ServerNameData[I.event.group.id] = I.sp[1]
                "设置成功".toPlainText()
            }else{
                "参数不足".toPlainText()
            }
        }else{
            "非管理员权限".toPlainText()
        }
    }
    //TODO 绑定服务器ssid
    private fun bindingServerSessionId(I: PullIntentTemp):Message{
        if (I.isAdmin){
            if (I.cmdSize>2){
                val ssid = getWelcomeMessage(I.sp[2])
                val apiLocale = setAPILocale(I.sp[2])
                if (!ssid.isSuccessful) return "失败请重试".toPlainText()
                if (!apiLocale.isSuccessful) return "失败请重试".toPlainText()
                return if (I.sp[1] == "All"){
                    GroupID[I.event.group.id]?.forEach {
                        it.forEach { (t, u) ->
                            it[t] = I.sp[2]
                        }
                    }
                    "绑定成功\n${ssid.firstMessage}".toPlainText()
                }else{
                    setBindServer(I.event.group.id,I.sp[1],I.sp[2])
                    "绑定成功\n${ssid.firstMessage}".toPlainText()
                }
            }else{
               return "参数不足".toPlainText()
            }
        }else{
            return "不是管理员".toPlainText()
        }
    }
    //TODO 添加绑定服务器
    private fun addBindServer(gid: Long, string: String): Boolean = GroupID[gid]!!.add(mutableMapOf(Pair(string, "")))
    //TODO 修改绑定服务器
    private fun setBindServer(gid: Long, serverID: String, sessionId: String = ""): Boolean {
        GroupID[gid]?.forEach {
            it.forEach { (t, u) ->
                if (t == serverID) {
                    it[t] = sessionId
                }
            }
        }
        return true
    }
    //TODO 移除绑定服务器
    private fun removeBindServer(gid: Long, serverID: String) {
        GroupID.values.forEach {
            it.removeIf {
                it[serverID] != null
            }
        }
    }
    //TODO 查询是否已绑定EAID
    private fun getEAID(I: PullIntent): String {
        return if (I.cmdSize > 1) {
            I.sp[1]
        } else {//查询自己
            var eaid = ""
            bindingData.forEach {
                if (it.key == I.event.sender.id) {
                    eaid = it.value
                }
            }
            return eaid.ifEmpty {
                ""
            }
        }
    }
    //TODO 发送信息
    private fun sendMsg(I: PullIntent, msg: Any) {
        CoroutineScope(Dispatchers.IO).launch {
            if (msg is Message) {
                I.event.subject.sendMessage(msg)
            } else {
                I.event.subject.sendMessage(msg.toString())
            }
        }
    }
}
