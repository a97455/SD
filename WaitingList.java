import java.nio.file.FileSystemNotFoundException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class WaitingList {
    private Message[] messageArray;
    private int currentIndex;

    // Constructor
    public WaitingList(int size) {
        messageArray = new Message[size];
        currentIndex = 0;
    }

    // Method to add a new Message to the array
    public void addMessage(Message message) {

        if (currentIndex < messageArray.length) {
            messageArray[currentIndex] = message;
            currentIndex++;
        } else {
            System.out.println("WaitingList is full. Cannot add more messages.");
        }
    }

    // Method to select messages based on available size in the server

    public List<Message> selectMessages(int serverSize) {
        List<Message> selectedMessages = new ArrayList<>();

        // Creating a copy of the messageArray for sorting
        Message[] sortedMessages = Arrays.copyOf(messageArray, currentIndex);

        // Creating a custom comparator that considers null elements as having the lowest priority
        Comparator<Message> comparator = Comparator.nullsFirst(Comparator.comparingInt(message -> message.counter));

        // Sorting the copy of messages based on priority (counter) with handling of null elements
        Arrays.sort(sortedMessages, comparator);

        // Selecting messages until the server is full
        for (Message message : sortedMessages) {
            if (message != null && serverSize >= message.size) {
                selectedMessages.add(message);
                serverSize -= message.size;
            }
        }

        // Decrementing the counter only for messages that were not selected
        for (Message message : messageArray) {
            if (message != null && !selectedMessages.contains(message)) {
                // Decrease the counter by one for messages that were not selected
                message.counter--;
            }
        }

        return selectedMessages;
    }


    // Method to remove selected messages from the original messageArray
    public WaitingList removeSelectedMessages(List<Message> selectedMessages) {
        // Creating a copy of the messageArray for manipulation
        Message[] newArray = Arrays.copyOf(messageArray, currentIndex);

        // Removing selected messages from the copy
        for (Message selectedMessage : selectedMessages) {
            for (int i = 0; i < currentIndex; i++) {
                if (newArray[i] != null && newArray[i].equals(selectedMessage)) {
                    newArray[i] = null;
                    break;
                }
            }
        }

        // Constructing a new WaitingList with the same size as the original
        WaitingList newWaitingList = new WaitingList(messageArray.length);
        for (Message message : newArray) {
            if (message != null) {
                newWaitingList.addMessage(message);
            }
        }

        return newWaitingList;
    }




    // Method to get values from the array
    public void getMessage(int index) {
        if (index >= 0 && index < currentIndex) {
            Message message = messageArray[index];
            if (message != null) {
                System.out.print("Message Nº"+index);
                System.out.print(" Type: " + message.type);
                System.out.print(" Size: " + message.size);
                /*
                System.out.println("Content at index " + index + ": " + byteArrayToString(message.content));
                System.out.println("NumMensagem at index " + index + ": " + message.numMensagem);
                 */
                System.out.println(" Counter: " + message.counter);
            } else {
                System.out.println("Null message at index " + index);
            }
        } else {
            System.out.println("Index out of bounds or no message at the specified index");
        }
    }

    public void printAllMessages() {
        System.out.println("All Messages:");
        for (int i = 0; i < messageArray.length; i++) {
            Message message = messageArray[i];
            System.out.print("Message Nº" + i);
            if (message != null) {
                System.out.print(" Type: " + message.type);
                System.out.print(" Size: " + message.size);
                System.out.print(" Counter: " + message.counter);
            } else {
                System.out.print(" Null message");
            }
            System.out.println();
        }
    }


    // Helper method to convert byte array to string for printing
    private String byteArrayToString(byte[] byteArray) {
        return new String(byteArray);
    }

    public static void main(String[] args) {

        WaitingList waitingList = new WaitingList(5);

        Message message1 = new Message(1, 6,"Hello".getBytes(), 123);
        waitingList.addMessage(message1);

        Message message2 = new Message(2, 6,"World".getBytes(), 456);
        waitingList.addMessage(message2);

        Message message3 = new Message(2, 4,"World".getBytes(), 234);
        waitingList.addMessage(message3);



        System.out.println();
        System.out.println("------------------------------------------");
        System.out.println("Primeira ronda: ");
        System.out.println("------------------------------------------");
        System.out.println();


        // Select messages for the server with available size 10
        List<Message> selectedMessages = waitingList.selectMessages(10);

        System.out.println("Mensagens selecionadas: ");
        System.out.println();

        // Print selected messages
        System.out.println("Selected Messages:");
        for (Message selectedMessage : selectedMessages) {
            System.out.println("Type: " + selectedMessage.type +
                    ", Size: " + selectedMessage.size +
                    ", Counter: " + selectedMessage.counter);
        }


        // Print messages after selection
        /*
        System.out.println("\nMessages after selection:");
        for (int i = 0; i < waitingList.currentIndex; i++) {
            Message message = waitingList.messageArray[i];
            if (message != null && message.counter < 4) {
                waitingList.getMessage(i);

            }
        }

         */
        System.out.println();
        System.out.println("------------------------------------------");
        System.out.println("Mensagens removidas: ");
        System.out.println("------------------------------------------");
        System.out.println();



        // Remove selected messages and get a new WaitingList
        WaitingList remainingMessages = waitingList.removeSelectedMessages(selectedMessages);

        System.out.println("------------------------------------------");
        System.out.println("Remaining messages: ");
        System.out.println("------------------------------------------");
        System.out.println();

        remainingMessages.printAllMessages();

        waitingList = remainingMessages;

        System.out.println("Acrescentar 4....");
        Message message4 = new Message(2, 2, "Java".getBytes(), 565);
        waitingList.addMessage(message4);

        System.out.println("Acrescentar 5....");
        Message message5 = new Message(2, 30, "Programming".getBytes(), 888);
        waitingList.addMessage(message5);

        System.out.println("Acrescentar 6....");
        Message message6 = new Message(2, 7, "AI".getBytes(), 999);
        waitingList.addMessage(message6);

        System.out.println("------------------------------------------");
        System.out.println("After adding: ");
        System.out.println("------------------------------------------");
        System.out.println();

        waitingList.printAllMessages();


        // Print remaining messages
        System.out.println("\nRemaining Messages:");
        for (int i = 0; i < waitingList.currentIndex; i++) {
            Message message = waitingList.messageArray[i];
            if (message != null) {
                waitingList.getMessage(i);

            }
        }


        System.out.println();
        System.out.println("------------------------------------------");
        System.out.println("Segunda Ronda: ");
        System.out.println("------------------------------------------");
        System.out.println();



        List<Message> selectedMessages2 = waitingList.selectMessages(10);

        System.out.println("Mensagens selecionadas: ");
        System.out.println();

        // Print selected messages
        System.out.println("Selected Messages:");
        for (Message selectedMessage : selectedMessages2) {
            System.out.println("Type: " + selectedMessage.type +
                    ", Size: " + selectedMessage.size +
                    ", Counter: " + selectedMessage.counter);
        }


        // Print messages after selection
        /*
        System.out.println("\nMessages after selection:");
        for (int i = 0; i < waitingList.currentIndex; i++) {
            Message message = waitingList.messageArray[i];
            if (message != null && message.counter < 4) {
                waitingList.getMessage(i);
                System.out.println();
            }
        }

         */
        System.out.println();
        System.out.println("------------------------------------------");
        System.out.println("Mensagens removidas: ");
        System.out.println("------------------------------------------");
        System.out.println();
        // Remove selected messages and get a new WaitingList
        WaitingList remainingMessages2 = waitingList.removeSelectedMessages(selectedMessages2);

        waitingList = remainingMessages2;

        // Print remaining messages
        System.out.println("\nRemaining Messages:");
        for (int i = 0; i < waitingList.currentIndex; i++) {
            Message message = waitingList.messageArray[i];
            if (message != null) {
                waitingList.getMessage(i);
                System.out.println();
            }
        }

        waitingList.printAllMessages();


    }
}

/*
        Message message6 = new Message(2, 10, "AI".getBytes(), 643);
        waitingList.addMessage(message6);

        Message message7 = new Message(2, 25, "Chatbot".getBytes(), 876);
        waitingList.addMessage(message7);

        Message message8 = new Message(2, 30, "OpenAI".getBytes(), 109);
        waitingList.addMessage(message8);

        Message message9 = new Message(2, 20, "GPT-3.5".getBytes(), 901);
        waitingList.addMessage(message9);

        Message message10 = new Message(2, 1, "Request".getBytes(), 185);
        waitingList.addMessage(message10);

        Message message11 = new Message(2, 40, "Response".getBytes(), 111);
        waitingList.addMessage(message11);

        Message message12 = new Message(2, 20, "Data".getBytes(), 222);
        waitingList.addMessage(message12);

        Message message13 = new Message(2, 6, "Task".getBytes(), 333);
        waitingList.addMessage(message13);

        Message message14 = new Message(2, 15, "Completion".getBytes(), 444);
        waitingList.addMessage(message14);

        Message message15 = new Message(2, 8, "ChatGPT".getBytes(), 555);
        waitingList.addMessage(message15);


         */




