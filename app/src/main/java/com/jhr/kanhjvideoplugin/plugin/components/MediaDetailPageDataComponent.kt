package com.jhr.kanhjvideoplugin.plugin.components

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import com.jhr.kanhjvideoplugin.plugin.components.Const.host
import com.jhr.kanhjvideoplugin.plugin.util.JsoupUtil
import com.su.mediabox.pluginapi.components.IMediaDetailPageDataComponent
import com.su.mediabox.pluginapi.action.ClassifyAction
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.action.PlayAction
import com.su.mediabox.pluginapi.data.*
import com.su.mediabox.pluginapi.util.TextUtil.urlEncode
import com.su.mediabox.pluginapi.util.UIUtil.dp
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

class MediaDetailPageDataComponent : IMediaDetailPageDataComponent {

    override suspend fun getMediaDetailData(partUrl: String): Triple<String, String, List<BaseData>> {
        var cover = ""
        var title = ""
        var desc = ""
        var score = -1F
        // 别名
        var alias = ""
        // 导演
        var director = ""
        // 主演
        var protagonist = ""
        // 更新时间
        var time = ""
        var upState = ""
        val url = Const.host + partUrl
        val tags = mutableListOf<TagData>()
        val details = mutableListOf<BaseData>()

        val document = JsoupUtil.getDocument(url)

        // ------------- 番剧头部信息
        cover = host+document.select(".myui-content__thumb").select("img").attr("data-original")
        title = document.select(".myui-content__detail").select("h1").text()
        // 更新状况
        val upStateItems = document.select(".myui-content__detail").select(".data")
        for (upStateEm in upStateItems){
            val t = upStateEm.text()
            when{
                t.contains("别名：") -> alias = t
                t.contains("集数：") -> {
                    upState = t.substringBefore("年份")
                    // 年份
                    val year = t.substringAfter("年份：")
                    if (year.isNotBlank()){
                        tags.add(TagData(year).apply {
                            action = ClassifyAction.obtain(
                                upStateEm.select("a").attr("href"), "", year
                            )
                        })
                    }
                }
                t.contains("更新：") -> time = t
                t.contains("导演：") -> director = t
                t.contains("主演：") -> protagonist = t
                t.contains("标签：") -> {
                    //类型
                    val typeElements: Elements = upStateEm.select("a")
                    for (l in typeElements.indices) {
                        tags.add(TagData(typeElements[l].text()).apply {
                            action = ClassifyAction.obtain(typeElements[l].attr("href"), "", typeElements[l].text())
                        })
                    }
                }
            }
        }


        //评分
        score = document.select(".myui-content__detail").select(".branch").text().toFloatOrNull() ?: -1F
        //动漫介绍
        desc = document.select("div[class='col-pd text-collapse content']").text()

        // ---------------------------------- 播放列表+header
        val module = document.select(".myui-panel-box")[1]
        val playNameList = module.select(".myui-panel_hd").select("ul").select("li")
        val playEpisodeList = module.select(".tab-pane").select("ul")
        for (index in 0..playNameList.size) {
            val playName = playNameList.getOrNull(index)
            val playEpisode = playEpisodeList.getOrNull(index)
            if (playName != null && playEpisode != null) {

                val episodes = parseEpisodes(playEpisode)

                if (episodes.isNullOrEmpty())
                    continue

                details.add(
                    SimpleTextData(
                        playName.select("a").text() + "(${episodes.size}集)"
                    ).apply {
                        fontSize = 16F
                        fontColor = Color.WHITE
                    }
                )

                details.add(EpisodeListData(episodes))
            }
        }
        // ----------------------------------  系列动漫推荐
        document.select("div[class='myui-panel myui-panel-bg clearfix']")[2].also {
            val series = parseSeries(it)
            if (series.isNotEmpty()) {
                details.add(
                    SimpleTextData("其他系列作品").apply {
                        fontSize = 16F
                        fontColor = Color.WHITE
                    }
                )
                details.addAll(series)
            }
        }
        return Triple(cover, title, mutableListOf<BaseData>().apply {
            add(Cover1Data(cover, score = score).apply {
                layoutConfig =
                    BaseData.LayoutConfig(
                        itemSpacing = 12.dp,
                        listLeftEdge = 12.dp,
                        listRightEdge = 12.dp
                    )
            })
            add(
                SimpleTextData(title).apply {
                    fontColor = Color.WHITE
                    fontSize = 20F
                    gravity = Gravity.CENTER
                    fontStyle = 1
                }
            )
            add(TagFlowData(tags))
            add(
                LongTextData(desc).apply {
                    fontColor = Color.WHITE
                }
            )
            add(SimpleTextData("·$alias").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            add(SimpleTextData("·$director").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            add(SimpleTextData("·$protagonist").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            add(SimpleTextData("·$time").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            add(SimpleTextData("·$upState").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            add(LongTextData(douBanSearch(title)).apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            addAll(details)
        })
    }

    private fun parseEpisodes(element: Element): List<EpisodeData> {
        val episodeList = mutableListOf<EpisodeData>()
        val elements: Elements = element.select("li").select("a")
        for (k in elements.indices) {
            val episodeUrl = elements[k].attr("href")
            episodeList.add(
                EpisodeData(elements[k].text(), episodeUrl).apply {
                    action = PlayAction.obtain(episodeUrl)
                }
            )
        }
        return episodeList
    }

    private fun parseSeries(element: Element): List<MediaInfo1Data> {
        val videoInfoItemDataList = mutableListOf<MediaInfo1Data>()
        val results: Elements = element.select("ul").select("li").select("a")
        for (i in results.indices) {
            val cover = results[i].attr("data-original")
            val title = results[i].attr("title")
            val url = results[i].attr("href")
            val item = MediaInfo1Data(
                title, host + cover, Const.host + url,
                nameColor = Color.WHITE, coverHeight = 120.dp
            ).apply {
                action = DetailAction.obtain(url)
            }
            videoInfoItemDataList.add(item)
        }
        return videoInfoItemDataList
    }

    private fun douBanSearch(name: String) =
        "·豆瓣评分：https://m.douban.com/search/?query=${name.urlEncode()}"
}