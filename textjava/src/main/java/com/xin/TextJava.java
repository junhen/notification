package com.xin;

import java.util.Scanner;

public class TextJava {

    public static void fn(int[] a)  // Java 中基本类型有引用传值吗？ 若没有的话，形参的 final int a 这种形式有什么意义？
    {
        //Sec sec = new Sec(a);
        //Sec sec1 = new Sec(c);
        //System.out.println(sec.mIn[0]);
        b = null;
        System.out.println(a[0]);
        /*c[0] = 2;
        System.out.println(sec1.get()[0]);
        System.out.println(sec1.get());*/
    }

    public static void main(String[] args) {

        /*for (int i = 0; i < 10; i++) {
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
        head = UtilXX.reverseHead(head);
        printHead();

        System.out.println("-------------");*/
        double dou = Math.ceil(1.1);

        System.out.println("dou : "+dou);
    }

    public static void printHead() {
        Note out = head;
        while (out != null) {
            System.out.println(out.num);
            out = out.next;
        }
    }

    //利用递归实现链表的翻转
    public static void reverseHead(Note in, Note last) {
        Note out1 = in;
        Note out2 = out1.next;
        if (out2 == null) {
            head = in;
            head.next = last;
        } else {
            reverseHead(out2, out1);
            in.next = last;
        }
    }

    //利用三参数实现链表翻转
    public static void reverseHead(Note in) {
        Note one = null;
        Note two = null;
        Note temp = null;
        if (in.next == null) {
            return;
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
        head = two;
    }

    private static Note head;
    private static Note next;

    public static int[] b = {0};

    public static int[] c = {1};

    public static void quickSort(int[] numbers, int start, int end) {
        if (start < end) {
            int base = numbers[start]; // 选定的基准值（第一个数值作为基准值）
            int temp; // 记录临时中间值
            int i = start, j = end;
            do {
                while ((numbers[i] < base) && (i < end))
                    i++;
                while ((numbers[j] > base) && (j > start))
                    j--;
                if (i <= j) {
                    swap(numbers, i, j);
                    i++;
                    j--;
                }
            } while (i <= j);
            if (start < j)
                quickSort(numbers, start, j);
            if (end > i)
                quickSort(numbers, i, end);
        }
    }

    public static void swap(int[] data, int i, int j) {
        if (i == j) {
            return;
        }
        data[i] = data[i] + data[j];
        data[j] = data[i] - data[j];
        data[i] = data[i] - data[j];
    }

    private static final class Sec {
        private int[] mIn;

        public Sec(final int[] in) {
            mIn = in;
        }

        public int[] get() {
            int[] result = mIn;
            mIn = null;
            return result;
        }
    }
}

