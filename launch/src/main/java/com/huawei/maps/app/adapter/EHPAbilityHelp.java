package com.huawei.maps.app.adapter;

import android.content.Context;

import com.google.gson.Gson;
import com.huawei.maps.app.bean.MetaDataMessage;
import com.huawei.maps.app.bean.PositionMessage;
import com.huawei.maps.app.bean.ProfileLongMessage;
import com.huawei.maps.app.bean.ProfileShortMessage;
import com.huawei.maps.app.bean.SegmentMessage;
import com.huawei.maps.app.bean.StatusMessage;
import com.huawei.maps.app.bean.StubMessage;
import com.huawei.maps.app.contants.MessageTypeContants;
import com.huawei.maps.app.utils.LogUtils;
import com.smart.sdk.ehp.v2.HznDataMessage;
import com.smart.sdk.ehp.v2.HznEdgeMessage;
import com.smart.sdk.ehp.v2.HznPosnMessage;
import com.smart.sdk.ehp.v2.HznProfLongMessage;
import com.smart.sdk.ehp.v2.HznProfShoMessage;
import com.smart.sdk.ehp.v2.HznSegMessage;
import com.smart.sdk.ehp.v2.HznSplyElectcStatus;
import com.smart.sdk.ehp.v2.profile.ProfLongValue;
import com.smart.sdk.ehp.v2.profile.ProfShortValue;

import java.util.Map;
import java.util.Set;

/**
 * EHP消息转换
 *
 * @since 2024/4/23
 */
public class EHPAbilityHelp {
    private static final String TAG = "EHPAbilityHelp";

    private static final int NUM_TRUE = 1;

    private static final int NUM_FALSE = 0;

    private Gson mGson;

    private EHPAbility mEhpAbility;

    private HznSplyElectcStatus mHznSplyElectcStatus;

    private HznDataMessage mHznDataMessage;

    private HznPosnMessage mHznPosnMessage;

    private HznSegMessage mHznSegMessage;

    private HznProfLongMessage mHznProfLongMessage;

    private ProfLongValue mProfLongValue;

    private HznProfShoMessage mHznProfShoMessage;

    private ProfShortValue mProfShortValue;

    private HznEdgeMessage mHznEdgeMessage;

    public EHPAbilityHelp() {
        LogUtils.getInstance().i(TAG, "EHPAbilityHelp");
    }

    public void initContext(Context context) {
        LogUtils.getInstance().i(TAG, "init");
        mEhpAbility = new EHPAbility(context);
        mGson = new Gson();
    }

    public void onCalculateEhpSuccess() {
        LogUtils.getInstance().i(TAG, "onCalculateEhpSuccess");
    }

    public void onCalculateEhpFailure(int var1) {
        LogUtils.getInstance().i(TAG, "onCalculateEhpFailure");
    }

    public void onEhpInfoUpdate(Map<String, String> ehpInfoMap) {
        Set<String> keySet = ehpInfoMap.keySet();
        for (String key : keySet) {
            LogUtils.getInstance().i(TAG, "doEhpInfoUpdate  key:" + key + "  info:" + ehpInfoMap.get(key).replaceAll("\n", ""));
            switch (key) {
                case MessageTypeContants.POSITION:
                    updatePosition(mGson.fromJson(ehpInfoMap.get(key), PositionMessage.class));
                    break;
                case MessageTypeContants.META_DATA:
                    updateMetaData(mGson.fromJson(ehpInfoMap.get(key), MetaDataMessage.class));
                    break;
                case MessageTypeContants.SEGMENT:
                    updateSegment(mGson.fromJson(ehpInfoMap.get(key), SegmentMessage.class));
                    break;
                case MessageTypeContants.STUB:
                    updateStub(mGson.fromJson(ehpInfoMap.get(key), StubMessage.class));
                    break;
                case MessageTypeContants.PROFILE_LONG:
                    updateProfileLong(mGson.fromJson(ehpInfoMap.get(key), ProfileLongMessage.class));
                    break;
                case MessageTypeContants.PROFILE_SHORT:
                    updateProfileShort(mGson.fromJson(ehpInfoMap.get(key), ProfileShortMessage.class));
                    break;
                case MessageTypeContants.STATUS:
                    updateStatus(mGson.fromJson(ehpInfoMap.get(key), StatusMessage.class));
                    break;
                default:
                    LogUtils.getInstance().i(TAG, "doEhpInfoUpdate unknow key");
                    break;
            }
        }
    }

