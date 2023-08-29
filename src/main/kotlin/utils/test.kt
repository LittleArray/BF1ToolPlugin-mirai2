package top.ffshaozi.utils

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Collector.collect
import org.jsoup.select.Elements
import top.ffshaozi.data.RecentlyJson
import top.ffshaozi.utils.BF1Api.getDateByStringBIH

/**
 * @Description
 * @Author littleArray
 * @Date 2023/8/28
 */
fun main() {
    val reqBody = BF1Api.getApi("https://battlefieldtracker.com/bf1/profile/pc/Jerrydonleo/matches",false)
    if (!reqBody.isSuccessful) return
    //println(reqBody)
    //document.getElementsByTagName("title")[0].innerHTML
    val document = Jsoup.parse(reqBody.reqBody)
    val elements = document.html(reqBody.reqBody).getElementsByClass("details")
    val ltemp = hashMapOf<String,String>()
    elements.forEach {
        for(i in 0.. it.childrenSize()){
            it.childNode(i).childNodes().forEach {
                
            }
        }

    }
}