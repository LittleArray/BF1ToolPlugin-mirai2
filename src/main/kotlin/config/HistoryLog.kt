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
object HistoryLog: AutoSavePluginData("HistoryLog") {
    @ValueDescription("历史进服名单")
    var log: MutableMap<String,String> by value()
    fun addHistoryLog(id:String,joinTime:Date,gameID:String){
        var old = ""
        log.forEach { (_id, data) ->
            if (_id == id){
                old = data
            }
        }
        if (old.isNotEmpty()){
            val sp = old.split(" ")
            log[id] = "${SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(joinTime)} $gameID ${sp[2].toInt()+1}"
        }else{
            log[id] = "${SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(joinTime)} $gameID 1"
        }
        save()
    }
}