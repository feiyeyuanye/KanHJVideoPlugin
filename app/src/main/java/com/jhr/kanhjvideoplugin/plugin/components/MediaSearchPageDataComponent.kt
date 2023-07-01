package com.jhr.kanhjvideoplugin.plugin.components

import android.util.Log
import com.jhr.kanhjvideoplugin.plugin.components.Const.host
import com.jhr.kanhjvideoplugin.plugin.util.JsoupUtil
import com.jhr.kanhjvideoplugin.plugin.util.ParseHtmlUtil
import com.su.mediabox.pluginapi.components.IMediaSearchPageDataComponent
import com.su.mediabox.pluginapi.data.BaseData

class MediaSearchPageDataComponent : IMediaSearchPageDataComponent {

    override suspend fun getSearchData(keyWord: String, page: Int): List<BaseData> {
        val searchResultList = mutableListOf<BaseData>()
        // https://www.kangii.com/search.html?searchword=%E9%BE%99&submit=submit
        // https://www.kangii.com/search.html?page=2&searchword=%E9%BE%99&submit=submit
        val url = "${host}/search.html?page=${page}&searchword=${keyWord}&submit=submit"
        Log.e("TAG", url)

        val document = JsoupUtil.getDocument(url)
        searchResultList.addAll(ParseHtmlUtil.parseSearchEm(document, url))
        return searchResultList
    }

}