    private void updateStatus(StatusMessage statusMessage) {
        LogUtils.getInstance().i(TAG, "onUpdateStatus");
        LogUtils.getInstance().d(TAG, "onUpdateStatus:" + statusMessage.toString());
        if (mHznSplyElectcStatus == null) {
            mHznSplyElectcStatus = new HznSplyElectcStatus();
        }
        mHznSplyElectcStatus.status = statusMessage.getEHPStatus();
        mEhpAbility.updadteHznMessage(mHznSplyElectcStatus);
    }

    private void updatePosition(PositionMessage positionMessage) {
        LogUtils.getInstance().i(TAG, "onUpdatePosition");
        LogUtils.getInstance().d(TAG, "onUpdatePosition:" + positionMessage.toString());
        if (mHznPosnMessage == null) {
            mHznPosnMessage = new HznPosnMessage();
        }
        mHznPosnMessage.hznPosnCurrentLane = positionMessage.getCurrentLane();
        mHznPosnMessage.hznPosnPositionAge = positionMessage.getPositionAge();
        mHznPosnMessage.hznPosnPositionConfidence = positionMessage.getPositionConfidence();
        mHznPosnMessage.hznPosnPositionIndex = positionMessage.getPositionIndex();
        mHznPosnMessage.hznPosnPositionProbability = positionMessage.getPositionProbability();
        mHznPosnMessage.hznPosnRelativeHeading = positionMessage.getRelativeHeading();
        mHznPosnMessage.hznPosnSpeed = positionMessage.getSpeed();
        mHznPosnMessage.hznPosnCyclicCounter = positionMessage.getCyclicCounter();
        mHznPosnMessage.hznPosnMessageType = positionMessage.getMessageType();
        mHznPosnMessage.hznPosnOffset = positionMessage.getOffset();
        mHznPosnMessage.hznPosnPathIndex = positionMessage.getPathIndex();
        mEhpAbility.updadteHznMessage(mHznPosnMessage);
    }

    private void updateMetaData(MetaDataMessage metaDataMessage) {
        LogUtils.getInstance().i(TAG, "onUpdateMetaData");
        LogUtils.getInstance().d(TAG, "onUpdateMetaData:" + metaDataMessage.toString());
        if (mHznDataMessage == null) {
            mHznDataMessage = new HznDataMessage();
        }
        mHznDataMessage.hznDataCountryCode = metaDataMessage.getCountryCode();
        mHznDataMessage.hznDataCyclicCounter = metaDataMessage.getCyclicCounter();
        mHznDataMessage.hznDataDrivingSide = metaDataMessage.getDrivingSide();
        mHznDataMessage.hznDataMessageType = metaDataMessage.getMessageType();
        mHznDataMessage.hznDataHardwareVersion = metaDataMessage.getHardwareVersion();
        mHznDataMessage.hznDataMajorProtocolVersion = metaDataMessage.getMajorProtocolVersion();
        mHznDataMessage.hznDataMapProvider = metaDataMessage.getMapProvider();
        mHznDataMessage.hznDataMapVersionYearQuarter = metaDataMessage.getMapVersionQuarter();
        mHznDataMessage.hznDataMapVersionYear = metaDataMessage.getMapVersionYear();
        mHznDataMessage.hznDataMinorProtocolSubVersion = metaDataMessage.getMinorProtocolSubVersion();
        mHznDataMessage.hznDataMinorProtocolVersion = metaDataMessage.getMinorProtocolVersion();
        mHznDataMessage.hznDataRegionCode = metaDataMessage.getRegionCode();
        mHznDataMessage.hznDataSpeedUnits = metaDataMessage.getSpeedUnit();
        mEhpAbility.updadteHznMessage(mHznDataMessage);
    }

