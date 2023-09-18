package top.ffshaozi.config

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import top.ffshaozi.NeriQQBot
import top.ffshaozi.utils.BF1Api

object Setting : AutoSavePluginData("Setting") {
    @ValueDescription("服务器Api端口")
    val port:Int by value(2086)

    /*fun addVip(groupID: Long, serverCount: Int, name: String, time: String): Boolean {
        if (isNullServer(groupID)) return false
        groupData[groupID]?.server?.forEachIndexed { index, it ->
            if (index + 1 == serverCount) {
                var times = System.currentTimeMillis()
                it.vipList.forEach { id, time ->
                    if (id == name) times = time.toLong()
                }
                it.vipList.put(
                    name,
                    times + (time.toFloat() * 24 * 60 * 60 * 1000)
                )
                return true
            }
        }
        return false
    }

    fun removeVip(groupID: Long, serverCount: Int, name: String): Boolean {
        if (isNullServer(groupID)) return false
        groupData[groupID]?.server?.forEachIndexed { index, it ->
            if (index + 1 == serverCount) {
                it.vipList.remove(name)
                return true
            }
        }
        return false
    }
*/
}

