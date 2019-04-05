
import Cipher.DiffieHellman;
import UPnP.UPnP;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.swing.JOptionPane;
import net.i2p.I2PException;
import net.i2p.client.streaming.I2PSocket;
import net.i2p.client.streaming.I2PSocketManager;
import net.i2p.client.streaming.I2PSocketManagerFactory;
import net.i2p.data.DataFormatException;
import net.i2p.data.Destination;

public class PrivChatClient extends Thread {

    private final Thread threadGUI;
    private final Thread threadreciever;

    private final NewJFrame GUI;
    private final Reciever reciever;

    private String bearerToken;
    private String username;

    private String name;
    private String channel;
    private volatile Boolean flag;

    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    private Socket socket;
    private I2PSocket i2pSocket;
    private String i2pDestination;

    public PrivChatClient() {
        this.GUI = new NewJFrame(this);
        this.threadGUI = new Thread(this.GUI);
        this.threadGUI.start();

        this.reciever = new Reciever(this);
        this.threadreciever = new Thread(this.reciever);
        this.threadreciever.start();

        this.bearerToken = null;

        this.channel = null;
        this.flag = false;

        this.socket = null;
        this.i2pDestination = null;
        this.i2pSocket = null;

        this.GUI.loginScreen();
    }

    @Override
    public void run() {
    }

    public void joinPUC() {
        if (this.bearerToken == null) {
            return;
        }
        try {
            if (this.channel == null); else if (this.channel.equals("PUC")) {
                return;
            } else if (this.channel.equals("PSC")) {
                sendObject(new Message(this.name, "Change Channel", ""));
                this.channel = "PUC";
                return;
            } else if (this.channel.equals("I2P")) {
                exitChat(true);
            }

            this.channel = "PUC";

            Socket sock = new Socket("localhost", 9001);

            this.socket = sock;
            this.oos = new ObjectOutputStream(sock.getOutputStream());
            this.ois = new ObjectInputStream(sock.getInputStream());
            this.flag = true;

            this.sendObject(new Message(this.username, "Authentication: bearerToken", this.bearerToken));
            System.out.println("Connected to PUC.");

        } catch (IOException ex) {
            System.out.println("kappa");
            closeStreams();
        }
    }

