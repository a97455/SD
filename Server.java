import sd23.JobFunction;
import sd23.JobFunctionException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;


class ServerWorker implements Runnable
{
    private Map<String, String> utilizadores;
    private WaitingList waitingList;
    private int capacity = 1024;
    private int availability = this.capacity;
    private TaggedConnection tagged;
    private ReentrantLock registoLoginLock;
    private ReentrantLock tarefaLock;


    public ServerWorker(Socket socket, Map<String,String> utilizadores, WaitingList waitingList) throws IOException
    {
        this.utilizadores = utilizadores;
        this.waitingList = waitingList;
        this.tagged= new TaggedConnection(socket);
        this.registoLoginLock= new ReentrantLock();
        this.tarefaLock= new ReentrantLock();
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                TaggedConnection.Frame frameIn = this.tagged.receive();
                Message messageIn = frameIn.mensagem;

                // REGISTO DE UM NOVO CLIENTE
                if (messageIn.type == 0) {
                    String s = new String(messageIn.content);
                    String[] parts = s.split(",");
                    String username = parts[0];
                    String password = parts[1];

                    String resposta;
                    int type;

                    registoLoginLock.lock();
                    if (!utilizadores.containsKey(username)) {
                        utilizadores.put(username, password);
                        type = 0;
                        resposta = "Novo cliente registado com sucesso!";
                    }
                    else {
                        type = 1;
                        resposta = "Não é possível fazer o registo. O nome de utilizador já existe.";
                    }
                    registoLoginLock.unlock();

                    Message messageOut = new Message(type, resposta.getBytes(), messageIn.numMensagem);
                    this.tagged.send(new TaggedConnection.Frame(frameIn.tag, messageOut));
                }
                // AUTENTICAÇÃO DE UM CLIENTE
                else if (messageIn.type == 1) {
                    String s = new String(messageIn.content);

                    String[] parts = s.split(",");
                    String username = parts[0];
                    String password = parts[1];

                    String resposta;
                    int type;

                    registoLoginLock.lock();
                    if (utilizadores.containsKey(username) && utilizadores.get(username).equals(password)) {
                        type = 0;
                        resposta = "Autenticação realizada com sucesso.";
                    } else {
                        type = 1;
                        resposta = "Não é possível fazer a autenticação.";
                    }
                    registoLoginLock.unlock();

                    Message messageOut = new Message(type, resposta.getBytes(),messageIn.numMensagem);
                    this.tagged.send(new TaggedConnection.Frame(frameIn.tag,messageOut));
                }
                // TAREFA PARA EXECUÇÃO
                else if (messageIn.type == 2)
                {
                    tarefaLock.lock();
                    waitingList.addMessage(messageIn);
                    List<Message> selected = waitingList.selectMessages(this.availability);
                    tarefaLock.unlock();

                    for (Message m: selected)
                    {
                        new Thread(() -> {
                            try {
                                executeJob(m, frameIn);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }).start();
                    }
                }
                // CONSULTAR MEMÓRIA DISPONÍVEL
                else if (messageIn.type == 3)
                {
                    int mem = this.availability;
                    String resposta = mem + " bytes de memória disponível";

                    Message messageOut = new Message(1, resposta.getBytes(), messageIn.numMensagem);
                    this.tagged.send(new TaggedConnection.Frame(frameIn.tag,messageOut));
                }
                // CONSULTAR TAREFAS EM FILA DE ESPERA
                else if (messageIn.type == 4)
                {
                    int waiting = this.waitingList.getSize();
                    String resposta = waiting + " tarefas em fila de espera";

                    Message messageOut = new Message(1, resposta.getBytes(), messageIn.numMensagem);
                    this.tagged.send(new TaggedConnection.Frame(frameIn.tag,messageOut));
                }
            }
            catch (IOException e){
                break;
            }
        }
    }

    public void executeJob(Message m, TaggedConnection.Frame frameIn) throws IOException
    {
        try
        {
            this.availability -= m.size;

            byte[] result = JobFunction.execute(m.content);

            this.availability += m.size;

            // Devolver resultado para o cliente
            Message messageOut = new Message(0, result,m.numMensagem);
            this.tagged.send(new TaggedConnection.Frame(frameIn.tag,messageOut));
        }
        catch (JobFunctionException e)
        {
            String resposta =  "Servidor não consegui realizar a tarefa " + m.numMensagem + ", código =" + e.getCode() + " message =" + e.getMessage();

            this.availability += m.size;

            // Devolver resultado para o cliente
            Message messageOut = new Message(1, resposta.getBytes(), m.numMensagem);
            this.tagged.send(new TaggedConnection.Frame(frameIn.tag,messageOut));
        }
    }
}



public class Server {
    public static void main(String[] args) throws IOException
    {
        ServerSocket serverSocket = new ServerSocket(12345);
        Map<String, String> utilizadores = new HashMap<>();
        WaitingList waitingList = new WaitingList();

        while (true) {
            Socket socket = serverSocket.accept();
            Thread worker = new Thread(new ServerWorker(socket, utilizadores, waitingList));
            worker.start();
        }
    }
}
