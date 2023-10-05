package top.ffshaozi.intent

import com.google.gson.Gson
import top.ffshaozi.config.BackgroundImgData
import top.ffshaozi.config.ServerInfos
import top.ffshaozi.data.ea.CurPlay
import top.ffshaozi.utils.BF1Api
import top.ffshaozi.utils.HtmlToImage
import java.text.SimpleDateFormat

/**
 * @Description
 * @Author littleArray
 * @Date 2023/10/5
 */
class CacheSearch {
    fun Run(name:String,groupid:Long) {
        val allStats = BF1Api.getAllStats(name)
        if (allStats.isSuccessful) {
            val backImg = BackgroundImgData.backgroundImgData[groupid]?.statsImg ?: "assets/MP_Blitz.jpg"
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
                if (recentlyJson[0].rp == "0" || recentlyJson[0].rp == "-1") {
                    var ssid = ""
                    run c@{
                        ServerInfos.serverInfo.forEach {
                            if (it.sessionId != null) {
                                ssid = it.sessionId!!
                                return@c
                            }
                        }
                    }
                    val CuPlay = BF1Api.getCuPlayByEA(pid = allStats.id, sessionId = ssid).reqBody
                    Gson().fromJson(CuPlay, CurPlay::class.java).result.firstNotNullOfOrNull {
                        if (it.value?.name != null) {
                            res = res
                                .replace(
                                    "最近KPM:-Dre_kpm 最近KD:-Dre_kd 游玩时间:-Dre_pt",
                                    "當前遊玩 ${it.value!!.name.substring(0, 36)}..."
                                )
                        } else {
                            res = res
                                .replace("-Dre_time", recentlyJson[0].rp)
                                .replace("-Dre_pt", recentlyJson[0].tp)
                                .replace("-Dre_spm", recentlyJson[0].spm)
                                .replace("-Dre_kpm", recentlyJson[0].kpm)
                                .replace("-Dre_kd", recentlyJson[0].kd)
                        }
                    }
                } else {
                    res = res
                        .replace("-Dre_time", recentlyJson[0].rp)
                        .replace("-Dre_pt", recentlyJson[0].tp)
                        .replace("-Dre_spm", recentlyJson[0].spm)
                        .replace("-Dre_kpm", recentlyJson[0].kpm)
                        .replace("-Dre_kd", recentlyJson[0].kd)
                }
            }

            htmlToImage.writeTempFile(res)
            htmlToImage.toImage(1280, 720)
        }
    }
}