    private void updateSegment(SegmentMessage segmentMessage) {
        LogUtils.getInstance().i(TAG, "onUpdateSegment");
        LogUtils.getInstance().d(TAG, "onUpdateSegment:" + segmentMessage.toString());
        if (mHznSegMessage == null) {
            mHznSegMessage = new HznSegMessage();
        }
        mHznSegMessage.hznSegBridge = segmentMessage.getBridge();
        mHznSegMessage.hznSegBuiltupArea = segmentMessage.getBuiltupArea();
        mHznSegMessage.hznSegComplexIntersection = segmentMessage.getComplexIntersection();
        mHznSegMessage.hznSegDividedRoad = segmentMessage.getDividedRoad();
        mHznSegMessage.hznSegEffectiveSpdLimit = segmentMessage.getEffectiveSpeedLimit();
        mHznSegMessage.hznSegEffectiveSpeedLimitType = segmentMessage.getEffectiveSpeedLimitType();
        mHznSegMessage.hznSegFormofWay = segmentMessage.getFormOfWay();
        mHznSegMessage.hznSegFunctionalRoadClass = segmentMessage.getFunctionalRoadClass();
        mHznSegMessage.hznSegNumberoflanesinoppositedirection = segmentMessage.getNumberOfLaneOpposite();
        mHznSegMessage.hznSegNumberoflanesindrivingdirection = segmentMessage.getNumberOfLane();
        mHznSegMessage.hznSegPartOfCalculatedRoute = segmentMessage.getPartOfCalculatedRoute();
        mHznSegMessage.hznSegRelativeProbability = segmentMessage.getRelativeProbability();
        mHznSegMessage.hznSegRetransmission = segmentMessage.isRetransmission() ? NUM_TRUE : NUM_FALSE;
        mHznSegMessage.hznSegTunnel = segmentMessage.getTunnel();
        mHznSegMessage.hznSegUpdate = segmentMessage.isUpdate() ? NUM_TRUE : NUM_FALSE;
        mHznSegMessage.hznSegCyclicCounter = segmentMessage.getCyclicCounter();
        mHznSegMessage.hznSegMessageType = segmentMessage.getMessageType();
        mHznSegMessage.hznSegOffset = segmentMessage.getOffset();
        mHznSegMessage.hznSegPathIndex = segmentMessage.getPathIndex();
        mEhpAbility.updadteHznMessage(mHznSegMessage);
    }

    private void updateProfileLong(ProfileLongMessage profileLongMessage) {
        LogUtils.getInstance().i(TAG, "onUpdateProfileLong");
        LogUtils.getInstance().d(TAG, "onUpdateProfileLong:" + profileLongMessage.toString());
        if (mHznProfLongMessage == null) {
            mHznProfLongMessage = new HznProfLongMessage();
        }
        if (mProfLongValue == null) {
            mProfLongValue = new ProfLongValue();
            mHznProfLongMessage.profLongValue = mProfLongValue;
        }
        mProfLongValue.hznProfLongProfileType = profileLongMessage.getProfileType();
        mProfLongValue.hznProfLongValue = profileLongMessage.getValue();
        mHznProfLongMessage.hznProfLongControlPoint = profileLongMessage.isControlPoint() ? NUM_TRUE : NUM_FALSE;
        mHznProfLongMessage.hznProfLongRetransmission = profileLongMessage.isRetransmission() ? NUM_TRUE : NUM_FALSE;
        mHznProfLongMessage.hznProfLongUpdate = profileLongMessage.isUpdate() ? NUM_TRUE : NUM_FALSE;
        mHznProfLongMessage.hznProfLongCyclicCounter = profileLongMessage.getCyclicCounter();
        mHznProfLongMessage.hznProfLongMessageType = profileLongMessage.getMessageType();
        mHznProfLongMessage.hznProfLongOffset = profileLongMessage.getOffset();
        mHznProfLongMessage.hznProfLongPathIndex = profileLongMessage.getPathIndex();
        mEhpAbility.updadteHznMessage(mHznProfLongMessage);
    }

