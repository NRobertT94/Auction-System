import javax.crypto.SecretKey;
import java.io.Serializable;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Robert on 04.11.2015.
 */
public class Seller implements Serializable {
    private int id;
    private String name;
    private String email;
    private HashMap<Integer,Auction> auctions;
    private PublicKey publicKey;
    private SecretKey commKey;

    public Seller(String name, String email,PublicKey publicKey){
        this.name = name;
        this.email = email;
        this.publicKey = publicKey;
        auctions = new HashMap<>();
    }

    public int getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    public String getEmail() {return email; }
    public PublicKey getPublicKey() { return publicKey; }
    public Auction getAuction(int id){
        return auctions.get(id);
    }
    public SecretKey getCommKey(){return commKey;}
    public void setCommKey(SecretKey commKey){this.commKey = commKey;}
    public void setId(int id){ this.id = id;}
    public void removeAuction(int id){ auctions.remove(id);}
    public void addAuction(int id,Auction auction){
        auctions.put(id,auction);
    }

}
