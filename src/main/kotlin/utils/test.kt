package top.ffshaozi.utils

import org.jsoup.Jsoup
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import top.ffshaozi.data.RecentlyJson
import top.ffshaozi.data.RecentlyServerJson
import java.text.SimpleDateFormat
import java.util.*


/**
 * @Description
 * @Author littleArray
 * @Date 2023/8/28
 */
fun main() {
    //val reqBody = BF1Api.getApi("https://battlefieldtracker.com/bf1/profile/pc/Jerrydonleo/matches",false)
    //if (!reqBody.isSuccessful) return
    //Html2ImageUtil.transferHtml2Image(File("d:\\test.html").toURI().toString(), "D:" + File.separator + "html2.png", 1920, 1080);
    println(SimpleDateFormat("M/dd/yyyy hh:mm:ss aa", Locale.ENGLISH).parse("8/29/2023 9:01:30 PM"))
    return
    val reqBody = BF1Api.getApi("https://battlefieldtracker.com/bf1/profile/pc/LittleArray/matches", false)
    val document = Jsoup.parse(reqBody.reqBody)
    val elements = document.getElementsByClass("card matches").iterator()
    val data: MutableSet<RecentlyServerJson> = mutableSetOf()
    while (elements.hasNext()) {
        val next = elements.next()
        var i = 0
        next.getElementsByClass("match").forEachIndexed {index ,it ->
            if (i > 4) return@forEachIndexed
            val details = it.getElementsByClass("details").first()?.getElementsByClass("description")?.text()
            val split = details?.split(" on ")
            var temp = RecentlyServerJson(
                map = it.getElementsByClass("details").first()
                    ?.getElementsByClass("title")
                    ?.text()
                    ?.replace("Conquest on ", ""),
                serverName = split?.get(0),
            )
            val matchUrl = "https://battlefieldtracker.com${it.attr("href")}"
            val matchBody = BF1Api.getApi(matchUrl, false)
            if (matchBody.isSuccessful) {
                val matchDoc = Jsoup.parse(matchBody.reqBody)
                val matchIterator = matchDoc.getElementsByClass("player-header").iterator()
                while (matchIterator.hasNext()) {
                    val matchNext = matchIterator.next()
                    if (matchNext.getElementsByClass("player-name").first()?.text()?.indexOf("LittleArray") != -1) {
                        matchNext.getElementsByClass("quick-stats").forEach {
                            if (it.child(1).getElementsByClass("value").text() == "0") return@forEachIndexed
                            temp = temp.copy(kills = it.child(1).getElementsByClass("value").text())
                            temp = temp.copy(deaths = it.child(2).getElementsByClass("value").text())
                            temp = temp.copy(kd = it.child(4).getElementsByClass("value").text())
                        }
                    }
                }
                data.add(temp)
                i++
            }
        }
    }
    println(data)
}

