import java.io.*;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

@SuppressWarnings("ALL")
public class Parser {
    Hashtable<String, Object> output = new Hashtable<>();
    Queue<String> readyQueue = new LinkedList<>();
    Queue<String> runningQueue = new LinkedList<>();
    Queue<String> finishedQueue = new LinkedList<>();
    Memory memory;
    String filename;
    State state;
    int pc;
    int min;
    int max;
    int instToExecute;
    Object a;
    Object b;
    int filenum = 0;
    private int currentProgramPCB;

    public Parser() {
        try {
            //Initializing the memory
            int memorySize = getMemorySize();
            memory = new Memory(memorySize, filenum);
            //Adding all available programs to READY QUEUE and to MEMORY
            fillQueue();
            //Displaying the initial state of the memory
            memory.displaymemory();
            System.out.println("___________________EXECUTING PROGRAMS___________________");
            //Starting the scheduling process using ROUND ROBIN algorithm
            scheduler();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Parser();
    }

    private static int countLineBufferedReader(String fileName) {
        int lines = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader("src/" + fileName + ".txt"))) {
            while (reader.readLine() != null) lines++;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    } //returns the number of instructions in a program

    private int getMemorySize() {
        int counter = 0;
        String filename;
        while (true) {
            filename = "Program " + (filenum + 1);
            try {
                new FileReader("src/" + filename + ".txt");
                filenum++;
                counter += countLineBufferedReader(filename);
            } catch (FileNotFoundException e) {
                return ((7 * filenum) + counter);
            }
        }
    }//returns the size that the memory should be initialized with

