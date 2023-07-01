package com.jhr.kanhjvideoplugin.plugin.util

import android.util.Log
import com.jhr.kanhjvideoplugin.plugin.components.Const.host
import com.jhr.kanhjvideoplugin.plugin.components.Const.layoutSpanCount
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import com.su.mediabox.pluginapi.action.ClassifyAction
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.data.*
import com.su.mediabox.pluginapi.util.UIUtil.dp
import java.net.URL

object ParseHtmlUtil {

    fun getCoverUrl(cover: String, imageReferer: String): String {
        return when {
            cover.startsWith("//") -> {
                try {
                    "${URL(imageReferer).protocol}:$cover"
                } catch (e: Exception) {
                    e.printStackTrace()
                    cover
                }
            }
            cover.startsWith("/") -> {
                //url不全的情况
                host + cover
            }
            else -> cover
        }
    }

    /**
     * 解析搜索的元素
     * @param element ul的父元素
     */
    fun parseSearchEm(
        element: Element,
        imageReferer: String
    ): List<BaseData> {
        val videoInfoItemDataList = mutableListOf<BaseData>()

        val lpic: Elements = element.select(".col-md-wide-7").select(".myui-panel_bd").select("ul")
        val results: Elements = lpic.select("li")
        for (i in results.indices) {
            var cover = results[i].select(".thumb").select("a").attr("data-original")
            if (imageReferer.isNotBlank())
                cover = getCoverUrl(cover, imageReferer)
            val title = results[i].select(".detail").select(".title").text()
            val url = results[i].select(".detail").select(".title").select("a").attr("href")
            val episode = results[i].select(".thumb").select(".pic-text").text()
            val tags = mutableListOf<TagData>()
            val tag = results[i].select(".detail").select("p")[3].text().substringAfter("分类：").substringBefore("地区：")
            tags.add(TagData(tag))
            val describe = results[i].select(".detail").select("p")[0].text()
            val item = MediaInfo2Data(
                title, cover, host + url, episode, describe, tags
            ).apply {
                    action = DetailAction.obtain(url)
                }
            videoInfoItemDataList.add(item)
        }
        return videoInfoItemDataList
    }
    /**
     * 解析分类下的元素
     *
     * @param element ul的父元素
     */
    fun parseClassifyEm(
        element: Element,
        imageReferer: String
    ): List<BaseData> {
        val videoInfoItemDataList = mutableListOf<BaseData>()
        val results: Elements = element.select(".myui-panel-box")[1]
            .select(".myui-panel_bd").select("ul").select("li")
        for (i in results.indices) {
            var cover = results[i].select("a").attr("data-original")
            if (imageReferer.isNotBlank())
                cover = getCoverUrl(cover, imageReferer)
            val title = results[i].select(".myui-vodlist__detail").select(".title").text()
            val url = results[i].select(".myui-vodlist__detail").select("a").attr("href")
            val episode = results[i].select(".pic-text").text()
            val item = MediaInfo1Data(title, cover, host + url, episode ?: "")
                .apply {
                    layoutConfig = BaseData.LayoutConfig(layoutSpanCount, 14.dp)
                    spanSize = layoutSpanCount / 3
                    action = DetailAction.obtain(url)
                }
            videoInfoItemDataList.add(item)
        }
        return videoInfoItemDataList
    }
    /**
     * 解析分类元素
     */
    fun parseClassifyEm(element: Element): List<ClassifyItemData> {
        val classifyItemDataList = mutableListOf<ClassifyItemData>()
        var classifyCategory = ""
        val li = element.select("li")
        for ((index,em) in li.withIndex()){
            val a = em.select("a")
            if (index == 0){
                classifyCategory = a.text()
            }else{
                classifyItemDataList.add(ClassifyItemData().apply {
                    action = ClassifyAction.obtain(
                        a.attr("href").apply {
                            Log.d("分类链接", this)
                        },
                        classifyCategory,
                        a.text()
                    )
                })
            }
        }
        return classifyItemDataList
    }
}