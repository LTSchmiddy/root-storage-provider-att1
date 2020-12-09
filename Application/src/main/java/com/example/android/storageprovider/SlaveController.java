package com.example.android.storageprovider;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.lang.Process;
import java.io.DataOutputStream;
import java.lang.ProcessBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

//import android.os.Process;

// The classes in rootslaves aren't invoked directly in the app process. Rather, they are
// alternative entry points to be invoked as sub-process calls by the root user. We don't have
// access to any Android APIs there, but the code will have root access to the system.

// Each entry point is effectively a different function that needs to be executed as root.
// Any information that must be returned will piped to stdout (via System.out.print[ln])

// This class serves as a wrapper for those entry points, to make them easy to call from inside the
// app and the storage-provider.

// execution command:
// su -c "CLASSPATH=/data/app/com.example.android.storageprovider.../base.apk /system/bin/app_process32 /system/bin com.example.android.storageprovider.rootslaves.[slaveName] optionalArgs"

public class SlaveController {

    public static String classpath_name = "com.example.android.storageprovider";
    public static String classpath_full = null;

//    public static void callBash(List<String> commands) {
//        ProcessBuilder pb = new ProcessBuilder();
//        pb.command(commands);
//
//    }

    public static InputStream callRootBash(String commandToExecute) {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream shell = new DataOutputStream(process.getOutputStream());
            shell.writeBytes(commandToExecute + "\n");
            shell.writeBytes("exit\n");
            shell.flush();
            process.waitFor();

            return process.getInputStream();
        } catch (IOException | InterruptedException e) {
            System.out.println(e);
            return null;
        }
    }

    public static BufferedReader callRootBashBuffered(String commandToExecute) {
        InputStream stream = callRootBash(commandToExecute);
        if (stream != null) {
            return new BufferedReader(new InputStreamReader(stream));
        }
        return null;
    }

    public static String determineClassPath() {
        try {
            BufferedReader ls_list = callRootBashBuffered("/sbin/.magisk/busybox/ls -w0 /data/app");
            ;
            assert ls_list != null;

            String line = null;
            while ((line = ls_list.readLine()) != null) {
                if (line.startsWith(classpath_name)) {
                    classpath_full = line;
                    return line;
                }
            }

            System.out.println("CLASSPATH NOT FOUND!!");
            return null;

        } catch (IOException e) {
            System.out.println(e);
            return null;
        }
    }

    public static String getSlaveCallCmd (String classpath, String slaveClass, String commands) {
        return "su -c \"CLASSPATH=/data/app/" + classpath + "/base.apk " +
                "/system/bin/app_process32 " +
                "/system/bin " +
                "com.example.android.storageprovider.rootslaves." + slaveClass + " " +
                commands + "\"";
    }

    public static InputStream callSlave(String slaveClass) throws IOException, InterruptedException {
        return callSlave(slaveClass, "");
    }

    public static InputStream callSlave(String slaveClass, String commands) throws IOException, InterruptedException {
        String classpath = determineClassPath();

        String cmdCall = getSlaveCallCmd(classpath, slaveClass, commands);

        return callRootBash(cmdCall);
    }

    public static BufferedReader callSlaveBuffered(String slaveClass) throws IOException, InterruptedException {
        return callSlaveBuffered(slaveClass, "");
    }

    public static BufferedReader callSlaveBuffered(String slaveClass, String commands) throws IOException, InterruptedException {
        InputStream stream = callSlave(slaveClass, commands);
        if (stream != null) {
            return new BufferedReader(new InputStreamReader(stream));
        }
        return null;
    }


}

