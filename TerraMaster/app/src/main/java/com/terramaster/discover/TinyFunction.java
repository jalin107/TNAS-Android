package com.terramaster.discover;

import org.apache.commons.net.util.SubnetUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;

public class TinyFunction {
    //change string ip to byte

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static byte[] ip2byte(String ip) {
        if (ip.length() == 0) return null;
        byte[] addr = new byte[4];
        short c;
        StringTokenizer st = new StringTokenizer(ip, ".");
        for (int i = 0; i < 4; i++) {
            c = Short.parseShort(st.nextToken());
            addr[i] = (byte) c;
        }
        return addr;
    }

    public static String byte2ip(byte[] buf, int src_start) {
        short a, b, c, d;
        a = (short) buf[src_start];
        if (a < 0) a += 256;

        b = (short) buf[src_start + 1];
        if (b < 0) b += 256;

        c = (short) buf[src_start + 2];
        if (c < 0) c += 256;

        d = (short) buf[src_start + 3];
        if (d < 0) d += 256;

        if (a == 0 && b == 0 && c == 0 && d == 0) {
            return null;
        }

        return a + "." + b + "." + c + "." + d;
    }

    //dangerous for memcpy, it's may out of stack
    public static byte[] memcpy(byte[] src, byte[] dst, int lens) {
        for (int i = 0; i < lens; i++) {
            dst[i] = src[i];
        }
        return dst;
    }

    public static byte[] memcpy(byte[] src, byte[] dst,
                                int src_start, int dst_start, int lens) {
        for (int i = 0; i < lens; i++) {
            dst[dst_start++] = src[src_start++];
        }
        return dst;
    }

    public static byte[] getByte(byte[] src, int src_start, int lens) {
        byte[] data = new byte[lens + 1];
        for (int i = 0; i < lens; i++) {
            data[i] = src[src_start++];
        }
        return data;
    }

    public static short htons(byte[] src, int src_start) {
        int i0 = ((int) src[src_start] + 256) % 256;
        int i1 = ((int) src[src_start + 1] + 256) % 256;
        return (short) ((i1 << 8) + i0);
    }

