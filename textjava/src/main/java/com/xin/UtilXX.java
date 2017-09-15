package com.xin;

/**
 * Created by xiaoxin on 17-5-12.
 */

public class UtilXX {
    //
    public static Note reverseHead(Note in) {
        Note one = null;
        Note two = null;
        Note temp = null;
        if (in.next == null) {
            return in;
        } else {
            one = in;
            two = in.next;
            one.next = null;
        }

        while (two.next != null) {
            temp = two;
            two = two.next;
            temp.next = one;
            one = temp;
        }
        two.next = one;
        return two;
    }
}

class Note {
    public Note next = null;
    public int num;

    public Note(int in) {
        num = in;
    }
}