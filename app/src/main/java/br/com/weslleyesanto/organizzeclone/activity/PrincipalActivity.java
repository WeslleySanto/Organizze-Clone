package br.com.weslleyesanto.organizzeclone.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import br.com.weslleyesanto.organizzeclone.R;
import br.com.weslleyesanto.organizzeclone.adapter.MovimentoAdapter;
import br.com.weslleyesanto.organizzeclone.config.ConfiguracaoFirebase;
import br.com.weslleyesanto.organizzeclone.helper.Base64Custom;
import br.com.weslleyesanto.organizzeclone.helper.DateCustom;
import br.com.weslleyesanto.organizzeclone.model.Movimentacao;
import br.com.weslleyesanto.organizzeclone.model.Usuario;

public class PrincipalActivity extends AppCompatActivity {

    private TextView textoSaudacao, textoSaldo;
    private MaterialCalendarView calendarView;
    private FirebaseAuth autenticacao       = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef   = ConfiguracaoFirebase.getFirebaseDatabase();
    private Double despesaTotal = 0.0;
    private Double receitaTotal = 0.0;
    private Double resumoUsuario = 0.0;
    private DatabaseReference usuarioRef;
    private ValueEventListener valueEventListenerUsuario;
    private ValueEventListener valueEventListenerMovimentacoes;

    private RecyclerView recyclerViewMovimentos;
    private List<Movimentacao> listaMovimentos = new ArrayList<>();
    private Movimentacao movimentacao;
    private MovimentoAdapter movimentoAdapter;
    private DatabaseReference movimentacaoRef;
    private String mesAnoSelecionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        Toolbar toolbar = findViewById(R.id.toolbar);
        //Alterando o nome do titulo da Activity, podendo ser alterado em Strings.xml
        toolbar.setTitle("Organizze");
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        textoSaudacao           = findViewById(R.id.textViewSaudacao);
        textoSaldo              = findViewById(R.id.textViewSaldo);
        calendarView            = findViewById(R.id.calendarView);
        recyclerViewMovimentos  = findViewById(R.id.recyclerViewMovimentos);

        configuraCalendarView();
        swipe();

        //Configurar um adapter
        movimentoAdapter = new MovimentoAdapter(listaMovimentos, this);

        //Configurar Recycler
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewMovimentos.setLayoutManager(layoutManager);
        recyclerViewMovimentos.setHasFixedSize(true);
        recyclerViewMovimentos.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayout.VERTICAL));
        recyclerViewMovimentos.setAdapter(movimentoAdapter);
    }//FINAL onCreate

    public void adicionarDespesa(View v){
        startActivity(new Intent(this, DespesasActivity.class));
    }

    public void adicionarReceita(View v){
        startActivity(new Intent(this, ReceitasActivity.class));
    }

    public void configuraCalendarView(){
        calendarView.setTitleMonths(DateCustom.getNomeCompletoMeses());

        CalendarDay dataAtual = calendarView.getCurrentDate();
        String mesSelecionado = String.format("%02d", dataAtual.getMonth());
        mesAnoSelecionado = String.valueOf( mesSelecionado + "" + dataAtual.getYear());

        calendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                String mesSelecionado = String.format("%02d", date.getMonth());
                mesAnoSelecionado = String.valueOf(mesSelecionado + "" + date.getYear());

                movimentacaoRef.removeEventListener(valueEventListenerMovimentacoes);
                recuperarMovimentacoes();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return super.onCreateOptionsMenu(menu);
    }//FINAL onCreateOptionsMenu

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menuSair:
                autenticacao.signOut();
                startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }//FINAL onOptionsItemSelected

    public void recuperarResumo(){
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
        usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

        valueEventListenerUsuario = usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);

                despesaTotal = usuario.getDespesaTotal();
                receitaTotal = usuario.getReceitaTotal();
                resumoUsuario = receitaTotal - despesaTotal;

                DecimalFormat decimalFormat = new DecimalFormat("0.##");
                String resultadoFormatado = decimalFormat.format(resumoUsuario);

                textoSaudacao.setText("Olá, " + usuario.getNome());
                textoSaldo.setText("R$ " + resultadoFormatado);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        usuarioRef.removeEventListener(valueEventListenerUsuario);
        movimentacaoRef.removeEventListener(valueEventListenerMovimentacoes);
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarResumo();
        recuperarMovimentacoes();
    }

    public void recuperarMovimentacoes(){
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);

        movimentacaoRef = firebaseRef.child("movimentacao")
                            .child(idUsuario)
                            .child(mesAnoSelecionado);

        valueEventListenerMovimentacoes = movimentacaoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaMovimentos.clear();

                for(DataSnapshot dados: dataSnapshot.getChildren()){
                    Movimentacao movimentacao = dados.getValue( Movimentacao.class );
                    movimentacao.setId(dados.getKey());
                    listaMovimentos.add(movimentacao);
                }

                movimentoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void swipe(){
        final ItemTouchHelper.Callback itemTouch = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.ACTION_STATE_IDLE;
                int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                return makeMovementFlags(dragFlags, swipeFlags);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                excluirMovimentacao(viewHolder);
            }
        };

        new ItemTouchHelper( itemTouch ).attachToRecyclerView(recyclerViewMovimentos);
    }

    public void excluirMovimentacao(final RecyclerView.ViewHolder viewHolder){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        //Configura AlertDialog
        alertDialog.setTitle("Excluir Movimentação da conta");
        alertDialog.setMessage("Você tem certeza que deseja realmente excluir essa movimentação da sua conta?");
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int position = viewHolder.getAdapterPosition();
                movimentacao = listaMovimentos.get(position);

                String emailUsuario = autenticacao.getCurrentUser().getEmail();
                String idUsuario = Base64Custom.codificarBase64(emailUsuario);

                movimentacaoRef = firebaseRef.child("movimentacao")
                        .child(idUsuario)
                        .child(mesAnoSelecionado);

                movimentacaoRef.child( movimentacao.getId()).removeValue();
                movimentoAdapter.notifyItemRemoved(position);
                atualizarSaldo();
            }
        });

        alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(PrincipalActivity.this, "Cancelado", Toast.LENGTH_LONG).show();
                movimentoAdapter.notifyDataSetChanged();
            }
        });

        AlertDialog alert = alertDialog.create();
        alert.show();
    }//FINAL excluirMovimentacao

    public void atualizarSaldo(){
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
        usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

        if(movimentacao.getTipo().equals("r")){
            receitaTotal = receitaTotal - movimentacao.getValor();
            usuarioRef.child("receitaTotal").setValue(receitaTotal);
        }

        if(movimentacao.getTipo().equals("d")){
            despesaTotal = despesaTotal - movimentacao.getValor();
            usuarioRef.child("despesaTotal").setValue(despesaTotal);
        }
    }
}
