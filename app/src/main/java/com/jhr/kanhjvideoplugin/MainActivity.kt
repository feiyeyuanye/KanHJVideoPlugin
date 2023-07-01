package com.jhr.kanhjvideoplugin

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = "这是看韩剧 MediaBox插件的主界面"
        })
    }
}
