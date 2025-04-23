/**
 * Programma.java
 * 	Serializable -> deve essere restituita da un metodo
 * 		remoto.
 * 	Costruttore = inizializza (a "") tutto il programma.
 * 	Stampa = metodo della classe di appoggio per visualizzare il programma.
 * 	Registra = metodo per registrare un nome in una  sessione. Ritorna 0 se OK.
 */

import java.io.Serializable;

public class Stanza implements Serializable {

  final int K = 100;
  public String nomeStanza;
  public String stato;
  public String stanza[] = new String[K];

  public Stanza() {
    for (int i = 0; i < K; i++) stanza[i] = "L";
    stato="L";
    nomeStanza="L";
  }

  public synchronized boolean elimina_utente(String nome) {
    System.out.println("Stanza: eliminazione di " + nome);
    boolean eliminato = false;
    for (int i = 0; i < K; i++) {
      if (stanza[i].equals(nome)) {
        stanza[i] = "L";
        eliminato = true;
        break;
      }
    }
    return eliminato;
  }

  public void stampaStanza() {
    System.out.println(nomeStanza);
  }
}
