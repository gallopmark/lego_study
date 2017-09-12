package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * id	用户ID
 * userName	用户名
 * realName	姓名
 * deptId	单位ID
 * deptName	单位名
 * avatar	用户头像地址
 */
public class MobileUser implements Serializable {

    public static String ROLE_STUDENT = "student";
    private static final long serialVersionUID = 1L;
    @Expose
    @SerializedName("avatar")
    private String avatar;
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("realName")
    private String realName;
    @Expose
    @SerializedName("userName")
    private String userName;
    @Expose
    @SerializedName("deptName")
    private String deptName;
    @Expose
    @SerializedName("role")
    private String role;
    @Expose
    @SerializedName("email")
    private String email;
    @Expose
    @SerializedName("phone")
    private String phone;
    @Expose
    @SerializedName("mStage")
    private DictEntryMobileEntity mStage;
    @Expose
    @SerializedName("mSubject")
    private DictEntryMobileEntity mSubject;
    @Expose
    @SerializedName("mDepartment")
    private MDepartment mDepartment;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public DictEntryMobileEntity getmStage() {
        return mStage;
    }

    public void setmStage(DictEntryMobileEntity mStage) {
        this.mStage = mStage;
    }

    public DictEntryMobileEntity getmSubject() {
        return mSubject;
    }

    public void setmSubject(DictEntryMobileEntity mSubject) {
        this.mSubject = mSubject;
    }

    public MDepartment getmDepartment() {
        return mDepartment;
    }

    public void setmDepartment(MDepartment mDepartment) {
        this.mDepartment = mDepartment;
    }
}