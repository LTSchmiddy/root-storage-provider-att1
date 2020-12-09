package com.example.android.storageprovider.rootslaves;

import android.os.Process;

// This class isn't invoked directly in the app process. Rather, it's an alternative entry point
// to be invoked as sub-process calls by the root user. We don't have access to any Android APIs
// here, but this code will have root access to the system.

// As we have a number of types of operations that need to run as root
public class SlaveTest {
    public static void main(String[] args) {
        System.out.println("uid of the current user = " + Process.myUid());

        System.out.println("My Args:");
        for (String i : args) {
            System.out.println(i);
        }

    }
}
