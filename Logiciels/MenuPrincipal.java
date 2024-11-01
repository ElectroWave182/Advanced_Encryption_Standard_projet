import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


public class MenuPrincipal extends JFrame implements WindowListener, KeyListener{
	
	private static final long serialVersionUID = 1L;
	private JButton b;
	private JLabel l1;
	public JLabel l3;
	public JTextArea l2;
	public JTextField entree;
	public Serveur s;
	public Client c;
	
	
	// Constructeurs
	public MenuPrincipal(Client c)
	{
		super("Client");
		this.c = c;
		
		this.instancie_fenetre();
	}
	
	public MenuPrincipal(Serveur s)
	{
		super("Serveur");
		this.s = s;
		
		this.instancie_fenetre();
	}

	
	public void instancie_fenetre ()
	{
		this.setSize(400,800);
		this.setLayout(new BorderLayout());
		JPanel p1 = new JPanel(new GridLayout(2,1));
		JPanel p2 = new JPanel(new GridLayout(3,1));
		JPanel p3 = new JPanel(new GridLayout(2,1));
		this.l1 = new JLabel("Historique des messages", SwingConstants.CENTER);
		this.l2 = new JTextArea();
		this.l2.setBackground(this.l1.getBackground());
		this.l3 = new JLabel("Personnes connect\u00e9es : 2", SwingConstants.CENTER);
		this.l2.setEditable(false);
		this.entree = new JTextField();
		this.b = new JButton("Envoyer");
		if (this.s == null)
		{
			this.b.addActionListener(new EcouteurBouton(c));
		}
		else
		{
			this.b.addActionListener(new EcouteurBouton(s));
		}
		this.addWindowListener(this);
		this.entree.addKeyListener(this);
		p1.add(l1);
		p1.add(l3);
		p2.add(l2);
		p2.add(entree);
		p2.add(b);
		this.add(p2, BorderLayout.CENTER);
		this.add(p1, BorderLayout.NORTH);
		this.add(p3, BorderLayout.WEST);
		this.setVisible(true);
		this.setResizable(false);
	}
	
	

	@Override
	public void windowOpened(WindowEvent e) {}

	@Override
	public void windowClosing(WindowEvent e)
	{
		if(this.c == null) {
            this.s.send_close();
        }
        else {
            this.c.send_close();
        }
		System.exit(0);
	}

	@Override
	public void windowClosed(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowDeactivated(WindowEvent e) {}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e)
	{
		if (e.getKeyCode () == 10)
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

	@Override
	public void keyReleased(KeyEvent e) {}

}