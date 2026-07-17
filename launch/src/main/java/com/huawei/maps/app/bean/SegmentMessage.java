package com.huawei.maps.app.bean;

import com.google.gson.annotations.SerializedName;

public class SegmentMessage {
    @SerializedName("BuiltupArea")
    private int builtupArea;

    @SerializedName("Bridge")
    private int bridge;

    @SerializedName("ComplexIntersection")
    private int complexIntersection;

    @SerializedName("EffectiveSpeedLimit")
    private int effectiveSpeedLimit;

    @SerializedName("DividedRoad")
    private int dividedRoad;

    @SerializedName("EffectiveSpeedLimitType")
    private int effectiveSpeedLimitType;

    @SerializedName("FunctionalRoadClass")
    private int functionalRoadClass;

    @SerializedName("FormOfWay")
    private int formOfWay;

    @SerializedName("NumberOfLane")
    private int numberOfLane;

    @SerializedName("PartOfCalculatedRoute")
    private int partOfCalculatedRoute;

    @SerializedName("NumberOfLaneOpposite")
    private int numberOfLaneOpposite;

    @SerializedName("RelativeProbability")
    private int relativeProbability;

    @SerializedName("Tunnel")
    private int tunnel;

    @SerializedName("Retransmission")
    private boolean retransmission;

    @SerializedName("Update")
    private boolean update;

    @SerializedName("MessageType")
    private int messageType;

    @SerializedName("CyclicCounter")
    private int cyclicCounter;

    @SerializedName("Offset")
    private int offset;

    @SerializedName("PathIndex")
    private int pathIndex;

    public int getBridge() {
        return bridge;
    }

    public void setBridge(int bridge) {
        this.bridge = bridge;
    }

    public int getBuiltupArea() {
        return builtupArea;
    }

    public void setBuiltupArea(int builtupArea) {
        this.builtupArea = builtupArea;
    }

    public int getComplexIntersection() {
        return complexIntersection;
    }

    public void setComplexIntersection(int complexIntersection) {
        this.complexIntersection = complexIntersection;
    }

    public int getDividedRoad() {
        return dividedRoad;
    }

    public void setDividedRoad(int dividedRoad) {
        this.dividedRoad = dividedRoad;
    }

    public int getEffectiveSpeedLimit() {
        return effectiveSpeedLimit;
    }

    public void setEffectiveSpeedLimit(int effectiveSpeedLimit) {
        this.effectiveSpeedLimit = effectiveSpeedLimit;
    }

    public int getEffectiveSpeedLimitType() {
        return effectiveSpeedLimitType;
    }

    public void setEffectiveSpeedLimitType(int effectiveSpeedLimitType) {
        this.effectiveSpeedLimitType = effectiveSpeedLimitType;
    }

    public int getFormOfWay() {
        return formOfWay;
    }

    public void setFormOfWay(int formOfWay) {
        this.formOfWay = formOfWay;
    }

    public int getFunctionalRoadClass() {
        return functionalRoadClass;
    }

    public void setFunctionalRoadClass(int functionalRoadClass) {
        this.functionalRoadClass = functionalRoadClass;
    }

    public int getNumberOfLaneOpposite() {
        return numberOfLaneOpposite;
    }

    public void setNumberOfLaneOpposite(int numberOfLaneOpposite) {
        this.numberOfLaneOpposite = numberOfLaneOpposite;
    }

    public int getNumberOfLane() {
        return numberOfLane;
    }

    public void setNumberOfLane(int numberOfLane) {
        this.numberOfLane = numberOfLane;
    }

    public int getRelativeProbability() {
        return relativeProbability;
    }

    public void setRelativeProbability(int relativeProbability) {
        this.relativeProbability = relativeProbability;
    }

    public int getPartOfCalculatedRoute() {
        return partOfCalculatedRoute;
    }

    public void setPartOfCalculatedRoute(int partOfCalculatedRoute) {
        this.partOfCalculatedRoute = partOfCalculatedRoute;
    }

    public int getTunnel() {
        return tunnel;
    }

    public void setTunnel(int tunnel) {
        this.tunnel = tunnel;
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
        return "SegmentMessage{" + "bridge=" + bridge + ", builtupArea=" + builtupArea + ", complexIntersection="
                + complexIntersection + ", dividedRoad=" + dividedRoad + ", effectiveSpeedLimit=" + effectiveSpeedLimit
                + ", effectiveSpeedLimitType=" + effectiveSpeedLimitType + ", formOfWay=" + formOfWay
                + ", functionalRoadClass=" + functionalRoadClass + ", numberOfLane=" + numberOfLane
                + ", numberOfLaneOpposite=" + numberOfLaneOpposite + ", partOfCalculatedRoute=" + partOfCalculatedRoute
                + ", relativeProbability=" + relativeProbability + ", retransmission=" + retransmission + ", tunnel="
                + tunnel + ", update=" + update + ", cyclicCounter=" + cyclicCounter + ", messageType=" + messageType
                + ", offset=" + offset + ", pathIndex=" + pathIndex + '}';
    }
}
