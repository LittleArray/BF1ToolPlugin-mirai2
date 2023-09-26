package top.ffshaozi.config

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

/**
 * @Description 全局可用的绑定数据
 * @Author littleArray
 * @Date 2023/9/16
 */
object BackgroundImgData : AutoSavePluginData("BackgroundImgData"){
    @ValueDescription("绑定的qq号")
    var backgroundImgData:MutableMap<Long,BackImg> by value()
    fun addImgOrDef(qq:Long,type:String,url:String):Boolean{
        if (url == "null"){
            val old = backgroundImgData[qq]
            if (old != null){
                when(type){
                    "stats" -> old.statsImg = "assets/MP_Blitz.jpg"
                    "weapon" -> old.weaponImg = "assets/MP_London.jpg"
                    "vehicle" -> old.vehicleImg = "assets/MP_Volga.jpg"
                    else -> return false
                }
                backgroundImgData[qq] = old
            }
            return true
        }else{
            val _backImg = backgroundImgData[qq] ?: BackImg()
            when(type){
                "stats" -> _backImg.statsImg = url
                "weapon" -> _backImg.weaponImg = url
                "vehicle" -> _backImg.vehicleImg = url
                else -> return false
            }
            backgroundImgData[qq] = _backImg
            return true
        }
    }
}
@Serializable
data class BackImg(
    var statsImg:String = "assets/MP_Blitz.jpg",
    var weaponImg:String = "assets/MP_London.jpg",
    var vehicleImg:String = "assets/MP_Volga.jpg"
)