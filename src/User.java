import javax.crypto.SecretKey;
import java.io.*;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Created by Robert on 02.01.2016.
 * This is a helper class. In case I close interface, it will save user information
 */

public class User implements Serializable{
    private Object user;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private PublicKey serverKey;
    private SecretKey commKey;

    public User(Seller user, PrivateKey privateKey, PublicKey publicKey, PublicKey serverKey, SecretKey commKey){
        this.user = user;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.serverKey = serverKey;
        this.commKey = commKey;
    }
    public User(Buyer user, PrivateKey privateKey, PublicKey publicKey, PublicKey serverKey){
        this.user = user;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.serverKey = serverKey;
    }
    public Object getUser(){
        return user;
    }
    public void setCommKey(SecretKey commKey){this.commKey = commKey;}
    public PrivateKey getPrivateKey(){
        return privateKey;
    }
    public PublicKey getPublicKey(){
        return publicKey;
    }
    public PublicKey getServerKey() { return  serverKey;}
    public SecretKey getCommKey(){return commKey;}
    public void setUser( Object user){
        this.user = user;
    }
    public void saveUser(){
        String workingDir = System.getProperty("user.dir");
        try{
            FileOutputStream fos = new FileOutputStream(user.getClass().getName());
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            oos.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public User loadUser(String name){
        try{
            FileInputStream fin = new FileInputStream(name);
            ObjectInputStream ois = new ObjectInputStream(fin);
            User usr = (User) ois.readObject();
            ois.close();
            return usr;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
