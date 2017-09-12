package com.haoyu.app.entity;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 创建日期：2016/12/23 on 15:12
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class AppActivityViewEntity implements Serializable {
    @Expose
    @SerializedName("mActivityResult")
    private ActivityResult mActivityResult;   //活动完成情况
    @Expose
    @SerializedName("mVideoUser")
    private VideoUserMobileEntity mVideoUser;  //视频类型
    @Expose
    @SerializedName("mSurveyUser")
    private SurveyUserMobileEntity mSurveyUser;  //问卷调查类型
    @Expose
    @SerializedName("mDiscussionUser")
    private DiscussionUserMobileEntity mDiscussionUser; //课程研讨类型
    @Expose
    @SerializedName("mTextInfoUser")
    private TextInfoUserMobileEntity mTextInfoUser;    //课件类型 html.pdf
    @Expose
    @SerializedName("mTestUser")
    private TestUserMobileEntity mTestUser;  //测验类型
    @Expose
    @SerializedName("mAssignmentUser")
    private HomeWorkEntity mAssignmentUser;  //作业
    @Expose
    @SerializedName("mLcec")    //听课评课
    private MLcecMobileEntity mLcec;

    public ActivityResult getmActivityResult() {
        return mActivityResult;
    }

    public void setmActivityResult(ActivityResult mActivityResult) {
        this.mActivityResult = mActivityResult;
    }

    public VideoUserMobileEntity getmVideoUser() {
        return mVideoUser;
    }

    public void setmVideoUser(VideoUserMobileEntity mVideoUser) {
        this.mVideoUser = mVideoUser;
    }

    public SurveyUserMobileEntity getmSurveyUser() {
        return mSurveyUser;
    }

    public void setmSurveyUser(SurveyUserMobileEntity mSurveyUser) {
        this.mSurveyUser = mSurveyUser;
    }

    public DiscussionUserMobileEntity getmDiscussionUser() {
        return mDiscussionUser;
    }

    public void setmDiscussionUser(DiscussionUserMobileEntity mDiscussionUser) {
        this.mDiscussionUser = mDiscussionUser;
    }

    public TextInfoUserMobileEntity getmTextInfoUser() {
        return mTextInfoUser;
    }

    public void setmTextInfoUser(TextInfoUserMobileEntity mTextInfoUser) {
        this.mTextInfoUser = mTextInfoUser;
    }

    public TestUserMobileEntity getmTestUser() {
        return mTestUser;
    }

    public void setmTestUser(TestUserMobileEntity mTestUser) {
        this.mTestUser = mTestUser;
    }

    public HomeWorkEntity getmAssignmentUser() {
        return mAssignmentUser;
    }

    public void setmAssignmentUser(HomeWorkEntity mAssignmentUser) {
        this.mAssignmentUser = mAssignmentUser;
    }

    public MLcecMobileEntity getmLcec() {
        return mLcec;
    }

    public void setmLcec(MLcecMobileEntity mLcec) {
        this.mLcec = mLcec;
    }

    public class ActivityResult implements Serializable {
        @Expose
        @SerializedName("id")
        private String id;
        @Expose
        @SerializedName("mActivity")
        private CourseSectionActivity mActivity;
        @SerializedName("state")
        @Expose
        private String state;
        @Expose
        @SerializedName("score")   //活动得分
        private double score;
        @Expose
        @SerializedName("mVideoUser")
        private VideoUserMobileEntity mVideoUser;
        @Expose
        @SerializedName("detailMap")
        private DetailMap detailMap;

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public CourseSectionActivity getmActivity() {
            return mActivity;
        }

        public void setmActivity(CourseSectionActivity mActivity) {
            this.mActivity = mActivity;
        }

        public VideoUserMobileEntity getmVideoUser() {
            return mVideoUser;
        }

        public void setmVideoUser(VideoUserMobileEntity mVideoUser) {
            this.mVideoUser = mVideoUser;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }

        public DetailMap getDetailMap() {
            return detailMap;
        }

        public void setDetailMap(DetailMap detailMap) {
            this.detailMap = detailMap;
        }
    }

    public class VideoUserMobileEntity implements Serializable {
        @Expose
        @SerializedName("id")
        private String id;
        @Expose
        @SerializedName("viewTime")
        private double viewTime;
        @Expose
        @SerializedName("lastViewTime")
        private double lastViewTime;
        @Expose
        @SerializedName("timing")
        private boolean timing;
        @Expose
        @SerializedName("mVideo")
        private VideoMobileEntity mVideo;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public double getViewTime() {
            return viewTime;
        }

        public void setViewTime(double viewTime) {
            this.viewTime = viewTime;
        }

        public double getLastViewTime() {
            return lastViewTime;
        }

        public void setLastViewTime(double lastViewTime) {
            this.lastViewTime = lastViewTime;
        }

        public boolean isTiming() {
            return timing;
        }

        public void setTiming(boolean timing) {
            this.timing = timing;
        }

        public VideoMobileEntity getmVideo() {
            return mVideo;
        }

        public void setmVideo(VideoMobileEntity mVideo) {
            this.mVideo = mVideo;
        }
    }

    public class SurveyUserMobileEntity implements Serializable {
        @Expose
        @SerializedName("state")
        private String state;
        @Expose
        @SerializedName("mSurvey")
        private CourseSurveyEntity mSurvey;

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public CourseSurveyEntity getmSurvey() {
            return mSurvey;
        }

        public void setmSurvey(CourseSurveyEntity mSurvey) {
            this.mSurvey = mSurvey;
        }
    }

    public class DiscussionUserMobileEntity implements Serializable {
        @Expose
        @SerializedName("mainPostNum")
        private int mainPostNum;
        @Expose
        @SerializedName("subPostNum")
        private int subPostNum;
        @Expose
        @SerializedName("mDiscussion")
        private DiscussEntity mDiscussion;

        public int getMainPostNum() {
            return mainPostNum;
        }

        public void setMainPostNum(int mainPostNum) {
            this.mainPostNum = mainPostNum;
        }

        public DiscussEntity getmDiscussion() {
            return mDiscussion;
        }

        public void setmDiscussion(DiscussEntity mDiscussion) {
            this.mDiscussion = mDiscussion;
        }

        public int getSubPostNum() {
            return subPostNum;
        }

        public void setSubPostNum(int subPostNum) {
            this.subPostNum = subPostNum;
        }
    }

    public class TextInfoUserMobileEntity implements Serializable {
        /**
         * id	课件完成情况ID	String	Y
         * viewNum	已观看次数	Int	Y
         * mTextInfo	课件对象	Object	Y
         */
        @Expose
        @SerializedName("id")
        private String id;
        @Expose
        @SerializedName("viewNum")
        private int viewNum;
        @Expose
        @SerializedName("mTextInfo")
        private CoursewareMobileEntity mTextInfo;

        public int getViewNum() {
            return viewNum;
        }

        public void setViewNum(int viewNum) {
            this.viewNum = viewNum;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public CoursewareMobileEntity getmTextInfo() {
            return mTextInfo;
        }

        public void setmTextInfo(CoursewareMobileEntity mTextInfo) {
            this.mTextInfo = mTextInfo;
        }
    }

    public class TestUserMobileEntity implements Serializable {
        /**
         * id	测验完成情况ID	String	Y
         * completionStatus	测验完成状态	String	Y	completed:已完成
         * null:未参与
         * attempts	参与次数	Int	Y
         * sumScore	测验满分	Double	Y
         * mTestSubmissionMap	题目提交明细	Map	Y
         * mTest	测验	Object	Y
         */
        @Expose
        @SerializedName("id")
        private String id;
        @Expose
        @SerializedName("completionStatus")
        private String completionStatus;
        @Expose
        @SerializedName("attempts")
        private int attempts;
        @Expose
        @SerializedName("sumScore")
        private double sumScore;
        @Expose
        @SerializedName("mTestSubmissionMap")
        private Object mTestSubmissionMap;
        @Expose
        @SerializedName("mTest")
        private AppTestMobileEntity mTest;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getCompletionStatus() {
            return completionStatus;
        }

        public void setCompletionStatus(String completionStatus) {
            this.completionStatus = completionStatus;
        }

        public int getAttempts() {
            return attempts;
        }

        public void setAttempts(int attempts) {
            this.attempts = attempts;
        }

        public double getSumScore() {
            return sumScore;
        }

        public void setSumScore(double sumScore) {
            this.sumScore = sumScore;
        }

        public Map<String, MTestSubmission> getmTestSubmissionMap() {
            try {
                Gson gson = new Gson();
                String toJson = gson.toJson(mTestSubmissionMap);
                Map<String, MTestSubmission> map = gson.fromJson(toJson, new TypeToken<Map<String, MTestSubmission>>() {
                }.getType());
                return map;
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                return new HashMap<>();
            }
        }

        public void setmTestSubmissionMap(Object mTestSubmissionMap) {
            this.mTestSubmissionMap = mTestSubmissionMap;
        }

        public AppTestMobileEntity getmTest() {
            return mTest;
        }

        public void setmTest(AppTestMobileEntity mTest) {
            this.mTest = mTest;
        }
    }

    public class HomeWorkEntity implements Serializable {
        @Expose
        @SerializedName("id")
        private String id;
        @Expose
        @SerializedName("mAssignment")
        private MAssignmentEntity mAssignment;
        @Expose
        @SerializedName("state")
        private String state;
        @Expose
        @SerializedName("responseTime")
        private long responseTime;
        @Expose
        @SerializedName("responseScore")
        private double responseScore;
        @Expose
        @SerializedName("markNum")
        private int markNum;
        @Expose
        @SerializedName("markScore")
        private double markScore;
        @Expose
        @SerializedName("markedNum")
        private int markedNum;
        @Expose
        @SerializedName("assignmentRelationId")
        private String assignmentRelationId;
        @Expose
        @SerializedName("mFileInfos")
        private List<MFileInfo> mFileInfos;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public MAssignmentEntity getmAssignmentEntity() {
            return mAssignment;
        }

        public void setmAssignmentEntity(MAssignmentEntity mAssignmentEntity) {
            this.mAssignment = mAssignmentEntity;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public long getResponseTime() {
            return responseTime;
        }

        public void setResponseTime(long responseTime) {
            this.responseTime = responseTime;
        }

        public double getResponseScore() {
            return responseScore;
        }

        public void setResponseScore(double responseScore) {
            this.responseScore = responseScore;
        }

        public int getMarkNum() {
            return markNum;
        }

        public void setMarkNum(int markNum) {
            this.markNum = markNum;
        }

        public double getMarkScore() {
            return markScore;
        }

        public void setMarkScore(double markScore) {
            this.markScore = markScore;
        }

        public int getMarkedNum() {
            return markedNum;
        }

        public void setMarkedNum(int markedNum) {
            this.markedNum = markedNum;
        }

        public String getAssignmentRelationId() {
            return assignmentRelationId;
        }

        public void setAssignmentRelationId(String assignmentRelationId) {
            this.assignmentRelationId = assignmentRelationId;
        }

        public List<MFileInfo> getmFileInfos() {
            return mFileInfos;
        }

        public void setmFileInfos(List<MFileInfo> mFileInfos) {
            this.mFileInfos = mFileInfos;
        }
    }

    public class MLcecMobileEntity implements Serializable {
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
        @SerializedName("textbook")
        private String textbook;
        @Expose
        @SerializedName("teacher")
        private MobileUser teacher;
        @Expose
        @SerializedName("mVideo")
        private MFileInfo mVideo;
        @Expose
        @SerializedName("mFileInfos")
        private List<MFileInfo> mFileInfos = new ArrayList<>();
        @Expose
        @SerializedName("type")
        private String type;
        @Expose
        @SerializedName("stage")
        private String stage;
        @Expose
        @SerializedName("subject")
        private String subject;
        @Expose
        @SerializedName("hasSubmitEvaluate")
        private boolean hasSubmitEvaluate;//是否提交

        public boolean isHasSubmitEvaluate() {
            return hasSubmitEvaluate;
        }

        public void setHasSubmitEvaluate(boolean hasSubmitEvaluate) {
            this.hasSubmitEvaluate = hasSubmitEvaluate;
        }

        public String getStage() {
            return stage;
        }

        public void setStage(String stage) {
            this.stage = stage;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
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

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getTextbook() {
            return textbook;
        }

        public void setTextbook(String textbook) {
            this.textbook = textbook;
        }

        public MobileUser getTeacher() {
            return teacher;
        }

        public void setTeacher(MobileUser teacher) {
            this.teacher = teacher;
        }

        public MFileInfo getmVideo() {
            return mVideo;
        }

        public void setmVideo(MFileInfo mVideo) {
            this.mVideo = mVideo;
        }

        public List<MFileInfo> getmFileInfos() {
            return mFileInfos;
        }

        public void setmFileInfos(List<MFileInfo> mFileInfos) {
            this.mFileInfos = mFileInfos;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
