import java.io.*;
import java.util.*;
import java.util.Arrays;

//defining the PCB class

class PCB{
    private char jobID[] = new char[4];
    private char TTL[] = new char[4];
    private char TLL[] = new char[4];

    //Constructor for the PCB class
    PCB() {
        //jobID
        Arrays.fill(jobID, '0');

        //TTL
        Arrays.fill(TTL, '0');

        //TLL
        Arrays.fill(TLL, '0');
    }

    public PCB(char jobID[], char TTL[], char TLL[]){
        this.jobID = jobID;
        this.TTL = TTL;
        this.TLL = TLL;
    }

    //Creating getters and setters for the PCB class

    public String getJobID() {
        String jobstr = new String(jobID);
        return jobstr;
    }

    public void setJobID(char[] jobID) {
        this.jobID = jobID;
    }

    public char[] getTTL() {
        return TTL;
    }

    public void setTTL(char[] TTL) {
        this.TTL = TTL;
    }

    public char[] getTLL() {
        return TLL;
    }

    public void setTLL(char[] TLL) {
        this.TLL = TLL;
    }

    //toString method to print the PCB object

    @Override

    public String toString() {
        return "PCB{" +
                "jobID=" + String.valueOf(jobID) +
                ", TTL=" + String.valueOf(TTL) +
                ", TLL=" + String.valueOf(TLL) +
                '}';
    }

}

class Cpu {
    
    private char[] IR = new char[4]; // Instruction register
    private boolean C = false; // toggle
    private int IC = 0; // Instruction counter
    private char[] R = new char[4]; // General purpose Register

    //Interrupt
    private int SI =0;
    private int PI =0;
    private int TI =0;

    public Cpu() {
        Arrays.fill(IR, '0'); // Initializing Instruction Register
        this.C = false; // Initializing toggle
        IC = 0; // Initializing Instruction Counter
        Arrays.fill(R, '0'); // Initializing General Purpose Register
    }
    
    public char[] getIR() {
        return this.IR;
    }

    public int getIR(int index) {
        return this.IR[index];
    }

    public void setIR(char[] IR) {
        this.IR = IR;
    }

    public boolean getC() {
        return this.C;
    }

    public void setC(boolean C) {
        this.C = C;
    }

    public int getIC() {
        return this.IC;
    }

    public void setIC(int IC) {
        this.IC = IC;
    }

    public char[] getR() {
        return this.R;
    }

    public void setR(char[] R) {
        this.R = R;
    }

    public int getPI() {
        return PI;
    }

    public void setPI(int PI) {
        this.PI = PI;
    }

    public int getTI() {
        return TI;
    }

    public void setTI(int TI) {
        this.TI = TI;
    }

    public int getSI() {
        return SI;
    }

    public void setSI(int sI) {
        SI = sI;
    }


    //// getter for getting the opcode
    //which is nothing but the first two bytes of the instruction register
    //which can be GD,PD,SR,LR,BT,H,CR
    public String getOpcode() {
        String opcode = "";
        opcode += this.IR[0];
        if(opcode.equals("H")){
            return opcode;
        }
        opcode += this.IR[1];
        return opcode;
    }

    //Similarly creating the getOperand Method which will return
    //the next 2 bytes of the instruction register which is mostly the mem loc
    public int getOperand() {
        return Integer.parseInt(String.valueOf(this.IR[2]) + String.valueOf(this.IR[3]));
    }

    
}


class MainMemory {
    private char[][] M = new char[300][4];
    public MainMemory() {
        this.M = new char[300][4];
    }

    public char[][] getMemory() {
        return this.M;
    }

    public void setMemory(char[][] M) {
        this.M = M;
    }
}

class Helper{
    protected Map.Entry<Integer, int[]> allocate(int[] arr){

        
        //generating a random value from 0-29
        int value = (int)(Math.random() * 30);

        //check wheather it is generated if it is then again generate new value
        while(true){
            if(arr[value] == 0){
                arr[value] = 1;
                break;
            }
            else{
                value = (int)(Math.random() * 30);
            }
        }

        //returning the value and the arr
        //creating a map entry
        Map.Entry<Integer, int[]> pair = new AbstractMap.SimpleEntry<Integer, int[]>(value, arr);

        return pair;
    }
}


