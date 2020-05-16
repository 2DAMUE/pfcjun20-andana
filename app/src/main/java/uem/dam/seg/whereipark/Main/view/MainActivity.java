package uem.dam.seg.whereipark.Main.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import uem.dam.seg.whereipark.Main.fragment.MapFragment;
import uem.dam.seg.whereipark.R;
import uem.dam.seg.whereipark.SharedPreferences.PreferencesHelper;
import uem.dam.seg.whereipark.SharedPreferences.model.UbicationModel;

/**
 * @author Andrea Muñoz, Daniel Alonso, Ignacio López.
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar toolbar;
    private LinearLayout linearLayout;
    private MapFragment mapFragment;
    private UbicationModel model;
    private Context context;

    /**
     * Método sobreescrito que se ejecutará al iniciar el activity
     * @see #configureActivity()
     * @see #configureDrawer()
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configureActivity();
        configureDrawer();
    }

    /**
     * Método que se encarga de configurar el navigationDrawer y la escucha de todos sus elementos
     * @see #addLocation()
     * @see #shareLocation()
     * @see #deleteLoc()
     */
    private void configureDrawer() {
        final NavigationView navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();

                if (id == R.id.addLoc) {
                    addLocation();

                } else if (id == R.id.shareLoc) {
                    shareLocation();

                } else if (id == R.id.delete) {
                    deleteLoc();


                } else if (id == R.id.llExit) {
                    System.exit(0);
                }

                return true;
            }
        });
    }

    /**
     * Método encargado de compartir la localización
     * Recupera el modelo del SharedPreferences y en función de lo obtenido nos muestra un AlertDialog o
     * comparte la localización a través de un chooser
     * @see PreferencesHelper#loadPreferences(Context)
     * @see UbicationModel#getLatitude()
     * @see UbicationModel#getLongitude()
     * @see UbicationModel#getNotes()
     */
    private void shareLocation() {
        context = getApplicationContext();
        model = PreferencesHelper.loadPreferences(context);

        if (model.getLongitude() == 0 && model.getLatitude() == 0){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialogStyle);
            builder.setTitle(R.string.share_loc_dialog_title);
            builder.setMessage(R.string.share_loc_dialog_msg);
            builder.setPositiveButton(R.string.accept, null);
            AlertDialog dialog = builder.create();
            dialog.show();

        } else {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT, model.getNotes() + " \nhttp://maps.google.com/maps?q="
                    + model.getLatitude() + "," + model.getLongitude() + getString(R.string.signature));

            try {
                startActivity(Intent.createChooser(i, getString(R.string.share_location_text)));
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(context, R.string.share_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Método encargado de añadir un marcador en nuestra localización actual y limpiar el marcador anterior
     * @see MapFragment#clearMarker()
     * @see MapFragment#addNotes()
     */
    private void addLocation() {
        mapFragment.clearMarker();
        mapFragment.addNotes();
    }

    /**
     * Método encargado de borrar los marcadores que tengamos en el mapa
     * @see MapFragment#deleteLocation()
     */
    private void deleteLoc(){
        mapFragment.deleteLocation();
    }

    /**
     * Método que recoge todos aquellos elementos que tienen que inicializarse y referenciarse al cargar el activity
     * como las variables, los layouts, el actionBar y cargar el mapFragment en su layout correspondiente
     */
    private void configureActivity() {
        linearLayout = findViewById(R.id.llExit);
        linearLayout.setOnClickListener(this);
        drawerLayout = findViewById(R.id.dl);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        mapFragment = new MapFragment();
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.contendor, mapFragment).commit();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     *  Método sobreescrito que controla el listener del navigationDrawer
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return actionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    /**
     * Método sobreescrito que controla el listener de los elementos que no forman parte del
     * menú del navigationDrawer, en este caso el boton "Salir"
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.llExit:
                System.exit(0);
        }
    }
}
