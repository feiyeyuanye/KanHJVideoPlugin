package com.jhr.kanhjvideoplugin.plugin.components

import android.util.Log
import com.jhr.kanhjvideoplugin.plugin.actions.CustomAction
import com.jhr.kanhjvideoplugin.plugin.components.Const.host
import com.jhr.kanhjvideoplugin.plugin.util.JsoupUtil
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.components.ICustomPageDataComponent
import com.su.mediabox.pluginapi.data.BaseData
import com.su.mediabox.pluginapi.data.SimpleTextData
import com.su.mediabox.pluginapi.data.ViewPagerData
import org.jsoup.nodes.Element

class RankPageDataComponent : ICustomPageDataComponent {

    override val pageName = "排行榜"
    override fun menus() = mutableListOf(CustomAction())

    override suspend fun getData(page: Int): List<BaseData>? {
        if (page != 1)
            return null
        val url = "$host/hot/"
        val doc = JsoupUtil.getDocument(url)

        val modules = doc.select(".myui-panel-box").select(".col-lg-3")

        //排行榜
        val hj = modules[0].let {
                    object : ViewPagerData.PageLoader {
                        override fun pageName(page: Int): String {
                            return "韩剧排行榜"
                        }

                        override suspend fun loadData(page: Int): List<BaseData> {
                            return getTotalRankData(it)
                        }
                    }
                }
        val dy = modules[1].let {
            object : ViewPagerData.PageLoader {
                override fun pageName(page: Int): String {
                    return "韩国电影排行榜"
                }

                override suspend fun loadData(page: Int): List<BaseData> {
                    return getTotalRankData(it)
                }
            }
        }
        val zy = modules[2].let {
            object : ViewPagerData.PageLoader {
                override fun pageName(page: Int): String {
                    return "韩国综艺排行榜"
                }

                override suspend fun loadData(page: Int): List<BaseData> {
                    return getTotalRankData(it)
                }
            }
        }

        return listOf(ViewPagerData(mutableListOf(hj, dy , zy)).apply {
            layoutConfig = BaseData.LayoutConfig(
                itemSpacing = 0,
                listLeftEdge = 0,
                listRightEdge = 0
            )
        })
    }
    /**
     * 解析排行
     */
    private fun getTotalRankData(element: Element): List<BaseData> {
        val data = mutableListOf<BaseData>()
        val li = element.select("ul").select("li")
        for (e in li){
            val rankIdx = e.select(".badge").text()
            val textName = e.select("a").attr("title")
            val href = e.select("a").attr("href")
            val rankValue = e.select(".pull-right").text()

            val item = SimpleTextData("[$rankIdx] $textName --$rankValue").apply {
                action = DetailAction.obtain(href)
            }
            data.add(item)
        }
        return data
    }
}