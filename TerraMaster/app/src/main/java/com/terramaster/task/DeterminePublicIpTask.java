package com.terramaster.task;

import android.content.Context;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DeterminePublicIpTask extends WorkerThread {
    public DeterminePublicIpTask(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }
    
    /*
     * For more alternet options for finding public ip, please refer:
     * http://stackoverflow.com/questions/6308000/determine-device-public-ip
     * 
     * @see com.terramaster.task.WorkerThread#onWorkInBackground()
     */

    @Override
    public String onWorkInBackground() {
        // TODO Auto-generated method stub
        URL url;
        BufferedReader bufferedReader = null;
        InputStreamReader in = null;
        HttpURLConnection urlConnection = null;
        try {
            url = new URL("http://ip2country.sourceforge.net/ip2c.php?format=JSON");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            in = new InputStreamReader(urlConnection.getInputStream());
            bufferedReader = new BufferedReader(in);
            String line;
            StringBuffer buffer = new StringBuffer();
            while ((line = bufferedReader.readLine()) != null) {
                buffer.append(line);
                buffer.append('\r');
            }
            bufferedReader.close();
            in.close();
            JSONObject json_data = new JSONObject(buffer.toString());
            String ip = json_data.getString("ip");
            return ip;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
