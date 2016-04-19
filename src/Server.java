/**
 * Created by Robert on 03.11.2015.
 */
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;


public class Server {

    public Server() {

        try {
            LocateRegistry.createRegistry(1099);
            SellerInterface seller = new Server_impl();
            Naming.rebind("rmi://localhost/AuctionSystem", seller);
        } catch (Exception e) {
            System.out.println("Server error: " + e);
        }
    }

    public static void main(String args[]) {
        new Server();
        System.out.println("Server working...");
    }
}
