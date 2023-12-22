import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TaggedConnection implements AutoCloseable {
    private final Socket socket;
    private final DataOutputStream out;
    private final DataInputStream in;
    private final ReentrantReadWriteLock readlock;
    private final ReentrantReadWriteLock writeLock;

    public static class Frame {
        public final long tag;
        public Message mensagem;

        public Frame(long tag, Message mensagem) {
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

    public void send(long tag, Message mensagem) throws IOException {
        writeLock.writeLock().lock();

        this.out.writeLong(tag);
        mensagem.serialize(out);
        this.out.flush();

        writeLock.writeLock().unlock();
    }

    public Frame receive() throws IOException {
        readlock.readLock().lock();

        long tag = this.in.readLong();
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