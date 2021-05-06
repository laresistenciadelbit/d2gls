package d2gls_v6;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
//import java.util.Collections;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.Timer;//import java.util.Timer;

import org.jnetpcap.JBufferHandler;
import org.jnetpcap.Pcap;
import org.jnetpcap.PcapBpfProgram;
import org.jnetpcap.PcapHeader;
import org.jnetpcap.PcapIf;

//import junit.framework.TestCase;
//import junit.textui.TestRunner;

import org.jnetpcap.nio.JBuffer;

/*
 -LADER HELL:
para paquete:  04 20 en bytes 11 y 12 de datos

antes de cada partida hex code:
30 00

y despues del nombre hay un:
00

--NUEVO:
	despues del nombre hay 2x 00
	si hay solo un 00 es que esa partida tiene r(read)
	que acaba en un 00.

<>intuyo que:
	bytes 11 y 12:  04 00 <-normal  04 10 <-nightmare  04 20 <-hell
	inicio partidas: 10 00 <-no ladder 30 00 <- ladder 
 
 */
@SuppressWarnings("deprecation")
public class gamelistsniffer extends Thread
{
	private final static boolean debug=true;
	private final static int namesize=100;
	
	//private static int oldGamesCap=6*4; //limite para que borre los elementos que siguen repitiendose en OLD, uso 6*4 porque por defecto salen partidas cada 10 segundos (*6 es un minuto) y 4 porque he decidido que cada 3 minutos borre las viejas (es una estimación mala pero viable)
	private static boolean firstloop=true;
	private static int timercounter=0;
	private final static int delay = 500; //milliseconds
	private static List<String> buffergames= new ArrayList<String>();
	private static List<String> readgames= new ArrayList<String>();
	//private static List<String> list=new ArrayList<String>();
	
	private static window w;
	
	gamelistsniffer() throws InvocationTargetException, InterruptedException//constructor
	{
		//w=new window();
		
		SwingUtilities.invokeAndWait(new Runnable() {
	        @Override
	        public void run() {
	        	w=new window();
	        }
	    });
	
		
		
	      ActionListener task = new ActionListener() {
	          public synchronized void actionPerformed(ActionEvent evt) {
	        	  while(!buffergames.isEmpty() && w.LM.isEmpty())	//espera a que se haya añadido el array (y limpiado)
	        		  try {	wait();} catch (InterruptedException e) {e.printStackTrace();}
	        	  
	        	  if(!firstloop)	//SOLO si ha encontrado al menos un paquete de partidas
	        	  {
		        	  if(timercounter>delay*0.001*2*10 * 2){ //mult por 2 de nuevo ya que el timercounter tb sube cada medio segundo y cada refresh de partidas
		        		  w.raisettl();	//cada 10 segundos/paquetes subimos en 1 los TTL
		        		  timercounter=0;
		        		  //y ocultamos el label de read
		        		  w.read_label.setVisible(false);
		        		  
		        	  }
		        	  w.clearOld();	//cada 0.5s borra los antiguos (ttl>10)
		              w.setFilter();
		              //w.redraw(); //necesario?
		              timercounter++;
		              //w.lista.clearSelection();
	        	  }
	        	  notifyAll();
	          }
	      };
	      new Timer(delay, task).start();
	}
	
