package com.trans.libnet.tcpclient.obu;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author Tom灿
 * @description: 路测交通消息
 * @date :2023/6/2 17:03
 */
public class OBU_RSI {

    @SerializedName("RSI")
    private RSIBean rSI;

    public RSIBean getRSI() {
        return rSI;
    }

    public void setRSI(RSIBean rSI) {
        this.rSI = rSI;
    }

    public static class RSIBean {
        private String id;
        private RefPosBean refPos;
        private List<RtesBean> rtes;
        private List<RtssBean> rtss;
        private double utcTime;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public RefPosBean getRefPos() {
            return refPos;
        }

        public void setRefPos(RefPosBean refPos) {
            this.refPos = refPos;
        }

        public List<RtesBean> getRtes() {
            return rtes;
        }

        public void setRtes(List<RtesBean> rtes) {
            this.rtes = rtes;
        }

        public List<RtssBean> getRtss() {
            return rtss;
        }

        public void setRtss(List<RtssBean> rtss) {
            this.rtss = rtss;
        }

        public double getUtcTime() {
            return utcTime;
        }

        public void setUtcTime(double utcTime) {
            this.utcTime = utcTime;
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

        public static class RtesBean {
            private String description;
            private int eventConfidence;
            private int eventSource;
            private int eventType;
            private int priority;
            private List<ReferenceLinksBean> referenceLinks;
            private List<ReferencePathsBean> referencePaths;
            private int rteId;
            private TimeDetailsBean timeDetails;
            private EventPosBean eventPos;
            private int eventRadius;

            public String getDescription() {
                return description;
            }

            public void setDescription(String description) {
                this.description = description;
            }

            public int getEventConfidence() {
                return eventConfidence;
            }

            public void setEventConfidence(int eventConfidence) {
                this.eventConfidence = eventConfidence;
            }

            public int getEventSource() {
                return eventSource;
            }

            public void setEventSource(int eventSource) {
                this.eventSource = eventSource;
            }

            public int getEventType() {
                return eventType;
            }

            public void setEventType(int eventType) {
                this.eventType = eventType;
            }

            public int getPriority() {
                return priority;
            }

            public void setPriority(int priority) {
                this.priority = priority;
            }

            public List<ReferenceLinksBean> getReferenceLinks() {
                return referenceLinks;
            }

            public void setReferenceLinks(List<ReferenceLinksBean> referenceLinks) {
                this.referenceLinks = referenceLinks;
            }

            public List<ReferencePathsBean> getReferencePaths() {
                return referencePaths;
            }

            public void setReferencePaths(List<ReferencePathsBean> referencePaths) {
                this.referencePaths = referencePaths;
            }

            public int getRteId() {
                return rteId;
            }

            public void setRteId(int rteId) {
                this.rteId = rteId;
            }

            public TimeDetailsBean getTimeDetails() {
                return timeDetails;
            }

            public void setTimeDetails(TimeDetailsBean timeDetails) {
                this.timeDetails = timeDetails;
            }

            public EventPosBean getEventPos() {
                return eventPos;
            }

            public void setEventPos(EventPosBean eventPos) {
                this.eventPos = eventPos;
            }

            public int getEventRadius() {
                return eventRadius;
            }

            public void setEventRadius(int eventRadius) {
                this.eventRadius = eventRadius;
            }

            public static class TimeDetailsBean {
                private int endTime;
                private int endTimeConfidence;
                private int startTime;

                public int getEndTime() {
                    return endTime;
                }

                public void setEndTime(int endTime) {
                    this.endTime = endTime;
                }

                public int getEndTimeConfidence() {
                    return endTimeConfidence;
                }

                public void setEndTimeConfidence(int endTimeConfidence) {
                    this.endTimeConfidence = endTimeConfidence;
                }

                public int getStartTime() {
                    return startTime;
                }

                public void setStartTime(int startTime) {
                    this.startTime = startTime;
                }
            }

            public static class EventPosBean {
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

            public static class ReferenceLinksBean {
                private DownstreamNodeIdBean downstreamNodeId;
                private int referenceLanes;
                private UpstreamNodeIdBean upstreamNodeId;

                public DownstreamNodeIdBean getDownstreamNodeId() {
                    return downstreamNodeId;
                }

                public void setDownstreamNodeId(DownstreamNodeIdBean downstreamNodeId) {
                    this.downstreamNodeId = downstreamNodeId;
                }

                public int getReferenceLanes() {
                    return referenceLanes;
                }

                public void setReferenceLanes(int referenceLanes) {
                    this.referenceLanes = referenceLanes;
                }

                public UpstreamNodeIdBean getUpstreamNodeId() {
                    return upstreamNodeId;
                }

                public void setUpstreamNodeId(UpstreamNodeIdBean upstreamNodeId) {
                    this.upstreamNodeId = upstreamNodeId;
                }

                public static class DownstreamNodeIdBean {
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
            }

            public static class ReferencePathsBean {
                private List<ActivePathBean> activePath;
                private int pathRadius;

                public List<ActivePathBean> getActivePath() {
                    return activePath;
                }

                public void setActivePath(List<ActivePathBean> activePath) {
                    this.activePath = activePath;
                }

                public int getPathRadius() {
                    return pathRadius;
                }

                public void setPathRadius(int pathRadius) {
                    this.pathRadius = pathRadius;
                }

                public static class ActivePathBean {
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

        public static class RtssBean {
            private String description;
            private int priority;
            private List<ReferenceLinksBean> referenceLinks;
            private List<ReferencePathsBean> referencePaths;
            private int rtsId;
            private SignPosBean signPos;
            private int signType;
            private TimeDetailsBean timeDetails;

            public String getDescription() {
                return description;
            }

            public void setDescription(String description) {
                this.description = description;
            }

            public int getPriority() {
                return priority;
            }

            public void setPriority(int priority) {
                this.priority = priority;
            }

            public List<ReferenceLinksBean> getReferenceLinks() {
                return referenceLinks;
            }

            public void setReferenceLinks(List<ReferenceLinksBean> referenceLinks) {
                this.referenceLinks = referenceLinks;
            }

            public List<ReferencePathsBean> getReferencePaths() {
                return referencePaths;
            }

            public void setReferencePaths(List<ReferencePathsBean> referencePaths) {
                this.referencePaths = referencePaths;
            }

            public int getRtsId() {
                return rtsId;
            }

            public void setRtsId(int rtsId) {
                this.rtsId = rtsId;
            }

            public SignPosBean getSignPos() {
                return signPos;
            }

            public void setSignPos(SignPosBean signPos) {
                this.signPos = signPos;
            }

            public int getSignType() {
                return signType;
            }

            public void setSignType(int signType) {
                this.signType = signType;
            }

            public TimeDetailsBean getTimeDetails() {
                return timeDetails;
            }

            public void setTimeDetails(TimeDetailsBean timeDetails) {
                this.timeDetails = timeDetails;
            }

            public static class SignPosBean {
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

            public static class TimeDetailsBean {
                private int endTime;
                private int endTimeConfidence;
                private int startTime;

                public int getEndTime() {
                    return endTime;
                }

                public void setEndTime(int endTime) {
                    this.endTime = endTime;
                }

                public int getEndTimeConfidence() {
                    return endTimeConfidence;
                }

                public void setEndTimeConfidence(int endTimeConfidence) {
                    this.endTimeConfidence = endTimeConfidence;
                }

                public int getStartTime() {
                    return startTime;
                }

                public void setStartTime(int startTime) {
                    this.startTime = startTime;
                }
            }

            public static class ReferenceLinksBean {
                private DownstreamNodeIdBean downstreamNodeId;
                private int referenceLanes;
                private UpstreamNodeIdBean upstreamNodeId;

                public DownstreamNodeIdBean getDownstreamNodeId() {
                    return downstreamNodeId;
                }

                public void setDownstreamNodeId(DownstreamNodeIdBean downstreamNodeId) {
                    this.downstreamNodeId = downstreamNodeId;
                }

                public int getReferenceLanes() {
                    return referenceLanes;
                }

                public void setReferenceLanes(int referenceLanes) {
                    this.referenceLanes = referenceLanes;
                }

                public UpstreamNodeIdBean getUpstreamNodeId() {
                    return upstreamNodeId;
                }

                public void setUpstreamNodeId(UpstreamNodeIdBean upstreamNodeId) {
                    this.upstreamNodeId = upstreamNodeId;
                }

                public static class DownstreamNodeIdBean {
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
            }

            public static class ReferencePathsBean {
                private List<ActivePathBean> activePath;
                private int pathRadius;

                public List<ActivePathBean> getActivePath() {
                    return activePath;
                }

                public void setActivePath(List<ActivePathBean> activePath) {
                    this.activePath = activePath;
                }

                public int getPathRadius() {
                    return pathRadius;
                }

                public void setPathRadius(int pathRadius) {
                    this.pathRadius = pathRadius;
                }

                public static class ActivePathBean {
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
    }
}
