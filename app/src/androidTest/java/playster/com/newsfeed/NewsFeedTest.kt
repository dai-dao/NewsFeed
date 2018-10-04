package playster.com.newsfeed

import android.content.Context
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.IdlingRegistry
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import android.support.test.espresso.intent.rule.IntentsTestRule
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.GrantPermissionRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import android.net.wifi.WifiManager


@RunWith(AndroidJUnit4::class)
@LargeTest
class NewsFeedTest {

    @get:Rule
    val activityTestRule = IntentsTestRule(MainActivity::class.java, true, false)

    @get:Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.ACCESS_WIFI_STATE,
            android.Manifest.permission.CHANGE_WIFI_STATE
    )

    @Before
    fun init() {
        IdlingRegistry.getInstance().register(MainActivity.espressoTestIdlingResource)
        activityTestRule.launchActivity(null)
        NewsFeedLoader.getInstance().deleteFile(activityTestRule.activity)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(MainActivity.espressoTestIdlingResource)
        setWifi(true)
    }

    private fun setWifi(enabled : Boolean) {
        val wifiManager = activityTestRule.activity.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiManager.isWifiEnabled = enabled
    }

    @Test
    fun testNewsLoadedWifi() {
        for (wifi in listOf(true, false)) {
            setWifi(wifi)
            onView(withId(R.id.rv_news_feed)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun testNewsDetail() {
        onView(withId(R.id.rv_news_feed))
                .perform(RecyclerViewActions.actionOnItemAtPosition<NewsFeedAdapter.NewsViewHolder>(5, click()))
        onView(withId(R.id.news_webview)).check(matches(isDisplayed()))
    }
}