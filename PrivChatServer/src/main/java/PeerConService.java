
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;


public class PeerConService implements Runnable{
    private final ExecutorService pool;
    private final ConnectionController connectionController;
    private final ChatConnections chatConnections;
    private final ArrayList<PeerConnection> peerConnections;
    
    public PeerConService(ExecutorService pool, ConnectionController connectionController, ChatConnections chatConnections) {
        this.pool = pool;
        this.connectionController = connectionController;
        this.chatConnections = chatConnections;
        this.peerConnections = new ArrayList();
    }
 
    @Override
    public void run() {
        SSLContext sslContext = this.createSSLContext();
        try {
            // Create server socket factory
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
             
            // Create server socket
            SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(9005);
             
            sslServerSocket.setNeedClientAuth(true);
            
            System.out.println("PSC-9005, Waiting....");

            while(true){
                SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
                System.out.println("new connection established");
                
                sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());
                sslSocket.startHandshake();
                
                PeerConnection peerCon = new PeerConnection(this.connectionController, sslSocket, this.peerConnections); 
                
                this.peerConnections.add(peerCon);
                pool.execute(peerCon);
                
//                ObjectOutputStream oos = new ObjectOutputStream(sslSocket.getOutputStream());
//                ObjectInputStream ois = new ObjectInputStream(sslSocket.getInputStream());
//                
//                Message peerInfo = (Message) ois.readObject();
                
                
                
                
                
//                oos.close();
//                ois.close();
//                sslSocket.close();
            }

        } catch (IOException ex) {
            Logger.getLogger(PUCService.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    
    private SSLContext createSSLContext(){
        try{
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream("ServerKeyStore.jks"),"1234567890".toCharArray());
             
            // Create key manager
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, "1234567890".toCharArray());
            KeyManager[] km = keyManagerFactory.getKeyManagers();
             
            // Create trust manager
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            TrustManager[] tm = trustManagerFactory.getTrustManagers();
             
            // Initialize SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(km,  tm, null);
             
            return sslContext;
        } catch (Exception ex){
            Logger.getLogger(PSCService.class.getName()).log(Level.SEVERE, null, ex);

        }
         
        return null;
    }
}
