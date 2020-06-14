package uem.dam.seg.whereipark.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import uem.dam.seg.whereipark.javaBean.Ubication;

public class UbicationsPersistence {

    private Context contexto;
    private UbicationsSQLiteHelper ush;

    public UbicationsPersistence(Context contexto) {
        this.contexto = contexto;
        ush = new UbicationsSQLiteHelper(contexto);
    }

    public SQLiteDatabase openReadable() {
        return ush.getReadableDatabase();
    }

    public SQLiteDatabase openWritable() {
        return ush.getWritableDatabase();
    }

    public void close(SQLiteDatabase database) {
        database.close();
    }

    public long insertUbication(Ubication ubication) {
        SQLiteDatabase database = openWritable();
        database.beginTransaction();

        //Gestionar insert
        ContentValues ubicationValues = new ContentValues();
        ubicationValues.put(UbicationContract.UbicationEntry.COLUMN_NAME, ubication.getName());
        ubicationValues.put(UbicationContract.UbicationEntry.COLUMN_DESCRIPTION, ubication.getDescription());
        ubicationValues.put(UbicationContract.UbicationEntry.COLUMN_LATITUDE, ubication.getLatitude());
        ubicationValues.put(UbicationContract.UbicationEntry.COLUMN_LONGITUDE, ubication.getLongitude());
        ubicationValues.put(UbicationContract.UbicationEntry.COLUMN_MARKER, ubication.getMarker());

        long id = database.insert(UbicationContract.UbicationEntry.TABLE_NAME, null, ubicationValues);

        if (id != -1) {
            database.setTransactionSuccessful();
        }
        database.endTransaction();
        close(database);

        return id;
    }

    public void modifyUbication(Ubication ubication) {
        SQLiteDatabase database = openWritable();
        database.beginTransaction();

        //Gestionar modificación
        ContentValues ubicationValues = new ContentValues();
        ubicationValues.put(UbicationContract.UbicationEntry.COLUMN_NAME, ubication.getName());
        ubicationValues.put(UbicationContract.UbicationEntry.COLUMN_DESCRIPTION, ubication.getDescription());
        ubicationValues.put(UbicationContract.UbicationEntry.COLUMN_LATITUDE, ubication.getLatitude());
        ubicationValues.put(UbicationContract.UbicationEntry.COLUMN_LONGITUDE, ubication.getLongitude());
        ubicationValues.put(UbicationContract.UbicationEntry.COLUMN_MARKER, ubication.getMarker());

        String [] whereArgs = {String.valueOf(ubication.getId())};
        database.update(UbicationContract.UbicationEntry.TABLE_NAME, ubicationValues,
                UbicationContract.UbicationEntry.COLUMN_ID + " = ?", whereArgs);

        database.setTransactionSuccessful();
        database.endTransaction();
        close(database);
    }

    public void deleteUbication(long idUbication) {
        SQLiteDatabase database = openWritable();
        database.beginTransaction();

        //Eliminar Ubicacion
        String [] whereArgs = {String.valueOf(idUbication)};
        database.delete(UbicationContract.UbicationEntry.TABLE_NAME,
                UbicationContract.UbicationEntry.COLUMN_ID + " = ?", whereArgs);

        database.setTransactionSuccessful();
        database.endTransaction();
        close(database);
    }

    public ArrayList<Ubication> readUbications() {
        ArrayList<Ubication> ubiactionList = new ArrayList<Ubication>();
        SQLiteDatabase database = openReadable();

        String query = "SELECT " + UbicationContract.UbicationEntry.COLUMN_ID
                + ", " + UbicationContract.UbicationEntry.COLUMN_NAME
                + ", " + UbicationContract.UbicationEntry.COLUMN_DESCRIPTION
                + ", " + UbicationContract.UbicationEntry.COLUMN_LATITUDE
                + ", " + UbicationContract.UbicationEntry.COLUMN_LONGITUDE
                + ", " + UbicationContract.UbicationEntry.COLUMN_MARKER
                + " FROM " + UbicationContract.UbicationEntry.TABLE_NAME;

        Cursor cursor = database.rawQuery(query, null);

        long id;
        String name;
        String description;
        double latitude;
        double longitude;
        int marker;
        Ubication ubication = null;
        if (cursor.moveToFirst()) {
            do {
                id = cursor.getLong(cursor.getColumnIndex(UbicationContract.UbicationEntry.COLUMN_ID));
                name = cursor.getString((cursor.getColumnIndex(UbicationContract.UbicationEntry.COLUMN_NAME)));
                description = cursor.getString((cursor.getColumnIndex(UbicationContract.UbicationEntry.COLUMN_DESCRIPTION)));
                latitude = cursor.getDouble(cursor.getColumnIndex(UbicationContract.UbicationEntry.COLUMN_LATITUDE));
                longitude = cursor.getDouble(cursor.getColumnIndex(UbicationContract.UbicationEntry.COLUMN_LONGITUDE));
                marker =  cursor.getInt(cursor.getColumnIndex(UbicationContract.UbicationEntry.COLUMN_MARKER));

                ubication = new Ubication(name, description, latitude, longitude);
                ubication.setId(id);
                ubication.setMarker(marker);

                ubiactionList.add(ubication);
            } while (cursor.moveToNext());
        }

        close(database);
        return ubiactionList;
    }
}
