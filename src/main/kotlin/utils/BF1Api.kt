package top.ffshaozi.utils

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import top.ffshaozi.BF1ToolPlugin
import top.ffshaozi.BF1ToolPlugin.Glogger
import top.ffshaozi.data.*
import java.util.*
import java.util.concurrent.TimeUnit

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


/*fun main() {
    var time = 0
    repeat(10) {
        var tun = 0
        val scope = Thread {
            while (true) {
                time += 1
                tun += 1
                Thread.sleep(1)
            }
        }
        val id = "LittleArray"
        scope.start()
        println(getStats(id))
        println(getPersonaid(id))
        println(getWeapon(id))
        scope.stop()
        println("$tun")
    }

    println((time / 10).toString())
}*/

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
                    Glogger.info("服管Api请求成功:${res}")
                    PostResponse(isSuccessful = true, reqBody = res)
                } else {
                    Glogger.warning("服管Api请求失败")
                    PostResponse(isSuccessful = false, error = "null body")
                }

            } else {
                val res = response.body?.string()
                if (res != null) {
                    Glogger.info("服管Api请求成功:${res}")
                    PostResponse(isSuccessful = false, reqBody = res)
                } else {
                    Glogger.warning("服管Api请求失败")
                    PostResponse(isSuccessful = false, error = "null body")
                }
            }
        } catch (ex: Exception) {
            Glogger.error("服管Api请求出错:${ex.stackTraceToString()}")
            PostResponse(isSuccessful = false, error = ex.stackTraceToString())
        }
    }

    //数据Api接口
    fun getApi(action: String): PostResponse {
        return try {
            Glogger.info("数据Api请求:${action}")
            val request = Request.Builder()
                .url(action)
                .addHeader("Accept", "application/json")
                .build()
            val response = okHttpClient
                .newBuilder()
                .connectTimeout(5, TimeUnit.SECONDS)//设置连接超时时间
                .readTimeout(5, TimeUnit.SECONDS)//设置读取超时时间
                .build()
                .newCall(request).execute()
            if (response.isSuccessful) {
                val res = response.body?.string()
                if (res != null) {
                    Glogger.info("数据Api请求成功:${res}")
                    PostResponse(isSuccessful = true, reqBody = res)
                } else {
                    Glogger.warning("数据Api请求失败")
                    PostResponse(isSuccessful = false, error = "null body")
                }
            } else {
                val res = response.body?.string()
                if (res != null) {
                    Glogger.info("数据Api请求失败:${res}")
                    PostResponse(isSuccessful = false, reqBody = res)
                } else {
                    Glogger.warning("数据Api请求失败")
                    PostResponse(isSuccessful = false, error = "null body")
                }
            }
        } catch (ex: Exception) {
            Glogger.error("数据Api请求出错${ex.stackTraceToString()}")
            PostResponse(isSuccessful = false, error = ex.stackTraceToString())
        }

    }

    //TODO 数据接口类
    //获取数据
    fun getStats(eaid: String): StatsJson {
        val response = getApi("https://jp-api.gametools.network/bf1/stats/?format_values=true&name=${eaid}&lang=zh-Tw")
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
            getApi("https://jp-api.gametools.network/bf1/weapons/?format_values=true&name=${eaid}&lang=zh-Tw")
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
    fun setAPILocale(sessionId: String): PostResponse {
        val method = "CompanionSettings.setLocale"
        val body = Gson().toJson(
            jsonrpc(
                method = method,
                params = object {
                    val locale = "zh_TW"
                }
            )
        )
        return postApi(body, sessionId)
    }
    //踢人
    fun RSPKickPlayer(sessionId: String, gameId: String, personaId: String, reason: String): PostResponse {
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
}