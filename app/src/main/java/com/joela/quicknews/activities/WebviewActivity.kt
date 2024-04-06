/*
Activity that opens a web browser instance with WebView and loads the news URL
 */

package com.joela.quicknews.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.joela.quicknews.R
import com.joela.quicknews.databinding.ActivityWebviewBinding


class WebviewActivity : AppCompatActivity() {

    lateinit var binding: ActivityWebviewBinding
    lateinit var url: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebviewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //Gets the URL from RecycleViewAdapter and open the link in WebView
        url = intent.getStringExtra("url").toString()
        if (url != "null") {
            binding.newsWebview.loadUrl(url)
        }
    }

    fun navigationAction(item: MenuItem){

        when(item.itemId){
            R.id.openbrowser_item ->{
                if(url != "null") {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(browserIntent)
                }
                else{
                    Toast.makeText(this, "Invalid URL", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.close_item ->{
                finish()
            }
            R.id.copy_item ->{
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip: ClipData = ClipData.newPlainText("News URL", url)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Link Copied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}