class OperatingSystem extends Helper {
    boolean isExceeded = false;
    boolean reachedH = false;
    private MainMemory M;
    private Cpu cpu;

    private FileReader input;
    private FileWriter output;

    private BufferedReader inputReader;
    private BufferedWriter outputReader;

    // The buffer to store the data.
    private char[] buffer = new char[40];

    // Store count of used memory.
    private int used_memory = 0;

    // Page Table Register
    private int pageTableRegister;
    // generated array to store random numbers;
    public int generated[] = new int[30];
           

    //PCB
    private PCB pcb;
    //counters
    private int TTC = 0;
    private int LLC = 0;
    //Temporary variables to get the values in int format
    private int ttl;
    private int tll;
    
    private int valid=0;

    int realAddress =0;

    boolean flag= true;

    public OperatingSystem(String input, String output) {
        // Initializing the input and output file
        try {
            this.input = new FileReader(input);
            this.output = new FileWriter(output);
            this.inputReader = new BufferedReader(this.input);
            this.outputReader = new BufferedWriter(this.output);
        } catch (Exception e) {
            System.out.println("Error in initializing the input and output file");
        }
    }

    // init Method to initialize everything to zero
    public void init() {
        Arrays.fill(this.generated, 0);
        // Inititalizing the main memory
        used_memory =0;
        // Initializing the main memory
        this.M = new MainMemory();

        // Initializing the page table register
        this.pageTableRegister = 0;

        // Initializing the cpu
        this.cpu = new Cpu();

        //Initializing the PCB
        this.pcb = new PCB();
    }

