package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 创建日期：2017/3/3 on 17:43
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class MTestSubmission {
    /**
     * MTestSubmission详细
     * 名称	说明	类型	必填	备注
     * candidateResponses	已选的选项	List<String>	N	格式为["Choice0","Choice1"]
     * Choice0, Choice1为选项ID.
     * 如果还没参与, 则返回null
     * correct	是否正确	Boolean	Y
     */
    @Expose
    @SerializedName("candidateResponses")
    private List<String> candidateResponses;
    @Expose
    @SerializedName("correct")
    private boolean correct;

    public List<String> getCandidateResponses() {
        return candidateResponses;
    }

    public void setCandidateResponses(List<String> candidateResponses) {
        this.candidateResponses = candidateResponses;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }
}
