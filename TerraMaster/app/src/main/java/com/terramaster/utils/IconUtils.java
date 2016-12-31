package com.terramaster.utils;

import com.terramaster.R;

import java.util.Locale;

public class IconUtils {
    public static int findIcon(String fileName) {
        if (isPdf(fileName)) {
            return R.drawable.ic_pdf;
        } else if (isExcel(fileName)) {
            return R.drawable.ic_excel;
        } else if (isPhoto(fileName)) {
            return R.drawable.ic_photo;
        } else if (isAudio(fileName)) {
            return R.drawable.ic_music;
        } else if (isVideo(fileName)) {
            return R.drawable.ic_video;
        } else if (isTxt(fileName)) {
            return R.drawable.ic_txt;
        } else if (isDoc(fileName)) {
            return R.drawable.ic_word;
        } else if (isPpt(fileName)) {
            return R.drawable.ic_ppt;
        } else if (isZip(fileName)) {
            return R.drawable.ic_zip;
        } else if (isApk(fileName)) {
            return R.drawable.ic_apk;
        }
        return R.drawable.ic_unknown;
    }

    public static boolean isApk(String fileName) {
        return fileName.toLowerCase(Locale.ENGLISH).endsWith(".apk");
    }

    public static boolean isTxt(String fileName) {
        return fileName.toLowerCase(Locale.ENGLISH).endsWith(".txt");
    }

    public static boolean isZip(String fileName) {
        fileName = fileName.toLowerCase(Locale.ENGLISH);
        return fileName.endsWith(".zip") || fileName.endsWith(".rar") || fileName.endsWith(".tar") || fileName.endsWith(".7z");
    }

    public static boolean isPdf(String fileName) {
        fileName = fileName.toLowerCase(Locale.ENGLISH);
        return fileName.endsWith(".pdf") || fileName.endsWith(".epub");
    }

    public static boolean isDoc(String fileName) {
        fileName = fileName.toLowerCase(Locale.ENGLISH);
        return fileName.endsWith(".doc") || fileName.endsWith(".docx") || fileName.endsWith(".odt") || fileName.endsWith(".rtf");
    }

    public static boolean isPpt(String fileName) {
        fileName = fileName.toLowerCase(Locale.ENGLISH);
        return fileName.endsWith(".ppt") || fileName.endsWith(".pptx");
    }

    public static boolean isExcel(String fileName) {
        fileName = fileName.toLowerCase(Locale.ENGLISH);
        return fileName.endsWith(".xls") || fileName.endsWith(".csv") || fileName.endsWith(".dif") || fileName.endsWith(".slk") || fileName.endsWith(".xlsx") || fileName.endsWith(".xlsm") || fileName.endsWith(".xlsb") || fileName.endsWith(".xltx")
                || fileName.endsWith(".xltm") || fileName.endsWith(".xlt") || fileName.endsWith(".xla") || fileName.endsWith(".xlw");
    }

    public static boolean isPhoto(String fileName) {
        fileName = fileName.toLowerCase(Locale.ENGLISH);
        return fileName.endsWith(".png")
                || fileName.endsWith(".jpg")
                || fileName.endsWith(".jpe")
                || fileName.endsWith(".jpeg")
                || fileName.endsWith(".gif")
                || fileName.endsWith(".bmp")
                || fileName.endsWith(".webp");
    }

    public static boolean isAudio(String fileName) {
        fileName = fileName.toLowerCase(Locale.ENGLISH);
        return fileName.endsWith(".mp3")
                || fileName.endsWith(".aac")
                || fileName.endsWith(".amr")
                || fileName.endsWith(".m4a")
                || fileName.endsWith(".flac")
                || fileName.endsWith(".wav")
                || fileName.endsWith(".mid")
                || fileName.endsWith(".xmf")
                || fileName.endsWith(".mxmf")
                || fileName.endsWith(".ota")
                || fileName.endsWith(".imy")
                || fileName.endsWith(".ogg");
    }

    public static boolean isVideo(String fileName) {
        fileName = fileName.toLowerCase(Locale.ENGLISH);
        return fileName.endsWith(".3gp")
                || fileName.endsWith(".mp4")
                || fileName.endsWith(".mkv")
                || fileName.endsWith(".webm")
                || fileName.endsWith(".flv")
                || fileName.endsWith(".wmv")
                || fileName.endsWith(".rmvb")
                || fileName.endsWith(".dat")
                || fileName.endsWith(".mpg")
                || fileName.endsWith(".mpeg")
                || fileName.endsWith(".avi");
    }
}
