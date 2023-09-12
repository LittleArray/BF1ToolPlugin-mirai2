package top.ffshaozi.utils

import kotlinx.coroutines.*
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.rootDir
import top.ffshaozi.intent.Cache
import java.io.File

/**
 * @Description
 * @Author littleArray
 * @Date 2023/9/6
 */
class HtmlToImage {
    private var plImg = ""
    private var tempFile = ""
    private val chromePath =
        MiraiConsole.INSTANCE.rootDir.path + System.getProperty("file.separator") + "chrome" + System.getProperty("file.separator") + "chrome.exe"
    private var fileName = ""
    private var imgName = ""
    private val random = "${(Math.random() * 10).toInt()}"

    fun toImage(width : Int = 1920 ,height : Int = 1080): Boolean {
        plImg =
            MiraiConsole.INSTANCE.rootDir.path + System.getProperty("file.separator") + "bf1toimg" + System.getProperty(
                "file.separator"
            ) + "${fileName}.${random}.png"
        if (!File(chromePath).exists()) {
            return false
        }
        //chrome.exe --no-sandbox --headless --disable-gpu --screenshot="E:\test.png"  --window-size=1920,1080 http://ipv6.ffshaozi.top/
        val cmd =
            "\"${chromePath}\" --no-sandbox --headless --disable-gpu --screenshot=\"${plImg}\"  --window-size=${width},${height} \"${tempFile}\""
        val process = Runtime.getRuntime().exec(cmd)
        CoroutineScope(Dispatchers.IO).launch {
            delay(15000)
            Runtime.getRuntime().exec("taskkill /f /im chrome.exe")
        }
        process.waitFor()
        runBlocking {
            delay(1000)
        }
        return true
    }

    fun readIt(htmlName: String): String {
        fileName = htmlName
        val plPath =
            MiraiConsole.INSTANCE.rootDir.path + System.getProperty("file.separator") + "bf1toimg" + System.getProperty(
                "file.separator"
            ) + "${fileName}.html"
        val io = File(plPath)
        return io.readText()
    }

    fun writeTempFile(content: String) {
        tempFile =
            MiraiConsole.INSTANCE.rootDir.path + System.getProperty("file.separator") + "bf1toimg" + System.getProperty(
                "file.separator"
            ) + "${fileName}.${random}.temp.html"
        File(tempFile).writeText(content)
    }

    fun getFilePath() : String{
        CoroutineScope(Dispatchers.IO).launch {
            delay(60000)
            removeIt()
        }
       return plImg
    }
    fun removeIt() {
        File(plImg).delete()
        File(tempFile).delete()
    }
    fun getImgPath() = "cache/${imgName}.png"
    fun cacheImg(url:String,imgName:String):Boolean{
        return if (imgName.isNotEmpty()){
            val _imgName = imgName.replace("/","_").replace(" ","_")
            return if (BF1Api.getImg(url,_imgName,true)) {
                this.imgName = _imgName
                true
            }else{
                false
            }
        }else{
            false
        }
    }
}
