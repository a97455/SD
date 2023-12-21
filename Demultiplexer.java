import java.io.*;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Demultiplexer implements AutoCloseable{
    public TaggedConnection taggedConnection;
    public Lock lock = new ReentrantLock();
    public HashMap<Integer, Entry> buf = new HashMap<>();
    public IOException exception = null;

    private class Entry{
        int waiters =0;
        final Condition cond = lock.newCondition();
        final ArrayDeque<Message> queue= new ArrayDeque<>();
    }

    private Entry get(int tag){
        Entry e = buf.get(tag);
        if (e==null){
            e = new Entry();
            buf.put(tag,e);
        }
        return e;
    }

    public Demultiplexer(TaggedConnection conn) {
        this.taggedConnection=conn;
    }

    public void start() {
        new Thread(() -> {
            try{
                while (true){
                    TaggedConnection.Frame frame = this.taggedConnection.receive();
                    lock.lock();
                    try {
                        Entry e = get(frame.tag);
                        e.queue.add(frame.mensagem);
                        e.cond.signal();
                    } finally {
                        lock.unlock();
                    }
                }
            } catch (IOException e){
                lock.lock();
                try {
                    exception = e;
                    this.buf.forEach((k,v) -> v.cond.signalAll());
                } finally {
                    lock.unlock();
                }
            }
        }).start();
    }

    public void send(TaggedConnection.Frame frame) throws IOException {
        this.taggedConnection.send(frame);
    }

    public void send(int tag, Message mensagem) throws IOException {
        this.taggedConnection.send(tag,mensagem);
    }

    public Message receive(int tag) throws IOException, InterruptedException {
        lock.lock();
        try{
            Entry e = get(tag);
            e.waiters++;
            while(true){
                if(!e.queue.isEmpty()){
                    Message res = e.queue.poll();
                    e.waiters--;
                    if (e.queue.isEmpty() && e.waiters==0){
                        buf.remove(tag);
                    }
                    return res;
                }
                if (exception!=null){
                    throw exception;
                }
                e.cond.await();
            }
        }finally {
            lock.unlock();
        }
    }

    public void close() throws IOException {
        this.taggedConnection.close();
    }
}