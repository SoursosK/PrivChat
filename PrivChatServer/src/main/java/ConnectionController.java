
import java.util.HashSet;


public class ConnectionController {
    private final ChatConnections chatConnections;
    private final TokenAuth tokenAuth;
    private String i2pDestination;
    
    
    public ConnectionController(ChatConnections chatConnections, TokenAuth tokenAuth){
        this.chatConnections = chatConnections;
        this.tokenAuth = tokenAuth;
        this.i2pDestination = null;   
        this.bannedWords = initializeBannedWords();
    }
    
    
    public void notifyUserForPC(String initiatorPseudoname, String channel, String pseudoname){
        this.chatConnections.findAndNotifyUser(initiatorPseudoname, channel, pseudoname);
    }
    
    public void banUser(String username){
        this.tokenAuth.banUser(username);
    }
    
    public boolean checkTokenValidity(String username, String token){
        return this.tokenAuth.checkTokenValidity(username, token);      
    }
 
    public void initializeI2Pdestination(String i2pDestination){
        this.i2pDestination = i2pDestination;
    }
    
    public String anounceI2Pdestination(){
        return this.i2pDestination;
    }
    
    public void changeChannel(String channel, SSLConnection con){
        removeSSLConnection(channel, con);
        this.chatConnections.addPUCSSLConnection(con);
    }
    
    public void registerI2PChannel(String channel, I2PConnection con){
        if(channel.equals("PSC"))
            this.chatConnections.addPSCI2PConnection(con);
        else if(channel.equals("PUC"))
            this.chatConnections.addPUCI2PConnection(con);
    }
    
    public void removeConnection(Connection con){
        this.chatConnections.removePUCConnection(con);
    }
    
    public void removeSSLConnection(String channel, SSLConnection con){
        if(channel.equals("PSC"))
            this.chatConnections.removePSCSSLConnection(con);
        else if(channel.equals("PUC"))
            this.chatConnections.removePUCSSLConnection(con);
    }
    
    public void removeI2PConnection(String channel, I2PConnection con){
        if(channel.equals("PSC"))
            this.chatConnections.removePSCI2PConnection(con);
        else if(channel.equals("PUC"))
            this.chatConnections.removePUCI2PConnection(con);
    }
    
    public void broadcastMessage(String channel, Message object){
        if(channel.equals("PUC"))
            this.chatConnections.broadcastPUCmessage(object);
        else if(channel.equals("PSC"))
            this.chatConnections.broadcastPSCmessage(object);
    }
    
    
    private final HashSet<String> bannedWords;
    
    public boolean checkVocabulary(String word){
        return this.bannedWords.contains(word);
    }
    
    public void addBadWord(String badWord){
        this.bannedWords.add(badWord);
    }
    
    private HashSet<String> initializeBannedWords(){
        HashSet<String> voc = new HashSet();
        voc.add("fuck");
        voc.add("motherfucker");
        voc.add("asshole");
        voc.add("idiot");
        voc.add("dump");
        voc.add("pussy");
        
        return voc;
    }
    
}//ConnectionController
