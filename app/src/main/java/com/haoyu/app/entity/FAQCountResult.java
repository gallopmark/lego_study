package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 创建日期：2016/12/7 on 16:31
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class FAQCountResult {
    @SerializedName("responseCode")
    @Expose
    private String responseCode;
    @SerializedName("responseData")
    @Expose
    private CountMap responseData;
    @SerializedName("success")
    @Expose
    private Boolean success;

    /**
     *
     * @return
     * The responseCode
     */
    public String getResponseCode() {
        return responseCode;
    }

    /**
     *
     * @param responseCode
     * The responseCode
     */
    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    /**
     *
     * @return
     * The responseData
     */
    public CountMap getResponseData() {
        return responseData;
    }

    /**
     *
     * @param responseData
     * The responseData
     */
    public void setResponseData(CountMap responseData) {
        this.responseData = responseData;
    }

    /**
     *
     * @return
     * The success
     */
    public Boolean getSuccess() {
        return success;
    }

    /**
     *
     * @param success
     * The success
     */
    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public class CountMap{

        @SerializedName("all")
        @Expose
        private int all;

        /**
         *
         * @return
         * The all
         */
        public int getAll() {
            return all;
        }

        /**
         *
         * @param all
         * The all
         */
        public void setAll(int all) {
            this.all = all;
        }

    }
}
