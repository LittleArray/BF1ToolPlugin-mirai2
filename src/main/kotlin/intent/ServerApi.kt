package top.ffshaozi.intent

import io.javalin.Javalin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import top.ffshaozi.NeriQQBot
import top.ffshaozi.config.Setting
import top.ffshaozi.utils.BF1Api

/**
 * @Description
 * @Author littleArray
 * @Date 2023/9/13
 */
object ServerApi {
    val app: Javalin = Javalin.create()
    fun run(port: Int) {
        app.start(port)
        //上传player数据
        app.post("/api/v1/player/{gameid}/{name}") { ctx ->
            ctx.status(200)
        }
        //上传playerList
        app.post("/api/v1/pl/{gameid}") { ctx ->
            ctx.status(200)
        }
        //转发服务器聊天数据
        app.post("/api/v1/chat/{GameId}") { ctx ->
            val gameId = ctx.pathParam("GameId")
            NeriQQBot.Glogger.warning("$gameId ${ctx.body()}")
            NeriQQBot.GlobalBots.forEach { bot ->
                CycleTask.serverInfoIterator { groupID, data, index, serverInfoForSave ->
                    if (serverInfoForSave.gameID == gameId) {
                        CoroutineScope(Dispatchers.IO).launch {
                            bot.getGroup(groupID)?.sendMessage("$index 服消息转发\n${ctx.body()}")
                        }
                    }
                }
            }
            ctx.status(200)
        }
        //Api保活
        app.get("/") { ctx ->
            ctx.result("Runing")
            ctx.status(200)
        }
    }
}