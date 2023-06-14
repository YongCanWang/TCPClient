package com.trans.libnet.tcpclient.obu;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author Tom灿
 * @description: 红路灯消息
 * @date :2023/6/5 9:05
 */
public class OBU_SPAT {


    @SerializedName("SPAT")
    private SPATBean sPAT;

    public SPATBean getSPAT() {
        return sPAT;
    }

    public void setSPAT(SPATBean sPAT) {
        this.sPAT = sPAT;
    }

    public static class SPATBean {
        private int msgCnt;
        private String msgType;
        private int timestamp;
        private String uuid;
        private List<IntersectionsBean> intersections;

        public int getMsgCnt() {
            return msgCnt;
        }

        public void setMsgCnt(int msgCnt) {
            this.msgCnt = msgCnt;
        }

        public String getMsgType() {
            return msgType;
        }

        public void setMsgType(String msgType) {
            this.msgType = msgType;
        }

        public int getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(int timestamp) {
            this.timestamp = timestamp;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public List<IntersectionsBean> getIntersections() {
            return intersections;
        }

        public void setIntersections(List<IntersectionsBean> intersections) {
            this.intersections = intersections;
        }

        public static class IntersectionsBean {
            private String intersectionId;
            private int intersectionTimestamp;
            private int nodeId;
            private List<PhasesBean> phases;
            private int regionId;
            private int status;
            private int timeConfidence;

            public String getIntersectionId() {
                return intersectionId;
            }

            public void setIntersectionId(String intersectionId) {
                this.intersectionId = intersectionId;
            }

            public int getIntersectionTimestamp() {
                return intersectionTimestamp;
            }

            public void setIntersectionTimestamp(int intersectionTimestamp) {
                this.intersectionTimestamp = intersectionTimestamp;
            }

            public int getNodeId() {
                return nodeId;
            }

            public void setNodeId(int nodeId) {
                this.nodeId = nodeId;
            }

            public List<PhasesBean> getPhases() {
                return phases;
            }

            public void setPhases(List<PhasesBean> phases) {
                this.phases = phases;
            }

            public int getRegionId() {
                return regionId;
            }

            public void setRegionId(int regionId) {
                this.regionId = regionId;
            }

            public int getStatus() {
                return status;
            }

            public void setStatus(int status) {
                this.status = status;
            }

            public int getTimeConfidence() {
                return timeConfidence;
            }

            public void setTimeConfidence(int timeConfidence) {
                this.timeConfidence = timeConfidence;
            }

            public static class PhasesBean {
                private List<PhaseStatesBean> phaseStates;
                private int phasesId;

                public List<PhaseStatesBean> getPhaseStates() {
                    return phaseStates;
                }

                public void setPhaseStates(List<PhaseStatesBean> phaseStates) {
                    this.phaseStates = phaseStates;
                }

                public int getPhasesId() {
                    return phasesId;
                }

                public void setPhasesId(int phasesId) {
                    this.phasesId = phasesId;
                }

                public static class PhaseStatesBean {
                    private int light;
                    private int likelyEndTime;
                    private int likelyEndUTCTime;
                    private int maxEndUTCTime;
                    private int minEndUTCTime;
                    private int nextDuration;
                    private int nextEndUTCTime;
                    private int nextStartUTCTime;
                    private int startTime;
                    private int startUTCTime;
                    private int timeChangeDetails;

                    public int getLight() {
                        return light;
                    }

                    public void setLight(int light) {
                        this.light = light;
                    }

                    public int getLikelyEndTime() {
                        return likelyEndTime;
                    }

                    public void setLikelyEndTime(int likelyEndTime) {
                        this.likelyEndTime = likelyEndTime;
                    }

                    public int getLikelyEndUTCTime() {
                        return likelyEndUTCTime;
                    }

                    public void setLikelyEndUTCTime(int likelyEndUTCTime) {
                        this.likelyEndUTCTime = likelyEndUTCTime;
                    }

                    public int getMaxEndUTCTime() {
                        return maxEndUTCTime;
                    }

                    public void setMaxEndUTCTime(int maxEndUTCTime) {
                        this.maxEndUTCTime = maxEndUTCTime;
                    }

                    public int getMinEndUTCTime() {
                        return minEndUTCTime;
                    }

                    public void setMinEndUTCTime(int minEndUTCTime) {
                        this.minEndUTCTime = minEndUTCTime;
                    }

                    public int getNextDuration() {
                        return nextDuration;
                    }

                    public void setNextDuration(int nextDuration) {
                        this.nextDuration = nextDuration;
                    }

                    public int getNextEndUTCTime() {
                        return nextEndUTCTime;
                    }

                    public void setNextEndUTCTime(int nextEndUTCTime) {
                        this.nextEndUTCTime = nextEndUTCTime;
                    }

                    public int getNextStartUTCTime() {
                        return nextStartUTCTime;
                    }

                    public void setNextStartUTCTime(int nextStartUTCTime) {
                        this.nextStartUTCTime = nextStartUTCTime;
                    }

                    public int getStartTime() {
                        return startTime;
                    }

                    public void setStartTime(int startTime) {
                        this.startTime = startTime;
                    }

                    public int getStartUTCTime() {
                        return startUTCTime;
                    }

                    public void setStartUTCTime(int startUTCTime) {
                        this.startUTCTime = startUTCTime;
                    }

                    public int getTimeChangeDetails() {
                        return timeChangeDetails;
                    }

                    public void setTimeChangeDetails(int timeChangeDetails) {
                        this.timeChangeDetails = timeChangeDetails;
                    }
                }
            }
        }
    }
}
