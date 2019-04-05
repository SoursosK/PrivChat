
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.i2p.I2PException;
import net.i2p.client.I2PSession;
import net.i2p.client.streaming.I2PServerSocket;
import net.i2p.client.streaming.I2PSocket;
import net.i2p.client.streaming.I2PSocketManager;
import net.i2p.client.streaming.I2PSocketManagerFactory;

public class AFUService implements Runnable {

    private final ExecutorService pool;
    private final ConnectionController connectionController;

    public AFUService(ExecutorService pool, ConnectionController connectionController) {
        this.pool = pool;
        this.connectionController = connectionController;
    }

    @Override
    public void run() {
        try {
            I2PSocketManager manager = I2PSocketManagerFactory.createManager();
            I2PServerSocket serverSocket = manager.getServerSocket();
            I2PSession session = manager.getSession();

            System.out.println(session.getMyDestination());
            System.out.println(session.getMyDestination().toBase64());

            ServerSocket ipAdvertizingConnection = new ServerSocket(9004);
            System.out.println("AFU-9004, Waiting....");

            while (true) {
                Socket sock = ipAdvertizingConnection.accept();
                //advertise ip-port
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
                bw.write(session.getMyDestination().toBase64());
                bw.flush();
                bw.close();
                sock.close();
                
                I2PSocket afusocket = serverSocket.accept();

                AFUConnection con = new AFUConnection(afusocket, this.connectionController);

                pool.execute(con);
            }

        } catch (I2PException ex) {
            Logger.getLogger(I2PService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AFUService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
