package sal.gui.core;

import java.io.Serializable;

/**
 *
 * @author Salvatore Minasola
 */

public class Prodotto implements Serializable {
    private String codice;
    private String nome;
    private float prezzo;
    private int quantita;

    public Prodotto(String codice,String nome,float prezzo,int quantita){
        this.codice = codice;
        this.nome = nome;
        this.prezzo = prezzo;
        this.quantita = quantita;
    }
    
    

    public String getCodice() {
        return codice;
    }

    public void setCodice(String codice) {
        this.codice = codice;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getPrezzo() {
        return prezzo;
    }

    public void setPrezzo(float prezzo) {
        this.prezzo = prezzo;
    }

    public int getQuantita() {
        return quantita;
    }

    public void setQuantita(int quantita){
        this.quantita = this.quantita + quantita;
    }
    
    @Override
    public String toString(){
        return "[" + this.codice + "-" + this.nome + "-" + this.prezzo + "-" + this.quantita + "]";
    }

}
