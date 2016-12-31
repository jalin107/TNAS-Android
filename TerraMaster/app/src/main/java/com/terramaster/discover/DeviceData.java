package com.terramaster.discover;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

public class DeviceData {
    public String jsonData;
    public String PHPSESSID = "";
    public String HOSTNAME;
    public String INTERFACE;
    public String FIRMWARE;
    public String PWD = null;
    public String IPV4;
    public String MASK;
    public String MAC;
    public String DEVICENAME;
    public String WORKGROUP;

    public int BCAST_PORT;

    public boolean Linked = false;

    public NetworkInterface NetIFC;
    public String IFCIPV4;
    public String IFCMASK;

    public ArrayList<DeviceNetwork> Networks = new ArrayList<DeviceNetwork>();
    public ArrayList<DeviceService> Services = new ArrayList<DeviceService>();

    public DeviceData(){}
    public DeviceData(int Port, String replyString) throws JSONException {
        this.jsonData = replyString;
        this.BCAST_PORT = Port;
        String JsonString = GetMessageValue(replyString, "DAT");
        IFCIPV4 = GetMessageValue(replyString, "IFC");
        PWD = GetMessageValue(replyString, "PWD");
        NetIFC = TinyFunction.GetInterfaceByIPv4(IFCIPV4);
        IFCMASK = TinyFunction.MaskIPv4(NetIFC);

        DecodeByJsonString(JsonString.getBytes(), JsonString.length());// 解析Json字符串，获取DeviceData结构
        try {
            Linked = TinyFunction.SameNetwork(this.IFCIPV4, this.IPV4, this.IFCMASK);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public DeviceData(byte[] buf, int length) {// 解析1.7的回应报文，为 了和2.0兼容，保持DeviceData结构一致

        this.BCAST_PORT = 14675;
        this.IPV4 = TinyFunction.byte2ip(buf, 4);
        this.HOSTNAME = new String(buf, 8, 16).trim();
        this.WORKGROUP = new String(buf, 24, 16).trim();
        byte[] macbytes = new byte[6];
        TinyFunction.memcpy(buf, macbytes, 66, 0, 6);
        this.MAC = TinyFunction.bytes2hex(macbytes);

        /************/
        Enumeration<NetworkInterface> allNetInterfaces = null;
        try {
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        InetAddress ip = null;
        while (allNetInterfaces.hasMoreElements()) {
            NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
            // System.out.println(netInterface.getName());
            Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                ip = (InetAddress) addresses.nextElement();
                // System.out.println("local IP = " + ip.getHostAddress());

                if (ip instanceof Inet6Address) {
                    continue;
                }

                if ("127.0.0.1".equals(ip.getHostAddress())) {
                    // System.out.println("ccccccc");
                    continue;
                }
                if (ip != null && ip instanceof Inet4Address) {
                    this.IFCIPV4 = ip.getHostAddress();
                }
            }
        }

        this.NetIFC = TinyFunction.GetInterfaceByIPv4(this.IFCIPV4);
        this.IFCMASK = TinyFunction.MaskIPv4(this.NetIFC);

        System.out.println("===================" + this.IFCIPV4 + "=====================" + this.NetIFC);
        try {
            Linked = TinyFunction.SameNetwork(this.IFCIPV4, this.IPV4, this.IFCMASK);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // System.out.println("*********************************************" );
        System.out.println("\r\nversion1.7\r\nIFC:" + this.IFCIPV4 + " IFCMASK:" + this.IFCMASK + " LINK:" + this.Linked);
        // IFC:10.18.13.17
        // DAT:[{"hostname":"alanyang","firmware":"TOS_S1.0_V2.502\n"},
        // {"network":"eth0","ip":"10.18.13.8","mask":"255.255.255.0","mac":"00:50:43:EC:97:72"},
        // {"service":[{"name":"ftp","url":"","port":21},
        // {"name":"dav","url":"webdav","port":8800},
        // {"name":"sys","url":"","port":8012},
        // {"name":"don","url":"pt","port":9091},
        // {"name":"smb","url":"","port":0}]}]
        Services.add(new DeviceService("don", "pt", TinyFunction.htons(buf, 72)));
        Services.add(new DeviceService("dav", "webdav", TinyFunction.htons(buf, 74)));
        Services.add(new DeviceService("sys", "", TinyFunction.htons(buf, 78)));
        Services.add(new DeviceService("smb", "", 0));
        String module;
        module = new String(buf, 56, 8).trim();
        if (module.equals("1185")) {
            Services.add(new DeviceService("ftp", "", TinyFunction.htons(buf, 64)));
        } else {
            Services.add(new DeviceService("ftp", "", TinyFunction.htons(buf, 76)));
        }
        DeviceNetwork deviceNetwork = new DeviceNetwork();
        deviceNetwork.name = "eth0";// 实际上无法获取，默认和设置eth0 whc
        deviceNetwork.IPv4 = this.IPV4;
        deviceNetwork.mask = this.IFCMASK;// 实际上无法获取，默认和设置为和客户端的mask一样 whc
        deviceNetwork.MAC = this.MAC;

        Networks.add(deviceNetwork);

        Iterator<DeviceNetwork> it = Networks.iterator();

        while (it.hasNext()) {
            DeviceNetwork dn = it.next();
            if (dn.IPv4.equals(IPV4)) {
                MAC = dn.MAC != null ? dn.MAC : "";
                MASK = dn.mask != null ? dn.mask : "";
                break;
            }
        }

        it = null;

    }

    public boolean equals(DeviceData deviceData) {
        if (!this.IPV4.equals(deviceData.IPV4))
            return false;
        if (!this.MAC.equals(deviceData.MAC))
            return false;
        return true;
    }

    private void DecodeByJsonString(byte[] buf, int length) throws JSONException {// 解析json
        String jsonString = null;
        try {
            jsonString = new String(buf, "UTF-8").trim();
            if (jsonString.isEmpty()) return;
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        JSONArray jsonArray = new JSONArray(jsonString);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            if (jsonObject.has("hostname")) {
                // set global various
                this.HOSTNAME = jsonObject.getString("hostname");
                this.FIRMWARE = jsonObject.getString("firmware");
            } else if (jsonObject.has("network")) {
                // set device interface
                DeviceNetwork deviceNetwork = new DeviceNetwork();
                deviceNetwork.name = jsonObject.getString("network");
                deviceNetwork.IPv4 = jsonObject.getString("ip");
                deviceNetwork.mask = jsonObject.getString("mask");
                deviceNetwork.MAC = jsonObject.getString("mac");

                // set global various
                this.IPV4 = deviceNetwork.IPv4;
                this.MASK = deviceNetwork.mask;
                this.MAC = deviceNetwork.MAC;
                this.INTERFACE = deviceNetwork.name;

                String name = INTERFACE.substring(0, INTERFACE.length() - 1);
                String last = INTERFACE.substring(INTERFACE.length() - 1);
                if (name.equals("eth")) name = "lan";
                this.DEVICENAME = HOSTNAME + " (" + name.toUpperCase() + " " + (Integer.parseInt(last) + 1) + ")";

                Networks.add(deviceNetwork);
            } else if (jsonObject.has("service")) {
                // set service
                JSONArray serviceArray = jsonObject.getJSONArray("service");
                for (int j = 0; j < serviceArray.length(); j++) {
                    JSONObject service = serviceArray.getJSONObject(j);
                    Services.add(new DeviceService(service.getString("name"), service.getString("url"), service.getInt("port")));
                }
            }
        }
    }

    public String GetMessageValue(String Message, String Key) {
        if (Message == null)
            return null;
        String[] MsgArray = Message.replace("\r","").split("\n");

        for (int i = 0; i < MsgArray.length; i++) {
            if (MsgArray[i].startsWith(Key)) {
                int SplitPos = MsgArray[i].indexOf(':');
                return MsgArray[i].substring(SplitPos + 1);
            }
        }

        return null;
    }

    public String debug() {
        String result = "[ip:" + IPV4 + "][" + HOSTNAME + "]";

        Iterator<DeviceService> it = Services.iterator();
        while (it.hasNext()) {
            DeviceService ds = it.next();
            result += "[" + ds.name + ":" + ds.port + "]";
        }

        return result;
    }

    public String toString() {
        return DEVICENAME;
    }

    public void addDeviceService(String name, String url, int port) {
        Services.add(new DeviceService(name, url, port));
    }

    public ArrayList<DeviceService> getDeviceServiceList() {
        return this.Services;
    }

    // Open service manage web 打开web
    public void OpenServiceWeb(String service_name) {
        Iterator<DeviceService> it = Services.iterator();
        String service_url_path = null;

        while (it.hasNext()) {
            DeviceService ds = it.next();
            if (service_name.equalsIgnoreCase(ds.name)) {
                service_url_path = new String("http://" + IPV4 + ":" + ds.port + "/" + ds.url);

                System.out.println("open service:" + service_name);

//		TinyFunction.openurl(service_url_path);

                break;
            }
        }
    }

    public void OpenService(String service_name) {
        Iterator<DeviceService> it = Services.iterator();
        String service_url_path = null;

        while (it.hasNext()) {
            DeviceService ds = it.next();
            System.out.println("servie:" + ds.name);
            if (service_name.equals(ds.name)) {
                service_url_path = new String("http://" + IPV4 + ":" + ds.port + "/" + ds.url);

                System.out.println("open service:" + service_name);

                if (service_name.equals("smb")) {
                    TinyFunction.openshare(IPV4);
                } else if (service_name.equals("ftp")) {
                    TinyFunction.explore("ftp://" + IPV4 + ":" + ds.port);
                } else {
//		    TinyFunction.openurl(service_url_path);
                }

                break;
            }
        }
    }

    public DeviceService getDeviceService(String service_name) {
        // TODO Auto-generated method stub
        Iterator<DeviceService> it = Services.iterator();
        while (it.hasNext()) {
            DeviceService ds = it.next();
            if (service_name.equals(ds.name)) {
                return ds;
            }
        }
        return null;
    }

    public String getMac() {
        // TODO Auto-generated method stub
        if (MAC == null) {
            return "";
        }
        return MAC;
    }
}
