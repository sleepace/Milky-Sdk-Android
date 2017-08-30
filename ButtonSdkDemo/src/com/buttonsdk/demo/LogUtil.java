package com.buttonsdk.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;
import android.util.Log;

public class LogUtil {

    public static boolean logEnable = true;// = (SleepConfig.PACKAGE_ENVIRONMENT != 3);
    public static final String TAG = "load_test";
	public static final String LOG_DIR = Environment.getExternalStorageDirectory()+"/medica/SDKLog";

    /**
     * 是否让tag固定不变
     */
    public static boolean isTagConstant = false;

    private static final String TIME = new SimpleDateFormat("yyyy_MM_dd").format(new Date());

    public static void log(Object obj) {
        if (!logEnable) return;

        if (obj == null) {
            obj = "Objec is null";
        }

        String log = obj.toString();
        Log.e(TAG, log);

        //String filename = "yong_" + TIME + ".log";
        //saveLog(filename, log);
    }


    public static void logE(Object msg) {
        if (!logEnable) return;
//        int size = 150;
//        for (int i = 0; i <= msg.toString().length() / size; i++) {
//            int start = i * size;
//            int end = (i + 1) * size;
//            end = end >= msg.toString().length() ? msg.toString().length() : end;
//            Log.e("Hao", msg.toString().substring(start, end));
//        }
        Log.e("Hao", msg.toString());
    }


    /**
     * 打印bytebuffer
     *
     * @param buffer
     */
    public static void logByteBuffer(ByteBuffer buffer) {
        if (!logEnable) return;
        try {
            buffer.position(0);
            String str = "";
            for (int i = 0; i < buffer.limit(); i++) {
                str += String.format("%x ", buffer.get() & 0xff);
            }
            buffer.position(0);
            logE("ByteBuffer内容：" + str);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
            logE(log);
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


    public static void e(String tag, String msg) {
        if (!logEnable) {
            return;
        }

        if (isTagConstant) {
            Log.e(TAG, tag + "   " + msg);
        } else {
            Log.e(tag, msg);
        }
    }

    public static void eThrowable(String tag, String msg) {
        if (!logEnable) {
            return;
        }

        if (isTagConstant) {
            Log.e(TAG, tag + "   " + msg,new Throwable());
        } else {
            Log.e(tag, msg,new Throwable());
        }
    }

    public static void w(String tag, String msg) {

        if (!logEnable) {
            return;
        }

        if (isTagConstant) {
            Log.w(TAG, tag + "   " + msg);
        } else {
            Log.w(tag, msg);
        }
    }

    /**
     * 以级别为 d 的形式输出LOG
     */
    public static void d(String msg) {
//        if (!logEnable) return;
        Log.e("HZL", msg);
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

