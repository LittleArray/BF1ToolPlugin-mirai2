package top.ffshaozi.intent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import top.ffshaozi.BF1ToolPlugin
import top.ffshaozi.config.CustomerLang
import top.ffshaozi.config.Setting
import top.ffshaozi.config.SettingController
import top.ffshaozi.utils.*
import java.io.File
import java.text.SimpleDateFormat


/*** 搜索实现
 * @Description
 * @Author littleArray
 * @Date 2023/9/4
 */
object EnquiryService {
    //TODO 绑定实现
    fun bindingUser(I: PullIntent): Message {
        return if (I.cmdSize > 1) {//绑定操作
            SettingController.addBinding(I.event.group.id, I.event.sender.id, I.sp[1])
            PlainText(CustomerLang.bindingSucc.replace("//id//", I.sp[1]))
        } else {
            //解绑操作
            val temp = SettingController.removeBinding(I.event.group.id, I.event.sender.id)
            if (temp != null) {
                PlainText(CustomerLang.unbindingSucc.replace("//id//", I.event.sender.nameCardOrNick))
            } else {
                PlainText(CustomerLang.unbindingErr)
            }
        }
    }

    //TODO 查询自己实现
    fun searchMe(I: PullIntent): Message {
        var id = SettingController.getBinding(I.event.group.id, I.event.sender.id)
        if (I.cmdSize > 1) id = I.sp[1]
        if (id.isEmpty()) {
            id = I.event.sender.nameCard
            SettingController.addBinding(I.event.group.id, I.event.sender.id, id)
            Intent.sendMsg(I, CustomerLang.unbindingErr.replace("//id//", id))
        }
        //查询
        Intent.sendMsg(I, CustomerLang.searching.replace("//id//", id).replace("//action//", "基础数据"))
        val tempStatsJson = BF1Api.getStats(id)
        return if (tempStatsJson.isSuccessful) {
            PlainText(
                """
                            ID:${tempStatsJson.userName} 
                            Lv.${tempStatsJson.rank} 经验条:${tempStatsJson.currentRankProgress.toFloat() / tempStatsJson.totalRankProgress.toFloat() * 100}%
                            PID:${tempStatsJson.id}
                            KPM:${tempStatsJson.killsPerMinute} SPM:${tempStatsJson.scorePerMinute} 
                            KD:${tempStatsJson.killDeath} 胜率:${tempStatsJson.winPercent}
                            死亡:${tempStatsJson.deaths} 击杀:${tempStatsJson.kills}
                            扎人数:${tempStatsJson.revives} 治疗数:${tempStatsJson.heals}
                            修理数:${tempStatsJson.repairs} 狗牌数:${tempStatsJson.dogtagsTaken}
                            复仇数:${tempStatsJson.avengerKills} 协助击杀:${tempStatsJson.killAssists}
                            命中率:${tempStatsJson.accuracy} 爆头率:${tempStatsJson.headshots}
                            技巧值:${tempStatsJson.skill}
                            最远击杀距离:${tempStatsJson.longestHeadShot}m
                            最高连续击杀:${tempStatsJson.highestKillStreak}
                            游玩时长:${tempStatsJson.secondsPlayed.toInt() / 60 / 60}h
                            最佳兵种:${tempStatsJson.bestClass}
                        """.trimIndent()
            )

        } else {
            PlainText(CustomerLang.searchErr.replace("//action//", "基础数据"))
        }
    }

