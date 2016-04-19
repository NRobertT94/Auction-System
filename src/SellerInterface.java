import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import java.rmi.RemoteException;
import java.security.PublicKey;

/**
 * Created by Robert on 04.11.2015.
 */
public interface SellerInterface extends java.rmi.Remote{

    String createSeller(Seller seller)  throws java.rmi.RemoteException;
    String createAuction(Seller seller, Auction auction)  throws java.rmi.RemoteException;
    String closeAuction(Seller seller, int auctionID) throws java.rmi.RemoteException;
    PublicKey getPublicKey() throws java.rmi.RemoteException;
    SecretKey getSecretKey(String userType,int id) throws java.rmi.RemoteException;
    SealedObject[] challengeServer(String userType, int id, SealedObject sealedRequest) throws java.rmi.RemoteException;
    SealedObject[] challengeClient(String userType, int id, SealedObject[] sealedRequest) throws java.rmi.RemoteException;






    //void run(String command) throws RemoteException;
}
