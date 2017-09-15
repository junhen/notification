package com.xin;

/**
 * Created by xiaoxin on 17-4-20.
 */

public class TestStaticFieldChild extends TestStaticFieldParent {

    public static void main(String... args){
        int i = 10;
        while (i > 0) {
            staticInt = i--;
            System.out.println("staticInt = " + staticInt);
        }
    }
}
