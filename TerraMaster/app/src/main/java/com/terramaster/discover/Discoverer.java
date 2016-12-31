package com.terramaster.discover;

import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Code for dealing with Boxee server discovery. This class tries to send a
 * broadcast UDP packet over your wifi network to discover the boxee service.
 */

public class Discoverer extends Thread {
    private static final String TAG = "Discovery";
    private static final int UDP_DISCOVERY_PORT = 57057;
    private final static String UDP_BCAST_IPV4 = "255.255.255.255";
    private static final int TIMEOUT_MS = 1000 * 5;

    private final static String UDP_SEARCH_MESSAGE = "SEARCH Message:\r\n" + "ST:TerraMaster TCloud Device\r\n" + "TM:" + System.currentTimeMillis() + "\r\n";

    private Receiver mReceiver;

    // TODO: Vary the challenge, or it's not much of a challenge :)
    // private static final String mChallenge = "myvoice";
    private WifiManager mWifi;

//    private StringBuilder sb = new StringBuilder();


    public Discoverer(WifiManager wifi, Receiver receiver) {
        mWifi = wifi;
        mReceiver = receiver;
    }

    /**
     * Get IP address from first non-localhost interface
     *
     * @param ipv4 true=return ipv4, false=return ipv6
     * @return address or empty string
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
        } // for now eat exceptions
        return "";
    }

    public void run() {
//	sb = new StringBuilder();
//	sb.append("Start discover...");
        ArrayList<DeviceData> deviceDatas = null;
        try {
            DatagramSocket socket = new DatagramSocket(UDP_DISCOVERY_PORT);
            socket.setBroadcast(true);
            socket.setSoTimeout(TIMEOUT_MS);

            sendDiscoveryRequest(socket);
            deviceDatas = listenForResponses(socket);
            socket.close();
        } catch (IOException e) {
            deviceDatas = new ArrayList<DeviceData>(); // use an empty one
            Log.e(TAG, "Could not send discovery request", e);
        }
//	sb.append("\n\nStop device searching");
//	try
//	{
//	    File folder = new File(Environment.getExternalStorageDirectory(), "Terra-Log");
//	    if (!folder.isDirectory())
//	    {
//		folder.mkdir();
//	    }
//	    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
//	    File myFile = new File(folder, "Terra-Discover-" + sdf.format(new Date()) + ".txt");
//	    myFile.createNewFile();
//	    FileOutputStream fOut = new FileOutputStream(myFile);
//	    OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
//	    myOutWriter.append(sb.toString());
//	    myOutWriter.close();
//	    fOut.close();
//	} catch (Exception e)
//	{
//	    e.printStackTrace();
//	}
        mReceiver.addAnnouncedServers(deviceDatas);
    }

    /**
     * Send a broadcast UDP packet containing a request for boxee services to
     * announce themselves.
     *
     * @throws IOException
     */
    private void sendDiscoveryRequest(DatagramSocket socket) throws IOException {
        // String data = String
        // .format("<bdp1 cmd=\"discover\" application=\"iphone_remote\" challenge=\"%s\" signature=\"%s\"/>",
        // mChallenge, getSignature(mChallenge));

        InetAddress is = getBroadcastAddress();
        String ip = getIPAddress(true);
        String data = UDP_SEARCH_MESSAGE + "IFC:" + ip + "\r\n" + "DAT:";// + UDP_SEARCH_MESSAGE + "\r\n";

        Log.d(TAG, "Sending data " + data);
        DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), is, UDP_DISCOVERY_PORT);
        socket.send(packet);

//	sb.append("\n\nPacket sending... " + data);
    }

    /**
     * Calculate the broadcast IP we need to send the packet along. If we send
     * it to 255.255.255.255, it never gets sent. I guess this has something to
     * do with the mobile network not wanting to do broadcast.
     */
    private InetAddress getBroadcastAddress() throws IOException {
        DhcpInfo dhcp = mWifi.getDhcpInfo();
        if (dhcp == null) {
            Log.d(TAG, "Could not get dhcp info");
            return null;
        }

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

    /**
     * Listen on socket for responses, timing out after TIMEOUT_MS
     *
     * @param socket socket on which the announcement request was sent
     * @return list of discovered servers, never null
     * @throws IOException
     */
    private ArrayList<DeviceData> listenForResponses(DatagramSocket socket) throws IOException {
        long start = System.currentTimeMillis();
        byte[] buf = new byte[1024];
        ArrayList<DeviceData> deviceDatas = new ArrayList<DeviceData>();

        // Loop and try to receive responses until the timeout elapses. We'll
        // get
        // back the packet we just sent out, which isn't terribly helpful, but
        // we'll
        // discard it in parseResponse because the cmd is wrong.
        try {
//	    sb.append("\n\nData receiving...");

            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String replyMessage = new String(packet.getData(), 0, packet.getLength());

//		sb.append("\n\nReplyMessage... " + replyMessage);

                Log.d(TAG, "Packet received after " + (System.currentTimeMillis() - start) + " " + replyMessage);
                if (replyMessage.startsWith("NOTIFY") == false)
                    continue;
                String deviceIP = packet.getAddress().toString().substring(1);
//		sb.append("\n\nDevice is Terra Device... ");
                try {
                    DeviceData dd = new DeviceData(UDP_DISCOVERY_PORT, replyMessage);
                    Iterator<DeviceData> iter = deviceDatas.iterator();
                    boolean isFound = false;
                    while (iter.hasNext()) {
                        DeviceData odd = iter.next();
                        if (odd.equals(dd)) {
                            isFound = true;
                            break;
                        }
                    }
                    if (!isFound) {
                        deviceDatas.add(dd);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (SocketTimeoutException e) {
            Log.d(TAG, "Receive timed out");
        }
        return deviceDatas;
    }

}
