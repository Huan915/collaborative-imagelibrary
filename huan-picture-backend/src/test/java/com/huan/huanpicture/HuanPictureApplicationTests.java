package com.huan.huanpicture;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


class HuanPictureApplicationTests {

//    private static boolean flag = false;
//
//    @Test
//    void contextLoads() {
//
//        new Thread(() -> {
//            while (!flag) {
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//                System.out.println("flag is false");
//            }
//        }).start();
//
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//
//        flag = true;
//        System.out.println("flag is true");
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//
//    }
private static volatile boolean flag = false;

    public static void main(String[] args) {
        new Thread(() -> {
            while (!flag) {
                System.out.println("hello");
            }
            System.out.println("Thread terminated.");
        }).start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        flag = true; // 没有 volatile, 这个改变对其他线程不可见
    }


}
