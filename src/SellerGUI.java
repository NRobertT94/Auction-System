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

import static javax.crypto.Cipher.getInstance;

/**
 * Created by Robert on 04.11.2015.
 */


public class SellerGUI {
    private SellerInterface sellerInterface;
    private Seller seller;
    private User user;
    private JPanel[] parts = new JPanel[5];
    private PrivateKey privKey;
    private PublicKey pubKey;
    private PublicKey serverPubKey;
    private SecretKey commKey;
    private Signature dsa;
    private JTextArea logArea;
    public SellerGUI() {
        try {
            sellerInterface = (SellerInterface) Naming.lookup("rmi://localhost/AuctionSystem");
            sellerGUI();
            generateKeys();
            //loadUser();
        } catch (Exception e) {
            System.out.print(e);
        }
    }

    //generate public and private keys
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
    // GUI for seller client
    public void sellerGUI() {


        JFrame frame = new JFrame("Seller");
        JPanel panel = new JPanel();
        JTextField[] textFields = new JTextField[8];
        JButton[] buttons = new JButton[5];
        logArea = new JTextArea(20, 60);
        JScrollPane scroll = new JScrollPane(logArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        panel.setLayout(new GridLayout(0, 1));
        frame.setSize(770, 550);
        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //initialize GUI parts
        for (int i = 0; i < parts.length; i++) {
            parts[i] = new JPanel();
        }
        for (int i = 0; i < textFields.length; i++) {
            textFields[i] = new JTextField(10);
        }
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = new JButton();
        }
        sellerActivitiesStatus(false);
        //add to first line (parts[0]) GUI items

        /*********************************************
         *      Register Seller Functionality        *
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
                        seller = new Seller(username,email,pubKey);
                        String response = sellerInterface.createSeller(seller);

                        if(!response.contains("Error")){
                            //log
                            logArea.append("Registering: " + textFields[0].getText() +
                                    "\nEmail: " + textFields[1].getText() +
                                    "\nID: " + response +"\n");
                            //log
                            seller.setId(Integer.parseInt(response));
                            //reset textFields and return seller ID
                            textFields[0].setEditable(false);
                            textFields[1].setEditable(false);
                            buttons[0].setEnabled(false);
                            frame.setTitle("Seller: " + response);
                            displaySellerAuth(false);
                            sellerActivitiesStatus(true);
                            frame.validate();


                            serverPubKey = sellerInterface.getPublicKey();
                            commKey = sellerInterface.getSecretKey("seller",Integer.parseInt(response));
                            user = new User(seller,privKey,pubKey,serverPubKey,commKey);
                            user.saveUser();
                            /////debug
                            System.out.println("\nPublic key algorithm: " + commKey.getAlgorithm() +
                                    "\nPublic key encoded: " + commKey.getEncoded() +
                                    "\nPublic Key format: " + commKey.getFormat());
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
        //add to second line (parts[1]) GUI items
        /*********************************************
         *                  Login                    *
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
                    Seller request = new Seller(username,email,pubKey);
                    boolean auth = authenticate(request);
                    logArea.append("\n-----------------------------------\n" + "logged in: "+ auth +
                            "\n-----------------------------------\n");
                    if(auth){
                       displaySellerAuth(false);
                        sellerActivitiesStatus(true);
                    }

                }
                else{
                    logArea.append("\nComputer not secure!\n");
                }
            }
        });

        /*********************************************
         *             Create Auction                *
         *********************************************/

        parts[2].add(new Label("Auction Name:"));
        parts[2].add(textFields[4]);
        parts[2].add(new Label("Initial Price:"));
        parts[2].add(textFields[5]);
        parts[2].add(new Label("Reserve Price:"));
        parts[2].add(textFields[6]);
        parts[2].add(buttons[2]);
        buttons[2].setText("Create");
        panel.add(parts[2]);
        buttons[2].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(textFields[5].getText().matches("[0-9]+") ||  textFields[6].getText().contains(".") &&
                        textFields[6].getText().matches("[0-9]+") ||  textFields[6].getText().contains(".")){
                    String name = textFields[4].getText();
                    double startingPrice = Double.parseDouble(textFields[5].getText());
                    double minPrice = Double.parseDouble(textFields[6].getText());
                    int comparisonResult = Double.compare(startingPrice,minPrice);

                    //check if reserve price is greater than starting price
                    if(comparisonResult <= 0){
                        Auction auction = new Auction(seller,name,startingPrice,minPrice);
                        String id = null;
                        try {
                            id = sellerInterface.createAuction(seller,auction);
                        } catch (RemoteException e1) {
                            e1.printStackTrace();
                        }
                        seller.addAuction(Integer.parseInt(id),auction);
                        user.saveUser();
                        logArea.append("\nCreated auction: " + textFields[4].getText() +
                                           "\nID:" + id + "\n");

                    }
                    else {
                        logArea.append("Starting price must be lower than reserve price");
                    }
                }
                else{
                    logArea.append("\n Errors found in input... try again...\n: ");
                }

                //reset textfields
                textFields[4].setText("");
                textFields[5].setText("");
                textFields[6].setText("");
            }
        });

        //add to third line (parts[3]) GUI items

        /*********************************************
         *             Close Auction                 *
         *********************************************/

        parts[3].add(new Label("Auction ID: "));
        parts[3].add(textFields[7]);
        parts[3].add(buttons[3]);
        buttons[3].setText("Close");
        panel.add(parts[3]);
        buttons[3].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if(textFields[7].getText().matches("[0-9]+")){
                        String message = sellerInterface.closeAuction(seller,Integer.parseInt(textFields[7].getText()));
                        logArea.append("\n" +message + "\n");
                        textFields[7].setText("");
                    } else {
                        logArea.append("\n Errors found in input... try again...\n: ");
                    }

                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }
            }
        });
        buttons[4].setSize(100,100);
        parts[4].add(buttons[4]);
        buttons[4].setText("Logout");
        panel.add(parts[4]);
        buttons[4].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displaySellerAuth(true);
                sellerActivitiesStatus(false);
                logArea.setText("******** Log ********\n\n");
            }
        });
