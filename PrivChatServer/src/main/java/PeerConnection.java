
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocket;

public class PeerConnection implements Runnable {

    private ConnectionController connectionController;
    private SSLSocket sock;
    private final ArrayList<PeerConnection> peerConnections;

    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    private String username;
    private String pseudoName;

    private PeerConnection peer;
//    private String peerIp;
//    private String peerPort;
//    private String peerCert;

    private Boolean initiator;
    private String userIp;
    private String userPort;
    private String userCert;

    PeerConnection(ConnectionController connectionController, SSLSocket sslSocket, ArrayList<PeerConnection> peerConnections) {
        this.connectionController = connectionController;
        this.sock = sslSocket;
        this.peerConnections = peerConnections;

        try {
            this.oos = new ObjectOutputStream(sock.getOutputStream());
            this.ois = new ObjectInputStream(sock.getInputStream());

            Message object = (Message) ois.readObject();
            
            if (object.tellType().equals("Authentication: bearerToken")) {
                if (!this.connectionController.checkTokenValidity(object.tellName(), object.tellPhrase())) {
                    this.closeConnection();
                    return;
                }
            }

            Message peerInfo = (Message) ois.readObject(); //username, channel, peerPseudoname
            Message userPseudoname = (Message) ois.readObject(); //pseudoname, initiator

            this.username = peerInfo.tellName();
            this.pseudoName = userPseudoname.tellPhrase();

            if (userPseudoname.tellType().equals("initiator")) {
                this.connectionController.notifyUserForPC(userPseudoname.tellPhrase(), peerInfo.tellType(), peerInfo.tellPhrase());
                this.initiator = true;

            } else if (userPseudoname.tellType().equals("non-initiator")) {
                this.peer = this.findPeer(peerInfo.tellPhrase());
                this.peer.peer = this;
                System.out.println(this.username);
                System.out.println(this.peer.username);
            }

        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(PeerConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        try {
            Thread.sleep(30000);
        } catch (InterruptedException ex) {
            Logger.getLogger(PeerConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(this.peer.tellYourPseudoname());
        foundPeer();

    }

    private void foundPeer() {
        if (this.peer != null) {
            try {
                System.out.println("ftasame1");
                //recieve user certificate
                Message connInfo = (Message) ois.readObject(); //ip port cert

                this.userIp = connInfo.tellName();
                //this.userPort = Integer.parseInt(connInfo.tellType());
                this.userPort = connInfo.tellType();
                this.userCert = connInfo.tellPhrase();
                
                System.out.println("ftasame2");
                //communicateConnInfo();
                this.peer.oos.writeObject(new Message(userIp, userPort, userCert));

                System.out.println("ftasame3");
//                Thread.sleep(5000);
//
//                if (this.initiator == true) {
//                    Message symmetricalKey = (Message) ois.readObject();
//                    this.peer.oos.writeObject(symmetricalKey);
//                }
//
//                Thread.sleep(5000);

            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(PeerConnection.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("ftasame4");
                this.closeConnection();
//            } catch (InterruptedException ex) {
//                Logger.getLogger(PeerConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        this.closeConnection();
        System.out.println("kai edw telos");
    }

//    private void communicateConnInfo() {
//        //this.peer.setConnInfo(userIp, userPort, userCert);
//        this.peer.oos.writeObject(new Message(userIp, ));
//    }
//    public void setConnInfo(String ip, int port, String cert) {
//        this.peerIp = ip;
//        this.peerPort = port;
//        this.peerCert = cert;
//    }

    public String tellYourPseudoname() {
        return this.pseudoName;
    }

    private PeerConnection findPeer(String peerPseudoname) {
        return this.peerConnections.get(this.peerConnections.size()-1);
//        System.out.println("klh8hke");
//        Iterator iterator = this.peerConnections.iterator();
//        PeerConnection a;
//        System.out.println("a1");
//        while (iterator.hasNext()) {
//            System.out.println("a2");
//            a = (PeerConnection) iterator.next();
//            
//            System.out.println(a.tellYourPseudoname());
//            if (a.tellYourPseudoname().equals(peerPseudoname)) {
//                System.out.println(peerPseudoname);
//                System.out.println("autos einai: " + a.tellYourPseudoname());
//                return a;
//            }
//        }
//        System.out.println("den ftanei");
//        closeConnection();
//        return null;
    }

    private void closeConnection() {
        try {
            this.oos.close();
            this.ois.close();
            this.sock.close();
            this.peerConnections.remove(this);
        } catch (IOException ex) {
            Logger.getLogger(PeerConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
