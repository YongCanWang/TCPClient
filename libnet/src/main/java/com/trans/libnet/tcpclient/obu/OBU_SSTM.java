package com.trans.libnet.tcpclient.obu;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author Tom灿
 * @description:
 * @date :2023/6/13 14:45
 */
public class OBU_SSTM {

    @SerializedName("SSTM")
    private List<SSTMBean> sSTM;

    public List<SSTMBean> getSSTM() {
        return sSTM;
    }

    public void setSSTM(List<SSTMBean> sSTM) {
        this.sSTM = sSTM;
    }

    public static class SSTMBean {
        private double distance;
        private int light;
        private int lightAttributes;
        private int lightSeq;
        private int lightStyle;
        private double likelyEndTime;
        private int likelyEndTimeAttributes;
        private PosBean pos;
        private double speed;
        private int speedLimit;
        private int type;

        public double getDistance() {
            return distance;
        }

        public void setDistance(double distance) {
            this.distance = distance;
        }

        public int getLight() {
            return light;
        }

        public void setLight(int light) {
            this.light = light;
        }

        public int getLightAttributes() {
            return lightAttributes;
        }

        public void setLightAttributes(int lightAttributes) {
            this.lightAttributes = lightAttributes;
        }

        public int getLightSeq() {
            return lightSeq;
        }

        public void setLightSeq(int lightSeq) {
            this.lightSeq = lightSeq;
        }

        public int getLightStyle() {
            return lightStyle;
        }

        public void setLightStyle(int lightStyle) {
            this.lightStyle = lightStyle;
        }

        public double getLikelyEndTime() {
            return likelyEndTime;
        }

        public void setLikelyEndTime(double likelyEndTime) {
            this.likelyEndTime = likelyEndTime;
        }

        public int getLikelyEndTimeAttributes() {
            return likelyEndTimeAttributes;
        }

        public void setLikelyEndTimeAttributes(int likelyEndTimeAttributes) {
            this.likelyEndTimeAttributes = likelyEndTimeAttributes;
        }

        public PosBean getPos() {
            return pos;
        }

        public void setPos(PosBean pos) {
            this.pos = pos;
        }

        public double getSpeed() {
            return speed;
        }

        public void setSpeed(double speed) {
            this.speed = speed;
        }

        public int getSpeedLimit() {
            return speedLimit;
        }

        public void setSpeedLimit(int speedLimit) {
            this.speedLimit = speedLimit;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public static class PosBean {
            private double lat;
            @SerializedName("long")
            private double longX;

            public double getLat() {
                return lat;
            }

            public void setLat(double lat) {
                this.lat = lat;
            }

            public double getLongX() {
                return longX;
            }

            public void setLongX(double longX) {
                this.longX = longX;
            }
        }
    }
}
