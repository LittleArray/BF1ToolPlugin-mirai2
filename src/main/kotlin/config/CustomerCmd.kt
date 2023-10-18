package top.ffshaozi.config

import net.mamoe.mirai.console.data.ReadOnlyPluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import top.ffshaozi.config.CustomerCmd.provideDelegate

/*** 自定义命令
 * @Description
 * @Author littleArray
 * @Date 2023/8/25
 */
object CustomerCmd : ReadOnlyPluginConfig("CustomerCmd") {
    @ValueDescription("前缀")
    val prefix by value(listOf("!", "！"))
    @ValueDescription("帮助")
    val help by value(listOf("help", "帮助", "?"))
    @ValueDescription("绑定")
    val binding by value(listOf("bd", "绑定"))
    @ValueDescription("查询你游")
    val bf1 by value(listOf("战地1", "战地一","你游"))
    @ValueDescription("查询黑队")
    val searchBlackTeam by value(listOf("查黑队", "chd"))
    @ValueDescription("查询历史")
    val searchHistory by value(listOf("ls", "历史"))
    @ValueDescription("搜索服务器")
    val ss by value(listOf("ss", "f") )
    @ValueDescription("搜索服务器列表")
    val ssi by value(listOf("ssi", "cxlb"))
    @ValueDescription("查询战绩")
    val stats by value(listOf("c", "查询", "战绩", "我是薯薯", "pro"))
    @ValueDescription("查询载具")
    val vehicle by value(listOf("vp", "载具"))
    @ValueDescription("查询武器")
    val weapon by value(listOf("wp", "武器"))
    @ValueDescription("查询最近")
    val recently by value(listOf("rec", "最近"))
    @ValueDescription("查询玩家列表")
    val playerList by value(listOf("pl", "玩家列表"))
    @ValueDescription("设置服务器kd")
    val setkd by value(listOf("setkd"))
    @ValueDescription("抗压白名单")
    val ky by value(listOf("抗压"))
    @ValueDescription("踢人")
    val kick by value(listOf("k", "kick", "踢人"))
    @ValueDescription("封禁")
    val ban by value(listOf("b", "ban"))
    @ValueDescription("v封禁")
    val vBan by value(listOf("vb", "vban"))
    @ValueDescription("v封禁")
    val warmProgress by value(listOf("nfp", "查进度"))
    @ValueDescription("切图")
    val chooseMap by value(listOf("qt", "切图"))
    @ValueDescription("换边")
    val movePlayer by value(listOf("hb", "换边"))
    @ValueDescription("查询EAC")
    val searchEac by value(listOf("eac", "eacban"))
    @ValueDescription("取消封禁")
    val removeBan by value(listOf("rb", "removeban"))
    @ValueDescription("取消v封禁")
    val removeVBan by value(listOf("rvb", "removevban"))
    @ValueDescription("添加vip")
    val addVip by value(listOf("av", "addvip"))
    @ValueDescription("取消vip")
    val removeVip by value(listOf("rv", "removevip"))
    @ValueDescription("查询Vip")
    val searchVip by value(listOf("gv", "getvip", "查v"))
    @ValueDescription("查询Ban")
    val searchBan by value(listOf("gb", "getban", "查ban"))
}