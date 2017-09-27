package com.haoyu.app.utils;

/**
 * 创建日期：2017/5/4 on 14:25
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class Action {
    /*通知公告模块*/
    public static String CREATE_ANNOUNCEMENT = "CREATE_ANNOUNCEMENT"; //创建通知公告
    public static String ALTER_ANNOUNCEMENT = "ALTER_ANNOUNCEMENT"; //修改通知公告
    public static String READ_ANNOUNCEMENT = "READ_ANNOUNCEMENT";  //查看通知公告
    public static String DELETE_ANNOUNCEMENT = "DELETE_ANNOUNCEMENT";  //删除通知公告
    /********************************************************/

    /*课程学习模块*/
    public static String CREATE_COURSE_DISCUSSION = "CREATE_COURSE_DISCUSSION";  //创建课程讨论
    public static String ALTER_COURSE_DISCUSSION = "ALTER_COURSE_DISCUSSION";  //修改课程讨论
    public static String DELETE_COURSE_DISCUSSION = "DELETE_COURSE_DISCUSSION";  //删除课程讨论
    public static String CREATE_FAQ_QUESTION = "CREATE_FAQ_QUESTION";  //创建课程问答
    public static String ALTER_FAQ_QUESTION = "ALTER_FAQ_QUESTION";  //修改课程问答
    public static String DELETE_FAQ_QUESTION = "DELETE_FAQ_QUESTION";  //删除课程问答
    public static String CREATE_FAQ_ANSWER = "ANSWER_COURSE_QUESTION";  //回答课程问题
    public static String ALTER_FAQ_ANSWER = "ALTER_COURSE_ANSWER";   //修改回答
    public static String DELETE_FAQ_ANSWER = "DELETE_COURSE_ANSWER";  //删除回答
    public static String CREATE_COURSE_NOTE = "CREATE_COURSE_NOTE";  //创建课程笔记
    public static String ALTER_COURSE_NOTE = "ALTER_COURSE_NOTE";  //修改课程笔记
    public static String DELETE_COURSE_NOTE = "DELETE_COURSE_NOTE";  //删除课程笔记
    public static String SUBMIT_COURSE_TEST = "SUBMIT_COURSE_TEST";  //提交测验
    public static String SUBMIT_COURSE_SURVEY = "SUBMIT_COURSE_SURVEY";  //提交问卷调查
    public static String GET_COURSE_ASSIGNMENT = "GET_COURSE_ASSIGNMENT";  //领取作业
    public static String SUBMIT_COURSE_ASSIGNMENT = "SUBMIT_COURSE_ASSIGNMENT";   //提交作业
    public static String RETURN_ASSIGNMENT_REDO = "RETURN_ASSIGNMENT_REDO"; //作业发回重做
    public static String READ_OVER_ASSIGNMENT = "READ_OVER_ASSIGNMENT";  //批阅作业
    public static String UPLOAD_RESOURCES = "UPLOAD_RESOURCES";  //上传课程资源
    public static String CLICK_PROGRESS = "CLICK_PROGRESS";   //点击进度条目回到章节学习
    public static String READ_COURSEWARE = "READ_COURSEWARE";   //阅读教学课件
    /***********************************************************/


    /*工作坊模块*/
    public static String ASSESS_MEMBER = "ASSESS_MEMBER";  //成员考核
    public static String CREATE_WORKSHOP_AT = "CREATE_WORKSHOP_AT";  //创建工作坊活动
    public static String CREATE_WORKSHOP_DISCUSSION = "CREATE_WORKSHOP_DISCUSSION";  //创建工作坊研讨
    public static String SUBMIT_WORKSHOP_LECE = "SUBMIT_WORKSHOP_LECE";  //提交听课评课表

    /***********************************************************/


    /*关注模块*/
    public static String COLLECTION = "COLLECTION";
//    public static String CREATE_COLLECTION = "CREATE_COLLECTION";  //创建关注
//    public static String CANCEL_COLLECTION = "CANCEL_COLLECTION";  //取消关注
    /***********************************************************/

    /*观点模块*/
    public static String CREATE_LIKE = "CREATE_LIKE";  //创建点赞
    /***********************************************************/


    /*研修简报*/
    public static String CREATE_BRIEF = "CREATE_BRIEF"; //创建研修简报
    public static String ALTER_BRIEF = "ALTER_BRIEF";   //修改研修简报
    public static String DELETE_BRIEF = "DELETE_BRIEF";     //删除研修简报
    /***********************************************************/

    /*教研模块*/
    public static String CREATE_STUDY_SAYS = "CREATE_STUDY_SAYS";  //创建研说
    public static String SUPPORT_STUDY_SAYS = "SUPPORT_STUDY_SAYS";  //研说点赞
    public static String SUPPORT_STUDY_CLASS = "SUPPORT_STUDY_CLASS";   //创课点赞
    public static String GIVE_STUDY_ADVICE = "GIVE_STUDY_ADVICE";  //创课提建议
    public static String CREATE_SUPPORT = "CREATE_SUPPORT";  //点赞（研说）
    public static String CREATE_GEN_CLASS = "CREATE_GEN_CLASS";  //创建创课
    public static String DELETE_STUDY_SAYS = "DELETE_STUDY_SAYS";  //删除研说
    public static String DELETE_GEN_CLASS = "DELETE_GEN_CLASS";  //删除创课
    public static String ALTER_GEN_CLASS = "ALTER_GEN_CLASS"; //修改创课
    public static String CREATE_MOVEMENT = "CREATE_MOVEMENT";  //创建活动
    public static String DELETE_MOVEMENT = "DELETE_MOVEMENT";  //删除活动
    public static String REGIST_MOVEMENT = "REGIST_MOVEMENT";  //报名活动
    public static String UNREGIST_MOVEMENT = "UNREGIST_MOVEMENT";
    /***********************************************************/

    /*回复*/
    public static String CREATE_MAIN_REPLY = "CREATE_MAIN_REPLY";    //创建主评论
    public static String CREATE_CHILD_REPLY = "CREATE_CHILD_REPLY";   //创建主评论下的子回复
    public static String DELETE_CHILD_REPLY = "DELETE_CHILD_REPLY";   //删除子回复
    public static String DELETE_MAIN_REPLY = "DELETE_MAIN_REPLY";   //删除主评论
    public static String DELETE_REPLY = "DELETE_REPLY";   //删除评论
    /***********************************************************/


    /*评论模块*/
    public static String CREATE_COMMENT = "CREATE_COMMENT";  //创建评论
    public static String CREATE_MAIN_COMMENT = "CREATE_MAIN_COMMENT";    //创建主回复
    public static String CREATE_CHILD_COMMENT = "CREATE_CHILD_COMMENT";   //创建主评论下的子评论
    public static String DELETE_CHILD_COMMENT = "DELETE_CHILD_COMMENT";   //删除子回复
    public static String DELETE_MAIN_COMMENT = "DELETE_MAIN_COMMENT";   //删除主评论
    public static String DELETE_COMMENT = "DELETE_REPLY";   //删除评论
    /***********************************************************/

    /*个人资料修改*/
    public static String CHANGE_USER_ICO = "CHANGE_USER_ICO";
    public static String CHANGE_USER_NAME = "CHANGE_USER_NAME";
    public static String CHANGE_DEPT_NAME = "CHANGE_DEPT_NAME";

    /*选课中心*/
    public static String SUBMIT_CHOOSE_COURSE = "SUBMIT_CHOOSE_COURSE";   //提交选课
    public static String CREATE_WORKSHOP = "CREATE_WORKSHOP";  //创建工作坊
}
