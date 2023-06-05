package com.trans.tcpclient.obu;

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
        private List<IntersectionsBean> intersections;

        public List<IntersectionsBean> getIntersections() {
            return intersections;
        }

        public void setIntersections(List<IntersectionsBean> intersections) {
            this.intersections = intersections;
        }

        public static class IntersectionsBean {
            private IntersectionIdBean intersectionId;
            private List<PhasesBean> phases;
            private int status;

            public IntersectionIdBean getIntersectionId() {
                return intersectionId;
            }

            public void setIntersectionId(IntersectionIdBean intersectionId) {
                this.intersectionId = intersectionId;
            }

            public List<PhasesBean> getPhases() {
                return phases;
            }

            public void setPhases(List<PhasesBean> phases) {
                this.phases = phases;
            }

            public int getStatus() {
                return status;
            }

            public void setStatus(int status) {
                this.status = status;
            }

            public static class IntersectionIdBean {
                private int id;
                private int region;

                public int getId() {
                    return id;
                }

                public void setId(int id) {
                    this.id = id;
                }

                public int getRegion() {
                    return region;
                }

                public void setRegion(int region) {
                    this.region = region;
                }
            }

            public static class PhasesBean {
                private int id;
                private List<PhaseStatesBean> phaseStates;

                public int getId() {
                    return id;
                }

                public void setId(int id) {
                    this.id = id;
                }

                public List<PhaseStatesBean> getPhaseStates() {
                    return phaseStates;
                }

                public void setPhaseStates(List<PhaseStatesBean> phaseStates) {
                    this.phaseStates = phaseStates;
                }

                public static class PhaseStatesBean {
                    private int light;
                    private TimingBean timing;

                    public int getLight() {
                        return light;
                    }

                    public void setLight(int light) {
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
