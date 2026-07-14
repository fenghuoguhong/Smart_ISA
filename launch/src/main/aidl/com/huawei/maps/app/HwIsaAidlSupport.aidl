// HwIsaAidlSupport.aidl
package com.huawei.maps.app;

// Declare any non-default types here with import statements

interface HwIsaAidlSupport {
    /**
     获取Isa离线数据版本号
     */
    String getIsaDataVersion();
    /**
     获取Isa版本号
     */
    String getIsaServiceVersion();
    /**
     校验EHP数据
     */
    boolean checkEHPOfflineData();
}