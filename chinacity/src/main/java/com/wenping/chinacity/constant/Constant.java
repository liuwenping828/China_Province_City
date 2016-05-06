package com.wenping.chinacity.constant;

import android.os.Environment;


public interface Constant {

    public static final String CITY_NAME = "城市";//选择城市

    public static final String PACKAGE_NAME = "com.wenping.city"; //包名(引入该库需替换自己包名)

    public static final int resultCode = 10;// 返回结果码

    public static final String DB_NAME = "china_city.db"; //数据库名字

    public static final String DB_PATH = "/data" + Environment.getDataDirectory().getAbsolutePath() + "/" +
            Constant.PACKAGE_NAME;  //在手机里存放数据库的位置(/data/data/PACKAGE_NAME/china_city.db)

}
