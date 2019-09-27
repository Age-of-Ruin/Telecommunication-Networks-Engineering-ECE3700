public class StudentNetworkSimulator extends NetworkSimulator
{
    /*
     * Predefined Constants (static member variables):
     *
     *   int MAXDATASIZE : the maximum size of the Message data and
     *                     Packet payload
     *
     *   int A           : a predefined integer that represents entity A
     *   int B           : a predefined integer that represents entity B 
     *
     * Predefined Member Methods:
     *
     *  void stopTimer(int entity): 
     *       Stops the timer running at "entity" [A or B]
     *  void startTimer(int entity, double increment): 
     *       Starts a timer running at "entity" [A or B], which will expire in
     *       "increment" time units, causing the interrupt handler to be
     *       called.  You should only call this with A.
     *  void toLayer3(int callingEntity, Packet p)
     *       Puts the packet "p" into the network from "callingEntity" [A or B]
     *  void toLayer5(String dataSent)
     *       Passes "dataSent" up to layer 5
     *  double getTime()
     *       Returns the current time in the simulator.  Might be useful for
     *       debugging.
     *  int getTraceLevel()
     *       Returns TraceLevel
     *  void printEventList()
     *       Prints the current event list to stdout.  Might be useful for
     *       debugging, but probably not.
     *
     *
     *  Predefined Classes:
     *
     *  Message: Used to encapsulate a message coming from layer 5
     *    Constructor:
     *      Message(String inputData): 
     *          creates a new Message containing "inputData"
     *    Methods:
     *      boolean setData(String inputData):
     *          sets an existing Message's data to "inputData"
     *          returns true on success, false otherwise
     *      String getData():
     *          returns the data contained in the message
     *  Packet: Used to encapsulate a packet
     *    Constructors:
     *      Packet (Packet p):
     *          creates a new Packet that is a copy of "p"
     *      Packet (int seq, int ack, int check, String newPayload)
     *          creates a new Packet with a sequence field of "seq", an
     *          ack field of "ack", a checksum field of "check", and a
     *          payload of "newPayload"
     *      Packet (int seq, int ack, int check)
     *          chreate a new Packet with a sequence field of "seq", an
     *          ack field of "ack", a checksum field of "check", and
     *          an empty payload
     *    Methods:
     *      boolean setSeqnum(int n)
     *          sets the Packet's sequence field to "n"
     *          returns true on success, false otherwise
     *      boolean setAcknum(int n)
     *          sets the Packet's ack field to "n"
     *          returns true on success, false otherwise
     *      boolean setChecksum(int n)
     *          sets the Packet's checksum to "n"
     *          returns true on success, false otherwise
     *      boolean setPayload(String newPayload)
     *          sets the Packet's payload to "newPayload"
     *          returns true on success, false otherwise
     *      int getSeqnum()
     *          returns the contents of the Packet's sequence field
     *      int getAcknum()
     *          returns the contents of the Packet's ack field
     *      int getChecksum()
     *          returns the checksum of the Packet
     *      int getPayload()
     *          returns the Packet's payload
     *
     */

    /*   Please use the following variables in your routines.
     *   int WindowSize  : the window size
     *   double RxmtInterval   : the retransmission timeout
     *   int LimitSeqNo  : when sequence number reaches this value, it wraps around
     */

    public static final int FirstSeqNo = 0;
    private int WindowSize;
    private double RxmtInterval;
    private int LimitSeqNo;
    
    // Add any necessary class variables here.  Remember, you cannot use
    // these variables to send messages error free!  They can only hold
    // state information for A or B.
    // Also add any necessary methods (e.g. checksum of a String)
    
    private final int A = 0; // used to track source of packets
    private final int B = 1;
    private int stateA = 1; // used to track sender/receiver states
    private int stateB = 1;
    private int globalCount = 0; // used to track completed transfers
    Packet pktAGlobal, pktBGlobal; // used to store the packet data
    
    private int calcChecksum(String payload, int seqNum, int ackNum){
        int chkSum = 0;
        
        for(int i = 0; i < payload.length(); i++)
            chkSum += payload.charAt(i);
        
        chkSum += (seqNum + ackNum);
        
        return chkSum;
    }//calcCheckSum
    
    private boolean corruptionCheck(Packet packet){
        int rcvdChkSum = packet.getChecksum();
        int calcChkSum = calcChecksum(packet.getPayload(), packet.getSeqnum(), packet.getAcknum());
        boolean result = false;
        
        if (rcvdChkSum == calcChkSum)
            result = true;
        
        return result;
        
    }//corruptionCheck
    
    /*
    Return true if checksums match
    */

    // This is the constructor.  Don't touch!
    public StudentNetworkSimulator(int numMessages,
                                   double loss,
                                   double corrupt,
                                   double avgDelay,
                                   int trace,
                                   int seed,
                                   int winsize,
                                   double delay)
    {
        super(numMessages, loss, corrupt, avgDelay, trace, seed);
	WindowSize = winsize;
	LimitSeqNo = winsize+1;
	RxmtInterval = delay;
    }

    
    // This routine will be called whenever the upper layer at the sender [A]
    // has a message to send.  It is the job of your protocol to insure that
    // the data in such a message is delivered in-order, and correctly, to
    // the receiving upper layer.
    protected void aOutput(Message message)
    {
        int seqNum, ackNum, chkSum;
        if(stateA == 1){
            seqNum = 0; ackNum = 0;
            chkSum = calcChecksum(message.getData(), seqNum, ackNum);
            String payload = message.getData();
            pktAGlobal = new Packet(seqNum, ackNum, chkSum, payload);

            System.out.println("\nA: Sending Message " + seqNum);
            toLayer3(A, pktAGlobal);
            startTimer(0, RxmtInterval*2); // RxmtInterval*1 caused erroneous timeouts
            
            stateA = 2;
        }//if
        else if(stateA == 3){
            seqNum = 1; ackNum = 0;
            chkSum = calcChecksum(message.getData(), seqNum, ackNum);
            String payload = message.getData();
            pktAGlobal = new Packet(seqNum, ackNum, chkSum, payload);

            System.out.println("\nA: Sending Message " + seqNum);
            toLayer3(A, pktAGlobal);
            startTimer(0, RxmtInterval*2);
            
            stateA = 4;
            
        }//else if
        else {
            //do nothing
        }//else
    }//aOutput
    
    // This routine will be called whenever a packet sent from the B-side 
    // (i.e. as a result of a toLayer3() being done by a B-side procedure)
    // arrives at the A-side.  "packet" is the (possibly corrupted) packet
    // sent from the B-side.
    protected void aInput(Packet packet)
    {
        int ackNum = packet.getAcknum();
        boolean corrupt = !corruptionCheck(packet);
        if (stateA == 2){
             if(!corrupt && ackNum == 0){
                System.out.println("A: Received ACK " + ackNum);
                stopTimer(0);
                globalCount++; // transfer complete
                System.out.println("\nPackets Completed: " + globalCount);
                stateA = 3;
            }//if
        }//if        
        else if (stateA == 4){
            if(!corrupt && ackNum == 1){
                System.out.println("A: Received ACK " + ackNum);
                stopTimer(0);
                globalCount++; // transfer complete
                System.out.println("\nPackets Completed: " + globalCount);
                stateA = 1;
            }//if
        }//else if    
        else{ 
            //do nothing
        }//else
        
        if (ackNum == -1) 
            System.out.println("A: NAK Received");
        
    }//aInput
    
    // This routine will be called when A's timer expires (thus generating a 
    // timer interrupt). You'll probably want to use this routine to control 
    // the retransmission of packets. See startTimer() and stopTimer(), above,
    // for how the timer is started and stopped. 
    protected void aTimerInterrupt()
    {
        System.out.println("\nTimeout has occured, retransmitting message: " + 
                pktAGlobal.getSeqnum());
        toLayer3(0, pktAGlobal);
        startTimer(0, RxmtInterval*2);

    }//aTimerInterrupt
    
    // This routine will be called once, before any of your other A-side 
    // routines are called. It can be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of entity A).
    protected void aInit()
    {
        /******* Alternating Bit Protocol - Leave Blank ********/
    }
    
    // This routine will be called whenever a packet sent from the B-side 
    // (i.e. as a result of a toLayer3() being done by an A-side procedure)
    // arrives at the B-side.  "packet" is the (possibly corrupted) packet
    // sent from the A-side.
    protected void bInput(Packet packet)
    {
        boolean corrupt = !corruptionCheck(packet);
        int seqNum = packet.getSeqnum();
        int ackNum, chkSum;
        if(stateB == 1){
            ackNum = 0;
            if(!corrupt && seqNum == 0) {
                System.out.println("B: Received Message " + seqNum);
                toLayer5(packet.getPayload()); // deliver payload

                chkSum = calcChecksum("", seqNum, ackNum);
                pktBGlobal = new Packet(seqNum, ackNum, chkSum);
                System.out.println("B: Sending ACK " + ackNum);
                toLayer3(B, pktBGlobal);
                stateB = 2;
            }//if
            else if(corrupt){
                ackNum = -1; //NAK
                chkSum = calcChecksum("", seqNum, ackNum);
                pktBGlobal = new Packet(seqNum, ackNum, chkSum);
                System.out.println("CHECKSUM ERROR: Received packet has been "
                    + "corrupted. Sending NAK.");
                toLayer3(B,pktBGlobal);
            }//else if
            else if(!corrupt && seqNum == 1){
                ackNum = 1;
                chkSum = calcChecksum("", seqNum, ackNum);
                pktBGlobal = new Packet(seqNum, ackNum, chkSum);
                toLayer3(B,pktBGlobal);
            }//else if
        }//if
        else if(stateB == 2){
            ackNum = 1;
            if (!corrupt && seqNum == 1) {
                System.out.println("B: Received Message " + seqNum);
                toLayer5(packet.getPayload()); // deliver payload

                chkSum = calcChecksum("", seqNum, ackNum);
                pktBGlobal = new Packet(seqNum, ackNum, chkSum);
                System.out.println("B: Sending ACK " + ackNum);
                toLayer3(B, pktBGlobal);
                stateB = 1;
            }//if
            else if(corrupt){
                ackNum = -1; //NAK
                chkSum = calcChecksum("", seqNum, ackNum);
                pktBGlobal = new Packet(seqNum, ackNum, chkSum);
                System.out.println("CHECKSUM ERROR: Received packet has been "
                        + "corrupt. Sending NAK.");
                toLayer3(B,pktBGlobal);
            }//else if
            else if(!corrupt && seqNum == 0){
                ackNum = 0;
                chkSum = calcChecksum("", seqNum, ackNum);
                pktBGlobal = new Packet(seqNum, ackNum, chkSum);
                toLayer3(B,pktBGlobal);
            }//else if            
        }//else if
        else{
            //do nothing
        }//else 
    }//bInput
    
    // This routine will be called once, before any of your other B-side 
    // routines are called. It can be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of entity B).
    protected void bInit()
    {
        /******* Alternating Bit Protocol - Leave Blank ********/
    }

    // Use to print final statistics
    protected void Simulation_done()
    {
        /** This method does not work (already defined in super class) **/
        
    }	

}
