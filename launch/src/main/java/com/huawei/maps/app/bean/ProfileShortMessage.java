package com.huawei.maps.app.bean;

import com.google.gson.annotations.SerializedName;

public class ProfileShortMessage {
    @SerializedName("Accuracy")
    private int accuracy;

    @SerializedName("ControlPoint")
    private boolean controlPoint;

    @SerializedName("Distance1")
    private int distance1;

    @SerializedName("ProfileType")
    private int profileType;

    @SerializedName("Retransmission")
    private boolean retransmission;

    @SerializedName("Update")
    private boolean update;

    @SerializedName("Value0")
    private int value0;

    @SerializedName("Value1")
    private int value1;

    @SerializedName("CyclicCounter")
    private int cyclicCounter;

    @SerializedName("MessageType")
    private int messageType;

    @SerializedName("Offset")
    private int offset;

    @SerializedName("PathIndex")
    private int pathIndex;

    public int getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }

    public boolean isControlPoint() {
        return controlPoint;
    }

    public void setControlPoint(boolean controlPoint) {
        this.controlPoint = controlPoint;
    }

    public int getDistance1() {
        return distance1;
    }

    public void setDistance1(int distance1) {
        this.distance1 = distance1;
    }

    public int getProfileType() {
        return profileType;
    }

    public void setProfileType(int profileType) {
        this.profileType = profileType;
    }

    public boolean isRetransmission() {
        return retransmission;
    }

    public void setRetransmission(boolean retransmission) {
        this.retransmission = retransmission;
    }

    public boolean isUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public int getValue0() {
        return value0;
    }

    public void setValue0(int value0) {
        this.value0 = value0;
    }

    public int getValue1() {
        return value1;
    }

    public void setValue1(int value1) {
        this.value1 = value1;
    }

    public int getCyclicCounter() {
        return cyclicCounter;
    }

    public void setCyclicCounter(int cyclicCounter) {
        this.cyclicCounter = cyclicCounter;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getPathIndex() {
        return pathIndex;
    }

    public void setPathIndex(int pathIndex) {
        this.pathIndex = pathIndex;
    }

    @Override
    public String toString() {
        return "ProfileShortMessage{" + "accuracy=" + accuracy + ", controlPoint=" + controlPoint + ", distance1="
                + distance1 + ", profileType=" + profileType + ", retransmission=" + retransmission + ", update=" + update
                + ", value0=" + value0 + ", value1=" + value1 + ", cyclicCounter=" + cyclicCounter + ", messageType="
                + messageType + ", offset=" + offset + ", pathIndex=" + pathIndex + '}';
    }
}
