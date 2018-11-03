package d2gls_v6;

import java.lang.reflect.InvocationTargetException;

public class d2gls_v6 {

	public static void main(String [ ] args) throws InvocationTargetException, InterruptedException
	{
		//java.net.URL url = ClassLoader.getSystemResource("resources/Akara.png");
		
		
		gamelistsniffer g;		
		g=new gamelistsniffer();	
		
		g.start();
	}
	
}