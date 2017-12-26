package ipchangershutdown;

import java.io.File;

public class ShutDown {
	
    public ShutDown()
    {
    }
    
	public static void main(String[] args) {
		
		int i = 0;
		while(i++ < 3)
		{
			try{
				File f = new File("IPChangerShutDown.sd");
				if(f.createNewFile())
				{
					return;
				}
			}catch(Exception e){
				System.out.println(e);
			}
		}
		
		System.out.println("Failed to create Shutdown File");
        return;

	}

}
