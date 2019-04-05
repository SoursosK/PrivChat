

import java.io.Serializable;

public class Message implements Serializable {
    private String name;
    private final String type;
    private final String phrase;

    public Message(String name, String type, String phrase){
        this.name = name;
        this.type = type;
        this.phrase = phrase;
    }
    
    public String tellType(){
        return this.type;
    }
    
    public String tellPhrase(){
        return this.phrase;
    }
    
    public String tellName(){
        return this.name;
    }
    
    @Override
    public String toString() {
        return "Message{" + "name=" + name + ", type=" + type + ", phrase=" + phrase + '}';
    }
            
}//Message
