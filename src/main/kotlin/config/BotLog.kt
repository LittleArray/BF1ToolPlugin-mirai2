package top.ffshaozi.config

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import top.ffshaozi.NeriQQBot.save
import java.text.SimpleDateFormat
import java.util.Date

/**
 * @Description
 * @Author littleArray
 * @Date 2023/9/9
 */
object BotLog: AutoSavePluginData("BotLog-${SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(System.currentTimeMillis())}") {
    @ValueDescription("进服log")
    var enterServerLog: MutableMap<String,String> by value()
    @ValueDescription("退服log")
    var exitServerLog: MutableMap<String,String> by value()
    @ValueDescription("重进log")
    var reEnterServerLog: MutableMap<String,String> by value()
    @ValueDescription("观战log")
    var spectatorServerLog: MutableMap<String,String> by value()
    @ValueDescription("换边log")
    var teamChangeLog: MutableList<String> by value()
    @ValueDescription("踢人log")
    var kickLog: MutableList<String> by value()
    @ValueDescription("驻留玩家log")
    var stateServerLog: MutableList<String> by value()
    fun enterServerLog(joinTime:Date,playerID:String,gameID:String){
        if (joinTime.time != 0L){
            enterServerLog[SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(joinTime)] = "$playerID $gameID"
            save()
        }
    }
    fun spectatorServerLog(joinTime:Date,playerID:String,gameID:String){
        if (joinTime.time != 0L){
            spectatorServerLog[SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(joinTime)] = "$playerID $gameID"
            save()
        }
    }
    fun exitServerLog(exitTime:Date,playerID:String,gameID:String){
        if (exitTime.time != 0L) {
            exitServerLog[SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(exitTime)] = "$playerID $gameID"
            save()
        }
    }
    fun reEnterServerLog(joinTime: Date,playerID:String,gameID:String){
        if (joinTime.time != 0L) {
            reEnterServerLog[SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(joinTime)] = "$playerID $gameID"
            save()
        }
    }
    fun teamChangeLog(playerID:String,gameID:String,newTeam:String){
        teamChangeLog.add("[${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis())}] 玩家${playerID}在服务器${gameID}执行换边操作 -> ${newTeam}")
        save()
    }
    fun kickLog(playerID:String,gameID:String,reason:String){
        kickLog.add("[${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis())}] 玩家${playerID}在服务器${gameID}被踢出 理由:${reason}")
        save()
    }
}