package d2gls_v6;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
//import javax.swing.plaf.ColorUIResource;


public class window extends JFrame
{
	private static final long serialVersionUID = 1L;
	
	boolean waction=false;	//mientras haya acciones en esta clase no se pasan mas partidas a la lista
	int ttlLimit=12;
	List<Integer> oldness= new ArrayList<Integer>();
	List<Integer> ttl= new ArrayList<Integer>();
	
	List<String> read_list= new ArrayList<String>();	//lista de read
	JLabel read_label = new JLabel("<read game content>");
	
	ClipboardClass cb = new ClipboardClass();
	JPanel jpanel = (JPanel) this.getContentPane();
	
	JList<String> listaF = new JList<String>(new DefaultListModel<String>());
	JScrollPane scrollF=new JScrollPane(listaF);
	JList<String> lista = new JList<String>(new DefaultListModel<String>());
	JScrollPane scroll=new JScrollPane(lista);
	
	DefaultListModel<String> LM=new DefaultListModel<String>();	//lista model
	DefaultListModel<String> FM=new DefaultListModel<String>();	//filter model
	
	Color Fcolor=new Color(125,148,100);
	Color Lcolor=new Color(168,148,120);
	//Color Ocolor=new Color(149,100,105);

	int Fx=150,Fy=120; //tamaño de N
	int Lx=150,Ly=620; //tamaño de R
	//int Ox=150,Oy=220; //tamaño de O
	
	int Fbox_x=150,Fbox_y=20;	//tamaño de la caja de texto del filtro
	JTextArea fbox= new JTextArea(Fbox_x,Fbox_y);
	
	int posFx=2, posFy=42, posLx=2, posLy=posFy+Fy+2/*, posOx=2, posOy=posRy+Ry+2*/;	//posicion en la ventana
	
	public window()
	{
		jpanel.setLayout(null);
		jpanel.setBackground(Color.BLACK);
		
		//setResizable(false);
		//setIconImage(new ImageIcon(getClass().getResource("resources/Akara2.png")).getImage());
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    setSize(Lx+20,Fy+posFy+Ly+45);//170,835
		setTitle("D2GLS");
		setLocation(930,100);
		validate();
		setVisible(true);
		
		jpanel.add(read_label);
		//read_label.setLocation(,);
		///////read_label.setVisible(false);
		read_label.setBounds(2,2,150,20);
		read_label.setBackground(new Color(136,50,21));
		read_label.setForeground(new Color(144,104,252));
		read_label.setBorder(new LineBorder(new Color(252,228,164)));
		read_label.setFont(new Font("Palatino Linotype",Font.BOLD,13));
		
		jpanel.add(fbox);
		
		fbox.setBounds(2,22,Fbox_x,Fbox_y);
		fbox.setBackground(new Color(136,0,21));
		fbox.setForeground(new Color(244,144,152));
		fbox.setBorder(new LineBorder(new Color(252,228,164)));
		fbox.setFont(new Font("Palatino Linotype",Font.BOLD,13));
		fbox.setText("baal");
		
		listaF.setModel(FM);
		lista.setModel(LM);
		
		setListandScroll(listaF,scrollF,posFx,posFy,Fx,Fy,Fcolor);
		setListandScroll(lista,scroll,posLx,posLy,Lx,Ly,Lcolor);
		
		lista.addListSelectionListener(new ListSelectionListener() {
		    @Override
		    public void valueChanged(ListSelectionEvent e) {
		        if (!e.getValueIsAdjusting()) {
		        		if(lista.getSelectedIndex()!=-1 && !LM.isEmpty() && !LM.get(0).equals(" "))	//si da una seleccion válida Y si la lista no esta vacía Y el primer elemento no es " " (porque lo rellenamos así al inicio) entonces hace el listener
		        		{
		        			//pasamos la casilla seleccionado al clipboard		        		
				            cb.setClipboardContents(lista.getSelectedValue());
	
				            if(!read_list.get( lista.getSelectedIndex() ).equals("") ) //pone el read en el label si el read no esta vacio
				            {
				            	read_label.setText( read_list.get( lista.getSelectedIndex() ) );
				            	//read_label.setLocation( (int)(lista.indexToLocation(lista.getSelectedIndex()).getX()+posLx+5), (int)(lista.indexToLocation(lista.getSelectedIndex()).getY()+posLy-5) );
				            	read_label.setVisible(true);
				            }
		        		}
		        		lista.clearSelection();
			        }
			    }
		    });
		
		listaF.addListSelectionListener(new ListSelectionListener() {	//Listener para pasar al clipboard los elementos del filtro al pincharlos
		    @Override
		    public void valueChanged(ListSelectionEvent e2) {
		        if (!e2.getValueIsAdjusting()) {
		        		if(listaF.getSelectedIndex()!=-1 && !LM.isEmpty() && !LM.get(0).equals(" "))	//si da una seleccion válida Y si la lista no esta vacía Y el primer elemento no es " " (porque lo rellenamos así al inicio) entonces hace el listener
		        		{
		        			//pasamos la casilla seleccionado al clipboard		        		
				            cb.setClipboardContents(listaF.getSelectedValue());
		        		}
		        		listaF.clearSelection();
			        }
			    }
		    });
		
		FM.addElement(" ");
		FM.addElement(" <games filtered>");
		
		LM.addElement(" ");
		LM.addElement("Welcome to D2GLS");
		LM.addElement("D2 Game list sniffer");
		LM.addElement(" ");
		LM.addElement("waiting to receive");
		LM.addElement("game list packets...");
		
		redraw();		
	}
	
