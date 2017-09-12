package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2016/12/24 on 10:12
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class SurveyAnswer implements Serializable {

    /**
     * id	问题id	String	Y
     * title	标题	String	Y
     * type	问题类型	String	Y	singleChoice:单选题
     * multipleChoice:多选题
     * trueOrFalse:是非题
     * textEntry:问答题
     * <p/>
     * minWords	最小字数	int	N	问答题答案最小字数，为0时不限
     * <p/>
     * maxWords	最大字数	int	N	问答题答案最大字数，为0时不限
     * minChoose	最小选中数	int	N	多选题至少选几项,为0时不限
     * maxChoose	最大选中数	int	N	多选题至多选几项，为0时不限
     */
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("title")
    private String title;
    @Expose
    @SerializedName("type")
    private String type;
    @Expose
    @SerializedName("minWords")
    private int minWords;
    @Expose
    @SerializedName("maxWords")
    private int maxWords;
    @Expose
    @SerializedName("minChoose")
    private int minChoose;
    @Expose
    @SerializedName("maxChoose")
    private int maxChoose;
    @Expose
    @SerializedName("mChoices")
    private List<MChoices> mChoices = new ArrayList<>();
    public static String singleChoice = "singleChoice";
    public static String multipleChoice = "multipleChoice";
    public static String trueOrFalse = "trueOrFalse";
    public static String textEntry = "textEntry";
    private List<SurveyAnswerSubmission> answerSubmissions = new ArrayList<>();  //问答题答案列表

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getMinWords() {
        return minWords;
    }

    public void setMinWords(int minWords) {
        this.minWords = minWords;
    }

    public int getMaxWords() {
        return maxWords;
    }

    public void setMaxWords(int maxWords) {
        this.maxWords = maxWords;
    }

    public int getMinChoose() {
        return minChoose;
    }

    public void setMinChoose(int minChoose) {
        this.minChoose = minChoose;
    }

    public int getMaxChoose() {
        return maxChoose;
    }

    public void setMaxChoose(int maxChoose) {
        this.maxChoose = maxChoose;
    }

    public List<MChoices> getmChoices() {
        return mChoices;
    }

    public void setmChoices(List<MChoices> mChoices) {
        this.mChoices = mChoices;
    }

    public static String getSingleChoice() {
        return singleChoice;
    }

    public static void setSingleChoice(String singleChoice) {
        SurveyAnswer.singleChoice = singleChoice;
    }

    public static String getMultipleChoice() {
        return multipleChoice;
    }

    public static void setMultipleChoice(String multipleChoice) {
        SurveyAnswer.multipleChoice = multipleChoice;
    }

    public static String getTrueOrFalse() {
        return trueOrFalse;
    }

    public static void setTrueOrFalse(String trueOrFalse) {
        SurveyAnswer.trueOrFalse = trueOrFalse;
    }

    public static String getTextEntry() {
        return textEntry;
    }

    public static void setTextEntry(String textEntry) {
        SurveyAnswer.textEntry = textEntry;
    }

    public List<SurveyAnswerSubmission> getAnswerSubmissions() {
        return answerSubmissions;
    }

    public void setAnswerSubmissions(List<SurveyAnswerSubmission> answerSubmissions) {
        this.answerSubmissions = answerSubmissions;
    }

    public static class MChoices implements Serializable {
        @Expose
        @SerializedName("id")
        private String id;
        @Expose
        @SerializedName("content")
        private String content;
        public static String choices[] = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
