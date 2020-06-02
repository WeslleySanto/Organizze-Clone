package br.com.weslleyesanto.organizzeclone.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import br.com.weslleyesanto.organizzeclone.R;
import br.com.weslleyesanto.organizzeclone.config.ConfiguracaoFirebase;
import br.com.weslleyesanto.organizzeclone.helper.Base64Custom;
import br.com.weslleyesanto.organizzeclone.helper.DateCustom;
import br.com.weslleyesanto.organizzeclone.model.Movimentacao;
import br.com.weslleyesanto.organizzeclone.model.Usuario;

public class DespesasActivity extends AppCompatActivity {

    private EditText valor;
    private TextInputEditText data, categoria, descricao;
    private Movimentacao movimentacao;
    private DatabaseReference firebaseRef   = ConfiguracaoFirebase.getFirebaseDatabase();
    private FirebaseAuth autenticacao       = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private Double despesaTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_despesas);

        valor       = findViewById(R.id.editValor);
        data        = findViewById(R.id.editData);
        categoria   = findViewById(R.id.editCategoria);
        descricao   = findViewById(R.id.editDescricao);

        //Preencher o campo data com a data atual
        data.setText(DateCustom.dataAtual());

        recuperarDespesaTotal();
    }

    public void salvarDespesa(View v){
        if(validarCampos()){
            movimentacao = new Movimentacao();

            Double valorRecuperado = Double.parseDouble(valor.getText().toString());
            movimentacao.setValor(valorRecuperado);

            String dataEscolhida = data.getText().toString();
            movimentacao.setData(dataEscolhida);

            movimentacao.setCategoria(categoria.getText().toString());
            movimentacao.setDescricao(descricao.getText().toString());
            movimentacao.setTipo("d");

            Double despesaAtualizada = despesaTotal + valorRecuperado;
            atualizarDespesas(despesaAtualizada);

            movimentacao.salvar(dataEscolhida);
            finish();
        }
    }

    public Boolean validarCampos(){
        String valorDigitado        = valor.getText().toString();
        String dataDigitado         = data.getText().toString();
        String categoriaDigitado    = categoria.getText().toString();
        String descricaoDigitado    = descricao.getText().toString();

        if(valorDigitado.isEmpty()){
            Toast.makeText(DespesasActivity.this, "Valor não foi preenchido", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(dataDigitado.isEmpty()){
            Toast.makeText(DespesasActivity.this, "Data não foi preenchida", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(categoriaDigitado.isEmpty()){
            Toast.makeText(DespesasActivity.this, "Categoria não foi preenchida", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(descricaoDigitado.isEmpty()){
            Toast.makeText(DespesasActivity.this, "Descrição não foi preenchida", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void recuperarDespesaTotal(){
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario    = Base64Custom.codificarBase64(emailUsuario);
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);
                despesaTotal = usuario.getDespesaTotal();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void atualizarDespesas(Double despesa){
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

        usuarioRef.child("despesaTotal").setValue(despesa);
    }
}
