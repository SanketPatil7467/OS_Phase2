import java.io.*;
import java.util.Random;

class PCB {
    public int jobID, TTL, TLL;
    public int TTC, LLC;
}

class VM {
    public FileInputStream infile;
    public FileWriter outfile;
    public char[][] Memory = new char[300][4];
    public char[] buffer = new char[40];
    public char[] IR = new char[4];
    public char[] R = new char[4];
    public boolean C;
    public int IC;
    public int SI, PI, TI;
    public PCB pcb;
    public int PTR;
    public int PTE;
    public int RA, VA;
    public int[] visited = new int[30];
    public int pageTablePTR;
    public int page_fault_valid = 0; // 1-Valid 0-Invalid
    public boolean Terminate;
    public int pageNo;
    public boolean run_mos;
    public Random random = new Random();

    public void init() {

        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = ' ';
        }

        for (int i = 0; i < 300; i++) {
            for (int j = 0; j < 4; j++) {
                Memory[i][j] = ' ';
            }
        }

        for (int i = 0; i < 4; i++) {
            IR[i] = '\0';
            R[i] = '\0';
        }

        C = true;
        IC = 0;
        SI = PI = TI = 0;

        pcb = new PCB();
        pcb.jobID = pcb.TLL = pcb.TTL = pcb.TTC = pcb.LLC = 0;

