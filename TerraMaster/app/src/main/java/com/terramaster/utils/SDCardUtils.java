package com.terramaster.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.terramaster.BaseActivity;
import com.terramaster.R;

import java.io.File;
import java.text.DecimalFormat;

public class SDCardUtils {
    public static final String CACHE_FOLDER_NAME = "TerraMaster";
    public static String[] thumbColumns = {MediaStore.Video.Thumbnails.DATA};
    public static String[] mediaColumns = {MediaStore.Video.Media._ID};

    @SuppressWarnings("deprecation")
    public static String getThumbnailPathForLocalFile(Context context, Uri fileUri) {

        long fileId = getFileId(context, fileUri);

        MediaStore.Video.Thumbnails.getThumbnail(context.getContentResolver(), fileId, MediaStore.Video.Thumbnails.MICRO_KIND, null);

        Cursor thumbCursor = null;
        try {

            thumbCursor = ((Activity) context).managedQuery(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, thumbColumns, MediaStore.Video.Thumbnails.VIDEO_ID + " = " + fileId, null, null);

            if (thumbCursor.moveToFirst()) {
                String thumbPath = thumbCursor.getString(thumbCursor.getColumnIndex(MediaStore.Video.Thumbnails.DATA));

                return thumbPath;
            }

        } finally {
        }

        return null;
    }

    @SuppressWarnings("deprecation")
    public static long getFileId(Context context, Uri fileUri) {

        Cursor cursor = ((Activity) context).managedQuery(fileUri, mediaColumns, null, null, null);

        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
            int id = cursor.getInt(columnIndex);

            return id;
        }

        return 0;
    }

    public static Bitmap getThumbnail(ContentResolver cr, String path) {

        Cursor ca = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.MediaColumns._ID}, MediaStore.MediaColumns.DATA + "=?", new String[]{path}, null);
        if (ca != null && ca.moveToFirst()) {
            int id = ca.getInt(ca.getColumnIndex(MediaStore.MediaColumns._ID));
            ca.close();
            return MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MICRO_KIND, null);
        }

        ca.close();
        return null;

    }

    public static File getDownloadingPath(){
        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!folder.isDirectory()) {
            folder.mkdir();
        }
        return folder;
    }

    public static File createDownloadingFile(String fromPath) {
        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        int index = fromPath.lastIndexOf("/");
        String name = null;
        if (index >= 0) {
            name = fromPath.substring(index);
        } else {
            name = fromPath;
        }
        File file = new File(folder, name);
//        int count = 0;
//        while (file.isFile()) {
//            count++;
//            file = new File(folder, generateName(name, count));
//        }

        return file;
    }

    public static File getCacheDir(){
        File folder = new File(Environment.getExternalStorageDirectory(), CACHE_FOLDER_NAME);
        if (!folder.isDirectory()) {
            folder.mkdir();
        }
        return folder;
    }

    public static File createCatcheFile(String fromPath) {
        File folder = new File(Environment.getExternalStorageDirectory(), CACHE_FOLDER_NAME);
        if (!folder.isDirectory()) {
            folder.mkdir();
        }
        int index = fromPath.lastIndexOf("/");
        String name = null;
        if (index >= 0) {
            name = fromPath.substring(index);
        } else {
            name = fromPath;
        }
        File file = new File(folder, name);
        if (file.isFile()) {
            file.delete();
        }

        return file;
    }

    private static String generateName(String name, int count) {
        String newName = null;
        int lastIndex = name.lastIndexOf(".");
        if (lastIndex >= 0) {
            newName = name.substring(0, lastIndex) + "[" + count + "]" + name.substring(lastIndex);
        } else {
            newName = name + "[" + count + "]";
        }
        return newName;
    }

    public static void openFile(Context context, File url) {
        // Create URI
        try {
            File file = url;
            Uri uri = Uri.fromFile(file);

            String fileName = url.getName();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            // Check what kind of file you are trying to open, by comparing the
            // url with extensions.
            // When the if condition is matched, plugin sets the correct intent
            // (mime) type,
            // so Android knew what application to use to open the file
            Log.e("andsotan",uri+"");
            if (IconUtils.isDoc(fileName)) {
                // Word document
                intent.setDataAndType(uri, "application/msword");
            } else if (IconUtils.isPdf(fileName)) {
                // PDF file
                intent.setDataAndType(uri, "application/pdf");
            } else if (IconUtils.isPpt(fileName)) {
                // Powerpoint file
                intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
            } else if (IconUtils.isExcel(fileName)) {
                // Excel file
                intent.setDataAndType(uri, "application/vnd.ms-excel");
            } else if (IconUtils.isZip(fileName)) {
                // WAV audio file
                intent.setDataAndType(uri, "application/zip");
            } else if (fileName.endsWith(".rtf")) {
                // RTF file
                intent.setDataAndType(uri, "application/rtf");
            } else if (IconUtils.isAudio(fileName)) {
                // WAV audio file
                intent.setDataAndType(uri, "audio/*");
            } else if (fileName.endsWith(".gif")) {
                // GIF file
                intent.setDataAndType(uri, "image/gif");
            } else if (IconUtils.isPhoto(fileName)) {
                // JPG file
                intent.setDataAndType(uri, "image/*");
            } else if (IconUtils.isTxt(fileName)) {
                // Text file
                intent.setDataAndType(uri, "text/plain");
            } else if (IconUtils.isVideo(fileName)) {
                // Video files
                intent.setDataAndType(uri, "video/*");
            } else {
                // if you want you can also define the intent type for any other
                // file

                // additionally use else clause below, to manage other unknown
                // extensions
                // in this case, Android will show all applications installed on
                // the device
                // so you can choose which application to use
                intent.setDataAndType(uri, "*/*");
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            BaseActivity.needToCheckLock = false;
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            AlertUtils.showSimpleAlert(context, context.getString(R.string.e_no_app_found));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            AlertUtils.showSimpleAlert(context, context.getString(R.string.e_unknown));
        }
    }

    public static void clearCacheFolder() {
        File folder = new File(Environment.getExternalStorageDirectory(), CACHE_FOLDER_NAME);
        deleteDirectory(folder,false);
    }

    private static void deleteDirectory(File dir, boolean flag) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                File file = new File(dir, children[i]);
                if(file.isDirectory()){
                    deleteDirectory(file,true);
                }else{
                    file.delete();
                }
            }
            if(flag) dir.delete();
        }
    }

    public static String getCacheFolderSize() {
        File folder = new File(Environment.getExternalStorageDirectory(), CACHE_FOLDER_NAME);
        return formatBytes(dirSize(folder));
    }

    public static String formatBytes(long size) {
        if (size <= 0)
            return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * Return the size of a directory in bytes
     */
    private static long dirSize(File dir) {

        if (dir.exists()) {
            long result = 0;
            File[] fileList = dir.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                // Recursive call if it's a directory
                if (fileList[i].isDirectory()) {
                    result += dirSize(fileList[i]);
                } else {
                    // Sum the file size in bytes
                    result += fileList[i].length();
                }
            }
            return result; // return the file size
        }
        return 0;
    }
}//centos amd64
