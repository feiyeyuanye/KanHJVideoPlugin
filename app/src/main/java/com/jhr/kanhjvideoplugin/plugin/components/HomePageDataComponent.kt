package com.jhr.kanhjvideoplugin.plugin.components

import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.Gravity
import com.jhr.kanhjvideoplugin.plugin.components.Const.host
import com.jhr.kanhjvideoplugin.plugin.components.Const.layoutSpanCount
import com.jhr.kanhjvideoplugin.plugin.util.JsoupUtil
import com.su.mediabox.pluginapi.action.ClassifyAction
import com.su.mediabox.pluginapi.action.CustomPageAction
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.components.IHomePageDataComponent
import com.su.mediabox.pluginapi.data.*
import com.su.mediabox.pluginapi.util.UIUtil.dp

class HomePageDataComponent : IHomePageDataComponent {

    override suspend fun getData(page: Int): List<BaseData>? {
        if (page != 1)
            return null
        val url = host
        val doc = JsoupUtil.getDocument(url)
        val data = mutableListOf<BaseData>()

        //1.横幅
        doc.select(".carousel-inner").apply {
            val bannerItems = mutableListOf<BannerData.BannerItemData>()
            select(".carousel-item").forEach { bannerItem ->
                val nameEm = ""
                val videoUrl = bannerItem.select("a").attr("href").substringAfter(host)
                val bannerImage = host + bannerItem.select("img").attr("src")
                if (bannerImage.isNotBlank()) {
//                    Log.e("TAG", "添加横幅项 封面：$bannerImage 链接：$videoUrl")
                    bannerItems.add(
                        BannerData.BannerItemData(
                            bannerImage, nameEm, nameEm
                        ).apply {
                            if (!videoUrl.isNullOrBlank())
                                action = DetailAction.obtain(videoUrl)
                        }
                    )
                }
            }
            if (bannerItems.isNotEmpty())
                data.add(BannerData(bannerItems, 6.dp).apply {
                    layoutConfig = BaseData.LayoutConfig(layoutSpanCount, 14.dp)
                    spanSize = layoutSpanCount
                })
        }

        // 榜单
        data.add(SimpleTextData("排行榜单").apply {
            fontSize = 15F
            fontStyle = Typeface.BOLD
            fontColor = Color.BLACK
            spanSize = layoutSpanCount / 2
        })
            data.add(SimpleTextData("查看更多 >").apply {
                fontSize = 12F
                gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
                fontColor = Const.INVALID_GREY
                spanSize = layoutSpanCount / 2
            }.apply {
                action = CustomPageAction.obtain(RankPageDataComponent::class.java)
            })

        //3.各类推荐
        val modules = doc.select("div[class='myui-panel myui-panel-bg clearfix']")
        for (em in modules){
            val moduleHeading = em.select(".myui-panel_hd")
            val type = moduleHeading.select(".title")
            val typeName = type.text()
            if (!typeName.isNullOrBlank()) {
                var count = layoutSpanCount
                if (typeName.contains("最新")) {
                    count = layoutSpanCount / 2
                }
                data.add(SimpleTextData(typeName).apply {
                    fontSize = 15F
                    fontStyle = Typeface.BOLD
                    fontColor = Color.BLACK
                    spanSize = count
                })
                if (typeName.contains("最新")) {
                    var typeUrl = ""
                    when (typeName){
                        "最新韩剧" -> typeUrl = "/search.html?searchtype=5&tid=1"
                        "最新韩国电影" -> typeUrl = "/search.html?searchtype=5&tid=2"
                        "最新韩国综艺" -> typeUrl = "/search.html?searchtype=5&tid=3"
                    }
                    data.add(SimpleTextData("查看更多 >").apply {
                        fontSize = 12F
                        gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
                        fontColor = Const.INVALID_GREY
                        spanSize = count
                    }.apply {
                        action = ClassifyAction.obtain(typeUrl, typeName)
                    })
                }
            }

            val li = em.select("ul[class='myui-vodlist clearfix']").select("li")
            for ((index,video) in li.withIndex()){
                video.apply {
                    val name = select("h4").text()
                    val videoUrl = select(".myui-vodlist__thumb").attr("href")
                    val coverUrl = host + select(".myui-vodlist__thumb").attr("data-original")
                    val episode = select("span[class='pic-text text-right']").text()

                    if (!name.isNullOrBlank() && !videoUrl.isNullOrBlank() && !coverUrl.isNullOrBlank()) {
                        data.add(
                            MediaInfo1Data(name, coverUrl, videoUrl, episode ?: "")
                                .apply {
                                    spanSize = layoutSpanCount / 3
                                    action = DetailAction.obtain(videoUrl)
                                })
//                        Log.e("TAG", "添加视频 ($name) ($videoUrl) ($coverUrl) ($episode)")
                    }
                }
                if (index == 11) break
            }
        }
        return data
    }
}