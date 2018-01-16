package com.xin;

import java.util.ArrayList;

/**
 * Created by xiaoxin on 17-4-20.
 */

public class TestStaticFieldTest {


    public static String str = "ddddd";
    public static ArrayList<String> mActions;
    public static void main(String... args){
        if (mActions == null) {
            mActions = new ArrayList<String>();
        }

        for(String action : mActions) {
            System.out.println("action: "+action);
        }
        System.out.println("action: "+mActions+",  size: "+mActions.size());
    }
}
