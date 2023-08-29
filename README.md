## Nekomura Haruka の BF1QQRobot
### 背景
项目基于[Mirai2](https://github.com/mamoe/mirai)提供的插件接口开发<br>
用于战地1的[Neri1服](http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=a0qodGbq0pxTsrTgrOUUDLBr3Yb8R1Sc&authKey=JJRLQ7z8TTgfhsRuiJj0N5la1%2BMpugQbQct5bxbN4WJDvbwTaAgMYk9TvnfmVAXK&noverify=0&group_code=702474262)的QQ群提供机器人服务<br>
至于为什么这么干,因为群里机器人寄了,也欢迎各位进群玩
### 用法
#### 插件下载与安装
1. 部署Mirai,详细教程请点击[这里](https://github.com/mamoe/mirai/blob/dev/docs/UserManual.md)
2. 在本项目的```Releases```中下载以```.mirai2.jar```后缀结尾的文件放到Mirai跟目录下的```Plugins```目录中
#### 插件配置
1. 启动Mirai控制台
2. 添加需要使用机器人的群,在控制台使用命令```bf1 group add QQ群号```
3. 添加成功后重启控制台,并在绑定的群内使用```*help```查看是否绑定成功
#### 绑定服务器
1. 在群里输入```*ss 你的服务器名(不需要全称)```搜索服务器
2. 复制搜索到的你的服务器的ServerID
3. 输入```*bds add 你的服务器的ServerID 你的服务器名(不需要全称)```
4. 通过管服工具或其他方法获取到拥有你的服务器管理权限的账号的SessionId
5. 私聊机器人输入```*bdssid All SessionId```如果有回复成功就意味着绑定成功
6. SessionId是临时性的,在每次重启游戏都会变化,此时需要重复第4,5步
 #### 配置修改
1. 配置文件的目录在```Mirai根目录\config\top.ffshaozi.bf1toolplugin```
2. 如果因为配置文件修改错误导致插件无法启动,请删除该目录下的所有文件并重启控制台
### 参与贡献方式
1. 提交```PR```
2. 提交Issue
### 开源协议
AGPL-3.0 license
