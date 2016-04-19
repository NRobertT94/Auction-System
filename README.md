# Auction-System

What it contains: 
  - database that stores buyers, sellers and auctions details.
  - has 2 types of clients: Sellers and Buyers
  - has a front-end that controls the replicas
  - replicas maintain the state of the database. In case of a couple of replicas crash, the rest of the replicas maintain the state of the clients can interact with the remaining replicas without noticing any changes.
  
How it works: 
  - Clients register
  - Sellers create auctions with starting price and minimum price, and can close auctions
  - Buyers bid for auctions, so when the auction is closed, the highest bidder wins
  - 
The system uses an asymmetric challenge-response protocol to authenticate clients.

