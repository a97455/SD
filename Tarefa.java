public class Tarefa {
    private byte[] byteArray;
    private int intValue;
    private static int counter = 4;

    // Constructor
    public Tarefa(byte[] byteArray, int intValue) {
        this.byteArray = byteArray;
        this.intValue = intValue;
    }

    // Getter for byte array
    public byte[] getByteArray() {
        return byteArray;
    }

    // Getter for int value
    public int getIntValue() {
        return intValue;
    }

    // Getter for counter
    public static int getCounter() {
        return counter;
    }

    // Setter for counter
    public static void setCounter(int newCounter) {
        counter = newCounter;
    }


    /*
    public static void main(String[] args) {
        // Example usage
        byte[] exampleByteArray = {1, 2, 3, 4};
        int exampleIntValue = 42;

        Tarefa tarefa = new Tarefa(exampleByteArray, exampleIntValue);

        System.out.println("Byte Array: " + tarefa.getByteArray());
        System.out.println("Int Value: " + tarefa.getIntValue());
        System.out.println("Counter: " + Tarefa.getCounter());
    }
    */
}

