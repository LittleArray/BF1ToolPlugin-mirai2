package top.ffshaozi.intent

import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.GroupTempMessageEvent

/*** 传入数据类
 * @Description
 * @Author littleArray
 * @Date 2023/9/4
 */
data class PullIntent(
    val event: GroupMessageEvent,
    val sp: List<String>,
    val isAdmin: Boolean,
    val cmdSize: Int,
)

data class PullIntentTemp(
    val event: GroupTempMessageEvent,
    val sp: List<String>,
    val isAdmin: Boolean,
    val cmdSize: Int,
)