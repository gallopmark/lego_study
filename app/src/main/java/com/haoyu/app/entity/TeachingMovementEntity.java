package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2017/1/11 on 10:48
 * 描述: 社区活动实体类
 * 作者:马飞奔 Administrator
 * id	ID	String	Y
 * title	标题	String	Y
 * location	地点	String	Y
 * mMovementRelations	活动关联关系	List	Y
 * creator	发起人	MUser	Y	MUser详见公共对象
 * image	封面图片	String	Y
 * 	mMovementRelations详细
 * 名称	说明	类型	必填	备注
 * id	ID	String	Y
 * timePeriod	活动时间	TimePeriod 	Y	TimePeriod详见公共对象
 * registerTimePeriod	报名时间	TimePeriod 	Y	TimePeriod详见公共对象
 * participateNum	参加人数	Integer	Y
 * browseNum	浏览人数	Integer	Y
 */
public class TeachingMovementEntity implements Serializable {
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("title")
    private String title;
    @Expose
    @SerializedName("content")
    private String content;
    @Expose
    @SerializedName("location")
    private String location;
    @Expose
    @SerializedName("mMovementRelations")
    private List<MovementRelation> mMovementRelations = new ArrayList<>();
    @Expose
    @SerializedName("creator")
    private MobileUser creator;
    @Expose
    @SerializedName("image")
    private String image;
    @Expose
    @SerializedName("sponsor")
    private String sponsor;
    @Expose
    @SerializedName("type")   //活动类型
    private String type;
    @Expose
    @SerializedName("participationType")
    private String participationType;
    @Expose
    @SerializedName("mMovementRegisters")
    private List<MovementRegisters> mMovementRegisters = new ArrayList<>();
    @Expose
    @SerializedName("mFileInfos")
    private List<MFileInfo> mFileInfos = new ArrayList<>();
    @Expose
    @SerializedName("state")
    private String state;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<MovementRelation> getmMovementRelations() {
        return mMovementRelations;
    }

    public void setmMovementRelations(List<MovementRelation> mMovementRelations) {
        this.mMovementRelations = mMovementRelations;
    }

    public MobileUser getCreator() {
        return creator;
    }

    public void setCreator(MobileUser creator) {
        this.creator = creator;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getSponsor() {
        return sponsor;
    }

    public void setSponsor(String sponsor) {
        this.sponsor = sponsor;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getParticipationType() {
        return participationType;
    }

    public void setParticipationType(String participationType) {
        this.participationType = participationType;
    }

    public List<MovementRegisters> getmMovementRegisters() {
        return mMovementRegisters;
    }

    public void setmMovementRegisters(List<MovementRegisters> mMovementRegisters) {
        this.mMovementRegisters = mMovementRegisters;
    }

    public List<MFileInfo> getmFileInfos() {
        return mFileInfos;
    }

    public void setmFileInfos(List<MFileInfo> mFileInfos) {
        this.mFileInfos = mFileInfos;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public static class MovementRelation {
        /**
         * id
         * timePeriod
         * registerTimePeriod
         * participateNum
         * browseNum
         */
        @Expose
        @SerializedName("id")
        private String id;
        @Expose
        @SerializedName("timePeriod")
        private TimePeriod timePeriod;     //活动时间
        @Expose
        @SerializedName("registerTimePeriod")
        private TimePeriod registerTimePeriod;  //报名时间
        @Expose
        @SerializedName("participateNum")
        private int participateNum;  //参与数
        @Expose
        @SerializedName("browseNum")   //总浏览数
        private int browseNum;
        @Expose
        @SerializedName("ticketNum")
        private int ticketNum;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public TimePeriod getTimePeriod() {
            return timePeriod;
        }

        public void setTimePeriod(TimePeriod timePeriod) {
            this.timePeriod = timePeriod;
        }

        public TimePeriod getRegisterTimePeriod() {
            return registerTimePeriod;
        }

        public void setRegisterTimePeriod(TimePeriod registerTimePeriod) {
            this.registerTimePeriod = registerTimePeriod;
        }

        public int getParticipateNum() {
            return participateNum;
        }

        public void setParticipateNum(int participateNum) {
            this.participateNum = participateNum;
        }

        public int getBrowseNum() {
            return browseNum;
        }

        public void setBrowseNum(int browseNum) {
            this.browseNum = browseNum;
        }

        public int getTicketNum() {
            return ticketNum;
        }

        public void setTicketNum(int ticketNum) {
            this.ticketNum = ticketNum;
        }
    }

    public static class MovementRegisters {
        @Expose
        @SerializedName("id")
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
