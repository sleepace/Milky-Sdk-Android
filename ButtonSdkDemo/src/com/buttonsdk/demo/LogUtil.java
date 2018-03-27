package com.buttonsdk.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;
import android.util.Log;

public class LogUtil {

    public static boolean logEnable = true;
    public static final String TAG = "SleepaceSdk";
	public static final String LOG_DIR = Environment.getExternalStorageDirectory()+"/medica/SDKLog";

    private static final String TIME = new SimpleDateFormat("yyyy_MM_dd").format(new Date());

    public static void log(Object obj) {
        if (!logEnable) return;
        
        String log = obj.toString();
        Log.e(TAG, log);

        //String filename = "yong_" + TIME + ".log";
        //saveLog(filename, log);
    }


    public static boolean logTemp(String log) {
        if (!logEnable) return false;
        String filename = "load_data_" + TIME + ".log";
        return saveLog(filename, log);
    }


    public static boolean saveLog(String filename, String log) {
        if (!logEnable) return false;
        try {
            File dir = new File(LOG_DIR);
            if (!dir.exists()) {
                boolean res = dir.mkdirs();
                if (!res) {
                    return false;
                }
            }
            File file = new File(dir, filename);
            FileOutputStream fos = new FileOutputStream(file, true);

            String time = System.currentTimeMillis()+"";
            log = "保存Log： " + time + "          " + log;
            fos.write(log.getBytes());
            fos.write("\r\n".getBytes());
            fos.flush();
            fos.close();
            return true;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    public static String getCaller(){
        StringBuilder sb = new StringBuilder();
        Throwable ex = new Throwable();
        StackTraceElement[] stackElements = ex.getStackTrace();
        if (stackElements != null) {
            for (int i = 0; i < stackElements.length; i++) {
                sb.append(stackElements[i].getClassName()+".");
//                sb.append(stackElements[i].getFileName()+"---");
                sb.append(stackElements[i].getMethodName()+":");
                sb.append(stackElements[i].getLineNumber()+"\n");
            }
        }
        return sb.toString();


    }
}

