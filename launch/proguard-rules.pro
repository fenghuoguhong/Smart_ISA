# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# saferoom 相关keep
#-keep class net.sqlcipher.** {*; }
#-keep class net.sqlcipher.database. { *; }
-dontoptimize
-ignorewarnings
-verbose
-repackageclasses
-keep class androidx.** {*;}
-keep class com.huawei.map.** {*;}
-keep class com.google.gson.** {*;}
-keep class com.huawei.maps.app.search.viewmodel.** {*;}
-keep class com.huawei.maps.businessbase.servicepermission.**{*;}
-keep class com.huawei.hms.navi.** {*;}
-keep class com.huawei.navi.** {*;}
-keep class com.huawei.android.** {*;}
-keep class com.commonsware.cwac.saferoom.** {*;}
-keep class com.huawei.maps.businessbase.database.encrypt.** {*;}
-keep class androidx.room.** {*;}
-keep class net.sqlcipher.** {*;}
-keep class com.huawei.updatesdk.**{ *; }
-keep class com.huawei.hms.framework.network.grs.GrsApi{*;}
-keep class com.huawei.hms.location.** { *; }
-keep class com.huawei.hms.common.** { *; }
-keep class com.huawei.hms.core.** { *; }
-keep class com.huawei.hms.adapter.** { *; }
-keep class com.huawei.hms.support.** { *; }
-keep class com.huawei.hms.api.** { *; }
-keep class com.huawei.hmf.tasks.** { *; }
#-keep class com.huawei.secure.android.common.** { *; }
-keep class com.huawei.maps.businessbase.network.** { *; }
-keep class huawei.android.widget.** { *; }
-keep class com.huawei.agconnect.**{*;}
-dontwarn com.huawei.agconnect.**
-keep class com.hianalytics.android.**{*;}
-keep class com.huawei.updatesdk.**{*;}
-keep class com.huawei.hms.**{*;}
-keep class com.huawei.android.hicloud.** {*;}

-keep class com.huawei.maps.businessbase.model.navirecords.NaviRecords {*;}
#-keepattributes Exceptions, Signature, InnerClasses, LineNumberTable
-repackageclasses
-keep class com.huawei.libcore.**{*;}
-keep class com.huawei.uikit.**{*;}

-keep class * extends com.huawei.maps.businessbase.model.dto.SiteBaseRequest
-keep class * extends com.huawei.maps.businessbase.network.ResponseData
-keep class com.huawei.maps.ugc.data.models.**{ *; }
-keep class com.huawei.maps.app.api.banner.** { *; }
-keep class com.huawei.maps.app.api.hotmore.** { *; }
-keep class com.huawei.maps.app.api.splash.** { *; }
-keep class com.huawei.maps.app.api.ridehailing.** { *; }
-keep class com.huawei.maps.app.api.contributionpoints.** { *; }

-keep class com.huawei.maps.aspect.** { *; }

-keepclasseswithmembers class * implements android.os.Parcelable {
 public <fields>;
 private <fields>;
 public static final android.os.Parcelable$Creator *;
}

#APMS
-keep class com.huawei.agconnect.apms.**{*;}
-dontwarn com.huawei.agconnect.apms.**
-keep class com.huawei.hianalytics.**{*;}
-keepattributes Exceptions, Signature, InnerClasses
-keepattributes *Annotation*

-keep class com.huawei.maps.businessbase.model.PetrolInfo{*;}
-keep class com.huawei.maps.businessbase.model.gasstation.GasStation{*;}
-keep class com.huawei.maps.businessbase.model.gasstation.GasSale{*;}
-keep class com.huawei.maps.businessbase.model.records.**{*;}
-keep class com.huawei.maps.businessbase.model.hotel.HotelDynInfo{*;}


# aop注解
-dontwarn org.aspectj.**
-keep class *.aspect.**{*;}
-keep public class org.aspectj.**{*;}


#动态卡片的ViewHolder 不能被混淆
-keep class com.huawei.maps.dynamic.card.viewholder.**{*;}
-keep class com.huawei.wisesecurity.ucs.**{*;}

# location full sdk
-ignorewarnings
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes Signature
-keep class com.huawei.hianalytics.**{*;}
-keep class com.huawei.updatesdk.**{*;}
-keep class com.huawei.hms.**{*;}
-keep class * extends com.huawei.hms.core.aidl.IMessageEntity{ *; }
-keep public class com.huawei.location.nlp.network.** {*; }
-keep class com.huawei.wisesecurity.ucs.**{*;}

# 不混淆实体类目录
-keep class com.huawei.maps.**.bean.** { *; }
-keep class com.huawei.maps.**.model.** { *; }
-keep class com.huawei.maps.app.api.wearable.dto.** {*;}
-keep class com.huawei.maps.app.api.wearable.model.** {*;}
-keep class com.huawei.maps.businessbase.model.discount.**{*;}
-keep class com.huawei.maps.businessbase.model.chargestation.**{*;}

-keep class com.huawei.maps.bean.apikey.ApiKeyResponse$* {*;}

# tips类
-keep class com.huawei.maps.app.petalmaps.tips.** {*;}
-keep class com.huawei.maps.businessbase.model.tips.TipsData {*;}
-keep class com.huawei.maps.businessbase.database.tips.** {*;}

#智能穿戴
-keepattributes EnclosingMethod
-keep class com.huawei.wearengine.**{*;}

#App Linking SDK 混淆
-keep class com.huawei.agconnect.**{*;}

