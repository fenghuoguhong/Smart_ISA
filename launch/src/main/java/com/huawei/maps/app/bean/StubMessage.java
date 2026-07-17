package com.huawei.maps.app.bean;

import com.google.gson.annotations.SerializedName;

public class StubMessage {
    @SerializedName("ComplexIntersection")
    private int complexIntersection;

    @SerializedName("FormOfWay")
    private int formOfWay;

    @SerializedName("FunctionalRoadClass")
    private int functionalRoadClass;

    @SerializedName("LastStubAtOffset")
    private boolean lastStubAtOffset;

    @SerializedName("NumberOfLane")
    private int numberOfLane;

    @SerializedName("NumberOfLaneOpposite")
    private int numberOfLaneOpposite;

    @SerializedName("PartOfCalculatedRoute")
    private int partOfCalculatedRoute;

    @SerializedName("RelativeProbability")
    private int relativeProbability;

    @SerializedName("Retransmission")
    private boolean retransmission;

    @SerializedName("RightOfWay")
    private int rightOfWay;

    @SerializedName("SubPathIndex")
    private int subPathIndex;

    @SerializedName("TurnAngle")
    private int turnAngle;

    @SerializedName("Update")
    private boolean update;

    @SerializedName("CyclicCounter")
    private int cyclicCounter;

    @SerializedName("MessageType")
    private int messageType;

    @SerializedName("Offset")
    private int offset;

    @SerializedName("PathIndex")
    private int pathIndex;

    public int getComplexIntersection() {
        return complexIntersection;
    }

    public void setComplexIntersection(int complexIntersection) {
        this.complexIntersection = complexIntersection;
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

    public boolean isLastStubAtOffset() {
        return lastStubAtOffset;
    }

    public void setLastStubAtOffset(boolean lastStubAtOffset) {
        this.lastStubAtOffset = lastStubAtOffset;
    }

    public int getNumberOfLane() {
        return numberOfLane;
    }

    public void setNumberOfLane(int numberOfLane) {
        this.numberOfLane = numberOfLane;
    }

    public int getNumberOfLaneOpposite() {
        return numberOfLaneOpposite;
    }

    public void setNumberOfLaneOpposite(int numberOfLaneOpposite) {
        this.numberOfLaneOpposite = numberOfLaneOpposite;
    }

    public int getPartOfCalculatedRoute() {
        return partOfCalculatedRoute;
    }

    public void setPartOfCalculatedRoute(int partOfCalculatedRoute) {
        this.partOfCalculatedRoute = partOfCalculatedRoute;
    }

    public int getRelativeProbability() {
        return relativeProbability;
    }

    public void setRelativeProbability(int relativeProbability) {
        this.relativeProbability = relativeProbability;
    }

    public boolean isRetransmission() {
        return retransmission;
    }

    public void setRetransmission(boolean retransmission) {
        this.retransmission = retransmission;
    }

    public int getRightOfWay() {
        return rightOfWay;
    }

    public void setRightOfWay(int rightOfWay) {
        this.rightOfWay = rightOfWay;
    }

    public int getSubPathIndex() {
        return subPathIndex;
    }

    public void setSubPathIndex(int subPathIndex) {
        this.subPathIndex = subPathIndex;
    }

    public int getTurnAngle() {
        return turnAngle;
    }

    public void setTurnAngle(int turnAngle) {
        this.turnAngle = turnAngle;
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

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public int getPathIndex() {
        return pathIndex;
    }

    public void setPathIndex(int pathIndex) {
        this.pathIndex = pathIndex;
    }

    @Override
    public String toString() {
        return "StubMessage{" + "complexIntersection=" + complexIntersection + ", formOfWay=" + formOfWay
                + ", functionalRoadClass=" + functionalRoadClass + ", lastStubAtOffset=" + lastStubAtOffset
                + ", numberOfLane=" + numberOfLane + ", numberOfLaneOpposite=" + numberOfLaneOpposite
                + ", partOfCalculatedRoute=" + partOfCalculatedRoute + ", relativeProbability=" + relativeProbability
                + ", retransmission=" + retransmission + ", rightOfWay=" + rightOfWay + ", subPathIndex=" + subPathIndex
                + ", turnAngle=" + turnAngle + ", update=" + update + ", cyclicCounter=" + cyclicCounter + ", messageType="
                + messageType + ", offset=" + offset + ", pathIndex=" + pathIndex + '}';
    }
}
