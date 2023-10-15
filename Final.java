import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

class PCB {
    int JID;
    int TTL;
    int TLL;
    int TTC;
    int LLC;

    public void setJID(int JID) {
        this.JID = JID;
    }

    public void setTTL(int TTL) {
        this.TTL = TTL;
    }

    public void setTLL(int TLL) {
        this.TLL = TLL;
    }

    public void setTTC(int TTC) {
        this.TTC = TTC;
    }

    public void setLLC(int LLC) {
        this.LLC = LLC;
    }

    public int getJID() {
        return JID;
    }

    public int getTTL() {
        return TTL;
    }

    public int getTLL() {
        return TLL;
    }

    public int getTTC() {
        return TTC;
    }

    public int getLLC() {
        return LLC;
    }

}

class OS {
    char[][] M = new char[300][4];
    char[] buffer = new char[40];
    char[] IR = new char[4];
    char[] R = new char[4];
    int[] visited = new int[30];
    boolean C;
    int IC;
    int SI;
    int PI;
    int TI;
    PCB pcb = new PCB();
    int PTR;
    int PTE;
    int RA;
    int VA;
    int pageTablePTR;
    int pageFaultFlag = 0;
    boolean terminateFlag;
    int pageNo;
    boolean runMosFlag;
    String line;

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
        Arrays.fill(buffer, ' ');
        for (int i = 0; i < 300; i++) {
            for (int j = 0; j < 4; j++) {
                M[i][j] = ' ';
            }
        }
        Arrays.fill(IR, '0');
        Arrays.fill(R, '0');
        Arrays.fill(visited, 0);
        C = true;
        IC = 0;
        SI = 0;
        TI = 0;
        PI = 0;
        pcb.setJID(0);
        pcb.setTTL(0);
        pcb.setTLL(0);
        pcb.setTTC(0);
        pcb.setLLC(0);
        pageTablePTR = 0;
        terminateFlag = false;
    }

    public void LOAD() {
        line = "";
        try {
            while ((line = inputReader.readLine()) != null) {
                buffer = line.toCharArray(); // Loading buffer
                if (buffer[0] == '$' && buffer[1] == 'A' && buffer[2] == 'M' && buffer[3] == 'J') {
                    INIT();
                    pcb.setJID((buffer[4] - 48) * 1000 + (buffer[5] - 48) * 100 + (buffer[6] - 48) * 10
                            + (buffer[7] - 48));
                    pcb.setTTL((buffer[8] - 48) * 1000 + (buffer[9] - 48) * 100 + (buffer[10] - 48) * 10
                            + (buffer[11] - 48));
                    pcb.setTTL((buffer[12] - 48) * 1000 + (buffer[13] - 48) * 100 + (buffer[14] - 48) * 10
                            + (buffer[15] - 48));
                    System.out.println("Program card detected JOB ID : " + pcb.getJID());
                    System.out.println("TTL : " + pcb.getTTL());
                    System.out.println("TLL : " + pcb.getTLL());

                    PTR = ALLOCATE() * 10;

                    for (int i = PTR; i < PTR + 10; i++) {
                        for (int j = 0; j < 4; j++) {
                            M[i][j] = '*';
                        }
                    }

                } else if (buffer[0] == '$' && buffer[1] == 'D' && buffer[2] == 'T' && buffer[3] == 'A') {
                    System.out.println("Data Card Loading.");
                    Arrays.fill(buffer, ' ');
                    STARTEXECUTION();
                } else if (buffer[0] == '$' && buffer[1] == 'E' && buffer[2] == 'N' && buffer[3] == 'D') {
                    System.out.println("End of Job.");
                } else {
                    Arrays.fill(buffer, ' ');
                    loadProgram();

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadProgram() {
        pageNo = ALLOCATE();
        M[PTR + pageTablePTR][2] = (char) (pageNo / 10 + '0');
        M[PTR + pageTablePTR][3] = (char) (pageNo % 10 + '0');
        pageTablePTR++;

        int length = line.length();

        // Buffer <-- Program Card
        for (int i = 0; i < length; i++) {
            buffer[i] = line.charAt(i);
        }

        int buff = 0;
        IC = pageNo * 10;
        int end = IC + 10;

        // Memory <-- Buffer
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

    public void READ()
    {

        String data = "";

        try {
            data = inputReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        } 
        if(data == null) {
            // "Input file is empty"
        }
        else if(data.startsWith("$END")){
            pcb.setTTC(pcb.getTTC()-1);
            TERMINATE(1);
            return;
        }

        buffer = data.toCharArray();


        int buff = 0;
        int mem_ptr = RA;
        int end = RA + 10;
        while (buff < 40 && buffer[buff] != '\0' && mem_ptr < end)
        {
            for (int i = 0; i < 4; i++)
            {
                M[mem_ptr][i] = buffer[buff];
                buff++;
            }
            mem_ptr++;
        }

        Arrays.fill(buffer, ' ');
        SI = 0;
    }

    public void WRITE()
    {
        pcb.setLLC(pcb.getLLC() + 1);
        if (pcb.getLLC() > pcb.getTLL())
        {
            pcb.setLLC(pcb.getLLC() - 1);

            TERMINATE(2);
            return;
        }
        String output2 = "";


        int i = RA;

        if (RA != -1)
        {
            for (i = RA; i < RA + 10; i++)
            {
                for (int j = 0; j < 4; j++)
                {
                    output2 += M[i][j];
                }
            }

            try {
                outputReader.write(output2+"\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
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
        while (!terminateFlag)
        {
            RA = ADDRESSMAP(IC);
            if (PI != 0)
            {
                return;
            }

            for (int i = 0; i < 4; i++)
            {
                IR[i] = M[RA][i];
            }

            IC++;

            String op = "";
            op += IR[2];
            op += IR[3];
            // GD - GET DATA
            if (IR[0] == 'G' && IR[1] == 'D')
            {
                SIMULATION();
                pageFaultFlag = 1;
                if (!Character.isDigit(IR[2]) || !Character.isDigit(IR[3]))
                {
                    PI = 2;
                    runMosFlag = true;
                }
                else
                {
                    VA = Integer.parseInt(op);
                    RA = ADDRESSMAP(VA);
                    SI = 1;
                    runMosFlag = true;
                }
            }

            else if (IR[0] == 'P' && IR[1] == 'D')
            {
                SIMULATION();
                pageFaultFlag = 0;
                if (!Character.isDigit(IR[2]) || !Character.isDigit(IR[3]))
                {
                    PI = 2;
                    pcb.TTC--;
                    runMosFlag = true;
                }
                else
                {
                    VA = Integer.parseInt(op);
                    RA = ADDRESSMAP(VA);
                    SI = 2;
                    runMosFlag = true;
                }
            }

            else if (IR[0] == 'H' && IR[1] == '\0')
            {
                SIMULATION();
                SI = 3;
                runMosFlag = true;
                return;
            }

            // LR - LOAD DATA (Register  <- Memory)
            else if (IR[0] == 'L' && IR[1] == 'R')
            {
                SIMULATION();
                pageFaultFlag = 0;
                if (!Character.isDigit(IR[2]) || !Character.isDigit(IR[3]))
                {
                    PI = 2;
                    runMosFlag = true;
                    // MOS();
                }
                else
                {
                    VA = Integer.parseInt(op);
                    RA = ADDRESSMAP(VA);
                    for (int i = 0; i < 4; i++)
                    {
                        R[i] = M[RA][i];
                    }
                }
            }

            // SR - STORE (Memory  <-  Register)
            else if (IR[0] == 'S' && IR[1] == 'R')
            {
                SIMULATION();
                pageFaultFlag = 1;
                if (!Character.isDigit(IR[2]) || !Character.isDigit(IR[3]))
                {
                    PI = 2;
                    runMosFlag = true;
                }

                else
                {
                    VA = Integer.parseInt(op);

                    RA = ADDRESSMAP(VA);

                    for (int i = 0; i < 4; i++)
                    {
                        M[RA][i] = R[i];
                    }
                }
            }

            // CR - COMPARE
            else if (IR[0] == 'C' && IR[1] == 'R')
            {
                SIMULATION();
                pageFaultFlag = 0;
                if (!Character.isDigit(IR[2]) || !Character.isDigit(IR[3]))
                {
                    PI = 2;
                    runMosFlag = true;

                }
                else
                {
                    VA = Integer.parseInt(op);
                    RA = ADDRESSMAP(VA);
                    String s1 = "";
                    String s2 = "";
                    for (int i = 0; i < 4; i++)
                    {
                        s1 += M[RA][i];
                        s2 += R[i];
                    }
                    if (s1 == s2)
                    {
                        C = true;
                    }
                    else
                    {
                        C = false;
                    }
                }
            }

            // BT (JUMP if toogle is T)
            else if (IR[0] == 'B' && IR[1] == 'T')
            {
                SIMULATION();
                pageFaultFlag = 0;
                if (!Character.isDigit(IR[2]) || !Character.isDigit(IR[3]))
                {
                    PI = 2;
                    runMosFlag = true;
                    // MOS();
                }
                else
                {
                    if (C)
                    {
                        String j = "";
                        j += IR[2];
                        j += IR[3];
                        IC = Integer.parseInt(j);
                    }
                }
            }
            else
            {
                SIMULATION();
                PI = 1;
                SI = 0;
                pcb.setTTC(pcb.getTTC() - 1);
                runMosFlag = true;
                // MOS();
            }
            if (runMosFlag)
            {
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
                String p = "";
                p += M[PTE][2];
                p += M[PTE][3];
                int pageNo = Integer.parseInt(p);
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
                // pcb.TTC--;
                int a = pcb.getTTC() - 1;
                pcb.setTTC(a);
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
            outputReader.write(String.format("JOB ID   :  %s\n", pcb.getJID()));
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
                    pcb.setTTC((pcb.getTTC() - 1));
                    outputReader.write("OPERAND ERROR\n");
                    break;
                case 6:
                    outputReader.write("INVAILD PAGE FAULT\n");
            }
            outputReader.write("IC       :  " + IC + "\n");
            outputReader.write("TTL      :  " + pcb.getTTL() + "\n");
            outputReader.write("TLL      :  " + pcb.getTLL() + "\n");
            outputReader.write("TTC      :  " + pcb.getTTC() + "\n");
            outputReader.write("LLC      :  " + pcb.getLLC() + "\n");
            outputReader.write("\n\n");

            SI = 0;
            PI = 0;
            TI = 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public int ALLOCATE() {
        int value;
        while (true) {
            value = (int) (Math.random() * 30);
            if (visited[value] == 0) {
                visited[value] = 1;
                break;
            }
        }

        return value;
    }

    public void SIMULATION() {
        if (IR[0] == 'G' && IR[1] == 'D') {
            pcb.setTTC(pcb.getTTC() + 1);
        }

        else if (IR[0] == 'P' && IR[1] == 'D') {
            pcb.setTTC(pcb.getTTC() + 1);
        }

        else if (IR[0] == 'H') {
            pcb.setTTC(pcb.getTTC() + 1);
        }

        else if (IR[0] == 'L' && IR[1] == 'R') {
            pcb.setTTC(pcb.getTTC() + 1);
        }

        else if (IR[0] == 'S' && IR[1] == 'R') {
            pcb.setTTC(pcb.getTTC() + 1);
        }

        else if (IR[0] == 'C' && IR[1] == 'R') {
            pcb.setTTC(pcb.getTTC() + 1);
        }

        else if (IR[0] == 'B' && IR[1] == 'T') {
            pcb.setTTC(pcb.getTTC() + 1);
        } else {
            pcb.setTTC(pcb.getTTC() + 1);
        }

        if (pcb.getTTC() > pcb.getTTL()) {
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
