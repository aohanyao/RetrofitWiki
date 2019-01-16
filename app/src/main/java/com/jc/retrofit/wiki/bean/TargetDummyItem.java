package com.jc.retrofit.wiki.bean;

/**
 * 实体类
 */
public class TargetDummyItem {
    public final String title;
    public final String des;
    public final Class targetActivity;

    public TargetDummyItem(String title, String des, Class targetActivity) {
        this.title = title;
        this.des = des;
        this.targetActivity = targetActivity;
    }

    @Override
    public String toString() {
        return des;
    }
}