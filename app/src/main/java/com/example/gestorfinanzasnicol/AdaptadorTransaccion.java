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

    public AdaptadorTransaccion(List<Transaccion> listaTransacciones) {
        this.listaTransacciones = listaTransacciones;
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

        // Mostrar los datos en los TextView
        holder.tvId.setText(String.valueOf(transaccionActual.getId()));  // ← NUEVO: muestra el ID
        holder.tvConcepto.setText(transaccionActual.getConcepto());
        holder.tvMonto.setText("$ " + transaccionActual.getMonto());
        holder.tvTipo.setText(transaccionActual.getTipo());

        // Colores pastel según tipo
        if (transaccionActual.getTipo().equals("Ingreso")) {
            holder.tvTipo.setTextColor(android.graphics.Color.parseColor("#66BB6A")); // Verde pastel
            holder.tvTipo.setBackgroundColor(android.graphics.Color.parseColor("#E8F5E9")); // Fondo verde clarito
        } else {
            holder.tvTipo.setTextColor(android.graphics.Color.parseColor("#EF5350")); // Rojo pastel
            holder.tvTipo.setBackgroundColor(android.graphics.Color.parseColor("#FFEBEE")); // Fondo rojo clarito
        }
    }

    @Override
    public int getItemCount() {
        return listaTransacciones.size();
    }

    public static class TransaccionViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvConcepto, tvMonto, tvTipo;

        public TransaccionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvItemId);          // ← NUEVO
            tvConcepto = itemView.findViewById(R.id.tvItemConcepto);
            tvMonto = itemView.findViewById(R.id.tvItemMonto);
            tvTipo = itemView.findViewById(R.id.tvItemTipo);
        }
    }
}