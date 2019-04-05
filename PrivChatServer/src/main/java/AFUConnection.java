
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.i2p.client.streaming.I2PSocket;

public class AFUConnection implements Runnable {

    private final ConnectionController connectionController;
    
    private String username;
    
    private I2PSocket sock;
    private BufferedWriter bw;
    private BufferedReader br;
    private InputStream in;         //reading incoming bytes from the client
    private FileOutputStream fos;   //writting bytes to the file
    private boolean flag;
    
    public AFUConnection(I2PSocket sock, ConnectionController connectionController) {
        this.connectionController = connectionController;
        
        try {
            ObjectOutputStream oos =  new ObjectOutputStream(sock.getOutputStream());
            ObjectInputStream ois =  new ObjectInputStream(sock.getInputStream());
            
            Message object = (Message) ois.readObject();        
            if (object.tellType().equals("Authentication: bearerToken")) 
                if ( !this.connectionController.checkTokenValidity(object.tellName(), object.tellPhrase()) ){
                    oos.writeObject(new Message(null, "Close Connection", ""));  
                    this.flag = false;
                    this.closeConnection();
                    return;
                } else{
                    this.username = object.tellName();
                    this.flag = true;
                }
                    
            this.bw = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
            this.br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            this.in = sock.getInputStream();

        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AFUConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
       if(this.flag == false){
           closeConnection();
           return;
       }
        try {
            String clientIp = br.readLine();                        //λαμβάνω ip, όνομα αρχείου και μήκος αρχείου σε string
            String receivingFileName = br.readLine();
            String receivingFileLength = br.readLine();
            
            System.out.println("Receiving file " + receivingFileName + " from user with ip " + clientIp);
            
            File receivingFile = new File( System.getProperty("user.home") + "/Desktop/eem/" +receivingFileName ); //δημιουργώ το αρχείο στο desktop
            int recevingFileLengthNum = Integer.parseInt( receivingFileLength );  //μετατρέπω το μέγεθος του αρχείου σε integer
            
            byte[] bytes = new byte[8192];                      //τα byte arrays μου έχουν μέγεθος 8192
            fos = new FileOutputStream(receivingFile);          //τα byte θα περνάνε στο νεοδημιουργηθέν αρχείο
            
            int bytesReceived = 0;
            while( bytesReceived < recevingFileLengthNum ){     //όσο τα byte που έχουν εγγραφεί μικρότερα από μέγεθος αρχείου
                in.read(bytes);                
                fos.write(bytes);
                fos.flush();
                
                bytesReceived+=8192;
            }
            System.out.println("File Received!");   
            
            bw.write("File Succesfully Received!");             //στέλνω ειδοποίηση οτι ελήφθει δίχως λάθη
            bw.newLine();   
            bw.flush();
            
            
        } catch (IOException ex) {
            Logger.getLogger(AFUConnection.class.getName()).log(Level.SEVERE, null, ex);
            closeConnection();
        } finally {
            closeConnection();
        }
            
    }
    
    public void closeConnection() {
        try {
            this.bw.close();
            this.br.close();
            this.in.close();
            this.fos.close();            
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
