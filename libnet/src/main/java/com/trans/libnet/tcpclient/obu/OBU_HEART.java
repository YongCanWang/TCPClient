package com.trans.libnet.tcpclient.obu;

import com.google.gson.annotations.SerializedName;

/**
 * @author Tom灿
 * @description: 心跳消息
 * @date :2023/6/2 17:18
 */
public class OBU_HEART {


    @SerializedName("HEART")
    private HEARTBean hEART;

    public HEARTBean getHEART() {
        return hEART;
    }

    public void setHEART(HEARTBean hEART) {
        this.hEART = hEART;
    }

    public static class HEARTBean {
        private String id;
        private double devType;
        private double count;
        private double status;
        private String softVer;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public double getDevType() {
            return devType;
        }

        public void setDevType(double devType) {
            this.devType = devType;
        }

        public double getCount() {
            return count;
        }

        public void setCount(double count) {
            this.count = count;
        }

        public double getStatus() {
            return status;
        }

        public void setStatus(double status) {
            this.status = status;
        }

        public String getSoftVer() {
            return softVer;
        }

        public void setSoftVer(String softVer) {
            this.softVer = softVer;
        }
    }
}
