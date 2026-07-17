package com.huawei.maps.app.bean;

import com.google.gson.annotations.SerializedName;

public class PositionMessage {
    @SerializedName("CurrentLane")
    private int currentLane;

    @SerializedName("PositionAge")
    private int positionAge;

    @SerializedName("PositionConfidence")
    private int positionConfidence;

    @SerializedName("PositionIndex")
    private int positionIndex;

    @SerializedName("PositionProbability")
    private int positionProbability;

    @SerializedName("RelativeHeading")
    private int relativeHeading;

    @SerializedName("Speed")
    private int speed;

    @SerializedName("CyclicCounter")
    private int cyclicCounter;

    @SerializedName("MessageType")
    private int messageType;

    @SerializedName("Offset")
    private int offset;

    @SerializedName("PathIndex")
    private int pathIndex;

    public int getCurrentLane() {
        return currentLane;
    }

    public void setCurrentLane(int currentLane) {
        this.currentLane = currentLane;
    }

    public int getPositionAge() {
        return positionAge;
    }

    public void setPositionAge(int positionAge) {
        this.positionAge = positionAge;
    }

    public int getPositionConfidence() {
        return positionConfidence;
    }

    public void setPositionConfidence(int positionConfidence) {
        this.positionConfidence = positionConfidence;
    }

    public int getPositionIndex() {
        return positionIndex;
    }

    public void setPositionIndex(int positionIndex) {
        this.positionIndex = positionIndex;
    }

    public int getPositionProbability() {
        return positionProbability;
    }

    public void setPositionProbability(int positionProbability) {
        this.positionProbability = positionProbability;
    }

    public int getRelativeHeading() {
        return relativeHeading;
    }

    public void setRelativeHeading(int relativeHeading) {
        this.relativeHeading = relativeHeading;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
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

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }


    @Override
    public String toString() {
        return "PositionMessage{" + "currentLane=" + currentLane + ", positionAge=" + positionAge
                + ", positionConfidence=" + positionConfidence + ", positionIndex=" + positionIndex
                + ", positionProbability=" + positionProbability + ", relativeHeading=" + relativeHeading + ", speed="
                + speed + ", cyclicCounter=" + cyclicCounter + ", messageType=" + messageType + ", offset=" + offset
                + ", pathIndex=" + pathIndex + '}';
    }
}
