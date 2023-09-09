package top.ffshaozi.utils

import com.google.gson.Gson
import data.BotsJson
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.rootDir
import top.ffshaozi.BF1ToolPlugin
import top.ffshaozi.config.Setting
import top.ffshaozi.intent.Cache
import top.ffshaozi.intent.Cache.refreshServerList


/**
 * @Description
 * @Author littleArray
 * @Date 2023/8/28
 */
fun test() {
    //val reqBody = BF1Api.getApi("https://battlefieldtracker.com/bf1/profile/pc/Jerrydonleo/matches",false)
    //if (!reqBody.isSuccessful) return
    val gameid = "8622725350482"
    val groupid = 702474262L
    var groupPlayer = 0
    var opPlayer = 0
    var blackPlayer = 0
    var loadingBots = 0
    var teamOne = ""
    var team1Index = 0
    var teamTwo = ""
    var team2Index = 0
    val text = readIt("playerList")
    val player =
        "<div class=\"player\" style=\"color: #fff;\"><div class=\"index\">INDEX</div><div class=\"rank\">RANK</div><div class=\"name\">NAME</div><div class=\"lifeKD\">LIFE_KD</div><div class=\"lifeKPM\">LIFE_KPM</div><div class=\"RKD\">R_KD</div><div class=\"RKPM\">R_KPM</div><div class=\"time\">TIME分</div><div class=\"latency\">PINGms</div></div>"
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
                    pa = "[${it.platoon}]"

                var color = "#fff"
                if (it.rank > 120) color = "chocolate"
                if (it.isBot) {
                    color = "blue"
                    if (it.botState == "Loading") loadingBots++
                }
                blackTeamSet.forEach { pa ->
                    if (it.platoon == pa) {
                        color = "chartreuse"
                        blackPlayer++
                    }
                }
                Setting.groupData[groupid]?.operator?.forEach { qq ->
                    Setting.groupData[groupid]?.bindingData?.forEach {
                        if (it.value == id) {
                            color = "pink"
                            groupPlayer++
                        }
                        if (it.key == qq)
                            if (it.value == id) {
                                color = "blue"
                                opPlayer++
                            }
                    }
                }
                if (it.teamId == 1) {
                    team1Index++
                    teamOne += player
                        .replace("NAME", "$pa$id")
                        .replace("RANK", it.rank.toString())
                        .replace("INDEX", team1Index.toString())
                        .replace("TIME", "${(System.currentTimeMillis() - (it.join_time / 1000)) / 1000 / 60}")
                        .replace("PING", "${it.latency}")
                        .replace("LIFE_KD", "${it.lkd}")
                        .replace("LIFE_KPM", "${it.lkp}")
                        .replace("R_KD", "${it.rkd}")
                        .replace("R_KPM", "${it.rkp}")
                        .replace("color: #fff;", "color: ${color};")
                } else {
                    team2Index++
                    teamTwo += player
                        .replace("NAME", "$pa$id")
                        .replace("RANK", it.rank.toString())
                        .replace("INDEX", team2Index.toString())
                        .replace("TIME", "${(System.currentTimeMillis() - (it.join_time / 1000)) / 1000 / 60}")
                        .replace("PING", "${it.latency}")
                        .replace("LIFE_KD", "${it.lkd}")
                        .replace("LIFE_KPM", "${it.lkp}")
                        .replace("R_KD", "${it.rkd}")
                        .replace("R_KPM", "${it.rkp}")
                        .replace("color: #fff;", "color: ${color};")
                }
            }
    }
    val res = text
        .replace("Team1Replace", teamOne)
        .replace("Team2Replace", teamTwo)
        .replace("-DPREFIX", Cache.ServerInfoList[gameid]!!.perfix)
        .replace("-DTEAM1PIC", Cache.ServerInfoList[gameid]!!.teamOneImgUrl)
        .replace("-DTEAM2PIC", Cache.ServerInfoList[gameid]!!.teamTwoImgUrl)
        .replace("-DMODE", Cache.ServerInfoList[gameid]!!.mode)
        .replace("-DMAP", Cache.ServerInfoList[gameid]!!.map)
        .replace("-DLBPL", Cache.ServerInfoList[gameid]!!.oldPlayers.toString())
        .replace("-DBOPL", Cache.ServerInfoList[gameid]!!.bots.toString())
        .replace("-DGOPL", groupPlayer.toString())
        .replace("-DOPPL", opPlayer.toString())
        .replace("-BOTSL", loadingBots.toString())
        .replace("-DBLPL", blackPlayer.toString())
        .replace("-DP", Cache.ServerInfoList[gameid]!!.players.toString())
        .replace("-DGameID", gameid)
        .replace("-DGameID", gameid)

    writeTempFile("playerList", res)
    htmlToImage("playerList")
}




