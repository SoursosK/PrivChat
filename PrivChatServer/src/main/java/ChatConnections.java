
import java.util.ArrayList;

public class ChatConnections {

    //PUC chat
    private final ArrayList<Connection> pucConnections;
    private final ArrayList<SSLConnection> pucSSLConnections;
    private final ArrayList<I2PConnection> pucI2PConnections;

    //PSC chat
    private final ArrayList<SSLConnection> pscSSLConnections;
    private final ArrayList<I2PConnection> pscI2PConnections;

    public ChatConnections() {
        this.pucConnections = new ArrayList<>();
        this.pucSSLConnections = new ArrayList<>();
        this.pucI2PConnections = new ArrayList<>();

        this.pscSSLConnections = new ArrayList<>();
        this.pscI2PConnections = new ArrayList<>();
    }

    //send message
    public void broadcastPUCmessage(Message object) {
        pucConnections.forEach((con) -> {
            con.sendObject(object);
        });
        pucSSLConnections.forEach((sslcon) -> {
            sslcon.sendObject(object);
        });
        pucI2PConnections.forEach((i2pcon) -> {
            i2pcon.sendObject(object);
        });
    }

    public void broadcastPSCmessage(Message object) {
        pscSSLConnections.forEach((sslcon) -> {
            sslcon.sendObject(object);
        });
        pscI2PConnections.forEach((i2pcon) -> {
            i2pcon.sendObject(object);
        });
    }

    //add connections
    //puc
    public void addPUCConnection(Connection connection) {
        this.pucConnections.add(connection);
    }

    public void addPUCSSLConnection(SSLConnection sslConnection) {
        this.pucSSLConnections.add(sslConnection);
    }

    public void addPUCI2PConnection(I2PConnection i2pConnection) {
        this.pucI2PConnections.add(i2pConnection);
    }

    //psc
    public void addPSCSSLConnection(SSLConnection sslConnection) {
        this.pscSSLConnections.add(sslConnection);
    }

    public void addPSCI2PConnection(I2PConnection i2pConnection) {
        this.pscI2PConnections.add(i2pConnection);
    }

    //remove connections
    //puc
    public void removePUCConnection(Connection connection) {
        this.pucConnections.remove(connection);
    }

    public void removePUCSSLConnection(SSLConnection sslConnection) {
        this.pucSSLConnections.remove(sslConnection);
    }

    public void removePUCI2PConnection(I2PConnection i2pConnection) {
        this.pucI2PConnections.remove(i2pConnection);
    }

    //psc
    public void removePSCSSLConnection(SSLConnection sslConnection) {
        this.pscSSLConnections.remove(sslConnection);
    }

    public void removePSCI2PConnection(I2PConnection i2pConnection) {
        this.pscI2PConnections.remove(i2pConnection);
    }

    public void findAndNotifyUser(String initiatorPseudoname, String channel, String pseudoname) {
        if (channel.equals("PUC")) {
            
            pucConnections.forEach((con) -> {
                if (con.tellYourName().equals(pseudoname)) {
                    con.sendObject(new Message(null, "PrivateConnectionRequest", initiatorPseudoname));
                    return;
                }
            });
            pucSSLConnections.forEach((sslcon) -> {
                if (sslcon.tellYourName().equals(pseudoname)) {
                    sslcon.sendObject(new Message(null, "PrivateConnectionRequest", initiatorPseudoname));
                    return;
                }
            });
            pucI2PConnections.forEach((i2pcon) -> {
                if (i2pcon.tellYourName().equals(pseudoname)) {
                    i2pcon.sendObject(new Message(null, "PrivateConnectionRequest", initiatorPseudoname));
                    return;
                };
            });
            
        } else if (channel.equals("PSC")) {
           
            pscSSLConnections.forEach((sslcon) -> {
                if (sslcon.tellYourName().equals(pseudoname)) {
                    sslcon.sendObject(new Message(null, "PrivateConnectionRequest", initiatorPseudoname));
                    return;
                }
            });
            pscI2PConnections.forEach((i2pcon) -> {
                if (i2pcon.tellYourName().equals(pseudoname)) {
                    i2pcon.sendObject(new Message(null, "PrivateConnectionRequest", initiatorPseudoname));
                    return;
                };
            });
            
        }//else if
    }//findAndNotifyUser

}//ChatConnections
