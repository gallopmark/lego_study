package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class CourseSingleResult {
    @SerializedName("responseCode")
    @Expose
    private String responseCode;
    @SerializedName("responseData")
    @Expose
    private CourseSingleResponseData responseData;
    @SerializedName("responseMsg")
    @Expose
    private String responseMsg;
    @SerializedName("success")
    @Expose
    private Boolean success;

    /**
     * @return The responseCode
     */
    public String getResponseCode() {
        return responseCode;
    }

    /**
     * @param responseCode The responseCode
     */
    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    /**
     * @return The responseData
     */
    public CourseSingleResponseData getResponseData() {
        return responseData;
    }

    /**
     * @param responseData The responseData
     */
    public void setResponseData(CourseSingleResponseData responseData) {
        this.responseData = responseData;
    }

    public String getResponseMsg() {
        return responseMsg;
    }

    public void setResponseMsg(String responseMsg) {
        this.responseMsg = responseMsg;
    }

    /**
     * @return The success
     */
    public Boolean getSuccess() {
        return success;
    }

    /**
     * @param success The success
     */
    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public class CourseSingleResponseData implements Serializable {
        /**
         * noSubmit：未报读
         submit：待审核
         pass：审核通过
         nopass：审核不通过
         */
        @SerializedName("registerState")
        @Expose
        private String registerState;
        @SerializedName("mCourseRegister")
        @Expose
        private MCourseRegister mCourseRegister;
        @SerializedName("teachers")
        @Expose
        private List<MobileUser> teachers;
        @SerializedName("mCourse")
        @Expose
        private CourseMobileEntity mCourse;

        public String getRegisterState() {
            return registerState;
        }

        public void setRegisterState(String registerState) {
            this.registerState = registerState;
        }

        public List<MobileUser> getTeachers() {
            return teachers;
        }

        public void setTeachers(List<MobileUser> teachers) {
            this.teachers = teachers;
        }

        public CourseMobileEntity getmCourse() {
            return mCourse;
        }

        public void setmCourse(CourseMobileEntity mCourse) {
            this.mCourse = mCourse;
        }

        public MCourseRegister getmCourseRegister() {
            return mCourseRegister;
        }

        public void setmCourseRegister(MCourseRegister mCourseRegister) {
            this.mCourseRegister = mCourseRegister;
        }
    }
}
