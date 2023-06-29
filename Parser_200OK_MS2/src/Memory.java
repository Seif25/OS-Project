public class Memory {

    Object[] indexes;
    int PCBIndex;
    int instindex;
    int noOfPrograms;

    public Memory(int memorySize, int noOfPrograms) {
        this.noOfPrograms = (noOfPrograms * 7);
        indexes = new Object[memorySize];
        PCBIndex = 0;
        instindex = noOfPrograms * 7;
    }

    public void addtoMemory(String processid, State state, int pc, String[] instructions) {
        if (PCBIndex < noOfPrograms - 1) {
            indexes[PCBIndex++] = processid;
            indexes[PCBIndex++] = state;
            indexes[PCBIndex++] = pc;
            indexes[PCBIndex++] = instindex;
            indexes[PCBIndex++] = instindex + instructions.length - 1;
            indexes[PCBIndex++] = "a = null";
            indexes[PCBIndex++] = "b = null";
        }
        for (String instruction : instructions) {
            indexes[instindex++] = instruction;
        }
    }

    public void displaymemory() {
        System.out.println("___________________MEMORY___________________");
        System.out.println("__________________PCB AREA__________________");
        for (int i = 0; i < indexes.length; i++) {
            System.out.println(i + " " + indexes[i]);
            if (i == noOfPrograms - 1) {
                System.out.println("___________________INSTRUCTIONS AREA___________________");
            }
        }
    }

    /*
            ____Memory Convention____ (Example if we have only 2 programs with 3 instructions each)
            ________PCB Area_________
            0 Program 1 ID
            1 State
            2 PC
            3 Min
            4 Max
            5 Variable a
            6 Variable b
            7 Program 2 ID
            8 State
            9 PC
            10 Min
            11 Max
            12 Variable a
            13 Variable b
            ____Instruction Area____
            14 Instruction 1
            15 Instruction 2
            16 Instruction 3
            17 Instruction 1
            18 Instruction 2
            19 Instruction 3
     */



}
