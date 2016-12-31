package com.terramaster.dbhelper;

/**
 * Static class
 * <p/>
 * <p>
 * Contains all columns and table names which defined into database
 * </p>
 *
 * @version 1.0
 * @since 2014-10-25
 */
public class DBKeys {
    // Tables name
    public static final String TASK_TB = "task_tb";
    public static final String ALBUM_BACKUP_TB = "album_backup_tb";
    public static final String IMAGES_BACKUP_TB = "images_backup_tb";

    //Task types
    public static final int TASK_DOWNLOADING = 101;
    public static final int TASK_UPLOADING = 102;
    public static final int TASK_DELETING_FILE = 103;
    public static final int TASK_DELETING_DIRECTORY = 104;
    public static final int TASK_RENAMEING = 105;

    //Task status
    public static final int STATUS_WAITING = 201;
    public static final int STATUS_RUNNING = 202;
    public static final int STATUS_PAUSED = 203;
    public static final int STATUS_ERROR = 204;
    public static final int STATUS_COMPLETE = 205;
    public static final int STATUS_SKIPED = 206;

    //Backup mode
    public static final int MODE_BACKUP_ALL = 301;
    public static final int MODE_BACKUP_ONLY_NEW = 302;

    // ID column name
    public static final String ROWID = "rowId";
    public static final String ID_ALBUM_TB = "id_album_tb";
    // Other column name
    public static final String FROM_PATH = "from_path";
    public static final String TO_PATH = "to_path";
    public static final String TASK_PROCESS = "task_process";
    public static final String TASK_FILESIZE = "task_filesize";
    public static final String TASK_TYPE = "task_type";
    public static final String TASK_STATUS = "task_status";
    public static final String LAST_UPDATED = "last_updated";
    public static final String CREATED_AT = "created_at";
    public static final String MAC_ADDRESS = "mac_address";

    public static final String NO_BACKUP_PHOTOS = "no_backup_photos";
    public static final String BACKUP_MODE = "backup_mode";
    public static final String IS_ENABLED = "is_enabled";


}
