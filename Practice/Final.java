import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

class PCB {
    public int jobID, TTL, TLL;
    public int TTC, LLC;
}

class OS {
    public char[][] M = new char[300][4];
    public char[] buffer = new char[40];
    public char[] IR = new char[4];
    public char[] R = new char[4];
    public int[] visited = new int[30];
    public boolean C;
    public int IC;
    public int SI;
    public int PI;
    public int TI;
    public PCB pcb;
    public int PTR;
    public int PTE;
    public int RA;
    public int VA;
    public int pageTablePTR;
    public int pageFaultFlag = 0;
    public boolean terminateFlag;
    public int pageNo;
    public boolean runMosFlag;
    Random random = new Random();

    FileReader input;
    FileWriter output;

    BufferedReader inputReader;
    BufferedWriter outputReader;

    public OS(String input_file, String output_file) {
        try {
            this.input = new FileReader(input_file);
            this.output = new FileWriter(output_file);
            this.inputReader = new BufferedReader(this.input);
            this.outputReader = new BufferedWriter(this.output);
        } catch (Exception e) {
            System.out.println("Failed to open file.");
            e.printStackTrace();
        }

    }

    public void INIT() {

        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = ' ';
        }

        for (int i = 0; i < 300; i++) {
            for (int j = 0; j < 4; j++) {
                M[i][j] = ' ';
            }
        }
        for (int i = 0; i < 4; i++) {
            IR[i] = '\0';
            R[i] = '\0';
        }

        for (int i = 0; i < 30; i++) {
            visited[i] = 0;
        }

        C = true;
        IC = 0;
        SI = 0;
        TI = 0;
        PI = 0;

        pcb = new PCB();
        pcb.jobID = pcb.TLL = pcb.TTL = pcb.TTC = pcb.LLC = 0;
        PTR = PTE = pageNo = -1;

