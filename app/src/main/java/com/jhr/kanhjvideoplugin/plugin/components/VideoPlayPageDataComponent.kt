package com.jhr.kanhjvideoplugin.plugin.components

import android.util.Log
import com.jhr.kanhjvideoplugin.plugin.components.Const.host
import com.jhr.kanhjvideoplugin.plugin.components.Const.ua
import com.jhr.kanhjvideoplugin.plugin.danmaku.OyydsDanmaku
import com.jhr.kanhjvideoplugin.plugin.danmaku.OyydsDanmakuParser
import com.jhr.kanhjvideoplugin.plugin.util.JsoupUtil
import com.jhr.kanhjvideoplugin.plugin.util.Text.trimAll
import com.jhr.kanhjvideoplugin.plugin.util.oyydsDanmakuApis
import com.kuaishou.akdanmaku.data.DanmakuItemData
import com.su.mediabox.pluginapi.components.IVideoPlayPageDataComponent
import com.su.mediabox.pluginapi.data.VideoPlayMedia
import com.su.mediabox.pluginapi.util.PluginPreferenceIns
import com.su.mediabox.pluginapi.util.WebUtil
import com.su.mediabox.pluginapi.util.WebUtilIns
import kotlinx.coroutines.*

class VideoPlayPageDataComponent : IVideoPlayPageDataComponent {

    private var episodeDanmakuId = ""
    override suspend fun getDanmakuData(
        videoName: String,
        episodeName: String,
        episodeUrl: String
    ): List<DanmakuItemData>? {
        try {
            val config = PluginPreferenceIns.get(OyydsDanmaku.OYYDS_DANMAKU_ENABLE, true)
            if (!config)
                return null
            val name = videoName.trimAll()
            var episode = episodeName.trimAll()
            //剧集对集去除所有额外字符，增大弹幕适应性
            val episodeIndex = episode.indexOf("集")
            if (episodeIndex > -1 && episodeIndex != episode.length - 1) {
                episode = episode.substring(0, episodeIndex + 1)
            }
            Log.d("请求Oyyds弹幕", "媒体:$name 剧集:$episode")
            return oyydsDanmakuApis.getDanmakuData(name, episode).data.let { danmukuData ->
                val data = mutableListOf<DanmakuItemData>()
                danmukuData?.data?.forEach { dataX ->
                    OyydsDanmakuParser.convert(dataX)?.also { data.add(it) }
                }
                episodeDanmakuId = danmukuData?.episode?.id ?: ""
                data
            }
        } catch (e: Exception) {
            throw RuntimeException("弹幕加载错误：${e.message}")
        }
    }

    override suspend fun putDanmaku(
        videoName: String,
        episodeName: String,
        episodeUrl: String,
        danmaku: String,
        time: Long,
        color: Int,
        type: Int
    ): Boolean = try {
        Log.d("发送弹幕到Oyyds", "内容:$danmaku 剧集id:$episodeDanmakuId")
        oyydsDanmakuApis.addDanmaku(
            danmaku,
            //Oyyds弹幕标准时间是秒
            (time / 1000F).toString(),
            episodeDanmakuId,
            OyydsDanmakuParser.danmakuTypeMap.entries.find { it.value == type }?.key ?: "scroll",
            String.format("#%02X", color)
        )
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }

    /**
     * bug -> 标题会显示上一个视频的标题
     */
    override suspend fun getVideoPlayMedia(episodeUrl: String): VideoPlayMedia {
        val url = host + episodeUrl
        val document = JsoupUtil.getDocument(url)
        Log.e("TAG", url)

        val cookies = mapOf("cookie" to PluginPreferenceIns.get(JsoupUtil.cfClearanceKey, ""))
        //解析链接
        val videoUrl = withContext(Dispatchers.Main) {
            val iframeUrl = withTimeoutOrNull(10 * 1000) {
                WebUtilIns.interceptResource(
                    url, "(.*).m3u8",
                    loadPolicy = object : WebUtil.LoadPolicy by WebUtil.DefaultLoadPolicy {
                        override val headers = cookies
                        override val userAgentString = ua
                        override val isClearEnv = false
                    }
                )
            } ?: ""
            async {
                // https://pptv.sd-play.com/202305/31/CYALmmRnfD3/video/index.m3u8
                // https://cdn.zoubuting.com/20210707/upRFRIgW/index.m3u8
                Log.e("TAG", iframeUrl)
                when {
                    iframeUrl.isBlank() -> iframeUrl
                    iframeUrl.endsWith(".m3u8") -> iframeUrl
                    else -> {}
                }
            }
        }
        //剧集名
        val name = withContext(Dispatchers.Default) {
            async {
                document.select("div[class='tips close-box']").select("ul").select("li")[0].text()
            }
        }
        Log.e("TAG", name.await())
        Log.e("TAG", videoUrl.await() as String)
        return VideoPlayMedia(name.await(), videoUrl.await() as String)
    }

}