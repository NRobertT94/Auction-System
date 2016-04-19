import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.mux.MuxRpcDispatcher;
import org.jgroups.util.RspList;
import org.jgroups.util.Util;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class FrontEnd extends ReceiverAdapter {
    private JChannel channel;
    private MuxRpcDispatcher dispatcher;
    private HashMap<String,HashMap<Integer,Object>> state = new HashMap<>();
    private JFrame frame;
    private JPanel panel;
    private JTextArea textArea;

    /**
     * Constructor initializes database and creates the communication channel
     * @throws Exception
     */
    public FrontEnd() throws Exception {

        channel = new JChannel("flush-udp.xml");
        channel.setReceiver(this);
        channel.setName("FrontEnd");
        channel.connect("Auct");
        state.put("SellersDB",new HashMap<>());
        state.put("BuyersDB",new HashMap<>());
        state.put("AuctionsDB",new HashMap<>());
        dispatcher = new MuxRpcDispatcher((short)1,channel, this,this,this);

        frontEndGUI();
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
        textArea.append("Updated: " + lst.size() + " items in databases:");
    }

    /**
     * Updates auctions tables
     * @param id
     * @param auction
     * @throws Exception
     */
    public void updateAuctions(int id, Auction auction) throws Exception{
        RspList responses = dispatcher.callRemoteMethods(null, "updAuctions", new Object[]{id, auction}, new Class[]{int.class, Auction.class}, new RequestOptions(ResponseMode.GET_ALL, 5000));
        for (String r: (List<String>)responses.getResults()){
            textArea.append("Response: " + r);
        }
    }

    /**
     * Updates sellers table
     * @param id
     * @param seller
     * @throws Exception
     */
    public void updateSellers(int id, Seller seller) throws Exception{
        RspList responses = dispatcher.callRemoteMethods(null, "updSellers", new Object[]{id, seller}, new Class[]{int.class, Seller.class}, new RequestOptions(ResponseMode.GET_ALL, 5000));
        System.out.println("response size: " + responses.size());
        for (String r: (List<String>)responses.getResults()){
            textArea.append("Response: " + r);
        }
    }

    /**
     * Updates Buyers table
     * @param id
     * @param buyer
     * @throws Exception
     */
    public void updateBuyers(int id, Buyer buyer) throws Exception{
        RspList responses = dispatcher.callRemoteMethods(null, "updBuyers", new Object[]{id, buyer}, new Class[]{int.class, Buyer.class}, new RequestOptions(ResponseMode.GET_ALL, 5000));
        for (String r: (List<String>)responses.getResults()){
            textArea.append("Response: " + r);
        }
    }

    /**
     * Retrieve sellers table
     * @return HashMap with sellers
     * @throws Exception
     */
    public HashMap<Integer,Seller> getSellers() throws Exception{
        RspList responses = dispatcher.callRemoteMethods(null, "retrieveSellers", new Object[]{}, new Class[]{}, new RequestOptions(ResponseMode.GET_ALL, 5000));
        textArea.append("Retrieving Sellers Database ");
        return (HashMap<Integer,Seller>) responses.getResults().get(0);
    }

    /**
     * Retrieve buyers table
     * @return HashMap with buyers
     * @throws Exception
     */
    public HashMap<Integer,Buyer> getBuyers() throws Exception{
        RspList responses = dispatcher.callRemoteMethods(null, "retrieveBuyers", new Object[]{}, new Class[]{}, new RequestOptions(ResponseMode.GET_ALL, 5000));
        textArea.append("Retrieving Buyers Database ");
        return (HashMap<Integer,Buyer>) responses.getResults().get(0);
    }

    /**
     * Retrieve auctions table
     * @return HashMap with auctions
     * @throws Exception
     */
    public HashMap<Integer,Auction> getAuctions() throws Exception{
        RspList responses = dispatcher.callRemoteMethods(null, "retrieveAuctions", new Object[]{}, new Class[]{}, new RequestOptions(ResponseMode.GET_ALL, 5000));
        textArea.append("Retrieving Auctions Database ");
        return (HashMap<Integer,Auction>) responses.getResults().get(0);
    }

    /**
     * Deletes an auction
     * @param id
     * @throws Exception
     */
    public void deleteAuction(int id) throws Exception{
        RspList responses = dispatcher.callRemoteMethods(null, "delAuction", new Object[]{id}, new Class[]{int.class}, new RequestOptions(ResponseMode.GET_ALL, 5000));
        for (String r: (List<String>)responses.getResults()){
            textArea.append("Response: " + r);
        }
    }
    public void disconnect(String name) throws  Exception {
        dispatcher.callRemoteMethods(null, "disConn", new Object[]{name}, new Class[]{String.class}, new RequestOptions(ResponseMode.GET_NONE, 5000));
    }
    @Override
    public void viewAccepted(View view) {
        System.out.println(view.toString());
    }
    public void frontEndGUI() {
        frame = new JFrame("Frontend");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 500);
        panel = new JPanel();
        frame.add(panel);
        textArea = new JTextArea(30, 20);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.append("******** Log ********\n\n");
        JScrollPane scrollPane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        textArea.setEditable(false);
        panel.add(scrollPane);
        frame.setVisible(true);
    }

}
