public class Entity3 extends Entity
{    
    //Globals
    final int numNodes;
    int[] rowOfInterest; // Tracks row of DV table associated w/ this node
    
    // Perform any necessary initialization in the constructor
    public Entity3()
    {
        //Inits
        numNodes = 4;
        int source = 3;
        int dest;
        Packet pkt;

        //Print Statement
        System.out.println("\nNode " + source + ": Initialization");

        //Setup row of interest
        rowOfInterest = new int[numNodes];
        rowOfInterest[0] = 7;
        rowOfInterest[1] = 999;
        rowOfInterest[2] = 2;
        rowOfInterest[3] = 0;
        
        //Setup initial DV Table
        //Set all links to infinity
        for(int i = 0; i < numNodes; i ++){
            for(int j = 0; j < numNodes; j++){
              distanceTable[i][j] = 999;  
            }//for
        }//for
        
        distanceTable[source] = rowOfInterest; // assign the known row to DV table         
        
        //Send to immediate neighbors
        //To node 0
        dest = 0;
        pkt = new Packet(source, dest, rowOfInterest);
        NetworkSimulator.toLayer2(pkt);
        System.out.println("Node " + source + " - Sending to Node: " + dest);
        
        //To node 2
        dest = 2;
        pkt = new Packet(source, dest, rowOfInterest);
        NetworkSimulator.toLayer2(pkt);
        System.out.println("Node " + source + " - Sending to Node: " + dest);

    
    }//Entity3
    
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
        
        //Print Statement
        System.out.println("\nRecieved Packet: " + p.toString());
        
        //Update Table
        for(int i = 0; i < numNodes; i ++){
            
            //Update received row with any new values
            if(p.getMincost(i) < distanceTable[rcvdSource][i]){
                if(distanceTable.length == numNodes)
                    distanceTable[rcvdSource][i] = p.getMincost(i);
                
            }//if
                
            // Perform check to update row of interest
            int min1, min2;
            int currentMin1, currentMin2, currentMin3;
            int otherMin1, otherMin2, otherMin3;
            if(i != rcvdDest){
                // BELLMAN FORD ALGORITHM
                // Using row of interest
                currentMin1 = rowOfInterest[0];
                currentMin2 = rowOfInterest[1];
                currentMin3 = rowOfInterest[2];
                
                // Using other rows of table
                otherMin1 = distanceTable[0][i];
                otherMin2 = distanceTable[1][i];
                otherMin3 = distanceTable[2][i];
                
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
        
        if(neededToUpdate == true){
            int source = 3;
            int dest; 
            Packet pkt;
            
            //Send to immediate neighbors
            //To node 0
            dest = 0;
            pkt = new Packet(source, dest, rowOfInterest);
            NetworkSimulator.toLayer2(pkt);
            System.out.println("Node " + source + " - Sending to Node: " + dest);

            //To node 2
            dest = 2;
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
        System.out.println("         via");
        System.out.println(" D3 |   0   2");
        System.out.println("----+--------");
        for (int i = 0; i < NetworkSimulator.NUMENTITIES; i++)
        {
            if (i == 3)
            {
                continue;
            }
            
            System.out.print("   " + i + "|");
            for (int j = 0; j < NetworkSimulator.NUMENTITIES; j += 2)
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
