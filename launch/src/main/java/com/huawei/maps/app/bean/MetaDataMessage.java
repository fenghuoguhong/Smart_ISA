package com.huawei.maps.app.bean;

import com.google.gson.annotations.SerializedName;

public class MetaDataMessage {
    @SerializedName("CountryCode")
    private int countryCode;

    @SerializedName("CyclicCounter")
    private int cyclicCounter;

    @SerializedName("DrivingSide")
    private int drivingSide;

    @SerializedName("HardwareVersion")
    private int hardwareVersion;

    @SerializedName("MajorProtocolVersion")
    private int majorProtocolVersion;

    @SerializedName("MapProvider")
    private int mapProvider;

    @SerializedName("MapVersionYear")
    private int mapVersionYear;

    @SerializedName("MapVersionQuarter")
    private int mapVersionQuarter;

    @SerializedName("MessageType")
    private int messageType;

    @SerializedName("MinorProtocolSubVersion")
    private int minorProtocolSubVersion;

    @SerializedName("MinorProtocolVersion")
    private int minorProtocolVersion;

    @SerializedName("RegionCode")
    private int regionCode;

    @SerializedName("SpeedUnit")
    private int speedUnit;

    public int getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(int countryCode) {
        this.countryCode = countryCode;
    }

    public int getCyclicCounter() {
        return cyclicCounter;
    }

    public void setCyclicCounter(int cyclicCounter) {
        this.cyclicCounter = cyclicCounter;
    }

    public int getDrivingSide() {
        return drivingSide;
    }

    public void setDrivingSide(int drivingSide) {
        this.drivingSide = drivingSide;
    }

    public int getHardwareVersion() {
        return hardwareVersion;
    }

    public void setHardwareVersion(int hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
    }

    public int getMajorProtocolVersion() {
        return majorProtocolVersion;
    }

    public void setMajorProtocolVersion(int majorProtocolVersion) {
        this.majorProtocolVersion = majorProtocolVersion;
    }

    public int getMapProvider() {
        return mapProvider;
    }

    public void setMapProvider(int mapProvider) {
        this.mapProvider = mapProvider;
    }

    public int getMapVersionYear() {
        return mapVersionYear;
    }

    public void setMapVersionYear(int mapVersionYear) {
        this.mapVersionYear = mapVersionYear;
    }

    public int getMapVersionQuarter() {
        return mapVersionQuarter;
    }

    public void setMapVersionQuarter(int mapVersionQuarter) {
        this.mapVersionQuarter = mapVersionQuarter;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public int getMinorProtocolSubVersion() {
        return minorProtocolSubVersion;
    }

    public void setMinorProtocolSubVersion(int minorProtocolSubVersion) {
        this.minorProtocolSubVersion = minorProtocolSubVersion;
    }

    public int getMinorProtocolVersion() {
        return minorProtocolVersion;
    }

    public void setMinorProtocolVersion(int minorProtocolVersion) {
        this.minorProtocolVersion = minorProtocolVersion;
    }

    public int getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(int regionCode) {
        this.regionCode = regionCode;
    }

    public int getSpeedUnit() {
        return speedUnit;
    }

    public void setSpeedUnit(int speedUnit) {
        this.speedUnit = speedUnit;
    }

    @Override
    public String toString() {
        return "MetaDataMessage{" + "countryCode=" + countryCode + ", cyclicCounter=" + cyclicCounter + ", drivingSide="
                + drivingSide + ", hardwareVersion=" + hardwareVersion + ", majorProtocolVersion=" + majorProtocolVersion
                + ", mapProvider=" + mapProvider + ", mapVersionYear=" + mapVersionYear + ", mapVersionQuarter="
                + mapVersionQuarter + ", messageType=" + messageType + ", minorProtocolSubVersion="
                + minorProtocolSubVersion + ", minorProtocolVersion=" + minorProtocolVersion + ", regionCode=" + regionCode
                + ", speedUnit=" + speedUnit + '}';
    }
}
