package top.ffshaozi.config

import net.mamoe.mirai.console.data.ReadOnlyPluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import top.ffshaozi.config.Setting.provideDelegate

/**
 * @Description
 * @Author littleArray
 * @Date 2023/8/25
 */
object CustomerLang :ReadOnlyPluginConfig("CustomerLang") {
    @ValueDescription("未知命令")
    val errCommand by value("未知命令 //err//")
    @ValueDescription("参数错误")
    val parameterErr by value("参数错误喵~ //para//")
    @ValueDescription("不是管理员")
    val notAdminErr by value("不是管理喵~")
    @ValueDescription("没有绑定的服务器错误")
    val nullServerErr by value("不存在绑定的//err//服务器~")
    @ValueDescription("数据刷新中")
    val serverInfoRefreshing by value("数据刷新中,请稍等~")
    @ValueDescription("数据刷新失败")
    val serverInfoRErr by value("数据刷新失败,请检查绑定的服务器信息~")
    @ValueDescription("绑定成功")
    val bindingSucc by value("绑定成功 //id//")
    @ValueDescription("解绑成功")
    val unbindingSucc by value("解绑成功 //id//")
    @ValueDescription("未绑定错误")
    val unbindingErr by value("未绑定EAID 命令:*bd ID")
    @ValueDescription("EAC找不到Ban")
    val nullEac by value("//id//没有BFEAC的记录哦~")
    @ValueDescription("查询中")
    val searching by value("//id//的//action//查询中~")
    @ValueDescription("查询服务器中")
    val searchingSer by value("正在查询//ser//中~")
    @ValueDescription("查询失败")
    val searchErr by value("//action//查询失败喵~")
    @ValueDescription("踢人成功")
    val kickSucc by value("//id//在服务器//serverCount//中被踹飞了,理由://res//")
    @ValueDescription("踢人失败")
    val kickErr by value("//id//逃过一劫,没被踹飞\n//err//")
    @ValueDescription("封禁成功")
    val banSucc by value("//id//在服务器//serverCount//被上市了")
    @ValueDescription("封禁失败")
    val banErr by value("//id//逃过一劫,没被上市\n//err//")
    @ValueDescription("取消封禁成功")
    val unBanSucc by value("服务器//serverCount//终于对//id//开放了,恭喜喵~")
    @ValueDescription("取消封禁失败")
    val unBanErr by value("//id//依旧上市\n//err//")
    @ValueDescription("加入VIP成功")
    val addVIPSucc by value("//id//在服务器//serverCount//中获得了//Time//VIP")
    @ValueDescription("加入VIP失败")
    val addVIPErr by value("//id//没能得到VIP\n//err//")
    @ValueDescription("移除VIP成功")
    val unVIPSucc by value("//id//失去了服务器//serverCount//的VIP")
    @ValueDescription("移除VIP失败")
    val unVIPErr by value("//id//的VIP移除失败\n//err//")
}