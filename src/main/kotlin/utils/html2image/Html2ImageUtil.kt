package utils.html2image

import org.fit.cssbox.demo.ImageRenderer
import org.fit.cssbox.layout.Dimension
import java.io.FileOutputStream

/**
 * @Description
 * @Author littleArray
 * @Date 2023/8/29
 */
object Html2ImageUtil {
    fun transferHtml2Image(htmlFilePath: String, imageFilePath: String, width: Int, height: Int) {
        val render = ImageRendererUtil()
        render.setWindowSize(Dimension(width.toFloat(), height.toFloat()), false)
        try {
            val out = FileOutputStream(imageFilePath)
            render.renderURL(htmlFilePath, out)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
