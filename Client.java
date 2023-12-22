import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class Client {
    private final Socket socket;
    private final Scanner scanner;
    private final Demultiplexer des;
    private final ReentrantLock lock;
    private final Condition cond;
    private int numMensagem;

    public Client() throws IOException{
        this.socket = new Socket("localhost", 12345);
        this.scanner = new Scanner(System.in);
        this.des= new Demultiplexer(new TaggedConnection(socket));
        this.des.start();
        this.lock=new ReentrantLock();
        this.cond=this.lock.newCondition();
        this.numMensagem=0;
    }

    /* ------------------------------------------------------
                            MENUS
    ------------------------------------------------------ */

    public void menu1() throws IOException, InterruptedException // Autenticação, saída (após registo)
    {
        System.out.println("\n1-Registo");
        System.out.println("2-Autenticação");
        System.out.println("0-Sair");
        System.out.print("Digite uma das opções: ");

        int option = this.scanner.nextInt();
        this.scanner.nextLine();

        switch (option)
        {
            case 0:
                sair();
                break;
            case 1:
                registo();
                break;
            case 2:
                autenticacao();
                break;

            default:
                System.out.println("Opção inválida.");
                break;
        }
    }

    public void menu2() throws IOException, InterruptedException // Enviar tarefa, saída (após autenticação)
    {
        lock.lock();
        System.out.println("\n1-Enviar tarefa");
        System.out.println("0-Sair");
        System.out.print("Digite uma das opções: ");

        int option = this.scanner.nextInt();
        this.scanner.nextLine();

        switch (option)
        {
            case 0:
                lock.unlock();
                sair();
                break;
            case 1:
                tarefa();
                menu2();
                break;

            default:
                System.out.println("Opção inválida.");
                lock.unlock();
                menu2();
                break;
        }
    }

    /* ------------------------------------------------------
                            AÇÕES
    ------------------------------------------------------ */

    public void registo() throws IOException, InterruptedException {
        // Ler input do utilizador
        System.out.println("Nome de utilizador: ");
        String username = this.scanner.nextLine();
        System.out.println("Palavra-passe: ");
        String password = this.scanner.nextLine();

        // Enviar mensagem para registo no servidor
        String s = username + "," + password;
        Message messageOut = new Message(0, s.getBytes(),numMensagem);

        long numThread = Thread.currentThread().threadId();
        this.des.send(new TaggedConnection.Frame(numThread, messageOut));

        // Receber resultado do registo
        Message messageIn = this.des.receive(numThread);

        System.out.println(new String(messageIn.content));

        menu1();
    }

    public void autenticacao() throws IOException, InterruptedException {
        // Ler input do utilizador
        System.out.println("Nome de utilizador: ");
        String username = this.scanner.nextLine();
        System.out.println("Palavra-passe: ");
        String password = this.scanner.nextLine();

        // Enviar mensagem para autenticação no servidor
        String s = username + "," + password;
        Message messageOut = new Message(1, s.getBytes(), numMensagem);

        long numThread = Thread.currentThread().threadId();
        this.des.send(new TaggedConnection.Frame(numThread, messageOut));

        // Receber resultado da autenticação
        Message messageIn = this.des.receive(numThread);
        System.out.println(new String(messageIn.content));

        // Se a autenticação for bem sucedida
        if (messageIn.type == 0) menu2();
        else menu1();
    }

    public void sair() throws IOException {
        this.scanner.close();
        this.socket.shutdownInput();
        this.socket.shutdownOutput();
        this.socket.close();
    }

    public void tarefa() throws InterruptedException {
        new Thread(() -> {
            try {
                lock.lock();
                // Ler input do utilizador
                System.out.println("Caminho para o ficheiro a executar: ");
                Path path1 = Paths.get(this.scanner.nextLine());

                System.out.println("Caminho para o ficheiro com o resultado: ");
                Path path2 = Paths.get(this.scanner.nextLine());

                System.out.println("Tamanho da Tarefa ");
                int size = this.scanner.nextInt();
                this.scanner.nextLine();

                // Enviar mensagem com a tarefa como conteúdo
                byte[] content = Files.readAllBytes(path1);
                Message messageOut = new Message(2, size, content, ++numMensagem);

                System.out.println("A Tarefa " + numMensagem + " foi enviada");

                this.cond.signal();
                lock.unlock();

                long numThread = Thread.currentThread().threadId();
                this.des.send(new TaggedConnection.Frame(numThread, messageOut));

                Message messageIn = this.des.receive(numThread);

                // Se a execução devolver o resultado, escrevê-lo num ficheiro
                if (messageIn.type == 2) {
                    Files.write(path2, messageIn.content);

                    lock.lock();
                    System.out.println("\nTarefa " + messageIn.numMensagem + " terminada com sucesso.");
                    lock.unlock();
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        this.cond.await();
        lock.unlock();
    }

    /* ------------------------------------------------------
                            MAIN
    ------------------------------------------------------ */

    public static void main(String[] args) {
        try {
            Client client=new Client();
            client.menu1();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}