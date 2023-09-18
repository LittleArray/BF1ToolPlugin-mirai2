package top.ffshaozi.data.btr

import java.util.Date

/**
 * @Description
 * @Author littleArray
 * @Date 2023/8/28
 */
data class RecentlyJson(
    var spm:String = "-1",
    var kd:String = "-1",
    var kpm:String = "-1",
    var bs:String="-1",
    var gs:String="-1",
    var tp:String="-1",
    var rp:String="0",
    val isSuccessful:Boolean = true
)
data class RecentlyServerJson(
    var serverName:String ?= "",
    var time:Date?=null,
    var map:String?="",
    var kd: String="-1",
    var kills:String="-1",
    var deaths:String="-1"
)