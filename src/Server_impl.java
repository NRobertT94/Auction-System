import javax.crypto.*;
import javax.swing.*;
import java.rmi.RemoteException;
import java.security.*;
import java.util.*;

/**
 * Created by Robert on 03.11.2015.
 */
public class Server_impl extends java.rmi.server.UnicastRemoteObject implements SellerInterface, BuyerInterface {
    private Random rand = new Random();
    private FrontEnd disp;
    // HashMaps represent databases for auction

    private JFrame frame;
    private JPanel panel;
    private JTextArea textArea;
    private PrivateKey privKey;
    private PublicKey pubKey;
    private Signature dsa;

    /**
     * Creates a simple GUI for the admin to view all interactions with the database
     * Generates public and private keys for server
     * @throws java.rmi.RemoteException
     */
    public Server_impl() throws java.rmi.RemoteException {
        super();
        serverGUI();
        generateKeys();

        System.out.println( "\nPublic key algorithm: " + pubKey.getAlgorithm() +
                "\nPublic key encoded: " +  pubKey.getEncoded() +
                "\nPublic Key format: " +  pubKey.getFormat() +
                "\n------------------------------------------\n"+
                "\nPrivate key algorithm: " + privKey.getAlgorithm() +
                "\nPrivate key encoded " + privKey.getEncoded() +
                "\nPrivate key format: " + privKey.getFormat());
        try{
            disp = new FrontEnd();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //consl();
    }

    /**
     * Creates a seller object and stores into database
     * @param seller
     * @return message to client if creation was succesful or not
     * @throws RemoteException
     */
    @Override
    public synchronized String createSeller(Seller seller) throws RemoteException {
        HashMap<Integer,Seller> sellers;
        try{
            sellers = disp.getSellers();
            String message = "";
            if(!sellers.isEmpty()){
                if(sellers.values().iterator().next().getName().equals(seller.getName())){
                    message = message + "\nError!User could not register with username: " + seller.getName() + "\n";
                    return message;
                }
                if(sellers.values().iterator().next().getEmail().equals(seller.getEmail())){
                    message = message + "\nError!User could not register with email: " + seller.getEmail() + "\n";
                    return message;
                }
            }
            int id = Math.abs(rand.nextInt());
            seller.setId(id);
            disp.updateSellers(id,seller);
            HashMap<Integer, Seller> test = disp.getSellers();
            for(Seller s: test.values()){
                System.out.println("In seller:"+s.getName() + "\n with id: " + s.getId());
            }
            message = "-------------------------------------------\n" +
                    "Seller: " + seller.getName() + " added to System\n" +
                    "Email: " + seller.getEmail() +
                    "\nid: " + seller.getId() +
                    "\n-----------------------------------------\n";
            textArea.append(message + "\n\n");
            return String.valueOf(id);
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates an Auction and stores into database if criteria is met
     * @param seller
     * @param auction
     * @return
     * @throws RemoteException
     */
    @Override
    public synchronized String createAuction(Seller seller, Auction auction) throws RemoteException {
        HashMap<Integer,Seller> sellers;
        try {
            // get db
            sellers = disp.getSellers();
            //decrypt sealed object
            System.out.println("SImple test: " + seller.getId());
            String clientMessage;
            /// should check in sellerGUI
            if (sellers.containsValue(auction.getSeller())) {
                clientMessage = "Seller ID could not be found, try again...";
            }
            //check at seller GUI
            if (auction.getStartinPrice() > auction.getMinPrice()) {
                clientMessage = "Starting price has to be lower than reserve price";
            } else {
                int auctionId = Math.abs(rand.nextInt());
                auction.setId(auctionId);
                disp.updateAuctions(auctionId, auction);
                System.out.println("size of auctions: " + disp.getAuctions().size());
                //update sellers db
                seller.addAuction(auctionId, auction);
                disp.updateSellers(seller.getId(),seller);
                String serverMessage = "---------------------------------------------\n" +
                        "Added auction!\n\n" +
                        "Auction name: " + auction.getName() +
                        "\nAuction ID: " + auction.getId() +
                        "\nSeller name: " + auction.getSeller().getName() +
                        "\nSeller ID: " + auction.getSeller().getId() +
                        "\n-------------------------------------------\n";
                textArea.append(serverMessage + "\n\n");
                clientMessage = String.valueOf(auctionId);
                return clientMessage;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Closes auction displaying the winner of the auction
     * @param seller
     * @param auctionID
     * @return message displaying winner or if the minumum price has not been met
     * @throws RemoteException
     */
    @Override
    public synchronized String closeAuction(Seller seller, int auctionID) throws RemoteException {
        HashMap<Integer,Buyer> buyers;
        HashMap<Integer,Auction> auctions;
        try{
            buyers = disp.getBuyers();
            auctions = disp.getAuctions();
            String message;
            System.out.println("Auctions size: " + auctions.size());
            //System.out.println("Try debg: " +auctions.get(auctionID).getSeller().getName());
            if (seller.getId() != auctions.get(auctionID).getSeller().getId()) {
                message = seller.getName() + ", you are not authorized to delete auction of another seller";
            } else {
                double minPrice = auctions.get(auctionID).getMinPrice();
                double highestBid = auctions.get(auctionID).getHighestBid();
                Auction auction = auctions.get(auctionID);
                Buyer winner = buyers.get(auctions.get(auctionID).getHighestBidder());
                if (minPrice <= highestBid) {
                    message = "------------------------------------------------\n" +
                            "Auction closed:\n" +
                            "Auction name: " + auction.getName() + "\n" +
                            "\nBuyer: " + winner.getName() +
                            "\nSold for: " + highestBid + "$"+
                            "\nSeller: " + seller.getName() +
                            "\n\nAuction id: " + auction.getId() +
                            "\nBuyer id: " + winner.getId() +
                            "\nSeller id: " + seller.getId() +
                            "\n------------------------------------------------\n";
                    //update winner's purchases
                    winner.addAuction(auction);
                    disp.updateBuyers(winner.getId(),winner);
                } else {
                    message = "------------------------------------------------\n" +
                            "Auction closed:\n" +
                            "Auction name: " + auction.getName() + "\n" +
                            "Auction id: " + auction.getId() +
                            "\n\nSeller: " + seller.getName() +
                            "\nSeller id: " + seller.getId() +
                            "\n\nReserve price has not been reached" +
                            "\n------------------------------------------------\n";
                }
                textArea.append(message + "\n\n");
                auctions.remove(auctionID);
                disp.deleteAuction(auctionID);
                ////remove it from seller as well or keep as history
                seller.removeAuction(auctionID);
                ////
            }
            return message;

        }catch(Exception e){
            e.printStackTrace();
            System.out.println("\nDEBUG:: Seller id:" +seller.getId());
        }
        return null;
    }
    //creates buyer and adds it to buyer DB

    /**
     * Creates a buyer object and stores into database
     * @param buyer
     * @return message if creation was succesful
     * @throws RemoteException
     */
    @Override
    public synchronized String createBuyer(Buyer buyer) throws RemoteException {
        try{
            String message = "";
            HashMap<Integer,Buyer> buyers = disp.getBuyers();
            if(!buyers.isEmpty()){
                if(buyers.values().iterator().next().getName().equals(buyer.getName())){
                    message = message + "\nError!User could not register with username: " + buyer.getName() + "\n";
                    return message;
                }
                if(buyers.values().iterator().next().getEmail().equals(buyer.getEmail())){
                    message = message + "\nError!User could not register with email: " + buyer.getEmail() + "\n";
                    return message;
                }
            }
            int id = Math.abs(rand.nextInt());
            buyer.setId(id);
            //buyers.put(id, buyer);
            disp.updateBuyers(id,buyer);
            message = "--------------------------------------------\n" +
                    "Buyer: " + buyer.getName() + " added to System\n" +
                    "Email: " + buyer.getEmail() +
                    "\nid: " + id +
                    "\n------------------------------------------\n";
            textArea.append(message + "\n\n");
            return String.valueOf(id);
        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    // potential buyer bids... highest bid and bidder is updated in Auction object

    /**
     * Potential buyer bids... highest bid and bidder is updated in Auction object
     * @param buyer
     * @param price
     * @param auctionId
     * @return message if bid was succesful
     * @throws RemoteException
     */
    @Override
    public synchronized String bid(Buyer buyer, double price, int auctionId) throws RemoteException {
        String message;
        HashMap<Integer,Auction> auctions;
        HashMap<Integer,Buyer> buyers;
        try {
            auctions = disp.getAuctions();
            buyers = disp.getBuyers();
            if (auctions.get(auctionId) == null || buyers.get(buyer.getId()) == null) {
                message = "\nError processing your request...\n";
            } else {
                auctions.get(auctionId).updateBid(buyer.getId(), price);
                disp.updateAuctions(auctionId,auctions.get(auctionId));
                textArea.append("\nBuyer: " + buyer.getId() +
                        "\nbid: " + String.valueOf(price) +
                        "\nfor: " + auctions.get(auctionId).getName() +
                        "\nAuction id: " + auctionId + "\n");

                message = "\nYou bid: " + String.valueOf(price) +
                        "\n for: " + auctions.get(auctionId).getName() + "\n";
            }
            return message;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Presents to client all auctions.
     * @return HashSet with all auctions
     * @throws RemoteException
     */
    @Override
    public synchronized HashSet<String> displayAuctions() throws RemoteException {
        try{
            HashSet<String> allAuctions = new HashSet<>();
            HashMap<Integer,Auction> auctions;
            auctions = disp.getAuctions();
            if (auctions.isEmpty()) {
                allAuctions.add("\nNo active auctions\n");
            } else {

                for (int key : auctions.keySet()) {
                    allAuctions.add("" + auctions.get(key).getAllInfo());
                }
            }
            return allAuctions;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;

    }

    /**
     * Simple GUI for server to track operations on database
     */
    public void serverGUI() {
        frame = new JFrame("Server");
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

    /**
     * Generates public and private keys used for challenge response
     */
    private void generateKeys() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(1024, random);
            KeyPair pair = keyGen.generateKeyPair();
            privKey = pair.getPrivate();
            pubKey = pair.getPublic();
            dsa = Signature.getInstance("SHA1withDSA", "SUN");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public synchronized PublicKey getPublicKey() throws RemoteException {
        return pubKey;
    }
    public synchronized SecretKey getSecretKey(String userType,int id) throws RemoteException {
        try{
            HashMap<Integer,Seller> sellers = disp.getSellers();
            HashMap<Integer, Buyer> buyers = disp.getBuyers();
            SecretKey key = KeyGenerator.getInstance("DES").generateKey();
            if(userType.equals("seller")){
                sellers.get(id).setCommKey(key);
                disp.updateSellers(id,sellers.get(id));
            }else if(userType.equals("buyer")){
                buyers.get(id).setCommKey(key);
                disp.updateBuyers(id, buyers.get(id));
            }else {
                System.out.println("not found");
            }
            return key;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Challenge for client.
     * -if challenge is achieved then returns message "Successful log"
     * and key used to encrypt communication between client and server if private key is obtained by an attacker (just extra precaution)
     * -else return null
     * @param userType
     * @param id
     * @param sealedRequest
     * @return SealedObject[0] - message if log is succesful or not
     *         SealedObject[1] - A key used to encrypt  client object used in the next authentication
     *
     * @throws RemoteException
     */
    public synchronized  SealedObject[] challengeClient(String userType,int id,SealedObject[] sealedRequest) throws RemoteException{
        try{
            HashMap<Integer,Seller> sellers = disp.getSellers();
            HashMap<Integer,Buyer> buyers = disp.getBuyers();
            SecretKey clCommKey = null;
            PublicKey clPbKey = null;
            Seller clientSeller = null;
            Buyer clientBuyer = null;
            if (userType.equals("seller")) {
                clCommKey = sellers.get(id).getCommKey();
                clPbKey = sellers.get(id).getPublicKey();
                clientSeller =  (Seller) sealedRequest[1].getObject(clCommKey);
            } else if (userType.equals("buyer")) {

                clCommKey = buyers.get(id).getCommKey();
                clPbKey = buyers.get(id).getPublicKey();
                clientBuyer =  (Buyer) sealedRequest[1].getObject(clCommKey);
            }

            SignedObject challenge = (SignedObject)sealedRequest[0].getObject(clCommKey);
            Signature sig = Signature.getInstance(clPbKey.getAlgorithm());
            boolean original = challenge.verify(clPbKey,sig);
            System.out.println("Received: " + challenge.getObject().toString()+
                    "\nSignature: " + original +
                    "\nUsername: ");
            if(original){
                boolean success = false;
                if(userType.equals("seller")){
                    Seller dbSeller = sellers.get(id);
                    if(clientSeller.getName().equals(dbSeller.getName()) &&
                            clientSeller.getEmail().equals(dbSeller.getEmail())){
                        success = true;
                    }
                }
                else if(userType.equals("buyer")){
                    Buyer dbBuyer = buyers.get(id);
                    if(clientBuyer.getName().equals(dbBuyer.getName()) &&
                            clientBuyer.getEmail().equals(dbBuyer.getEmail())){
                        success = true;
                    }
                }
                if(success){
                    Cipher enCipher = Cipher.getInstance(clCommKey.getAlgorithm());
                    enCipher.init(Cipher.ENCRYPT_MODE, clCommKey);
                    SealedObject[] sealedObj = new SealedObject[2];
                    sealedObj[0] = new SealedObject(new String("Successful log"),enCipher);
                    sealedObj[1] = new SealedObject(getSecretKey(userType,id), enCipher);
                    return sealedObj;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Challenge for server. Used to check if client connects to genuine server
     * @param userType
     * @param id
     * @param sealedRequest
     * @return SealedObject[0] - UUID signed with server's private key
     *         SealedObject[1] - Challenge for client
     *
     * @throws RemoteException
     */
    public synchronized SealedObject[] challengeServer(String userType, int id, SealedObject sealedRequest) throws  RemoteException {
        try {
            HashMap<Integer,Seller> sellers = disp.getSellers();
            HashMap<Integer,Buyer> buyers = disp.getBuyers();
            SecretKey clCommKey;
            if (userType.equals("seller")) {
                clCommKey = sellers.get(id).getCommKey();
            } else if (userType.equals("buyer")) {
                clCommKey = buyers.get(id).getCommKey();
            } else {
                clCommKey = null;
            }
            //cast the challenge from client to String
            String challenge = (String) sealedRequest.getObject(clCommKey);
            String challengeClient = UUID.randomUUID().toString();
            // Sign challenge with server private key
            SignedObject signedObj = new SignedObject(challenge,privKey,dsa);
            // Create encryption cipher. Initiate with communications key.
            // Create array of sealed objects containing the signed object and challenge for client.
            Cipher enCipher = Cipher.getInstance(clCommKey.getAlgorithm());
            enCipher.init(Cipher.ENCRYPT_MODE, clCommKey);
            SealedObject[] sealedObj = new SealedObject[2];
            sealedObj[0] = new SealedObject(signedObj, enCipher);
            sealedObj[1] = new SealedObject(challengeClient,enCipher);
            String msg = ("Challenge from client: " + challenge +
                    "\nChallenge to client: " + challengeClient);
            textArea.append(msg);
            System.out.println(msg);
            return sealedObj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("challenge response is null");
        return null;
    }

    /**
     * This is would have been used to control replicas using the console, not yet put into application due to some issues
     */
    public void consl(){
        Scanner in = new Scanner(System.in);
        String command = "";
        while(!command.equals("exit")){
            System.out.println("Actions:" );
            command = in.nextLine();
            String[] parts = command.split(" ");
            String part1 = parts[0]; // 004
            String part2 = parts[1]; // 034556
            if(parts[1].equals("replica") || parts[1].equals("replicas")){
                for(int i =0; i<Integer.parseInt(parts[0]); i++){
                    try {
                        new Replica();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            else if(parts[0].equals("close")){
                try {
                    disp.disconnect(parts[1]);
                    System.out.println(parts[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else{
                System.out.println("Retry command");
            }
        }
    }

}
