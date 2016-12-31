package com.terramaster.dbhelper;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.terramaster.model.AlbumBackupDetail;
import com.terramaster.model.ImageBackupDetail;
import com.terramaster.model.TaskDetail;
import com.terramaster.utils.DateUtils;
import com.terramaster.utils.LogM;
import com.terramaster.utils.SDCardUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import static com.terramaster.dbhelper.DBKeys.ALBUM_BACKUP_TB;
import static com.terramaster.dbhelper.DBKeys.BACKUP_MODE;
import static com.terramaster.dbhelper.DBKeys.CREATED_AT;
import static com.terramaster.dbhelper.DBKeys.FROM_PATH;
import static com.terramaster.dbhelper.DBKeys.ID_ALBUM_TB;
import static com.terramaster.dbhelper.DBKeys.IMAGES_BACKUP_TB;
import static com.terramaster.dbhelper.DBKeys.IS_ENABLED;
import static com.terramaster.dbhelper.DBKeys.LAST_UPDATED;
import static com.terramaster.dbhelper.DBKeys.MAC_ADDRESS;
import static com.terramaster.dbhelper.DBKeys.NO_BACKUP_PHOTOS;
import static com.terramaster.dbhelper.DBKeys.ROWID;
import static com.terramaster.dbhelper.DBKeys.STATUS_COMPLETE;
import static com.terramaster.dbhelper.DBKeys.TASK_STATUS;
import static com.terramaster.dbhelper.DBKeys.TASK_TB;
import static com.terramaster.dbhelper.DBKeys.TASK_TYPE;
import static com.terramaster.dbhelper.DBKeys.TO_PATH;
import static com.terramaster.dbhelper.DBKeys.TASK_PROCESS;
import static com.terramaster.dbhelper.DBKeys.TASK_FILESIZE;

