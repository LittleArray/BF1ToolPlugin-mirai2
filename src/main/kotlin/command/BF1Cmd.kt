package top.ffshaozi.command

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import top.ffshaozi.NeriQQBot
import top.ffshaozi.config.GroupSetting
import top.ffshaozi.config.ServerInfos
import top.ffshaozi.intent.Cache.BotGroups
import top.ffshaozi.intent.CycleTask

/**
 * 这一部分是高级管理命令设置
 */

object BF1Cmd : CompositeCommand(
    NeriQQBot,
    primaryName = "bf1",
    description = "战地1设置"
) {
    @ExperimentalCommandDescriptors
    override val prefixOptional: Boolean = true

    @SubCommand()
    @Description("管理设定")
    suspend fun CommandSender.op(groupId: Long?, opId: Long? = null) {
        if (opId == null) {
            sendMessage("${groupId?.let { GroupSetting.groupOpList(it) }}")
        } else {
            groupId?.let {
                GroupSetting.addOp(groupId, opId)
                sendMessage("成功")
            }
        }
    }

    @SubCommand()
    @Description("群组设定")
    suspend fun CommandSender.group(operation: String, groupId: Long? = null) {
        when (operation) {
            "add" -> {
                if (groupId != null) {
                    GroupSetting.addGroup(groupId)
                    sendMessage("添加成功")
                } else {
                    sendMessage("添加失败")
                }
            }

            "remove" -> {
                if (groupId != null) {
                    GroupSetting.removeGroup(groupId)
                    sendMessage("移除成功")
                } else {
                    sendMessage("移除失败 群组ID不为空")
                }
            }

            "get" -> {
                sendMessage("已绑定群号:${GroupSetting.getGroupList()}")
            }

            "list" -> {
                sendMessage("Bot拥有的群\n$BotGroups")
            }

            else -> {
                sendMessage(
                    """
                    存在命令:
                    add
                    remove
                    get
                    list
                """.trimIndent()
                )
            }
        }
    }

    @SubCommand()
    @Description("移除服务器")
    suspend fun CommandSender.removeServer(gameID: String) {
        if (ServerInfos.removeServer(gameID)) {
            sendMessage("移除成功")
        } else {
            sendMessage("移除失败")
        }
    }

    @SubCommand()
    @Description("设置ssid")
    suspend fun CommandSender.setSSID(gameID: String, ssid: String) {
        if (ServerInfos.updateServerSSID(gameID, ssid,gameID == "All")) {
            sendMessage("更新成功")
        } else {
            sendMessage("更新失败")
        }
    }

    @SubCommand()
    @Description("更新服务器")
    suspend fun CommandSender.updateServer(gameID: String) {
        if (ServerInfos.updateServer(gameID)) {
            sendMessage("更新成功")
        } else {
            sendMessage("更新失败")
        }
    }

    @SubCommand()
    @Description("添加服务器")
    suspend fun CommandSender.addServer(gameID: String, name: String) {
        ServerInfos.addServer(name, gameID)
        sendMessage("服务器添加成功")
    }

    @SubCommand()
    @Description("连接服务器")
    suspend fun CommandSender.linkServer(groupId: Long, gameID: String, serverName: String) {
        if (GroupSetting.addGroupBindingServer(groupId, gameID, serverName)) {
            sendMessage("连接成功")
        } else {
            sendMessage("连接失败")
        }
    }

    @SubCommand()
    @Description("移除连接服务器")
    suspend fun CommandSender.unlinkServer(groupId: Long, gameID: String) {
        if (GroupSetting.removeGroupBindingServer(groupId, gameID)) {
            sendMessage("移除成功")
        } else {
            sendMessage("移除失败")
        }
    }

    @SubCommand()
    @Description("获取群组连接服务器")
    suspend fun CommandSender.getLinkServer(groupId: Long) {
        val games = GroupSetting.getGroupBindingServer(groupId)
        var temp  = "群组$groupId 连接的服务器如下\n"
        games?.forEach {
            temp += """
                服务器设定名:${it.name}
                是否在此群广播:${it.isEnableBroadcast}
                GameID:${it.gameID}
            """.trimIndent()+"\n"
        }
        sendMessage(temp)
    }

    @SubCommand()
    @Description("设置广播")
    suspend fun CommandSender.groupBro(groupId: Long, gameID: String, isEnableBroadcast: Boolean) {
        if (GroupSetting.setGroupBroadcast(groupId, gameID, isEnableBroadcast)) {
            sendMessage("设置成功")
        } else {
            sendMessage("设置失败")
        }
    }

    @SubCommand()
    @Description("玩家管理服务")
    suspend fun CommandSender.sls() {
        val message = CycleTask.serverManageRefresh()
        sendMessage(message)
    }

    @SubCommand()
    @Description("VIP管理服务")
    suspend fun CommandSender.vips() {
        val message = CycleTask.vipRefresh()
        sendMessage(message)
    }
}


