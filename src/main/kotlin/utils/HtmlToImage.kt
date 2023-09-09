package top.ffshaozi.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.rootDir
import java.io.File

/**
 * @Description
 * @Author littleArray
 * @Date 2023/9/6
 */
fun htmlToImage(htmlName:String):Boolean{
        val plImg = MiraiConsole.INSTANCE.rootDir.path + System.getProperty("file.separator") + "bf1toimg" + System.getProperty("file.separator") + "${htmlName}.png"
        val tempFile = MiraiConsole.INSTANCE.rootDir.path + System.getProperty("file.separator") + "bf1toimg" + System.getProperty("file.separator") + "${htmlName}.temp.html"
        val chromePath = MiraiConsole.INSTANCE.rootDir.path + System.getProperty("file.separator") + "chrome" + System.getProperty("file.separator") + "chrome.exe"
        if (!File(chromePath).exists()) {
            return false
        }
        //chrome.exe --no-sandbox --headless --disable-gpu --screenshot="E:\test.png"  --window-size=1920,1080 http://ipv6.ffshaozi.top/
        val cmd = "\"${chromePath}\" --no-sandbox --headless --disable-gpu --screenshot=\"${plImg}\"  --window-size=1920,1080 \"${tempFile}\""
        val process = Runtime.getRuntime().exec(cmd)
        CoroutineScope(Dispatchers.IO).launch{
                delay(15000)
                Runtime.getRuntime().exec("taskkill /f /im chrome.exe")
        }
        process.waitFor()
        return true
}
fun readIt(fileName:String):String{
        val plPath = MiraiConsole.INSTANCE.rootDir.path + System.getProperty("file.separator") + "bf1toimg" + System.getProperty("file.separator") + "${fileName}.html"
        val io = File(plPath)
        return io.readText()
}
fun writeTempFile(fileName: String, content:String){
        val tempFile = MiraiConsole.INSTANCE.rootDir.path + System.getProperty("file.separator") + "bf1toimg" + System.getProperty("file.separator") + "${fileName}.temp.html"
        File(tempFile).writeText(content)
}
fun getFilePath(fileName: String) = MiraiConsole.INSTANCE.rootDir.path + System.getProperty("file.separator") + "bf1toimg" + System.getProperty("file.separator") + "${fileName}.png"
fun getImgPath(fileName: String) = "cache/${fileName}.png"