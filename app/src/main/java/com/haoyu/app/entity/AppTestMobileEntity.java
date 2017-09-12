package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2016/12/30 on 15:47
 * 描述:id	测验ID	String	Y
 * title	测验标题	String	Y
 * description	测验描述	String	Y
 * maxAttempts	允许提交最大次数	Int	Y	0表示不限制
 * mQuestions	题目列表	List	Y
 * <p/>
 * 作者:马飞奔 Administrator
 */
public class AppTestMobileEntity implements Serializable {
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("title")
    private String title;
    @Expose
    @SerializedName("description")
    private String description;
    @Expose
    @SerializedName("maxAttempts")
    private int maxAttempts;
    @Expose
    @SerializedName("mQuestions")
    private List<AppTestQuestion> mQuestions = new ArrayList<>();

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public List<AppTestQuestion> getmQuestions() {
        return mQuestions;
    }

    public void setmQuestions(List<AppTestQuestion> mQuestions) {
        this.mQuestions = mQuestions;
    }

    public static class AppTestQuestion implements Serializable {
        /**
         * id	题目ID	String	Y
         * quesType	测验类型	String	Y	SINGLE_CHOICE:单选题
         * MULTIPLE_CHOICE:多选题
         * TRUE_FALSE:是非题
         * itemKey	题目标示	String	Y	用于提交测验
         * title	标题	String	Y
         * score	分数	Double	Y
         * interactionOptions	选项列表	List	Y
         */
        public static String SINGLE_CHOICE = "SINGLE_CHOICE";
        public static String MULTIPLE_CHOICE = "MULTIPLE_CHOICE";
        public static String TRUE_FALSE = "TRUE_FALSE";
        @Expose
        @SerializedName("id")
        private String id;
        @Expose
        @SerializedName("quesType")
        private String quesType;
        @Expose
        @SerializedName("itemKey")
        private String itemKey;
        @Expose
        @SerializedName("title")
        private String title;
        @Expose
        @SerializedName("score")
        private double score;
        @Expose
        @SerializedName("interactionOptions")
        private List<InteractionOptions> interactionOptions = new ArrayList<>();

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getQuesType() {
            return quesType;
        }

        public void setQuesType(String quesType) {
            this.quesType = quesType;
        }

        public String getItemKey() {
            return itemKey;
        }

        public void setItemKey(String itemKey) {
            this.itemKey = itemKey;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }

        public List<InteractionOptions> getInteractionOptions() {
            return interactionOptions;
        }

        public void setInteractionOptions(List<InteractionOptions> interactionOptions) {
            this.interactionOptions = interactionOptions;
        }
    }

    public static class InteractionOptions implements Serializable {
        @Expose
        @SerializedName("id")
        private String id;
        @SerializedName("fixed")
        @Expose
        private boolean fixed;
        @Expose
        @SerializedName("text")
        private String text;
        private boolean check;
        public static String choices[] = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public boolean isFixed() {
            return fixed;
        }

        public void setFixed(boolean fixed) {
            this.fixed = fixed;
        }

        public boolean isCheck() {
            return check;
        }

        public void setCheck(boolean check) {
            this.check = check;
        }
    }

}
