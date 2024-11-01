import java.io.*;
import java.net.Socket;
import java.security.*;
import java.util.*;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

public class Client {
	public Socket echoSocket;
	private PrintWriter printeur;
	private BufferedReader input;
	private MenuPrincipal f;
	public String historique;
	public int nb_connecte;
	private int nombre_depart;
	public int nombre;
	public long key;
	public boolean eteint;
	
	private SecretKey clef;
	private Cipher crypteur;
	
	
	public Client() {
		
		this.historique = "";
		this.nombre_depart = 428;
		this.key = 0;
		try {
			echoSocket = new Socket("127.0.0.1", 20800);
			this.crypteur = Cipher.getInstance("AES");
			System.out.println("La connexion a \u00e9t\u00e9 \u00e9tablie");
			this.nb_connecte = 2;
			this.nombre = genereNombre(1000);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		this.f = new MenuPrincipal(this);
		try {
			this.printeur = new PrintWriter(this.echoSocket.getOutputStream(), true);
			this.input = new BufferedReader(new InputStreamReader(this.echoSocket.getInputStream()));
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		while(true) {
			this.reception_message();
		}
	}
	
	public int genereNombre(int upperBound)
	{
		Random rng = new Random();
		return rng.nextInt(1000);
	}
	
	public void envoyer_message () {
		
		try {
			crypteur.init (1, clef);
		} catch (InvalidKeyException e1) {
			e1.printStackTrace();
		}
		
		String message = this.f.entree.getText ();
		String code = encryptage (message);
		
		
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
			this.printeur.println("message;"+code);
			
			if(this.historique.chars().filter(ch -> ch == '\n').count()>15) {
				this.historique = "Vous : "+ message + "\n";
			}
			else {
				this.historique += "Vous : "+ message + "\n";
			}
			
			this.f.l2.setText(historique);
		}
	}
	public void send_close() {
		this.printeur.println("bye");
	}
	
	public void reception_message() {
		try {
			if(this.input.ready()) {
				String texte = this.input.readLine();
				String[] list = texte.split(";");
				switch (list[0]) {
				case "message":
					crypteur.init (2, clef);
					String message = decryptage (list[2]);
					if(this.historique.chars().filter(ch -> ch == '\n').count()>15) {
						this.historique = list[1] + " : "+ message + "\n";
					}
					else {
						this.historique += list[1] + " : "+ message + "\n";
					}
					
					this.f.l2.setText(historique);
					break;
				case "nouv_clef":
					this.printeur.println("nouv_clef;" + Integer.toString(nombre_depart*this.nombre));
					this.key = this.nombre*Integer.parseInt(list[1]);
					this.clef = generate ((int) this.key);
					break;
				case "connectes":
					this.nb_connecte = Integer.parseInt(list[1]);
					this.f.l3.setText("Personnes connect\u00e9es : " + Integer.toString(nb_connecte));
					break;
				case "bye":
					this.f.setVisible(false);
					System.exit(0);
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

	
	public static void main(String[] args)
	{
		new Client();
	}

}