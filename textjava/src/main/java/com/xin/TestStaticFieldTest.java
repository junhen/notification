package com.xin;

/**
 * Created by xiaoxin on 17-4-20.
 */

public class TestStaticFieldTest {


    public static String str = "ddddd";
    public static void main(String... args){
        int i = 10;
        TestStaticFieldParent parent = new TestStaticFieldParent();
        TestStaticFieldChild child = new TestStaticFieldChild();
        while (i > 0) {
            if(i % 2 ==0 ) {
                parent.staticInt = i--;
            } else {
                child.staticInt = i--;
            }
            System.out.println("TestStaticFieldParent.staticInt = " + TestStaticFieldParent.staticInt);
            System.out.println("TestStaticFieldChild.staticInt = " + TestStaticFieldChild.staticInt);
            System.out.println("parent.staticInt = " + parent.staticInt);
            System.out.println("child.staticInt = " + child.staticInt);
            Thread.dumpStack();
        }
    }
}
