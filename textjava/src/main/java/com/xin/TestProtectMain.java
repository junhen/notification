package com.xin;

/**
 * Created by xiaoxin on 17-9-26.
 */

public class TestProtectMain {
    public static void main(String[] args) {
        TestProtect mTp = new TestProtect();
        System.out.println(mTp.mName);
        mTp.setName();
        System.out.println(mTp.mName);
    }
}
