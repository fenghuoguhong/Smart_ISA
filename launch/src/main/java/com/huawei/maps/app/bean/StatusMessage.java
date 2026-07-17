package com.huawei.maps.app.bean;

import com.google.gson.annotations.SerializedName;

public class StatusMessage {
    @SerializedName("EHPStatus")
    private int eHPStatus;

    @SerializedName("Update")
    private boolean update;

    public int getEHPStatus() {
        return eHPStatus;
    }

    public void setEHPStatus(int eHPStatus) {
        this.eHPStatus = eHPStatus;
    }

    public boolean isUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    @Override
    public String toString() {
        return "StatusMessage{" + "eHPStatus=" + eHPStatus + ", update=" + update + '}';
    }
}
