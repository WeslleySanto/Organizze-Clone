package br.com.weslleyesanto.organizzeclone.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.weslleyesanto.organizzeclone.R;
import br.com.weslleyesanto.organizzeclone.model.Movimentacao;

public class MovimentoAdapter extends RecyclerView.Adapter<MovimentoAdapter.MyViewHolder> {

    private List<Movimentacao> listaMovimentacao;
    Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView textViewDescricao, textViewValor, textViewCategoria;
        public MyViewHolder(View itemView){
            super(itemView);
            textViewDescricao = itemView.findViewById(R.id.textAdapterTitulo);
            textViewValor     = itemView.findViewById(R.id.textAdapterValor);
            textViewCategoria = itemView.findViewById(R.id.textAdapterCategoria);
        }
    }

    public MovimentoAdapter(List<Movimentacao> lista, Context context) {
        this.listaMovimentacao = lista;
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return this.listaMovimentacao.size();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemLista = LayoutInflater
                            .from(parent.getContext())
                            .inflate(R.layout.adapter_movimentacao, parent, false);

        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Movimentacao movimento = listaMovimentacao.get(position);

        holder.textViewDescricao.setText(movimento.getDescricao());
        holder.textViewValor.setText(String.valueOf(movimento.getValor()));
        holder.textViewCategoria.setText(movimento.getCategoria());

        holder.textViewValor.setTextColor(context.getResources().getColor(R.color.colorAccentReceita));

        if (movimento.getTipo() == "d" || movimento.getTipo().equals("d")) {
            holder.textViewValor.setTextColor(context.getResources().getColor(R.color.colorAccent));
            holder.textViewValor.setText("-" + movimento.getValor());
        }
    }
}