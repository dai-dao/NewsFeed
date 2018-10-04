package playster.com.newsfeed

import android.app.Activity
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.withContext
import org.jsoup.Jsoup
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.net.URL
import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import java.io.*


private const val ERR_TAG = "NEWS_DATA"
private const val IO_TAG = "IO_ERR"

private const val TITLE = "title"
private const val AUTHOR = "author"
private const val PUBDATE = "pubDate"
private const val LINK = "link"
private const val DESCRIPTION = "description"


@SuppressLint("Typos")
class NewsFeedLoader private constructor() {

    private lateinit var newsList : MutableList<News>
    private val ns: String? = null
    private val fileName = "news_list.txt"
    private val gson = Gson()

    companion object {
        private val instance = NewsFeedLoader()

        fun getInstance() : NewsFeedLoader {
            return instance
        }
    }

    fun getNewsSize() : Int {
        return newsList.size
    }

    fun getNews(i : Int) : News {
        return newsList[i]
    }

    // This function runs on background
    // Additionally it suspends the coroutine, to make sure the data is available before updating the views
    // This does NOT block the UI thread, it only suspends the co-routine
    suspend fun loadData(activity: Activity) = withContext(Dispatchers.Default) {
        try {
            val cached = readFromFile(activity)
            newsList = gson.fromJson(cached, Array<News>::class.java).toMutableList()
        } catch (e : Exception) {
            newsList = mutableListOf()
            loadDataFromWeb(activity)
            // Write data to file to cache
            writeToFile(activity)
        }
    }

    private fun loadDataFromWeb(activity: Activity) {
        val parserFactory = XmlPullParserFactory.newInstance()
        val parser = parserFactory.newPullParser()

        val url = URL("https://www.cbc.ca/cmlink/rss-topstories")
        val stream = getInputStream(url)

        if (stream != null) {
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(stream, null)
            parser.nextTag() // Go into RSS tag
            parser.nextTag() // Go into channel tag
            processXML(parser)
        } else {
        }
    }

    fun deleteFile(activity: Activity) {
        val file = File(activity.filesDir, fileName)
        file.delete()
    }

    private fun writeToFile(activity: Activity) {
        val dataJson = gson.toJson(newsList)

        try {
            val outputStreamWriter = OutputStreamWriter(activity.openFileOutput(fileName, MODE_PRIVATE))
            outputStreamWriter.write(dataJson)
            outputStreamWriter.close()
        } catch (e: IOException) {
            Log.e(IO_TAG, "File write failed: " + e.toString())
        }
    }

    private fun readFromFile(activity: Activity): String {
        var ret = ""
        try {
            val inputStream = activity.openFileInput(fileName)
            val inputStreamReader = InputStreamReader(inputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            var receiveString = bufferedReader.readLine()
            val stringBuilder = StringBuilder()

            while (receiveString != null) {
                stringBuilder.append(receiveString)
                receiveString = bufferedReader.readLine()
            }

            inputStream.close()
            ret = stringBuilder.toString()
        } catch (e: FileNotFoundException) {
            Log.e(IO_TAG, "File not found: " + e.toString())
        } catch (e: IOException) {
            Log.e(IO_TAG, "Can not read file: " + e.toString())
        }

        return ret
    }

    private fun processXML(parser: XmlPullParser){
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "item" -> newsList.add(readItem(parser))
                else -> skip(parser)
            }
        }
    }

    private fun readItem(parser: XmlPullParser) : News {
        var title = ""
        var pubDate = ""
        var author = ""
        var link = ""
        var description = ""

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "title" -> title = readTag(parser, TITLE)
                "pubDate" -> pubDate = readTag(parser, PUBDATE)
                "author" -> author = readTag(parser, AUTHOR)
                "description" -> description = readTag(parser, DESCRIPTION)
                "link" -> link = readTag(parser, LINK)
                else -> skip(parser)
            }
        }

        val imgLink = parseImgLink(description.trim())
        return News(title, pubDate, author, imgLink, link)
    }

    private fun readTag(parser: XmlPullParser, tag : String) : String {
        parser.require(XmlPullParser.START_TAG, ns, tag)
        val out = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, tag)
        return out
    }

    private fun parseImgLink(description : String) : String {
        val html = Jsoup.parse(description)
        val img = html.select("img").first()
        return img.absUrl("src")
    }

    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }

    private fun getInputStream(url: URL): InputStream? {
        return try {
            url.openConnection().getInputStream()
        } catch (e: IOException) {
            Log.e(ERR_TAG, "Fetch data failed " + e.toString())
            null
        }
    }
}

data class News(val title: String, val pubDate: String, val author: String, val imgLink : String, val link: String)