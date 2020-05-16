package uem.dam.seg.whereipark.SplashScreen;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import uem.dam.seg.whereipark.Main.view.MainActivity;
import uem.dam.seg.whereipark.R;

/**
 * Clase splash que representa el activity de carga de la aplicación
 */
public class SplashScreen extends AppCompatActivity {

    private final static int TIME_WAIT = 1500;

    private ProgressBar pb;

    /**
     * Método sobreescrito oncreate que se ejecuta al cargar el activity
     * @param savedInstanceState
     * @see #configureView()
     * @see #openApp()
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        this.configureView();
        openApp();
    }

    /**
     * Método encargado de configurar los elementos de la vista
     */
    private void configureView() {
        pb = findViewById(R.id.progressBar);
        pb.setVisibility(View.VISIBLE);
    }

    /**
     * Método encargado de lanzar la siguiente activity después del tiempo indicado
     */
    private void openApp() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, TIME_WAIT);
    }
}
