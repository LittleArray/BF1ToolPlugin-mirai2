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
    @ValueDescription("vip管理服务")
    val vips by value(listOf("*vips"))
    @ValueDescription("玩家管理服务")
    val sls by value(listOf("*sls"))
    @ValueDescription("帮助")
    val help by value(listOf("*help", "*帮助", "*?"))
    @ValueDescription("绑定")
    val binding by value(listOf("*bd", "*绑定"))
    @ValueDescription("搜索服务器")
    val ss by value(listOf("*ss", "*f") )
    @ValueDescription("搜索服务器列表")
    val ssi by value(listOf("*ssi", "*cxlb"))
    @ValueDescription("查询战绩")
    val stats by value(listOf("*c", "*查询", "*战绩", "我是薯薯", "pro"))
    @ValueDescription("查询载具")
    val vehicle by value(listOf("*vp", "*载具"))
    @ValueDescription("查询武器")
    val weapon by value(listOf("*wp", "*武器"))
    @ValueDescription("查询最近")
    val recently by value(listOf("*rec", "*最近"))
    @ValueDescription("查询玩家列表")
    val playerList by value(listOf("*pl", "*玩家列表"))
    @ValueDescription("绑定服务器")
    val bindingServer by value(listOf("*bds", "*绑服"))
    @ValueDescription("设置服务器kd")
    val setkd by value(listOf("*setkd"))
    @ValueDescription("抗压白名单")
    val ky by value(listOf("*抗压"))
    @ValueDescription("踢人")
    val kick by value(listOf("*k", "*kick", "*踢人"))
    @ValueDescription("封禁")
    val ban by value(listOf("*b", "*ban"))
    @ValueDescription("切图")
    val chooseMap by value(listOf("*qt", "*切图"))
    @ValueDescription("换边")
    val movePlayer by value(listOf("*hb", "*换边"))
    @ValueDescription("查询EAC")
    val searchEac by value(listOf("*eac", "*eacban"))
    @ValueDescription("取消封禁")
    val removeBan by value(listOf("*rb", "*removeban"))
    @ValueDescription("添加vip")
    val addVip by value(listOf("*av", "*addvip"))
    @ValueDescription("取消vip")
    val removeVip by value(listOf("*rv", "*removevip"))
    @ValueDescription("查询Vip")
    val searchVip by value(listOf("*gv", "*getvip", "*查v"))
    @ValueDescription("查询Ban")
    val searchBan by value(listOf("*gb", "*getban", "*查ban"))
}