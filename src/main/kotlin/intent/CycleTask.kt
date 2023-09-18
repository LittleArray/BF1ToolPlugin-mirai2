package top.ffshaozi.intent

import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import top.ffshaozi.NeriQQBot
import top.ffshaozi.utils.BF1Api


object CycleTask {
    fun serverManageRefresh(): Message {
        Cache.ServerManageThreadPool = Thread {
            while (Cache.ServerManageAlive) {
                Cache.refreshServerList()
                NeriQQBot.Glogger.info("服务器列表更新线程")
                Thread.sleep(15 * 1000)
            }
        }
        return if (!Cache.ServerManageAlive) {
            Cache.ServerManageAlive = true
            Cache.ServerManageThreadPool.start()
            "启用玩家管理服务".toPlainText()
        } else {
            Cache.ServerManageAlive = false
            Cache.ServerManageThreadPool.stop()
            "关闭玩家管理服务".toPlainText()
        }
    }

    fun vipRefresh(): Message {
        Cache.VipCThreadPool = Thread {
            while (Cache.VipAlive) {
                //移除VIP
                /*groupData.forEach { groupID, Data ->
                    Data.server.forEach {
                        var removeID = ""
                        it.vipList.forEach { (id, endTime) ->
                            if (System.currentTimeMillis() > endTime) {
                                removeID = id
                            }
                        }
                        if (removeID.isNotEmpty()) {
                            val pid = BF1Api.getPersonaid(removeID)
                            if (BF1Api.removeServerVIP(
                                    it.sessionId.toString(),
                                    it.serverRspID,
                                    pid.id.toString()
                                ).isSuccessful
                            ) {
                                it.vipList.remove(removeID)
                                Cache.sendMessage(it.gameID.toString(), "已移除${removeID}的Vip,原因:过期了")
                            }
                        }
                    }
                }*/
                NeriQQBot.Glogger.info("VIP管理服务")
                Thread.sleep(10000)
            }
        }
        return if (!Cache.VipAlive) {
            Cache.VipAlive = true
            Cache.VipCThreadPool.start()
            "启用VIP管理服务".toPlainText()
        } else {
            Cache.VipAlive = false
            Cache.VipCThreadPool.stop()
            "关闭VIP管理服务".toPlainText()
        }
    }


}
