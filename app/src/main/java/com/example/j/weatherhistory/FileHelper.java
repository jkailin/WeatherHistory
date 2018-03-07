package com.example.j.weatherhistory;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

/**
 * Created by j on 3/6/18.
 */

public class FileHelper {

    private FileOutputStream outputStream;

    public FileHelper() {

    }

    public String readFile( Context context) {
        try {
            FileInputStream file = context.openFileInput(context.getString(R.string.data_file));
            StringBuffer fileContent = new StringBuffer("");

            byte[] buffer = new byte[1024];
            int pos;

            while ((pos = file.read(buffer)) != -1) {
                fileContent.append(new String(buffer, 0, pos));
            }
            return fileContent.toString();
        }catch(Exception e) {
            return "no data available";
        }

    }

    public void writeFile(Context context, String data){
        try {
            // append mode stores in app private data
            outputStream = context.openFileOutput(context.getString(R.string.data_file), Context.MODE_PRIVATE|Context.MODE_APPEND);
            outputStream.write(data.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteFile(Context context) {
        context.deleteFile(context.getString(R.string.data_file));
    }



}
