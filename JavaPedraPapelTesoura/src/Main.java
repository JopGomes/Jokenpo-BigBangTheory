
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {

        Conexao Section = new Conexao(40000);
        //Inicia uma sessão de jogo com o java como servidor
        Section.NewGameAsServidor();
    }
}
