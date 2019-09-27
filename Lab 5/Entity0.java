public class Entity0 extends Entity
{    
    //Globals
    final int numNodes;
    int[] rowOfInterest; // Tracks row of DV table associated w/ this node
    
    // Perform any necessary initialization in the constructor
    public Entity0()
    {
        //Inits
        numNodes = 4;
        int source = 0;
        int dest;
        Packet pkt;
        
        //Print Statement
        System.out.println("\nNode " + source + ": Initialization");

        //Setup row of interest
        rowOfInterest = new int[numNodes];
        rowOfInterest[0] = 0;
        rowOfInterest[1] = 1;
        rowOfInterest[2] = 3;
        rowOfInterest[3] = 7;
        
        //Setup initial DV Table
        //Set all links to infinity
        for(int i = 0; i < numNodes; i ++){
            for(int j = 0; j < numNodes; j++){
              distanceTable[i][j] = 999;  
            }//for
        }//for
        
        distanceTable[source] = rowOfInterest; // assign the known row to DV table       
        
        //Send to immediate neighbors
        //To node 1
        dest = 1;
        pkt = new Packet(source, dest, rowOfInterest);
        NetworkSimulator.toLayer2(pkt);
        System.out.println("Node " + source + " - Sending to Node: " + dest);
        
        //To node 2
        dest = 2;
        pkt = new Packet(source, dest, rowOfInterest);
        NetworkSimulator.toLayer2(pkt);
        System.out.println("Node " + source + " - Sending to Node: " + dest);
        
        //To node 3
        dest = 3;
        pkt = new Packet(source, dest, rowOfInterest);
        NetworkSimulator.toLayer2(pkt);
        System.out.println("Node " + source + " - Sending to Node: " + dest);
    
    }//Entity0
    
    // Handle updates when a packet is received.  Students will need to call
    // NetworkSimulator.toLayer2() with new packets based upon what they
    // send to update.  Be careful to construct the source and destination of
    // the packet correctly.  Read the warning in NetworkSimulator.java for more
    // details.
    public void update(Packet p)
    {        
        int rcvdSource = p.getSource();
        int rcvdDest = p.getDest();
        boolean neededToUpdate = false;
        
        //Print Rcvd Statement
        System.out.println("\nRecieved Packet: " + p.toString());

        //Update Table
        for(int i = 0; i < numNodes; i ++){
            
            //Update received row with any new values
            if(p.getMincost(i) < distanceTable[rcvdSource][i]){

                distanceTable[rcvdSource][i] = p.getMincost(i); // use row received to update table
                
                // Send to neighbors who haven't received these rows                
                int dest; 
                Packet pkt;
                if(rcvdSource == 1){
                    dest = 3;
                    pkt = new Packet(rcvdSource, dest, distanceTable[rcvdSource]);
                    NetworkSimulator.toLayer2(pkt);
                    System.out.println("Node " + rcvdSource + " - Sending to Node: " + dest);
                }//if
                else if(rcvdSource == 3){
                    dest = 1;
                    pkt = new Packet(rcvdSource, dest, distanceTable[rcvdSource]);
                    NetworkSimulator.toLayer2(pkt);
                    System.out.println("Node " + rcvdSource + " - Sending to Node: " + dest);
                }//else if
                
            }//if
                
            // Perform check to update row of interest
            int min1, min2;
            int currentMin1, currentMin2, currentMin3;
            int otherMin1, otherMin2, otherMin3;
            if(i != rcvdDest){
                // BELLMAN FORD ALGORITHM
                // Using row of interest
                currentMin1 = rowOfInterest[1];
                currentMin2 = rowOfInterest[2];
                currentMin3 = rowOfInterest[3];
                
                // Using other rows of table
                otherMin1 = distanceTable[1][i];
                otherMin2 = distanceTable[2][i];
                otherMin3 = distanceTable[3][i];
                
                // Calculate minimum distances
                min1 = Math.min((currentMin1 + otherMin1), (currentMin2 + otherMin2));
                min2 = Math.min(min1, (currentMin3 + otherMin3));
                
                // Update row of interest
                if(min2 < rowOfInterest[i]){
                                
                    System.out.println("******* Update Detected *******");
                    System.out.println("Changing entry (" + rcvdDest + ", " + i +
                            ") from " + rowOfInterest[i] + " to " + min2);
            
                    rowOfInterest[i] = min2;
                    distanceTable[rcvdDest][i] = min2;
                    neededToUpdate = true;   
                }//if
            }//if
        }//for
        
        //Send updated cost info
        if(neededToUpdate == true){
            int source = 0;
            int dest; 
            Packet pkt;

            //Send to immediate neighbors
            //To node 1
            dest = 1;
            pkt = new Packet(source, dest, rowOfInterest);
            NetworkSimulator.toLayer2(pkt);
            System.out.println("Node " + source + " - Sending to Node: " + dest);

            //To node 2
            dest = 2;
            pkt = new Packet(source, dest, rowOfInterest);
            NetworkSimulator.toLayer2(pkt);
            System.out.println("Node " + source + " - Sending to Node: " + dest);

            //To node 3
            dest = 3;
            pkt = new Packet(source, dest, rowOfInterest);
            NetworkSimulator.toLayer2(pkt);
            System.out.println("Node " + source + " - Sending to Node: " + dest);
                    
        }//if
        
        //Print DVTable
        printDT();
    
    }//update
    
    public void linkCostChangeHandler(int whichLink, int newCost)
    {

        /**************** Not using **********************/
    
    }//linkCostChangeHandler
    
    public void printDT()
    {
        System.out.println();
        System.out.println("           via");
        System.out.println(" D0 |   1   2   3");
        System.out.println("----+------------");
        for (int i = 1; i < NetworkSimulator.NUMENTITIES; i++)
        {
            System.out.print("   " + i + "|");
            for (int j = 1; j < NetworkSimulator.NUMENTITIES; j++)
            {
                if (distanceTable[i][j] < 10)
                {    
                    System.out.print("   ");
                }
                else if (distanceTable[i][j] < 100)
                {
                    System.out.print("  ");
                }
                else 
                {
                    System.out.print(" ");
                }
                
                System.out.print(distanceTable[i][j]);
            }
            System.out.println();
        }
    }
}