#Location kit所需混淆
# Gson需要的规则 start
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
# Gson需要的规则 end
-keep class com.huawei.wisesecurity.ucs.**{*;}
-keep class com.huawei.location.activity.DownLoadFileBean$*{*; }
-keep class com.huawei.location.lite.common.**{*; }

-keep public class com.huawei.hms.location.** {*;}
-keep public class com.huawei.hms.support.api.entity.location.** {*;}
-keep public class com.huawei.hms.support.api.location.common.** {*; }
-keep public class com.huawei.hms.support.api.entity.location.updates.* {*;}

-keep class com.huawei.hms.api.** { *; }
-keep class com.huawei.hms.common.** { *; }
-keep class com.huawei.hms.core.** { *; }
-keep class com.huawei.hms.support.** { *; }

-keep public class com.huawei.location.base.activity.**{*;}
-keep public class com.huawei.location.base.activity.callback.*
-keep public class com.huawei.location.base.activity.constant.**{*; }
-keep public class com.huawei.location.base.activity.entity.*

-keep public class com.huawei.location.router.** { *; }

-keep class * extends com.huawei.hms.core.aidl.IMessageEntity{ *; }
-keep public class com.huawei.location.nlp.network.** {*; }

-keep class com.huawei.riemann.location.bean.** { *; }
-keep class com.huawei.riemann.location.bean.eph.** { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}

# 离线搜索SDK
-keep class com.mapswithme.util.** { *; }
-keep class com.mapswithme.maps.** { *; }
-keep class com.mapswithme.maps.search.** { *; }
-keep class com.bean.** { *; }

#MapArSDK
-keepattributes *Annotation*,InnerClasses,Signature,EnclosingMethod

-keep class com.huawei.armap.mapapi.**{
  public *;
}

-keepclassmembers enum *{
 public static **[] values();
 public static ** valueof(java.lang.String);
}
-keep class com.huawei.armap.arnavi.pojo.gnss.** { *; }
-keep class com.huawei.armap.arnavi.pojo.route.** { *; }

-keepclasseswithmembernames class *{
  native <methods>;
}
-keep class com.huawei.armap.MapController$*Listener{
 public *;
}
#-keep class com.huawei.armap.mapcore.**{*;}

-dontwarn androidx.**

-keep class androidx.** { *; }

-keep interface androidx.** { *; }

-keep public class * extends androidx.**

-keep public class * extends android.app.Fragment

-dontwarn com.huawei.armap.mapapi.SupportMapFragment

-dontwarn android.support.annotation.NonNull

-keep class com.almeros.android.multitouch.**{*;}

-dontwarn com.almeros.android.multitouch.**

-dontwarn com.huawei.armap.BuildConfig

-dontwarn com.huawei.armap.mapapi.HWMapOptions
-dontwarn com.huawei.armap.mapapi.model.CameraPosition
-dontwarn com.huawei.armap.mapapi.model.LatLngBounds
-dontwarn com.huawei.armap.utils.AttributesUtil
-dontwarn com.huawei.armap.R
-dontwarn  com.huawei.armap.R$styleable

# 组队
-keep class com.huawei.maps.team.request.**{*;}

#微信相关
-keep class com.tencent.mm.opensdk.** {
    *;
}

-keep class com.tencent.wxop.** {
    *;
}

-keep class com.tencent.mm.sdk.** {
    *;
}

-keep class kotlinx.coroutines.android.** {*;}

-keep class com.huawei.maps.businessbase.model.systemdata.** {*;}

# 鸿蒙座舱接口代码混淆
-keep class com.huawei.hosauto.vehiclecontrol.**{*;}

# hwsdk
-keep class com.huawei.android.app.**{*;}
-keep class com.huawei.system.**{*;}
-keep class com.huawei.android.os.**{*;}
-keep class com.huawei.android.view.**{*;}
-keep class com.huawei.maps.aidl.**{*;}

-keep class com.huawei.maps.businessbase.utils.**{*;}

# 解决方案SDK
-keep class com.hp.hpl.sparta.**{*;}
-dontwarn com.hp.hpl.sparta.**


-keep class net.sourceforge.pinyin4j.**{*;}
-dontwarn net.sourceforge.pinyin4j.**


-keep class com.huawei.hmsforcar.**{*;}
-dontwarn com.huawei.hmsforcar.**

-keep class com.huawei.hmsforcar.**{*;}
-dontwarn com.huawei.hmsforcar.**

-keep class com.huawei.carkit.**{*;}
-dontwarn com.huawei.carkit.**

-keep class android.app.**{*;}
-dontwarn android.app.**


-keep class android.hardware.**{*;}
-dontwarn android.hardware.**

# 解决方案SDK 使用定位接口
-keep class android.location.**{*;}
-dontwarn android.location.**

-keep class com.alibaba.**{*;}
-dontwarn com.alibaba.**



-keepattributes Exceptions, Signature, InnerClasses
-keepattributes *Annotation*

-dontwarn com.android.car.internal.**
-keep class com.android.car.internal.**{*;}

-dontwarn android.car.**
-keep class android.car.**{*;}

-dontwarn com.ecarx.sdk**
-keep class com.ecarx.sdk.**{*;}

-dontwarn ecarx.naviservice.**
-keep class ecarx.naviservice.**{*;}

-keep class com.huawei.voice.car.tts.** {*;}
-keep class com.huawei.maps.app.common.network.** {*;}

-keep class net.zetetic.database.** {*;}

-keep class com.huawei.maps.commonui.view.** { *; }

# webView处理，项目中没有使用到webView忽略即可
-keepclassmembers class android.webkit.WebView {
    public *;
}
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String);
}
-keep class android.net.** { *; }