package top.ffshaozi.intent

import kotlinx.coroutines.*
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.data.*
import top.ffshaozi.config.*
import top.ffshaozi.utils.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


/*** 搜索实现
 * @Description
 * @Author littleArray
 * @Date 2023/9/4
 */
object EnquiryService {
    //TODO 绑定实现
    fun bindingUser(I: PullIntent): Message {
        return if (I.cmdSize > 1) {//绑定操作
            Bindings.addBinding(I.event.group.id, I.event.sender.id, I.sp[1])
            PlainText(CustomerLang.bindingSucc.replace("//id//", I.sp[1]))
        } else {
            //解绑操作
            val temp = Bindings.removeBinding(I.event.group.id, I.event.sender.id)
            if (temp != null) {
                PlainText(CustomerLang.unbindingSucc.replace("//id//", I.event.sender.nameCardOrNick))
            } else {
                PlainText(CustomerLang.unbindingErr)
            }
        }
    }

    fun bf1(I: PullIntent): Message {
        val bF1Json = BF1Api.searchBF1()
        if (bF1Json.isSuccessful) {
            var temp = "战地1当前活跃度\n"
            bF1Json.regions?.forEach { reg, data ->
                temp += """
                    地区:${reg} 
                    --私服数量:${data.amounts.communityServerAmount}
                    --官服数量:${data.amounts.diceServerAmount}
                    --玩家数量:${data.amounts.soldierAmount}
                    --观战数量:${data.amounts.spectatorAmount}
                """.trimIndent() + "\n"
            }
            return temp.toPlainText()
        } else {
            return PlainText(CustomerLang.searchErr.replace("//action//", "战地一活跃度"))
        }
    }

