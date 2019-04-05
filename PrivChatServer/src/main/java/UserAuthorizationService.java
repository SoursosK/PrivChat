
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import org.springframework.security.crypto.bcrypt.BCrypt;


public class UserAuthorizationService implements Runnable {

    private final ExecutorService pool;
    private final DatabaseManager db;
    private final TokenAuth tokenAuth;
    
    public UserAuthorizationService(ExecutorService pool, TokenAuth tokenAuth) {
        this.pool = pool;
        this.db = new DatabaseManager();
        this.tokenAuth = tokenAuth;
    }

    @Override
    public void run() {
        //edw ginetai connection
        //String username = "george";
        //String password = "123";
        
        //System.out.println(BCrypt.hashpw(password, BCrypt.gensalt()));;
        
        
        SSLContext sslContext = this.createSSLContext();
        try {
            // Create server socket factory
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
             
            // Create server socket
            SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(9000);
             
            sslServerSocket.setNeedClientAuth(true);
            
            System.out.println("PSC-9000, Waiting....");

            while(true){
                SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
                
                sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());
                sslSocket.startHandshake();
                
                ObjectOutputStream oos = new ObjectOutputStream(sslSocket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(sslSocket.getInputStream());
                
                Message credentials = (Message) ois.readObject();
                
                //System.out.println("cred:" + credentials.tellType() + "  " + credentials.tellPhrase());
                
                Boolean authenticated = this.db.authenticateUser(credentials.tellType(), credentials.tellPhrase());
                        
                //System.out.println(authenticated);
                
                if(authenticated == true){
                    String bearerToken = this.tokenAuth.generateToken(credentials.tellType());
                    oos.writeObject(new Message(null, "BearerToken", bearerToken));
                }
                
                credentials = null;
                
                oos.close();
                ois.close();
                sslSocket.close();
            }

        } catch (IOException ex) {
            Logger.getLogger(PUCService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(UserAuthorizationService.class.getName()).log(Level.SEVERE, null, ex);
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
    
    
    
    public static void main(String[] args) {
        
        
//        UserAuthorizationService a = new UserAuthorizationService(null);
//        
//        String username = "george";
//        String password = "123";
//        
//        //System.out.println("user registered " + a.db.registerUser(username, password));
//        
//        Boolean x = a.db.authenticateUser(username, password);
//        
//        System.out.println("authenticated " + x);
//        String x = BCrypt.hashpw(password, BCrypt.gensalt());
//        System.out.println(x);
//        
//        System.out.println(BCrypt.checkpw(password, x));;
//        System.out.println(BCrypt.checkpw("134", x));
    }
    
}
