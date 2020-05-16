package uem.dam.seg.whereipark.Main.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import uem.dam.seg.whereipark.Main.view.MainActivity;
import uem.dam.seg.whereipark.R;
import uem.dam.seg.whereipark.SharedPreferences.PreferencesHelper;
import uem.dam.seg.whereipark.SharedPreferences.model.UbicationModel;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {
    private static final int MY_REQUEST_INT = 177;
    private final static int TIME_WAIT = 3000;

    private GoogleMap mGoogleMap;
    private MapView mMapView;
    private View mView;
    private MainActivity operative;
    private UbicationModel model;
    private Location currentLocation;

    public MapFragment() {
        // Required empty public constructor
    }

    /**
     * Método sobreescrito que se ejecuta al crearse la vista
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     * @see #configureMap()
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_map, container, false);

        if (getActivity() != null) {
            this.operative = ((MainActivity) getActivity());
        }

        this.model = new UbicationModel();
        this.configureMap();

        return mView;
    }

    /**
     * Método encargado de configurar el mapa
     */
    private void configureMap() {
        mMapView = mView.findViewById(R.id.map);
        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }
    }

    /**
     * Método sobreescrito que se ejecuta una vez la vista ha sido creada
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * Método sobreescrito que se encarga de configurar el mapa una vez esté listo
     * aquí se analizará qué permisos tenemos concedidos y actuará en función de éstos.
     * @param googleMap
     * @see #getLastLocation()
     * @see #addSavedMarker()
     * @see PreferencesHelper#loadPreferences(Context)
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        if (ActivityCompat.checkSelfPermission(operative, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_REQUEST_INT);
            }

            return;

        } else {
            mGoogleMap.setMyLocationEnabled(true);
            getLastLocation();
        }
        mGoogleMap.getUiSettings().setCompassEnabled(false);

        this.model = PreferencesHelper.loadPreferences(operative);
        addSavedMarker();
    }

    /**
     * Método encargado de recuperar y guardar la localización actual en la que nos encontramos a través de fusedLocationProviderClient
     * @see UbicationModel#setLatitude(double)
     * @see UbicationModel#setLongitude(double)
     */
    private void fetchLastLocation() {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(operative);
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    model.setLatitude(currentLocation.getLatitude());
                    model.setLongitude(currentLocation.getLongitude());
                }
            }
        });
    }

    /**
     * Método encargado de añadir un marcador en el mapa y guardar las coordenadas en el sharedPreferences
     * @see PreferencesHelper#savePreferences(Context, UbicationModel)
     * @see UbicationModel#getLatitude()
     * @see UbicationModel#getLongitude()
     * @see UbicationModel#getNotes()
     * @see #bitmapDescriptorFromVector(Context, int)
     */
    private void addMarkerMap() {
        Toast.makeText(this.operative, R.string.toast_saved_marker, Toast.LENGTH_SHORT).show();

        LatLng latLng = new LatLng(model.getLatitude(), model.getLongitude());
        mGoogleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(model.getNotes())
                .icon(bitmapDescriptorFromVector(operative, R.drawable.ic_marker_car)));

        // AÑADIMOS EL MARCADOR Y GUARDAMOS LA UBICACION EN EL SHAREDPREFERENCES; PARA ESO UTILIZAMOS LA NUEVA CLASE UBICATIONMODEL ;D
        PreferencesHelper.savePreferences(operative, model);
    }

    /**
     * Método que limpia los marcadores del mapa
     */
    public void clearMarker() {
        mGoogleMap.clear();
    }

    /**
     * Método encargado de añadir y guardar las notas que serán añadidas al marcador a través de un editText
     * situado dentro de un AlertDialog.
     * @see #addMarkerMap()
     * @see #fetchLastLocation()
     * @see UbicationModel#getNotes()
     * @see UbicationModel#setNotes(String)
     */
    public void addNotes() {
        fetchLastLocation();//ASIGNA VALORES LATITUDE, LONGITUDE Y CURRENT LOCATION GLOBAL
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(operative, R.style.AlertDialogStyle);
        alertDialog.setTitle(R.string.dialog_notes_title);
        alertDialog.setMessage(R.string.dialog_notes_msg);

        LinearLayout layout = new LinearLayout(operative);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(40, 0, 40, 0);

        final EditText text = new EditText(operative);
        text.setTextAppearance(operative, R.style.EditText);

        layout.addView(text, lp);
        alertDialog.setView(layout);

        alertDialog.setPositiveButton(R.string.dialog_notes_save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO: interaccion con el texto recogido
                String msg = text.getText().toString();
                model.setNotes(msg);
                if (model.getNotes().isEmpty()) {
                    model.setNotes(getString(R.string.def_note));
                }

                addMarkerMap();
                operative.drawerLayout.closeDrawers();
            }
        });

        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alert = alertDialog.create();
        alert.show();

        Button nbutton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        nbutton.setTextColor(getResources().getColor(R.color.orangeYellowDark));
    }

    /**
     * Método que se encarga de localizar nuestra posicion actual y posicionarnos en ella en el mapa.
     */
    public void getLastLocation() {
        FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(operative);
        try {
            locationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // GPS location can be null if GPS is switched off
                            if (location != null) {
                                if (mGoogleMap != null) {
                                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("MapDemoActivity", "Error trying to get last GPS location");
                            e.printStackTrace();
                        }
                    });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método implementado que se ejecutará solo en el momento de aceptar o recharar los permisos.
     * En función de la opción seleccionada cargará la aplicación o se cerrará
     * @param requestCode
     * @param permissions
     * @param grantResults
     * @see #getLastLocation()
     * @see PreferencesHelper#loadPreferences(Context)
     * @see #addSavedMarker()
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mGoogleMap.setMyLocationEnabled(true);
            getLastLocation();
            this.model = PreferencesHelper.loadPreferences(operative);
            addSavedMarker();
            mGoogleMap.getUiSettings().setCompassEnabled(false);
        } else {
            Toast.makeText(getActivity(), R.string.toast_trash, Toast.LENGTH_SHORT).show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    System.exit(0);
                }
            }, TIME_WAIT);
        }
    }

    /**
     * Método encargado de añadir en el mapa un marcador previamente guardado en el sharedPreferences
     * @see UbicationModel#getNotes()
     * @see UbicationModel#getLongitude()
     * @see UbicationModel#getLatitude()
     */
    public void addSavedMarker() {
        if (model.getLongitude() != 0 && model.getLatitude() != 0) {
            mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(model.getLatitude(),
                    model.getLongitude())).title(model.getNotes()).icon(bitmapDescriptorFromVector(operative, R.drawable.ic_marker_car)));
        }
    }

    /**
     * Método encargado de eliminar los marcadores y de limpiar el registro del sharedPreferences
     * la operación se confirma o cancela a través de un AlertDialog
     * @see #clearMarker()
     * @see PreferencesHelper#loadPreferences(Context)
     * @see PreferencesHelper#deletePreferences(Context)
     * @see UbicationModel#getLongitude()
     * @see UbicationModel#getLatitude()
     */
    public void deleteLocation(){
        model = PreferencesHelper.loadPreferences(operative);

        final AlertDialog.Builder builder = new AlertDialog.Builder(operative, R.style.AlertDialogStyle);

        if (model.getLatitude() == 0 && model.getLongitude() == 0 || model == null) {
            builder.setTitle(R.string.delete_no_loc_dialog_title);
            builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

        } else {
            builder.setTitle(R.string.delete_loc_dialog_title);
            builder.setMessage(R.string.delete_loc_dialog_msg);
            builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    clearMarker();
                    PreferencesHelper.deletePreferences(operative);
                    operative.drawerLayout.closeDrawers();
                    Toast.makeText(operative, R.string.delete_ok, Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        Button nbutton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        nbutton.setTextColor(getResources().getColor(R.color.orangeYellowDark));
    }

    /**
     * Método encargado de transformar un vector_asset en un bitMap para poder ser utilizado por la aplicación.
     * @param context
     * @param vectorResId
     * @return
     */
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId){
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
