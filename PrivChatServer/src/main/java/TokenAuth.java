
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.RandomStringUtils;

public class TokenAuth {
    //<Token, Date>>
    private final HashMap<String, String> tokens;
    //<Username, Date>>
    private final HashMap<String, String> activeBanRecord;

    public TokenAuth() {
        this.tokens = new HashMap();
        this.activeBanRecord = new HashMap();
    }

//    public static void main(String[] args) {
//        TokenAuth a = new TokenAuth();
//        System.out.println(a.isExpire("Ιαν-20-2019 1:30:55 πμ"));
//        System.out.println(getToday("MMM-dd-yyyy hh:mm:ss a"));
//        System.out.println(new TokenAuth().add2Hours());
//        System.out.println(TokenAuth.getToday("MMM-dd-yyyy hh:mm:ss a"));
//    }

    public void banUser(String username) {
        String banExpTime = this.add2Hours();     
        this.activeBanRecord.put(username, banExpTime);
    }
    
    private boolean isBanned(String username) {
        if(this.activeBanRecord.containsKey(username))
            if( this.isExpire(this.activeBanRecord.get(username)) ){
                this.activeBanRecord.remove(username);
                return false;
            }
            else
                return true;
        
        return false;
    }
    
    public String generateToken(String username) {
        if( this.isBanned(username) ){
            return null;
        }
        
        String date = getToday("MMM-dd-yyyy hh:mm:ss a");
        String randomString = RandomStringUtils.randomAlphabetic(64);

        String x = username + date + randomString;

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(x.getBytes(StandardCharsets.UTF_8));
            String token = Base64.getEncoder().encodeToString(hash);

            this.insertNewToken(token, this.add2Hours());

            return token;

        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(TokenAuth.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    private void insertNewToken(String token, String date) {
        this.tokens.put(token, date);
    }

    public boolean checkTokenValidity(String username, String token) {
        if( this.isBanned(username) )
            return false;
        
        String date = this.tokens.get(token);

        if (date == null) {
            return false;
        }

        if (isExpire(date)) {
            this.tokens.remove(token);
            return false;
        } else 
            return true;   
    }

    private boolean isExpire(String date) {
        if (date.isEmpty() || date.trim().equals("")) {
            return false;
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy hh:mm:ss a"); // Jan-20-2015 1:30:55 PM
            Date d = null;
            Date d1 = null;
            String today = getToday("MMM-dd-yyyy hh:mm:ss a");
            try {
                //System.out.println("expdate>> " + date);
                //System.out.println("today>> " + today + "\n\n");
                d = sdf.parse(date);
                d1 = sdf.parse(today);
                if (d1.compareTo(d) < 0) {// not expired
                    return false;
                } else if (d.compareTo(d1) == 0) {// both date are same
                    if (d.getTime() < d1.getTime()) {// not expired
                        return false;
                    } else if (d.getTime() == d1.getTime()) {//expired
                        return true;
                    } else {//expired
                        return true;
                    }
                } else {//expired
                    return true;
                }
            } catch (ParseException e) {
                return false;
            }
        }
    }

    private String add2Hours() {
        Calendar cal = Calendar.getInstance(); // creates calendar
        cal.setTime(new Date()); // sets calendar time/date
        cal.add(Calendar.HOUR_OF_DAY, 2); // adds one hour
        cal.getTime(); // returns new date object, one hour in the future
        SimpleDateFormat format = new SimpleDateFormat("MMM-dd-yyyy hh:mm:ss a");
        return format.format(cal.getTime());
        
        //return new SimpleDateFormat("MMM-dd-yyyy hh:mm:ss a").format(cal);
    }
    
    private static String getToday(String format) {
        Date date = new Date();
        return new SimpleDateFormat(format).format(date);
    }

}
