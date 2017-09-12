package com.haoyu.app.entity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.haoyu.app.base.BaseResponseResult;

import java.util.HashMap;
import java.util.Map;

/**
 * 创建日期：2017/5/22 on 9:19
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class CourseRegistStateResultBase extends BaseResponseResult<CourseRegistStateResultBase.CourseRegistStateData> {

    /**
     * 创建日期：2017/5/19 on 16:19
     * 	responseData详细
     * 名称	说明	类型	必填	备注
     * registedCourseNum	已选课数	int	Y
     * registedStudyHours	已选总学时	int	Y
     * requireStudyHours	需要选的总学时	int	Y
     * topicCountMap	按主题选课详情	Map<String,TopicCount>	N	当trainCourseConfig为hastopic时返回此项，此项内容是当前人在每个主题下选了多少门课，多少学时
     * 	TopicCount详细
     * 名称	说明	类型	必填	备注
     * courseNum	已选课数	int	Y
     * studyHours	已选学时	int	Y
     * <p>
     * 作者:马飞奔 Administrator
     */
    public class CourseRegistStateData {
        @Expose
        @SerializedName("registedCourseNum")
        private int registedCourseNum;
        @Expose
        @SerializedName("registedStudyHours")
        private int registedStudyHours;
        @Expose
        @SerializedName("requireStudyHours")
        private int requireStudyHours;
        @Expose
        @SerializedName("topicCountMap")
        private Object topicCountMap;

        public int getRegistedCourseNum() {
            return registedCourseNum;
        }

        public void setRegistedCourseNum(int registedCourseNum) {
            this.registedCourseNum = registedCourseNum;
        }

        public int getRegistedStudyHours() {
            return registedStudyHours;
        }

        public void setRegistedStudyHours(int registedStudyHours) {
            this.registedStudyHours = registedStudyHours;
        }

        public int getRequireStudyHours() {
            return requireStudyHours;
        }

        public void setRequireStudyHours(int requireStudyHours) {
            this.requireStudyHours = requireStudyHours;
        }

        public Map<String, TopicCount> getTopicCountMap() {
            try {
                Gson gson = new GsonBuilder().create();
                return gson.fromJson(topicCountMap.toString(),
                        new TypeToken<Map<String,TopicCount>>(){}.getType());
            } catch (JsonSyntaxException e) {
                return new HashMap<>();
            }
        }

        public void setTopicCountMap(Object topicCountMap) {
            this.topicCountMap = topicCountMap;
        }

        public class TopicCount {
            @Expose
            @SerializedName("courseNum")
            private int courseNum;
            @Expose
            @SerializedName("studyHours")
            private int studyHours;

            public int getCourseNum() {
                return courseNum;
            }

            public void setCourseNum(int courseNum) {
                this.courseNum = courseNum;
            }

            public int getStudyHours() {
                return studyHours;
            }

            public void setStudyHours(int studyHours) {
                this.studyHours = studyHours;
            }
        }
    }
}
