package br.com.weslleyesanto.organizzeclone.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import br.com.weslleyesanto.organizzeclone.R;
import br.com.weslleyesanto.organizzeclone.config.ConfiguracaoFirebase;
import br.com.weslleyesanto.organizzeclone.model.Usuario;

public class LoginActivity extends AppCompatActivity {
    private EditText email, senha;
    private Button botaoEntrar;
    private Usuario usuario;
    private FirebaseAuth autenticaco;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email       = findViewById(R.id.editTextEmail);
        senha       = findViewById(R.id.editTextSenha);
        botaoEntrar = findViewById(R.id.buttonEntrar);

        botaoEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String emailDigitado    = email.getText().toString();
                String senhaDigitado    = senha.getText().toString();

                if(!emailDigitado.isEmpty()){
                    if(!senhaDigitado.isEmpty()){
                        usuario = new Usuario();
                        usuario.setEmail(emailDigitado);
                        usuario.setSenha(senhaDigitado);
                        validarLogin();
                    }else{
                        Toast.makeText(LoginActivity.this, "Preencha a senha", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(LoginActivity.this, "Preencha o email", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void validarLogin(){
        autenticaco = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticaco.signInWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    abrirTelaPrincipal();
                }else{
                    String excecao = "";
                    try {
                        throw task.getException();
                    }catch ( FirebaseAuthInvalidUserException e ){
                        excecao = "Usuário não está cadastrado";
                    }catch ( FirebaseAuthInvalidCredentialsException e ) {
                        excecao = "E-mail e/ou senha incorretos";
                    }catch (Exception e){
                        excecao = "Erro ao cadastrar usuário: " + e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText(LoginActivity.this, excecao, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void abrirTelaPrincipal(){
        startActivity(new Intent(this, PrincipalActivity.class));
        finish();
    }
}
