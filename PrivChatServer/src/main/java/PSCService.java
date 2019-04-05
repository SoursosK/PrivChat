

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
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

public class PSCService implements Runnable{
    private final ExecutorService pool;
    private final ConnectionController connectionController;
    private final ChatConnections chatConnections;
 
    
    public PSCService(ExecutorService pool,ConnectionController connectionController, ChatConnections chatConnections){
        this.pool = pool;
        this.connectionController = connectionController;
        this.chatConnections = chatConnections;
    }
    
    @Override
    public void run(){
        SSLContext sslContext = this.createSSLContext();
        try {
            // Create server socket factory
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
             
            // Create server socket
            SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(9002);
            
            sslServerSocket.setNeedClientAuth(true);
            
            System.out.println("PSC-9002, Waiting....");

            while(true){
                SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
                
                sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());
                sslSocket.startHandshake();
                
                SSLConnection con = new SSLConnection(this.connectionController, this, sslSocket); 
                
                this.chatConnections.addPSCSSLConnection(con);
                pool.execute(con);
            }

        } catch (IOException ex) {
            Logger.getLogger(PUCService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Create and initialize the SSLContext
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
    
}//PUCService


//private SSLContext createSSLContext(){
//        try{
//            KeyStore keyStore = KeyStore.getInstance("JKS");
//            keyStore.load(new FileInputStream("A2KeyStore.jks"),"1234567890".toCharArray());
//             
//            // Create key manager
//            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
//            keyManagerFactory.init(keyStore, "1234567890".toCharArray());
//            KeyManager[] km = keyManagerFactory.getKeyManagers();
//             
//            // Create trust manager
//            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
//            trustManagerFactory.init(keyStore);
//            TrustManager[] tm = trustManagerFactory.getTrustManagers();
//             
//            // Initialize SSLContext
//            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
//            sslContext.init(km,  tm, null);
//             
//            return sslContext;
//        } catch (Exception ex){
//            Logger.getLogger(PSCService.class.getName()).log(Level.SEVERE, null, ex);
//
//        }
//         
//        return null;
//    }