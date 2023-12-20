import sd23.JobFunction;
import sd23.JobFunctionException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


class ServerWorker implements Runnable
{
    private Socket socket;
    private Map<String, String> utilizadores;
    private int capacity = 1024;

    public ServerWorker(Socket socket, Map<String,String> utilizadores)
    {
        this.socket = socket;
        this.utilizadores = utilizadores;
    }

    @Override
    public void run() {

        try {
            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            while (true) {
                Message messageIn = Message.deserialize(in);
                // REGISTO DE UM NOVO CLIENTE
                if (messageIn.type == 0) {
                    String s = new String(messageIn.content);
                    String[] parts = s.split(",");
                    String username = parts[0];
                    String password = parts[1];

                    String resposta = null;
                    int type;

                    if (utilizadores.containsKey(username)) {
                        type = 1;
                        resposta = "Não é possível fazer o registo. O nome de utilizador já existe.";
                    } else {
                        utilizadores.put(username, password);
                        type = 0;
                        resposta = "Novo cliente registado com sucesso!";
                        // utilizadores.forEach((key, value) -> System.out.println(key + " : " + value));
                    }

                    Message messageOut = new Message(type, resposta.getBytes());
                    messageOut.serialize(out);
                    out.flush();
                }

                // AUTENTICAÇÃO DE UM CLIENTE
                else if (messageIn.type == 1) {
                    String s = new String(messageIn.content);

                    String[] parts = s.split(",");
                    String username = parts[0];
                    String password = parts[1];

                    String resposta = null;
                    int type;

                    if (utilizadores.containsKey(username) && utilizadores.get(username).equals(password)) {
                        type = 0;
                        resposta = "Autenticação realizada com sucesso.";
                    } else {
                        type = 1;
                        resposta = "Não é possível fazer a autenticação.";
                    }

                    Message messageOut = new Message(type, resposta.getBytes());
                    messageOut.serialize(out);
                    out.flush();

                }
                // TAREFA PARA EXECUÇÃO
                else if (messageIn.type == 2) {
                    try {
                        byte[] result = JobFunction.execute(messageIn.content);
                        // System.err.println("Tarefa executada com successo. Resultado " + result.length + " bytes");

                        // Devolver resultado para o cliente
                        Message messageOut = new Message(2, result);
                        messageOut.serialize(out);
                        out.flush();
                    } catch (JobFunctionException e) {
                        // Mensagem de erro
                        // System.err.println("Tarefa sem sucesso. Código=" + e.getCode() + " Mensagem=" + e.getMessage());
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
            /* int i = Thread.activeCount();
            System.out.println("Há " + (i-1) + " threads"); */
        }
    }
}