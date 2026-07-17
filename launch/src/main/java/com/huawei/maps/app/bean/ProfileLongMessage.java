package com.huawei.maps.app.bean;

import com.google.gson.annotations.SerializedName;

public class ProfileLongMessage {
    @SerializedName("ControlPoint")
    private boolean controlPoint;

    @SerializedName("ProfileType")
    private int profileType;

    @SerializedName("Retransmission")
    private boolean retransmission;

    @SerializedName("Update")
    private boolean update;

    @SerializedName("Value")
    private int value;

    @SerializedName("CyclicCounter")
    private int cyclicCounter;

    @SerializedName("MessageType")
    private int messageType;

    @SerializedName("Offset")
    private int offset;

    @SerializedName("PathIndex")
    private int pathIndex;

    public boolean isControlPoint() {
        return controlPoint;
    }

    public void setControlPoint(boolean controlPoint) {
        this.controlPoint = controlPoint;
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

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getCyclicCounter() {
        return cyclicCounter;
    }

    public void setCyclicCounter(int cyclicCounter) {
        this.cyclicCounter = cyclicCounter;
    }

    public int getPathIndex() {
        return pathIndex;
    }

    public void setPathIndex(int pathIndex) {
        this.pathIndex = pathIndex;
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


    @Override
    public String toString() {
        return "ProfileLongMessage{" + "controlPoint=" + controlPoint + ", profileType=" + profileType
                + ", retransmission=" + retransmission + ", update=" + update + ", value=" + value + ", cyclicCounter="
                + cyclicCounter + ", messageType=" + messageType + ", offset=" + offset + ", pathIndex=" + pathIndex + '}';
    }
}
