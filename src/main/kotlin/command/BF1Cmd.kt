package top.ffshaozi.command

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import top.ffshaozi.NeriQQBot
import top.ffshaozi.config.DataForGroup
import top.ffshaozi.config.Setting
import top.ffshaozi.config.Setting.groupData
import top.ffshaozi.config.SettingController
import top.ffshaozi.intent.Cache.BotGroups

// 简单指令

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
            sendMessage("${groupData[groupId]?.operator}")
        } else {
            groupId?.let { SettingController.setOperator(opId, it) }
        }
    }

    @SubCommand()
    @Description("群组设定")
    suspend fun CommandSender.group(operation: String, groupId: Long? = null) {
        when (operation) {
            "add" -> {
                if (groupId != null) {
                    Setting.groupData[groupId] = DataForGroup()
                    sendMessage("添加成功")
                } else {
                    sendMessage("添加失败")
                }
            }

            "remove" -> {
                Setting.groupData.remove(groupId)
                sendMessage("移除成功")
            }

            "get" -> {
                var temp = ""
                Setting.groupData.forEach {
                    temp += "${it.key}+,"
                }
                sendMessage("已绑定群号:$temp")
            }

            "list" -> {
                sendMessage("Bot拥有的群\n$BotGroups")
            }

            else -> {
                sendMessage("无效命令 输入bf1获取设置内容")
            }
        }
    }
}


