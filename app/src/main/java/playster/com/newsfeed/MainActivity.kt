package playster.com.newsfeed

import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch
import android.support.test.espresso.idling.CountingIdlingResource
import android.widget.Toast


class MainActivity : AppCompatActivity() {

    companion object {
        // this idling resource is used for testing
        val espressoTestIdlingResource = CountingIdlingResource("Load_News_Feed")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val activity = this

        espressoTestIdlingResource.increment()
        // This co-routine runs on the UI context so it can freely updates the UI
        // Non-blocking the UI thread
        GlobalScope.launch(Dispatchers.Main) {
            //

            // Load data for the first time from the web
            NewsFeedLoader.getInstance().loadData(activity)

            // Finish loading data, now load views
            setRecyclerView()

            //
            espressoTestIdlingResource.decrement()
        }
    }

    private fun setRecyclerView() {
        if (NewsFeedLoader.getInstance().getNewsSize() == 0) {
            Toast.makeText(this, "Could not fetch data online or offline. Please try again!",
                    Toast.LENGTH_LONG).show()
        } else {
            val manager = GridLayoutManager(this, 1)
            rv_news_feed.layoutManager = manager
            val adapter = NewsFeedAdapter(this)
            rv_news_feed.adapter = adapter
            rv_news_feed.setHasFixedSize(true)
            rv_news_feed.setItemViewCacheSize(NewsFeedLoader.getInstance().getNewsSize())
        }
    }
}