        pageTablePTR = 0;
        terminateFlag = false;
    }

    public void resetBuffer() {
        for (int i = 0; i < 40; i++) {
            buffer[i] = ' ';
        }
    }

    public int ALLOCATE() {
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

    public void LOAD() {
        String line = "";
        try {
            while ((line = inputReader.readLine()) != null) {
                if (line.charAt(0) == '$' && line.charAt(1) == 'A' && line.charAt(2) == 'M' && line.charAt(3) == 'J') {
                    INIT();
                    pcb.jobID = (line.charAt(4) - 48) * 1000 + (line.charAt(5) - 48) * 100 + (line.charAt(6) - 48) * 10 + (line.charAt(7) - 48);
                    pcb.TTL = (line.charAt(8) - 48) * 1000 + (line.charAt(9) - 48) * 100 + (line.charAt(10) - 48) * 10 + (buffer[11] - 48);
                    pcb.TLL = (line.charAt(12) - 48) * 1000 + (line.charAt(13) - 48) * 100 + (line.charAt(14) - 48) * 10 + (line.charAt(15) - 48);
                    System.out.println("Program card detected JOB ID : " + pcb.jobID);
                    System.out.println("TTL : " + pcb.TTL);
                    System.out.println("TLL : " + pcb.TLL);

                    PTR = ALLOCATE() * 10;

                    for (int i = PTR; i < PTR + 10; i++) {
                        for (int j = 0; j < 4; j++) {
                            M[i][j] = '*';
                        }
                    }

                } else if (line.charAt(0) == '$' && line.charAt(1) == 'D' && line.charAt(2) == 'T' && line.charAt(3) == 'A') {
                    System.out.println("Data Card Loading.");
                    resetBuffer();
                    STARTEXECUTION();
                } else if (line.charAt(0) == '$' && line.charAt(1) == 'E' && line.charAt(2) == 'N' && line.charAt(3) == 'D') {
                    System.out.println("End of Job.");
                } else {
                    resetBuffer();
                    pageNo = ALLOCATE();
                    M[PTR + pageTablePTR][2] = (char) (pageNo / 10 + '0');
                    M[PTR + pageTablePTR][3] = (char) (pageNo % 10 + '0');
                    pageTablePTR++;

                    int length = line.length();
                    for (int i = 0; i < length; i++) {
                        buffer[i] = line.charAt(i);
                    }
                    int buff = 0;
                    IC = pageNo * 10;
                    int end = IC + 10;

                    while (buff < 40 && buffer[buff] != '\0' && IC < end) {
                        for (int j = 0; j < 4; j++) {
                            if (buffer[buff] == 'H') {
                                M[IC][j] = 'H';
                                buff++;
                                break;
                            }
                            M[IC][j] = buffer[buff];
                            buff++;
                        }
                        IC++;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void READ() {

        String data = "";

        try {
            data = inputReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (data.startsWith("$END")) {
            pcb.TTC--;
            TERMINATE(1);
            return;
        }

        for (int i = 0; i < data.length(); i++) {
            buffer[i] = data.charAt(i);
        }

        int buff = 0;
        int mem_ptr = RA;
        int end = RA + 10;
        while (buff < 40 && buffer[buff] != '\0' && mem_ptr < end) {
            for (int i = 0; i < 4; i++) {
                M[mem_ptr][i] = buffer[buff];
                buff++;
            }
            mem_ptr++;
        }

        resetBuffer();
        SI = 0;
    }

    public void WRITE() {
        pcb.LLC++;
        if (pcb.LLC > pcb.TLL) {
            pcb.LLC--;
            TERMINATE(2);
            return;
        }

        StringBuilder output2 = new StringBuilder();

        int i = RA;

        if (RA != -1) {
            for (i = RA; i < RA + 10; i++) {
                for (int j = 0; j < 4; j++) {
                    output2.append(M[i][j]);
                }
            }

            try {
                outputReader.write(output2.toString() + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            TERMINATE(5);
            return;
        }

        SI = 0;
        try {
            outputReader.write("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void STARTEXECUTION() {
        IC = 0;
        EXECUTEUSERPROGRAM();

    }

    public void EXECUTEUSERPROGRAM() {
        while (!terminateFlag) {
            RA = ADDRESSMAP(IC);
            if (PI != 0) {
                return;
            }

            for (int i = 0; i < 4; i++) {
                IR[i] = M[RA][i];
            }

            IC++;

            StringBuilder op = new StringBuilder();
            op.append(IR[2]);
            op.append(IR[3]);
            // GD - GET DATA
            if (IR[0] == 'G' && IR[1] == 'D') {
                SIMULATION();
                pageFaultFlag = 1;
                if (!Character.isDigit(IR[2]) || !Character.isDigit(IR[3])) {
                    PI = 2;
                    runMosFlag = true;
                } else {
                    VA = Integer.parseInt(op.toString());
                    RA = ADDRESSMAP(VA);
                    SI = 1;
                    runMosFlag = true;
                }
            }

            else if (IR[0] == 'P' && IR[1] == 'D') {
                SIMULATION();
                pageFaultFlag = 0;
                if (!Character.isDigit(IR[2]) || !Character.isDigit(IR[3])) {
                    PI = 2;
                    pcb.TTC--;
                    runMosFlag = true;
                } else {
                    VA = Integer.parseInt(op.toString());
                    RA = ADDRESSMAP(VA);
                    SI = 2;
                    runMosFlag = true;
                }
            }

            else if (IR[0] == 'H' && IR[1] == '\0') {
                SIMULATION();
                SI = 3;
                runMosFlag = true;
                return;
            }

            // LR - LOAD DATA (Register <- Memory)
            else if (IR[0] == 'L' && IR[1] == 'R') {
                SIMULATION();
                pageFaultFlag = 0;
                if (!Character.isDigit(IR[2]) || !Character.isDigit(IR[3])) {
                    PI = 2;
                    runMosFlag = true;
                    // MOS();
                } else {
                    VA = Integer.parseInt(op.toString());
                    RA = ADDRESSMAP(VA);
                    for (int i = 0; i < 4; i++) {
                        R[i] = M[RA][i];
                    }
                }
            }

            // SR - STORE (Memory <- Register)
            else if (IR[0] == 'S' && IR[1] == 'R') {
                SIMULATION();
                pageFaultFlag = 1;
                if (!Character.isDigit(IR[2]) || !Character.isDigit(IR[3])) {
                    PI = 2;
                    runMosFlag = true;
                }

                else {
                    VA = Integer.parseInt(op.toString());

                    RA = ADDRESSMAP(VA);

                    for (int i = 0; i < 4; i++) {
                        M[RA][i] = R[i];
                    }
                }
            }

            // CR - COMPARE
            else if (IR[0] == 'C' && IR[1] == 'R') {
                SIMULATION();
                pageFaultFlag = 0;
                if (!Character.isDigit(IR[2]) || !Character.isDigit(IR[3])) {
                    PI = 2;
                    runMosFlag = true;

                } else {
                    VA = Integer.parseInt(op.toString());
                    RA = ADDRESSMAP(VA);
                    StringBuilder s1 = new StringBuilder();
                    StringBuilder s2 = new StringBuilder();
                    for (int i = 0; i < 4; i++) {
                        s1.append(M[RA][i]);
                        s2.append(R[i]);
                    }
                    if (s1 == s2) {
                        C = true;
                    } else {
                        C = false;
                    }
                }
            }

            // BT (JUMP if toogle is T)
            else if (IR[0] == 'B' && IR[1] == 'T') {
                SIMULATION();
                pageFaultFlag = 0;
                if (!Character.isDigit(IR[2]) || !Character.isDigit(IR[3])) {
                    PI = 2;
                    runMosFlag = true;
                    // MOS();
                } else {
                    if (C) {
                        StringBuilder j = new StringBuilder();
                        j.append(IR[2]);
                        j.append(IR[3]);
                        IC = Integer.parseInt(j.toString());
                    }
                }
            } else {
                SIMULATION();
                PI = 1;
                SI = 0;
                pcb.TTC--;
                runMosFlag = true;
                // MOS();
            }
            if (runMosFlag) {
                MOS();
            }
        }
    }

    public int ADDRESSMAP(int VA) {
        if (0 <= VA && VA < 100) {
            PTE = PTR + (VA / 10);
            if (M[PTE][2] == '*') {
                PI = 3; // Page fault no such page exist
                MOS();
            } else {
                StringBuilder p = new StringBuilder();

                p.append(M[PTE][2]);
                p.append(M[PTE][3]);
                int pageNo = Integer.parseInt(p.toString());
                RA = pageNo * 10 + (VA % 10);
                return RA;
            }
        } else {
            PI = 2; // Operand Error;
            MOS();
        }
        return pageNo * 10;
    }

    public void MOS() { // Master Mode

        // Case TI and SI
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
        }

        // Case TI and PI
        else if (TI == 0 && PI == 1) {
            TERMINATE(4);
        } else if (TI == 0 && PI == 2) {
            TERMINATE(5);
        } else if (TI == 0 && PI == 3) {

            if (pageFaultFlag == 1) {
                pageNo = ALLOCATE();
                M[PTE][2] = (char) (pageNo / 10 + '0');
                M[PTE][3] = (char) (pageNo % 10 + '0');
                pageTablePTR++;
                PI = 0;
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

    public void TERMINATE(int EM) {
        try {

            terminateFlag = true;
            outputReader.write(String.format("\nJOB ID   :  %d\n", pcb.jobID));
            switch (EM) {
                case 0:
                    outputReader.write(" NO ERROR\n");
                    break;
                case 1:
                    outputReader.write("OUT OF DATA\n");
                    break;
                case 2:
                    outputReader.write("LINE LIMIT EXCEEDED\n");
                    break;
                case 3:

                    if (TI == 2 && PI == 1) {
                        outputReader.write(" OPERATION CODE ERROR\n");

                    }
                    if (TI == 2 && PI == 2) {
                        outputReader.write(" OPERAND ERROR\n");
                    }
                    outputReader.write(" TIME LIMIT EXCEEDED\n");

                    break;
                case 4:
                    outputReader.write(" OPERATION CODE ERROR\n");
                    break;
                case 5:
                    pcb.TTC--;
                    outputReader.write("OPERAND ERROR\n");
                    break;
                case 6:
                    outputReader.write("INVAILD PAGE FAULT\n");
            }
            String IR_str = new String(IR);
            outputReader.write("IC       :  " + IC + "\n");
            outputReader.write("IR       :  " + IR_str + "\n");
            outputReader.write("TTL      :  " + pcb.TTL + "\n");
            outputReader.write("TLL      :  " + pcb.TLL + "\n");
            outputReader.write("TTC      :  " + pcb.TTC + "\n");
            outputReader.write("LLC      :  " + pcb.LLC + "\n");
            outputReader.write("\n\n");

            SI = 0;
            PI = 0;
            TI = 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void SIMULATION() {
        if (IR[0] == 'G' && IR[1] == 'D') {
            pcb.TTC += 1;
        }

        else if (IR[0] == 'P' && IR[1] == 'D') {
            pcb.TTC += 1;
        }

        else if (IR[0] == 'H') {
            pcb.TTC += 1;
        }

        else if (IR[0] == 'L' && IR[1] == 'R') {
            pcb.TTC += 1;
        }

        else if (IR[0] == 'S' && IR[1] == 'R') {
            pcb.TTC += 1;
        }

        else if (IR[0] == 'C' && IR[1] == 'R') {
            pcb.TTC += 1;
        }

        else if (IR[0] == 'B' && IR[1] == 'T') {
            pcb.TTC += 1;
        } else {
            pcb.TTC += 1;
        }

        if (pcb.TTC > pcb.TTL) {
            TI = 2;
            runMosFlag = true;
        }
    }
}

public class Final {
    public static void main(String[] args) {
        OS os = new OS("input_phase2.txt", "output_file.txt");
        os.INIT();
        os.LOAD();
    }
}