    public static void openshare(String path) {
        String osname = System.getProperty("os.name");
        try {
            if (osname.contains("Win") || osname.contains("win")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler file://" + path);
            } else {
                if (osname.startsWith("Mac OS")) {
                    Class fileMgr = Class.forName("com.apple.eio.FileManager");
                    Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[]{String.class});
                    openURL.invoke(null, new Object[]{"smb://" + path});
                } else {
                    /* Assume Unix or Linux */
                    String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
                    String browser = null;
                    for (int count = 0; count < browsers.length && browser == null; count++) {
                        if (Runtime.getRuntime().exec(new String[]{"which", browsers[count]}).waitFor() == 0) {
                            browser = browsers[count];
                        }
                    }

                    if (browser == null) {
                        throw new Exception("Could not find web browser");
                    } else {
                        Runtime.getRuntime().exec(new String[]{browser, "http://" + path});
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

//	public static void openurl(String path) {
//    	File handle = new File(path);
//        URI uripath = handle.toURI();
//        String osname = System.getProperty("os.name");
//        
//        if(handle.exists() == false){
//        	try {
//				uripath = new URI(path);
//			} catch (URISyntaxException e) {
//				e.printStackTrace();
//			}
//        }
//        
//        try {
//        	System.out.println(uripath.toString());
//        	
//            if (osname.startsWith("Mac OS")) {
//            	if(handle.exists()){
//            		Desktop.getDesktop().open(handle);
//            	}else{          	
//	                Class fileMgr = Class.forName("com.apple.eio.FileManager");
//	                Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[]{String.class});
//	                openURL.invoke(null, new Object[]{uripath.toString()});               
//            	}	
//            } else if (osname.startsWith("Windows")) {
//                Runtime.getRuntime().exec("explorer " + uripath.toString());
//            } else {
//                /* Assume Unix or Linux */
//                String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
//                String browser = null;
//                for (int count = 0; count < browsers.length && browser == null; count++) {
//                    if (Runtime.getRuntime().exec(new String[]{"which", browsers[count]}).waitFor() == 0) {
//                        browser = browsers[count];
//                    }
//                }
//
//                if (browser == null) {
//                    throw new Exception("Could not find web browser");
//                } else {
//                    Runtime.getRuntime().exec(new String[]{browser, uripath.toString()});
//                }
//            }
//        } catch (Exception ex) {
//            System.out.println(ex);
//        }
//     
//        handle = null;
//    }

//	public static BufferedImage createResizedCopy(Image originalImage, 
//    		int scaledWidth, int scaledHeight, 
//    		boolean preserveAlpha)
//    {
//    	System.out.println("resizing...");
//    	int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
//    	BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
//    	Graphics2D g = scaledBI.createGraphics();
//    	if (preserveAlpha) {
//    		g.setComposite(AlphaComposite.Src);
//    	}
//    	g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null); 
//    	g.dispose();
//    	return scaledBI;
//    }

    public static void explore(String path) {
        try {
            String osname = System.getProperty("os.name");
            if (osname.startsWith("Mac OS")) {
                Class fileMgr = Class.forName("com.apple.eio.FileManager");
                Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[]{String.class});
                openURL.invoke(null, new Object[]{path});
            } else if (osname.startsWith("Windows")) {
                Runtime.getRuntime().exec("explorer " + path);
                //java.awt.Desktop.getDesktop().browse(new URI("ftp://" + url + "/"));
            } else {
                String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
                String browser = null;
                for (int count = 0; count < browsers.length && browser == null; count++) {
                    if (Runtime.getRuntime().exec(new String[]{"which", browsers[count]}).waitFor() == 0) {
                        browser = browsers[count];
                    }
                }

                if (browser == null) {
                    throw new Exception("Could not connect ftp browser");
                } else {
                    Runtime.getRuntime().exec(new String[]{browser, path});
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static String showOperationSystem() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.indexOf("mac") >= 0) {
            return "MAC";
        } else if (os.indexOf("win") >= 0) {
            return "WIN";
        } else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {
            return "LINUX/UNIX";
        } else if (os.indexOf("sunos") >= 0) {
            return "SOLARIS";
        }

        return "UNKNOW";
    }

    public static String bytes2hex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 3];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = hexArray[v >>> 4];
            hexChars[j * 3 + 1] = hexArray[v & 0x0F];
            hexChars[j * 3 + 2] = ':';
        }

        return new String(hexChars).substring(0, 17);
    }

    public static boolean IsLinked(NetworkInterface ni, String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            boolean reachable = address.isReachable(ni, 1, 3000);

            if (reachable) {
                System.out.println("ip:[" + ip + "] has been used");
                return true;
            } else {
                System.out.println("ip:[" + ip + "] is free");
            }

            return false;
        } catch (UnknownHostException ex) {
            System.out.println(ex);
            return false;
        } catch (IOException ex) {
            System.out.println(ex);
            return false;
        }
    }

    public static String MaskIPv4(NetworkInterface networkInterface) {
        List<InterfaceAddress> ifcaddrs = networkInterface.getInterfaceAddresses();

        for (int i = 0; i < ifcaddrs.size(); i++) {
            short masklens = ifcaddrs.get(i).getNetworkPrefixLength();

            if (masklens > 0 && masklens < 32) {
//				System.out.println(ifcaddrs.get(i).getAddress().getHostAddress() + "/" +
//								masklens);

                SubnetUtils subnetutil =
                        new SubnetUtils(
                                ifcaddrs.get(i).getAddress().getHostAddress() + "/" +
                                        masklens);

                return subnetutil.getInfo().getNetmask();
            }
        }

        return "255.255.255.0";
    }

    public static boolean SameNetwork(String ip1, String ip2, String mask)
            throws Exception {

        byte[] a1 = InetAddress.getByName(ip1).getAddress();
        byte[] a2 = InetAddress.getByName(ip2).getAddress();
        byte[] m = InetAddress.getByName(mask).getAddress();

        for (int i = 0; i < a1.length; i++)
            if ((a1[i] & m[i]) != (a2[i] & m[i]))
                return false;

        return true;

    }


    public static String GetUnusedIPAddress(NetworkInterface ni) {
        String local_ip = null;
        Enumeration<InetAddress> inetaddrs = ni.getInetAddresses();
        while (inetaddrs.hasMoreElements()) {
            InetAddress iaddr = (InetAddress) inetaddrs.nextElement();
            if (iaddr instanceof Inet6Address) {
                continue;
            }

            local_ip = iaddr.toString();
            break;
        }

        if (local_ip == null) return null;

        String tmp_ip = null;
        local_ip = local_ip.substring(1);

        byte[] checked = new byte[255];

        int total = 253;

        while ((total--) != 0) {
            int i = (int) ((Math.random() * 253) + 1);

            //check already use this num
            if (checked[i] == 7) {
                continue;
            }

            checked[i] = 7; //set this num is been used

            tmp_ip = local_ip.substring(0, local_ip.lastIndexOf(".") + 1) + i;

            System.out.println("[" + tmp_ip + "][" + local_ip + "]");

            if (tmp_ip.equals(local_ip)) {
                System.out.println("same ip address...skip");
                continue;
            }

            if (IsLinked(ni, tmp_ip) == false) {
                return tmp_ip;
            }
        }

        return null;
    }

    public static boolean IPOK(String IPv4) {
        // TODO Auto-generated method stub
        String regex = "(2[5][0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})";

        return IPv4.matches(regex);
    }

    public static NetworkInterface GetInterfaceByIPv4(String IFCIP) {
        // TODO Auto-generated method stub
        InetAddress ia = null;
        NetworkInterface ni = null;

        try {
            ia = InetAddress.getByName(IFCIP);
            ni = NetworkInterface.getByInetAddress(ia);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            return null;
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            return null;
        }

        return ni;
    }
}
