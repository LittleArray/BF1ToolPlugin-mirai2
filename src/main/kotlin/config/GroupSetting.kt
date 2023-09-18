package top.ffshaozi.config

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

/**
 * @Description 群组信息类
 * @Author littleArray
 * @Date 2023/9/16
 */
object GroupSetting : AutoSavePluginData("GroupSetting") {
    @ValueDescription("群组信息类")
    var groupSetting: MutableList<_GroupSetting> by value()//服务器信息

    /**
     * 添加绑定的群组
     * @param groupID Long
     */
    fun addGroup(groupID: Long) {
        val temp = _GroupSetting(groupID)
        groupSetting.add(temp)
    }
    /**
     * 移除绑定的群组
     * @param groupID Long
     */
    fun removeGroup(groupID: Long) {
        groupSetting.removeIf { it.groupID == groupID }
    }

    /**
     * 获取机器人列表
     * @return MutableList<Long>
     */
    fun getGroupList():MutableList<Long>{
        val temp = mutableListOf<Long>()
        groupSetting.forEach {
            it.groupID?.let { it1 -> temp.add(it1) }
        }
        return temp
    }
    /**
     * 给群组添加服务器
     * @param groupID Long
     * @param gameID String
     * @return Boolean
     */
    fun addGroupBindingServer(groupID: Long, gameID: String,bindingName:String):Boolean {
        groupSetting.forEach {
            if (it.groupID == groupID) {
                //用来存旧的数据
                var old: _GroupSetting.Games? = null
                //有没有绑定这个服务器
                it.games.removeIf {
                    if (it.gameID == gameID) {
                        //有绑定这个服务器
                        old = it
                        true
                    }else{
                        false
                    }
                }
                val temp = old?.copy(gameID = gameID, name = bindingName) ?: _GroupSetting.Games(gameID = gameID, name = bindingName)
                it.games.add(temp)
                return true
            }
        }
        return false
    }

    /**
     * 移除群组绑定服务器
     * @param groupID Long
     * @param gameID String
     * @return Boolean
     */
    fun removeGroupBindingServer(groupID: Long, gameID: String):Boolean{
        groupSetting.forEach {
            if (it.groupID == groupID) {
                return it.games.removeIf { gameID == it.gameID }
            }
        }
        return false
    }
    /**
     * 获取群组绑定服务器
     * @param groupID Long
     * @param gameID String
     * @return Boolean
     */
    fun getGroupBindingServer(groupID: Long):MutableList<_GroupSetting.Games>?{
        groupSetting.forEach {
            if (it.groupID == groupID) {
                return it.games
            }
        }
        return null
    }
    /**
     * 设置群组是否可以广播消息
     * @param groupID Long
     * @param gameID String
     * @param isEnableBroadcast Boolean
     * @return Boolean
     */
    fun setGroupBroadcast(groupID: Long,gameID: String,isEnableBroadcast: Boolean):Boolean{
        groupSetting.forEach {
            if (it.groupID == groupID) {
                it.games.forEach {
                    if (it.gameID == gameID){
                        it.isEnableBroadcast = isEnableBroadcast
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * 获取广播的群列表
     * @param gameID String
     * @return MutableList<Long> 群列表
     */
    fun getServerBroadcast(gameID: String):MutableList<Long>{
        val temp = mutableListOf<Long>()
        groupSetting.forEach {g->
            g.games.forEach {
                if (it.gameID == gameID) g.groupID?.let { it1 -> temp.add(it1) }
            }
        }
        return temp
    }

    /**
     * 添加群管理
     * @param groupID Long
     * @param operator Long
     */
    fun addOp(groupID: Long,operator: Long):Boolean{
        groupSetting.forEach {
            if (groupID == it.groupID){
                it.operator.add(operator)
                return true
            }
        }
        return false
    }

    /**
     * 移除群管理
     * @param groupID Long
     * @param operator Long
     * @return Boolean
     */
    fun removeOp(groupID: Long,operator: Long):Boolean{
        groupSetting.forEach {
            return it.operator.removeIf { it == operator }
        }
        return false
    }

    /**
     * 获取群管理列表
     * @param groupID Long
     * @param operator Long
     */
    fun groupOpList(groupID: Long):MutableList<Long>{
        var temp = mutableListOf<Long>()
        groupSetting.forEach {
            if (groupID == it.groupID){
                temp = it.operator.toMutableList()
            }
        }
        return temp
    }
}

@Serializable
data class _GroupSetting(
    var groupID: Long? = null,
    var games: MutableList<Games> = mutableListOf(),
    var operator: MutableSet<Long> = mutableSetOf(),
    var eacApiKey: String = "",
) {
    @Serializable
    data class Games(
        var name: String = "",
        var gameID: String = "",
        var isEnableBroadcast: Boolean = false
    )
}