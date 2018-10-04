package playster.com.newsfeed

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_news_detail.*


class NewsDetailActivity : AppCompatActivity() {

    companion object {
        val URL = "url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_detail)

        //
        setBar()

        //
        val url = intent.getStringExtra(URL)

        //
        setWebView(url)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setWebView(url : String) {
        val settings = news_webview.settings
        settings.allowContentAccess = true
        settings.domStorageEnabled = true
        news_webview.webViewClient = WebViewClient()
        news_webview.loadUrl(url)
    }

    private fun setBar() {
        val appBar = supportActionBar
        appBar?.setDisplayHomeAsUpEnabled(true)
    }
}
