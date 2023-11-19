import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


public class Client
{
    /* ------------------------------------------------------
                            MENUS
    ------------------------------------------------------ */

    public static void menu1(DataInputStream in, DataOutputStream out) // Registo, autenticação, saída
    {
        Scanner scanner = new Scanner(System.in);

        System.out.println("1-Registo");
        System.out.println("2-Autenticação");
        System.out.println("0-Sair");
        System.out.print("Digite uma das opções: ");

        int option = scanner.nextInt();

        switch (option)
        {
            case 0:
                sair();

            case 1:
                registo(scanner, in, out);
                break;

            case 2:
                autenticacao(scanner, in, out);
                break;

            default:
                System.out.println("Opção inválida.");
                break;
        }

        scanner.close();
    }

    public static void menu2(DataInputStream in, DataOutputStream out) // Autenticação, saída (após registo)
    {
        Scanner scanner = new Scanner(System.in);

        System.out.println("1-Autenticação");
        System.out.println("0-Sair");
        System.out.print("Digite uma das opções: ");

        int option = scanner.nextInt();

        switch (option)
        {
            case 0:
                sair();

            case 1:
                autenticacao(scanner, in, out);
                break;

            default:
                System.out.println("Opção inválida.");
                break;
        }

        scanner.close();
    }

    public static void menu3() // Enviar tarefa, saída (após autenticação)
    {
        Scanner scanner = new Scanner(System.in);

        System.out.println("1-Enviar tarefa");
        System.out.println("0-Sair");
        System.out.print("Digite uma das opções: ");

        int option = scanner.nextInt();

        switch (option)
        {
            case 0:
                sair();

            case 1:
                tarefa();
                break;

            default:
                System.out.println("Opção inválida.");
                break;
        }

        scanner.close();
    }

    /* ------------------------------------------------------
                            AÇÕES
    ------------------------------------------------------ */

    public static void registo(Scanner scanner,  DataInputStream in, DataOutputStream out)
    {
        try
        {
            // Ler input do utilizador
            System.out.println("Nome de utilizador: ");
            scanner.nextLine();
            String username = scanner.nextLine();
            System.out.println("Palavra-passe: ");
            String password = scanner.nextLine();

            // Enviar mensagem para registo no servidor
            String s = username + "," + password;
            Message messageOut = new Message(0, s.getBytes());
            messageOut.serialize(out);
            out.flush();

            // Receber resultado do registo
            Message messageIn = Message.deserialize(in);
            System.out.println(new String(messageIn.content));

            // Se o registo for bem sucedido
            if (messageIn.type == 0) menu2(in ,out);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void autenticacao(Scanner scanner, DataInputStream in, DataOutputStream out)
    {
        try
        {
            // Ler input do utilizador
            System.out.println("Nome de utilizador: ");
            scanner.nextLine();
            String username = scanner.nextLine();
            System.out.println("Palavra-passe: ");
            String password = scanner.nextLine();

            // Enviar mensagem para autenticação no servidor
            String s = username + "," + password;
            Message messageOut = new Message(1, s.getBytes());
            messageOut.serialize(out);
            out.flush();

            // Receber resultado da autenticação
            Message messageIn = Message.deserialize(in);
            System.out.println(new String(messageIn.content));

            // Se a autenticação for bem sucedida
            if (messageIn.type == 0) menu3();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sair()
    {
        // Desconectar o cliente?
    }

    public static void tarefa()
    {
        // Pedir para o servidor executar uma tarefa
    }

    /* ------------------------------------------------------
                            MAIN
    ------------------------------------------------------ */

    public static void main(String[] args)
    {
        try
        (
            Socket socket = new Socket("localhost", 12345);
            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        )
        {
            menu1(in, out);
        }
        catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}




/*

try
        {
            Socket socket = new Socket("localhost", 12345);

            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            Scanner scanner = new Scanner(System.in);

            System.out.println("1-Registo");
            System.out.println("2-Autenticação");
            System.out.println("0-Sair");
            System.out.print("Digite uma das opções: ");

            try
            {
                int option = scanner.nextInt();

                // REGISTO DE UM NOVO CLIENTE
                if (option == 1)
                {
                    // Ler input do utilizador
                    System.out.println("Nome de utilizador: ");
                    scanner.nextLine();
                    String username = scanner.nextLine();
                    System.out.println("Palavra-passe: ");
                    String password = scanner.nextLine();

                    // Enviar mensagem para registo no servidor
                    String s = username + "," + password;
                    Message messageOut = new Message(0, s.getBytes());
                    messageOut.serialize(out);
                    out.flush();

                    // Receber resultado do registo
                    Message messageIn = Message.deserialize(in);
                    System.out.println(new String(messageIn.content));
                }

                // AUTENTICAÇÃO DE UM CLIENTE
                else if (option == 2) {

                    // Ler input do utilizador
                    System.out.println("Nome de utilizador: ");
                    scanner.nextLine();
                    String username = scanner.nextLine();
                    System.out.println("Palavra-passe: ");
                    String password = scanner.nextLine();

                    // Enviar mensagem para autenticação no servidor
                    String s = username + "," + password;
                    Message messageOut = new Message(1, s.getBytes());
                    messageOut.serialize(out);
                    out.flush();

                    // Receber resultado da autenticação
                    Message messageIn = Message.deserialize(in);
                    System.out.println(new String(messageIn.content));
                }

                // SAIR
                else if (option == 0)
                {
                    // Desconectar o cliente?
                }

                // OPÇÃO INVÁLIDA
                else
                {
                    System.out.println("Opção inválida.");
                }

            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                scanner.close();
            }
        }
        catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }


 */