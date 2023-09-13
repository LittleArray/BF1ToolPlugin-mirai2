package top.ffshaozi.intent

import kotlinx.coroutines.*
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.data.*
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
    suspend fun searchMe(I: PullIntent): Any {
        var id = SettingController.getBinding(I.event.group.id, I.event.sender.id)
        if (I.cmdSize > 1) id = I.sp[1]
        if (id.isEmpty()) {
            id = I.event.sender.nameCard
            SettingController.addBinding(I.event.group.id, I.event.sender.id, id)
            Intent.sendMsg(I, CustomerLang.unbindingErr.replace("//id//", id))
        }
        //查询
        Intent.sendMsg(I, CustomerLang.searching.replace("//id//", id).replace("//action//", "基础数据"))
        val name = id
        val allStats = BF1Api.getAllStats(name)
        if (allStats.isSuccessful) {
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
                .replace("-DGENTIME", SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()))
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
                <p>NAME</p>
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
                <p>NAME</p>
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
            allStats.weapons?.sortedByDescending { weapons -> weapons.kills }?.forEachIndexed { index, weapon ->
                if (index < 3) {
                    htmlToImage.cacheImg(weapon.image, "Weapon_${weapon.weaponName}")
                    wpText += wpModel
                        .replace("KILLS", if (weapon.kills > 1000) "${weapon.kills.div(100)} ★" else "${weapon.kills}")
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
                        .replace(
                            "KILLS",
                            if (vehicles.kills > 1000) "${vehicles.kills.div(100)} ★" else "${vehicles.kills}"
                        )
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
            res = res.replace("WPTEXT", wpText).replace("VPTEXT", vpText).replace("CLTEXT", classText)
                .replace("GAMEWINTEXT", gameWinText)
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
        var id = SettingController.getBinding(I.event.group.id, I.event.sender.id)
        if (I.cmdSize > 1) id = I.sp[1]
        if (id.isEmpty()) {
            id = I.event.sender.nameCard
            SettingController.addBinding(I.event.group.id, I.event.sender.id, id)
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
    suspend fun searchWp(I: PullIntent, type: String? = null): Message {
        val typeI = when (type) {
            "*霰弹枪" -> "霰彈槍"
            "*轻机枪" -> "輕機槍"
            "*配备" -> "配備"
            "*半自动步枪" -> "半自動步槍"
            "*配枪" -> "佩槍"
            "*佩枪" -> "佩槍"
            "*手枪" -> "佩槍"
            "*副武器" -> "佩槍"
            "*近战武器" -> "近戰武器"
            "*手榴弹" -> "手榴彈"
            "*步枪" -> "步槍"
            "*战场装备" -> "戰場裝備"
            "*驾驶员" -> "坦克/駕駛員"
            "*制式步枪" -> "制式步槍"
            "*冲锋枪" -> "衝鋒槍"
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
        val name = id
        val allStats = BF1Api.getAllStats(name)
        if (allStats.isSuccessful) {
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
                <p>NAME</p>
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
                if (weapon.weaponName.indexOf("三八") !=-1 ){
                    weapon.type = "步槍"
                }
                if (typeI == weapon.type || typeI == null) {
                    weaponIndex++
                    htmlToImage.cacheImg(weapon.image, "Weapon_${weapon.weaponName}")
                    val temp = wpModel
                        .replace("KILLS", if (weapon.kills > 1000) "${weapon.kills.div(100)} ★" else "${weapon.kills}")
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
        var id = SettingController.getBinding(I.event.group.id, I.event.sender.id)
        if (I.cmdSize > 1) id = I.sp[1]
        if (id.isEmpty()) {
            id = I.event.sender.nameCard
            SettingController.addBinding(I.event.group.id, I.event.sender.id, id)
            Intent.sendMsg(I, CustomerLang.unbindingErr.replace("//id//", id))
        }
        //查询
        Intent.sendMsg(I, CustomerLang.searching.replace("//id//", id).replace("//action//", "载具数据"))
        val name = id
        val allStats = BF1Api.getAllStats(name)
        if (allStats.isSuccessful) {
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
                <p>NAME</p>
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
                    .replace(
                        "KILLS",
                        if (vehicles.kills > 1000) "${vehicles.kills.div(100)} ★" else "${vehicles.kills}"
                    )
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
        if (SettingController.isNullServer(I.event.group.id)) return CustomerLang.nullServerErr.replace(
            "//err//",
            ""
        )
            .toPlainText()
        if (re) SettingController.refreshServerInfo(I.event.group.id)
        val index = I.sp[1].toInt()
        var msg: Message = PlainText(CustomerLang.searchErr.replace("//action//", "玩家列表"))
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
                    val htmlToImage = HtmlToImage()
                    val text = htmlToImage.readIt("playerList")
                    //background-color: rgb(86, 196, 73);
                    val player =
                        "<div class=\"player\"><div class=\"index\">INDEX</div><div class=\"rank\"  style=\"rankback\">RANK</div><div class=\"black\">BLACK_TEXT</div><div style=\"color: #fff;id\" class=\"name\">NAME</div><div style=\"color: #fff;lkd\" class=\"lifeKD\">LIFE_KD</div><div style=\"color: #fff;lkp\" class=\"lifeKPM\">LIFE_KPM</div><div style=\"color: #fff;rkd\" class=\"RKD\">R_KD</div><div style=\"color: #fff;rkp\" class=\"RKPM\">R_KPM</div><div class=\"time\">TIME分</div><div class=\"latency\">PINGms</div></div>"
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
                                var blackText = ""
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
                                        if (!it.isBot) {
                                            blackText = "[!]"
                                            blackPlayer++
                                        }
                                    }
                                }
                                Setting.groupData[groupid]?.bindingData?.forEach {
                                    if (it.value.indexOf(id, 0, true) != -1) {
                                        color = "pink"
                                        groupPlayer++
                                    }
                                }
                                run o@{
                                    Setting.groupData[groupid]?.bindingData?.forEach {
                                        Setting.groupData[groupid]?.operator?.forEach { qq ->
                                            if (it.key == qq && it.value.indexOf(id, 0, true) != -1) {
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

                    htmlToImage.writeTempFile(res)
                    htmlToImage.toImage()
                    runBlocking {
                        msg = I.event.subject.uploadImage(File(htmlToImage.getFilePath()))
                    }

                }
            }
        }
        return msg
    }


    //TODO 搜索服务器玩家
    fun searchServerListPlayer(I: PullIntent, re: Boolean = false): Message {
        if (I.cmdSize < 3) return CustomerLang.parameterErr.replace("//para//", "*ssi <ServerCount> <ID>")
            .toPlainText()
        if (SettingController.isNullServer(I.event.group.id)) return CustomerLang.nullServerErr.replace(
            "//err//",
            ""
        )
            .toPlainText()
        if (re) SettingController.refreshServerInfo(I.event.group.id)
        val serverCount = I.sp[1].toInt()
        Setting.groupData[I.event.group.id]?.server?.forEachIndexed { index, it ->
            if (index + 1 == serverCount) {
                if (it.gameID.isNullOrEmpty()) {
                    if (re) return CustomerLang.serverInfoRErr.toPlainText()
                    searchServerListPlayer(I, re = true)
                    return CustomerLang.serverInfoRefreshing.toPlainText()
                }
                Intent.sendMsg(
                    I,
                    CustomerLang.searching.replace("//id//", I.sp[2]).replace("//action//", "服务器玩家")
                )
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