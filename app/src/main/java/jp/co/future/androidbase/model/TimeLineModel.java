package jp.co.future.androidbase.model;

import java.io.Serializable;

/**
 * Created by itaru on 2016/07/02.
 */
public class TimeLineModel {
    private String name;


    private String msg;
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
