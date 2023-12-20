import java.io.*;

class Message
{
    public int type;
    public int size;
    public byte[] content;

    // Constructor without size argument
    public Message(int type, byte[] content) {
        this.type = type;
        this.size = content.length; // Use content length as size
        this.content = content;
    }

    // Constructor with size argument
    public Message(int type,int size, byte[] content)
    {
        this.type = type;
        this.size = size;
        this.content = content;
    }

    public void serialize(DataOutputStream out) throws IOException
    {
        out.writeInt(this.type);
        out.writeInt(this.size);
        out.writeInt(this.content.length);
        out.write(this.content);
    }

    public static Message deserialize(DataInputStream in) throws IOException
    {
        int type = in.readInt();
        int size = in.readInt();
        int contentLength = in.readInt();
        byte[] content = new byte[contentLength];
        in.readFully(content);

        return (new Message(type,size,content));
    }

}