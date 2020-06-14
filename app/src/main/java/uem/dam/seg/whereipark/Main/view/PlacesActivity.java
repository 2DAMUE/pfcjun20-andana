package uem.dam.seg.whereipark.Main.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import uem.dam.seg.whereipark.R;
import uem.dam.seg.whereipark.db.UbicationsPersistence;
import uem.dam.seg.whereipark.javaBean.Ubication;
import uem.dam.seg.whereipark.rvUtils.UbicationAdapter;

public class PlacesActivity extends AppCompatActivity implements View.OnClickListener {

    private UbicationsPersistence ubicationsPersistence;
    private RecyclerView recyclerView;
    private UbicationAdapter adapter;
    private LinearLayoutManager llm;
    private Ubication ubi;
    private ArrayList<Ubication> ubicationsList;
    private ImageView back;
    private TextView tvNoData;
    private ImageView ivSad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);

        recyclerView = findViewById(R.id.recyclerView);
        back = findViewById(R.id.back);
        back.setOnClickListener(this);

        tvNoData = findViewById(R.id.tvNoData);
        ivSad = findViewById(R.id.ivSad);

        ubicationsPersistence = new UbicationsPersistence(this);
        ubicationsList = ubicationsPersistence.readUbications();

        if (!ubicationsList.isEmpty()){
            tvNoData.setVisibility(View.INVISIBLE);
            ivSad.setVisibility(View.INVISIBLE);
            configureRv();
        } else {
            tvNoData.setVisibility(View.VISIBLE);
            ivSad.setVisibility(View.VISIBLE);
        }
    }

    private void configureRv() {
        adapter = new UbicationAdapter(ubicationsList);
        llm = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(llm);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new UbicationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                ubi = ubicationsList.get(position);

                if (ubi.getMarker() == 1) {
                    ubi.setMarker(0);
                    ubicationsPersistence.modifyUbication(ubi);
                } else {
                    ubi.setMarker(1);
                    ubicationsPersistence.modifyUbication(ubi);
                }
                adapter.notifyItemChanged(position);
            }

            @Override
            public void onShareClick(int position) {
                ubi = ubicationsList.get(position);

                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, ubi.getName() + "\n" + ubi.getDescription() +  " \nhttp://maps.google.com/maps?q="
                        + ubi.getLatitude() + "," + ubi.getLongitude() + getString(R.string.signature));

                try {
                    startActivity(Intent.createChooser(i, getString(R.string.share_location_text)));
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(getApplicationContext(), R.string.share_error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onModifyClick(int position) {
                long id = ubicationsList.get(position).getId();
                ubi = ubicationsList.get(position);
                configureAlertDialog(ubi, id);
            }

            @Override
            public void onDeleteClick(int position) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(PlacesActivity.this, R.style.AlertDialogStyle);
                final int pos = position;
                builder.setTitle(R.string.delete_alert_tittle);
                builder.setMessage(R.string.delete_alert_msg);

                builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        long id = ubicationsList.get(pos).getId();
                        ubicationsPersistence.deleteUbication(id);

                        ubicationsList.remove(pos);
                        adapter.notifyItemRemoved(pos);
                        if (ubicationsList.isEmpty()){
                            recreate();
                        }
                    }
                });

                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

                Button nbutton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                nbutton.setTextColor(getResources().getColor(R.color.colorAccentDark));
            }

            @Override
            public void onCenterClick(int position) {
                ubi = ubicationsList.get(position);
                Intent returnIntent = new Intent();
                returnIntent.putExtra("ubi", ubi);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
    }

    private void configureAlertDialog(final Ubication ubication, final long idUbi) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        alertDialog.setTitle(R.string.dialog_modif_title);
        alertDialog.setMessage(R.string.dialog_modif_msg);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(40, 0, 40, 0);

        final EditText title = new EditText(this);
        title.setTextAppearance(this, R.style.EditText);
        title.setHint(R.string.title_fav_hint);
        title.setHintTextColor(getResources().getColor(R.color.colorPrimary));
        title.setMaxLines(3);
        layout.addView(title, lp);

        final EditText description = new EditText(this);
        description.setTextAppearance(this, R.style.EditText);
        description.setHint(R.string.desc_fav_hint);
        description.setHintTextColor(getResources().getColor(R.color.colorPrimary));
        description.setMaxLines(3);
        layout.addView(description, lp);

        alertDialog.setView(layout);

        alertDialog.setPositiveButton(R.string.dialog_notes_save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String tit = title.getText().toString();
                String des = description.getText().toString();

                if (!tit.isEmpty()){
                    ubication.setName(tit);
                }

                if (!des.isEmpty()){
                    ubication.setDescription(des);
                }

                ubication.setId(idUbi);
                ubicationsPersistence.modifyUbication(ubication);

                adapter.notifyDataSetChanged();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back:
                Intent i = new Intent(this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
        }
    }
}
