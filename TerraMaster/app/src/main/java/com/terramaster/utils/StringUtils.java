package com.terramaster.utils;

import java.text.DecimalFormat;

public class StringUtils {

    public static String formatCount(int count, String suffix) {
        if (count == 0) {
            return "No " + suffix;
        }
        return count + " " + suffix;

    }

    public static String formatFileSize(long length) {
        if (length <= 0)
            return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(length) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(length
                / Math.pow(1024, digitGroups))
                + " " + units[digitGroups];
    }

    public static boolean isEmpty(String name) {
        // TODO Auto-generated method stub
        return name == null || name.length() <= 0;
    }

    public static String getNameFromPath(String path) {
        String name = null;
        int index = path.lastIndexOf("/");
        if (index >= 0) {
            name = path.substring(index);
        } else {
            name = path;
        }
        return name;
    }

    public static String _ReplaceAllUrl(String url){
        url = url.replaceAll("%", "%25");
        url = url.replaceAll("#", "%23");
        url = url.replaceAll("[\\s ]","%20");
        return url;
    }

    public static String getExtensionFromPath(String path) {
        String extenson = null;
        int index = path.lastIndexOf(".");
        if (index >= 0) {
            extenson = path.substring(index);
        }
        return extenson;
    }

}
