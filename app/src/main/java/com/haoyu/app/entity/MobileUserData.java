package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by acer1 on 2017/2/16.
 */
public class MobileUserData {
    @Expose
    @SerializedName("mUsers")
    private List<MobileUser> mUsers;
    @Expose
    @SerializedName("paginator")
    private Paginator paginator;

    public List<MobileUser> getmUsers() {
        if (mUsers == null) {
            return new ArrayList<>();
        }
        return mUsers;
    }

    public void setmUsers(List<MobileUser> mUsers) {
        this.mUsers = mUsers;
    }

    public Paginator getPaginator() {
        return paginator;
    }

    public void setPaginator(Paginator paginator) {
        this.paginator = paginator;
    }
}
