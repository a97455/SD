import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("1-Registo");
        System.out.println("2-Autenticação");
        System.out.println("0-Sair");
        System.out.print("Digite uma das opções: ");

        try {
            Socket socket = new Socket("localhost", 12345);
            int opcao = scanner.nextInt();

            switch (opcao) {
                case 1:
                    // Le os valores do nome e pass
                    System.out.println("Nome de utilizador: ");
                    scanner.nextLine(); // Consumir a quebra de linha pendente
                    String nome = scanner.nextLine();
                    System.out.println("Palavra-passe: ");
                    String pass = scanner.nextLine();


                    // Manda dados para o servidor
                    OutputStream outputStream = socket.getOutputStream();
                    PrintWriter writer = new PrintWriter(outputStream, true);

                    // Assuming the client wants to send the username "john" and password "password123"
                    String message = nome + "," + pass;
                    writer.println(message);

                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String resposta = reader.readLine();

                    // Processa a resposta
                    System.out.println("Resposta do servidor: " + resposta);

                    // Fechar recursos
                    writer.close();
                    reader.close();
                    socket.close();
                    break;
                case 2:
                    // Lógica para o caso 2
                    break;
                default:
                    System.out.println("Opção inválida.");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}