        PTR = PTE = pageNo = -1;
        for (int i = 0; i < 30; i++) {
            visited[i] = 0;
        }
        pageTablePTR = 0;
        Terminate = false;
    }

    public void resetBuffer() {
        for (int i = 0; i < 40; i++) {
            buffer[i] = ' ';
        }
    }

    public int allocate() {
        // int pageNo;
        boolean check = true;
        while (check) {
            pageNo = random.nextInt(30);
            if (visited[pageNo] == 0) {
                visited[pageNo] = 1;
                check = false;
            }
        }
        return pageNo;
    }

    public void MOS() {
        if (TI == 0 && SI == 1) {
            READ();
        } else if (TI == 0 && SI == 2) {
            WRITE();
        } else if (TI == 0 && SI == 3) {
            TERMINATE(0);
        } else if (TI == 2 && SI == 1) {
            TERMINATE(3);
        } else if (TI == 2 && SI == 2) {
            WRITE();
            TERMINATE(3);
        } else if (TI == 2 && SI == 3) {
            TERMINATE(0);
        } else if (TI == 0 && PI == 1) {
            TERMINATE(4);
        } else if (TI == 0 && PI == 2) {
            TERMINATE(5);
        } else if (TI == 0 && PI == 3) {
            if (page_fault_valid == 1) {
                System.out.print("Valid Page Fault: ");
                pageNo = allocate();
                Memory[PTE][2] = (char) (pageNo / 10 + '0');
                Memory[PTE][3] = (char) (pageNo % 10 + '0');
                pageTablePTR++;
                PI = 0;
                System.out.println("Allocated Page Number: " + pageNo);
            } else {
                pcb.TTC--;
                TERMINATE(6);
            }
        } else if (TI == 2 && PI == 1) {
            TERMINATE(3);
        } else if (TI == 2 && PI == 2) {
            TERMINATE(3);
        } else if (TI == 2 && PI == 3) {
            TERMINATE(3);
        }
    }

    private void READ() {
        System.out.println("Read function called");

        String data = new String(buffer).trim();
        if (data.startsWith("$END")) {
            pcb.TTC--;
            TERMINATE(1);
            return;
        }

        int len = data.length();
        for (int i = 0; i < len; i++) {
            buffer[i] = data.charAt(i);
        }

        int buff = 0, mem_ptr = RA, end = RA + 10;
        while (buff < 40 && buffer[buff] != '\0' && mem_ptr < end) {
            for (int i = 0; i < 4; i++) {
                Memory[mem_ptr][i] = buffer[buff];
                buff++;
            }
            mem_ptr++;
        }

        resetBuffer();
        SI = 0;
    }

    private void WRITE() {
        System.out.println("Write function called");

        pcb.LLC++;
        if (pcb.LLC > pcb.TLL) {
            pcb.LLC--;
            TERMINATE(2);
            return;
        }

        try {
            outfile = new FileWriter("output.txt", true);
            StringBuilder output = new StringBuilder();

            int i = RA;
            String s;
            int k = 0;

            if (RA != -1) {
                for (int j = RA; j < RA + 10; j++) {
                    for (int l = 0; l < 4; l++) {
                        output.append(Memory[j][l]);
                    }
                }
                outfile.write(output.toString() + "\n");
            } else {
                TERMINATE(5); // Operand Error
                return;
            }

            SI = 0;
            outfile.write("\n");
            outfile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void TERMINATE(int EM) {
        Terminate = true;
        try {
            outfile = new FileWriter("output.txt", true);
            outfile.write("\n");
            outfile.write("Job Id: " + pcb.jobID + "\n");
            switch (EM) {
                case 0:
                    outfile.write("No Error: PROGRAM EXECUTED SUCCESSFULLY\n");
                    break;
                case 1:
                    outfile.write("Error: OUT IF DATA ERROR\n");
                    break;
                case 2:
                    outfile.write("Error: LINE LIMIT EXCEEDED\n");
                    break;
                case 3:
                    if (TI == 2 && PI == 1) {
                        outfile.write("Error: OPERATION CODE ERROR\n");
                    }
                    if (TI == 2 && PI == 2) {
                        outfile.write("Error: OPERAND ERROR\n");
                    }
                    outfile.write("Error: TIME LIMIT EXCEEDED\n");
                    break;
                case 4:
                    outfile.write("Error: OPERATION CODE ERROR\n");
                    break;
                case 5:
                    pcb.TTC--;
                    outfile.write("Error: OPERAND ERROR\n");
                    break;
                case 6:
                    outfile.write("Error: INVALID PAGE FAULT\n");
            }
            outfile.write("\nIC: " + IC + "\n");
            outfile.write("IR: ");
            for (int i = 0; i < 4; i++) {
                if (IR[i] != '\0')
                    outfile.write(IR[i]);
            }
            outfile.write("\n");
            outfile.write("TTC: " + pcb.TTC + "\n");
            outfile.write("LLC: " + pcb.LLC + "\n");
            outfile.write("\n\n\n");
            SI = 0;
            PI = 0;
            TI = 0;
            outfile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void LOAD() {
        try {
            infile = new FileInputStream("input_phase2.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(infile));
            String s;

            while ((s = br.readLine()) != null) {
                if (s.startsWith("$AMJ")) {
                    init();
                    System.out.println("New Job started");
                    pcb.jobID = Integer.parseInt(s.substring(4, 8));
                    pcb.TTL = Integer.parseInt(s.substring(8, 12));
                    pcb.TLL = Integer.parseInt(s.substring(12, 16));
                    PTR = allocate() * 10;

                    for (int i = PTR; i < PTR + 10; i++) {
                        for (int j = 0; j < 4; j++) {
                            Memory[i][j] = '*';
                        }
                    }
                    System.out.println("\nAllocated Page for Page Table: " + PTR / 10);
                    System.out.println("jobID: " + pcb.jobID);
                    System.out.println("TTL: " + pcb.TTL);
                    System.out.println("TLL: " + pcb.TLL);
                } else if (s.startsWith("$DTA")) {
                    System.out.println("Data card loading");
                    resetBuffer();
                    MOSstartexe();
                } else if (s.startsWith("$END")) {
                    System.out.println("END of Job");
                    for (int i = 0; i < 300; i++) {
                        if (i == PTR) {
                            System.out.println("---Page Table---");
                        }
                        if (i == PTR + 10) {
                            System.out.println("--Page Table End--");
                        }
                        System.out.print("[ " + i + " ] : ");
                        for (int j = 0; j < 4; j++) {
                            System.out.print(Memory[i][j] + " ");
                        }
                        System.out.println();
                    }
                } else {
                    resetBuffer();
                    pageNo = allocate();
                    Memory[PTR + pageTablePTR][2] = (char) (pageNo / 10 + '0');
                    Memory[PTR + pageTablePTR][3] = (char) (pageNo % 10 + '0');
                    pageTablePTR++;
                    System.out.println("Program Card loading");
                    System.out.println("Allocated page number for program card = " + pageNo);
                    int length = s.length();
                    for (int i = 0; i < length; i++) {
                        buffer[i] = s.charAt(i);
                    }
                    int buff = 0;
                    IC = pageNo * 10;
                    int end = IC + 10;
                    while (buff < 40 && buffer[buff] != '\0' && IC < end) {
                        for (int j = 0; j < 4; j++) {
                            if (buffer[buff] == 'H') {
                                Memory[IC][j] = 'H';
                                buff++;
                                break;
                            }
                            Memory[IC][j] = buffer[buff];
                            buff++;
                        }
                        IC++;
                    }
                }
            }
            infile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int ADDRESSMAP(int VA) {
        if (0 <= VA && VA < 100) {
            PTE = PTR + (VA / 10);
            if (Memory[PTE][2] == '*') {
                PI = 3;
                System.out.println("\nPage fault occurred");
                System.out.println("TI= " + TI + "\nSI= " + SI + "\nPI= " + PI);
                MOS();
            } else {
                String p = String.valueOf(Memory[PTE][2]) + Memory[PTE][3];
                int pageNo = Integer.parseInt(p);
                RA = pageNo * 10 + (VA % 10);
                System.out.println("\nReturned RA= " + RA);
                return RA;
            }
        } else {
            PI = 2;
            System.out.println("\nOperand error called");
            MOS();
        }
        return pageNo * 10;
    }

    private void MOSstartexe() {
        IC = 0;
        executeUserProgram();
    }

    private void executeUserProgram() {
        while (!Terminate) {
            RA = ADDRESSMAP(IC);
            if (PI != 0) {
                return;
            }
            for (int i = 0; i < 4; i++) {
                IR[i] = Memory[RA][i];
            }
            System.out.println("Instruction register");
            for (int i = 0; i < 4; i++) {
                System.out.print(IR[i]);
            }
            System.out.println();
            IC++;
            String op = String.valueOf(IR[2]) + IR[3];
            System.out.println("\nInstruction = " + IR[0] + IR[1] + IR[2] + IR[3]);

            if (IR[0] == 'G' && IR[1] == 'D') {
                SIMULATION();
                page_fault_valid = 1;
                if (!Character.isDigit(IR[2]) || !Character.isDigit(IR[3])) {
                    PI = 2;
                    run_mos = true;
                } else {
                    VA = Integer.parseInt(op);
                    System.out.println("\nVirtual address = " + VA);
                    RA = ADDRESSMAP(VA);
                    System.out.println(RA);
                    SI = 1;
                    run_mos = true;
                }
            } else if (IR[0] == 'P' && IR[1] == 'D') {
                SIMULATION();
                page_fault_valid = 0;
                if (!Character.isDigit(IR[2]) || !Character.isDigit(IR[3])) {
                    PI = 2;
                    pcb.TTC--;
                    run_mos = true;
                } else {
                    VA = Integer.parseInt(op);
                    RA = ADDRESSMAP(VA);
                    SI = 2;
                    run_mos = true;
                }
            } else if (IR[0] == 'H' && IR[1] == '\0') {
                SIMULATION();
                SI = 3;
                run_mos = true;
                return;
            } else if (IR[0] == 'L' && IR[1] == 'R') {
                SIMULATION();
                page_fault_valid = 0;
                if (!Character.isDigit(IR[2]) || !Character.isDigit(IR[3])) {
                    PI = 2;
                    run_mos = true;
                } else {
                    VA = Integer.parseInt(op);
                    RA = ADDRESSMAP(VA);
                    for (int i = 0; i < 4; i++) {
                        R[i] = Memory[RA][i];
                    }
                }
            } else if (IR[0] == 'S' && IR[1] == 'R') {
                SIMULATION();
                page_fault_valid = 1;
                if (!Character.isDigit(IR[2]) || !Character.isDigit(IR[3])) {
                    PI = 2;
                    run_mos = true;
                } else {
                    VA = Integer.parseInt(op);
                    RA = ADDRESSMAP(VA);
                    for (int i = 0; i < 4; i++) {
                        Memory[RA][i] = R[i];
                    }
                }
            } else if (IR[0] == 'C' && IR[1] == 'R') {
                SIMULATION();
                page_fault_valid = 0;
                if (!Character.isDigit(IR[2]) || !Character.isDigit(IR[3])) {
                    PI = 2;
                    run_mos = true;
                } else {
                    VA = Integer.parseInt(op);
                    RA = ADDRESSMAP(VA);
                    StringBuilder s1 = new StringBuilder();
                    StringBuilder s2 = new StringBuilder();
                    for (int i = 0; i < 4; i++) {
                        s1.append(Memory[RA][i]);
                        s2.append(R[i]);
                    }
                    if (s1.toString().equals(s2.toString())) {
                        C = true;
                    } else {
                        C = false;
                    }
                }
            } else if (IR[0] == 'B' && IR[1] == 'T') {
                SIMULATION();
                page_fault_valid = 0;
                if (!Character.isDigit(IR[2]) || !Character.isDigit(IR[3])) {
                    PI = 2;
                    run_mos = true;
                } else {
                    if (C) {
                        String j = String.valueOf(IR[2]) + IR[3];
                        IC = Integer.parseInt(j);
                    }
                }
            } else {
                SIMULATION();
                PI = 1;
                SI = 0;
                pcb.TTC--;
                run_mos = true;
            }
            if (run_mos) {
                System.out.println("\nMOS called for - " + IR[0] + IR[1] + IR[2] + IR[3]);
                MOS();
            }
        }
    }

    private void SIMULATION() {
        if (IR[0] == 'G' && IR[1] == 'D') {
            pcb.TTC += 1;
        } else if (IR[0] == 'P' && IR[1] == 'D') {
            pcb.TTC += 1;
        } else if (IR[0] == 'H') {
            pcb.TTC += 1;
        } else if (IR[0] == 'L' && IR[1] == 'R') {
            pcb.TTC += 1;
        } else if (IR[0] == 'S' && IR[1] == 'R') {
            pcb.TTC += 1;
        } else if (IR[0] == 'C' && IR[1] == 'R') {
            pcb.TTC += 1;
        } else if (IR[0] == 'B' && IR[1] == 'T') {
            pcb.TTC += 1;
        } else {
            pcb.TTC += 1;
        }

        if (pcb.TTC > pcb.TTL) {
            TI = 2;
            run_mos = true;
        }
    }

    public VM() {
        init();
        LOAD();
    }

    public static void main(String[] args) {
        VM vm = new VM();
    }
}