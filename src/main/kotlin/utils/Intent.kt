package top.ffshaozi.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.GroupTempMessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildForwardMessage
import net.mamoe.mirai.message.data.toPlainText
import top.ffshaozi.BF1ToolPlugin.logger
import top.ffshaozi.config.CustomerLang.addVIPErr
import top.ffshaozi.config.CustomerLang.addVIPSucc
import top.ffshaozi.config.CustomerLang.banErr
import top.ffshaozi.config.CustomerLang.banSucc
import top.ffshaozi.config.CustomerLang.bindingSucc
import top.ffshaozi.config.CustomerLang.errCommand
import top.ffshaozi.config.CustomerLang.kickErr
import top.ffshaozi.config.CustomerLang.kickSucc
import top.ffshaozi.config.CustomerLang.notAdminErr
import top.ffshaozi.config.CustomerLang.nullEac
import top.ffshaozi.config.CustomerLang.nullServerErr
import top.ffshaozi.config.CustomerLang.parameterErr
import top.ffshaozi.config.CustomerLang.searchErr
import top.ffshaozi.config.CustomerLang.searching
import top.ffshaozi.config.CustomerLang.searchingSer
import top.ffshaozi.config.CustomerLang.serverInfoRErr
import top.ffshaozi.config.CustomerLang.serverInfoRefreshing
import top.ffshaozi.config.CustomerLang.unBanErr
import top.ffshaozi.config.CustomerLang.unBanSucc
import top.ffshaozi.config.CustomerLang.unVIPErr
import top.ffshaozi.config.CustomerLang.unVIPSucc
import top.ffshaozi.config.CustomerLang.unbindingErr
import top.ffshaozi.config.CustomerLang.unbindingSucc
import top.ffshaozi.config.ServerInfoForSave
import top.ffshaozi.config.Setting.groupData
import top.ffshaozi.data.FullServerInfoJson
import top.ffshaozi.utils.BF1Api.addServerBan
import top.ffshaozi.utils.BF1Api.addServerVIP
import top.ffshaozi.utils.BF1Api.getFullServerDetails
import top.ffshaozi.utils.BF1Api.getPersonaid
import top.ffshaozi.utils.BF1Api.getStats
import top.ffshaozi.utils.BF1Api.getWeapon
import top.ffshaozi.utils.BF1Api.getWelcomeMessage
import top.ffshaozi.utils.BF1Api.kickPlayer
import top.ffshaozi.utils.BF1Api.recentlySearch
import top.ffshaozi.utils.BF1Api.recentlyServerSearch
import top.ffshaozi.utils.BF1Api.removeServerBan
import top.ffshaozi.utils.BF1Api.removeServerVIP
import top.ffshaozi.utils.BF1Api.seaechBFEAC
import top.ffshaozi.utils.BF1Api.searchServerList
import top.ffshaozi.utils.BF1Api.setAPILocale
import top.ffshaozi.utils.CycleTask.serverPlayerListRefresh
import top.ffshaozi.utils.CycleTask.vipRefresh
import top.ffshaozi.utils.SettingController.addBinding
import top.ffshaozi.utils.SettingController.addVip
import top.ffshaozi.utils.SettingController.getBinding
import top.ffshaozi.utils.SettingController.isNullServer
import top.ffshaozi.utils.SettingController.refreshServerInfo
import top.ffshaozi.utils.SettingController.removeBinding
import top.ffshaozi.utils.SettingController.removeVip
import top.ffshaozi.utils.Value.helpText
import top.ffshaozi.utils.Value.helpTextAdmin
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*


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
            listOf("*bdssid", "*绑定服务器ssid") to { bindingServerSessionId(pullIntent) },
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
            result as Message
        } else {
            PlainText(errCommand.replace("//err//", msg))
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
            listOf("*vips") to { vipRefresh(pullIntent) },
            listOf("*sls") to { serverPlayerListRefresh(pullIntent) },
            listOf("*help", "*帮助", "*?") to { help(pullIntent) },
            listOf("*bd", "*绑定") to { bindingUser(pullIntent) },
            listOf("*ss", "*f") to { searchServer(pullIntent) },
            listOf("*ssi", "*cxlb") to { searchServerListPlayer(pullIntent) },
            listOf("*c", "*查询", "*战绩") to { searchMe(pullIntent) },
            listOf("*vp", "*载具") to { searchVehicle(pullIntent) },
            listOf("*wp", "*武器") to { searchWp(pullIntent) },
            listOf("*rec", "*最近") to { searchRecently(pullIntent) },
            listOf("*pl", "*玩家列表") to { searchServerList(pullIntent) },
            listOf("*bds", "*绑服") to { bindingServer(pullIntent) },
            listOf("*setkd") to { setKDInfo(pullIntent) },
            listOf("*抗压") to { wl(pullIntent) },
            listOf("*k", "*kick", "*踢人") to { kickPlayer(pullIntent) },
            listOf("*b", "*ban") to { banPlayer(pullIntent) },
            listOf("*eac", "*eacban") to { searchEACBan(pullIntent) },
            listOf("*rb", "*removeban") to { unBanPlayer(pullIntent) },
            listOf("*av", "*addvip") to { addVipPlayer(pullIntent) },
            listOf("*rv", "*removevip") to { unVipPlayer(pullIntent) },
            listOf("*gv", "*getvip", "*查v") to { getVipList(pullIntent) },
            listOf("*gb", "*getban", "*查ban") to { getBanList(pullIntent) },
            vp to { vpType: String? -> searchWp(pullIntent, vpType) },
        )
        cmdList.forEach { v ->
            v.key.forEach {
                if (it == sp[0].lowercase(Locale.getDefault())) {
                    result = v.value(it)
                }
            }
        }
        return if (result is Message) {
            result as Message
        } else {
            PlainText(errCommand.replace("//err//", msg))
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
            addBinding(I.event.group.id, I.event.sender.id, I.sp[1])
            PlainText(bindingSucc.replace("//id//", I.sp[1]))
        } else {
            //解绑操作
            val temp = removeBinding(I.event.group.id, I.event.sender.id)
            if (temp != null) {
                PlainText(unbindingSucc.replace("//id//", I.event.sender.nameCardOrNick))
            } else {
                PlainText(unbindingErr)
            }
        }
    }

    //TODO 查询自己实现
    private fun searchMe(I: PullIntent): Message {
        var id = getBinding(I.event.group.id, I.event.sender.id)
        if (I.cmdSize > 1) id = I.sp[1]
        if (id.isEmpty()) return PlainText(unbindingErr)
        //查询
        sendMsg(I, searching.replace("//id//", id).replace("//action//", "基础数据"))
        val tempStatsJson = getStats(id)
        return if (tempStatsJson.isSuccessful) {
            PlainText(
                """
                            ID:${tempStatsJson.userName} Lv.${tempStatsJson.rank} 
                            PID:${tempStatsJson.id}
                            KPM:${tempStatsJson.killsPerMinute} SPM:${tempStatsJson.scorePerMinute} 
                            LifeKD:${tempStatsJson.killDeath} 胜率:${tempStatsJson.winPercent}
                            死亡:${tempStatsJson.deaths} 击杀:${tempStatsJson.kills}
                            扎人数:${tempStatsJson.revives} 治疗数:${tempStatsJson.heals}
                            修理数:${tempStatsJson.repairs}  狗牌数:${tempStatsJson.dogtagsTaken}
                            复仇数:${tempStatsJson.avengerKills} 协助击杀:${tempStatsJson.killAssists}
                            命中率:${tempStatsJson.accuracy} 爆头率:${tempStatsJson.headshots}
                            技巧值:${tempStatsJson.skill}
                            最远击杀距离:${tempStatsJson.longestHeadShot}m
                            游玩时长:${tempStatsJson.secondsPlayed.toInt() / 60 / 60}h
                            最佳兵种:${tempStatsJson.bestClass}
                        """.trimIndent()
            )

        } else {
            PlainText(searchErr.replace("//action//", "基础数据"))
        }
    }

    //TODO 查询BFEAC的实现
    private fun searchEACBan(I: PullIntent): Message {
        var id = getBinding(I.event.group.id, I.event.sender.id)
        if (I.cmdSize > 1) id = I.sp[1]
        if (id.isEmpty()) return PlainText(unbindingErr)
        //查询
        sendMsg(I, searching.replace("//id//", id).replace("//action//", "EACBan数据"))
        val eacInfoJson = seaechBFEAC(id)
        if (eacInfoJson.error_code != 0) return PlainText(nullEac.replace("//id//", id))
        if (eacInfoJson.data.isNullOrEmpty()) return PlainText(nullEac.replace("//id//", id))
        return when (eacInfoJson.data[0].current_status) {
            0 -> PlainText("ID:${eacInfoJson.data[0].current_name}有记录但未处理\nLink:https://www.bfeac.com/?#/case/${eacInfoJson.data[0].case_id}")
            1 -> PlainText("ID:${eacInfoJson.data[0].current_name}判定为石锤\nLink:https://www.bfeac.com/?#/case/${eacInfoJson.data[0].case_id}")
            2 -> PlainText("ID:${eacInfoJson.data[0].current_name}判定为证据不足\nLink:https://www.bfeac.com/?#/case/${eacInfoJson.data[0].case_id}")
            3 -> PlainText("ID:${eacInfoJson.data[0].current_name}判定为自证通过\nLink:https://www.bfeac.com/?#/case/${eacInfoJson.data[0].case_id}")
            4 -> PlainText("ID:${eacInfoJson.data[0].current_name}判定为自证中\nLink:https://www.bfeac.com/?#/case/${eacInfoJson.data[0].case_id}")
            5 -> PlainText("ID:${eacInfoJson.data[0].current_name}判定为刷枪\nLink:https://www.bfeac.com/?#/case/${eacInfoJson.data[0].case_id}")
            else -> PlainText("ID:${eacInfoJson.data[0].current_name}未知判定\nLink:https://www.bfeac.com/?#/case/${eacInfoJson.data[0].case_id}")
        }
    }

    //TODO 最近实现
    private fun searchRecently(I: PullIntent): Message {
        var id = getBinding(I.event.group.id, I.event.sender.id)
        if (I.cmdSize > 1) id = I.sp[1]
        if (id.isEmpty()) return PlainText(unbindingErr)
        //查询
        sendMsg(I, searching.replace("//id//", id).replace("//action//", "最近数据"))
        val recentlyJson = recentlySearch(id)
        val hashMap = recentlyServerSearch(id)
        var temp = "${id}的最近数据\n"
        recentlyJson.forEachIndexed() { index, it ->
            if (index + 1 > 2) return@forEachIndexed
            if (!it.isSuccessful) return PlainText(searchErr.replace("//action//", "最近数据"))
            temp += """
            统计时间:${it.rp} 游玩时间:${it.tp.replace("h", "小时").replace("m", "分钟")}
            最近SPM/KPM/KD:${it.spm}/${it.kpm}/${it.kd}
            """.trimIndent() + "\n==========================\n"
        }
        temp += "\n${id}最近游玩服务器:\n"
        hashMap.forEach {
            temp += """
            服务器:${it.serverName} 
            地图:${it.map}
            击杀/死亡/KD:${it.kills}/${it.deaths}/${it.kd}
            对局时间:${SimpleDateFormat("MM-dd HH:mm:ss").format(it.time)}
            """.trimIndent() + "\n==========================\n"
        }
        return temp.toPlainText()
    }

    //TODO 查询武器实现
    private fun searchWp(I: PullIntent, type: String? = null): Message {
        val typeI = when (type) {
            "*霰弹枪" -> "霰彈槍"
            "*轻机枪" -> "輕機槍"
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
        var id = getBinding(I.event.group.id, I.event.sender.id)
        if (I.cmdSize > 1) id = I.sp[1]
        if (id.isEmpty()) return PlainText(unbindingErr)
        //查询
        sendMsg(I, searching.replace("//id//", id).replace("//action//", "武器数据"))
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
                            爆头击杀:${it.headshotKills}
                            爆头率:${it.headshots}
                            命中率:${it.accuracy}
                            KPM:${it.killsPerMinute}
                            配备时长:${it.timeEquipped / 60 / 60}h
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
                            爆头击杀:${it.headshotKills}
                            爆头率:${it.headshots}
                            命中率:${it.accuracy}
                            KPM:${it.killsPerMinute}
                            配备时长:${it.timeEquipped / 60 / 60}h
                            枪械类型:${it.type}
                                            """.trimIndent()
                                )
                            )
                            index++
                        }
                    }
                }
            } else {
                add(I.event.bot.id, I.event.bot.nick, PlainText(searchErr.replace("//action//", "武器数据")))
            }
        }
        return message
    }

    //TODO 查询载具实现
    private fun searchVehicle(I: PullIntent): Message {
        var id = getBinding(I.event.group.id, I.event.sender.id)
        if (I.cmdSize > 1) id = I.sp[1]
        if (id.isEmpty()) return PlainText(unbindingErr)
        //查询
        sendMsg(I, searching.replace("//id//", id).replace("//action//", "载具数据"))
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
                add(I.event.bot.id, I.event.bot.nick, searchErr.replace("//action//", "载具数据").toPlainText())
            }
        }
        return message
    }

    //TODO 查询服务器实现
    private fun searchServer(I: PullIntent): Message {
        return if (I.cmdSize > 1) {
            sendMsg(I, searchingSer.replace("//ser//", I.sp[1]))
            val serverSearchJson = BF1Api.searchServer(I.sp[1])
            return if (serverSearchJson.isSuccessful) {
                var temp = ""
                serverSearchJson.servers?.forEachIndexed { index, it ->
                    temp += """
                         ===================
                         服务器 ${index + 1} 
                         名称:${it.prefix!!.substring(0, 30)}
                         局内人数:${it.serverInfo} 观战人数:${it.inSpectator}
                         当前地图:${it.currentMap}
                         
                         GameID:
                         ${it.gameId}
                         ServerID:
                         ${it.serverId}
                    """.trimIndent() + "\n"
                }
                temp.toPlainText()
            } else {
                searchErr.replace("//action//", "服务器").toPlainText()
            }
        } else {
            parameterErr.replace("//para//", "*ss <ServerName>").toPlainText()
        }
    }

    //TODO 查询服务器玩家列表的实现
    private fun searchServerList(I: PullIntent, re: Boolean = false): Message {
        if (I.cmdSize < 2) return parameterErr.replace("//para//", "*pl <ServerCount>").toPlainText()
        if (isNullServer(I.event.group.id)) return nullServerErr.replace("//err//", "").toPlainText()
        if (re) refreshServerInfo(I.event.group.id)
        val serverCount = I.sp[1].toInt()
        groupData[I.event.group.id]?.server?.forEachIndexed { index, it ->
            if (index + 1 == serverCount) {
                if (it.gameID.isNullOrEmpty()) {
                    if (re) return serverInfoRErr.toPlainText()
                    searchServerList(I, re = true)
                    return serverInfoRefreshing.toPlainText()
                }
                val serverListJson = searchServerList(it.gameID!!)
                val msg = I.event.buildForwardMessage {
                    if (serverListJson.isSuccessful == true) {
                        var player = 0
                        val team1 = I.event.buildForwardMessage {
                            add(I.event.bot.id, "teamOne", "队伍1".toPlainText())
                            serverListJson.teams?.forEach { team ->
                                if (team.teamid == "teamOne") {
                                    team.players.forEach {
                                        add(
                                            I.event.bot.id, team.name, PlainText(
                                                """
                                                    ID:${it.name} Lv.${it.rank}
                                                    延迟:${it.latency} ms
                                                    小队:${it.platoon}
                                                    在线:${(System.currentTimeMillis() - it.join_time / 1000) / 1000 / 60}min
                                                """.trimIndent()
                                            )
                                        )
                                        player += 1
                                    }
                                }
                            }
                        }
                        val team2 = I.event.buildForwardMessage {
                            add(I.event.bot.id, "teamOne", "队伍2".toPlainText())
                            serverListJson.teams?.forEach { team ->
                                if (team.teamid == "teamTwo") {
                                    team.players.forEach {
                                        add(
                                            I.event.bot.id,team.name , PlainText(
                                                """
                                                    ID:${it.name} Lv.${it.rank}
                                                    延迟:${it.latency} ms
                                                    小队:${it.platoon}
                                                    在线:${(System.currentTimeMillis() - it.join_time / 1000) / 1000 / 60}min
                                                """.trimIndent()
                                            )
                                        )
                                        player += 1
                                    }
                                }
                            }
                        }
                        add(
                            I.event.bot.id, I.event.bot.nick, PlainText(
                                """
                                名称:${serverListJson.serverinfo?.name}
                                地图:${serverListJson.serverinfo?.level?.replace("MP_", "")}
                                模式:${serverListJson.serverinfo?.mode?.replace("Conquest", "征服")?.replace("BreakthroughLarge0","行动")}
                                人数:${player}/64
                                描述:${serverListJson.serverinfo?.description?.replace("\n","")?.substring(0,32)}
                                """.trimIndent()
                            )
                        )
                        add(I.event.bot.id, "队伍1", team1)
                        add(I.event.bot.id, "队伍2", team2)
                    } else {
                        add(I.event.bot.id, "Error", PlainText("查询失败"))
                    }
                }
                return msg
            }
        }
        return nullServerErr.replace("//err//", "第${serverCount}个").toPlainText()
    }

    //TODO 搜索服务器玩家
    private fun searchServerListPlayer(I: PullIntent, re: Boolean = false): Message {
        if (I.cmdSize < 3) return parameterErr.replace("//para//", "*ssi <ServerCount> <ID>").toPlainText()
        if (isNullServer(I.event.group.id)) return nullServerErr.replace("//err//", "").toPlainText()
        if (re) refreshServerInfo(I.event.group.id)
        val serverCount = I.sp[1].toInt()
        groupData[I.event.group.id]?.server?.forEachIndexed { index, it ->
            if (index + 1 == serverCount) {
                if (it.gameID.isNullOrEmpty()) {
                    if (re) return serverInfoRErr.toPlainText()
                    searchServerList(I, re = true)
                    return serverInfoRefreshing.toPlainText()
                }
                sendMsg(I, searching.replace("//id//", I.sp[2]).replace("//action//", "服务器玩家"))
                val serverListJson = searchServerList(it.gameID!!)
                return if (serverListJson.isSuccessful == true) {
                    var p = "在服务器${serverCount}中查找到\n"
                    serverListJson.teams?.forEach { team ->
                        team.players.forEach {
                            if (it.name.indexOf(I.sp[2], 0, true) != -1) {
                                p += "ID:${it.name} 所在队伍:${team.name}\n"
                            }
                        }
                    }
                    p.toPlainText()
                } else {
                    searchErr.replace("//action//", "服务器玩家").toPlainText()
                }
            }
        }
        return nullServerErr.replace("//err//", "第${serverCount}个").toPlainText()
    }

    //TODO 踢人实现
    private fun kickPlayer(I: PullIntent, re: Boolean = false): Message {
        if (!I.isAdmin) return notAdminErr.toPlainText()
        if (I.cmdSize < 3) return parameterErr.replace("//para//", "*k <ID> <Reason>").toPlainText()
        if (isNullServer(I.event.group.id)) return "不存在绑定的服务器".toPlainText()
        if (re) refreshServerInfo(I.event.group.id)
        val pid = getPersonaid(I.sp[1])
        groupData[I.event.group.id]?.server?.forEachIndexed { index, it ->
            if (it.gameID.isNullOrEmpty() || it.sessionId.isNullOrEmpty()) {
                if (re) serverInfoRErr.toPlainText()
                kickPlayer(I, true)
                return serverInfoRefreshing.toPlainText()
            }
            val kickR = when (I.sp[2]) {
                "*tj" -> "禁止偷家"
                "*zz" -> "禁止蜘蛛人"
                "*ur" -> "違反規則"
                "*nf" -> "nuan 服战神是吧"
                else -> I.sp[2]
            }
            val kickPlayer = kickPlayer(it.sessionId!!, it.gameID!!, pid.id.toString(), kickR)
            if (kickPlayer.isSuccessful) {
                sendMsg(
                    I,
                    kickSucc.replace("//id//", I.sp[1]).replace("//serverCount//", "${index + 1}")
                        .replace("//res//", kickR)
                )
            } else {
                refreshServerInfo(I.event.group.id)
                sendMsg(I, kickErr.replace("//id//", I.sp[1]).replace("//err//", kickPlayer.reqBody))
            }
        }
        return "Ok".toPlainText()
    }

    //TODO ban人实现
    private fun banPlayer(I: PullIntent, re: Boolean = false): Message {
        if (!I.isAdmin) return notAdminErr.toPlainText()
        if (I.cmdSize < 3) return parameterErr.replace("//para//", "*b <ServerCount> <ID>").toPlainText()
        if (isNullServer(I.event.group.id)) return nullServerErr.replace("//err//", "").toPlainText()
        if (re) refreshServerInfo(I.event.group.id)
        val serverCount = I.sp[1].toInt()
        groupData[I.event.group.id]?.server?.forEachIndexed { index, it ->
            if (index + 1 == serverCount) {
                if (it.serverRspID == 0 || it.sessionId.isNullOrEmpty()) {
                    if (re) serverInfoRErr.toPlainText()
                    banPlayer(I, true)
                    return serverInfoRefreshing.toPlainText()
                }
                val serverBan = addServerBan(it.sessionId!!, it.serverRspID, I.sp[2])
                if (serverBan.isSuccessful) {
                    return banSucc.replace("//id//", I.sp[2]).replace("//serverCount//", "$serverCount").toPlainText()
                } else {
                    refreshServerInfo(I.event.group.id)
                    return banErr.replace("//id//", I.sp[2]).replace("//err//", serverBan.reqBody).toPlainText()
                }
            }
        }
        return nullServerErr.replace("//err//", "第${serverCount}个").toPlainText()
    }

    //TODO unban人实现
    private fun unBanPlayer(I: PullIntent, re: Boolean = false): Message {
        if (!I.isAdmin) return notAdminErr.toPlainText()
        if (I.cmdSize < 3) return parameterErr.replace("//para//", "*rb <ServerCount> <ID>").toPlainText()
        if (isNullServer(I.event.group.id)) return nullServerErr.toPlainText()
        if (re) refreshServerInfo(I.event.group.id)
        val serverCount = I.sp[1].toInt()
        val pid = getPersonaid(I.sp[2])
        groupData[I.event.group.id]?.server?.forEachIndexed { index, it ->
            if (index + 1 == serverCount) {
                if (it.serverRspID == 0 || it.sessionId.isNullOrEmpty()) {
                    if (re) serverInfoRErr.toPlainText()
                    unBanPlayer(I, true)
                    return serverInfoRefreshing.toPlainText()
                }
                val serverBan = removeServerBan(it.sessionId!!, it.serverRspID, pid.id.toString())
                return if (serverBan.isSuccessful) {
                    unBanSucc.replace("//serverCount//", "$serverCount").replace("//id//", I.sp[2]).toPlainText()
                } else {
                    refreshServerInfo(I.event.group.id)
                    unBanErr.replace("//id//", I.sp[2]).replace("//err//", serverBan.reqBody).toPlainText()
                }
            }
        }
        return nullServerErr.replace("//err//", "第${serverCount}个").toPlainText()
    }

    //TODO 查询服务器Ban实现
    private fun getBanList(I: PullIntent, re: Boolean = false): Message {
        if (I.cmdSize < 2) return parameterErr.replace("//para//", "*gb <ServerCount>").toPlainText()
        if (isNullServer(I.event.group.id)) return nullServerErr.toPlainText()
        if (re) refreshServerInfo(I.event.group.id)
        val serverCount = I.sp[1].toInt()
        groupData[I.event.group.id]?.server?.forEachIndexed { index, it ->
            if (index + 1 == serverCount) {
                if (it.gameID.isNullOrEmpty()) {
                    if (re) return serverInfoRErr.toPlainText()
                    getBanList(I, re = true)
                    return serverInfoRefreshing.toPlainText()
                }
                val it: FullServerInfoJson = getFullServerDetails(it.sessionId.toString(), it.gameID.toString())
                return if (it.isSuccessful == true) {
                    var banStr = ""
                    it.result?.rspInfo?.bannedList?.sortedBy { bannedList -> bannedList.displayName }?.forEach {
                        banStr += "ID:${it.displayName}\n"
                    }
                    "服务器${serverCount}的Ban:\n$banStr".toPlainText()
                } else {
                    refreshServerInfo(I.event.group.id)
                    searchErr.replace("//action//", "封禁列表").toPlainText()
                }
            }
        }
        return nullServerErr.replace("//err//", "第${serverCount}个").toPlainText()
    }

    //TODO 添加VIP实现
    private fun addVipPlayer(I: PullIntent, re: Boolean = false): Message {
        if (!I.isAdmin) return notAdminErr.toPlainText()
        if (I.cmdSize < 2) return parameterErr.replace("//para//", "*av <ServerCount> <ID> <Time>").toPlainText()
        if (isNullServer(I.event.group.id)) return nullServerErr.toPlainText()
        if (re) refreshServerInfo(I.event.group.id)
        val serverCount = I.sp[1].toInt()
        groupData[I.event.group.id]?.server?.forEachIndexed { index, it ->
            if (index + 1 == serverCount) {
                if (it.gameID.isNullOrEmpty() || it.serverRspID == 0) {
                    if (re) return serverInfoRErr.toPlainText()
                    addVipPlayer(I, re = true)
                    return serverInfoRefreshing.toPlainText()
                }
                val serverVIP = addServerVIP(it.sessionId.toString(), it.serverRspID, I.sp[2])
                if (serverVIP.isSuccessful) {
                    if (I.cmdSize > 2) {
                        addVip(I.event.group.id, serverCount, I.sp[2], I.sp[3])
                        return addVIPSucc
                            .replace("//id//", I.sp[2])
                            .replace("//serverCount//", "$serverCount")
                            .replace("//Time//", "${I.sp[3]}天的")
                            .toPlainText()
                    }
                    return addVIPSucc
                        .replace("//id//", I.sp[2])
                        .replace("//serverCount//", "$serverCount")
                        .replace("//Time//", "")
                        .toPlainText()
                } else {
                    refreshServerInfo(I.event.group.id)
                    return addVIPErr.replace("//id//", I.sp[2]).replace("//err//", serverVIP.reqBody).toPlainText()
                }
            }
        }
        return nullServerErr.replace("//err//", "第${serverCount}个").toPlainText()
    }

    //TODO 移除VIP人实现
    private fun unVipPlayer(I: PullIntent, re: Boolean = false): Message {
        if (!I.isAdmin) return notAdminErr.toPlainText()
        if (I.cmdSize < 2) return parameterErr.replace("//para//", "*rv <ServerCount> <ID>").toPlainText()
        if (isNullServer(I.event.group.id)) return nullServerErr.toPlainText()
        if (re) refreshServerInfo(I.event.group.id)
        val serverCount = I.sp[1].toInt()
        groupData[I.event.group.id]?.server?.forEachIndexed { index, it ->
            if (index + 1 == serverCount) {
                if (it.gameID.isNullOrEmpty() || it.serverRspID == 0) {
                    if (re) return serverInfoRErr.toPlainText()
                    unVipPlayer(I, re = true)
                    return serverInfoRefreshing.toPlainText()
                }
                val pid = getPersonaid(I.sp[2])
                if (!pid.isSuccessful) {
                    if (re) return serverInfoRErr.toPlainText()
                    unVipPlayer(I, true)
                    return serverInfoRefreshing.toPlainText()
                }
                val serverVIP = removeServerVIP(it.sessionId.toString(), it.serverRspID, pid.id.toString())
                if (serverVIP.isSuccessful) {
                    removeVip(I.event.group.id, serverCount, I.sp[2])
                    return unVIPSucc.replace("//id//", I.sp[2]).replace("//serverCount//", "$serverCount").toPlainText()
                } else {
                    if (re) return serverInfoRErr.toPlainText()
                    unVipPlayer(I, true)
                    return unVIPErr.replace("//id//", I.sp[2]).replace("//err//", serverVIP.reqBody).toPlainText()
                }
            }
        }
        return nullServerErr.replace("//err//", "第${serverCount}个").toPlainText()
    }

    //TODO 查询VIP实现
    private fun getVipList(I: PullIntent): Message {
        /*var serverName = ""
        ServerNameData.forEach { (t, u) ->
            if (t == I.event.group.id) {
                serverName = u
            }
        }
        if (serverName.isEmpty()) return "未定义默认服务器名称".toPlainText()
        val serverID = BF1Api.searchServer(serverName)
        if (!serverID.isSuccessful) return "查找服务器失败".toPlainText()
        val exc: HashMap<String, String> = getGameIDList(I.event.group.id, serverID)
        val fullServerInfoJson: MutableList<FullServerInfoJson> = mutableListOf()
        exc.forEach { (t, u) ->
            fullServerInfoJson.add(getFullServerDetails(u, t))
        }*/
        if (I.cmdSize < 2) return parameterErr.replace("//para//", "*gv <ServerCount>").toPlainText()
        val serverCount = I.sp[1].toInt()
        val serverSize = groupData[I.event.group.id]?.server?.size
        var p = "服务器${serverCount}的临时VIP:\n"
        if (serverSize != null) {
            if (serverSize >= serverCount) {
                groupData[I.event.group.id]!!.server.forEachIndexed { index, it ->
                    if (index + 1 == serverCount) {
                        it.vipList.forEach { id, endTime ->
                            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            val sd = sdf.format(Date(endTime.toLong()))
                            p += "ID:${id} 到期时间:${sd}\n"
                        }
                    }
                }
            }
        }
        return p.toPlainText()
    }

    //TODO 绑定服务器实现
    private fun bindingServer(I: PullIntent): Message {
        if (I.isAdmin) {
            when (I.cmdSize) {
                in 1..2 -> {
                    var temp = "绑定的服务器\n"
                    groupData[I.event.group.id]?.server?.forEachIndexed { index, it ->
                        temp += """
                            Server:${index + 1}:
                            ServerName:${it.serverName}
                            ServerGuid:${it.serverGuid}
                            GameID:${it.gameID}
                        """.trimIndent() + "\n"
                    }
                    return temp.toPlainText()
                }

                in 3..5 -> {
                    when (I.sp[1]) {
                        "add" -> {
                            return if (I.sp[2].isNotEmpty() && I.sp[3].isNotEmpty()) {
                                if (groupData[I.event.group.id]?.server?.add(
                                        ServerInfoForSave(
                                            serverGuid = I.sp[2],
                                            serverName = I.sp[3]
                                        )
                                    ) == true
                                ) {
                                    "成功".toPlainText()
                                } else {
                                    "已存在该绑定".toPlainText()
                                }
                            } else {
                                parameterErr.replace("//para//", "*bds add <ServerID> <ServerName>").toPlainText()
                            }
                        }

                        "remove" -> {
                            return if (I.sp[2].isNotEmpty()) {
                                groupData[I.event.group.id]?.server?.removeIf {
                                    it.serverGuid == I.sp[2]
                                }
                                "成功".toPlainText()
                            } else {
                                parameterErr.replace("//para//", "*bds remove <ServerID>").toPlainText()
                            }
                        }

                        else -> return parameterErr.replace("//para//", "*bds <add/remove/list>").toPlainText()
                    }
                }

                else -> {
                    return errCommand.replace("//err//", "").toPlainText()
                }
            }
        } else {
            return notAdminErr.toPlainText()
        }
    }

    //TODO 绑定服务器ssid
    private fun bindingServerSessionId(I: PullIntentTemp): Message {
        if (I.isAdmin) {
            if (I.cmdSize > 2) {
                val apiLocale = setAPILocale(I.sp[2])
                if (!apiLocale.isSuccessful) return "失败请重试\n${apiLocale.reqBody}".toPlainText()
                val ssid = getWelcomeMessage(I.sp[2])
                if (!ssid.isSuccessful) return "失败请重试\n${apiLocale.reqBody}".toPlainText()
                return if (I.sp[1] == "All") {
                    setBindServer(I.event.group.id, "All", I.sp[2])
                    "绑定成功\n${ssid.firstMessage}".toPlainText()
                } else {
                    setBindServer(I.event.group.id, I.sp[1], I.sp[2])
                    "绑定成功\n${ssid.firstMessage}".toPlainText()
                }
            } else {
                return parameterErr.replace("//para//", "*bdssid <ServerID> <SessionID>").toPlainText()
            }
        } else {
            return notAdminErr.toPlainText()
        }
    }

    //TODO 修改绑定服务器
    private fun setBindServer(gid: Long, serverID: String, sessionId: String = ""): Boolean {
        groupData[gid]?.server?.forEach {
            if (serverID == "All") {
                it.sessionId = sessionId
            }
            if (it.serverGuid == serverID) {
                it.sessionId = sessionId
            }
        }
        return true
    }

    //TODO 修改服务器kd实现
    private fun setKDInfo(I: PullIntent): Message {
        if (!I.isAdmin) return notAdminErr.toPlainText()
        if (I.cmdSize < 4) return parameterErr.replace("//para//", "*setkd <ServerCount> <lkd/lkp/rkd/rkp> <Float>")
            .toPlainText()
        if (isNullServer(I.event.group.id)) return nullServerErr.replace("//err//", "").toPlainText()
        val serverCount = I.sp[1].toInt()
        when (I.sp[2]) {
            "lkd" -> setKD(I.event.group.id, serverCount, lifeMaxKD = I.sp[3].toFloat())
            "lkp" -> setKD(I.event.group.id, serverCount, lifeMaxKPM = I.sp[3].toFloat())
            "rkp" -> setKD(I.event.group.id, serverCount, recentlyMaxKPM = I.sp[3].toFloat())
            "rkd" -> setKD(I.event.group.id, serverCount, recentlyMaxKD = I.sp[3].toFloat())
            else -> return parameterErr.replace("//para//", "*setkd <ServerCount> <lkd/lkp/rkd/rkp> <Float>")
                .toPlainText()
        }
        return "成功".toPlainText()
    }

    //TODO 修改服务器kd
    private fun setKD(
        gid: Long,
        serverCount: Int,
        recentlyMaxKD: Float = 0F,
        recentlyMaxKPM: Float = 0F,
        lifeMaxKD: Float = 0F,
        lifeMaxKPM: Float = 0F,
    ): Boolean {
        groupData[gid]?.server?.forEachIndexed { index, it ->
            if (serverCount == index + 1) {
                if (lifeMaxKD > 0)
                    it.lifeMaxKD = lifeMaxKD
                if (lifeMaxKPM > 0)
                    it.lifeMaxKPM = lifeMaxKPM
                if (recentlyMaxKPM > 0)
                    it.recentlyMaxKPM = recentlyMaxKPM
                if (recentlyMaxKD > 0)
                    it.lifeMaxKD = recentlyMaxKD
            }
        }
        return true
    }

    //TODO 修改服务器抗压白名单实现
    private fun wl(I: PullIntent): Message {
        if (!I.isAdmin) return notAdminErr.toPlainText()
        if (isNullServer(I.event.group.id)) return nullServerErr.replace("//err//", "").toPlainText()
        if (I.cmdSize < 2) return groupData[I.event.group.id]?.recentlyTempWhitelist.toString().toPlainText()
        return if (addWl(I.event.group.id, I.sp[1])) {
            "添加白名单成功".toPlainText()
        } else {
            "移除白名单成功".toPlainText()
        }
    }

    //TODO 修改服务器抗压白名单
    private fun addWl(
        gid: Long,
        eaid: String,
    ): Boolean {
        var temp = ""
        groupData[gid]?.recentlyTempWhitelist?.forEach {
            if (eaid == it) temp = it
        }
        return if (temp.isNotEmpty()) {
            groupData[gid]?.recentlyTempWhitelist?.removeIf {
                it == eaid
            }
            false
        } else {
            groupData[gid]?.recentlyTempWhitelist?.add(eaid)
            true
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