//        // add all parts to panel
//        for (int i = 0; i < parts.length; i++) {
//            panel.add(parts[i]);
//        }
        panel.add(scroll);
        frame.setVisible(true);

    }
    private void displaySellerAuth(boolean status){
        parts[0].setVisible(status);
        parts[1].setVisible(status);
    }
    private void sellerActivitiesStatus(boolean status){
        for(int i = 2; i<parts.length; i++){
            parts[i].setVisible(status);
        }
    }
    private SealedObject[] challengeServer(){
        String uuid = UUID.randomUUID().toString();
        try {
            Cipher enCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            enCipher.init(Cipher.ENCRYPT_MODE, commKey);
            SealedObject sealedObj = new SealedObject(uuid, enCipher);
            //get response from server
            SealedObject[] response = sellerInterface.challengeServer("seller",seller.getId(),sealedObj);
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
    private boolean handleSvChallenge(String challenge,Seller request){
        try{

                Cipher enCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
                enCipher.init(Cipher.ENCRYPT_MODE, commKey);
                SignedObject sigChallenge = new SignedObject(challenge, privKey, dsa);
                SealedObject[] sealedObjects = new SealedObject[2];
                sealedObjects[0] = new SealedObject(sigChallenge,enCipher);
                sealedObjects[1] = new SealedObject(request,enCipher);
                //get response
                SealedObject[] sObj = sellerInterface.challengeClient("seller",seller.getId(),sealedObjects);
                if(sObj != null){
                    String logDetail = (String) sObj[0].getObject(commKey);
                    SecretKey newKey = (SecretKey) sObj[1].getObject(commKey);
                    System.out.println("Answer from sv: " + logDetail);
                    //renew communication key
                    commKey = newKey;
                    user.setCommKey(commKey);
                    return true;
                }else {
                    logArea.append("Authentication failed. Check username and password");
                    return false;
                }

        }catch(Exception e){
            e.printStackTrace();
        }
        return false;

    }
    private boolean authenticate(Seller request){
        SealedObject[] response = challengeServer();
        String challenge = verifySvResponse(response);
        boolean authenticated = handleSvChallenge(challenge,request);
        return authenticated;
    }
    private void loadUser(){
        String message = JOptionPane.showInputDialog(null, "Load user: ");
        System.out.println("Loading: " + message);
        if(!message.equals("no")){
            user = user.loadUser(message);
            seller = (Seller)user.getUser();
            privKey = user.getPrivateKey();
            pubKey = user.getPublicKey();
            serverPubKey = user.getServerKey();
            commKey = user.getCommKey();
        }

    }
    public static void main(String args[]) throws RemoteException {
        System.out.println("Welcome to auction");
        new SellerGUI();
    }
}
