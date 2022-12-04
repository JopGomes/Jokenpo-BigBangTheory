import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Conexao {
    private ServerSocket server;
    private Socket client;
    private DataOutputStream saida;
    //ObjectInputStream para receber o nome do arquivo

    public Conexao(Integer porta) throws IOException {
        //Abre o servidor em um porta
        server = new ServerSocket(porta);
        System.out.println("Servidor Aberto");
    }

    public void NewGameAsServidor(){
        try {
            //Busca um client
            client= server.accept();

            //Inicializa o jogo
            Rodada rodadas = new Rodada();
            System.out.println("Cliente conectado: " + client.getInetAddress().getHostAddress());
            //Inicio das rodadas
            Integer continua=1;

            while(true){
                //Variaveis para obter a saida e entrada a cada rodada pelo socket
                saida = new DataOutputStream(client.getOutputStream());
                BufferedReader inClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String jogada = inClient.readLine();

                //Variavel para definir a proxima jogada
                int proxJog = rodadas.nextPlay(jogada);

                //Printa o log até o momento dos jogos
                System.out.println(rodadas.logResults());
                saida.flush();
                saida.writeInt(proxJog);
                continua++;
                //Caso chegue em 15 rodads
                if(continua>15){
                    //Irá fechar a saida e o cliente
                    saida.close();
                    client.close();

                    //E iniciara uma nova instancia
                    rodadas = new Rodada();
                    client= server.accept();
                    continua=1;
                }
            }
        }
        catch(Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
}
