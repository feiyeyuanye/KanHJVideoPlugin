package com.jhr.kanhjvideoplugin.plugin

import com.jhr.kanhjvideoplugin.plugin.components.Const
import com.jhr.kanhjvideoplugin.plugin.components.HomePageDataComponent
import com.jhr.kanhjvideoplugin.plugin.components.MediaClassifyPageDataComponent
import com.jhr.kanhjvideoplugin.plugin.components.MediaDetailPageDataComponent
import com.jhr.kanhjvideoplugin.plugin.components.MediaSearchPageDataComponent
import com.jhr.kanhjvideoplugin.plugin.components.MediaUpdateDataComponent
import com.jhr.kanhjvideoplugin.plugin.components.RankPageDataComponent
import com.jhr.kanhjvideoplugin.plugin.components.VideoPlayPageDataComponent
import com.jhr.kanhjvideoplugin.plugin.danmaku.OyydsDanmaku
import com.su.mediabox.pluginapi.IPluginFactory
import com.su.mediabox.pluginapi.components.IBasePageDataComponent
import com.su.mediabox.pluginapi.components.IHomePageDataComponent
import com.su.mediabox.pluginapi.components.IMediaClassifyPageDataComponent
import com.su.mediabox.pluginapi.components.IMediaDetailPageDataComponent
import com.su.mediabox.pluginapi.components.IMediaSearchPageDataComponent
import com.su.mediabox.pluginapi.components.IMediaUpdateDataComponent
import com.su.mediabox.pluginapi.components.IVideoPlayPageDataComponent
import com.su.mediabox.pluginapi.util.PluginPreferenceIns

/**
 * 每个插件必须实现本类
 *
 * 注意包和类名都要相同，且必须提供公开的无参数构造方法
 */
class PluginFactory : IPluginFactory() {

    override val host: String = Const.host

    override fun pluginLaunch() {
        PluginPreferenceIns.initKey(OyydsDanmaku.OYYDS_DANMAKU_ENABLE, defaultValue = true)
    }

    override fun <T : IBasePageDataComponent> createComponent(clazz: Class<T>) = when (clazz) {
        IHomePageDataComponent::class.java -> HomePageDataComponent()  // 主页
        IMediaSearchPageDataComponent::class.java -> MediaSearchPageDataComponent()  // 搜索
        IMediaDetailPageDataComponent::class.java -> MediaDetailPageDataComponent()  // 详情
        IMediaClassifyPageDataComponent::class.java -> MediaClassifyPageDataComponent()  // 媒体分类
        IMediaUpdateDataComponent::class.java -> MediaUpdateDataComponent
        IVideoPlayPageDataComponent::class.java -> VideoPlayPageDataComponent() // 视频播放
        //自定义页面，需要使用具体类而不是它的基类（接口）
        RankPageDataComponent::class.java -> RankPageDataComponent()  // 榜单
        else -> null
    } as? T

}