package uem.dam.seg.whereipark.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class UbicationsSQLiteHelper extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "UBICATIONS_DB";
    static final int VERSION_DB = 9;
    static final String CREATE_TABLE_UBICATIONS = "CREATE TABLE " + UbicationContract.UbicationEntry.TABLE_NAME
            + "( " + UbicationContract.UbicationEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + UbicationContract.UbicationEntry.COLUMN_NAME + " TEXT NOT NULL,"
            + UbicationContract.UbicationEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL,"
            + UbicationContract.UbicationEntry.COLUMN_LATITUDE + " REAL NOT NULL,"
            + UbicationContract.UbicationEntry.COLUMN_LONGITUDE + " REAL NOT NULL,"
            + UbicationContract.UbicationEntry.COLUMN_MARKER + " INTEGER NOT NULL);";


    public UbicationsSQLiteHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, VERSION_DB);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_UBICATIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + UbicationContract.UbicationEntry.TABLE_NAME);
        onCreate(db);
    }
}
