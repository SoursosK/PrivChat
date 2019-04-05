
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;


public final class PrivChatServer extends Thread{

    private static PrivChatServer serverInstance;

    private Thread PUCService;      //9001
    private Thread PSCService;      //9002
    private Thread I2PService;      //9003 --
    private Thread AFUService;      //9004 --
    private Thread UAuthService;    //9000
    private Thread PeerConService;  //9005
    
    private PUCService puc;
    private PSCService psc;
    private I2PService i2p;
    private AFUService afu;
    private UserAuthorizationService userAuth;
    private PeerConService peercs;
    
    private ExecutorService pool;
    private ChatConnections chatConnections;
    private ConnectionController connectionController;
    private TokenAuth tokenAuth;
    
    public PrivChatServer(){
        super();
        initiateServer();
    }
    
    @Override
    public void run() {
        try {
            sleep(20000);
        } catch (InterruptedException ex) {
            Logger.getLogger(PrivChatServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Scanner sc = new Scanner(System.in);
        while(true){
            System.out.println("\nTerminal Options:\n-#addBannedWord\n");
            String option = sc.nextLine();
            if(option.equals("#addBannedWord")){
                System.out.println("Please insert the word you would like to ban: ");
                this.connectionController.addBadWord(sc.next());
            }
        }
        
    }
    
    public static void main (String[] args){
        serverInstance = new PrivChatServer();
        serverInstance.start();
    }

    void initiateServer(){
        this.pool = Executors.newFixedThreadPool(10);
        this.chatConnections = new ChatConnections();
        this.tokenAuth = new TokenAuth();
        this.connectionController = new ConnectionController(this.chatConnections, this.tokenAuth);
        
        this.puc = new PUCService(this.pool, this.connectionController, this.chatConnections);
        this.psc = new PSCService(this.pool, this.connectionController, this.chatConnections);
        this.i2p = new I2PService(this.pool, this.connectionController, this.chatConnections);
        this.afu = new AFUService(this.pool, this.connectionController);
        this.userAuth = new UserAuthorizationService(this.pool, this.tokenAuth);
        this.peercs = new PeerConService(this.pool, this.connectionController, this.chatConnections);
        
        this.PUCService = new Thread( puc );
        this.PSCService = new Thread( psc );
        this.I2PService = new Thread( i2p );
        this.AFUService = new Thread( afu );
        this.UAuthService = new Thread( userAuth );
        this.PeerConService = new Thread( peercs );
        this.PUCService.start();
        this.PSCService.start();
        this.I2PService.start();
        this.AFUService.start();
        this.UAuthService.start();
        this.PeerConService.start();

    }

    
    void terminateConnections(){
        serverInstance.PUCService.interrupt();
        serverInstance.PSCService.interrupt();
        serverInstance.I2PService.interrupt();
    }
    
}//PrivChatServer