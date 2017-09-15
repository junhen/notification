package com.xin;

import java.io.File;
import java.util.*;

import static java.util.Arrays.*;

/**
 * Created by xiaoxin on 17-8-2.
 */

public class TestStr {

    public static String str = "ddddd";

    public static String deleteStr(String st) {
        st = st.substring(1);
        return st;
    }

    static Ab ab = new Ab("hhhh");
    static Ab ac = new Ab("ffff");

    public static void main(String... args) {
        System.out.println("str = " + str);
        str = deleteStr(str);
        System.out.println("str = " + str);
        System.out.println("ab.a = " + ab.a);
        change(ab);
        System.out.println("ab.a = " + ab.a);
        System.out.println("File.separatorChar = " + File.separatorChar);

        int left = 5;
        int k = 9;
        k = ++left;
        System.out.println("k = " + k + ",  left = " + left);

        int[] aaa = {151, 117, 24, 23, 22, 18, 9, 3};
        int index = Arrays.binarySearch(aaa, 117);
        System.out.println("index = " + index);
        index = Arrays.binarySearch(aaa, 151);
        System.out.println("index = " + index);
        index = Arrays.binarySearch(aaa, 9);
        System.out.println("index = " + index);
        index = Arrays.binarySearch(aaa, 24);
        System.out.println("index = " + index);
        index = Arrays.binarySearch(aaa, 23);
        System.out.println("index = " + index);
        index = Arrays.binarySearch(aaa, 100);
        System.out.println("index = " + index);
        index = Arrays.binarySearch(aaa, 9);
        System.out.println("index = " + index);
        index = Arrays.binarySearch(aaa, 3);
        System.out.println("index = " + index);
        //sort(aaa, 0, aaa.length-1);
        Arrays.sort(aaa, 0, aaa.length);
        for (int i : aaa) {
            System.out.println(i);
        }
        index = Arrays.binarySearch(aaa, 117);
        System.out.println("index = " + index);
        index = Arrays.binarySearch(aaa, 151);
        System.out.println("index = " + index);
        index = Arrays.binarySearch(aaa, 9);
        System.out.println("index = " + index);
        index = Arrays.binarySearch(aaa, 24);
        System.out.println("index = " + index);
        index = Arrays.binarySearch(aaa, 23);
        System.out.println("index = " + index);
        index = Arrays.binarySearch(aaa, 100);
        System.out.println("index = " + index);
        System.out.println("index = " + index);
        index = Arrays.binarySearch(aaa, 3);
        System.out.println("index = " + index);
        int[] aaaaa = {11};
        System.out.println("hashcode = " + Arrays.hashCode(new int[]{11, 21, 36, 100}));
        ArrayList al = new ArrayList<Byte>();
        al.add(8);
        al.add('\u0001');
        al.add(5);
        al.add("afdadsfffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
        al.add(9);
        al.add(2);
        System.out.println("al = " + al);
        System.out.println("al 5 = " + al.get(5));
        al.remove(new Integer(5));
        System.out.println("al = " + al);
        /*System.out.println("al = "+ '\\97');
        System.out.println("al = "+ '\056');
        System.out.println("al = "+ "\u0097");*/
        Object[] all = new Object[7];
        Object[] alint = al.toArray(all);
        for (Object a : alint) {
            System.out.println("alint = " + a);
        }

        for (Object a : all) {
            System.out.println("all = " + a);
        }


        float mReceiverPtr = 0f;
        if (mReceiverPtr == 0f) {
            System.out.println("mReceiverPtr == 0" + ",  mReceiverPtr = " + mReceiverPtr);
        } else {
            System.out.println("mReceiverPtr != 0" + ",  mReceiverPtr = " + mReceiverPtr);
        }

    }

    public static void change(Ab aaa) {
        //aaa.a = "change";
        aaa = ac;
    }


    public static void sort(int[] a, int left, int right) {
        do {
            if (left >= right) {
                return;
            }
        } while (a[++left] >= a[left - 1]);

                /*
                 * Every element from adjoining part plays the role
                 * of sentinel, therefore this allows us to avoid the
                 * left range check on each iteration. Moreover, we use
                 * the more optimized algorithm, so called pair insertion
                 * sort, which is faster (in the context of Quicksort)
                 * than traditional implementation of insertion sort.
                 */
        for (int k = left; ++left <= right; k = ++left) {
            System.out.println("k = " + k + ",  left = " + left);
            int a1 = a[k], a2 = a[left];

            if (a1 < a2) {
                a2 = a1;
                a1 = a[left];
            }
            while (a1 < a[--k]) {
                a[k + 2] = a[k];
            }
            a[++k + 1] = a1;

            while (a2 < a[--k]) {
                a[k + 1] = a[k];
            }
            a[k + 1] = a2;
        }
        int last = a[right];

        while (last < a[--right]) {
            a[right + 1] = a[right];
        }
        a[right + 1] = last;
    }

}

class Ab {
    public String a;

    public Ab(String aa) {
        a = aa;
    }
}
