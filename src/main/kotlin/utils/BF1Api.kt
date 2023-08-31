package top.ffshaozi.utils

import com.google.gson.Gson
import data.EacInfoJson
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.jsoup.Jsoup
import org.jsoup.internal.StringUtil
import top.ffshaozi.BF1ToolPlugin.Glogger
import top.ffshaozi.data.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap


data class PostResponse(
    var isSuccessful: Boolean = false,
    var error: String = "",
    var reqBody: String = "",
)

data class jsonrpc(
    val jsonrpc: String = "2.0",
    var method: String? = "",
    var params: Any? = "",
    val id: String = UUID.randomUUID().toString()
)

data class jsonrpcR(
    val jsonrpc: String = "2.0",
    var result: Any,
    val id: String = UUID.randomUUID().toString()
)


/*
fun main() {
    println(getFullServerDetails("6f7d8664-42d8-402f-963c-b2ea3c0ee7b9", "8623424970902"))
}
*/

object BF1Api {
    private val okHttpClient = OkHttpClient()

    //服管Api接口
    fun postApi(body: String, sessionId: String = ""): PostResponse {
        return try {
            Glogger.info("服管Api请求:${body} SSID:${sessionId}")
            val request = Request.Builder()
                .url("https://sparta-gw.battlelog.com/jsonrpc/pc/api")
                .post(body.toRequestBody("application/json".toMediaType()))
                .apply {
                    if (sessionId.isNotBlank()) {
                        addHeader("X-GatewaySession", sessionId)
                    }
                }
                .build()

            val response = okHttpClient.newCall(request).execute()
            return if (response.isSuccessful) {
                val res = response.body?.string()
                if (res != null) {
                    if (res.length > 32) Glogger.info(
                        "服管Api请求成功:${
                            res.subSequence(
                                0,
                                31
                            )
                        }"
                    ) else Glogger.info("服管Api请求成功:${res}")
                    PostResponse(isSuccessful = true, reqBody = res)
                } else {
                    Glogger.error("服管Api请求失败")
                    PostResponse(isSuccessful = false, error = "null body")
                }

            } else {
                val res = response.body?.string()
                if (res != null) {
                    Glogger.error("服管Api请求失败:${res}")
                    PostResponse(isSuccessful = false, reqBody = res)
                } else {
                    Glogger.error("服管Api请求失败")
                    PostResponse(isSuccessful = false, error = "null body")
                }
            }
        } catch (ex: Exception) {
            Glogger.error("服管Api请求出错:${ex.stackTraceToString()}")
            PostResponse(isSuccessful = false, error = ex.stackTraceToString())
        }
    }

    //数据Api接口
    fun getApi(action: String, isLog: Boolean = true): PostResponse {
        return try {
            if (isLog)
                Glogger.info("数据Api请求:${action}")
            val request = Request.Builder()
                .url(action)
                .addHeader("Accept", "application/json")
                .build()
            val response = okHttpClient
                .newBuilder()
                .connectTimeout(15, TimeUnit.SECONDS)//设置连接超时时间
                .readTimeout(15, TimeUnit.SECONDS)//设置读取超时时间
                .build()
                .newCall(request).execute()
            if (response.isSuccessful) {
                val res = response.body?.string()
                if (res != null) {
                    if (isLog)
                        if (res.length > 32) Glogger.info(
                            "数据Api请求成功:${
                                res.subSequence(
                                    0,
                                    31
                                )
                            }"
                        ) else Glogger.info("数据Api请求成功:${res}")
                    PostResponse(isSuccessful = true, reqBody = res)
                } else {
                    if (isLog)
                        Glogger.error("数据Api请求失败")
                    PostResponse(isSuccessful = false, error = "null body")
                }
            } else {
                val res = response.body?.string()
                if (res != null) {
                    if (isLog)
                        Glogger.error("数据Api请求失败:${res}")
                    PostResponse(isSuccessful = false, reqBody = res)
                } else {
                    if (isLog)
                        Glogger.error("数据Api请求失败")
                    PostResponse(isSuccessful = false, error = "null body")
                }
            }
        } catch (ex: Exception) {
            if (isLog)
                Glogger.error("数据Api请求出错${ex.stackTraceToString()}")
            PostResponse(isSuccessful = false, error = ex.stackTraceToString())
        }

    }

    //TODO 数据接口类
    //获取数据
    fun getStats(eaid: String): StatsJson {
        val response = getApi("https://api.gametools.network/bf1/stats/?format_values=true&name=${eaid}&lang=zh-Tw")
        return if (response.isSuccessful) {
            //Glogger.info("请求成功转换数据中")
            Gson().fromJson(response.reqBody, StatsJson::class.java).copy(isSuccessful = true)
        } else {
            StatsJson(isSuccessful = false)
        }
    }

    //获取武器
    fun getWeapon(eaid: String): WeaponsJson {
        val response =
            getApi("https://api.gametools.network/bf1/weapons/?format_values=true&name=${eaid}&lang=zh-Tw")
        return if (response.isSuccessful) {
            //Glogger.info("请求成功转换数据中")
            Gson().fromJson(response.reqBody, WeaponsJson::class.java).copy(isSuccessful = true)
        } else {
            WeaponsJson(isSuccessful = false)
        }
    }

    //获取载具
    fun getVehicles(eaid: String): TankJson {
        val response = getApi("https://api.gametools.network/bf1/vehicles/?name=${eaid}&lang=zh-Tw")
        return if (response.isSuccessful) {
            Gson().fromJson(response.reqBody, TankJson::class.java).copy(isSuccessful = true)
        } else {
            TankJson(isSuccessful = false)
        }
    }

    //获取PID
    fun getPersonaid(eaid: String): PlayerJson {
        val response = getApi("https://api.gametools.network/bf1/player/?name=${eaid}&lang=zh-Tw")

        return if (response.isSuccessful) {
            //Glogger.info("请求成功转换数据中")
            Gson().fromJson(response.reqBody, PlayerJson::class.java).copy(isSuccessful = true)
        } else {
            PlayerJson(isSuccessful = false)
        }
    }

    //搜索服务器
    fun searchServer(serverName: String): ServerSearchJson {
        val response =
            getApi("https://api.gametools.network/bf1/servers/?name=${serverName}&limit=10&region=all&lang=zh-Tw")
        return if (response.isSuccessful) {
            //Glogger.info("请求成功转换数据中")
            Gson().fromJson(response.reqBody, ServerSearchJson::class.java).copy(isSuccessful = true)
        } else {
            ServerSearchJson(isSuccessful = false)
        }
    }

    //服务器列表数据
    fun searchServerList(gameId: String, isLog: Boolean = true): ServerListJson {
        val response =
            getApi("https://api.gametools.network/bf1/players/?gameid=${gameId}", isLog)
        return if (response.isSuccessful) {
            //Glogger.info("请求成功转换数据中")
            Gson().fromJson(response.reqBody, ServerListJson::class.java).copy(isSuccessful = true)
        } else {
            ServerListJson(isSuccessful = false)
        }
    }

    //BFEAC查询
    fun seaechBFEAC(eaid: String, isLog: Boolean = true): EacInfoJson {
        val postResponse = getApi("https://api.bfeac.com/case/EAID/$eaid", isLog)
        return if (postResponse.isSuccessful) {
            Gson().fromJson(postResponse.reqBody, EacInfoJson::class.java)
        } else {
            EacInfoJson(error_code = 404)
        }
    }

    //最近查询
    fun recentlySearch(eaid: String, isLog: Boolean = true): List<RecentlyJson> {
        val reqBody = BF1Api.getApi("https://battlefieldtracker.com/bf1/profile/pc/${eaid}", isLog)
        if (!reqBody.isSuccessful) return listOf(RecentlyJson(isSuccessful = false))
        //println(reqBody)
        //document.getElementsByTagName("title")[0].innerHTML
        val document = Jsoup.parse(reqBody.reqBody)
        val elements = document.html(reqBody.reqBody).getElementsByClass("sessions")
        val ltemp = mutableListOf<RecentlyJson>()
        elements.forEach a@{
            val date =
                it.getElementsByClass("time").first()?.getElementsByAttribute("data-livestamp")?.attr("data-livestamp")
            it.getElementsByClass("session-stats").forEach {
                val temp = RecentlyJson()
                it.children().forEachIndexed { index, ctx ->
                    when (index) {
                        0 -> temp.spm = ctx.children().first()?.text()!!
                        1 -> {
                            if (ctx.children().first()?.text()!! == "0.00") {
                                return@a
                            }
                            temp.kd = ctx.children().first()?.text()!!
                        }

                        2 -> temp.kpm = ctx.children().first()?.text()!!
                        3 -> temp.bs = ctx.children().first()?.text()!!
                        4 -> temp.gs = ctx.children().first()?.text()!!
                        5 -> temp.tp = ctx.children().first()?.text()!!
                    }
                }
                temp.rp = SimpleDateFormat("MM-dd HH:mm").format(getDateByStringBIH(date.toString()))
                ltemp.add(temp)
            }
        }
        if (ltemp.size == 0) return listOf(RecentlyJson(isSuccessful = false))
        return ltemp
    }

    fun recentlyServerSearch(eaid: String): MutableSet<RecentlyServerJson> {
        val reqBody = BF1Api.getApi("https://battlefieldtracker.com/bf1/profile/pc/${eaid}/matches", false)
        val document = Jsoup.parse(reqBody.reqBody)
        val elements = document.getElementsByClass("card matches").iterator()
        val data: MutableSet<RecentlyServerJson> = mutableSetOf()
        runBlocking {
            while (elements.hasNext()) {
                val next = elements.next()
                var i = 0
                next.getElementsByClass("match").forEachIndexed { index, it ->
                    if (i > 4) return@forEachIndexed
                    val details = it.getElementsByClass("details").first()?.getElementsByClass("description")?.text()
                    val split = details?.split(" on ")
                    var temp = RecentlyServerJson(
                        map = it.getElementsByClass("details").first()
                            ?.getElementsByClass("title")
                            ?.text()
                            ?.replace("Conquest on ", "[征服]")
                            ?.replace("BreakthroughLarge0 on","[行动]"),
                        serverName = split?.get(0),
                        time = timeTR(split?.get(1))
                    )
                    val matchUrl = "https://battlefieldtracker.com${it.attr("href")}"
                    var matchBody = BF1Api.getApi(matchUrl)
                    if (!matchBody.isSuccessful) {
                        Thread.sleep(8000)
                        matchBody = BF1Api.getApi(matchUrl)
                        if (!matchBody.isSuccessful)
                            return@forEachIndexed
                    }
                    val matchDoc = Jsoup.parse(matchBody.reqBody)
                    val matchIterator = matchDoc.getElementsByClass("player-header").iterator()
                    while (matchIterator.hasNext()) {
                        val matchNext = matchIterator.next()
                        if (matchNext.getElementsByClass("player-name").first()?.text()?.indexOf(eaid) != -1) {
                            matchNext.getElementsByClass("quick-stats").forEach {
                                if (it.child(1).getElementsByClass("value").text() == "0") return@forEachIndexed
                                temp = temp.copy(kills = it.child(1).getElementsByClass("value").text())
                                temp = temp.copy(deaths = it.child(2).getElementsByClass("value").text())
                                temp = temp.copy(kd = it.child(4).getElementsByClass("value").text())
                            }
                        }
                    }
                    i++
                    data.add(temp)
                }
            }
        }
        return data
    }

    fun timeTR(dateS: String?): Date? {
        var date = dateS
        return if (!date.isNullOrEmpty()) {
            date = if (date.indexOf("AM") != -1) {
                date.replace("AM", "PM")
            } else {
                date.replace("PM", "AM")
            }
            val sdf = SimpleDateFormat("M/dd/yyyy hh:mm:ss aa", Locale.ENGLISH)
            val chinaSdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            try {
                val time = Date(sdf.parse(date).time + 1000 * 60 * 60)
                val result = chinaSdf.format(time)
                chinaSdf.parse(result)
            } catch (e: java.lang.Exception) {
                throw RuntimeException("转换为日期类型错误timeTR")
            }
        } else {
            null
        }
    }

    /**
     * 国际标准时间格式String转Date
     * 2023-04-20T16:00:00.000Z
     */
    fun getDateByStringBIH(dateS: String): Date? {
        var date = dateS
        return if (date.isNotEmpty()) {
            date = date.replace("Z", " UTC")
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z")
            val chinaSdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            try {
                val time = sdf.parse(date)
                val result = chinaSdf.format(time)
                chinaSdf.parse(result)
            } catch (e: java.lang.Exception) {
                throw RuntimeException("转换为日期类型错误getDateByStringBIH")
            }
        } else {
            null
        }
    }

    //TODO 服管接口类
    //获取欢迎信息,用于验证ssid是否有效
    fun getWelcomeMessage(sessionId: String): WelcomeMessage {
        val method = "Onboarding.welcomeMessage"
        val body = Gson().toJson(
            jsonrpc(
                method = method,
                params = object {
                    val game = "tunguska"
                    val gameClient = "tunguska"
                    val minutesToUTC = "-480"
                }
            )
        )
        val postResponse = postApi(body, sessionId)
        return if (postResponse.isSuccessful) {
            //Glogger.info("请求成功转换数据中")
            val result = Gson().fromJson(postResponse.reqBody, jsonrpcR::class.java)
            val resData = Gson().fromJson(result.result.toString(), WelcomeMessage::class.java)
            resData.copy(isSuccessful = true)
        } else {
            WelcomeMessage(isSuccessful = false)
        }
    }

    //设置BF1语言
    fun setAPILocale(sessionId: String, locale: String = "zh_TW"): PostResponse {
        val method = "CompanionSettings.setLocale"
        val body = Gson().toJson(
            jsonrpc(
                method = method,
                params = object {
                    val locale = locale
                }
            )
        )
        return postApi(body, sessionId)
    }

    //踢人
    fun kickPlayer(sessionId: String, gameId: String, personaId: String, reason: String): PostResponse {
        val method = "RSP.kickPlayer"
        val body = Gson().toJson(
            jsonrpc(
                method = method,
                params = object {
                    val game = "tunguska"
                    val gameId = gameId
                    val personaId = personaId
                    val reason = reason
                }
            )
        )
        return postApi(body, sessionId)
    }

    //addBan
    fun addServerBan(sessionId: String, RSPserverId: Int, personaName: String): PostResponse {
        val method = "RSP.addServerBan"
        val body = Gson().toJson(
            jsonrpc(
                method = method,
                params = object {
                    val game = "tunguska"
                    val serverId = RSPserverId
                    val personaName = personaName
                }
            )
        )
        return postApi(body, sessionId)
    }

    //removeBan
    fun removeServerBan(sessionId: String, RSPserverId: Int, personaId: String): PostResponse {
        val method = "RSP.removeServerBan"
        val body = Gson().toJson(
            jsonrpc(
                method = method,
                params = object {
                    val game = "tunguska"
                    val serverId = RSPserverId
                    val personaId = personaId
                }
            )
        )
        return postApi(body, sessionId)
    }

    //addVIP
    fun addServerVIP(sessionId: String, RSPserverId: Int, personaName: String): PostResponse {
        val method = "RSP.addServerVip"
        val body = Gson().toJson(
            jsonrpc(
                method = method,
                params = object {
                    val game = "tunguska"
                    val serverId = RSPserverId
                    val personaName = personaName
                }
            )
        )
        return postApi(body, sessionId)
    }

    //removeVIP
    fun removeServerVIP(sessionId: String, RSPserverId: Int, personaId: String): PostResponse {
        val method = "RSP.removeServerVip"
        val body = Gson().toJson(
            jsonrpc(
                method = method,
                params = object {
                    val game = "tunguska"
                    val serverId = RSPserverId
                    val personaId = personaId
                }
            )
        )
        return postApi(body, sessionId)
    }

    //获取服务器完整信息
    fun getFullServerDetails(sessionId: String, gameId: String): FullServerInfoJson {
        val method = "GameServer.getFullServerDetails"
        val body = Gson().toJson(
            jsonrpc(
                method = method,
                params = object {
                    val game = "tunguska"
                    val gameId = gameId
                }
            )
        )
        val postResponse = postApi(body, sessionId)
        return if (postResponse.isSuccessful) {
            Gson().fromJson(postResponse.reqBody, FullServerInfoJson::class.java).copy(isSuccessful = true)
        } else {
            FullServerInfoJson(isSuccessful = false)
        }
    }
}