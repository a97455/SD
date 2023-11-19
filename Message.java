import java.io.*;

class Message
{
    public int type;
    public byte[] content;

    public Message(int type, byte[] content)
    {
        this.type = type;
        this.content = content;
    }

    public void serialize(DataOutputStream out) throws IOException
    {
        out.writeInt(this.type);
        out.writeInt(this.content.length);
        out.write(this.content);
    }

    public static Message deserialize(DataInputStream in) throws IOException
    {
        int type = in.readInt();
        int contentLength = in.readInt();
        byte[] content = new byte[contentLength];
        in.readFully(content);

        return (new Message(type, content));
    }

}