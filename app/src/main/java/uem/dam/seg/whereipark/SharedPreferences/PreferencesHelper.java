package uem.dam.seg.whereipark.SharedPreferences;

import android.content.Context;
import android.content.SharedPreferences;

import uem.dam.seg.whereipark.SharedPreferences.model.UbicationModel;

/**
 * Clase sharedPreferences que funciona como una base de datos interna a cada dispositivo
 * con funcionalidad de tipo clave-valor.
 * En ella guardaremos, cargaremos o borraremos los datos referentes a nuestro modelo.
 */
public class PreferencesHelper {

    private static final String UBICATIONS = "ubications";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String NOTES = "notes";
    private static final String IMAGE = "image";
    private static final float DEF_LAT = 0f;
    private static final float DEF_LNG = 0f;
    private static final String DEF_NOTES = "";
    private static final String DEF_IMAGE = null;

    /**
     * Guardar preferencias
     * @param context
     * @param model
     */
    public static void savePreferences(Context context, UbicationModel model) {
        SharedPreferences preferences = context.getSharedPreferences(UBICATIONS, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putFloat(LATITUDE, (float)model.getLatitude());
        editor.putFloat(LONGITUDE, (float)model.getLongitude());
        editor.putString(NOTES, model.getNotes());
        editor.putString(IMAGE, model.getBitmap());

        editor.apply();
    }

    /**
     * Cargar preferencias
     * @param context
     * @return
     */
    public static UbicationModel loadPreferences(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(UBICATIONS, context.MODE_PRIVATE);

        float fLatitude = preferences.getFloat(LATITUDE, DEF_LAT);
        float fLongitude = preferences.getFloat(LONGITUDE, DEF_LNG);
        String sMsg = preferences.getString(NOTES, DEF_NOTES);
        String sBitmap = preferences.getString(IMAGE, DEF_IMAGE);

        return new UbicationModel(fLatitude, fLongitude, sMsg, sBitmap);
    }

    /**
     * Borrar preferencias
     * @param context
     */
    public static void deletePreferences(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(UBICATIONS, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }
}
