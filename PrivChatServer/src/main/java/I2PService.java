
import java.util.concurrent.ExecutorService;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.i2p.I2PException;
import net.i2p.client.streaming.I2PSocket;
import net.i2p.util.I2PThread;

import net.i2p.client.I2PSession;
import net.i2p.client.streaming.I2PServerSocket;
import net.i2p.client.streaming.I2PSocketManager;
import net.i2p.client.streaming.I2PSocketManagerFactory;
import net.i2p.data.Destination;

public class I2PService implements Runnable {
    private final ExecutorService pool;
    private final ConnectionController connectionController;
    private final ChatConnections chatConnections;
            
    public I2PService(ExecutorService pool, ConnectionController connectionController, ChatConnections chatConnections) {
        this.pool = pool;
        this.connectionController = connectionController;
        this.chatConnections = chatConnections;
    }
    
    @Override
    public void run() {
        I2PServerSocket serverSocket = initializeI2Papplication();
        System.out.println("I2P-9003, Waiting....");
        
        while(true){
            try {
                I2PSocket i2psocket = serverSocket.accept();
                //System.out.println("I2P connection accepted");
                
                I2PConnection con = new I2PConnection(this.connectionController, i2psocket);   
                
                pool.execute(con);

            } catch (I2PException | ConnectException | SocketTimeoutException ex) {
                Logger.getLogger(I2PService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    //Initialize application
    public I2PServerSocket initializeI2Papplication(){
        I2PSocketManager manager = I2PSocketManagerFactory.createManager();
        I2PServerSocket serverSocket = manager.getServerSocket();
        I2PSession session = manager.getSession();
        
        System.out.println(session.getMyDestination());
        System.out.println(session.getMyDestination().toBase64());
        
        this.connectionController.initializeI2Pdestination(session.getMyDestination().toBase64());

        return serverSocket;
    }
    
}
