import sd23.JobFunction;
import sd23.JobFunctionException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


class ServerWorker implements Runnable
{
    private final Map<String, String> utilizadores;
    private final int capacity = 1024;
    private final TaggedConnection tagged;

    public ServerWorker(Socket socket, Map<String,String> utilizadores) throws IOException {
        this.utilizadores = utilizadores;
        this.tagged= new TaggedConnection(socket);
    }

    @Override
    public void run() {

        try {
            while (true) {
                Message messageIn = this.tagged.receive().mensagem;
                // REGISTO DE UM NOVO CLIENTE
                if (messageIn.type == 0) {
                    String s = new String(messageIn.content);
                    String[] parts = s.split(",");
                    String username = parts[0];
                    String password = parts[1];

                    String resposta;
                    int type;

                    if (utilizadores.containsKey(username)) {
                        type = 1;
                        resposta = "Não é possível fazer o registo. O nome de utilizador já existe.";
                    } else {
                        utilizadores.put(username, password);
                        type = 0;
                        resposta = "Novo cliente registado com sucesso!";
                    }

                    Message messageOut = new Message(type, resposta.getBytes());
                    System.out.println(messageOut.numMensagem);
                    this.tagged.send(new TaggedConnection.Frame(messageOut.numMensagem,messageOut));
                }

                // AUTENTICAÇÃO DE UM CLIENTE
                else if (messageIn.type == 1) {
                    String s = new String(messageIn.content);

                    String[] parts = s.split(",");
                    String username = parts[0];
                    String password = parts[1];

                    String resposta;
                    int type;

                    if (utilizadores.containsKey(username) && utilizadores.get(username).equals(password)) {
                        type = 0;
                        resposta = "Autenticação realizada com sucesso.";
                    } else {
                        type = 1;
                        resposta = "Não é possível fazer a autenticação.";
                    }

                    Message messageOut = new Message(type, resposta.getBytes());
                    this.tagged.send(new TaggedConnection.Frame(messageOut.numMensagem,messageOut));
                }
                // TAREFA PARA EXECUÇÃO
                else if (messageIn.type == 2) {
                    try {
                        byte[] result = JobFunction.execute(messageIn.content);

                        // Devolver resultado para o cliente
                        Message messageOut = new Message(2, result);
                        this.tagged.send(new TaggedConnection.Frame(messageOut.numMensagem,messageOut));
                    } catch (JobFunctionException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}


public class Server
{
    public static void main(String[] args) throws IOException
    {
        ServerSocket serverSocket = new ServerSocket(12345);
        Map<String, String> utilizadores = new HashMap<>();

        while (true)
        {
            Socket socket = serverSocket.accept();
            Thread worker = new Thread(new ServerWorker(socket, utilizadores));
            worker.start();
        }
    }
}