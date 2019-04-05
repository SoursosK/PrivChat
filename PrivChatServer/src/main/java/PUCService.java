

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PUCService implements Runnable{
    private final ExecutorService pool;
    private final ConnectionController connectionController;
    private final ChatConnections chatConnections;
    
    
    public PUCService(ExecutorService pool, ConnectionController connectionController, ChatConnections chatConnections){
        this.pool = pool;
        this.connectionController = connectionController;
        this.chatConnections = chatConnections;     
    }
    
    @Override
    public void run(){
        try {
            ServerSocket PUCss = new ServerSocket(9001);
            System.out.println("PUC-9001, Waiting....");
            
            while (true) {                
                Socket sock = PUCss.accept();  
                //sock.   checkarw an h IP einai valid
                Connection con = new Connection(this.connectionController, this, sock);
                
                this.chatConnections.addPUCConnection(con);   
                pool.execute(con);
                
            }
        } catch (IOException ex) {
            Logger.getLogger(PUCService.class.getName()).log(Level.SEVERE, null, ex);
        }   
    }
  
}//PUCService