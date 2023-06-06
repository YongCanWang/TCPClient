package com.trans.libnet.tcpclient.obu;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author Tom灿
 * @description:
 * @date :2023/6/5 9:04
 */
public class OBU_MAP {

    @SerializedName("MAP")
    private MAPBean mAP;

    public MAPBean getMAP() {
        return mAP;
    }

    public void setMAP(MAPBean mAP) {
        this.mAP = mAP;
    }

    public static class MAPBean {
        private List<NodesBean> nodes;
        private double utcTime;

        public List<NodesBean> getNodes() {
            return nodes;
        }

        public void setNodes(List<NodesBean> nodes) {
            this.nodes = nodes;
        }

        public double getUtcTime() {
            return utcTime;
        }

        public void setUtcTime(double utcTime) {
            this.utcTime = utcTime;
        }

        public static class NodesBean {
            private IdBean id;
            private List<InLinksBean> inLinks;
            private String name;
            private RefPosBean refPos;

            public IdBean getId() {
                return id;
            }

            public void setId(IdBean id) {
                this.id = id;
            }

            public List<InLinksBean> getInLinks() {
                return inLinks;
            }

            public void setInLinks(List<InLinksBean> inLinks) {
                this.inLinks = inLinks;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public RefPosBean getRefPos() {
                return refPos;
            }

            public void setRefPos(RefPosBean refPos) {
                this.refPos = refPos;
            }

            public static class IdBean {
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

            public static class RefPosBean {
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

            public static class InLinksBean {
                private List<LanesBean> lanes;
                private double linkWidth;
                private String name;
                private List<PodoublesBean> podoubles;
                private List<SpeedLimitsBean> speedLimits;
                private UpstreamNodeIdBean upstreamNodeId;

                public List<LanesBean> getLanes() {
                    return lanes;
                }

                public void setLanes(List<LanesBean> lanes) {
                    this.lanes = lanes;
                }

                public double getLinkWidth() {
                    return linkWidth;
                }

                public void setLinkWidth(double linkWidth) {
                    this.linkWidth = linkWidth;
                }

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public List<PodoublesBean> getPodoubles() {
                    return podoubles;
                }

                public void setPodoubles(List<PodoublesBean> podoubles) {
                    this.podoubles = podoubles;
                }

                public List<SpeedLimitsBean> getSpeedLimits() {
                    return speedLimits;
                }

                public void setSpeedLimits(List<SpeedLimitsBean> speedLimits) {
                    this.speedLimits = speedLimits;
                }

                public UpstreamNodeIdBean getUpstreamNodeId() {
                    return upstreamNodeId;
                }

                public void setUpstreamNodeId(UpstreamNodeIdBean upstreamNodeId) {
                    this.upstreamNodeId = upstreamNodeId;
                }

                public static class UpstreamNodeIdBean {
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

                public static class LanesBean {
                    private double laneID;

                    public double getLaneID() {
                        return laneID;
                    }

                    public void setLaneID(double laneID) {
                        this.laneID = laneID;
                    }
                }

                public static class PodoublesBean {
                    private PosBean pos;

                    public PosBean getPos() {
                        return pos;
                    }

                    public void setPos(PosBean pos) {
                        this.pos = pos;
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

                public static class SpeedLimitsBean {
                    private double speed;
                    private double type;

                    public double getSpeed() {
                        return speed;
                    }

                    public void setSpeed(double speed) {
                        this.speed = speed;
                    }

                    public double getType() {
                        return type;
                    }

                    public void setType(double type) {
                        this.type = type;
                    }
                }
            }
        }
    }
}
