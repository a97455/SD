import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TaggedConnection implements AutoCloseable {
    public Socket socket;
    public DataOutputStream out;
    public DataInputStream in;
    public ReentrantReadWriteLock readlock;
    public ReentrantReadWriteLock writeLock;

    public static class Frame {
        public final int tag;
        public Message mensagem;

        public Frame(int tag, Message mensagem) {
            this.tag = tag;
            this.mensagem = mensagem;
        }
    }

    public TaggedConnection(Socket socket) throws IOException {
        this.socket=socket;
        this.out= new DataOutputStream(new BufferedOutputStream(this.socket.getOutputStream()));
        this.in = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));
        this.readlock = new ReentrantReadWriteLock();
        this.writeLock = new ReentrantReadWriteLock();
    }

    public void send(Frame frame) throws IOException {
        this.send(frame.tag,frame.mensagem);
    }

    public void send(int tag, Message mensagem) throws IOException {
        writeLock.writeLock().lock();

        this.out.write(tag);
        mensagem.serialize(out);
        this.out.flush();

        writeLock.writeLock().unlock();
    }

    public Frame receive() throws IOException {
        readlock.readLock().lock();

        int tag = this.in.readInt();
        Message mensagem=Message.deserialize(in);

        readlock.readLock().unlock();
        return new Frame(tag,mensagem);
    }

    public void close() throws IOException {
        this.socket.shutdownOutput();
        this.socket.shutdownInput();
        this.socket.close();
    }
}