	public void setListandScroll(JList<String> JL, JScrollPane JS, int pos_x, int pos_y, int size_x, int size_y,Color colorletra)
	{
		jpanel.add(JS, BorderLayout.CENTER);
		
		//JS.getVerticalScrollBar().setUI(uiasdfasdfa);
		JL.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		//lista.setBounds(new Rectangle(0, 0, Rx, Ry));
		JL.setBackground(Color.BLACK);
		JL.setForeground(colorletra);
		JL.setBorder(new LineBorder(new Color(252,228,164)));
		JL.setFont(new Font("Palatino Linotype",Font.BOLD,13));
	    
		/*JL.addListSelectionListener(new ListSelectionListener() {
	    @Override
	    public void valueChanged(ListSelectionEvent e) {
	        if (!e.getValueIsAdjusting()) {
		            String selectedGame = JL.getSelectedValue();
		            //AQUI VA EL PASAR selectedGame al CLIPBOARD
		            cb.setClipboardContents(selectedGame);
		            //((DefaultListModel<String>)listaR.getModel()).removeAllElements();

		            if(!read_list.get( JL.getSelectedIndex() ).equals("") ) //pone el read en el label si el read no esta vacio
		            {
		            	read_label.setText( read_list.get(JL.getSelectedIndex()) ); 
		            	read_label.setLocation(JL.getLocation());
		            	//read_label.setLocation( (int)(JL.getX()+5), (int)(JL.getY()-5) );
		            	read_label.setVisible(true);
		            }
		            
		            JL.clearSelection(); //quitamos la seleccion
		        }
		    }
	    });*/

	    JS.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	    JS.setBounds(new Rectangle(pos_x, pos_y, size_x, size_y));
	    
	    
	}

	public synchronized void addGames(String[] games, String[]r)
	{
		//listaN.setListData(n);	//Ahora lo pasamos a R ya que new no recoge bien los paquetes porque directamente manda otro nuevo. usamos N como filtro
		boolean found=false;
		int pos=0;
		
		for(int i=0;i<LM.size();i++)
			if(LM.getElementAt(i).matches("[^\\d\\w\\s'_-]+"))
				LM.remove(i);
		
		for(int i=0;i<games.length;i++)
		{
			for(int j=0;j<LM.size() && !found ;j++)
			{
				if( LM.getElementAt(j).equals(games[i]) )	//si no está en la lista lo añade y le da 0 de oldness, si está le suma 1 al array de antiguedad
				{
					found=true;
					pos=j;	//guardamos su posicion de encontrado
				}
			}
			if(found)	//buscar pos del elemento y aumentar su oldness
			{
				oldness.set(pos, oldness.get(pos)+1 );	//aumentamos su antiguedad
				ttl.set(pos, ttl.get(pos)-1 );	//reducimos su ttl (si el ttl llega a 15? eliminamos la partida)
				found=false;
			}else{		//sino lo añade a la lista
				LM.addElement(games[i]);
				read_list.add(r[i]);
				oldness.add(0);
				ttl.add(0);
			}
		}
		
		//bucle para aumentar el ttl de las partidas que no han vuelto a salir (found ya viene iniciazo a false de antes)
		for(int i=0;i<LM.size();i++)
		{
			for(int j=0;j<games.length && !found;j++)
			{
				if( LM.getElementAt(i).equals(games[j]) )	//si no está en la lista lo añade y le da 0 de oldness, si está le suma 1 al array de antiguedad
					found=true;
			}
			if(!found)
				ttl.set(i, ttl.get(i)+1);
			
			found=false;
		}	
		notifyAll();
	}
	
	public synchronized void raisettl() //aumentamos el ttl de las partidas cada 10 segundos
	{
		waction=true;
		for(int i=0;i<LM.size();i++)
				ttl.set(i, ttl.get(i)+1);
		waction=false;
		notifyAll();
	}
	
	public synchronized void orderList()
	{
        int i, j, aux, auxttl;
        String auxS,auxR;
        for(i=0;i<oldness.size()-1;i++)
             for(j=0;j<oldness.size()-i-1;j++)
                  if(oldness.get(j+1)<oldness.get(j))
                  {
                     aux=oldness.get(j+1);
                     auxttl=ttl.get(j+1);
                     auxS=LM.get(j+1);
                     auxR=read_list.get(j+1);
                     
                     oldness.set(j+1,oldness.get(j));
                     ttl.set(j+1,ttl.get(j));
                     LM.set(j+1,LM.get(j));
                     read_list.set(j+1,read_list.get(j));
                     
                     oldness.set(j,aux);
                     ttl.set(j,auxttl);
                     LM.set(j,auxS);
                     read_list.set(j,auxR);
                  }
        notifyAll();
	}
	
	public synchronized void clearLists()
	{
		LM.removeAllElements();
		FM.removeAllElements();
		notifyAll();
	}
	
	public synchronized void redraw()
	{
		jpanel.revalidate();
		jpanel.repaint();
	}
	
	public synchronized void clearOld()
	{
		waction=true;
		for(int i=0;i<LM.size();i++)
			if(ttl.get(i)>ttlLimit)
			{
				LM.remove(i);
				oldness.remove(i);
				ttl.remove(i);
				read_list.remove(i);
			}
		waction=false;
		notifyAll();
	}
	
	public synchronized void setFilter()	//llamar a setFilter cada segundo?
	{
		waction=true;
		String f=fbox.getText(); //lo metemos en una variable por si justo cambia el filtro
		
		FM.removeAllElements();

		if(!f.equals(""))
		{
			for(int i=0;i<LM.size();i++)
			{
				if( LM.getElementAt(i).toUpperCase().contains(f.toUpperCase()) )
						FM.addElement(LM.getElementAt(i));
			}
		}
		waction=false;
		notifyAll();
	}

}