    // LOAD method to load the data from the input file to the main memory
    public void LOAD() {
        String line = "";

        try {
            while ((line = inputReader.readLine()) != null) {
                buffer = line.toCharArray(); // Loading buffer

                if (buffer[0] == '$' && buffer[1] == 'A' && buffer[2] == 'M' && buffer[3] == 'J') {
                    System.out.println("Program card detected with JobID : " + buffer[4] + buffer[5] + buffer[6] + buffer[7]);
                    init();

                    // Initializing the TTC and LLC
                    TTC = 0;
                    LLC = 0;

                    //Initializing the PCB
                    pcb.setJobID(new char[] {buffer[4],buffer[5],buffer[6],buffer[7]});
                    pcb.setTTL(new char[] {buffer[8],buffer[9],buffer[10],buffer[11]});
                    pcb.setTLL(new char[] {buffer[12],buffer[13],buffer[14],buffer[15]});

                    //Printing the PCB
                    System.out.println("\n The PCB is : \n"+pcb.toString()+"\n");

                    //Converting the TTL and TLL to int
                    ttl = Integer.parseInt(String.valueOf(pcb.getTTL()));
                    tll = Integer.parseInt(String.valueOf(pcb.getTLL()));


                    //Now calling the allocate method to allocate the memory
                   //Creating the pageTableRegister
                    //assiging a random value from 0-29 to the pageTableRegister variable 
                    Map.Entry<Integer, int[]> pair = allocate(generated); 
                    pageTableRegister = pair.getKey() * 10; //Page table register 
                    used_memory = pageTableRegister;
                    generated = pair.getValue();

                    //Initializing the PTR block with special characters
                    char[][] memory = M.getMemory();

                    for(int i=pageTableRegister; i<(pageTableRegister+10); i++){
                        System.out.println("The value of i is : "+i+"\n");
                        for(int j=0; j<4; j++){
                            memory[i][j] = '*';
                        }
                    }
                    M.setMemory(memory);
                    continue;


                } else if (buffer[0] == '$' && buffer[1] == 'D' && buffer[2] == 'T' && buffer[3] == 'A') {
                    System.out.println("Data card detected");
                    STARTEXECUTION();
                    continue;

                } else if (buffer[0] == '$' && buffer[1] == 'E' && buffer[2] == 'N' && buffer[3] == 'D') {
                    System.out.println("End card detected\n");
                
                    continue;
                } else {
                     System.out.println("Loading the instructions to memory");
                    char memory[][] = M.getMemory();

                    loadProgram(memory, buffer);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //loadProgram method to load the program to the memory
    private void loadProgram(char[][] memory, char[] buffer) {
        if (used_memory >= (pageTableRegister+10)) {
                    System.out.println("Memory is full");
        }

        //getting the frame number for storing the porgram card
        Map.Entry<Integer, int[]> pair = allocate(generated); 

        //getting the frame number
        int frameNumber = pair.getKey();

        //getting the generated array
        generated = pair.getValue();

        //storing the frame number into the page table register
        memory[used_memory][2] = (char)(frameNumber/10 + '0');
        memory[used_memory][3] = (char)(frameNumber%10 + '0');

        //storing the data into the frame
        int framePtr = frameNumber*10;
        int k = 0;
        for(int i=framePtr; i<(framePtr+10) && k<buffer.length; i++) {
            for(int j=0; j<4 && k<buffer.length; j++) {
                    memory[i][j] = buffer[k++];
            }
        }

        //printing the memory
        M.setMemory(memory);
        used_memory++;
    }

    private void STARTEXECUTION() {
        char memory[][] = M.getMemory();
        cpu.setIC(0);
        EXECUTEUSERPROGRAM(memory);

    }

    private void EXECUTEUSERPROGRAM(char[][] memory) {
        while(true){
            int RIC = addressMap(cpu.getIC());
            cpu.setIR(new char[] {
                memory[RIC][0],
                memory[RIC][1],
                memory[RIC][2],
                memory[RIC][3]

            });

            cpu.setIC(cpu.getIC()+1); //incrementing the IC            

            //checking if the operand is number or not
            if(cpu.getIR(0) != 'H' && (!Character.isDigit(cpu.getIR()[2]) || !Character.isDigit(cpu.getIR()[3]))){
                System.out.println("Invalid operand");
                cpu.setPI(2);
                MOS();
                break;
                //System.exit(0);
            }
            if(cpu.getIR(0) != 'H'){
                realAddress = addressMap(cpu.getOperand());
            }
            // else if(cpu.getIR(0) != 'B' && cpu.getIR(1) != 'T')
            //     realAddress = addressMap(cpu.getOperand());

            //if realAdrees is -1 then there is pagefault
            if(cpu.getPI() != 0 || (cpu.getTI() != 0 && cpu.getPI() != 0)){
                //cpu.setPI(3);
                MOS();
                if(!flag){
                    flag = true;
                    return;
                }
                realAddress = addressMap(cpu.getOperand());
                System.out.println("The real address Now has become is : "+realAddress+"\n");
                cpu.setPI(0);
               // System.exit(0);
            }

            String opcode = cpu.getOpcode();

            M.setMemory(memory);
            
            examine();

            if (isExceeded) {
                isExceeded = false;
                return;
            }
            if (reachedH) {
                reachedH = false;
                return;
            }
            if(cpu.getPI()!=0 || cpu.getTI()!=0){
                MOS();
                return;
            }

            if(!flag) {
                flag = true;
                return;
            }

            M.setMemory(memory);
            SIMULATION();
           
            
        }
        
    }

    private void examine() {
        String opcode = cpu.getOpcode();
        System.out.println("The opcode is : "+opcode+"\n");
        char[][] memory = M.getMemory();
            switch(opcode) {
                case "LR": {
                        
                        cpu.setR(
                            new char[] {
                                memory[realAddress][0],
                                memory[realAddress][1],
                                memory[realAddress][2],
                                memory[realAddress][3]
                            }
                        );
                }
                break;
                case "SR": {
                    System.out.println("The operand is : "+cpu.getOperand()+"\n");
                    char[] arr = cpu.getR();
                    for(char c: arr) {
                        System.out.println(c);
                    }
                    memory[realAddress][0] = arr[0];
                    memory[realAddress][1] = arr[1];
                    memory[realAddress][2] = arr[2];
                    memory[realAddress][3] = arr[3];
                }
                break;
                case "CR": {
                    if(cpu.getR()[0] == memory[realAddress][0] &&
                        cpu.getR()[1] == memory[realAddress][1] &&
                        cpu.getR()[2] ==  memory[realAddress][2] &&
                        cpu.getR()[3] == memory[realAddress][3]
                    ) {
                        cpu.setC(true);
                    }else {
                        cpu.setC(false);
                    }
                }
                break;
                case "BT": {
                    if(cpu.getC())
                    cpu.setIC(cpu.getOperand());

                }
                break;
                case "GD": {
                   System.out.println("\n Inside GD Instructions \n");
                    cpu.setSI(1);
                    M.setMemory(memory);
                    MOS();

                }
                break;
                case "PD": {
                     
                    cpu.setSI(2);
                    M.setMemory(memory);
                    MOS();
                }
                break;
                case "H": {
                     
                    cpu.setSI(3);
                    M.setMemory(memory);
                    MOS();
                    reachedH = true;
                    return;
                }
                

                default: {
                    System.out.println("Invalid opcode");
                    cpu.setPI(1);
                    
                }
            }
            

            M.setMemory(memory);

    }

    private void SIMULATION() {
        TTC++;
        if(TTC == ttl){
            cpu.setTI(2);
        }
    }

    private int addressMap(int va) {
        int pte = pageTableRegister+va/10;

        //checking wheather the page table register is empty or not
        if(M.getMemory()[pte][2] != '*') {
            int realAddress = Integer.parseInt(String.valueOf(M.getMemory()[pte][2]) + String.valueOf(M.getMemory()[pte][3])) * 10 + va%10;

            return realAddress;


        } else {
            cpu.setPI(3);
            return -1;
        }

        

    }

    

     protected int allocate(){

        
        //generating a random value from 0-29
        int value = (int)(Math.random() * 30);
        

        //check wheather it is generated if it is then again generate new value
        while(true){
            if(generated[value] == 0){
                generated[value] = 1;
                System.out.println("The value is : "+value+"\n");
                break;
            }
            else{
                value = (int)(Math.random() * 30);
            }
        }

        //returning the value and the arr
        //creating a map entry
        return value;
    }


    private void WRITE() {
        //incrementing the line counter
        LLC++;
        //checking if the line counter is greater than the tll
        if(LLC > tll){
            System.out.println("The TTL exceeded so calling terminate");
            isExceeded = true;
            cpu.setTI(2);
            TERMINATE(2);
            return;
        }

        //take the data from the memory and put it into the output file
        char[][] memory = M.getMemory();
        int oprand = cpu.getOperand();

        //--converting the las bit to 0
        if(oprand%10 != 0){
            //convert that las bit to 0
            oprand = oprand - (oprand%10);
            
        }
        //printing the memory for refernce
        // printMemory();

        for(int i=realAddress; i<realAddress+10; i++){
            for(int j=0; j<4; j++){
                if(memory[i][j] != '\0'){
                System.out.println("The line is : "+memory[i][j] +"\n");
                    // line.append(memory[i][j]);
                    try {
                        outputReader.write(memory[i][j]);
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                    
                }
            }
        }

        System.out.println("The realAddress NOW in write fun() is : "+realAddress+"\n");

        try {
            //outputReader.write(line.toString());
            outputReader.newLine();
            outputReader.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        cpu.setSI(0);
    }

    

    private void READ() {
        flag = true;
         String line = "";
        try {
             line = inputReader.readLine();
             System.out.println("The Line in read is::" + line);
            if(line == null) {
                System.out.println("Input file is empty");
                
            }else if(line.startsWith("$END")){
                System.out.println("Out of data");
                flag = false;
                TERMINATE(1);
                return;
                //System.out.println("Out of Data Card");
            }
            char[] buffer = line.toCharArray();
            char[][] memory = M.getMemory();
            int oprand = cpu.getOperand();

            //--converting the las bit to 0
            if(oprand%10 != 0){
                //convert that las bit to 0
                oprand = oprand - (oprand%10);
                
            }
            //putting the whole buffer starting from the given operand address
            for (int i = 0; i < line.length();) {
                    memory[realAddress][i % 4] = buffer[i];
                    
                    i++;
                    if (i % 4 == 0) {
                        realAddress++;
                    }

                }
            M.setMemory(memory);
        } catch (Exception e) {
            e.printStackTrace();
        }
        cpu.setSI(0);

    }

    private void MOS() {
        System.out.println("The Switch value is : for TI SI" + cpu.getTI() + cpu.getSI() + "For TI PI " + cpu.getTI() + cpu.getPI());
        switch(""+cpu.getTI()+cpu.getSI()) {
            case "01": {
                READ();
            }
            break;
            case "02": {
                WRITE();
                 
            }break;
            case "03": {
                cpu.setSI(0);
                TERMINATE(0);
            }
            break;
            case "21":TERMINATE(1);
			break;

			case "22": {
                isExceeded= true;
                WRITE();
				TERMINATE(3);
                
            }
			break;

			case "23":{
                TERMINATE(0);
            }
			break;

            case "20": {
                TERMINATE(3);
            }
            
        }
        
        switch ("" + cpu.getTI() + cpu.getPI()) {
            case "01":{
                
                TERMINATE(4);
                
            }
            break;

            case "02":{
                TERMINATE(5);
               
            }
            break;

            case "03": {
                    
                    if(cpu.getIR()[0] == 'G' || cpu.getIR()[0] == 'S') {
                        valid = 1;

                    }
                    if(valid==1){

                        System.out.println("The page fault was valid so: ");
                        //Valid Page Fault
                        //allocate2(br.readLine());
                            valid =0;
                        int al = allocate();
                        char [][] memory = M.getMemory();
                        //storing the frame in the page table 
                        System.out.println("The value of AL is : "+al+"\n");
                        System.out.println("The location in PTR store GD is : "+pageTableRegister+cpu.getIR(2)+"\n");
                        int ir = cpu.getIR(2) - '0';
                        //System.out.print("The value of IR is : "+ir+"\n");
                        memory[pageTableRegister+ir][2] = (char)(al/10 + '0');
                        memory[pageTableRegister+ir][3] = (char)(al%10 + '0');
                        M.setMemory(memory);
                        //printMemory();
                        //cpu.setIC(cpu.getIC()+1);
                        System.out.println("NOW IC:"+cpu.getIC());
                        cpu.setPI(0);
                    }else{
                        flag = false;
                        TERMINATE(6);
                        
                    }
            }
            break;

            case "21":
				TERMINATE(3+4);
				break;
				
			case "22":
				TERMINATE(3+5);
				break;
			
			case "23":
				TERMINATE(3);
				return;
        }
        cpu.setSI(0);
        cpu.setPI(0);

    }

      private void TERMINATE(int code) {
        System.out.println("Terminate called");
        
        try {
                    String line = getErrorMessage(code);
                    
                    System.out.println("The value of EM is : "+line+"\n");
                    outputReader.write(String.format("JOB ID   :  %s\n",pcb.getJobID()));
                    outputReader.write(line);
                    outputReader.write("\n");
                    outputReader.write("IC       :  "+cpu.getIC()+"\n");
                    outputReader.write("IR       :  "+ String.valueOf(cpu.getIR())+"\n");
                    outputReader.write("TTC      :  "+TTC+"\n");
                    outputReader.write("LLC      :  "+LLC+"\n");
                    outputReader.write("\n\n");
                    cpu.setSI(0);
                    cpu.setTI(0);
                    cpu.setPI(0);                    

        } catch (IOException e) {
                    e.printStackTrace();
        }

    }

    private String getErrorMessage(int code) {
        switch(code) {
            case 0: {
                return " No error";
            }
            case 1: {
                return " Out of Data";
            }
            case 2: {
                return " Line Limit Exceeded";
            }
            case 3: {
                return " Time Limit Exceeded";
            }
            case 4: {
                return " Operation Code Error";
            }
            case 5: {
                return " Operand Error";
            }
            case 6: {
                return " Invalid Page Fault";
            }
            case 7: {
                return " Time Limit Exceeded with Opcode Error";
            }
            case 8: {
                return " Time Limit Exceeded with Operand Error";
            }
            default: {
                return " Invalid Error";
            }

        }

    }

}


public class Main {
    public static void main(String[] args) {
        OperatingSystem os = new OperatingSystem("input_phase2.txt", "output.txt");
        System.out.println("\nOS initialized");
        os.init();
        os.LOAD();
    }
}