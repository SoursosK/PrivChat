
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.security.crypto.bcrypt.BCrypt;

public class DatabaseManager {

    private java.sql.Connection conn = null;
    private Statement stm = null;

    public DatabaseManager() {
        
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + System.getProperty("user.home") + "\\Desktop\\projectDB.db");
            
            stm = conn.createStatement();
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, null, ex);
        } 
           
    }

    public synchronized boolean authenticateUser(String username, String password) {
        try {
            if(username == null)
                return false;
            
            ResultSet res = stm.executeQuery("SELECT * FROM users WHERE username = '" + username + "' ");
            
            return BCrypt.checkpw(password , res.getString("password"));
            
        } catch (SQLException ex) {
            return false;
        }
    }
    
    public synchronized boolean registerUser(String username, String password) {
        try {
            if( !usernameExistance(username) ){
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                
                stm.executeUpdate("insert into users (username, password) values ('" + username + "', '" + hashedPassword + "');");

                return true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    private synchronized boolean usernameExistance(String username) {
        try {
            if(username == null)
                return true;
            
            ResultSet res = stm.executeQuery("SELECT * FROM users WHERE username = '" + username + "' ");
            
            return res.getString("username").equals(username);
        } catch (SQLException ex) {
            return false;
        }
    }
     
    
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");

        java.sql.Connection conn = DriverManager.getConnection("jdbc:sqlite:" + System.getProperty("user.home") + "\\Desktop\\projectDB.db");
        Statement stm = conn.createStatement();
        
        
        
//         PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE id = x)");
//         pstmt.setInt(3, );
        
        //String x = BCrypt.hashpw("123", BCrypt.gensalt());
        //stm.executeUpdate("create table users (id integer primary key autoincrement, username string , password string);");
        //stm.executeUpdate("insert into users (username, password) values ('kostas', '123');");

        
        //stm.executeUpdate("DROP TABLE users;");
//      stm.executeUpdate("create table users (id integer primary key autoincrement, name string, surname string, username string , birthday string, gender string, description string, country string, town string);");

//      stm.executeUpdate("create table friends (id integer primary key autoincrement, username text, friend text);");
      
//      stm.executeUpdate("create table posts (id integer primary key autoincrement, owner text, creator text, content string);");
//      
//      stm.executeUpdate("insert into users (name, surname , username, birthday, gender, description,country,town) values ('george', 'giorgou' ,'george100', '9-10-98', 'male', 'gamaw', 'greece', 'karlovasi');");
//      
//      stm.executeUpdate("insert into users (name, surname , username, birthday, gender, description, country, town) values ('kostas', 'soursos' ,'kostas100', '10-10-98', 'male', 'gamawyeah', 'england', 'london');");
//
//      stm.executeUpdate("insert into friends (username, friend) values ('maraki100', 'nikos100');");
//
//      stm.executeUpdate("insert into posts (owner, creator, content) values ('kostas100', 'kostas100', 'sou aresoun ta mpiskota?');");

//      stm.executeUpdate("alter table users add password string");

    //stm.executeUpdate(" UPDATE users SET password = '123456' WHERE username = 'kostas100'; ");

    ResultSet res = stm.executeQuery("SELECT * FROM users");

        while(res.next()){
                System.out.println("Id : " + res.getString("id") );
                System.out.println("Username : " + res.getString("username"));
                System.out.println("Password : " + res.getString("password"));

                System.out.println("-----------------------------");
        }
    
    
//        ResultSet res = stm.executeQuery("SELECT * FROM users");
//
//        while(res.next()){
//                System.out.println("ID : " + res.getInt("id"));
//                System.out.println("UserName : " + res.getString("username"));
//                System.out.println("Name : " + res.getString("name"));
//                System.out.println("Password : " + res.getString("password"));
//                System.out.println("Description : " + res.getString("description"));
//
//                System.out.println("-----------------------------");
//        }
//        
//        System.out.println("");
//        System.out.println("");
//        System.out.println("");
//        
//        res = stm.executeQuery("SELECT * FROM friends");
//        //res = stm.executeQuery("SELECT * FROM friends WHERE username = 'maraki100' AND friend = 'nikos100'; ");        
//
//        while(res.next()){
//                System.out.println("ID : " + res.getInt("id"));
//                System.out.println("username : " + res.getString("username"));
//                System.out.println("friend : " + res.getString("friend"));
//
//                System.out.println("-----------------------------");
//        }
//
//        System.out.println("");
//        System.out.println("");
//        System.out.println("");
//        
//        res = stm.executeQuery("SELECT * FROM posts");
//
//        while(res.next()){
//                System.out.println("ID : " + res.getInt("id"));
//                System.out.println("owner : " + res.getString("owner"));
//                System.out.println("creator : " + res.getString("creator"));
//                System.out.println("content : " + res.getString("content"));
//
//                System.out.println("-----------------------------");
//        }
        

        conn.close();
    }
    
    
    
    
   
}
//ResultSet rs = stm.executeQuery(" SELECT * FROM posts WHERE id = '" + postId +"'; ");
//            
//if( !rs.getString("owner").equals(username) && !rs.getString("creator").equals(username) )
//    return 403;