    //TODO 查询BFEAC的实现
    fun searchEACBan(I: PullIntent): Message {
        var id = SettingController.getBinding(I.event.group.id, I.event.sender.id)
        if (I.cmdSize > 1) id = I.sp[1]
        if (id.isEmpty()) {
            id = I.event.sender.nameCard
            SettingController.addBinding(I.event.group.id, I.event.sender.id, id)
            Intent.sendMsg(I, CustomerLang.unbindingErr.replace("//id//", id))
        }
        //查询
        Intent.sendMsg(I, CustomerLang.searching.replace("//id//", id).replace("//action//", "EACBan数据"))
        val eacInfoJson = BF1Api.seaechBFEAC(id)
        if (eacInfoJson.error_code != 0) return PlainText(CustomerLang.nullEac.replace("//id//", id))
        if (eacInfoJson.data.isNullOrEmpty()) return PlainText(CustomerLang.nullEac.replace("//id//", id))
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
    fun searchRecently(I: PullIntent): Message {
        var id = SettingController.getBinding(I.event.group.id, I.event.sender.id)
        if (I.cmdSize > 1) id = I.sp[1]
        if (id.isEmpty()) {
            id = I.event.sender.nameCard
            SettingController.addBinding(I.event.group.id, I.event.sender.id, id)
            Intent.sendMsg(I, CustomerLang.unbindingErr.replace("//id//", id))
        }
        //查询
        Intent.sendMsg(I, CustomerLang.searching.replace("//id//", id).replace("//action//", "最近数据"))
        val recentlyJson = BF1Api.recentlySearch(id)
        val hashMap = BF1Api.recentlyServerSearch(id)
        var temp = "${id}的最近数据\n"
        recentlyJson.forEachIndexed() { index, it ->
            if (index + 1 > 2) return@forEachIndexed
            if (!it.isSuccessful) return PlainText(CustomerLang.searchErr.replace("//action//", "最近数据"))
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
    fun searchWp(I: PullIntent, type: String? = null): Message {
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
        var id = SettingController.getBinding(I.event.group.id, I.event.sender.id)
        if (I.cmdSize > 1) id = I.sp[1]
        if (id.isEmpty()) {
            id = I.event.sender.nameCard
            SettingController.addBinding(I.event.group.id, I.event.sender.id, id)
            Intent.sendMsg(I, CustomerLang.unbindingErr.replace("//id//", id))
        }
        //查询
        Intent.sendMsg(I, CustomerLang.searching.replace("//id//", id).replace("//action//", "武器数据"))
        val tempWeaponsJson = BF1Api.getWeapon(id)
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
                            击杀数:${it.kills} ${it.kills / 100}☆
                            爆头数:${it.headshotKills}
                            爆头率:${it.headshots}
                            命中率:${it.accuracy}
                            KPM:${it.killsPerMinute}
                            时长:${it.timeEquipped / 60 / 60}h
                            类型:${it.type}
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
                            击杀数:${it.kills} ${it.kills / 100}☆
                            爆头数:${it.headshotKills}
                            爆头率:${it.headshots}
                            命中率:${it.accuracy}
                            KPM:${it.killsPerMinute}
                            时长:${it.timeEquipped / 60 / 60}h
                            类型:${it.type}
                                            """.trimIndent()
                                )
                            )
                            index++
                        }
                    }
                }
            } else {
                add(
                    I.event.bot.id,
                    I.event.bot.nick,
                    PlainText(CustomerLang.searchErr.replace("//action//", "武器数据"))
                )
            }
        }
        return message
    }

    //TODO 查询载具实现
    fun searchVehicle(I: PullIntent): Message {
        var id = SettingController.getBinding(I.event.group.id, I.event.sender.id)
        if (I.cmdSize > 1) id = I.sp[1]
        if (id.isEmpty()) {
            id = I.event.sender.nameCard
            SettingController.addBinding(I.event.group.id, I.event.sender.id, id)
            Intent.sendMsg(I, CustomerLang.unbindingErr.replace("//id//", id))
        }
        //查询
        Intent.sendMsg(I, CustomerLang.searching.replace("//id//", id).replace("//action//", "载具数据"))
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
                            时长:${it.timeIn / 60 / 60}h
                            击杀数:${it.kills} ${it.kills / 100}☆
                            摧毁数:${it.destroyed}
                            KPM:${it.killsPerMinute}
                        """.trimIndent().toPlainText()
                    )
                }
            } else {
                add(
                    I.event.bot.id,
                    I.event.bot.nick,
                    CustomerLang.searchErr.replace("//action//", "载具数据").toPlainText()
                )
            }
        }
        return message
    }

    //TODO 查询服务器实现
    fun searchServer(I: PullIntent): Message {
        return if (I.cmdSize > 1) {
            Intent.sendMsg(I, CustomerLang.searchingSer.replace("//ser//", I.sp[1]))
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
                CustomerLang.searchErr.replace("//action//", "服务器").toPlainText()
            }
        } else {
            CustomerLang.parameterErr.replace("//para//", "*ss <ServerName>").toPlainText()
        }
    }

    //TODO 查询服务器玩家列表的实现
    fun searchServerList(I: PullIntent, re: Boolean = false): Message {
        if (I.cmdSize < 2) return CustomerLang.parameterErr.replace("//para//", "*pl <ServerCount>").toPlainText()
        if (SettingController.isNullServer(I.event.group.id)) return CustomerLang.nullServerErr.replace("//err//", "")
            .toPlainText()
        if (re) SettingController.refreshServerInfo(I.event.group.id)
        val index = I.sp[1].toInt()
        var msg: Message? = null
        val team1 = ForwardMessageBuilder(I.event.group)
        team1.add(I.event.bot, PlainText("队伍1"))
        val team2 = ForwardMessageBuilder(I.event.group)
        team2.add(I.event.bot, PlainText("队伍2"))
        val blackTeam = ForwardMessageBuilder(I.event.group)
        blackTeam.add(I.event.bot, PlainText("黑队查询"))
        CycleTask.serverInfoIterator { groupID, data, serverCount, serverInfoForSave ->
            run p@{
                if (index == serverCount) {
                    if (serverInfoForSave.gameID.isNullOrEmpty()) {
                        if (re) {
                            msg = CustomerLang.serverInfoRErr.toPlainText()
                            return@p
                        }
                        searchServerList(I, re = true)
                        msg = CustomerLang.serverInfoRefreshing.toPlainText()
                        return@p
                    }
                    val gameid = serverInfoForSave.gameID!!
                    val groupid = I.event.group.id
                    var groupPlayer = 0
                    var opPlayer = 0
                    var blackPlayer = 0
                    var loadingBots = 0
                    var teamOne = ""
                    var teamOneName = ""
                    var team1Index = 0
                    var teamTwo = ""
                    var teamTwoName = ""
                    var team2Index = 0
                    val text = readIt("playerList")
                    //background-color: rgb(86, 196, 73);
                    val player =
                        "<div class=\"player\"><div class=\"index\">INDEX</div><div class=\"rank\"  style=\"rankback\">RANK</div><div style=\"color: #fff;id\" class=\"name\">NAME</div><div style=\"color: #fff;lkd\" class=\"lifeKD\">LIFE_KD</div><div style=\"color: #fff;lkp\" class=\"lifeKPM\">LIFE_KPM</div><div style=\"color: #fff;rkd\" class=\"RKD\">R_KD</div><div style=\"color: #fff;rkp\" class=\"RKPM\">R_KPM</div><div class=\"time\">TIME分</div><div class=\"latency\">PINGms</div></div>"
                    val platoonSet: MutableSet<String> = mutableSetOf()
                    val blackTeamSet: MutableSet<String> = mutableSetOf()
                    Cache.PlayerListInfo.forEach { gameID, players ->
                        if (gameid == gameID) {
                            players.forEach { (id, data) ->
                                if (data.platoon.isNotEmpty()) {
                                    platoonSet.forEach {
                                        if (it == data.platoon) {
                                            blackTeamSet.add(it)
                                        }
                                    }
                                    platoonSet.add(data.platoon)
                                }
                            }
                        }
                    }
                    Cache.PlayerListInfo.forEach { gameID, players ->
                        if (gameid == gameID)
                            players.forEach { (id, it) ->
                                var pa = ""
                                if (it.platoon.isNotEmpty())
                                    pa = "[ ${it.platoon} ] "

                                var color = "#fff"
                                var colorlkd = "#fff"
                                var colorlkp = "#fff"
                                var colorrkp = "#fff"
                                var colorrkd = "#fff"
                                if (it.lkd > serverInfoForSave.lifeMaxKD * 0.6) colorlkd = "#ff0"
                                if (it.lkp > serverInfoForSave.lifeMaxKPM * 0.6) colorlkp = "#ff0"
                                if (it.rkp > serverInfoForSave.recentlyMaxKPM * 0.6) colorrkp = "#ff0"
                                if (it.rkd > serverInfoForSave.recentlyMaxKD * 0.6) colorrkd = "#ff0"
                                if (it.lkd > serverInfoForSave.lifeMaxKD * 0.8) colorlkd = "#ff6600"
                                if (it.lkp > serverInfoForSave.lifeMaxKPM * 0.8) colorlkp = "#ff6600"
                                if (it.rkp > serverInfoForSave.recentlyMaxKPM * 0.8) colorrkp = "#ff6600"
                                if (it.rkd > serverInfoForSave.recentlyMaxKD * 0.8) colorrkd = "#ff6600"
                                if (it.isBot) {
                                    color = "aqua"
                                    if (it.botState == "Loading") loadingBots++
                                }
                                blackTeamSet.forEach { pa ->
                                    if (it.platoon == pa) {
                                        if (!it.isBot){
                                            color = "chartreuse"
                                            blackPlayer++
                                        }
                                    }
                                }
                                Setting.groupData[groupid]?.bindingData?.forEach {
                                    if (it.value == id) {
                                        color = "pink"
                                        groupPlayer++
                                    }
                                }
                                run o@{
                                    Setting.groupData[groupid]?.bindingData?.forEach {
                                        Setting.groupData[groupid]?.operator?.forEach { qq ->
                                            if (it.key == qq && it.value == id) {
                                                color = "#f9767b"
                                                opPlayer++
                                                return@o
                                            }
                                        }
                                    }
                                }
                                if (it.teamId == 1) {
                                    teamOneName = it.team
                                    team1Index++
                                    teamOne += player
                                        .replace("NAME", "$pa$id")
                                        .replace("RANK", it.rank.toString())
                                        .replace("INDEX", team1Index.toString())
                                        .replace(
                                            "TIME",
                                            "${(System.currentTimeMillis() - (it.join_time / 1000)) / 1000 / 60}"
                                        )
                                        .replace("PING", "${it.latency}")
                                        .replace("LIFE_KD", "${it.lkd}")
                                        .replace("LIFE_KPM", "${it.lkp}")
                                        .replace("R_KD", "${it.rkd}")
                                        .replace("R_KPM", "${it.rkp}")
                                        .replace("color: #fff;id", "color: ${color};")
                                        .replace("color: #fff;lkd", "color: ${colorlkd};")
                                        .replace("color: #fff;lkp", "color: ${colorlkp};")
                                        .replace("color: #fff;rkp", "color: ${colorrkp};")
                                        .replace("color: #fff;rkd", "color: ${colorrkd};")
                                        .replace("rankback",if (it.rank >120)"background-color: rgb(86, 196, 73);" else "")
                                } else {
                                    teamTwoName = it.team
                                    team2Index++
                                    teamTwo += player
                                        .replace("NAME", "$pa$id")
                                        .replace("RANK", it.rank.toString())
                                        .replace("INDEX", team2Index.toString())
                                        .replace(
                                            "TIME",
                                            "${(System.currentTimeMillis() - (it.join_time / 1000)) / 1000 / 60}"
                                        )
                                        .replace("PING", "${it.latency}")
                                        .replace("LIFE_KD", "${it.lkd}")
                                        .replace("LIFE_KPM", "${it.lkp}")
                                        .replace("R_KD", "${it.rkd}")
                                        .replace("R_KPM", "${it.rkp}")
                                        .replace("color: #fff;id", "color: ${color};")
                                        .replace("color: #fff;lkd", "color: ${colorlkd};")
                                        .replace("color: #fff;lkp", "color: ${colorlkp};")
                                        .replace("color: #fff;rkp", "color: ${colorrkp};")
                                        .replace("color: #fff;rkd", "color: ${colorrkd};")
                                        .replace("rankback",if (it.rank >120)"background-color: rgb(86, 196, 73);" else "")
                                }
                            }
                    }
                    var mapName = Cache.ServerInfoList[gameid]!!.map
                    Cache.mapCache.forEach {
                        if (Cache.ServerInfoList[gameid]!!.map == it.key)
                            mapName = it.value
                    }
                    var modeName = Cache.ServerInfoList[gameid]!!.mode
                    Cache.modeCache.forEach {
                        if (Cache.ServerInfoList[gameid]!!.mode == it.key)
                            modeName = it.value
                    }
                    val tOimg = teamOneName.replace(" ","_")
                    val tTimg = teamTwoName.replace(" ","_")
                    var teamOneImg = ""
                    var teamTwoImg = ""
                    if (BF1Api.getImg(Cache.ServerInfoList[gameid]!!.teamOneImgUrl,tOimg,true)) {
                        teamOneImg = getImgPath(tOimg)
                    }
                    if (BF1Api.getImg(Cache.ServerInfoList[gameid]!!.teamTwoImgUrl,tTimg,true)) {
                        teamTwoImg = getImgPath(tTimg)
                    }
                    val res = text
                        .replace("Team1Replace", teamOne)
                        .replace("Team2Replace", teamTwo)
                        .replace("-DPREFIX", Cache.ServerInfoList[gameid]!!.perfix)
                        .replace("-DTEAM1PIC", teamOneImg)
                        .replace("-DTEAM2PIC", teamTwoImg)
                        .replace("-DMODE", modeName)
                        .replace("-DMAP", mapName)
                        .replace("-DMP_back", Cache.ServerInfoList[gameid]!!.map)
                        .replace("-DTIME", SimpleDateFormat("MM-dd HH:mm:ss").format(Cache.ServerInfoList[gameid]!!.cacheTime))
                        .replace("-DLBPL", Cache.ServerInfoList[gameid]!!.oldPlayers.toString())
                        .replace("-DBOPL", Cache.ServerInfoList[gameid]!!.bots.toString())
                        .replace("-DGOPL", groupPlayer.toString())
                        .replace("-DOPPL", opPlayer.toString())
                        .replace("-BOTSL", loadingBots.toString())
                        .replace("-DBLPL", blackPlayer.toString())
                        .replace("-DTEAM1NAME", teamOneName)
                        .replace("-DTEAM2NAME", teamTwoName)
                        .replace("-DP", Cache.ServerInfoList[gameid]!!.players.toString())
                        .replace("-DGameID", gameid)
                        .replace("-DGameID", gameid)

                    writeTempFile("playerList", res)
                    htmlToImage("playerList")
                    CoroutineScope(Dispatchers.IO).launch {
                        I.event.subject.sendImage(File(getFilePath("playerList")))
                    }
                }
            }
        }
        return "OK".toPlainText()
    }

    //TODO 搜索服务器玩家
    fun searchServerListPlayer(I: PullIntent, re: Boolean = false): Message {
        if (I.cmdSize < 3) return CustomerLang.parameterErr.replace("//para//", "*ssi <ServerCount> <ID>").toPlainText()
        if (SettingController.isNullServer(I.event.group.id)) return CustomerLang.nullServerErr.replace("//err//", "")
            .toPlainText()
        if (re) SettingController.refreshServerInfo(I.event.group.id)
        val serverCount = I.sp[1].toInt()
        Setting.groupData[I.event.group.id]?.server?.forEachIndexed { index, it ->
            if (index + 1 == serverCount) {
                if (it.gameID.isNullOrEmpty()) {
                    if (re) return CustomerLang.serverInfoRErr.toPlainText()
                    searchServerList(I, re = true)
                    return CustomerLang.serverInfoRefreshing.toPlainText()
                }
                Intent.sendMsg(I, CustomerLang.searching.replace("//id//", I.sp[2]).replace("//action//", "服务器玩家"))
                val serverListJson = BF1Api.searchServerList(it.gameID!!)
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
                    CustomerLang.searchErr.replace("//action//", "服务器玩家").toPlainText()
                }
            }
        }
        return CustomerLang.nullServerErr.replace("//err//", "第${serverCount}个").toPlainText()
    }
}