    private void updateProfileShort(ProfileShortMessage profileShortMessage) {
        LogUtils.getInstance().i(TAG, "updateProfileShort");
        LogUtils.getInstance().d(TAG, "updateProfileShort:" + profileShortMessage.toString());
        if (mHznProfShoMessage == null) {
            mHznProfShoMessage = new HznProfShoMessage();
        }
        if (mProfShortValue == null) {
            mProfShortValue = new ProfShortValue();
            mHznProfShoMessage.profShortValue0 = mProfShortValue;
        }
        mProfShortValue.hznProfShoValue0 = profileShortMessage.getValue0();
        mProfShortValue.hznProfShoProfileType = profileShortMessage.getProfileType();
        mHznProfShoMessage.hznProfShoControlPoint = profileShortMessage.isControlPoint() ? NUM_TRUE : NUM_FALSE;
        mHznProfShoMessage.hznProfShoRetransmission = profileShortMessage.isRetransmission() ? NUM_TRUE : NUM_FALSE;
        mHznProfShoMessage.hznProfShoUpdate = profileShortMessage.isUpdate() ? NUM_TRUE : NUM_FALSE;
        mHznProfShoMessage.hznProfShoCyclicCounter = profileShortMessage.getCyclicCounter();
        mHznProfShoMessage.hznProfShoMessageType = profileShortMessage.getMessageType();
        mHznProfShoMessage.hznProfShoOffset = profileShortMessage.getOffset();
        mHznProfShoMessage.hznProfShoPathIndex = profileShortMessage.getPathIndex();
        mHznProfShoMessage.hznProfShoDistance1 = profileShortMessage.getDistance1();
        mHznProfShoMessage.hznProfShoValue1 = profileShortMessage.getValue1();
        mHznProfShoMessage.hznProfShoAccuracy = profileShortMessage.getAccuracy();
        mEhpAbility.updadteHznMessage(mHznProfShoMessage);
    }

    private void updateStub(StubMessage stubMessage) {
        LogUtils.getInstance().i(TAG, "onUpdateStub");
        LogUtils.getInstance().d(TAG, "onUpdateStub:" + stubMessage.toString());
        if (mHznEdgeMessage == null) {
            mHznEdgeMessage = new HznEdgeMessage();
        }
        mHznEdgeMessage.hznEdgeComplexIntersection = stubMessage.getComplexIntersection();
        mHznEdgeMessage.hznEdgeFormOfWay = stubMessage.getFormOfWay();
        mHznEdgeMessage.hznEdgeFunctionalRoadClass = stubMessage.getFunctionalRoadClass();
        mHznEdgeMessage.hznEdgeLastStubAtOffset = stubMessage.isLastStubAtOffset() ? NUM_TRUE : NUM_FALSE;
        mHznEdgeMessage.hznEdgeNumberOfLanesInDrivingDirection = stubMessage.getNumberOfLane();
        mHznEdgeMessage.hznEdgeNumberOfLanesInOppositeDirection = stubMessage.getNumberOfLaneOpposite();
        mHznEdgeMessage.hznEdgePartOfCalculatedRoute = stubMessage.getPartOfCalculatedRoute();
        mHznEdgeMessage.hznEdgeRelativeProbability = stubMessage.getRelativeProbability();
        mHznEdgeMessage.hznEdgeRetransmission = stubMessage.isRetransmission() ? NUM_TRUE : NUM_FALSE;
        mHznEdgeMessage.hznEdgeRightofWay = stubMessage.getRightOfWay();
        mHznEdgeMessage.hznEdgeSubPathIndex = stubMessage.getSubPathIndex();
        mHznEdgeMessage.hznEdgeTurnAngle = stubMessage.getTurnAngle();
        mHznEdgeMessage.hznEdgeUpdate = stubMessage.isUpdate() ? NUM_TRUE : NUM_FALSE;
        mHznEdgeMessage.hznEdgeCyclicCounter = stubMessage.getCyclicCounter();
        mHznEdgeMessage.hznEdgeMessageType = stubMessage.getMessageType();
        mHznEdgeMessage.hznEdgeOffset = stubMessage.getOffset();
        mHznEdgeMessage.hznEdgePathIndex = stubMessage.getPathIndex();
        mEhpAbility.updadteHznMessage(mHznEdgeMessage);
    }
}
