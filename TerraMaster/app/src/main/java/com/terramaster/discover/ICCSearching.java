package com.terramaster.discover;

import android.net.wifi.WifiManager;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;

public class ICCSearching {

    private static final int UDP_BCAST_PORT = 57057;// Tcloud 2.0 端�?��?�
    private static final int UDP_BCAST_PORT3 = 58058;// Tcloud 3.0 端�?��?�
    private final static String UDP_BCAST_IPV4 = "255.255.255.255";
    private static final int UDP_BCAST_SEARCH_PORT = 57058;
    private final static String UDP_SEARCH_MESSAGE = "SEARCH Message:\r\n" + "ST:TerraMaster TCloud Device\r\n" + "TM:" + System.currentTimeMillis() + "\r\n";

    private final static String UDP_CONFIG_MESSAGE = "CONFIG Message:\r\n";

    private static int UDP_BUFF_SIZE = 1024;
    private static int UDP_BCAST_TIMEOUT = 500;

    ArrayList<DeviceData> DeviceDatas = new ArrayList<DeviceData>();
    ArrayList<InterfaceSocket> interfaceSockets = new ArrayList<InterfaceSocket>();

    private Thread ReceiverThread = null;
    private ICCSearchReceiver Receiver = null;

    private Thread ReceiverThread_intel = null;
    private ICCSearchReceiver Receiver_intel = null;

    private Receiver mReceiver;
    private int threadCount, totalThread;
    // TODO: Vary the challenge, or it's not much of a challenge :)
    // private static final String mChallenge = "myvoice";
    private WifiManager mWifi;

    public ICCSearching(WifiManager wifi, Receiver receiver) {
        //initialize interface
        mWifi = wifi;
        mReceiver = receiver;
        EnumerationInitialization();
        //create receive thread

        totalThread = 2;
        threadCount = 0;
        Receiver = new ICCSearchReceiver(UDP_BCAST_PORT);
        ReceiverThread = new Thread(Receiver);// 查找的线程，Tcloud
        // 2.0版本

        Receiver_intel = new ICCSearchReceiver(UDP_BCAST_PORT3);
        ReceiverThread_intel = new Thread(Receiver_intel);// 查找的线程，Tcloud
        // 3.0版本
    }

    private void logMsg(String msg) {
        Log.i("ICCSearching", msg);
    }

    private void onSerchingComplete() {
        threadCount++;
        logMsg("onSerchingComplete" + threadCount);
        if (threadCount >= totalThread) {
            threadCount = 0;
            mReceiver.addAnnouncedServers(DeviceDatas);
        }else{
            logMsg("onSerchingComplete else");
        }
    }

