package com.example.gestorfinanzasnicol;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

        holder.tvId.setText(transaccionActual.getId());
        holder.tvConcepto.setText(transaccionActual.getConcepto());
        holder.tvMonto.setText("$ " + transaccionActual.getMonto());

        // 🔹 SIN ICONOS: solo el texto
        holder.tvTipo.setText(transaccionActual.getTipo());

        // Colores según tipo
        if (transaccionActual.getTipo().equals("Ingreso")) {
            holder.tvTipo.setBackgroundColor(android.graphics.Color.parseColor("#81C784"));  // Verde pastel
            holder.tvTipo.setTextColor(android.graphics.Color.WHITE);
        } else {
            holder.tvTipo.setBackgroundColor(android.graphics.Color.parseColor("#E57373"));  // Rojo pastel
            holder.tvTipo.setTextColor(android.graphics.Color.WHITE);
        }

        // Click en el item
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
        TextView tvId, tvConcepto, tvMonto, tvTipo;

        public TransaccionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvItemId);
            tvConcepto = itemView.findViewById(R.id.tvItemConcepto);
            tvMonto = itemView.findViewById(R.id.tvItemMonto);
            tvTipo = itemView.findViewById(R.id.tvItemTipo);
        }
    }
}