/**
 * Database Helper class
 * <p/>
 * <p>
 * Contains all methods for insert/update/delete operation on database
 * </p>
 *
 * @version 1.0
 * @since 2014-10-25
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String log_tag = "log_tag";
    private static String DB_PATH;
    private static String DB_NAME = "terramaster_db.db";
    private static DatabaseHelper instance;
    private final Context myContext;
    protected SQLiteDatabase myDataBase;

    /**
     * Constructor Takes and keeps a reference of the passed context in order to
     * access to the application assets and resources.
     *
     * @param context
     */
    @SuppressLint("SdCardPath")
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 2);
        this.myContext = context;
        DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        //DB_PATH = SDCardUtils.getCacheDir()+"/databases/";
        try {
            createDataBase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context);
            instance.openDataBase();
        }
        return instance;
    }

    /**
     * Creates a empty database on the system and rewrites it with your own
     * database.
     */
    public void createDataBase() throws IOException {
        boolean dbExist = checkDataBase();
        if (dbExist){
            // do nothing - database already exist
        } else {
            // By calling this method and empty database will be created into
            // the default system path
            // of your application so we are gonna be able to overwrite that
            // database with our database.
            this.getReadableDatabase();
            try {
                copyDataBase();
                Log.d(log_tag, "Data base created.......");
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }

    }

    /**
     * Check if the database already exist to avoid re-copying the file each
     * time you open the application.
     *
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        try {
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        } catch (Exception e) {
        }

        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null ? true : false;
    }

    /**
     * Copies your database from your local assets-folder to the just created
     * empty database in the system folder, from where it can be accessed and
     * handled. This is done by transfering bytestream.
     */
    private void copyDataBase() throws IOException {
        InputStream myInput = myContext.getAssets().open(DB_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);
        if (myInput != null && myOutput != null) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }
        } else {
            Log.d("error", "Not Found");
        }
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    public void openDataBase() throws SQLException {
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
        Log.d(log_tag, "Database is opened....");
    }

    @Override
    public synchronized void close() {
    /*
	 * if (myDataBase != null) myDataBase.close(); super.close();
	 */
        Log.d(log_tag, "Database is closed....");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void deleteTask(long rowId) {
        // TODO Auto-generated method stub
        myDataBase.delete(TASK_TB, ROWID + "=" + rowId, null);
    }

    public long insertTask(TaskDetail taskDetail){
        // TODO Auto-generated method stub
        ContentValues values = new ContentValues();
        values.put(FROM_PATH, taskDetail.getFrom());
        values.put(TO_PATH, taskDetail.getTo());
        values.put(TASK_PROCESS, taskDetail.getProcess());
        values.put(TASK_FILESIZE, taskDetail.getTaskFileSize());
        values.put(TASK_STATUS, taskDetail.getTaskStatus());
        values.put(TASK_TYPE, taskDetail.getTaskType());
        values.put(LAST_UPDATED, DateUtils.SQL_DATE_SDF.format(taskDetail.getLastUpdated()));
        values.put(CREATED_AT, DateUtils.SQL_DATE_SDF.format(taskDetail.getCreatedAt()));
        values.put(MAC_ADDRESS, taskDetail.getMacAddress());

        LogM.e(taskDetail.getFrom() + "--Insert Task-->" + taskDetail.getTo());
        return myDataBase.insert(TASK_TB, null, values);
    }

    public ArrayList<TaskDetail> getAllTasks(String macAddress) {
        // TODO Auto-generated method stub
        ArrayList<TaskDetail> list = new ArrayList<TaskDetail>();
        Cursor cursor = myDataBase.query(TASK_TB, null, MAC_ADDRESS + "='" + macAddress + "'", null, null, null, CREATED_AT + " desc");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    TaskDetail taskDetail = new TaskDetail();
                    parseTask(taskDetail, cursor);

                    list.add(taskDetail);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return list;
    }

    public int updateTaskDetail(TaskDetail taskDetail) {
        // TODO Auto-generated method stub
        ContentValues values = new ContentValues();
        values.put(FROM_PATH, taskDetail.getFrom());
        values.put(TO_PATH, taskDetail.getTo());
        if(taskDetail.getProcess() != 0) {
            values.put(TASK_PROCESS, taskDetail.getProcess());
        }
        values.put(TASK_STATUS, taskDetail.getTaskStatus());
        values.put(TASK_TYPE, taskDetail.getTaskType());
        values.put(LAST_UPDATED, DateUtils.SQL_DATE_SDF.format(taskDetail.getLastUpdated()));

        return myDataBase.update(TASK_TB, values, ROWID + "=" + taskDetail.getRowId(), null);
    }

    private void parseTask(TaskDetail taskDetail, Cursor cursor) {
        taskDetail.setRowId(cursor.getLong(cursor.getColumnIndex(ROWID)));
        taskDetail.setFrom(cursor.getString(cursor.getColumnIndex(FROM_PATH)));
        taskDetail.setTo(cursor.getString(cursor.getColumnIndex(TO_PATH)));
        taskDetail.setProcess(cursor.getLong(cursor.getColumnIndex(TASK_PROCESS)));
        taskDetail.setTaskFileSize(cursor.getLong(cursor.getColumnIndex(TASK_FILESIZE)));
        taskDetail.setTaskType(cursor.getInt(cursor.getColumnIndex(TASK_TYPE)));
        taskDetail.setTaskStatus(cursor.getInt(cursor.getColumnIndex(TASK_STATUS)));
        taskDetail.setMacAddress(cursor.getString(cursor.getColumnIndex(MAC_ADDRESS)));
        try {
            taskDetail.setLastUpdated(DateUtils.SQL_DATE_SDF.parse(cursor.getString(cursor.getColumnIndex(LAST_UPDATED))));
            taskDetail.setCreatedAt(DateUtils.SQL_DATE_SDF.parse(cursor.getString(cursor.getColumnIndex(CREATED_AT))));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public TaskDetail findNextTask(TaskDetail taskDoneDetail) {
        // TODO Auto-generated method stub
        TaskDetail taskDetail = null;
        String query = "select * from task_tb where " + TASK_STATUS + "=" + DBKeys.STATUS_WAITING + " and " + TASK_TYPE + "=" + taskDoneDetail.getTaskType() + " and " + MAC_ADDRESS + "='" + taskDoneDetail.getMacAddress() + "' order by created_at";
        LogM.e("Query: " + query);
        Cursor cursor = myDataBase.rawQuery(query, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                taskDetail = new TaskDetail();
                parseTask(taskDetail, cursor);
            }
            cursor.close();
        }
        return taskDetail;
    }

    public TaskDetail findWaitingTask(String macAddress) {
        // TODO Auto-generated method stub
        TaskDetail taskDetail = null;
        String query = "select * from task_tb where task_status=" + DBKeys.STATUS_WAITING + " and " + MAC_ADDRESS + "='" + macAddress + "' order by created_at";
        LogM.e("Query: " + query);
        Cursor cursor = myDataBase.rawQuery(query, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                taskDetail = new TaskDetail();
                parseTask(taskDetail, cursor);
            }
            cursor.close();
        }
        return taskDetail;
    }

    public void deleteAllTask() {
        // TODO Auto-generated method stub
        myDataBase.delete(TASK_TB, null, null);
    }

    public void deleteAllCompletedTask() {
        // TODO Auto-generated method stub
        myDataBase.delete(TASK_TB, TASK_STATUS + "=" + STATUS_COMPLETE, null);
    }


    public void deleteAlbumBackup(long rowId) {
        // TODO Auto-generated method stub
        myDataBase.delete(ALBUM_BACKUP_TB, ROWID + "=" + rowId, null);
    }

    public long insertAlbumBackup(AlbumBackupDetail autoBackupDetail) {
        // TODO Auto-generated method stub
        ContentValues values = new ContentValues();
        values.put(FROM_PATH, autoBackupDetail.getFrom());
        values.put(TO_PATH, autoBackupDetail.getTo());
        values.put(BACKUP_MODE, autoBackupDetail.getBakupMode());
        values.put(NO_BACKUP_PHOTOS, autoBackupDetail.getNoOfBackupPhotos());
        values.put(LAST_UPDATED, DateUtils.SQL_DATE_SDF.format(autoBackupDetail.getLastUpdated()));
        values.put(CREATED_AT, DateUtils.SQL_DATE_SDF.format(autoBackupDetail.getCreatedAt()));
        values.put(MAC_ADDRESS, autoBackupDetail.getMacAddress());
        values.put(IS_ENABLED, autoBackupDetail.isEnabled() ? 1 : 0);

        return myDataBase.insert(ALBUM_BACKUP_TB, null, values);
    }

    public int updateAlbumBackup(AlbumBackupDetail autoBackupDetail) {
        // TODO Auto-generated method stub
        ContentValues values = new ContentValues();
        values.put(FROM_PATH, autoBackupDetail.getFrom());
        values.put(TO_PATH, autoBackupDetail.getTo());
        values.put(BACKUP_MODE, autoBackupDetail.getBakupMode());
        values.put(NO_BACKUP_PHOTOS, autoBackupDetail.getNoOfBackupPhotos());
        values.put(LAST_UPDATED, DateUtils.SQL_DATE_SDF.format(autoBackupDetail.getLastUpdated()));
        values.put(CREATED_AT, DateUtils.SQL_DATE_SDF.format(autoBackupDetail.getCreatedAt()));
        values.put(MAC_ADDRESS, autoBackupDetail.getMacAddress());
        values.put(IS_ENABLED, autoBackupDetail.isEnabled() ? 1 : 0);

        return myDataBase.update(ALBUM_BACKUP_TB, values, ROWID + "=" + autoBackupDetail.getRowId(), null);
    }

    private void parseAlbumBackup(AlbumBackupDetail autoBackupDetail, Cursor cursor) {
        autoBackupDetail.setRowId(cursor.getLong(cursor.getColumnIndex(ROWID)));
        autoBackupDetail.setFrom(cursor.getString(cursor.getColumnIndex(FROM_PATH)));
        autoBackupDetail.setTo(cursor.getString(cursor.getColumnIndex(TO_PATH)));
        autoBackupDetail.setBakupMode(cursor.getInt(cursor.getColumnIndex(BACKUP_MODE)));
        autoBackupDetail.setNoOfBackupPhotos(cursor.getInt(cursor.getColumnIndex(NO_BACKUP_PHOTOS)));
        autoBackupDetail.setMacAddress(cursor.getString(cursor.getColumnIndex(MAC_ADDRESS)));
        autoBackupDetail.setEnabled(cursor.getInt(cursor.getColumnIndex(IS_ENABLED)) == 1);
        try {
            autoBackupDetail.setLastUpdated(DateUtils.SQL_DATE_SDF.parse(cursor.getString(cursor.getColumnIndex(LAST_UPDATED))));
            autoBackupDetail.setCreatedAt(DateUtils.SQL_DATE_SDF.parse(cursor.getString(cursor.getColumnIndex(CREATED_AT))));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public ArrayList<AlbumBackupDetail> getAllAlbumBackupList(String macAddress) {
        // TODO Auto-generated method stub
        ArrayList<AlbumBackupDetail> list = new ArrayList<AlbumBackupDetail>();
        Cursor cursor = myDataBase.query(ALBUM_BACKUP_TB, null, MAC_ADDRESS + "='" + macAddress + "' and " + IS_ENABLED + "=1", null, null, null, CREATED_AT + " desc");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    AlbumBackupDetail taskDetail = new AlbumBackupDetail();
                    parseAlbumBackup(taskDetail, cursor);

                    list.add(taskDetail);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return list;
    }

    public boolean isAutoBackupAvailable(String macAddress) {
        // TODO Auto-generated method stub
        boolean b = false;
        Cursor cursor = myDataBase.query(ALBUM_BACKUP_TB, null, MAC_ADDRESS + "='" + macAddress + "'", null, null, null, null);
        if (cursor != null) {
            b = cursor.moveToFirst();
            cursor.close();
        }
        return b;
    }

//    public boolean isAutoBackupEnabled(String macAddress)
//    {
//	// TODO Auto-generated method stub
//	boolean b=false;
//	Cursor cursor=myDataBase.query(ALBUM_BACKUP_TB, null, IS_ENABLED+"=1 and "+MAC_ADDRESS+"='"+macAddress+"'", null, null, null, null);
//	if(cursor!=null)
//	{
//	    b=cursor.moveToFirst();
//	    cursor.close();
//	}
//	return b;
//    }

//    public void enableAutoBackup(boolean isEnable)
//    {
//	ContentValues values=new ContentValues();
//	values.put(IS_ENABLED, isEnable?1:0);
//	
//	myDataBase.update(ALBUM_BACKUP_TB, values, null, null);
//    }

    private void parseImageBackup(ImageBackupDetail detail, Cursor cursor) {
        detail.setRowId(cursor.getLong(cursor.getColumnIndex(ROWID)));
        detail.setFrom(cursor.getString(cursor.getColumnIndex(FROM_PATH)));
        detail.setTo(cursor.getString(cursor.getColumnIndex(TO_PATH)));
        detail.setTaskStatus(cursor.getInt(cursor.getColumnIndex(TASK_STATUS)));
        detail.setMacAddress(cursor.getString(cursor.getColumnIndex(MAC_ADDRESS)));
        detail.setAlbumId(cursor.getLong(cursor.getColumnIndex(ID_ALBUM_TB)));
        try {
            detail.setLastUpdated(DateUtils.SQL_DATE_SDF.parse(cursor.getString(cursor.getColumnIndex(LAST_UPDATED))));
            detail.setCreatedAt(DateUtils.SQL_DATE_SDF.parse(cursor.getString(cursor.getColumnIndex(CREATED_AT))));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public ImageBackupDetail getImageBackup(long albumId, String path, String macAddress) {
        // TODO Auto-generated method stub
        ImageBackupDetail detail = null;
        Cursor cursor = myDataBase.query(IMAGES_BACKUP_TB, null, ID_ALBUM_TB + "=" + albumId + " and " + FROM_PATH + "='" + path + "' and " + MAC_ADDRESS + "='" + macAddress + "'", null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                detail = new ImageBackupDetail();
                parseImageBackup(detail, cursor);
            }
            cursor.close();
        }
        return detail;
    }

    public int updateImageBackupDetail(ImageBackupDetail detail) {
        // TODO Auto-generated method stub
        ContentValues values = new ContentValues();
        values.put(FROM_PATH, detail.getFrom());
        values.put(TO_PATH, detail.getTo());
        values.put(TASK_STATUS, detail.getTaskStatus());
        values.put(LAST_UPDATED, DateUtils.SQL_DATE_SDF.format(detail.getLastUpdated()));
        // values.put(CREATED_AT,
        // DateUtils.SQL_DATE_SDF.format(taskDetail.getCreatedAt()));
//	values.put(MAC_ADDRESS, taskDetail.getMacAddress());

        return myDataBase.update(IMAGES_BACKUP_TB, values, ROWID + "=" + detail.getRowId(), null);
    }

    public long insertImageBackup(ImageBackupDetail detail) {
        // TODO Auto-generated method stub
        ContentValues values = new ContentValues();
        values.put(FROM_PATH, detail.getFrom());
        values.put(TO_PATH, detail.getTo());
        values.put(TASK_STATUS, detail.getTaskStatus());
        values.put(LAST_UPDATED, DateUtils.SQL_DATE_SDF.format(detail.getLastUpdated()));
        values.put(CREATED_AT, DateUtils.SQL_DATE_SDF.format(detail.getCreatedAt()));
        values.put(MAC_ADDRESS, detail.getMacAddress());
        values.put(ID_ALBUM_TB, detail.getAlbumId());

        return myDataBase.insert(IMAGES_BACKUP_TB, null, values);
    }

    public int getBackedupPhotosCount() {
        // TODO Auto-generated method stub
        int count = 0;
        Cursor cursor = myDataBase.query(IMAGES_BACKUP_TB, null, TASK_STATUS + "=" + STATUS_COMPLETE, null, null, null, null);
        if (cursor != null) {
            count = cursor.getCount();
            cursor.close();
        }
        return count;
    }

    public void clearAll() {
        myDataBase.delete(TASK_TB,null,null);
        myDataBase.delete(ALBUM_BACKUP_TB,null,null);
        myDataBase.delete(IMAGES_BACKUP_TB,null,null);
    }
}
