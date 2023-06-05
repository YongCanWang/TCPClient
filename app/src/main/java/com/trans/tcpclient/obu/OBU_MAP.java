package com.trans.tcpclient.obu;

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
                private int linkWidth;
                private String name;
                private List<PointsBean> points;
                private List<SpeedLimitsBean> speedLimits;
                private UpstreamNodeIdBean upstreamNodeId;

                public List<LanesBean> getLanes() {
                    return lanes;
                }

                public void setLanes(List<LanesBean> lanes) {
                    this.lanes = lanes;
                }

                public int getLinkWidth() {
                    return linkWidth;
                }

                public void setLinkWidth(int linkWidth) {
                    this.linkWidth = linkWidth;
                }

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public List<PointsBean> getPoints() {
                    return points;
                }

                public void setPoints(List<PointsBean> points) {
                    this.points = points;
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

                public static class LanesBean {
                    private int laneID;

                    public int getLaneID() {
                        return laneID;
                    }

                    public void setLaneID(int laneID) {
                        this.laneID = laneID;
                    }
                }

                public static class PointsBean {
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
                    private int type;

                    public double getSpeed() {
                        return speed;
                    }

                    public void setSpeed(double speed) {
                        this.speed = speed;
                    }

                    public int getType() {
                        return type;
                    }

                    public void setType(int type) {
                        this.type = type;
                    }
                }
            }
        }
    }
}