    //TODO 查询自己实现
    suspend fun searchMe(I: PullIntent): Message {
        var id = Bindings.getBinding(I.event.group.id, I.event.sender.id)
        if (I.cmdSize > 1) id = I.sp[1]
        if (I.cmdSize > 2) BackgroundImgData.addImgOrDef(I.event.sender.id,"stats",I.sp[2])
        if (id.isEmpty()) {
            id = I.event.sender.nameCard
            Bindings.addBinding(I.event.group.id, I.event.sender.id, id)
            Intent.sendMsg(I, CustomerLang.unbindingErr.replace("//id//", id))
        }
        //查询
        Intent.sendMsg(I, CustomerLang.searching.replace("//id//", id).replace("//action//", "基础数据"))
        val name = id
        val allStats = BF1Api.getAllStats(name)
        if (allStats.isSuccessful) {
            val backImg = BackgroundImgData.backgroundImgData[I.event.sender.id]?.statsImg ?: "assets/MP_Blitz.jpg"
            val htmlToImage = HtmlToImage()
            val content = htmlToImage.readIt("stats")
            var res = content
                .replace("-DNAME", name)
                .replace("-DRANK", allStats.rank.toString())
                .replace("-DCPRO", allStats.currentRankProgress.toString())
                .replace("-DTPRO", allStats.totalRankProgress.toString())
                .replace("-DGTIME", "${allStats.secondsPlayed?.div(60)?.div(60)}")
                .replace("-DBEST", "${allStats.bestClass}")
                .replace(
                    "-DKILLS",
                    if (allStats.kills != null && allStats.kills > 10000) "${allStats.kills.div(100)} ★" else "${allStats.kills}"
                )
                .replace(
                    "-DDEATH",
                    if (allStats.deaths != null && allStats.deaths > 10000) "${allStats.deaths.div(100)} ★" else "${allStats.deaths}"
                )
                .replace("-DLKD", "${allStats.killDeath}")
                .replace("-DLKPM", "${allStats.killsPerMinute}")
                .replace("-DREVIVES", "${allStats.revives}")
                .replace("-DHEALS", "${allStats.heals}")
                .replace("-Drepairs", "${allStats.repairs}")
                .replace("-DdogtagsTaken", "${allStats.dogtagsTaken}")
                .replace("-DavengerKills", "${allStats.avengerKills}")
                .replace("-Dskill", "${allStats.skill}")
                .replace("-Daccuracy", "${allStats.accuracy}")
                .replace("-Dheadshots", "${allStats.headshots}")
                .replace("-DkillAssists", "${allStats.killAssists}")
                .replace("-DhighestKillStreak", "${allStats.highestKillStreak}")
                .replace("-DlongestHeadShotm", "${allStats.longestHeadShot}")
                .replace("-DACTP", "${allStats.activePlatoon?.name}")
                .replace("-DGENTIME", SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()))
                .replace("assets/MP_Blitz.jpg", backImg)
            if (allStats.activePlatoon != null) {
                if (allStats.activePlatoon.emblem != null && allStats.activePlatoon.tag != null) {
                    val cacheImg =
                        htmlToImage.cacheImg(allStats.activePlatoon.emblem, "Platoon_${allStats.activePlatoon.tag}")
                    res = if (cacheImg) {
                        res.replace("-DPROFILEP", htmlToImage.getImgPath()).replace("-DPO", allStats.activePlatoon.tag)
                    } else {
                        htmlToImage.cacheImg(allStats.activePlatoon.emblem, "Avatar_Def")
                        res.replace("-DPROFILEP", htmlToImage.getImgPath()).replace("-DPO", allStats.activePlatoon.tag)
                    }
                } else {
                    //头像缓存
                    if (allStats.avatar != null) {
                        val cacheImg = htmlToImage.cacheImg(allStats.avatar, "Avatar_${name}")
                        res = if (cacheImg) {
                            res.replace("-DPROFILEP", htmlToImage.getImgPath()).replace("[-DPO]", "")
                        } else {
                            htmlToImage.cacheImg(allStats.avatar, "Avatar_Def")
                            res.replace("-DPROFILEP", htmlToImage.getImgPath()).replace("[-DPO]", "")
                        }
                    }
                }
            }
            val wpModel = """
            <div class="item">
                <p>NAME - STATS</p>
                <img src="IMG">
                <span>
                    <p>擊殺:KILLS</p>
                    <p>KPM:-DKPM</p>
                    <p>效率:-DVP</p>
                </span>
                <span>
                    <p>精确率:-DAC</p>
                    <p>爆头率:-DHS</p>
                    <p>时长:-DTPh</p>
                </span>
            </div>
        """.trimIndent()
            val vpModel = """
            <div class="item">
                <p>NAME - STATS</p>
                <img src="IMG">
                <span>
                    <p>擊殺:KILLS</p>
                    <p>KPM:-DKPM</p>
                    <p>摧毁:-DS</p>
                </span>
                <p>时长:-DTPh</p>
            </div>
        """.trimIndent()
            val classModel = """
                <span>
                    <img src="IMG" style="height: 16px;">
                    <p>Name</p>
                    <p>K:kills</p>
                    <p>KPM:kpms</p>
                    <p>T:timePlayh</p>
                </span>
            """.trimIndent()
            val gameWinModel = """
                <span>
                    <p>NAME</p>
                    <p>P:WINP</p>
                    <p>W:WINS</p>
                    <p>L:LOSSES</p>
                </span>
            """.trimIndent()
            var wpText = ""
            var classText = ""
            var vpText = ""
            var gameWinText = ""
            var plText = ""
            allStats.weapons?.sortedByDescending { weapons -> weapons.kills }?.forEachIndexed { index, weapon ->
                if (index < 3) {
                    htmlToImage.cacheImg(weapon.image, "Weapon_${weapon.weaponName}")
                    wpText += wpModel
                        .replace("STATS", if (weapon.kills > 100) "${weapon.kills.div(100)} ★" else "0 ★")
                        .replace("KILLS", "${weapon.kills}")
                        .replace("NAME", weapon.weaponName)
                        .replace("-DKPM", weapon.killsPerMinute.toString())
                        .replace("-DVP", weapon.hitVKills.toString())
                        .replace("-DAC", weapon.accuracy)
                        .replace("-DHS", weapon.headshots)
                        .replace("-DTP", "${weapon.timeEquipped / 60 / 60}")
                        .replace("IMG", htmlToImage.getImgPath())
                }
            }
            allStats.vehicles?.sortedByDescending { vehicles -> vehicles.kills }?.forEachIndexed { index, vehicles ->
                if (index < 3) {
                    htmlToImage.cacheImg(vehicles.image, "Vehicles_${vehicles.vehicleName}")
                    vpText += vpModel
                        .replace("STATS", if (vehicles.kills > 100) "${vehicles.kills.div(100)} ★" else "0 ★")
                        .replace("KILLS", "${vehicles.kills}")
                        .replace("NAME", vehicles.vehicleName)
                        .replace("-DKPM", vehicles.killsPerMinute.toString())
                        .replace("-DS", vehicles.destroyed.toString())
                        .replace("-DTP", "${vehicles.timeIn / 60 / 60}")
                        .replace("IMG", htmlToImage.getImgPath())
                }
            }
            allStats.classes?.forEachIndexed { index, classes ->
                htmlToImage.cacheImg(classes.image, "Class_${classes.className}")
                classText += classModel
                    .replace("IMG", htmlToImage.getImgPath())
                    .replace("Name", classes.className)
                    .replace("kpms", classes.kpm.toString())
                    .replace("kills", if (classes.kills > 1000) "${classes.kills.div(100)} ★" else "${classes.kills}")
                    .replace("timePlay", "${classes.secondsPlayed / 60 / 60}")
            }
            allStats.gamemodes?.forEachIndexed { index, gamemodes ->
                gameWinText += gameWinModel
                    .replace("NAME", gamemodes.gamemodeName)
                    .replace("WINP", gamemodes.winPercent)
                    .replace("WINS", gamemodes.wins.toString())
                    .replace("LOSSES", gamemodes.losses.toString())
            }
            allStats.platoons?.forEachIndexed { index, platoon ->
                if (index < 4)
                    plText += "[${platoon.tag}]"
            }
            res = res.replace("WPTEXT", wpText).replace("VPTEXT", vpText).replace("CLTEXT", classText)
                .replace("GAMEWINTEXT", gameWinText).replace("-DPLIST", plText)
            var eacState = "無記錄"
            val eacInfoJson = BF1Api.searchBFEAC(name)
            if (eacInfoJson.error_code == 0) {
                if (!eacInfoJson.data.isNullOrEmpty()) {
                    eacState = when (eacInfoJson.data[0].current_status) {
                        0 -> "有记录但未处理\ncase/${eacInfoJson.data[0].case_id}"
                        1 -> "判定为石锤\ncase/${eacInfoJson.data[0].case_id}"
                        2 -> "判定为证据不足\ncase/${eacInfoJson.data[0].case_id}"
                        3 -> "判定为自证通过\ncase/${eacInfoJson.data[0].case_id}"
                        4 -> "判定为自证中\ncase/${eacInfoJson.data[0].case_id}"
                        5 -> "判定为刷枪\ncase/${eacInfoJson.data[0].case_id}"
                        else -> "未知判定\ncase/${eacInfoJson.data[0].case_id}"
                    }
                }
            }
            res = res.replace("-DeacState", eacState)
            val recentlyJson = BF1Api.recentlySearch(name)
            if (recentlyJson.isNotEmpty()) {
                res = res
                    .replace("-Dre_time", recentlyJson[0].rp)
                    .replace("-Dre_pt", recentlyJson[0].tp)
                    .replace("-Dre_spm", recentlyJson[0].spm)
                    .replace("-Dre_kpm", recentlyJson[0].kpm)
                    .replace("-Dre_kd", recentlyJson[0].kd)
            }
            htmlToImage.writeTempFile(res)
            htmlToImage.toImage(1280, 720)
            return I.event.subject.uploadImage(File(htmlToImage.getFilePath()))
        } else {
            return PlainText(CustomerLang.searchErr.replace("//action//", "基础数据"))
        }
    }

    //TODO 查询BFEAC的实现
    fun searchEACBan(I: PullIntent): Message {
        var id = Bindings.getBinding(I.event.group.id, I.event.sender.id)
        if (I.cmdSize > 1) id = I.sp[1]
        if (id.isEmpty()) {
            id = I.event.sender.nameCard
            Bindings.addBinding(I.event.group.id, I.event.sender.id, id)
            Intent.sendMsg(I, CustomerLang.unbindingErr.replace("//id//", id))
        }
        //查询
        Intent.sendMsg(I, CustomerLang.searching.replace("//id//", id).replace("//action//", "EACBan数据"))
        val eacInfoJson = BF1Api.searchBFEAC(id)
        if (eacInfoJson.error_code != 0) return PlainText(CustomerLang.nullEac.replace("//id//", id))
        if (eacInfoJson.data.isNullOrEmpty()) return PlainText(CustomerLang.nullEac.replace("//id//", id))
        return when (eacInfoJson.data[0].current_status) {
            0 -> PlainText("有记录但未处理\nLink:https://www.bfeac.com/?#/case/${eacInfoJson.data[0].case_id}")
            1 -> PlainText("判定为石锤\nLink:https://www.bfeac.com/?#/case/${eacInfoJson.data[0].case_id}")
            2 -> PlainText("判定为证据不足\nLink:https://www.bfeac.com/?#/case/${eacInfoJson.data[0].case_id}")
            3 -> PlainText("判定为自证通过\nLink:https://www.bfeac.com/?#/case/${eacInfoJson.data[0].case_id}")
            4 -> PlainText("判定为自证中\nLink:https://www.bfeac.com/?#/case/${eacInfoJson.data[0].case_id}")
            5 -> PlainText("判定为刷枪\nLink:https://www.bfeac.com/?#/case/${eacInfoJson.data[0].case_id}")
            else -> PlainText("未知判定\nLink:https://www.bfeac.com/?#/case/${eacInfoJson.data[0].case_id}")
        }
    }

    //TODO 最近实现
    fun searchRecently(I: PullIntent): Message {
        var id = Bindings.getBinding(I.event.group.id, I.event.sender.id)
        if (I.cmdSize > 1) id = I.sp[1]
        if (id.isEmpty()) {
            id = I.event.sender.nameCard
            Bindings.addBinding(I.event.group.id, I.event.sender.id, id)
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
    suspend fun searchWp(I: PullIntent, type: String? = null): Message {
        val typeI = when (type) {
            "霰弹枪" -> "霰彈槍"
            "轻机枪" -> "輕機槍"
            "机枪" -> "輕機槍"
            "配备" -> "配備"
            "半自动步枪" -> "半自動步槍"
            "配枪" -> "佩槍"
            "佩枪" -> "佩槍"
            "手枪" -> "佩槍"
            "副武器" -> "佩槍"
            "近战武器" -> "近戰武器"
            "手榴弹" -> "手榴彈"
            "步枪" -> "步槍"
            "战场装备" -> "戰場裝備"
            "驾驶员" -> "坦克/駕駛員"
            "制式步枪" -> "制式步槍"
            "冲锋枪" -> "衝鋒槍"
            else -> null
        }
        var id = Bindings.getBinding(I.event.group.id, I.event.sender.id)
        if (I.cmdSize > 1) id = I.sp[1]
        if (I.cmdSize > 2) BackgroundImgData.addImgOrDef(I.event.sender.id,"weapon",I.sp[2])
        if (id.isEmpty()) {
            id = I.event.sender.nameCard
            Bindings.addBinding(I.event.group.id, I.event.sender.id, id)
            Intent.sendMsg(I, CustomerLang.unbindingErr.replace("//id//", id))
        }
        //查询
        Intent.sendMsg(I, CustomerLang.searching.replace("//id//", id).replace("//action//", "武器数据"))
        val name = id
        val allStats = BF1Api.getAllStats(name)
        if (allStats.isSuccessful) {
            val backImg = BackgroundImgData.backgroundImgData[I.event.sender.id]?.weaponImg ?: "assets/MP_London.jpg"
            val htmlToImage = HtmlToImage()
            val content = htmlToImage.readIt("weapon")
            var res = content
                .replace("-DNAME", name)
                .replace("-DRANK", allStats.rank.toString())
                .replace("-DCPRO", allStats.currentRankProgress.toString())
                .replace("-DTPRO", allStats.totalRankProgress.toString())
                .replace("-DGTIME", "${allStats.secondsPlayed?.div(60)?.div(60)}")
                .replace("-DBEST", "${allStats.bestClass}")
                .replace("-DLKD", "${allStats.killDeath}")
                .replace("-DLKPM", "${allStats.killsPerMinute}")
                .replace("-DGENTIME", SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()))
                .replace("assets/MP_London.jpg", backImg)
                .replace(
                    "-DKILLS",
                    if (allStats.kills != null && allStats.kills > 10000) "${allStats.kills.div(100)} ★" else "${allStats.kills}"
                )
                .replace(
                    "-DDEATH",
                    if (allStats.deaths != null && allStats.deaths > 10000) "${allStats.deaths.div(100)} ★" else "${allStats.deaths}"
                )
            if (allStats.activePlatoon != null) {
                if (allStats.activePlatoon.emblem != null && allStats.activePlatoon.tag != null) {
                    val cacheImg =
                        htmlToImage.cacheImg(allStats.activePlatoon.emblem, "Platoon_${allStats.activePlatoon.tag}")
                    res = if (cacheImg) {
                        res.replace("-DPROFILEP", htmlToImage.getImgPath()).replace("-DPO", allStats.activePlatoon.tag)
                    } else {
                        htmlToImage.cacheImg(allStats.activePlatoon.emblem, "Avatar_Def")
                        res.replace("-DPROFILEP", htmlToImage.getImgPath()).replace("-DPO", allStats.activePlatoon.tag)
                    }
                } else {
                    //头像缓存
                    if (allStats.avatar != null) {
                        val cacheImg = htmlToImage.cacheImg(allStats.avatar, "Avatar_${name}")
                        res = if (cacheImg) {
                            res.replace("-DPROFILEP", htmlToImage.getImgPath()).replace("[-DPO]", "")
                        } else {
                            htmlToImage.cacheImg(allStats.avatar, "Avatar_Def")
                            res.replace("-DPROFILEP", htmlToImage.getImgPath()).replace("[-DPO]", "")
                        }
                    }
                }
            }
            val wpModel = """
            <div class="item">
                <p>NAME - STATS</p>
                <img src="IMG">
                <span>
                    <p>擊殺:KILLS</p>
                    <p>KPM:-DKPM</p>
                    <p>效率:-DVP</p>
                </span>
                <span>
                    <p>精确率:-DAC</p>
                    <p>爆头率:-DHS</p>
                    <p>时长:-DTPh</p>
                </span>
            </div>
        """.trimIndent()
            var line1 = ""
            var line2 = ""
            var line3 = ""
            var line4 = ""
            var weaponIndex = 0
            allStats.weapons?.sortedByDescending { weapons -> weapons.kills }?.forEach { weapon ->
                if (weapon.weaponName.indexOf("三八") != -1) {
                    weapon.type = "步槍"
                }
                if (typeI == weapon.type || typeI == null) {
                    weaponIndex++
                    htmlToImage.cacheImg(weapon.image, "Weapon_${weapon.weaponName}")
                    val temp = wpModel
                        .replace("STATS", if (weapon.kills > 100) "${weapon.kills.div(100)} ★" else "0 ★")
                        .replace("KILLS", "${weapon.kills}")
                        .replace("NAME", weapon.weaponName)
                        .replace("-DKPM", weapon.killsPerMinute.toString())
                        .replace("-DVP", weapon.hitVKills.toString())
                        .replace("-DAC", weapon.accuracy)
                        .replace("-DHS", weapon.headshots)
                        .replace("-DTP", "${weapon.timeEquipped / 60 / 60}")
                        .replace("IMG", htmlToImage.getImgPath())
                    when (weaponIndex) {
                        in 1..3 -> line1 += temp
                        in 4..6 -> line2 += temp
                        in 7..9 -> line3 += temp
                        in 10..12 -> line4 += temp
                    }
                }
            }
            res = res.replace("LINE1", line1).replace("LINE2", line2).replace("LINE3", line3).replace("LINE4", line4)
            htmlToImage.writeTempFile(res)
            htmlToImage.toImage(1280, 720)
            return I.event.subject.uploadImage(File(htmlToImage.getFilePath()))
        } else {
            return PlainText(CustomerLang.searchErr.replace("//action//", "武器数据"))
        }
    }


    //TODO 查询载具实现
    suspend fun searchVehicle(I: PullIntent): Message {
        var id = Bindings.getBinding(I.event.group.id, I.event.sender.id)
        if (I.cmdSize > 1) id = I.sp[1]
        if (I.cmdSize > 2) BackgroundImgData.addImgOrDef(I.event.sender.id,"vehicle",I.sp[2])
        if (id.isEmpty()) {
            id = I.event.sender.nameCard
            Bindings.addBinding(I.event.group.id, I.event.sender.id, id)
            Intent.sendMsg(I, CustomerLang.unbindingErr.replace("//id//", id))
        }
        //查询
        Intent.sendMsg(I, CustomerLang.searching.replace("//id//", id).replace("//action//", "载具数据"))
        val name = id
        val allStats = BF1Api.getAllStats(name)
        if (allStats.isSuccessful) {
            val backImg = BackgroundImgData.backgroundImgData[I.event.sender.id]?.vehicleImg ?: "assets/MP_Volga.jpg"
            val htmlToImage = HtmlToImage()
            val content = htmlToImage.readIt("vehicle")
            var res = content
                .replace("-DNAME", name)
                .replace("-DRANK", allStats.rank.toString())
                .replace("-DCPRO", allStats.currentRankProgress.toString())
                .replace("-DTPRO", allStats.totalRankProgress.toString())
                .replace("-DGTIME", "${allStats.secondsPlayed?.div(60)?.div(60)}")
                .replace("-DBEST", "${allStats.bestClass}")
                .replace("-DLKD", "${allStats.killDeath}")
                .replace("-DLKPM", "${allStats.killsPerMinute}")
                .replace("-DGENTIME", SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()))
                .replace("assets/MP_Volga.jpg", backImg)
                .replace(
                    "-DKILLS",
                    if (allStats.kills != null && allStats.kills > 10000) "${allStats.kills.div(100)} ★" else "${allStats.kills}"
                )
                .replace(
                    "-DDEATH",
                    if (allStats.deaths != null && allStats.deaths > 10000) "${allStats.deaths.div(100)} ★" else "${allStats.deaths}"
                )
            if (allStats.activePlatoon != null) {
                if (allStats.activePlatoon.emblem != null && allStats.activePlatoon.tag != null) {
                    val cacheImg =
                        htmlToImage.cacheImg(allStats.activePlatoon.emblem, "Platoon_${allStats.activePlatoon.tag}")
                    res = if (cacheImg) {
                        res.replace("-DPROFILEP", htmlToImage.getImgPath()).replace("-DPO", allStats.activePlatoon.tag)
                    } else {
                        htmlToImage.cacheImg(allStats.activePlatoon.emblem, "Avatar_Def")
                        res.replace("-DPROFILEP", htmlToImage.getImgPath()).replace("-DPO", allStats.activePlatoon.tag)
                    }
                } else {
                    //头像缓存
                    if (allStats.avatar != null) {
                        val cacheImg = htmlToImage.cacheImg(allStats.avatar, "Avatar_${name}")
                        res = if (cacheImg) {
                            res.replace("-DPROFILEP", htmlToImage.getImgPath()).replace("[-DPO]", "")
                        } else {
                            htmlToImage.cacheImg(allStats.avatar, "Avatar_Def")
                            res.replace("-DPROFILEP", htmlToImage.getImgPath()).replace("[-DPO]", "")
                        }
                    }
                }
            }
            val vpModel = """
            <div class="item">
                <p>NAME - STATS</p>
                <img src="IMG">
                <span>
                    <p>擊殺:KILLS</p>
                    <p>KPM:-DKPM</p>
                    <p>摧毁:-DS</p>
                </span>
                <p>时长:-DTPh</p>
            </div>
        """.trimIndent()
            var line1 = ""
            var line2 = ""
            var line3 = ""
            var line4 = ""
            var vehiclesIndex = 0
            allStats.vehicles?.sortedByDescending { vehicles -> vehicles.kills }?.forEach { vehicles ->
                vehiclesIndex++
                htmlToImage.cacheImg(vehicles.image, "Vehicles_${vehicles.vehicleName}")
                val temp = vpModel
                    .replace("STATS", if (vehicles.kills > 100) "${vehicles.kills.div(100)} ★" else "0 ★")
                    .replace("KILLS", "${vehicles.kills}")
                    .replace("NAME", vehicles.vehicleName)
                    .replace("-DKPM", vehicles.killsPerMinute.toString())
                    .replace("-DS", vehicles.destroyed.toString())
                    .replace("-DTP", "${vehicles.timeIn / 60 / 60}")
                    .replace("IMG", htmlToImage.getImgPath())
                when (vehiclesIndex) {
                    in 1..3 -> line1 += temp
                    in 4..6 -> line2 += temp
                    in 7..9 -> line3 += temp
                    in 10..12 -> line4 += temp
                }

            }
            res = res.replace("LINE1", line1).replace("LINE2", line2).replace("LINE3", line3).replace("LINE4", line4)
            htmlToImage.writeTempFile(res)
            htmlToImage.toImage(1280, 720)
            return I.event.subject.uploadImage(File(htmlToImage.getFilePath()))
        } else {
            return PlainText(CustomerLang.searchErr.replace("//action//", "载具数据"))
        }
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
                         ${it.prefix!!.substring(0, 30)}...
                         ${it.currentMap} - ${it.mode} ${it.serverInfo} (${it.inSpectator})
                         GameID:${it.gameId}
                    """.trimIndent() + "\n"
                }
                if (serverSearchJson.servers?.size == 0) {
                    temp = "没有找到对应服务器"
                }
                temp.toPlainText()
            } else {
                CustomerLang.searchErr.replace("//action//", "服务器").toPlainText()
            }
        } else {
            CustomerLang.parameterErr.replace("//para//", "*ss <ServerName>").toPlainText()
        }
    }

    /**
     * 查询历史老毕登
     * @param I PullIntent
     * @return Message
     */
    fun searchHistory(I: PullIntent): Message {
        if (!I.isAdmin) return CustomerLang.notAdminErr.toPlainText()
        if (I.cmdSize < 1) return CustomerLang.parameterErr.replace("//para//", "*ls <ServerCount>").toPlainText()
        val name = I.sp[1]
        val server = ServerInfos.getServerByName(I.event.group.id, name) ?: return CustomerLang.nullServerErr.replace(
            "//err//",
            name
        ).toPlainText()
        val comparator = kotlin.Comparator { key1: Int, key2: Int -> key2.compareTo(key1) }
        val map = TreeMap<Int, String>(comparator)
        var temp = "服务器$name 的历史进服数据\n"
        HistoryLog.log.forEach { id, data ->
            val sp = data.split(" ")
            if (sp[1] == server.gameID!!) {
                map[sp[2].toInt()] = (map[sp[2].toInt()] ?: "") + ",$id"
            }
        }
        map.forEach { time, id ->
            if (time > 5){
                var ids = ""
                val sp = id.split(",")
                sp.forEachIndexed{index, s ->
                    if (index != 0) {
                        val isBinding = Bindings.bindingData.any {it.value == s}
                        ids+="ID:$s ${if (isBinding) "是群友" else ""} \n"
                    }
                }
                temp += "进服次数:$time \n${ids}  \n"
            }
        }
        return temp.toPlainText()
    }

    /**
     * 查询服务器玩家列表的实现
     * @param I PullIntent
     * @return Message
     */
    fun searchServerList(I: PullIntent): Message {
        if (I.cmdSize < 2) return CustomerLang.parameterErr.replace("//para//", "*pl <ServerCount>").toPlainText()
        val name = I.sp[1]
        var msg: Message = PlainText(CustomerLang.searchErr.replace("//action//", "玩家列表"))
        val team1 = ForwardMessageBuilder(I.event.group)
        team1.add(I.event.bot, PlainText("队伍1"))
        val team2 = ForwardMessageBuilder(I.event.group)
        team2.add(I.event.bot, PlainText("队伍2"))
        val blackTeam = ForwardMessageBuilder(I.event.group)
        blackTeam.add(I.event.bot, PlainText("黑队查询"))
        val gameid = ServerInfos.getGameIDByName(I.event.group.id, name)
        if (gameid.isEmpty()) return msg
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
        val htmlToImage = HtmlToImage()
        val text = htmlToImage.readIt("playerList")
        //background-color: rgb(86, 196, 73);
        val player ="""
            <div class="player">
                <div class="index">INDEX</div>
                <div class="rank"  style="rankback">RANK</div>
                <div class="black">BLACK_TEXT</div>
                <div style="color: #fff;id" class="name">NAME</div>
                <div style="color: #fff;lkd" class="lifeKD">LIFE_KD</div>
                <div style="color: #fff;lkp" class="lifeKPM">LIFE_KPM</div>
                <div style="color: #fff;rkd" class="RKD">R_KD</div>
                <div style="color: #fff;rkp" class="RKPM">R_KPM</div>
                <div class="time">TIME分</div>
                <div class="latency">PINGms</div>
                <div class="latency">LANG</div>
            </div>
        """.trimIndent()
        val platoonSet: MutableSet<String> = mutableSetOf()
        val blackPlayerSet: HashMap<String, MutableList<String>> = hashMapOf()
        Cache.PlayerListInfo.forEach { players ->
            if (gameid == players.gameID) {
                players.platoonTagList.forEach {
                    platoonSet.add(it)
                }
            }
        }
        Cache.PlayerListInfo.forEach { players ->
            platoonSet.forEach {
                players.platoonTagList.forEach { p ->
                    if (it == p) {
                        if (blackPlayerSet[p].isNullOrEmpty()) {
                            blackPlayerSet[p] = mutableListOf()
                            blackPlayerSet[p]!!.add(players.id)
                        } else {
                            if (!blackPlayerSet[p]!!.any { it == players.id }) {
                                blackPlayerSet[p]!!.add(players.id)
                            }
                        }
                    }
                }
            }
        }
        Cache.PlayerListInfo.sortedByDescending { it.join_time }.reversed().forEach {
            if (gameid != it.gameID) return@forEach
            var pa = ""
            if (it.platoon.isNotEmpty())
                pa = "[ ${it.platoon} ] "

            var color = "#fff"
            var blackText = ""
            var colorlkd = "#fff"
            var colorlkp = "#fff"
            var colorrkp = "#fff"
            var colorrkd = "#fff"
            if (it.lkd > it.lifeMaxKD * 0.6) colorlkd = "#ff0"
            if (it.lkp > it.lifeMaxKPM * 0.6) colorlkp = "#ff0"
            if (it.rkp > it.recentlyMaxKPM * 0.6) colorrkp = "#ff0"
            if (it.rkd > it.recentlyMaxKD * 0.6) colorrkd = "#ff0"
            if (it.lkd > it.lifeMaxKD * 0.8) colorlkd = "#ff6600"
            if (it.lkp > it.lifeMaxKPM * 0.8) colorlkp = "#ff6600"
            if (it.rkp > it.recentlyMaxKPM * 0.8) colorrkp = "#ff6600"
            if (it.rkd > it.recentlyMaxKD * 0.8) colorrkd = "#ff6600"
            if (it.isBot) {
                color = "aqua"
                if (it.botState == "Loading") loadingBots++
            }
            if (!it.isBot) {
                blackPlayerSet.forEach { (p, data) ->
                    if (data.size > 1) {
                        data.forEach { id ->
                            if (id == it.id) {
                                blackText = "[!]"
                                blackPlayer++
                            }
                        }
                    }
                }
            }
            Bindings.bindingData.forEach { p ->
                if (p.value == it.id) {
                    color = "pink"
                    groupPlayer++
                }
            }
            run o@{
                val sp = Cache.ServerInfoList[gameid]!!.opPlayers.split(";")
                sp.forEach { sp ->
                    if (it.pid == sp.toLong()) {
                        color = "#f9767b"
                        opPlayer++
                        return@o
                    }
                }
            }
            if (it.teamId == 0) {
                teamOneName = it.team
                team1Index++
                teamOne += player
                    .replace("NAME", "$pa${it.id}")
                    .replace("RANK", it.rank.toString())
                    .replace("INDEX", team1Index.toString())
                    .replace(
                        "TIME",
                        "${(System.currentTimeMillis() - (it.join_time / 1000)) / 1000 / 60}"
                    )
                    .replace("PING", "${it.latency}")
                    .replace("LANG",
                        it.langLong.toString(16).chunked(2).map { it.toInt(16).toByte() }.toByteArray().toString(Charsets.US_ASCII)
                    )
                    .replace("LIFE_KD", "${it.lkd}")
                    .replace("LIFE_KPM", "${it.lkp}")
                    .replace("R_KD", "${it.rkd}")
                    .replace("R_KPM", "${it.rkp}")
                    .replace("BLACK_TEXT", blackText)
                    .replace("color: #fff;id", "color: ${color};")
                    .replace("color: #fff;lkd", "color: ${colorlkd};")
                    .replace("color: #fff;lkp", "color: ${colorlkp};")
                    .replace("color: #fff;rkp", "color: ${colorrkp};")
                    .replace("color: #fff;rkd", "color: ${colorrkd};")
                    .replace(
                        "rankback",
                        if (it.rank > 120) "background-color: rgb(86, 196, 73);" else ""
                    )
            } else if (it.teamId == 1) {
                teamTwoName = it.team
                team2Index++
                teamTwo += player
                    .replace("NAME", "$pa${it.id}")
                    .replace("RANK", it.rank.toString())
                    .replace("INDEX", team2Index.toString())
                    .replace(
                        "TIME",
                        "${(System.currentTimeMillis() - (it.join_time / 1000)) / 1000 / 60}"
                    )
                    .replace("PING", "${it.latency}")
                    .replace("LANG",
                        it.langLong.toString(16).chunked(2).map { it.toInt(16).toByte() }.toByteArray().toString(Charsets.US_ASCII)
                    )
                    .replace("LIFE_KD", "${it.lkd}")
                    .replace("LIFE_KPM", "${it.lkp}")
                    .replace("R_KD", "${it.rkd}")
                    .replace("R_KPM", "${it.rkp}")
                    .replace("BLACK_TEXT", blackText)
                    .replace("color: #fff;id", "color: ${color};")
                    .replace("color: #fff;lkd", "color: ${colorlkd};")
                    .replace("color: #fff;lkp", "color: ${colorlkp};")
                    .replace("color: #fff;rkp", "color: ${colorrkp};")
                    .replace("color: #fff;rkd", "color: ${colorrkd};")
                    .replace(
                        "rankback",
                        if (it.rank > 120) "background-color: rgb(86, 196, 73);" else ""
                    )
            } else {

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

        val teamOneImg =
            if (htmlToImage.cacheImg(Cache.ServerInfoList[gameid]!!.teamOneImgUrl, teamOneName)) {
                htmlToImage.getImgPath()
            } else {
                ""
            }
        val teamTwoImg =
            if (htmlToImage.cacheImg(Cache.ServerInfoList[gameid]!!.teamTwoImgUrl, teamTwoName)) {
                htmlToImage.getImgPath()
            } else {
                ""
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
            .replace(
                "-DTIME",
                SimpleDateFormat("MM-dd HH:mm:ss").format(Cache.ServerInfoList[gameid]!!.cacheTime)
            )
            .replace("-DLBPL", Cache.ServerInfoList[gameid]!!.oldPlayers.toString())
            .replace("-DSPL", Cache.ServerInfoList[gameid]!!.spectatorPlayers.toString())
            .replace("-DBOPL", Cache.ServerInfoList[gameid]!!.bots.toString())
            .replace("-DGOPL", groupPlayer.toString())
            .replace("-DOPPL", opPlayer.toString())
            .replace("-BOTSL", Cache.ServerInfoList[gameid]!!.loadingPlayers.toString())
            .replace("-DBLPL", blackPlayer.toString())
            .replace("-DTEAM1NAME", teamOneName)
            .replace("-DTEAM2NAME", teamTwoName)
            .replace("-DP", Cache.ServerInfoList[gameid]!!.players.toString())
            .replace("-DGameID", gameid)
            .replace("-DGameID", gameid)

        htmlToImage.writeTempFile(res)
        htmlToImage.toImage()
        runBlocking {
            msg = I.event.subject.uploadImage(File(htmlToImage.getFilePath()))
        }
        return msg
    }


    //TODO 搜索服务器玩家
    fun searchServerListPlayer(I: PullIntent): Message {
        if (I.cmdSize < 3) return CustomerLang.parameterErr.replace("//para//", "*ssi <ServerCount> <ID>").toPlainText()
        val name = I.sp[1]
        val gameID = ServerInfos.getGameIDByName(I.event.group.id, name)
        if (gameID.isBlank()) return CustomerLang.nullServerErr.replace("//err//", name).toPlainText()
        val serverListJson = BF1Api.searchServerList(gameID)
        return if (serverListJson.isSuccessful == true) {
            var p = "在服务器${name}中查找到\n"
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

    /**
     * 查询黑队
     * @param I PullIntent
     */
    fun searchBlackTeam(I: PullIntent): Message {
        if (I.cmdSize < 2) return CustomerLang.parameterErr.replace("//para//", "*chd <ServerCount>").toPlainText()
        val name = I.sp[1]
        val gameID = ServerInfos.getGameIDByName(I.event.group.id, name)
        if (gameID.isBlank()) return CustomerLang.nullServerErr.replace("//err//", name).toPlainText()
        //记录服务器内所以战队
        val platoonSet: MutableSet<String> = mutableSetOf()
        //记录ID
        val blackPlayerSet: HashMap<String, MutableList<String>> = hashMapOf()
        Cache.PlayerListInfo.forEach { players ->
            if (gameID == players.gameID) {
                players.platoonTagList.forEach {
                    platoonSet.add(it)
                }
            }
        }
        Cache.PlayerListInfo.forEach { players ->
            platoonSet.forEach {
                players.platoonTagList.forEach { p ->
                    if (it == p) {
                        if (blackPlayerSet[p].isNullOrEmpty()) {
                            blackPlayerSet[p] = mutableListOf()
                            blackPlayerSet[p]!!.add(players.id)
                        } else {
                            if (!blackPlayerSet[p]!!.any { it == players.id }) {
                                blackPlayerSet[p]!!.add(players.id)
                            }
                        }
                    }
                }
            }
        }
        return I.event.buildForwardMessage {
            add(
                I.event.bot.id, "唧唧", """
                服务器中拥有战队:${platoonSet}
            """.trimIndent().toPlainText()
            )
            blackPlayerSet.forEach { (p1, _) ->
                var temp = "战队:[${p1}]\n"
                var size = 0
                blackPlayerSet.forEach { p, data ->
                    if (p1 == p) {
                        if (data.size > 1) {
                            data.forEach {
                                temp += "$it \n"
                                size++
                            }
                        }
                    }
                }
                if (size > 1) {
                    add(I.event.bot.id, "唧唧", temp.toPlainText())
                }
            }

        }
    }
}