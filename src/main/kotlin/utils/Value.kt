package top.ffshaozi.utils

object Value{
    /*私聊命令:
    *SSID <sessionId> -设置sessionId
    暂时不可用命令:
    *查询  -查询总数据
    *武器  -查询武器
    *载具  -查询载具
    *最近  -查询最近数据
    *对局  -查询对局数据
    *天眼查  -查询玩家服务器相关信息
    *查b  -查被ban的服务器
    *查v  -查拥有vip的服务器
    *查a  -查拥有管理员的服务器
    *服务器 server -查询已绑定服务器
    *查服务器 keyword -关键字查询服务器
    *玩家列表 server <排序>(选) -查询服务器玩家列表
    *alias -查看可添加别名的指令代号
    *+alias 别名 指令代号  -给指令代号添加别名
    *-alias 别名  -删除别名*/
    var groups=""
    val helpText = """
        Nekomura Haruka の BF1QQRobot v1.0.0
        感谢api.gametools.network,battlefieldtracker.com,www.bfeac.com提供的数据接口
        通用指令:
        *help
        命令解释:帮助
        *bd <ID>
        命令解释:绑定EAID
        *ss <SerName> 
        命令解释:查询服务器
        *c <ID?> 
        命令解释:查询数据
        *eac <ID> 
        命令解释:查询BFEAC的Ban
        *rec <ID?> 
        命令解释:查询最近账号数据
        *wp <ID?> 
        命令解释:查询武器
        *vp <ID?> 
        命令解释:查询载具
        *pl <ServerCount> 
        命令解释:查询服务器内玩家列表
        *vips
        命令解释:启动vip管理服务
        *sls
        命令解释:启动玩家列表管理服务
        *<武器类型> <ID?>
        命令解释:查询<武器类型>的数据
        [ 冲锋枪, 霰弹枪, 轻机枪, 配备, 半自动步枪, 佩枪, 近战武器, 手榴弹, 步枪, 战场装备, 驾驶员, 制式步枪]
    """.trimIndent()
    val helpTextAdmin = """
        管理可用指令
        *bds <Operation> <ServerID> <ServerName>
        命令解释:绑定服务器 <Operation> 参数有 [add,remove,无参数]
        *bdssid <ServerID> <SessionId>
        命令解释:绑定服务器的sessionId(私聊设置)ServerID为All时设置群内绑定的所有服务器        
        *k <Id> <理由(繁体或英文)>
        命令解释:踢人 快速理由:*zz 禁止蜘蛛人 *tj 禁止偷家 *nf 暖服战神 *ur 违反规则
        *b <serverCount> <ID>
        命令解释:Ban人
        *rb <serverCount> <ID>
        命令解释:取消ban
        *av <serverCount> <Id> <Time?>
        命令解释:添加vip 时间单位为天,允许小数
        *rv <serverCount> <Id>
        命令解释:移除vip
        *gv <serverCount>
        命令解释:获取群内vip玩家列表
        *gb <serverCount>
        命令解释:获取ban玩家列表
    """.trimIndent()
}