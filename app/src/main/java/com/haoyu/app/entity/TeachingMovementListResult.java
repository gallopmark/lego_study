package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.haoyu.app.base.BaseResponseResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2017/1/11 on 10:56
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class TeachingMovementListResult extends BaseResponseResult<TeachingMovementListResult.MData> {
    public class MData {
        /*mMovements	活动List	List	Y
        paginator	分页信息	Object	Y	paginator详见公共对象
        */
        @Expose
        @SerializedName("mMovements")
        private List<TeachingMovementEntity> mMovements;
        @Expose
        @SerializedName("paginator")
        private Paginator paginator;

        public Paginator getPaginator() {
            return paginator;
        }

        public void setPaginator(Paginator paginator) {
            this.paginator = paginator;
        }

        public List<TeachingMovementEntity> getmMovements() {
            if (mMovements == null) {
                return new ArrayList<>();
            }
            return mMovements;
        }

        public void setmMovements(List<TeachingMovementEntity> mMovements) {
            this.mMovements = mMovements;
        }
    }
}
