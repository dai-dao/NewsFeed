package playster.com.newsfeed

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.news_layout.view.*


class NewsFeedAdapter (
        private val activity: Activity
) : RecyclerView.Adapter<NewsFeedAdapter.NewsViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): NewsViewHolder {
        val context = p0.context
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.news_layout, p0, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, index: Int) {
        val news = NewsFeedLoader.getInstance().getNews(index)
        holder.title.text = activity.getString(R.string.title_text, news.title)
        holder.pubDate.text = activity.getString(R.string.pubdate_text, news.pubDate)
        holder.author.text = activity.getString(R.string.author_text, news.author)
        holder.link = news.link
        // Glide will automatically cache the image
        Glide.with(activity)
                .load(news.imgLink)
                .into(holder.img)
    }

    override fun getItemCount(): Int {
        return NewsFeedLoader.getInstance().getNewsSize()
    }

    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener  {
        var title: TextView = itemView.title_text
        var pubDate: TextView = itemView.pubdate_text
        var author: TextView = itemView.author_text
        var img: ImageView = itemView.news_image
        var link = ""

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View) {
            val intent = Intent(activity, NewsDetailActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra(NewsDetailActivity.URL, link)
            activity.startActivity(intent)
        }
    }
}