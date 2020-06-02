package br.com.weslleyesanto.organizzeclone.model;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;

import br.com.weslleyesanto.organizzeclone.config.ConfiguracaoFirebase;
import br.com.weslleyesanto.organizzeclone.helper.Base64Custom;
import br.com.weslleyesanto.organizzeclone.helper.DateCustom;

public class Movimentacao implements Serializable {

    private double valor;
    private String data;
    private String categoria;
    private String descricao;
    private String tipo;
    private String id;

    public Movimentacao() {
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void salvar(String dataEscolhida){

        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String idUsuario = Base64Custom.codificarBase64(autenticacao.getCurrentUser().getEmail());

        String mesAno = DateCustom.mesAnoDataEscolhida(dataEscolhida);

        DatabaseReference firebase = ConfiguracaoFirebase.getFirebaseDatabase();
        firebase.child("movimentacao")
                .child(idUsuario)
                .child(mesAno)
                .push()
                .setValue(this);
    }
}
