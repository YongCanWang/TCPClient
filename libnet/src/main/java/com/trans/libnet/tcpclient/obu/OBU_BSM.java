package com.trans.libnet.tcpclient.obu;

import com.google.gson.annotations.SerializedName;

/**
 * @author Tom灿
 * @description:
 * @date :2023/6/5 9:03
 */
public class OBU_BSM {

    @SerializedName("BSM")
    private BSMBean bSM;

    public BSMBean getBSM() {
        return bSM;
    }

    public void setBSM(BSMBean bSM) {
        this.bSM = bSM;
    }

    public static class BSMBean {
        private AccelSetBean accelSet;
        private BrakesBean brakes;
        private double heading;
        private double hostFlag;
        private String id;
        private double msg_id;
        private double pcert_event;
        private PosBean pos;
        private PosConfidenceBean posConfidence;
        private SizeBean size;
        private double speed;
        private double transmission;
        private double utcTime;
        private VehicleClassBean vehicleClass;

        public AccelSetBean getAccelSet() {
            return accelSet;
        }

        public void setAccelSet(AccelSetBean accelSet) {
            this.accelSet = accelSet;
        }

        public BrakesBean getBrakes() {
            return brakes;
        }

        public void setBrakes(BrakesBean brakes) {
            this.brakes = brakes;
        }

        public double getHeading() {
            return heading;
        }

        public void setHeading(double heading) {
            this.heading = heading;
        }

        public double getHostFlag() {
            return hostFlag;
        }

        public void setHostFlag(double hostFlag) {
            this.hostFlag = hostFlag;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public double getMsg_id() {
            return msg_id;
        }

        public void setMsg_id(double msg_id) {
            this.msg_id = msg_id;
        }

        public double getPcert_event() {
            return pcert_event;
        }

        public void setPcert_event(double pcert_event) {
            this.pcert_event = pcert_event;
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

        public SizeBean getSize() {
            return size;
        }

        public void setSize(SizeBean size) {
            this.size = size;
        }

        public double getSpeed() {
            return speed;
        }

        public void setSpeed(double speed) {
            this.speed = speed;
        }

        public double getTransmission() {
            return transmission;
        }

        public void setTransmission(double transmission) {
            this.transmission = transmission;
        }

        public double getUtcTime() {
            return utcTime;
        }

        public void setUtcTime(double utcTime) {
            this.utcTime = utcTime;
        }

        public VehicleClassBean getVehicleClass() {
            return vehicleClass;
        }

        public void setVehicleClass(VehicleClassBean vehicleClass) {
            this.vehicleClass = vehicleClass;
        }

        public static class AccelSetBean {
        }

        public static class BrakesBean {
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

        public static class SizeBean {
            private double length;
            private double width;

            public double getLength() {
                return length;
            }

            public void setLength(double length) {
                this.length = length;
            }

            public double getWidth() {
                return width;
            }

            public void setWidth(double width) {
                this.width = width;
            }
        }

        public static class VehicleClassBean {
            private double classification;

            public double getClassification() {
                return classification;
            }

            public void setClassification(double classification) {
                this.classification = classification;
            }
        }
    }
}
