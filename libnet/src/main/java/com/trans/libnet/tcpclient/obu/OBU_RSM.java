package com.trans.libnet.tcpclient.obu;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author Tom灿
 * @description:
 * @date :2023/6/5 9:04
 */
public class OBU_RSM {

    @SerializedName("RSM")
    private RSMBean rSM;

    public RSMBean getRSM() {
        return rSM;
    }

    public void setRSM(RSMBean rSM) {
        this.rSM = rSM;
    }

    public static class RSMBean {
        private String id;
        private List<ParticipantsBean> participants;
        private RefPosBean refPos;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<ParticipantsBean> getParticipants() {
            return participants;
        }

        public void setParticipants(List<ParticipantsBean> participants) {
            this.participants = participants;
        }

        public RefPosBean getRefPos() {
            return refPos;
        }

        public void setRefPos(RefPosBean refPos) {
            this.refPos = refPos;
        }

        public static class RefPosBean {
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

        public static class ParticipantsBean {
            private AccelSetBean accelSet;
            private double heading;
            private String id;
            private PosBean pos;
            private PosConfidenceBean posConfidence;
            private int ptcId;
            private int ptcType;
            private SizeBean size;
            private int source;
            private int speed;
            private int transmission;
            private double utcTime;
            private VehicleClassBean vehicleClass;

            public AccelSetBean getAccelSet() {
                return accelSet;
            }

            public void setAccelSet(AccelSetBean accelSet) {
                this.accelSet = accelSet;
            }

            public double getHeading() {
                return heading;
            }

            public void setHeading(double heading) {
                this.heading = heading;
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
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

            public int getPtcId() {
                return ptcId;
            }

            public void setPtcId(int ptcId) {
                this.ptcId = ptcId;
            }

            public int getPtcType() {
                return ptcType;
            }

            public void setPtcType(int ptcType) {
                this.ptcType = ptcType;
            }

            public SizeBean getSize() {
                return size;
            }

            public void setSize(SizeBean size) {
                this.size = size;
            }

            public int getSource() {
                return source;
            }

            public void setSource(int source) {
                this.source = source;
            }

            public int getSpeed() {
                return speed;
            }

            public void setSpeed(int speed) {
                this.speed = speed;
            }

            public int getTransmission() {
                return transmission;
            }

            public void setTransmission(int transmission) {
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
                private int lat;
                @SerializedName("long")
                private int longX;
                private int vert;
                private int yaw;

                public int getLat() {
                    return lat;
                }

                public void setLat(int lat) {
                    this.lat = lat;
                }

                public int getLongX() {
                    return longX;
                }

                public void setLongX(int longX) {
                    this.longX = longX;
                }

                public int getVert() {
                    return vert;
                }

                public void setVert(int vert) {
                    this.vert = vert;
                }

                public int getYaw() {
                    return yaw;
                }

                public void setYaw(int yaw) {
                    this.yaw = yaw;
                }
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

            public static class PosConfidenceBean {
                private int pos;

                public int getPos() {
                    return pos;
                }

                public void setPos(int pos) {
                    this.pos = pos;
                }
            }

            public static class SizeBean {
                private int length;
                private int width;

                public int getLength() {
                    return length;
                }

                public void setLength(int length) {
                    this.length = length;
                }

                public int getWidth() {
                    return width;
                }

                public void setWidth(int width) {
                    this.width = width;
                }
            }

            public static class VehicleClassBean {
                private int classification;

                public int getClassification() {
                    return classification;
                }

                public void setClassification(int classification) {
                    this.classification = classification;
                }
            }
        }
    }
}
