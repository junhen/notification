package com.example;

import java.util.ArrayList;
import java.util.Scanner;

public class MyClass {

    public static void main(String... args) {
        Scanner scanner = new Scanner(System.in);
        ArrayList<Integer> as = new ArrayList<>();
        int i = 0;
        while (scanner.hasNext()){
            as.add(scanner.nextInt());
            System.out.println(as.get(i)+"  i = "+i);
            i++;
            if (i > 5) break;
        }
        int j = 0;
        for(int a : as){
            System.out.println(as.get(j++));
        }
        System.out.println("adsfadsfasdf");
    }
}
