package br.com.weslleyesanto.organizzeclone.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import br.com.weslleyesanto.organizzeclone.R;
import br.com.weslleyesanto.organizzeclone.config.ConfiguracaoFirebase;
import br.com.weslleyesanto.organizzeclone.helper.Base64Custom;
import br.com.weslleyesanto.organizzeclone.model.Usuario;

public class CadastroActivity extends AppCompatActivity {

    private EditText nome, email, senha;
    private Button botaoCadastrar;
    private FirebaseAuth autenticacao;
    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        nome            = findViewById(R.id.editTextNome);
        email           = findViewById(R.id.editTextEmail);
        senha           = findViewById(R.id.editTextSenha);
        botaoCadastrar  = findViewById(R.id.buttonCadastrar);

        botaoCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nomeDigitado     = nome.getText().toString();
                String emailDigitado    = email.getText().toString();
                String senhaDigitado    = senha.getText().toString();
                
                //Validar se os campos foram preenchidos
                if(!nomeDigitado.isEmpty()){
                    if(!emailDigitado.isEmpty()){
                        if(!senhaDigitado.isEmpty()){
                            usuario = new Usuario();
                            usuario.setNome(nomeDigitado);
                            usuario.setEmail(emailDigitado);
                            usuario.setSenha(senhaDigitado);
                            cadastrarUsuario();
                        }else{
                            Toast.makeText(CadastroActivity.this, "Preencha a senha", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(CadastroActivity.this, "Preencha o email", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(CadastroActivity.this, "Preencha o nome", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void cadastrarUsuario(){
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        autenticacao.createUserWithEmailAndPassword(
            usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    String idUsuario = Base64Custom.codificarBase64(usuario.getEmail());
                    usuario.setIdUsuario(idUsuario);
                    usuario.salvar();

                    finish();
                }else{
                    String excecao = "";
                    try{
                        throw task.getException();
                    }catch ( FirebaseAuthWeakPasswordException e){
                        excecao = "Digite uma senha mais forte!";
                    }catch ( FirebaseAuthInvalidCredentialsException e){
                        excecao = "Por favor digite um e-mail válido";
                    }catch ( FirebaseAuthUserCollisionException e ){
                        excecao = "Essa conta já foi cadastrada";
                    }catch (Exception e){
                        excecao = "Erro ao cadastrar usuário: " + e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText(CadastroActivity.this, excecao, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
