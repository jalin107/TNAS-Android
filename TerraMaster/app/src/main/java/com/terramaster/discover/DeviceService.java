package com.terramaster.discover;

import android.util.Log;

public class DeviceService {
    public String name;
    public String url;
    public int port;

    public DeviceService(String name, String url, int port) {
        // TODO Auto-generated constructor stub
        this.name = name;
        Log.e("tgm",name);
        this.url = url;
        if(name.equals("ftp"))
        {
            if(port==0) port = 21;
        }
        this.port = port;
    }


}