    private void scheduler() throws FileNotFoundException {
        while (!readyQueue.isEmpty()) {
            //Retriving the program that is ready to be executed
            filename = readyQueue.remove();
            //Retriving program information [PCB,Variables] from memory
            String[] pid = filename.split(" ");
            returnProgramInfo("P" + pid[1]);
            String line;
            instToExecute = pc + min;
            int quanta = 0;
            int finishedQuanta = countLineBufferedReader(filename);
            boolean maxReached = false;
            //Adding program to Running Queue
            runningQueue.add(filename);

                System.out.println(state + " " + filename);
                for (int i = 0; i < 2; i++) {
                    try {
                        line = (String) memory.indexes[instToExecute];//Instruction to be executed
                        interpreter(line);
                        instToExecute++;
                        pc = instToExecute - min;
                        quanta++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (instToExecute > max) {
                        maxReached = true;
                        break;
                    }

            }
            //If the last instruction in the program wasn't executed then its returned to the ready queue
            if (!maxReached) {
                state = State.READY;
                Object newa = output.get("a");
                Object newb = output.get("b");
                System.out.println(runningQueue.peek() + " was PREEMPTED and took " + quanta + " quanta");
                //Removing the program from the RUNNING QUEUE and adding it to the READY QUEUE
                readyQueue.add(runningQueue.remove());
                //Saving the current state of the program and its info[PCB,variables] in the memory
                saveProgramInfo(state, newa, newb);
            }
            //Else then it finished executing and is added to the finished queue
            else {
                state = State.FINISHED;
                Object newa = output.get("a");
                Object newb = output.get("b");
                System.out.println(runningQueue.peek() + " " + State.FINISHED + " and took " + finishedQuanta+ " quanta in " +
                        "total");
                //Removing the program from the RUNNING QUEUE and adding it to the FINISHED QUEUE
                finishedQueue.add(runningQueue.remove());
                //Saving the current state of the program and its info[PCB,variables] in the memory
                saveProgramInfo(state, newa, newb);
            }
        }
    }

    private void returnProgramInfo(String filename) {
        System.out.println("________READING FROM MEMORY________");
        boolean flag = false;
        for (int i = 0; i < memory.indexes.length; i++) { //Searching for the program's PCB in the memory using its ID
            if (memory.indexes[i].equals(filename)) { //If found we retrieve its information
                currentProgramPCB = i; //Indicates which index does this program's PCB start at
                state = State.RUNNING;
                //PC
                int index = i + 2;
                System.out.println("Reading " + memory.indexes[index] + " from index " + index);
                pc = (int) memory.indexes[index];
                //MIN
                index = i + 3;
                System.out.println("Reading " + memory.indexes[index] + " from index " + index);
                min = (int) memory.indexes[index];
                //MAX
                index = i + 4;
                System.out.println("Reading " + memory.indexes[index] + " from index " + index);
                max = (int) memory.indexes[index];
                //Variable a
                index = i + 5;
                System.out.println("Reading " + memory.indexes[index] + " from index " + index);
                a = memory.indexes[index];
                //Variable b
                index = i + 6;
                System.out.println("Reading " + memory.indexes[index] + " from index " + index);
                b = memory.indexes[index];
                output.put("a", a);
                output.put("b", b);
                flag = false;
                break;
            } else flag = true;
        }
        if (flag) System.out.println("Data not found in memory");
        System.out.println("_________________________________");
    }

    private void saveProgramInfo(State state, Object a, Object b) {
        System.out.println("________WRITING INTO MEMORY________");
        //SAVING STATE
        int index = currentProgramPCB + 1;
        System.out.println("Writing " + state + " in index " + index);
        memory.indexes[index] = state;
        //SAVING PC
        index = currentProgramPCB + 2;
        System.out.println("Writing " + pc + " in index " + index);
        memory.indexes[index] = pc;
        //SAVING VARIABLE a
        index = currentProgramPCB + 5;
        System.out.println("Writing " + a + " in index " + index);
        memory.indexes[index] = a;
        //SAVING VARIABLE b
        index = currentProgramPCB + 6;
        System.out.println("Writing " + b + " in index " + index);
        memory.indexes[index] = b;
    }

    private void fillQueue() {
        for (int i = 1; i <= filenum; i++) {
            String filename = "Program " + i;
            //Getting Array of Instructions
            String[] Instructions = new String[countLineBufferedReader(filename)];
            //Adding Programs to Ready Queue
            readyQueue.add(filename);
            try {
                FileInputStream fis = new FileInputStream("src/" + filename + ".txt");
                Scanner sc = new Scanner(fis);    //file to be scanned
                int j = 0;
                while (sc.hasNextLine()) { //Adding program's instructions to the instructions array
                    Instructions[j] = sc.nextLine();
                    j++;
                }
                sc.close();
                memory.addtoMemory(("P" + i), State.READY, 0, Instructions); //adding program to memory
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void interpreter(String file) throws IOException {
        String[] splitStr = file.split(" ");
        switch (splitStr[0]) {
            case "assign":
                assignHelper(splitStr);
                break;
            case "add":
                addHelper(splitStr[1], splitStr[2]);
                break;
            case "writeFile":
                writeFile(splitStr[1], splitStr[2]);
                break;
            case "readFile":
                readFile(splitStr[1]);
                break;
            case "print":
                print(splitStr[1]);
                break;
            default:
                System.out.println("no match");
                break;
        }
    }

    private void addHelper(String a, String b) {
        int val1 = Integer.parseInt((String) output.get(a));
        int val2 = Integer.parseInt((String) output.get(b));
        int result = add(val1, val2);
        output.put(a, result);
    }

    private String readFile(String filename) {
        String line;
        StringBuilder result = new StringBuilder();
        try {
            FileReader fr = new FileReader("src/" + filename + ".txt");
            BufferedReader br = new BufferedReader(fr);
            while ((line = br.readLine()) != null) {
                result.append(line);
                result.append("\n");
            }
        } catch (IOException e) {
            System.out.println("Please make sure that the filename and/or extension are correct");
        }
        return result.toString();
    }

    private void writeFile(String filename, String data) {
        BufferedWriter writer;
        String line = "\n";
        filename = (output.get(filename) != null) ? (String) output.get(filename) : filename;
        data = (output.get(data) != null) ? (String) output.get(data) : data;
        try {
            writer = new BufferedWriter(new FileWriter("src\\" + filename + ".txt", true));
            writer.append(data);
            writer.append(line);
            writer.close();
            System.out.println("File Appended and/or created successfully");

        } catch (Exception e) {
            System.out.println("Cannot write this data to file.Please make sure that filename and data are correct");

        }
    }

    private int add(int a, int b) {
        return (a + b);
    }

    private void assignHelper(String[] splitStr) {
        String x = splitStr[1];
        String s;
        String result;
        if (splitStr[2].equals("readFile")) {
            s = (output.get(splitStr[3]) != null) ? (String) output.get(splitStr[3]) : readFile(splitStr[3]);
            result = readFile(s);
            assign(x, result);
        } else if (splitStr[2].equals("input")) {
            Object userInput = input();
            assign(x, userInput);
        } else {
            assign(x, splitStr[2]);
        }
    }

    private Object input() {
        System.out.print("Please enter input: ");
        Scanner sc = new Scanner(System.in);
        return sc.nextLine();
    }

    private void assign(String varName, Object varValue) {
        output.put(varName, varValue);
    }

    private void print(Object printable) {
        String lines;
        if (printable instanceof BufferedReader) {
            try {
                while ((lines = ((BufferedReader) printable).readLine()) != null) {
                    System.out.println(lines);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (output.get(printable) != null) {
            System.out.println(output.get(printable));
        } else {
            System.out.println(printable);
        }
    }

}