    public void joinPSC() {
        if (this.bearerToken == null) {
            return;
        }
        if (this.channel == null); else if (this.channel.equals("PSC")) {
            return;
        } else if (this.channel.equals("PUC")) {
            exitChat(false);
        } else if (this.channel.equals("I2P")) {
            exitChat(true);
            System.out.println(flag);
        }

        this.channel = "PSC";

        try {
            SSLContext sslContext = this.createSSLContext();

            // Create socket factory
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            // Create socket
            SSLSocket sslSock = (SSLSocket) sslSocketFactory.createSocket("localhost", 9002);

            sslSock.setEnabledCipherSuites(sslSock.getSupportedCipherSuites());
            sslSock.startHandshake();

            this.socket = sslSock;
            this.oos = new ObjectOutputStream(sslSock.getOutputStream());
            this.ois = new ObjectInputStream(sslSock.getInputStream());
            this.flag = true;

            this.sendObject(new Message(this.username, "Authentication: bearerToken", this.bearerToken));
            System.out.println("Connected to PSC.");

        } catch (IOException ex) {
            Logger.getLogger(PrivChatClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void enableI2P() {
        if (this.bearerToken == null) {
            return;
        }
        if (this.channel == null) {
            return;
        }

        String previousChannel = this.channel;

        sendObject(new Message(this.name, "Change Connection", "I2P"));

        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            Logger.getLogger(PrivChatClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.exitChat(true);
        //System.out.println(this.i2pDestination);

        I2PSocketManager manager = I2PSocketManagerFactory.createManager();

        Destination destination;

        try {
            destination = new Destination(this.i2pDestination);

            I2PSocket sock = manager.connect(destination);
            this.i2pDestination = null;

            this.i2pSocket = sock;
            this.oos = new ObjectOutputStream(sock.getOutputStream());
            this.ois = new ObjectInputStream(sock.getInputStream());
            this.flag = true;

        } catch (I2PException | IOException ex) {
            Logger.getLogger(PrivChatClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.sendObject(new Message(this.username, "Authentication: bearerToken", this.bearerToken));

        sendObject(new Message(this.name, "Register Channel", previousChannel));
        this.channel = "I2P";
        System.out.println("Connected via I2P");
    }

    public void uploadFileAnonymously(File chosenFile) throws DataFormatException, I2PException {
        if (this.bearerToken == null) {
            return;
        }
        
        BufferedWriter bw;
        BufferedReader br;
        InputStream in;
        OutputStream out;

        try {
            Socket sock = new Socket("localhost", 9004);
            br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            this.i2pDestination = br.readLine();
            br.close();
            sock.close();

            I2PSocketManager manager = I2PSocketManagerFactory.createManager();

            Destination destination;

            destination = new Destination(this.i2pDestination);
            //System.out.println(this.i2pDestination);

            I2PSocket afuSock = manager.connect(destination);
            this.i2pSocket = afuSock;

            this.oos = new ObjectOutputStream(sock.getOutputStream());
            this.ois = new ObjectInputStream(sock.getInputStream());
            this.sendObject(new Message(this.username, "Authentication: bearerToken", this.bearerToken));

            try {
                this.wait(5000);
            } catch (InterruptedException ex) {
                Logger.getLogger(PrivChatClient.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (this.flag == false) {

                this.i2pSocket.close();
                this.i2pSocket = null;
                this.i2pDestination = null;
                return;
            }

            bw = new BufferedWriter(new OutputStreamWriter(this.i2pSocket.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(this.i2pSocket.getInputStream()));

            String ip = InetAddress.getLocalHost().getHostAddress();        //στέλνω την ip του client
            bw.write(ip);
            bw.newLine();
            bw.flush();

            bw.write(chosenFile.getName());                                            //στέλνω το όνομα του αρχείου
            bw.newLine();
            bw.flush();

            String sendingFileLength = Long.toString(chosenFile.length());             //στέλνω το μέγεθος του αρχείου
            bw.write(sendingFileLength);
            bw.newLine();
            bw.flush();

            byte[] bytes = new byte[8192];                                              //μέγεθος Byte array που στέλνω κάθε φορά
            in = new FileInputStream(chosenFile);                           //λαμβάνω bytes από το αρχείο
            out = this.i2pSocket.getOutputStream();

            int count;
            while ((count = in.read(bytes)) > 0) {                                      //στέλνω τα bytes
                out.write(bytes, 0, count);
            }

            String answer = br.readLine();                                              //λαμβάνω επιβεβαίωση 
            if (answer.equals("File Succesfully Received!")) {
                JOptionPane.showMessageDialog(this.GUI, "The file has been sent successfully!");
            }

            bw.close();
            br.close();
            in.close();
            out.close();
            this.i2pSocket.close();
            this.i2pSocket = null;
            this.i2pDestination = null;
        } catch (IOException ex) {
            Logger.getLogger(PrivChatClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void login(String username, String password) {
        this.username = username;
        try {
            SSLContext sslContext = this.createSSLContext();

            // Create socket factory
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            // Create socket
            SSLSocket sslSock = (SSLSocket) sslSocketFactory.createSocket("localhost", 9000);

            sslSock.setEnabledCipherSuites(sslSock.getSupportedCipherSuites());
            sslSock.startHandshake();

            ObjectOutputStream oos = new ObjectOutputStream(sslSock.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(sslSock.getInputStream());

            oos.writeObject(new Message(null, username, password));
            Message token = (Message) ois.readObject();

            if (token.tellType().equals("BearerToken")) {
                this.bearerToken = token.tellPhrase();
                System.out.println(this.bearerToken);
            }

            return;

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Wrong Credentials", "Error", JOptionPane.ERROR_MESSAGE);
            this.GUI.loginScreen();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(PrivChatClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void connectWithPeer(String peerPseudoname, Boolean initiator) {
        if (this.bearerToken == null) {
            return;
        }
        System.out.println("mphka kai egw");
        try {
            SSLContext sslContext = this.createSSLContext();

            // Create socket factory
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            // Create socket
            SSLSocket sslSock = (SSLSocket) sslSocketFactory.createSocket("localhost", 9005);

            sslSock.setEnabledCipherSuites(sslSock.getSupportedCipherSuites());
            sslSock.startHandshake();

            this.socket = sslSock;
            this.oos = new ObjectOutputStream(sslSock.getOutputStream());
            this.ois = new ObjectInputStream(sslSock.getInputStream());
            System.out.println("yeah");
            
            this.sendObject(new Message(this.username, "Authentication: bearerToken", this.bearerToken));

            //username, channel, peerPseudoname
            this.sendObject(new Message(this.username, this.channel, peerPseudoname));

            System.out.println("eftasa edw");
            if (initiator == true)//pseudoname, initiator
            {
                this.sendObject(new Message(this.name, "initiator", null));
            } else {
                this.sendObject(new Message(this.name, "non-initiator", null));
            }

            FileInputStream fin = new FileInputStream("ClientCA.cer");
            CertificateFactory f = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) f.generateCertificate(fin);
            PublicKey pk = certificate.getPublicKey();

            byte[] encodedPublicKey = pk.getEncoded();
            String b64PublicKey = Base64.getEncoder().encodeToString(encodedPublicKey);

            this.sendObject(new Message("localhost", "9006", b64PublicKey));
            Message peerInfo = (Message) ois.readObject(); //peer's ip, port, publicKey
            
            System.out.println("kai edw");
            //this.closeStreams();

//            String peerb64PublicKey = peerInfo.tellPhrase();
//            
//            //create keyGenerator AES-CBC
//            KeyGenerator keyGenAES = KeyGenerator.getInstance("AES");
//            keyGenAES.init(128);                                                    //initialize the generator for key size 128
//            //create key AES-CBC
//            SecretKey key1 = keyGenAES.generateKey();
//            String encodedPayloadKey1 = Base64.getEncoder().encodeToString(key1.getEncoded());  //encoding + printing of the key in base64 format, testing purposes
//
//            //create keyGenerator HmacSHA256
//            KeyGenerator keyGenHmacSHA256 = KeyGenerator.getInstance("HmacSHA256");
//            keyGenHmacSHA256.init(128);   
//            //create key HmacSha256
//            SecretKey key2 = keyGenHmacSHA256.generateKey();
//            String encodedMACKey2 = Base64.getEncoder().encodeToString(key2.getEncoded());
            System.out.println("aa");

            if (initiator == true) {
                try {
                    ServerSocket clientSSocket = new ServerSocket(9006);
                    UPnP.openPortTCP(9006);

                    Socket sock = clientSSocket.accept();

                    clientSSocket.close();
                    UPnP.closePortTCP(9006);

                    this.socket = sock;

                } catch (IOException ex) {
                } finally {
                    UPnP.closePortTCP(9006);
                }

            } else if (initiator == false) {
                System.out.println("isws edw?");
                Socket socket = new Socket(peerInfo.tellName(), 9006);
                
                DiffieHellman dh = new DiffieHellman();
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                PrintWriter out = new PrintWriter(socket.getOutputStream());

                dh.proceedDHagreement(new BufferedReader(new InputStreamReader(socket.getInputStream())), out);
                String line;

                while ((line = in.readLine()) != null) {
                    line = dh.encrypt(line);
                    out.println(line);
                    out.flush();
                }

                socket.shutdownInput();
                socket.shutdownOutput();
                socket.close();
            }

            
            
        } catch (IOException ex) {
            this.closeStreams();
        } catch (CertificateException ex) {
            Logger.getLogger(PrivChatClient.class.getName()).log(Level.SEVERE, null, ex);
            this.closeStreams();

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(PrivChatClient.class.getName()).log(Level.SEVERE, null, ex);
            this.closeStreams();

//        } catch (NoSuchAlgorithmException ex) {
//            Logger.getLogger(PrivChatClient.class.getName()).log(Level.SEVERE, null, ex);
//            this.closeStreams();
        }

    }

    public void sendMessage(String phrase) {
        sendObject(new Message(this.name, "Message", phrase));
    }

    private void sendObject(Message message) { //stelnei to message sto object
        try {
            oos.writeObject(message);
        } catch (IOException ex) {
            //Logger.getLogger(Reciever.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void requestNewName() {
        sendObject(new Message(this.name, "Change Name", ""));
    }

    public void exitChat(Boolean clear) {
        if (clear == true) {
            this.channel = null;
        }
        sendObject(new Message(this.name, "Exit", ""));
        closeStreams();
    }

    public Boolean getChannel() {
        if (this.channel == null) {
            return false;
        } else {
            return true;
        }
    }

    public String getChannelType() {
        if (this.channel == null) {
            return "None";
        } else {
            return this.channel;
        }
    }

    public void updateChat(String message) {
        this.GUI.updateChat(message);
    }

    public ObjectInputStream getOis() {
        return this.ois;
    }

    public Boolean getFlag() {
        return this.flag;
    }

    public void setFlag(Boolean bool) {
        this.flag = bool;
    }

    public void informI2Pdestination(String i2pDestination) {
        this.i2pDestination = i2pDestination;
    }

    private void closeStreams() {
        try {
            if (this.socket == null) {
                return;
            }
            System.out.println("Streams closed");

            this.channel = null;
            this.flag = false;
            this.oos.close();
            this.ois.close();
            this.socket.close();
        } catch (IOException ex) {
            Logger.getLogger(Reciever.class.getName()).log(Level.SEVERE, null, ex);

        }
    }

    // Create and initialize the SSLContext
    private SSLContext createSSLContext() {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream("ClientKeyStore.jks"), "1234567890".toCharArray());

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
            sslContext.init(km, tm, null);

            return sslContext;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private static PrivChatClient clientInstance;

    public static void main(String[] args) {
        clientInstance = new PrivChatClient();
//        clientInstance.start();
//        clientInstance.run();
//        Thread client = new Thread(new PrivChatClient());
//        client.start();

    }
}
