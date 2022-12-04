import com.mysql.cj.jdbc.MysqlDataSource;
import java.sql.*;


public class MySQL {
    private String user;
    //Variaveis que irão armazenar as informações necessarias do banco de dados
    private String password;
    private String server;
    private String database;
    private MysqlDataSource dataSource;
    public MySQL(String user,String password,String server,String database){
        //Informações necessarias para acessar o banco de dados
        this.user=user;
        this.password=password;
        this.server=server;
        this.database=database;

        dataSource = new MysqlDataSource();
        dataSource.setUser(user);
        dataSource.setPassword(password);
        dataSource.setServerName(server);
        dataSource.setDatabaseName(database);
    }
    public MySQL(){
        this("root","password","localhost","jokenpo");
    }

    public void saveRound(String mine, String opp,String resultado){
        //No MySQL usei o comando:
        //create table Rodada(
        //        BotJava varchar(30),
        //        Oponente varchar(30),
        //        Resultado varchar(100)
        //);
        try {
            //Faz a conecção com o BD e cria uma declaração que irá ser rodada no MySQL
            Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement();
            //String que irá ter o comando em MySQL para inserir na tabela
            String accessDatabase = "INSERT INTO Rodada" + " VALUES('"+ mine +"','"+opp+"','"+resultado+"') ";
            int result = stmt.executeUpdate(accessDatabase);
        } catch (Exception e) {
            //Imprimir o erro
            System.out.println("Error>"+e);
            e.printStackTrace();
        }
    }
}