    private void EnumerationInitialization() {
        Enumeration<NetworkInterface> interfaces;
        NetworkInterface ni;
        try {
            // here we lookup interface
            interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                ni = (NetworkInterface) interfaces.nextElement();
                if (ni.isLoopback() == true)
                    continue;
                if (ni.isUp() == false)
                    continue;
                if (ni.isPointToPoint() == true)
                    continue;
                InterfaceSocket is = new InterfaceSocket(ni);

                if (is.iav4 == null) {
                    continue;
                }
                interfaceSockets.add(is);
            }
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void start() {
        PostSearchMessage();
    }

    public void PostMessage(String MessageHead, String MessageData, String Mesg, int port) {
        Iterator<InterfaceSocket> it = interfaceSockets.iterator();
        DatagramPacket datagramPacket = null;
        byte[] buf = new byte[1024];

        try {
            datagramPacket = new DatagramPacket(buf, buf.length, InetAddress.getByName(UDP_BCAST_IPV4), port);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }

        while (it.hasNext()) {
            InterfaceSocket is = it.next();
            DatagramSocket datagramSocket = null;
            try {
                datagramSocket = new DatagramSocket(null);
                datagramSocket.setReuseAddress(true);
                // 绑定本地网卡和端口
                datagramSocket.bind(new InetSocketAddress(is.iav4, UDP_BCAST_SEARCH_PORT));
                datagramSocket.setBroadcast(true);
                datagramSocket.setSoTimeout(UDP_BCAST_TIMEOUT);
            } catch (SocketException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return;
            }
            String MessageString = MessageHead + "IFC:" + is.iav4.toString().substring(1) + "\r\n" + "DAT:" + MessageData + "\r\n";
            datagramPacket.setData(MessageString.getBytes());
            try {
                for (int i = 0; i < 3; i++) {
                    datagramSocket.send(datagramPacket);
                }
                logMsg("post message on ifcccccccccccc:" + datagramSocket.getLocalSocketAddress().toString());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            datagramSocket.disconnect();
            datagramSocket.close();
        }
    }

    public void PostSearchMessage() {
        ReceiverThread_intel.start();
        ReceiverThread.start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                PostMessage(UDP_SEARCH_MESSAGE, "", "", UDP_BCAST_PORT);// 2.0
            }
        }).start();
    }

    public void PostConfigMessage(String ip, DeviceData DeviceData) {
        // TODO Auto-generated method stub
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("ip", ip);
            jsonObject.put("mask", DeviceData.IFCMASK);
            jsonObject.put("mac", DeviceData.MAC);
            PostMessage(UDP_CONFIG_MESSAGE, jsonObject.toString(), "", UDP_BCAST_PORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /******************************/
    // 冒泡排�?
    private ArrayList<DeviceData> ArraySort(ArrayList<DeviceData> List) {
        int number = List.size();
        if (number > 1) {
            for (int i = 0; i < number; i++) {
                for (int j = 0; j < number - 1; j++) {
                    String name1 = List.get(j).DEVICENAME;
                    String name2 = List.get(j + 1).DEVICENAME;
                    if (name1.compareTo(name2) > 0) {
                        DeviceData tmp = List.get(j);
                        List.set(j, List.get(j + 1));
                        List.set(j + 1, tmp);
                    }
                }
            }
        }
        return List;
    }

    public ArrayList<DeviceData> GetDeviceData() {
        // TODO Auto-generated method stub
        return this.ArraySort(DeviceDatas);
    }

    public void Destory(){
        // TODO Auto-generated method stub
        Receiver_intel.StopReceiver();
        Receiver.StopReceiver();
        try {
            ReceiverThread_intel.join();
            ReceiverThread.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Iterator<InterfaceSocket> it = interfaceSockets.iterator();
        while (it.hasNext()) {
            it.next().Destory();
        }

        this.interfaceSockets.clear();
        this.interfaceSockets = null;

        this.DeviceDatas.clear();
        this.DeviceDatas = null;
    }

    class ICCSearchReceiver implements Runnable {// 查找 whc
        int port = 0;
        private volatile boolean stopRunning = false;

        ICCSearchReceiver(int port) {
            this.port = port;
        }

        public void StopReceiver() {
            stopRunning = true;
        }

        public synchronized void InsertDeviceData(DeviceData dd) {
            Iterator<DeviceData> iter = DeviceDatas.iterator();
            while (iter.hasNext()) {
                DeviceData odd = iter.next();
                if (odd.equals(dd)) return;
            }
            DeviceDatas.add(dd);
        }

        public void closeSocket(DatagramSocket ds) {
            try {
                ds.disconnect();
                ds.close();
                ds = null;
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        public void run() {
            DatagramSocket datagramSocket = null;
            try {
                datagramSocket = new DatagramSocket(null);
                datagramSocket.setReuseAddress(true);
                datagramSocket.bind(new InetSocketAddress(this.port));
                datagramSocket.setBroadcast(true);
                // whc
                datagramSocket.setSoTimeout(UDP_BCAST_TIMEOUT * 2);
            } catch (SocketException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return;
            }
            logMsg("Thread start===>" + this.port);
            int i = 3;// andsontan
            while (!stopRunning) {
                byte[] buf = new byte[1024];
                byte[] buf_old = new byte[128];

                DatagramPacket rpkg = new DatagramPacket(buf, buf.length);
                try {
                    datagramSocket.receive(rpkg);
                } catch (SocketTimeoutException e) {
                    stopRunning = true;
                    logMsg("Receive timed out");
                    continue;
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    if (i > 0) {// andsontan
                        PostMessage(UDP_SEARCH_MESSAGE, "", "", UDP_BCAST_PORT);
                    } else {
                        continue;
                    }
                    i--; // andsontan
                }
                DeviceData dd = null;
                String replyMessage = new String(rpkg.getData());
                logMsg("replyMessage: " + this.port + " -- " + replyMessage);
                //logMsg("receive Data:" + replyMessage);
                if (replyMessage.startsWith("NOTIFY") == false) continue;// 2.0回应报文以NOTIFY开头 whc
                if (replyMessage.indexOf("network") == -1) continue;

                String deviceIP = rpkg.getAddress().toString().substring(1);
                int devicePORT = rpkg.getPort();
                // filter ip... jalin
                boolean my = false;
                Iterator<InterfaceSocket> iter = interfaceSockets.iterator();
                while (iter.hasNext()) {
                    InterfaceSocket idd = iter.next();
                    String ip = idd.iav4.toString().substring(1);
                    if (replyMessage.indexOf(ip) > 0) my = true;
                }
                if (!my) continue;
                logMsg("Before insert DeviceData===>" + deviceIP + ":" + devicePORT + "===>" + replyMessage);
                try {
                    dd = new DeviceData(this.port, replyMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                if (dd != null) InsertDeviceData(dd); //synchronized
                rpkg = null;
            }
            closeSocket(datagramSocket);
            onSerchingComplete();
        }
    }

    class InterfaceSocket {
        public NetworkInterface networkInterface = null;
        public InetAddress iav4 = null;
        public InetAddress iav6 = null;

        public InterfaceSocket(NetworkInterface networkInterface) {
            // set interface
            this.networkInterface = networkInterface;
            // initialize ipv4 and ipv6 address string
            Enumeration<InetAddress> inetaddrs = networkInterface.getInetAddresses();
            while (inetaddrs.hasMoreElements()) {
                InetAddress iaddr = (InetAddress) inetaddrs.nextElement();

                if (iaddr instanceof Inet6Address) {
                    this.iav6 = iaddr;
                }
                if (iaddr instanceof Inet4Address) {
                    this.iav4 = iaddr;
                }
            }

        }

        public void Destory() {
            // TODO Auto-generated method stub
            iav4 = iav6 = null;
            networkInterface = null;
        }
    }
}
