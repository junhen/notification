package com.xin;

/**
 * Created by xiaoxin on 17-9-26.
 */

public class TestProtectMain extends Main{
    private static final String TAG1 = Main.class.getName();
    private static final String TAG2 = Main.class.getSimpleName();

    public static void main(String[] args) {
        TestProtect mTp = new TestProtect();
        System.out.println(mTp.mName);
        mTp.setName();
        System.out.println(mTp.mName);
        System.out.println();
        String a = "abc";
        String b = "abc";
        String c = new StringBuilder().append("abc").toString();
        //System.out.println("'abc == abc' = "+(a .equals(c) ));
        System.out.println("a == 'abc' = "+(a == "abc" ));
        int[] aaaa = new int[3];
        System.out.println("aaaa   "+aaaa[2]);
        System.out.println("getName ----- " + TAG1 + "\n" + "getSimpleName ----- " + TAG2);
        Main main = new SubMain();
        main.printName();
    }
}
class SubMain extends Main{

}
class Main {
    public void printName() {
        String name = getClass().getName();
        String simpleName = getClass().getSimpleName();
        System.out.println("name ----- " + name + "\n" + "simpleName ----- " + simpleName);
    }
}
