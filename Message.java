import java.io.*;

public class Message {
    public int type;
    public int size; //tamanho arbitrario mensagem (fornecida pelo cliente)
    public byte[] content;
    public int numMensagem;

    // Constructor without size argument
    public Message(int type, byte[] content,int numMensagem) {
        this.type = type;
        this.size = content.length; // Use content length as size
        this.content = content;
        this.numMensagem=numMensagem;
    }

    // Constructor with size argument
    public Message(int type,int size, byte[] content,int numMensagem){
        this.type = type;
        this.size = size;
        this.content = content;
        this.numMensagem=numMensagem;
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(this.type);
        out.writeInt(this.size);
        out.writeInt(this.content.length);
        out.write(this.content);
        out.writeInt(this.numMensagem);
    }

    public static Message deserialize(DataInputStream in) throws IOException {
        int type = in.readInt();
        int size = in.readInt();
        int contentLength = in.readInt();
        byte[] content = new byte[contentLength];
        in.readFully(content);
        int numMensagem = in.readInt();

        return (new Message(type,size,content,numMensagem));
    }
}