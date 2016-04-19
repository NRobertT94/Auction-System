import org.jgroups.*;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.blocks.mux.MuxRpcDispatcher;
import org.jgroups.conf.PropertyConverters;
import org.jgroups.util.Util;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Replica constructed following tutorial on JGroups and adapted to auctioning system
 */
public class Replica extends ReceiverAdapter {

    private JChannel channel;
    private LinkedList<String> words = new LinkedList<>();
    private MuxRpcDispatcher dispatcher;
    private String name;

    private HashMap<String,HashMap<Integer,Object>> state = new HashMap<>();

    public Replica() throws Exception {

        channel = new JChannel("flush-udp.xml");
        channel.setReceiver(this);
        Random rnd = new Random();
        name = "Replica" + rnd.nextInt(100);
        channel.setName(name);
        dispatcher = new MuxRpcDispatcher((short)1,channel,this,this,this);
        state.put("SellersDB",new HashMap<>());
        state.put("BuyersDB",new HashMap<>());
        state.put("AuctionsDB",new HashMap<>());
        channel.connect("Auct");

        View view = channel.getView();
        List<Address> addresses = view.getMembers();
        for(Address a : addresses) {

            try {
                channel.getState(a,10000);

            } catch (Exception e) {
                System.err.println("Error on: " + a.toString());
                continue;
            }
            System.out.println("Transfered state from: " + a.toString());
            break;
        }
       // checkItems();
    }


    public String updAuctions(int id, Auction auction) {
        state.get("AuctionsDB").put(id, auction);
        return "Updated Auctions DB with: "+ auction.getName();
    }
    public String updSellers(int id, Seller seller) {
        state.get("SellersDB").put(id, seller);
        return "Updated Sellers DB with: " + seller.getName();
    }
    public String updBuyers(int id, Buyer buyer) {
        state.get("BuyersDB").put(id, buyer);
        return "Updated Buyers DB with:" + buyer.getName();
    }


    public HashMap<Integer, Auction> retrieveAuctions(){
        return (HashMap) state.get("AuctionsDB");
    }
    public HashMap<Integer, Seller> retrieveSellers(){
        return (HashMap) state.get("SellersDB");
    }
    public HashMap<Integer, Buyer> retrieveBuyers(){
        return (HashMap) state.get("BuyersDB");
    }

    public String delAuction(int id){
        state.get("AuctionsDB").remove(id);
        return "Removed Auction with ID: " + id;
    }
    public void disConn(String n){
        String msg;
        if(n.equals(this.name)){
            msg = "Disconnected: " + name;
            channel.disconnect();
            channel.close();
        }
        else {
            msg = "Could not find " + name;
        }

    }

    @Override
    public void getState(OutputStream output) throws Exception {
        synchronized (state) {
            Util.objectToStream(state, new DataOutputStream(output));
        }

    }
    @Override
    public void setState(InputStream input) throws Exception {

        HashMap<String,HashMap<Integer,Object>> lst = (HashMap<String,HashMap<Integer,Object>>)Util.objectFromStream(new DataInputStream(input));
        synchronized (state) {
            state.clear();
            state.putAll(lst);
        }
        System.out.println("Updated: " + lst.size() + " items in databases:");
    }

    private void checkItems(){
        Thread updateAuctions = new Thread() {
            @Override
            public void run() {
                while (true) {

                    try {
                        Thread.sleep(10000); // sleep for 5 seconds
                        int counter = 0;
                        for (HashMap<Integer,Object> hm: state.values()){
                            counter++;
                            System.out.println("\n****************\n" +
                                    "Hashmaps in state: " + counter +
                                    "\nObjects found: " + hm.size() +
                                    "\n****************\n");
                        }
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
            }
        };
        updateAuctions.start();
    }
    @Override
    public void viewAccepted(View view) {
        System.out.println(view.toString());
    }
    public static void main(String[] args) throws Exception {
        new Replica();
    }
}

