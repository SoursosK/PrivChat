
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocket;

class SSLConnection implements Runnable {

    //private PSCService psc;
    private ConnectionController connectionController;

    private String channel;
    private SSLSocket sock;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    private String username;
    
    private String name;
    private int report;

    public SSLConnection(ConnectionController connectionController, PSCService psc, SSLSocket sock) {

        //this.psc = psc;
        this.connectionController = connectionController;

        this.channel = "PSC";
        this.sock = sock;

        this.name = "null";
        this.report = 0;

        try {
            this.oos = new ObjectOutputStream(sock.getOutputStream());
            this.ois = new ObjectInputStream(sock.getInputStream());

            recieveObject(); 
            
            randomName();
            forwardMessage(new Message("--", "Message", "User " + this.name + " has joined the channel."));
            
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        while (!(this.channel == null)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(I2PConnection.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (!(this.channel == null)) {
                recieveObject();
            }
        }
    }

    private void forwardMessage(Message object) {
        this.connectionController.broadcastMessage(this.channel, object);
    }

    public void recieveObject() {
        try {
            handleRequest((Message) ois.readObject());
        } catch (IOException | ClassNotFoundException ex) {
            //Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            this.closeConnection();
        }
    }

    public void handleRequest(Message object) {
        if (object.tellType().equals("Exit")) {
            closeConnection();
        } else if (object.tellType().equals("Message")) {
            forwardMessage(new Message(this.name, "Message", analyzePhrase(object.tellPhrase())));
        
        } else if (object.tellType().equals("Authentication: bearerToken")) {
            if ( !this.connectionController.checkTokenValidity(object.tellName(), object.tellPhrase() ) )
                sendObject(new Message(null, "Close Connection", ""));
            else
                this.username = object.tellName();
            
        } else if (object.tellType().equals("Change Name")) {
            randomName();
        
        } else if (object.tellType().equals("Change Connection")) {
            if (object.tellPhrase().equals("I2P")) 
                sendObject(new Message(this.name, "Change Connection", this.connectionController.anounceI2Pdestination()));  
        
        } else if (object.tellType().equals("Change Channel")) {
            this.connectionController.changeChannel(channel, this);
            this.channel = "PUC";
            forwardMessage(new Message("--", "Message", "User " + this.name + " has joined the channel."));
        }
    }

    public String analyzePhrase(String phrase) {
        String[] arr = phrase.split(" ");

        for (int i = 0; i < arr.length; i++) {
            if (this.connectionController.checkVocabulary(arr[i])) {
                arr[i] = "@@@@";
                report++;
                sendObject(new Message("--", "Report", "You have " + this.report + " report(s)"));
            }
        }

        if (report >= 3) {
            this.connectionController.banUser(this.username);
            sendObject(new Message("--", "Ban", "You have been banned."));
            forwardMessage(new Message("--", "Message", "User " + this.name + "has been banned."));
            closeConnection();
        }

        return String.join(" ", arr);
    }

    public void sendObject(Message message) {//stelnei to message sto object
        try {
            oos.writeObject(message);
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void randomName() {
        String randomName = Integer.toString((int) (Math.random() * 1000000 + 1));
        this.name = randomName;
        sendObject(new Message("--", "Change Name", this.name));
    }

    public String tellYourName() {
        return this.name;
    }
    
    public void closeConnection() {
        try {
            this.oos.close();
            this.ois.close();
            this.sock.close();
            this.connectionController.removeSSLConnection(this.channel, this);
            this.channel = null;
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}//SSLConnection
