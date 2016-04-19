import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import java.security.PublicKey;
import java.util.HashSet;

/**
 * Created by Robert on 04.11.2015.
 */
public interface BuyerInterface extends java.rmi.Remote{

    String createBuyer(Buyer buyer)  throws java.rmi.RemoteException;
    String bid(Buyer buyer, double price, int auctionId) throws java.rmi.RemoteException;
    HashSet<String> displayAuctions() throws java.rmi.RemoteException;
    PublicKey getPublicKey() throws java.rmi.RemoteException;
    SecretKey getSecretKey(String userType,int id) throws java.rmi.RemoteException;
    SealedObject[] challengeServer(String userType, int id, SealedObject sealedRequest) throws java.rmi.RemoteException;
    SealedObject[] challengeClient(String userType, int id, SealedObject[] sealedRequest) throws java.rmi.RemoteException;



}
