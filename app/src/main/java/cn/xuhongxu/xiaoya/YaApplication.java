package cn.xuhongxu.xiaoya;

import android.app.Application;
import android.content.Intent;

import java.util.ArrayList;

import cn.xuhongxu.Assist.EvaluationCourse;
import cn.xuhongxu.Assist.EvaluationItem;
import cn.xuhongxu.Assist.SchoolworkAssist;
import cn.xuhongxu.xiaoya.Activity.ErrorActivity;

/**
 * Created by xuhongxu on 16/4/12.
 * <p/>
 * YaApplication
 *
 * @author Hongxu Xu
 * @version 0.1
 */
public class YaApplication extends Application {

    private SchoolworkAssist assist;
    private ArrayList<EvaluationItem> evaluationItemList;
    private ArrayList<EvaluationCourse> evaluationCourses;

    @Override
    public void onCreate() {
        super.onCreate();

        evaluationItemList = new ArrayList<>();
        evaluationCourses = new ArrayList<>();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                handleUncaughtException(thread, e);
            }
        });

    }

    private void handleUncaughtException(Thread thread, Throwable e) {
        e.printStackTrace();
        Intent intent = new Intent (getApplicationContext(),ErrorActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        String stackTraceString = "";
        for(StackTraceElement trace : e.getStackTrace()) {
            stackTraceString += "\n" + trace.toString();
        }
        stackTraceString += "\n\nCause:\n" + e.getCause().getMessage() + "\n";
        for(StackTraceElement trace : e.getCause().getStackTrace()) {
            stackTraceString += "\n" + trace.toString();
        }
        intent.putExtra("Error", e.getMessage() + "\n" + stackTraceString);
        startActivity(intent);
        System.exit(1);
    }

    public SchoolworkAssist getAssist() {
        return assist;
    }

    public void setAssist(SchoolworkAssist assist) {
        this.assist = assist;
    }

    public ArrayList<EvaluationItem> getEvaluationItemList() {
        return evaluationItemList;
    }

    public void setEvaluationItemList(ArrayList<EvaluationItem> evaluationItemList) {
        this.evaluationItemList = evaluationItemList;
    }

    public ArrayList<EvaluationCourse> getEvaluationCourses() {
        return evaluationCourses;
    }

    public void setEvaluationCourses(ArrayList<EvaluationCourse> evaluationCourses) {
        this.evaluationCourses = evaluationCourses;
    }
}
