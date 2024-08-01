package com.example.ble.Helper;

import android.content.Context;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CSVUtils {

    public static List<float[]> readCSV(Context context, String fileName) {
        List<float[]> data = new ArrayList<>();
        try {
            InputStream is = context.getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                float[] values = new float[tokens.length];
                for (int i = 0; i < tokens.length; i++) {
                    values[i] = Float.parseFloat(tokens[i]);
                }
                data.add(values);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }
}
