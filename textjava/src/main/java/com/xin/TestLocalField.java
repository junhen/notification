package com.xin;

/**
 * Created by xiaoxin on 17-7-11.
 */

public class TestLocalField {

    public static void main(String... args){
        Name one = new Name();
        one.name = 2;
        System.out.println(one);
        TestLocalField test = new TestLocalField();
        test.setName(one);
        System.out.println(one.name);
    }

    public void setName(Name two) {
        two = new Name();
        two.name = 3;
        System.out.println(two);
    }
}

class Name{
    public  int name = 0;
}
