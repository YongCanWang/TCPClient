package com.trans.libnet.tcpclient.obu;

import com.google.gson.annotations.SerializedName;

/**
 * @author Tom灿
 * @description: 预警消息
 * @date :2023/6/9 10:09
 */
public class OBU_TM {


    @SerializedName("TM")
    private TMBean tM;

    public TMBean getTM() {
        return tM;
    }

    public void setTM(TMBean tM) {
        this.tM = tM;
    }

    public static class TMBean {
        private String description;
        private double hostHeading;
        private String hostId;
        private String hostPlateNo;
        private HostPosBean hostPos;
        private double hostSpeed;
        private double priority;
        private double relativePos;
        private double remoteHeading;
        private String remoteId;
        private RemotePosBean remotePos;
        private double remoteSpeed;
        private double subType;
        private double ttc;
        private double type;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public double getHostHeading() {
            return hostHeading;
        }

        public void setHostHeading(double hostHeading) {
            this.hostHeading = hostHeading;
        }

        public String getHostId() {
            return hostId;
        }

        public void setHostId(String hostId) {
            this.hostId = hostId;
        }

        public String getHostPlateNo() {
            return hostPlateNo;
        }

        public void setHostPlateNo(String hostPlateNo) {
            this.hostPlateNo = hostPlateNo;
        }

        public HostPosBean getHostPos() {
            return hostPos;
        }

        public void setHostPos(HostPosBean hostPos) {
            this.hostPos = hostPos;
        }

        public double getHostSpeed() {
            return hostSpeed;
        }

        public void setHostSpeed(double hostSpeed) {
            this.hostSpeed = hostSpeed;
        }

        public double getPriority() {
            return priority;
        }

        public void setPriority(double priority) {
            this.priority = priority;
        }

        public double getRelativePos() {
            return relativePos;
        }

        public void setRelativePos(double relativePos) {
            this.relativePos = relativePos;
        }

        public double getRemoteHeading() {
            return remoteHeading;
        }

        public void setRemoteHeading(double remoteHeading) {
            this.remoteHeading = remoteHeading;
        }

        public String getRemoteId() {
            return remoteId;
        }

        public void setRemoteId(String remoteId) {
            this.remoteId = remoteId;
        }

        public RemotePosBean getRemotePos() {
            return remotePos;
        }

        public void setRemotePos(RemotePosBean remotePos) {
            this.remotePos = remotePos;
        }

        public double getRemoteSpeed() {
            return remoteSpeed;
        }

        public void setRemoteSpeed(double remoteSpeed) {
            this.remoteSpeed = remoteSpeed;
        }

        public double getSubType() {
            return subType;
        }

        public void setSubType(double subType) {
            this.subType = subType;
        }

        public double getTtc() {
            return ttc;
        }

        public void setTtc(double ttc) {
            this.ttc = ttc;
        }

        public double getType() {
            return type;
        }

        public void setType(double type) {
            this.type = type;
        }

        public static class HostPosBean {
            private double elevation;
            private double lat;
            @SerializedName("long")
            private double longX;

            public double getElevation() {
                return elevation;
            }

            public void setElevation(double elevation) {
                this.elevation = elevation;
            }

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

        public static class RemotePosBean {
            private double elevation;
            private double lat;
            @SerializedName("long")
            private double longX;

            public double getElevation() {
                return elevation;
            }

            public void setElevation(double elevation) {
                this.elevation = elevation;
            }

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
