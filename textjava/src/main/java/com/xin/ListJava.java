package com.xin;

import java.util.Scanner;

public class ListJava {

    private static Note head;
    private static Note next;

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            if (head == null) {
                head = new Note(i);
                next = head;
            } else {
                Note temp = new Note(i);
                next.next = temp;
                next = temp;
            }
        }
        printHead();
        System.out.println("-------------");
        //reverseHead(head, null);
        reverseHead(head);
        //head = UtilXX.reverseHead(head);
        printHead();

        System.out.println("-------------");
        /*double dou = Math.ceil(1.1);
        System.out.println("dou : "+dou);*/
    }

    public static void printHead() {
        Note out = head;
        while (out != null) {
            System.out.print(out.num + "   ");
            out = out.next;
        }
        System.out.println("");
    }

    //利用递归实现链表的翻转
    public static void reverseHead(Note cur, Note prev) {
        Note out1 = cur;
        Note out2 = cur.next;
        if (out2 == null) {
            head = cur;
            head.next = prev;
        } else {
            reverseHead(out2, out1); //递归，放在下一句之前
            cur.next = prev;
        }
    }

    //利用三参数实现链表翻转
    public static void reverseHead(Note in) {
        Note cur = null;
        Note nex = null;
        Note temp = null;
        if (in.next == null) {
            return;
        } else {
            cur = in;
            nex = in.next;
            cur.next = null;
        }

        while (nex.next != null) { //整体后移一位
            temp = nex;
            nex = nex.next;
            temp.next = cur;
            cur = temp;
        }
        nex.next = cur; //把最后的next加上
        head = nex;
    }
}

