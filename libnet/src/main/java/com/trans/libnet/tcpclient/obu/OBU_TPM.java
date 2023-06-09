package com.trans.libnet.tcpclient.obu;

import com.google.gson.annotations.SerializedName;

/**
 * @author Tom灿
 * @description:
 * @date :2023/6/5 9:07
 */
public class OBU_TPM {

    @SerializedName("TPM")
    private TPMBean tPM;

    public TPMBean getTPM() {
        return tPM;
    }

    public void setTPM(TPMBean tPM) {
        this.tPM = tPM;
    }

    public static class TPMBean {
        private double heading;
        private PosBean pos;
        private PosConfidenceBean posConfidence;
        private double speed;
        private double utcTime;

        public double getHeading() {
            return heading;
        }

        public void setHeading(double heading) {
            this.heading = heading;
        }

        public PosBean getPos() {
            return pos;
        }

        public void setPos(PosBean pos) {
            this.pos = pos;
        }

        public PosConfidenceBean getPosConfidence() {
            return posConfidence;
        }

        public void setPosConfidence(PosConfidenceBean posConfidence) {
            this.posConfidence = posConfidence;
        }

        public double getSpeed() {
            return speed;
        }

        public void setSpeed(double speed) {
            this.speed = speed;
        }

        public double getUtcTime() {
            return utcTime;
        }

        public void setUtcTime(double utcTime) {
            this.utcTime = utcTime;
        }

        public static class PosBean {
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

        public static class PosConfidenceBean {
            private double elevation;
            private double pos;

            public double getElevation() {
                return elevation;
            }

            public void setElevation(double elevation) {
                this.elevation = elevation;
            }

            public double getPos() {
                return pos;
            }

            public void setPos(double pos) {
                this.pos = pos;
            }
        }
    }
}
