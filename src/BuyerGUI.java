import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;

import javax.crypto.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.security.*;
import java.util.UUID;


/**
 * Created by Robert on 05.11.2015.
 */
public class BuyerGUI {

    private BuyerInterface buyerInterface;
    private Buyer buyer;
    private JPanel[] parts = new JPanel[4];
    private JFrame allAuctionsWindow;
    private User user;
    private PrivateKey privKey;
    private PublicKey pubKey;
    private PublicKey serverPubKey;
    private SecretKey commKey;
    private Signature dsa;

    public BuyerGUI() {
        System.out.println("Started BuyerGUI\n");
        buyerGUI();
        generateKeys();
        try {
            buyerInterface = (BuyerInterface) Naming.lookup("rmi://localhost/AuctionSystem");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void generateKeys() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(1024, random);
            KeyPair pair = keyGen.generateKeyPair();
            pubKey = pair.getPublic();
            privKey = pair.getPrivate();
            dsa = Signature.getInstance("SHA1withDSA", "SUN");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
    }
    public void buyerGUI() {

        JFrame frame = new JFrame("Buyer");
        JPanel panel = new JPanel();
        JTextField[] textFields = new JTextField[6];
        JButton[] buttons = new JButton[4];
        JTextArea logArea =  new JTextArea(100, 20);

        //setup for logArea
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.append("******** Log ********\n\n");

        //adding scroll to log area
        JScrollPane scrollPane = new JScrollPane(logArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.setLayout(new GridLayout(0, 1));

        //frame configuration
        frame.setSize(650, 550);
        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //initializes components of GUI
        for (int i = 0; i < parts.length; i++) {
            parts[i] = new JPanel();
        }
        for (int i = 0; i < textFields.length; i++) {
            textFields[i] = new JTextField(10);
        }
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = new JButton();
        }
        buyerActivitiesStatus(false);
        //add all GUI items to first panel

        /*********************************************
         *             Register Buyer                *
         *********************************************/

        parts[0].add(new Label("Username:"));
        parts[0].add(textFields[0]);
        parts[0].add(new Label("Email:"));
        parts[0].add(textFields[1]);
        parts[0].add(buttons[0]);
        buttons[0].setText("Register");
        panel.add(parts[0]);
        buttons[0].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if(textFields[0].getText().length() > 4 && textFields[1].getText().contains("@")){

                        String username = textFields[0].getText();
                        String email = textFields[1].getText();
                        buyer = new Buyer(username,email,pubKey);
                        String response = buyerInterface.createBuyer(buyer);
                        if(!response.contains("Error") || response != null){
                            //log
                            logArea.append("Registering: " + textFields[0].getText() +
                                    "\nEmail: " + textFields[1].getText() +
                                    "\nID: " + response +"\n");
                            //log
                            buyer.setId(Integer.parseInt(response));
                            //reset textFields and return seller ID
                            textFields[0].setEditable(false);
                            textFields[1].setEditable(false);
                            buttons[0].setEnabled(false);
                            frame.setTitle("Buyer: " + response);
                            displayBuyerAuth(false);
                            buyerActivitiesStatus(true);
                            frame.validate();
                            //reset textFields and return seller ID

                            serverPubKey = buyerInterface.getPublicKey();
                            commKey = buyerInterface.getSecretKey("buyer",Integer.parseInt(response));
                            user = new User(buyer,privKey,pubKey,serverPubKey);

                            /////debug
                            System.out.println("\nPublic key algorithm: " + commKey.getAlgorithm() +
                                    "\nPublic key encoded: " + commKey.getEncoded() +
                                    "\nPublic Key format: " + commKey.getFormat());
                            displayAuctions();
                        }
                        else {
                            logArea.append(response);
                        }
                    }
                    else {
                        logArea.append("\nUsername length must be greater than 4\n"+
                                "\nEmail must contain '@'\n\n");
                    }
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }
            }
        });

        //add all GUI items to second panel

        /*********************************************
         *                Login Buyer                *
         *********************************************/
        parts[1].add(new Label("Username:"));
        parts[1].add(textFields[2]);
        parts[1].add(new Label("Email:"));
        parts[1].add(textFields[3]);
        parts[1].add(buttons[1]);
        buttons[1].setText("Login");
        panel.add(parts[1]);
        buttons[1].addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if(commKey != null){
                    String username = textFields[2].getText();
                    String email = textFields[3].getText();
                    boolean auth = authenticate(new Buyer(username,email,pubKey));
                    logArea.append("\n-----------------------------------\n" + "logged in: "+ auth +
                            "\n-----------------------------------\n");
                    if(auth){
                        displayBuyerAuth(false);
                        buyerActivitiesStatus(true);
                        displayAuctions();
                    }

                }
                else{
                    logArea.append("\nComputer not secure!\n");
                }

            }
        });

        //add GUI items to second panel
        /*********************************************
         *             Bid for auction               *
         *********************************************/

        parts[2].add(new Label("Bid:"));
        parts[2].add(textFields[4]); //price
        parts[2].add(new Label("Auction ID:"));
        parts[2].add(textFields[5]); //auction id
        parts[2].add(buttons[2]);
        buttons[2].setText("Bid");
        panel.add(parts[2]);
        buttons[2].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if ((textFields[4].getText().matches("[0-9]+") ||  textFields[4].getText().contains(".")) &&
                            textFields[5].getText().matches("[0-9]+")) {

                        double bidAmount = Double.parseDouble(textFields[4].getText());
                        int auctionID =  Integer.parseInt(textFields[5].getText());
                        String message = buyerInterface.bid(buyer, bidAmount, auctionID); //price
                        logArea.append(message);
                    } else {
                        logArea.append("\nError found in input... try again...\n");
                    }
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }
            }
        });
        parts[3].add(buttons[3]);
        buttons[3].setText("Logout");
        panel.add(parts[3]);
        buttons[3].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayBuyerAuth(true);
                buyerActivitiesStatus(false);
                allAuctionsWindow.removeAll();
                allAuctionsWindow.dispose();
                logArea.setText("******** Log ********\n\n");
            }
        });
        panel.add(scrollPane);
        frame.setVisible(true);
        // Created new JFrame with all active auctions, due to bug
        // when having 2 Jscroll panes in one panel
        //initializing Active auctions GUI parts

    }
    private void displayBuyerAuth(boolean status){
        parts[0].setVisible(status);
        parts[1].setVisible(status);
    }
    private void buyerActivitiesStatus(boolean status){
        parts[2].setVisible(status);
        parts[3].setVisible(status);
    }
    private SealedObject[] challengeServer(){
        String uuid = UUID.randomUUID().toString();
        try {
            Cipher enCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            enCipher.init(Cipher.ENCRYPT_MODE, commKey);
            SealedObject sealedObj = new SealedObject(uuid, enCipher);
            //get response from server
            SealedObject[] response = buyerInterface.challengeServer("buyer",buyer.getId(),sealedObj);
            System.out.println("Challenge sent: " + uuid + "\n");
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private String verifySvResponse(SealedObject[] response){
        try{

            SignedObject signedObject = (SignedObject)response[0].getObject(commKey); //answer
            String challenge = (String) response[1].getObject(commKey); //challenge for client
            //verify signature
            Signature sig = Signature.getInstance(serverPubKey.getAlgorithm());
            boolean original = signedObject.verify(serverPubKey, sig);
            System.out.println("Signature: " + original + "\n" +
                    "Challenge received: " + challenge);
            if(original){
                return challenge;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("Signature not original");
        return null;

    }
    private boolean handleSvChallenge(String challenge,Buyer request){
        try{

            Cipher enCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            enCipher.init(Cipher.ENCRYPT_MODE, commKey);
            SignedObject sigChallenge = new SignedObject(challenge, privKey, dsa);
            SealedObject[] sealedObjects = new SealedObject[2];
            sealedObjects[0] = new SealedObject(sigChallenge,enCipher);
            sealedObjects[1] = new SealedObject(request,enCipher);
            //get response
            SealedObject[] sObj = buyerInterface.challengeClient("buyer",buyer.getId(),sealedObjects);
            if(sObj != null){
                String logDetail = (String) sObj[0].getObject(commKey);
                SecretKey newKey = (SecretKey) sObj[1].getObject(commKey);
                System.out.println("Answer from sv: " + logDetail);
                //renew communication key
                commKey = newKey;
                return true;
            }else {
                System.out.println("Authentication failed. Check username and password");
                return false;
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        return false;

    }
    private boolean authenticate(Buyer request){
        SealedObject[] response = challengeServer();
        String challenge = verifySvResponse(response);
        boolean authenticated = handleSvChallenge(challenge,request);
        return authenticated;
    }
    private void displayAuctions(){
        allAuctionsWindow = new JFrame("Active auctions");
        JPanel allAuctionsPane = new JPanel();
        allAuctionsWindow.add(allAuctionsPane);
        allAuctionsWindow.setSize(300,500);
        JTextArea allAuctionsArea = new JTextArea(40,20);
        JScrollPane scroller = new JScrollPane(allAuctionsArea,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        allAuctionsPane.add(scroller);

        //This thread will run every 5 seconds, updating active bids for client
        Thread updateAuctions = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(4000); // sleep for 5 seconds
                        allAuctionsArea.setText("");
                        for (String auctions: buyerInterface.displayAuctions()) {
                            allAuctionsArea.append(auctions);
                        }

                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        updateAuctions.start();
        allAuctionsWindow.setVisible(true);
    }

    public static void main(String args[]) {
        new BuyerGUI();

    }
}
