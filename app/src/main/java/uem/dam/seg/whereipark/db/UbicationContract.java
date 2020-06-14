package uem.dam.seg.whereipark.db;

import android.provider.BaseColumns;

/**
 * Clase UbicationContract
 * representa los campos de la tabla UBICATIONS
 */

public class UbicationContract {

    public static abstract class UbicationEntry implements BaseColumns {
        public static final String COLUMN_ID = BaseColumns._ID;
        public static final String COLUMN_NAME = "NAME";
        public static final String COLUMN_DESCRIPTION = "DESCRIPTION";
        public static final String COLUMN_LATITUDE = "LATITUDE";
        public static final String COLUMN_LONGITUDE = "LONGITUDE";
        public static final String COLUMN_MARKER = "MARKER";
        public static final String TABLE_NAME = "UBICATIONS";
    }
}
