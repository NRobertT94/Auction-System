import javax.crypto.SecretKey;
import java.io.Serializable;
import java.security.PublicKey;
import java.util.HashMap;

/**
 * Created by Robert on 04.11.2015.
 */
public class Buyer implements Serializable {
    private int id;
    private String name;
    private String email;
    private PublicKey publicKey;
    private SecretKey commKey;
    private HashMap<Integer,Auction> auctions;
    public Buyer(String name, String email, PublicKey publicKey){
        this.name = name;
        this.email = email;
        this.publicKey = publicKey;
        auctions = new HashMap();

    }
    public String getName(){
        return name;
    }
    public int getId(){
        return id;
    }
    public Auction getAuction(int id){
        return auctions.get(id);
    }
    public HashMap<Integer, Auction> getAuctions(){
        return auctions;
    }
    public PublicKey getPublicKey(){ return  publicKey;}
    public void setCommKey(SecretKey commKey){ this.commKey = commKey;}
    public void removeAuction(int id){auctions.remove(id); }
    public void addAuction(Auction a){
        auctions.put(a.getId(),a);
    }
    public void setId(int id) {this.id = id;}
    public String getEmail(){
        return email;
    }
    public SecretKey getCommKey(){return commKey;}

}