	public synchronized void obtiene_partidas(byte[] datos)
	{
		int pos;
		char[] game=new char[namesize];
		 for(int wtf=0;wtf<namesize;wtf++) game[wtf]=0;	//lo iniciamos a 0
		 
		boolean nomoreread=false;
		int pos_read;
		char[] read=new char[namesize];
		 for(int wtf=0;wtf<namesize;wtf++) read[wtf]=0;	//lo iniciamos a 0
		
		//List<Integer> ttl= new ArrayList<Integer>();		
		
		//if(datos.length>120) //la parte de datos del paquete tiene que ser mayor a 100 bytes si tiene partidas (normalmente ocupa entre 600 y 900 bytes) (hacemos esto para que luego al comparar pueda comparar sino da underflow)
		//{
			if(datos[54+10]==0x04 && (datos[54+11]==0x00 || datos[54+11]==0x10 || datos[54+11]==0x20 ) )	//byte 54 es donde acaba la cabecera, 04 y 20 son los identificadores de paquete de partida en los bytes 10 y 11 de datos para hell (0400para normal y 0410 para nightmare)
			{
				for(int i=54+12;i<datos.length;i++)	//54+12 es donde empiezan los nombres de partidas (54 donde empezaban los datos del paquete)
				{
					if(i+1<datos.length && (datos[i]==0x10 || datos[i]==0x30 ) && datos[i+1]==0x00)	//avanzamos hasta encontrar partida (byte 30 y 00 indican inicio de nombre de partida) (y 10 00 en no ladder) 
					{	
						i+=2; //avanzamos dos bytes porque estamos en el primer byte de linea nueva (cada partida se separa por dos bytes)
						
						pos=0;
						
						while(i<datos.length && datos[i]!=0x00)	//hasta que termine el nombre de partida (byte 00)
						{
							game[pos]=(char)datos[i];	//System.out.printf("%c",(char)datos[i]);
							pos++;
							i++;
						}
						
						//añadimos el read de la partida si lo tiene (las partidas con read tienen un solo 0x00 al terminar no 2, y acontinuacion tienen el read)
						if( (i+1)<datos.length && datos[i]==0x00 && isAlphanumeric_readchar( (char)datos[i+1] ) ) //lo primero comprobamos que no este fuera del array ya que puede haber un 00 en el ultimo byte del paquete
						{
							i++;
							pos_read=0;
							nomoreread=false;
							while(i<datos.length && datos[i]!=0x00 && !nomoreread)
							{
								if(isAlphanumeric_readchar( (char)datos[i]) )
								{
									read[pos_read]=(char)datos[i];
									i++;
									pos_read++;
								}
								else nomoreread=true;	//si ha encontrado un caracter raro no lee mas read
							}
							//if(isAlphanumeric_read((new String(read,0,pos_read))))	//si es alfanumérica
							readgames.add(new String(read,0,pos_read));	//mete la partida en la lista (el nombre va hasta la posicion pos, no más).
							//else
								//readgames.add(new String(""));	//sino añadimos un read vacío (ha leido mal el read porque no ha cogido bien el final del read o algo asi)
						}
						else readgames.add(new String(""));	//si no tiene read añadimos un read vacío
						
						
						if(isAlphanumeric((new String(game,0,pos))))	//si es alfanumérica
							buffergames.add(new String(game,0,pos));	//mete la partida en la lista (el nombre va hasta la posicion pos, no más).
						
						//System.out.println("");
					}
				}
				
				
				
				//vectoriza_partidas(buffergames);
				
				if(debug)
				{
					System.out.println("****************************\n");	
					System.out.println(buffergames);
					System.out.println(readgames);
					System.out.println(w.oldness);
					System.out.println(w.ttl);
				}
				
				if(!buffergames.isEmpty() && buffergames.size()<100) //si se han sacado partidas
				{
					while(w.waction) //bucle mientras haya acciones en la clase ventana
						try {	wait();} catch (InterruptedException e) {e.printStackTrace();}
					if(firstloop)
					{
						w.clearLists();
						w.read_label.setVisible(false);
						//w.redraw();
						firstloop=false;//ya no lo hace mas veces (solo era para limpiar la ventana la primera vez)
					}
					
					w.addGames( buffergames.toArray(new String[buffergames.size()]), readgames.toArray(new String[readgames.size()]) );
					w.orderList();
					//w.clearOld(); //metido en el timer
					w.setFilter( /*buffergames.toArray(new String[buffergames.size()])*/ );
					//w.redraw();
				}
				buffergames.clear();	//cada vez que añadimos o falla limpiamos el buffer para saber que han sido añadidos
				readgames.clear();
				
			}
		//}
		

		
	}
	
	public static boolean isAlphanumeric(String str) {
        for (int i=0; i<str.length(); i++) {
            char c = str.charAt(i);
            if (!Character.isDigit(c) && !Character.isLetter(c) && c !='\'' && c!='-' && c!='_' && c!=' ')
                return false;
        }
        return true;
    }

	/*public static boolean isAlphanumeric_read(String str) {
        for (int i=0; i<str.length(); i++) {
            char c = str.charAt(i);
            if (!Character.isDigit(c) && !Character.isLetter(c) && c !='\'' && c!='-' && c!='_' && c!=' ' && c!='!' && c!='"' && c!='$' && c!='%' && c!='&' && c!='/' && c!='(' && c!=')' && c!='=' && c!='?' && c!='^' && c!='*' && c!='<' && c!='>' && c!='|' && c!='@' && c!='#' && c!='~' && c!='+' && c!='[' && c!=']' && c!='{' && c!='}' && c!=',' && c!='.')
                return false;
        }
        return true;
    }*/
	
