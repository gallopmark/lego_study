package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by acer1 on 2017/1/11.
 */
public class ConsultResponseData {
    @Expose
    @SerializedName("mUsers")
    private List<MobileUser> mUsers;
    @Expose
    @SerializedName("paginator")
    private Paginator paginator;

    public void setMUsers(List<MobileUser> mUsers) {
        this.mUsers = mUsers;
    }

    public List<MobileUser> getMUsers() {
        return this.mUsers;
    }

    public void setPaginator(Paginator paginator) {
        this.paginator = paginator;
    }

    public Paginator getPaginator() {
        return this.paginator;
    }

}