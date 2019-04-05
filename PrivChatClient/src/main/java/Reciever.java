
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class Reciever implements Runnable {

    private final PrivChatClient client;
    //private volatile Boolean flag;

    public Reciever(PrivChatClient client) {
        this.client = client;
        //this.flag = false;
    }

    @Override
    public void run() {
        while (true) {
            if (this.client.getFlag()) {
                recieveObject();
            }
        }
    }

    public void recieveObject() {
        try {
            Message recievedMessage = (Message) client.getOis().readObject();

            if (recievedMessage.tellType().equals("Change Name")) {
                this.client.updateChat(recievedMessage.tellName() + ": " + "Your name is now " + recievedMessage.tellPhrase());

            } else if (recievedMessage.tellType().equals("Message")) {
                this.client.updateChat(recievedMessage.tellName() + ": " + recievedMessage.tellPhrase());

            } else if (recievedMessage.tellType().equals("Change Connection")) {
                this.client.informI2Pdestination(recievedMessage.tellPhrase());
                
            } else if (recievedMessage.tellType().equals("Close Connection")) {
                this.client.updateChat("Token is expired/invalid. Please re-login.");
                this.client.exitChat(true);   
                this.client.setFlag(false);
            
            } else if (recievedMessage.tellType().equals("PrivateConnectionRequest")) {
                System.out.println("ihaa");
                int n = JOptionPane.showConfirmDialog( null, "Would you like to connect privately with " 
                        + recievedMessage.tellPhrase() + " ?", "Private Connection Request", 
                        JOptionPane.YES_NO_OPTION);
                if (n == JOptionPane.YES_OPTION) 
                    this.client.connectWithPeer(recievedMessage.tellPhrase(), false);
                
            } else if (recievedMessage.tellType().equals("Report")) {
                this.client.updateChat(recievedMessage.tellName() + ": " + recievedMessage.tellPhrase());

            } else if (recievedMessage.tellType().equals("Ban")) {
                this.client.updateChat(recievedMessage.tellName() + ": " + recievedMessage.tellPhrase());
                this.client.exitChat(false);
            }
        } catch (IOException ex) {
            //Logger.getLogger(Reciever.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Reciever.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
