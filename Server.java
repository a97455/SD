import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server {

    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(12345);
        Map<String, String> utilizadores = new HashMap<>(); //Utilizador->pass

        while (true) {
            Socket socket = ss.accept();
            Thread thread = new Thread(new ServerRunnable(socket, utilizadores));
            thread.start();
        }
    }

    private static class ServerRunnable implements Runnable {
        private Socket socket;
        private BufferedReader reader;
        private BufferedWriter writer;
        private Map<String, String> utilizadores;

        public ServerRunnable(Socket socket, Map<String, String> utilizadores) {
            this.socket = socket;
            this.utilizadores = utilizadores;

            try {
                this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                String mensagem;
                while ((mensagem = reader.readLine()) != null) {

                    // Assuming the client sends the data in the format "username,password"
                    String[] parts = mensagem.split(",");
                    String username = parts[0];
                    String password = parts[1];

                    // Perform authentication logic here
                    // For example, check if the username and password match some stored values
                    if (utilizadores.containsKey(username) && utilizadores.get(username).equals(password)) {
                        writer.write("Authentication failed");
                    } else {
                        utilizadores.put(username,password);
                        writer.write("Authentication successful");
                    }

                    // Add a newline to indicate the end of the response
                    writer.newLine();
                    writer.flush(); // Ensure the response is sent immediately
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
