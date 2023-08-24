package top.ffshaozi.command

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.permission.PermissionService.Companion.permit
import top.ffshaozi.BF1ToolPlugin
import top.ffshaozi.command.BF1Cmd.group
import top.ffshaozi.config.Setting
import top.ffshaozi.utils.Value.groups

// 简单指令

object BF1Cmd : CompositeCommand(
    BF1ToolPlugin,
    primaryName = "bf1",
    description = "战地1设置"
) {
    @ExperimentalCommandDescriptors
    override val prefixOptional: Boolean = true

    @SubCommand()
    @Description("群组设定")
    suspend fun CommandSender.group(operation: String, groupId: Long? = null) {
        when (operation) {
            "add" -> {
                if (groupId != null) {
                    Setting.GroupID.put(groupId, mutableSetOf())
                    sendMessage("添加成功")
                } else {
                    sendMessage("添加失败")
                }
            }

            "remove" -> {
                Setting.GroupID.remove(groupId)
                sendMessage("移除成功")
            }

            "get" -> {
                var temp=""
                Setting.GroupID.forEach {
                    temp += "${it.key}+,"
                }
                sendMessage("已绑定群号:$temp")
            }

            "list" -> {
                sendMessage("Bot拥有的群\n$groups")
            }

            else -> {
                sendMessage("无效命令 输入bf1获取设置内容")
            }
        }
    }
}

