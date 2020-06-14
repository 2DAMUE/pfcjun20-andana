package uem.dam.seg.whereipark.rvUtils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import uem.dam.seg.whereipark.R;
import uem.dam.seg.whereipark.javaBean.Ubication;

public class UbicationAdapter extends RecyclerView.Adapter<UbicationAdapter.UbicationVH> {

    private ArrayList <Ubication> ubicationsList;
    private OnItemClickListener itemClickListener;

    public UbicationAdapter(ArrayList<Ubication> ubicationsList) {
        this.ubicationsList = ubicationsList;
    }

    @NonNull
    @Override
    public UbicationVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.ubication_item,parent,false);
        UbicationVH uvh = new UbicationVH(v, itemClickListener);
        return uvh;
    }

    @Override
    public void onBindViewHolder(@NonNull UbicationVH holder, int position) {
        holder.bindUbication(ubicationsList.get(position));
    }

    @Override
    public int getItemCount() {
        return ubicationsList.size();
    }

    public class UbicationVH extends RecyclerView.ViewHolder {

        private TextView name;
        private TextView description;
        private ImageView share;
        private ImageView modify;
        private ImageView delete;
        private ImageView ivVisible;
        private ImageView ivGo;

        public UbicationVH(@NonNull View v, final OnItemClickListener listener) {
            super(v);
            name = v.findViewById(R.id.nameI);
            description = v.findViewById(R.id.descriptionI);
            share = v.findViewById(R.id.shareI);
            modify = v.findViewById(R.id.modifyI);
            delete = v.findViewById(R.id.deleteI);
            ivVisible = v.findViewById(R.id.ivVisible);
            ivGo = v.findViewById(R.id.ivGo);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });

            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onShareClick(position);
                        }
                    }
                }
            });

            modify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onModifyClick(position);
                        }
                    }
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onDeleteClick(position);
                        }
                    }
                }
            });

            ivGo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onCenterClick(position);
                        }
                    }
                }
            });
        }

        public void bindUbication(Ubication u){
            name.setText(u.getName());
            description.setText(u.getDescription());

            if (u.getMarker() == 1) {
                ivVisible.setImageResource(R.drawable.ic_visible);
                ivGo.setVisibility(View.VISIBLE);
                ivGo.setEnabled(true);
            } else {
                ivVisible.setImageResource(R.drawable.ic_not_visible);
                ivGo.setVisibility(View.INVISIBLE);
                ivGo.setEnabled(false);
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onShareClick(int position);
        void onModifyClick(int position);
        void onDeleteClick(int position);
        void onCenterClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        itemClickListener = listener;
    }
}
