package com.trans.libnet.tcpclient.obu;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author Tom灿
 * @description:
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
        private List<IntersectionsBean> doubleersections;

        public List<IntersectionsBean> getIntersections() {
            return doubleersections;
        }

        public void setIntersections(List<IntersectionsBean> doubleersections) {
            this.doubleersections = doubleersections;
        }

        public static class IntersectionsBean {
            private IntersectionIdBean doubleersectionId;
            private List<PhasesBean> phases;
            private double status;

            public IntersectionIdBean getIntersectionId() {
                return doubleersectionId;
            }

            public void setIntersectionId(IntersectionIdBean doubleersectionId) {
                this.doubleersectionId = doubleersectionId;
            }

            public List<PhasesBean> getPhases() {
                return phases;
            }

            public void setPhases(List<PhasesBean> phases) {
                this.phases = phases;
            }

            public double getStatus() {
                return status;
            }

            public void setStatus(double status) {
                this.status = status;
            }

            public static class IntersectionIdBean {
                private double id;
                private double region;

                public double getId() {
                    return id;
                }

                public void setId(double id) {
                    this.id = id;
                }

                public double getRegion() {
                    return region;
                }

                public void setRegion(double region) {
                    this.region = region;
                }
            }

            public static class PhasesBean {
                private double id;
                private List<PhaseStatesBean> phaseStates;

                public double getId() {
                    return id;
                }

                public void setId(double id) {
                    this.id = id;
                }

                public List<PhaseStatesBean> getPhaseStates() {
                    return phaseStates;
                }

                public void setPhaseStates(List<PhaseStatesBean> phaseStates) {
                    this.phaseStates = phaseStates;
                }

                public static class PhaseStatesBean {
                    private double light;
                    private TimingBean timing;

                    public double getLight() {
                        return light;
                    }

                    public void setLight(double light) {
                        this.light = light;
                    }

                    public TimingBean getTiming() {
                        return timing;
                    }

                    public void setTiming(TimingBean timing) {
                        this.timing = timing;
                    }

                    public static class TimingBean {
                        private CountingBean counting;

                        public CountingBean getCounting() {
                            return counting;
                        }

                        public void setCounting(CountingBean counting) {
                            this.counting = counting;
                        }

                        public static class CountingBean {
                            private double likelyEndTime;
                            private double startTime;

                            public double getLikelyEndTime() {
                                return likelyEndTime;
                            }

                            public void setLikelyEndTime(double likelyEndTime) {
                                this.likelyEndTime = likelyEndTime;
                            }

                            public double getStartTime() {
                                return startTime;
                            }

                            public void setStartTime(double startTime) {
                                this.startTime = startTime;
                            }
                        }
                    }
                }
            }
        }
    }
}
