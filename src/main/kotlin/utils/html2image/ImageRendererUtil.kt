package utils.html2image

import cz.vutbr.web.css.MediaSpec
import org.fit.cssbox.awt.GraphicsEngine
import org.fit.cssbox.css.CSSNorm
import org.fit.cssbox.css.DOMAnalyzer
import org.fit.cssbox.io.DOMSource
import org.fit.cssbox.io.DefaultDOMSource
import org.fit.cssbox.io.DefaultDocumentSource
import org.fit.cssbox.io.DocumentSource
import org.fit.cssbox.layout.BrowserConfig
import org.fit.cssbox.layout.Dimension
import org.xml.sax.SAXException
import java.io.IOException
import java.io.OutputStream
import javax.imageio.ImageIO

/**
 * @Description
 * @Author littleArray
 * @Date 2023/8/29
 */
open class ImageRendererUtil {
    private var mediaType = "screen"
    private var windowSize: Dimension
    private var cropWindow = false
    private var loadImages = true
    private var loadBackgroundImages = true

    init {
        windowSize = Dimension(DEFAULT_WIDTH.toFloat(), DEFAULT_HEIGHT.toFloat())
    }

    fun setMediaType(media: String?) {
        mediaType = java.lang.String(media) as String
    }

    fun setWindowSize(size: Dimension?, crop: Boolean) {
        windowSize = Dimension(size)
        cropWindow = crop
    }

    fun setLoadImages(content: Boolean, background: Boolean) {
        loadImages = content
        loadBackgroundImages = background
    }

    /**
     * Renders the URL and prints the result to the specified output stream in the specified
     * format.
     * @param urlstring the source URL
     * @param out output stream
     * @return true in case of success, false otherwise
     * @throws SAXException
     */
    @Throws(IOException::class, SAXException::class)
    fun renderURL(urlstring: String, out: OutputStream?): Boolean {
        var urlstring = urlstring
        if (!urlstring.startsWith(HTTP) && !urlstring.startsWith(HTTPS) && !urlstring.startsWith(FTP)
            && !urlstring.startsWith("file:")
        ) {
            urlstring = "http://$urlstring"
        }

        //Open the network connection
        val docSource: DocumentSource = DefaultDocumentSource(urlstring)

        //Parse the input document
        val parser: DOMSource = DefaultDOMSource(docSource)
        val doc = parser.parse()

        //create the media specification
        val media = MediaSpec(mediaType)
        media.setDimensions(windowSize.width, windowSize.height)
        media.setDeviceDimensions(windowSize.width, windowSize.height)

        //Create the CSS analyzer
        val da = DOMAnalyzer(doc, docSource.url)
        da.mediaSpec = media
        da.attributesToStyles()
        da.addStyleSheet(null, CSSNorm.stdStyleSheet(), DOMAnalyzer.Origin.AGENT)
        da.addStyleSheet(null, CSSNorm.userStyleSheet(), DOMAnalyzer.Origin.AGENT)
        da.addStyleSheet(null, CSSNorm.formsStyleSheet(), DOMAnalyzer.Origin.AGENT)
        da.getStyleSheets()
        val contentCanvas = GraphicsEngine(da.root, da, docSource.url)
        contentCanvas.autoMediaUpdate = false
        contentCanvas.config.clipViewport = cropWindow
        contentCanvas.config.loadImages = loadImages
        contentCanvas.config.loadBackgroundImages = loadBackgroundImages
        contentCanvas.isUseKerning = false
        contentCanvas.createLayout(windowSize)
        ImageIO.write(contentCanvas.image, "png", out)
        docSource.close()
        return true
    }

    /**
     * Sets some common fonts as the defaults for generic font families.
     */
    protected fun defineLogicalFonts(config: BrowserConfig) {
        config.setLogicalFont(BrowserConfig.SERIF, mutableListOf("Times", "Times New Roman"))
        config.setLogicalFont(BrowserConfig.SANS_SERIF, mutableListOf("Arial", "Helvetica"))
        config.setLogicalFont(BrowserConfig.MONOSPACE, mutableListOf("Courier New", "Courier"))
    }

    companion object {
        private const val DEFAULT_WIDTH = 1200
        private const val DEFAULT_HEIGHT = 600
        private const val FTP = "ftp:"
        private const val HTTP = "http:"
        private const val HTTPS = "https:"
    }
}
