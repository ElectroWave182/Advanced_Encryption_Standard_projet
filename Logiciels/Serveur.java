import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

public class Serveur{
	
	public ServerSocket serverSocket;
	public MenuPrincipal f;
	public String historique;
	public ArrayList<Socket> client_sockets;
	private int nombre_depart;
	public long current_key;
	public ArrayList<SecretKey> liste_cles;
	private int nombre_genere;
	public int nb_connectes;
	private Cipher crypteur;
	
	
	public Serveur() {
		this.nombre_depart = 428;
		this.historique = "";
		this.current_key = this.nombre_depart;
		this.client_sockets = new ArrayList<Socket>();
		this.liste_cles = new ArrayList<SecretKey>();
		try {
			this.crypteur = Cipher.getInstance("AES");
			this.nb_connectes = 2;
			serverSocket = new ServerSocket(20800); // On utilise un port plus ou moins aléatoire
			System.out.println("Serveur cr\u00e9\u00e9 avec succ\u00e8s");
			client_sockets.add(serverSocket.accept());
			this.nouvelle_clef();
			System.out.println("Connexion accept\u00e9e");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.f = new MenuPrincipal(this);
		Mon_Thread thread = new Mon_Thread(this, client_sockets);
		thread.start();
		while(true) {//LA CONNEXION DOIT SE FAIRE AVANT D'ARRIVER ICI
			this.reception_message();
			
		}
	}
	public int genereNombre(int upperBound) {
		Random rng = new Random();
		return rng.nextInt(1000);
	}
	
	
	
	public void update_participants() {
		this.nb_connectes ++;
		this.f.l3.setText("Personnes connect\u00e9es : " + Integer.toString(this.client_sockets.size()+1));
		for(int j=0;j<this.client_sockets.size();j++) {
			PrintWriter printeur;
			try {
				printeur = new PrintWriter(this.client_sockets.get(j).getOutputStream(), true);
				printeur.println(String.format("connectes;%d", this.nb_connectes));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public void envoyer_message() {
		
	
		
		String message = this.f.entree.getText ();
		
		
		try {
		if(message.equals("bye")) {
			
			this.send_close();
			this.f.setVisible(false);
			System.exit(0);
			
		}
		else if(message.equals("")) {
			System.out.println("Impossible d'envoyer un message vide");
		}
		else {
			this.f.entree.setText("");
			for(int j=0;j<this.client_sockets.size();j++) {
					crypteur.init (1, this.liste_cles.get(j));
					String code = encryptage (message);
					PrintWriter printeur = new PrintWriter(this.client_sockets.get(j).getOutputStream(), true);
					printeur.println(String.format("message;Serveur;%s", code));
				}
			if(this.historique.chars().filter(ch -> ch == '\n').count()>15) {
				this.historique = "Vous : " + message + "\n";
			}
			else {
				this.historique += "Vous : " + message + "\n";
			}
			
			this.f.l2.setText(historique);
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void reception_message() {
		try {
			for(int i=0;i<this.client_sockets.size();i++) {
				Socket clientSocket=this.client_sockets.get(i);
				BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				if(input.ready()) {
					
					
					String texte = input.readLine();

					String[] list = texte.split(";");
					switch(list[0]) {
					case "message":
						crypteur.init (2, this.liste_cles.get(i));
						String message = decryptage (list[1]);
						if(this.historique.chars().filter(ch -> ch == '\n').count()>15) {
							this.historique = "Client"+ Integer.toString(i+1) + " : " + message + "\n";
						}
						else {
							this.historique += "Client"+ Integer.toString(i+1) + " : " + message + "\n";
						}
						this.f.l2.setText(historique);
						for(int j=0;j<this.client_sockets.size();j++) {
							if (i!=j) {
								crypteur.init (1, this.liste_cles.get(j));
								String code = encryptage (message);
								PrintWriter printeur = new PrintWriter(this.client_sockets.get(j).getOutputStream(), true);
								printeur.println(String.format("message;Client%d;%s", i+1, code));
							}
						}
						break;
					case "nouv_clef":
						this.liste_cles.add(generate(Integer.parseInt(list[1])*nombre_genere));
						break;
					case "bye":
						this.nb_connectes--;
                        this.f.l3.setText("Personnes connect\u00e9es : " + Integer.toString(this.nb_connectes));
                        for(int j=0;j<this.client_sockets.size();j++) {
                            if (i!=j) {
                                PrintWriter printeur = new PrintWriter(this.client_sockets.get(j).getOutputStream(), true);
                                printeur.println("connectes;" + this.nb_connectes);
                            }
                        }
					}
					
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private SecretKey generate (int password)
	{
        try
        {
            String chaine = "" + password;
            // Une cle de 256 bits pour l'algorithme AES-256
    		MessageDigest sha = MessageDigest.getInstance("SHA-256");
    		/*
    		* On prend les bytes du fichier. Le charset UTF-8 est important en cas
    		* d'accentuation des caracteres dans le mot de passe
    		*/
    		byte[] key = sha.digest(chaine.getBytes("UTF-8"));
    		SecretKey secret = new SecretKeySpec(key, "AES");

    		return secret;
        }
        catch (NoSuchAlgorithmException unused)
        {
            System.out.println ("Generation impossible : NoSuchAlgorithmException");
        }
        catch (UnsupportedEncodingException unused)
        {
            System.out.println ("Generation impossible : UnsupportedEncodingException");
        }

        SecretKey none = new SecretKeySpec (new byte[0], "");
        return none;
	}
	
	// Fonctions de cryptage
	public static byte[] ma_function(String s) {
        ArrayList<Byte> liste = new ArrayList<>();
        String[] tableau = s.split(",");
        for(int i=0;i<tableau.length-1;i++) {
            String ch = tableau[i].substring(1);
            liste.add(Byte.decode(ch));
        }
        String ch = tableau[tableau.length-1].substring(1, tableau[tableau.length-1].length()-1);
        liste.add(Byte.decode(ch));
        byte[] resultat = new byte[liste.size()];
        for(int i=0;i<liste.size();i++) {
            resultat[i] = liste.get(i);
        }
        return resultat;
    }
	
	
	
	public static String bytesToString (byte[] octets)
	{
		String result = "";
		
		for (byte o: octets)
		{
			result += (char) o;
		}
		
		return result;
	}
	
	private String encryptage (String in)
	{
		byte[] octets = in.getBytes ();
		
		try
		{
			octets = crypteur.doFinal(octets);
		}
		catch (IllegalBlockSizeException | BadPaddingException e)
		{
			e.printStackTrace();
		}

		return Arrays.toString (octets);
	}
	
	private String decryptage (String in)
	{
		byte[] octets = ma_function (in);
		
		try
		{
			octets = crypteur.doFinal(octets);
		}
		catch (IllegalBlockSizeException | BadPaddingException e)
		{
			e.printStackTrace();
		}

		return bytesToString (octets);
	}
	
	
	public void send_close() {
        for(int j=0;j<this.client_sockets.size();j++) {

            PrintWriter printeur;
            try {
                printeur = new PrintWriter(this.client_sockets.get(j).getOutputStream(), true);
                printeur.println("bye;" + this.nb_connectes);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    } 
	
	
	public static void main(String[] args)
	{
		new Serveur();
	}
	
	public void nouvelle_clef() {
		this.nombre_genere = genereNombre(1000);
			try {
				PrintWriter printeur = new PrintWriter(this.client_sockets.get(this.client_sockets.size()-1).getOutputStream(), true);
				printeur.println("nouv_clef;" + Integer.toString(nombre_genere*nombre_depart));
				this.current_key =nombre_genere*nombre_depart;
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	
}