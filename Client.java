import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;


public class Client {
    Socket socket;
    Scanner scanner;
    TaggedConnection tagged;
    Demultiplexer des;
    ReentrantLock lock;
    private int numMensagem;
    public Client() throws IOException{
        this.socket = new Socket("localhost", 12345);
        this.scanner = new Scanner(System.in);
        this.tagged = new TaggedConnection(socket);
        this.des= new Demultiplexer(tagged);
        this.lock=new ReentrantLock();
        this.numMensagem=0;
    }

    /* ------------------------------------------------------
                            MENUS
    ------------------------------------------------------ */

    public void menu1(Scanner scanner,DataInputStream in, DataOutputStream out) // Autenticação, saída (após registo)
    {
        System.out.println("1-Registo");
        System.out.println("2-Autenticação");
        System.out.println("0-Sair");
        System.out.print("Digite uma das opções: ");

        int option = scanner.nextInt();
        scanner.nextLine();

        switch (option)
        {
            case 0:
                break;
            case 1:
                registo(scanner,in,out);
                break;
            case 2:
                autenticacao(scanner, in, out);
                break;

            default:
                System.out.println("Opção inválida.");
                break;
        }
    }

    public void menu2(Scanner scanner,DataInputStream in, DataOutputStream out) // Enviar tarefa, saída (após autenticação)
    {
        lock.lock();
        System.out.println("1-Enviar tarefa");
        System.out.println("0-Sair");
        System.out.print("Digite uma das opções: ");

        int option = scanner.nextInt();
        scanner.nextLine();

        switch (option)
        {
            case 0:
                lock.unlock();
                break;
            case 1:
                tarefa(scanner, in, out);
                menu2(scanner,in,out);
                break;

            default:
                System.out.println("Opção inválida.");
                lock.unlock();
                menu2(scanner,in,out);
                break;
        }
    }

    /* ------------------------------------------------------
                            AÇÕES
    ------------------------------------------------------ */

    public void registo(Scanner scanner,DataInputStream in,DataOutputStream out)
    {
        try{
            // Ler input do utilizador
            System.out.println("Nome de utilizador: ");
            String username = scanner.nextLine();
            System.out.println("Palavra-passe: ");
            String password = scanner.nextLine();

            // Enviar mensagem para registo no servidor
            String s = username + "," + password;
            Message messageOut = new Message(0, s.getBytes(),++numMensagem);
            messageOut.serialize(out);
            out.flush();

            // Receber resultado do registo
            Message messageIn = Message.deserialize(in);
            System.out.println(new String(messageIn.content));

            // Se o registo for bem sucedido
            if (messageIn.type == 0) menu1(scanner,in ,out);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void autenticacao(Scanner scanner, DataInputStream in, DataOutputStream out)
    {
        try
        {
            // Ler input do utilizador
            System.out.println("Nome de utilizador: ");
            String username = scanner.nextLine();
            System.out.println("Palavra-passe: ");
            String password = scanner.nextLine();

            // Enviar mensagem para autenticação no servidor
            String s = username + "," + password;
            Message messageOut = new Message(1, s.getBytes(),++numMensagem);
            messageOut.serialize(out);
            out.flush();

            // Receber resultado da autenticação
            Message messageIn = Message.deserialize(in);
            System.out.println(new String(messageIn.content));

            // Se a autenticação for bem sucedida
            if (messageIn.type == 0) menu2(scanner,in, out);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sair(Socket socket,Scanner scanner) throws IOException {
        scanner.close();
        socket.shutdownInput();
        socket.shutdownOutput();
        socket.close();
    }

    public void tarefa(Scanner scanner, DataInputStream in, DataOutputStream out){
        new Thread(() -> {
            try {
                int numThread =Thread.activeCount();
                // Ler input do utilizador
                System.out.println("Caminho para o ficheiro a executar: ");
                Path path1 = Paths.get(scanner.nextLine());

                System.out.println("Tamanho da Tarefa ");
                int size = scanner.nextInt();
                scanner.nextLine();

                // Enviar mensagem com a tarefa como conteúdo
                byte[] content = Files.readAllBytes(path1);
                Message messageOut = new Message(2, size, content,++numMensagem);

                System.out.println("A mensagem "+numMensagem+" foi enviada");
                lock.unlock();

                this.des.send(new TaggedConnection.Frame(numThread, messageOut));

                Message messageIn =this.des.receive(numThread);

                // Se a execução devolver o resutlado, escrevê-lo num ficheiro
                if (messageIn.type == 2) {
                    lock.lock();
                    System.out.println("Caminho para o ficheiro com o resultado: ");
                    Path path2 = Paths.get(scanner.nextLine());

                    Files.write(path2, messageIn.content);
                    System.out.println("Tarefa "+messageIn.numMensagem+" terminada com sucesso.");
                    lock.unlock();
                }
            }catch(IOException e){
                    throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();




    }

    /* ------------------------------------------------------
                            MAIN
    ------------------------------------------------------ */

    public static void main(String[] args) {
        try {
            Client client=new Client();
            client.menu1(client.scanner,client.tagged.in,client.tagged.out);
            client.sair(client.socket,client.scanner);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}