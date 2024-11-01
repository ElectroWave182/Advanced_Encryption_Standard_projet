import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class Mon_Thread extends Thread{
	
	private Serveur serveur;
	public ArrayList<Socket> client_sockets;

	public Mon_Thread(Serveur server, ArrayList<Socket> liste) {
		
		this.serveur=server;
		this.client_sockets = liste;
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				this.client_sockets.add(serveur.serverSocket.accept());
				this.serveur.nouvelle_clef();
				this.serveur.update_participants();
				System.out.println("Connexion accept\u00e9e !");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}