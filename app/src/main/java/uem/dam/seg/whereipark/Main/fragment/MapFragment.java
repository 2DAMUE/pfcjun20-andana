package uem.dam.seg.whereipark.Main.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import uem.dam.seg.whereipark.Main.view.MainActivity;
import uem.dam.seg.whereipark.R;
import uem.dam.seg.whereipark.SharedPreferences.PreferencesHelper;
import uem.dam.seg.whereipark.SharedPreferences.model.UbicationModel;
import uem.dam.seg.whereipark.db.UbicationsPersistence;
import uem.dam.seg.whereipark.javaBean.Ubication;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {
    private static final int MY_REQUEST_INT = 177;
    private static final int TIME_WAIT = 3000;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    private GoogleMap mGoogleMap;
    private MapView mMapView;
    private View mView;
    private MainActivity operative;
    private UbicationModel model;
    private UbicationsPersistence ubicationsPersistence;
    private Location currentLocation;
    private Marker parkingMarker;

    private String titleFav;
    private String descriptionFav;
    private double latitudeFav;
    private double longitudeFav;
    private String parkingTitle;

    private String currentPhotoPath;
    private File imageFile;


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
        this.ubicationsPersistence = new UbicationsPersistence(operative);
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
     * aquí se analizará qué permisos tenemos concedidos y actuará en función de estos.
     * @param googleMap
     * @see #getLastLocation()
     * @see #addParkingMarker()
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
        addParkingMarker();
        showFavorites();
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
     * Método que limpia los marcadores del mapa
     */
    public void clearMarker() {
        if (parkingMarker != null) {
            parkingMarker.remove();
        }
    }

    /**
     * Método encargado de añadir y guardar las notas, ubicación e imagen que serán añadidas al marcador a través de un editText
     * situado dentro de un AlertDialog.
     * @see #addParkingMarker()
     * @see #fetchLastLocation()
     * @see UbicationModel#getNotes()
     * @see UbicationModel#setNotes(String)
     * @see #takePicture()
     */
    public void addNotes() {
        fetchLastLocation();//ASIGNA VALORES LATITUDE, LONGITUDE Y CURRENT LOCATION GLOBAL
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(operative, R.style.AlertDialogStyle);
        alertDialog.setTitle(R.string.dialog_notes_title);
        alertDialog.setIcon(R.drawable.ic_wippy_happy_inverted);

        LinearLayout layout = new LinearLayout(operative);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(40, 0, 40, 0);

        final EditText text = new EditText(operative);
        text.setTextAppearance(operative, R.style.EditText);
        text.setHint(R.string.notes_hint);
        text.setHintTextColor(getResources().getColor(R.color.colorPrimary));
        text.setMaxLines(3);

        layout.addView(text, lp);
        alertDialog.setView(layout);

        alertDialog.setPositiveButton(R.string.dialog_notes_save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearMarker();
                // Interacción con el texto recogido
                parkingTitle = text.getText().toString();

                model.setNotes(parkingTitle);
                if (model.getNotes().isEmpty()) {
                    model.setNotes(getString(R.string.def_note));
                }

                PreferencesHelper.savePreferences(operative, model);
                addParkingMarker();
                operative.drawerLayout.closeDrawers();
            }
        });

        alertDialog.setNegativeButton(" ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                parkingTitle = text.getText().toString();

                takePicture();

                operative.drawerLayout.closeDrawers();

            }
        });

        alertDialog.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });



        AlertDialog alert = alertDialog.create();
        alert.show();

        Button neutralButton = alert.getButton(DialogInterface.BUTTON_NEUTRAL);
        neutralButton.setTextColor(getResources().getColor(R.color.colorAccentDark));

        Drawable drawable = getActivity().getResources().getDrawable(R.drawable.ic_photo_camera_black_24dp);

        Button negativeButton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        negativeButton.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);

        Button positiveButton = alert.getButton(DialogInterface.BUTTON_POSITIVE);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0,0,25,0);
        negativeButton.setLayoutParams(layoutParams);
    }

    /**
     * Método encargado de abrir la cámara del movil y enviar la informacion a traves de un intent
     */
    private void takePicture() {
        String fileName = "photo";
        File storageDirectory = operative.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try {
            imageFile = File.createTempFile(fileName, ".jpg", storageDirectory);

            currentPhotoPath = imageFile.getAbsolutePath();

            Uri imageUri = FileProvider.getUriForFile(operative, "uem.dam.seg.whereipark.fileprovider", imageFile);

            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            i.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(i, MY_CAMERA_PERMISSION_CODE);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método encargado de recibir la información de los intent y gestionarla
     * Los datos recogidos son convertidos a Bitmap, reescalados, transformados a String y almacenadas en el sharePreferences
     * @param requestCode
     * @param resultCode
     * @param data
     * @see #resizeBitmap(Bitmap)
     * @see #encodeTobase64(Bitmap)
     * @see UbicationModel#setBitmap(String)
     * @see UbicationModel#setNotes(String)
     * @see UbicationModel#getNotes()
     * @see PreferencesHelper#savePreferences(Context, UbicationModel)
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MY_CAMERA_PERMISSION_CODE && resultCode == operative.RESULT_OK) {
            clearMarker();
            final Bitmap bitmap = resizeBitmap(BitmapFactory.decodeFile(currentPhotoPath));

            String bitmapEncoded = encodeTobase64(bitmap);
            model.setBitmap(bitmapEncoded);

            model.setNotes(parkingTitle);
            if (model.getNotes().isEmpty()) {
                model.setNotes(getString(R.string.def_note));
            }

            PreferencesHelper.savePreferences(operative, model);

            imageFile.delete();

            addParkingMarker();
        }
    }

    /**
     * Método encargado de transformar a String un Bitmap recogido como parámetro
     * @param image
     * @return
     */
    public static String encodeTobase64(Bitmap image) {
        Bitmap immage = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        Log.d("Image Log:", imageEncoded);
        return imageEncoded;
    }

    /**
     * Método encargado de transformar un String en un Bitmap
     * @param input
     * @return
     */
    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    /**
     * Método encargado de reescalar un Bitmap a una resolucion más pequeña y manejable
     * @param bitmap
     * @return
     */
    public static Bitmap resizeBitmap(Bitmap bitmap) {
        Bitmap resized = Bitmap.createScaledBitmap(bitmap,(int)(bitmap.getWidth()*0.5), (int)(bitmap.getHeight()*0.5), true);
        return resized;
    }

    /**
     * Método encargado de mostrar una imagen mecogida del model que previamente tiene que decodificar
     * De no haber ninguna imagen guardada en el modelo mostrara un mensaje de aviso
     * @see #decodeBase64(String)
     * @see UbicationModel#getBitmap()
     */
    public void showPhoto() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(operative, R.style.AlertDialogStyle);

        if (model.getBitmap() != null) {
            alertDialog.setTitle(model.getNotes());
            LinearLayout layout = new LinearLayout(operative);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);

            final ImageView image = new ImageView(operative);
            image.setImageBitmap(decodeBase64(model.getBitmap()));
            layout.addView(image, lp);
            alertDialog.setView(layout);

            alertDialog.setNegativeButton(R.string.close_alert_msg, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

        } else {
            alertDialog.setTitle(R.string.no_image);
            alertDialog.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
        }

        AlertDialog alert = alertDialog.create();
        alert.show();

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
     * @see #addParkingMarker()
     * @see #showFavorites()
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mGoogleMap.setMyLocationEnabled(true);
            getLastLocation();
            this.model = PreferencesHelper.loadPreferences(operative);
            addParkingMarker();
            showFavorites();
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
    public void addParkingMarker() {
        if (model.getLongitude() != 0 && model.getLatitude() != 0) {
            parkingMarker = mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(model.getLatitude(),
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
        //model = PreferencesHelper.loadPreferences(operative);

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
                    model = PreferencesHelper.loadPreferences(operative);
                    operative.drawerLayout.closeDrawers();
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
        nbutton.setTextColor(getResources().getColor(R.color.colorAccentDark));
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

    /**
     * Método que se encarga de configurar un objeto Ubication y añadirlo a la base de datos
     * @see #fetchFavoriteLocation()
     * @see #configureAlertDialogFav()
     */
    public void addFavorites(){
        fetchFavoriteLocation();
        configureAlertDialogFav();
    }

    /**
     * Método que solicita información a través de un alertDialog y lo guarda en la base de datos
     * confirma la operación mediante un snackBar
     * @see UbicationsPersistence#insertUbication(Ubication)
     * @see #addFavoriteMarker(Ubication)
     */
    private void configureAlertDialogFav() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(operative, R.style.AlertDialogStyle);
        alertDialog.setTitle(R.string.dialog_add_title_fav);
        alertDialog.setIcon(R.drawable.ic_marker_fav);

        LinearLayout layout = new LinearLayout(operative);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(40, 0, 40, 0);
        lp.width = 600;

        final EditText title = new EditText(operative);
        title.setTextAppearance(operative, R.style.EditText);
        title.setHint(R.string.title_fav_hint);
        title.setHintTextColor(getResources().getColor(R.color.colorPrimary));
        title.setMaxLines(3);
        layout.addView(title, lp);

        final EditText description = new EditText(operative);
        description.setTextAppearance(operative, R.style.EditText);
        description.setHint(R.string.desc_fav_hint);
        description.setHintTextColor(getResources().getColor(R.color.colorPrimary));
        description.setMaxLines(3);
        layout.addView(description, lp);

        alertDialog.setView(layout);

        alertDialog.setPositiveButton(R.string.dialog_notes_save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                titleFav = title.getText().toString();
                descriptionFav = description.getText().toString();

                if (titleFav.isEmpty()) {
                    titleFav = getString(R.string.fav_title_default);
                }

                if (descriptionFav.isEmpty()) {
                    descriptionFav = getString(R.string.fav_description_default);
                }

                Ubication favorite = new Ubication(titleFav, descriptionFav, latitudeFav, longitudeFav);

                long id = ubicationsPersistence.insertUbication(favorite);
                favorite.setId(id);
                operative.drawerLayout.closeDrawers();

                if (favorite.getMarker() == 1) {
                    addFavoriteMarker(favorite);
                }

                Snackbar snackbar = Snackbar.make(getView(), R.string.toast_added_fav, Snackbar.LENGTH_LONG);
                snackbar.setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark));
                snackbar.setTextColor(getResources().getColor(R.color.colorAccent));
                snackbar.show();

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
        nbutton.setTextColor(getResources().getColor(R.color.colorAccentDark));
    }

    /**
     * Método que se encarga de recoger la posición actual para añadirsela posteriormente a los favoritos
     */
    private void fetchFavoriteLocation() {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(operative);
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    latitudeFav = (currentLocation.getLatitude());
                    longitudeFav = (currentLocation.getLongitude());
                }
            }
        });
    }

    /**
     * Método que se encarga de mostrar en el mapa los marcadores favoritos.
     * Recupera la lista de favoritos de la base de datos, la recorre, y muestra los que corresponden
     * @see UbicationsPersistence#readUbications()
     * @see #addFavoriteMarker(Ubication)
     */
    private void showFavorites(){
        ArrayList<Ubication> favoriteList = ubicationsPersistence.readUbications();
        for (Ubication ubi : favoriteList){
            if (ubi.getMarker() == 1){
                addFavoriteMarker(ubi);
            }
        }
    }

    /**
     * Método encargado de configurar un Marcador para el objeto Ubication introducido como parámetro
     * @param ubication
     */
    private void addFavoriteMarker(Ubication ubication) {
        LatLng latLng = new LatLng(ubication.getLatitude(), ubication.getLongitude());
        mGoogleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(ubication.getName())
                .icon(bitmapDescriptorFromVector(operative, R.drawable.ic_marker_fav)));
    }

    /**
     * Método encargado de centrar el mapa en el objeto Ubication introducido como parámetro
     * @param ubication
     */
    public void centerFavorite(Ubication ubication) {
        showFavorites();
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(ubication.getLatitude(), ubication.getLongitude()), 15));
    }
}
