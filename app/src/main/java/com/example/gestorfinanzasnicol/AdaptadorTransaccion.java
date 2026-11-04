package com.example.gestorfinanzasnicol;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class AdaptadorTransaccion extends RecyclerView.Adapter<AdaptadorTransaccion.TransaccionViewHolder> {

    private List<Transaccion> listaTransacciones;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Transaccion transaccion);
    }

    public AdaptadorTransaccion(List<Transaccion> listaTransacciones, OnItemClickListener listener) {
        this.listaTransacciones = listaTransacciones;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TransaccionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaccion, parent, false);
        return new TransaccionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransaccionViewHolder holder, int position) {
        Transaccion transaccionActual = listaTransacciones.get(position);

        // 🔹 ASIGNAR EL ID AL TEXTVIEW
        holder.tvItemId.setText("ID: " + transaccionActual.getId());

        holder.tvConcepto.setText(transaccionActual.getConcepto());
        holder.tvMonto.setText("$ " + transaccionActual.getMonto());
        holder.tvTipo.setText(transaccionActual.getTipo());

        if (transaccionActual.getTipo().equals("Ingreso")) {
            holder.tvTipo.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"));
        } else {
            holder.tvTipo.setBackgroundColor(android.graphics.Color.parseColor("#E57373"));
        }

        String urlFoto = transaccionActual.getUrlFoto();
        if (urlFoto != null && !urlFoto.isEmpty()) {
            holder.ivItemFoto.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(urlFoto)
                    .centerCrop()
                    .placeholder(R.drawable.ic_caja)
                    .error(R.drawable.ic_caja)
                    .into(holder.ivItemFoto);
        } else {
            holder.ivItemFoto.setVisibility(View.GONE);
            Glide.with(holder.itemView.getContext()).clear(holder.ivItemFoto);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(transaccionActual);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaTransacciones.size();
    }

    public static class TransaccionViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemId, tvConcepto, tvMonto, tvTipo; // 🔹 Agregamos tvItemId
        ImageView ivItemFoto;

        public TransaccionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemId = itemView.findViewById(R.id.tvItemId); // 🔹 Lo inicializamos
            tvConcepto = itemView.findViewById(R.id.tvItemConcepto);
            tvMonto = itemView.findViewById(R.id.tvItemMonto);
            tvTipo = itemView.findViewById(R.id.tvItemTipo);
            ivItemFoto = itemView.findViewById(R.id.ivItemFoto);
        }
    }
}