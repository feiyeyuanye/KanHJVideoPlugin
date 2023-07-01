package com.jhr.kanhjvideoplugin.plugin.components

import android.util.Log
import com.jhr.kanhjvideoplugin.plugin.util.JsoupUtil
import com.jhr.kanhjvideoplugin.plugin.util.ParseHtmlUtil
import com.su.mediabox.pluginapi.action.ClassifyAction
import com.su.mediabox.pluginapi.components.IMediaClassifyPageDataComponent
import com.su.mediabox.pluginapi.data.BaseData
import com.su.mediabox.pluginapi.data.ClassifyItemData
import com.su.mediabox.pluginapi.util.PluginPreferenceIns
import com.su.mediabox.pluginapi.util.TextUtil.urlDecode
import com.su.mediabox.pluginapi.util.WebUtil
import com.su.mediabox.pluginapi.util.WebUtilIns
import org.jsoup.Jsoup

class MediaClassifyPageDataComponent : IMediaClassifyPageDataComponent {
    var classify : String = Const.host +"/search.html?searchtype=5&tid=1"

    override suspend fun getClassifyItemData(): List<ClassifyItemData> {
        val classifyItemDataList = mutableListOf<ClassifyItemData>()
        //示例：使用WebUtil解析动态生成的分类项
        // https://www.kangii.com/search.html?searchtype=5&tid=1
        // https://www.kangii.com/search.html?searchtype=5&tid=1&year=2022
        // https://www.kangii.com/search.html?page=1&searchtype=5&order=hit&tid=1&year=2022
        val cookies = mapOf("cookie" to PluginPreferenceIns.get(JsoupUtil.cfClearanceKey, ""))
        Log.e("TAG","classify ${classify}")
        val document = Jsoup.parse(
            WebUtilIns.getRenderedHtmlCode(
                 classify, loadPolicy = object :
                    WebUtil.LoadPolicy by WebUtil.DefaultLoadPolicy {
                    override val headers = cookies
                    override val userAgentString = Const.ua
                    override val isClearEnv = false
                }
            )
        )
        document.select(".myui-panel-box")[0].select(".myui-panel_bd").select("ul").forEach {
            classifyItemDataList.addAll(ParseHtmlUtil.parseClassifyEm(it))
        }
        return classifyItemDataList
    }

    override suspend fun getClassifyData(
        classifyAction: ClassifyAction,
        page: Int
    ): List<BaseData> {
        val classifyList = mutableListOf<BaseData>()
        Log.e("TAG", "获取分类数据 ${classifyAction.url}")

        val str = classifyAction.url?.urlDecode() ?: ""
        var url = str.replace("?","?page=${page}&")
        if (!url.startsWith(Const.host)){
            url = Const.host + url
        }
        Log.e("TAG", "获取分类数据 $url")

        val document = JsoupUtil.getDocument(url)
        classifyList.addAll(ParseHtmlUtil.parseClassifyEm(document, url))
        return classifyList
    }
}