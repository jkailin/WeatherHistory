package com.example.j.weatherhistory;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

/**
 * Created by j on 3/6/18.
 */

public class FileHelper {

    private static final int READ_BLOCK_SIZE = 100;
    private FileOutputStream outputStream;

    public FileHelper() {

    }

    public String readFile( Context context) {
        try {
            FileInputStream fileIn = context.openFileInput(context.getString(R.string.data_file));
            InputStreamReader InputRead = new InputStreamReader(fileIn);

            char[] inputBuffer = new char[READ_BLOCK_SIZE];
            String s = "";
            int charRead;

            while ((charRead = InputRead.read(inputBuffer)) > 0) {
                // char to string conversion
                String readstring = String.copyValueOf(inputBuffer, 0, charRead);
                s += readstring;
            }
            InputRead.close();
            return s;
        }
        catch (Exception e) {
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
