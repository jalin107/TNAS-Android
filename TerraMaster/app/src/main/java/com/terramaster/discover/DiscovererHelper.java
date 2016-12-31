package com.terramaster.discover;

import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

public class DiscovererHelper {
    private static final int UDP_BCAST_PORT = 57057;// Tcloud 2.0 端口号
    private static final int UDP_BCAST_PORT2 = 14675;// Tcloud 1.7 端口号
    private static final int UDP_BCAST_PORT3 = 58058;// Tcloud 3.0 端口号
    private final static String UDP_BCAST_IPV4 = "255.255.255.255";
    private static final int UDP_BCAST_SEARCH_PORT = 57058;
    private final static String UDP_SEARCH_MESSAGE = "SEARCH Message:\r\n" + "ST:TerraMaster TCloud Device\r\n" + "TM:" + System.currentTimeMillis() + "\r\n";
    private static int UDP_BCAST_TIMEOUT = 1000 * 5;

    ArrayList<DeviceData> DeviceDatas = new ArrayList<DeviceData>();
    ArrayList<InterfaceSocket> interfaceSockets = new ArrayList<InterfaceSocket>();

    private Thread ReceiverThread = null;
    private ICCSearchReceiver Receiver = null;

    private Thread ReceiverThread_intel = null;
    private ICCSearchReceiver Receiver_intel = null;

    private Thread ReceiverThread_old = null;
    private ICCSearchReceiver Receiver_old = null;

    private WifiManager mWifi;

    private Receiver mReceiver;


    public DiscovererHelper(WifiManager wifi, Receiver receiver) {
        mWifi = wifi;
        mReceiver = receiver;
    }

    public void start() {

    }

    private void logMsg(String msg) {
        Log.e("discover", msg);
    }

    public void sendOldRequest(DatagramSocket datagramSocket) {// 发送1.7版本的广播报文，简单的字符
        logMsg("sendOldRequest"); // whc
        byte[] s = new byte[128];
        s[0] = 0x0001;
        s[2] = 0x0000;
        try {
            DatagramPacket spkg = new DatagramPacket(s, s.length, InetAddress.getByName(UDP_BCAST_IPV4), UDP_BCAST_PORT2);
            datagramSocket.send(spkg);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    public void PostSearchMessage() {
        ReceiverThread_intel.start();
        ReceiverThread.start();
        ReceiverThread_old.start(); // start thread
        // delay andsontan
//
//	PostMessage(UDP_SEARCH_MESSAGE, "", "", UDP_BCAST_PORT);// 2.0 广播包//
//								// ,包的内容和1.7不同，回应的内容也不同//
//								// whc
//	PostMessage("", "", "", UDP_BCAST_PORT2);// 1.7 广播包
    }

    class ICCSearchReceiver implements Runnable {// 查找 whc
        int port = 0;
        private volatile boolean stopRunning = false;

        ICCSearchReceiver(int port) {
            this.port = port;
            logMsg("" + port);
        }

        ;

        public void StopReceiver() {
            stopRunning = true;
        }

        public synchronized void InsertDeviceData(DeviceData dd) {
            Iterator<DeviceData> iter = DeviceDatas.iterator();
            // System.out.println("===================> "+dd.HOSTNAME+" , "+dd.BCAST_PORT+" <=====================");
            while (iter.hasNext()) {
                DeviceData odd = iter.next();
                if (odd.equals(dd))
                    return;
            }
            DeviceDatas.add(dd);// 将发现的设备加入 ArrayList
        }

        public void closeSocket(DatagramSocket ds) {
            ds.disconnect();
            ds.close();
            ds = null;
        }

        public void run() {
            DatagramSocket datagramSocket = null;
            try {
                datagramSocket = new DatagramSocket(null);
                datagramSocket.setReuseAddress(true);
                datagramSocket.bind(new InetSocketAddress(this.port));
                datagramSocket.setBroadcast(true);// 发送广播包给nas， whc
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
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    // System.out.println("debug:===>>>>" + e1.getMessage());
                    // System.out.println("####timeout="+UDP_BCAST_TIMEOUT+", port="
                    // +this.port);
                    if (this.port == UDP_BCAST_PORT2) {
                        sendOldRequest(datagramSocket);// 1.7
                        // 版本，如果receive超时，重新发送广播报文，
                        // whc
                    } else {
                        if (i > 0) {// andsontan
//			    PostMessage(UDP_SEARCH_MESSAGE, "", "", UDP_BCAST_PORT);
                        } else {
                            continue;
                        }
                        i--; // andsontan
                    }
                }
                DeviceData dd = null;
                if (this.port != UDP_BCAST_PORT2) {
                    String replyMessage = new String(rpkg.getData());
                    logMsg("replyMessage: " + this.port + " -- " + replyMessage);
                    // System.out.println("receive Data:" + replyMessage);
                    if (replyMessage.startsWith("NOTIFY") == false)
                        continue;// 2.0回应报文以NOTIFY开头 whc
                    if (replyMessage.indexOf("network") == -1)
                        continue;

                    String deviceIP = rpkg.getAddress().toString().substring(1);
                    int devicePORT = rpkg.getPort();
                    // filter ip... jalin
                    boolean my = false;
                    Iterator<InterfaceSocket> iter = interfaceSockets.iterator();
                    while (iter.hasNext()) {
                        InterfaceSocket idd = iter.next();
                        String ip = idd.iav4.toString().substring(1);
                        if (replyMessage.indexOf(ip) > 0)
                            my = true;
                    }
                    if (!my) {
                        continue;
                    }
                    System.out.println("Before insert DeviceData===>" + deviceIP + ":" + devicePORT + "===>" + replyMessage);
                    try {
                        dd = new DeviceData(this.port, replyMessage);// 解析replyMessage，得到DeviceData结构
                    } catch (Exception e) {
                        // whc
                    }
                } else {
                    if ((short) buf_old[0] == 1 && ((short) buf_old[2] == 0 || (short) buf_old[2] == 2)) {
                        continue;
                    }
                    if (buf_old[0] != 0x01 || buf_old[2] != 0x01) { // Not response packet
                        continue;
                    }
                    logMsg("replyMessage: " + this.port);
                    dd = new DeviceData(rpkg.getData(), rpkg.getLength());// 解析rpkg，得到1.7
                    // DeviceData结构
                }
                if (dd != null)
                    InsertDeviceData(dd); // synchronized
                rpkg = null;
            }
            closeSocket(datagramSocket);
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
