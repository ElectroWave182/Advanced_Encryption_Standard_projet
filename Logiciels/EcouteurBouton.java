import java.awt.event.*;

public class EcouteurBouton implements ActionListener
{
	
	// Attributs
	private Serveur s;
	private Client c;
	
	// Constructeurs
	public EcouteurBouton (Serveur s)
	{
		this.s = s;
	}
	public EcouteurBouton (Client c)
	{
		this.c = c;
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (this.s == null)
		{
			c.envoyer_message ();
		}
		else
		{
			s.envoyer_message ();
		}
	}

}