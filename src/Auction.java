import java.io.Serializable;
import java.text.CollationKey;
import java.util.*;

/**
 * Created by Robert on 03.11.2015.
 */
public class Auction implements Serializable{
    private int id;
    private String name;
    private Seller seller;
    private double startingPrice;
    private double minPrice;
    private double highestBid;
    private int highestBidderID;
    private HashMap<Integer, Double> bidders;

    public Auction(Seller seller, String name, double startingPrice, double minPrice){
        this.seller = seller;
        this.name = name;
        this.startingPrice = startingPrice;
        this.minPrice = minPrice;
        highestBid = 0;
        highestBidderID = 0;
        bidders = new HashMap();
    }
    public String getName(){
        return name;
    }
    public int getId(){
        return this.id;
    }
    public Seller getSeller() {return seller;}
    public double getStartinPrice(){
        return startingPrice;
    }
    public double getMinPrice(){
        return minPrice;
    }
    public String getAllInfo(){
        return  "\n--------------------------------------------"+
                    "\nAuction Name: " + name +
                    "\nID: " +id +
                    "\nStarting price: " + startingPrice +
                    "\nHighest bid: " + highestBid +
                "\n--------------------------------------------";

    }
    //updates highest bid and bidder
    public void updateBid(int buyerID, double price){
        bidders.put(buyerID, price);
        highestBid = Collections.max(bidders.values());
        System.out.println("highest bid: "+highestBid);
        int comparisonResult = 0;
        for (int key: bidders.keySet()){
            comparisonResult = Double.compare(bidders.get(key),highestBid);
            if(comparisonResult == 0) {
                highestBidderID = key;
                System.out.println("Highest bidder ID: " + highestBidderID);
            }
        }
    }
    public HashMap<Integer,Double> getBidders(){
        return bidders;
    }
    public double getHighestBid(){
        return highestBid;
    }
    public int getHighestBidder(){
        return highestBidderID;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setId(int id){ this.id = id;}
    public void setStartingPrice(double startingPrice){
        this.startingPrice = startingPrice;
    }
    public void setMinPrice(double minPrice){
        this.minPrice = minPrice;
    }
}
