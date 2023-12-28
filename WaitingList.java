import java.util.*;


public class WaitingList {
    List<Message> messageList;

    // Constructor
    public WaitingList() {
        messageList = new ArrayList<Message>();
    }

    // Method to add a new Message to the array
    public void addMessage(Message message) {
        messageList.add(message);
    }

    // Method to select messages based on available size in the server
    public List<Message> selectMessages(int serverSize) {
        Comparator<Message> comp = Comparator.comparingInt(m -> m.counter);
        messageList.sort(comp);

        List<Message> selectedMessages = new ArrayList<>();

        Iterator<Message> iterator = messageList.iterator();

        while (iterator.hasNext()) {
            Message m = iterator.next();
            if (serverSize >= m.size) {
                selectedMessages.add(m);
                iterator.remove();
                serverSize -= m.size;
            }
        }

        for (Message message : messageList) {
            if (!selectedMessages.contains(message)) {
                // Decrease the counter by one for messages that were not selected
                message.counter--;
            }
        }

        return selectedMessages;
    }

    public int getSize()
    {
        return messageList.size();
    }
}