	public static boolean isAlphanumeric_readchar(char c) {
            if (!Character.isDigit(c) && !Character.isLetter(c) && c !='\'' && c!='-' && c!='_' && c!=' ' && c!='!' && c!='"' && c!='$' && c!='%' && c!='&' && c!='/' && c!='(' && c!=')' && c!='=' && c!='?' && c!='^' && c!='*' && c!='<' && c!='>' && c!='|' && c!='@' && c!='#' && c!='~' && c!='+' && c!='[' && c!=']' && c!='{' && c!='}' && c!=',' && c!='.')
                return false;
        return true;
    }
	

	  public void run() {  
	        List<PcapIf> alldevs = new ArrayList<PcapIf>(); // Will be filled with NICs  
	        StringBuilder errbuf = new StringBuilder(); // For any error msgs  
	  
//First get a list of devices on this system  
	        int r = Pcap.findAllDevs(alldevs, errbuf);  
	        if (r == Pcap.NOT_OK || alldevs.isEmpty()) {  
	            System.err.printf("Can't read list of devices, error is %s", errbuf  
	                .toString());  
	            return;  
	        }  
	  
	        if(debug)
	        {
	        	System.out.println("Network devices found:");    
		  
		        int i = 0;  
		        for (PcapIf device : alldevs) {
		            String description =  
		                (device.getDescription() != null) ? device.getDescription()  
		                    : "No description available";  
		            System.out.printf("#%d: %s [%s]\n", i++, device.getName(), description);  
		        }
	        }
	  
	        PcapIf device;
            if( !alldevs.get(0).getDescription().contains("loopback"))
                device = alldevs.get(0);
            else
                device = alldevs.get(1);
            
	        System.out  
	            .printf("\nChoosing '%s' on your behalf:\n",  
	                (device.getDescription() != null) ? device.getDescription()  
	                    : device.getName());  
	  
//Second we open up the selected device 
	        int snaplen = 64 * 1024;           // Capture all packets, no trucation  
	        int flags = Pcap.MODE_PROMISCUOUS; // capture all packets  
	        int timeout = 10 * 1000;           // 10 seconds in millis  
	        Pcap pcap =  
	            Pcap.openLive(device.getName(), snaplen, flags, timeout, errbuf);  
	  
	        if (pcap == null) {  
	            System.err.printf("Error while opening device for capture: "  
	                + errbuf.toString());  
	            return;  
	        }  
	  
//Third WE CREATE A FILTER:
	        
			PcapBpfProgram program = new PcapBpfProgram();
			String expression = "port 6112";
			int optimize = 0;         // 0 = false
			int netmask = 0xFFFFFE00; // 255.255.254.0 //0xFFFFFF00; para 255
			
			if (pcap.compile(program, expression, optimize, netmask) != Pcap.OK) {
				System.err.println(pcap.getErr());
				return;
			}
			
			if (pcap.setFilter(program) != Pcap.OK) {
				System.err.println(pcap.getErr());
				return;		
			}
			
	        
//Then we create a packet handler which will receive packets from the  libpcap loop.  
			JBufferHandler<String> jpacketHandler = new JBufferHandler<String>()
	        {  
	  
	            public void nextPacket(PcapHeader header, JBuffer buffer, String user)//PcapPacket packet, String user) 
	            {
	            	if(buffer.size()>120) //la parte de datos del paquete tiene que ser mayor a 100 bytes si tiene partidas (normalmente ocupa entre 600 y 900 bytes) (hacemos esto para que luego al comparar pueda comparar sino da underflow)
	            	{
	            		System.out.printf("\nReceived packet, size %s\n",  buffer.size()  );
	            		obtiene_partidas( buffer.getByteArray(0, buffer.size()) ); //la cabecera ocupa 54bytes asi que empezamos extrayendo desde ahi (lo hemos quitado porque cogia paquetes con cabeceras menores que 54bytes WTF??)
	            	}
	            }
	        };  
	        
	        pcap.loop(Pcap.LOOP_INFINITE, jpacketHandler, "hi");  

	        pcap.close();  
	    }  
	}  