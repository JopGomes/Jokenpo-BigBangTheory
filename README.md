# Trabalho pedra-papel-tesoura-lagarto-Spock

# Python

- Imports:

```python
import statistics

from reportlab.pdfgen import canvas 

import matplotlib.pyplot as plt

import random

from pymongo import MongoClient
import gridfs
import base64

from socket import *
```

- Funções que definem a próxima jogada a partir de pegar as opções que o oponente mais jogou até o momento e pegar um valor que ganhe dela

```python
#Função que irá definir qual a proxima jogada a partir da heuristica
def nextPlay(msgOpponent):
    if(len(msgOpponent)>0):
        moda = statistics.mode(msgOpponent)
        msgAu = [i for i in msgOpponent if i != moda]   
        ganhaDaModa = losers[moda]
        if(len(msgAu)>0):
            moda2 = statistics.mode(msgAu)[0]
            for i in ganhaDaModa:
                if( not bool(loser(i,moda2))):
                    return i
        else:
            return ganhaDaModa[0]
    else:
        x = random.randint(0,4)
        return tradutor[str(x)]
    
#Função para retornar o log do que ocorreu até o momento
def printAnswer(msgOpp,msgAnswer):
    k=0
    log=''
    for i in msgAnswer:
        j=msgOpp[k]
        k+=1
        log+=f'Na rodada {k}:\n O adversario jogou {j} e foi jogado pelo bot python {i} resultando em {result(i,j)}\n'
    log+='\n'
    return log

```

- Funções que geram os resultados por imagem ou como pdf:

```python
#Função que gera as imagens
def GenerateImg(msgOpp,msgAnswer,resultados):
    img=[]
    plt.title('Frequência de valores para o oponente')
    plt.xlabel('Classe')
    plt.ylabel('Numero de vezes')
    plt.hist(msgOpp)
    plt.savefig('opp.png')
    img.append('opp.png')
    plt.show()

    plt.title('Frequência de valores para o bot')
    plt.xlabel('Classe')
    plt.ylabel('Numero de vezes')
    plt.hist(msgOpp)
    plt.savefig('bot.png')
    img.append('bot.png')
    plt.show()

    plt.title('Resultado geral')
    plt.xlabel('Classe')
    plt.ylabel('Numero de vezes')
    plt.hist(resultados)
    plt.savefig('resultado.png')
    img.append('resultado.png')
    plt.show()
    return img

#Função que irá gerar o PDF com as imagens 
def GeneratePDF(msgOpp,msgAnswer):
    nome_pdf = 'Resultados'
    try:
        resultados=[]
        pdf = canvas.Canvas('{}.pdf'.format(nome_pdf))
        x = 700
        k=0
        for i in msgOpp:
            j=msgAnswer[k]
            k+=1
            x -= 20
            pdf.drawString(247,x, 'Rodada nº {}- {}   x   {}   :  {}'.format(k, j,i,result(j,i) ) )
            resultados.append(result(j,i))
            
        pdf.setTitle(nome_pdf)
        pdf.setFont("Helvetica-Oblique", 14)
        pdf.drawString(245,750, 'Lista de Resultados')
        pdf.drawString(80,750, 'Lista de Gráficos')
        pdf.setFont("Helvetica-Bold", 10)
        pdf.drawString(245,724, 'Rodada - Jogada oponente  x  Jogada bot python : Resultado')
        
        img = GenerateImg(msgOpp,msgAnswer,resultados)
        
        x_start=0
        y_start=600
        
        for img_file in img:
            pdf.drawImage(img_file, x_start, y_start, width=240, preserveAspectRatio=True, mask='auto')
            y_start-=200
            
        pdf.save()
        
        print('{}.pdf criado com sucesso!'.format(nome_pdf))
    except:
        print('Erro ao gerar {}.pdf'.format(nome_pdf))
```

- Funções para salvar no banco de dados

```python
#Função que cria e recupera um banco de dados mongoDB
def get_database():
 
   # URL do Mongo
    CONNECTION_STRING = "mongodb+srv://Jop:jop@cluster0.cjy8ggp.mongodb.net/test"
 
   # Cria conexão com o Mongo. 
    client = MongoClient(CONNECTION_STRING)
 
   # Cria ou usa database 
    return client["Rodadas"]

#Função para salvar um PDF e os resultados no mongoDB
def saveMongo(msgOpp,msgAnswer):
    k=0
    a={}
    
    path = 'Resultados.pdf'
    
    dbname = get_database()
    rodada = dbname["Rodada"]
    
    #Adicionando todos os resultados no MongoDB
    for opp in msgOpp:
        my=msgAnswer[k]
        k+=1
        key = f'item_{k}'
        value = {
        "Rodada":k,
        "Oponente" : opp,
        "BotPython" : my,
        "Resultado": result(my,opp),
        }
        a[key]=value
        rodada.insert_one(a[key])
    
    #Adicionando o PDF no mongoDB
    fs = gridfs.GridFS(dbname)
    with open(path,"rb") as f:
        encoded_string = base64.b64encode(f.read())
    with fs.new_file(chunkSize=800000,filename=path) as fp:
        fp.write(encoded_string)
    
#Função para ler o pdf do mongoDB   
def read_pdf(filename):
    db = get_database()
    fs = gridfs.GridFS(db)
    # leitura do arquivo salo no mongo
    data = fs.find_one(filter=dict(filename=filename))
    with open('ResultadoMongoDb', "wb") as f:
        f.write(base64.b64decode(data.read()))
```

- Variaveis e dicionarios para auxiliar as funções:

```python

options=['pedra','papel','tesoura','lagarto','spock']

#Dicionario para definir qual classe ganha de qual outra
winners={ #Exemplo pedra ganha de tesoura
    "pedra":["tesoura","lagarto"],
    "lagarto":["papel","spock"],
    "spock":["tesoura","pedra"],
    "tesoura":["lagarto","papel"],
    "papel":["spock","pedra"]
}
#Dicionario para definir qual classe perde para qual outra
losers={ #Exemplo pedra perde para spock
    "pedra":["spock","papel"],
    "lagarto":["pedra","tesoura"],
    "spock":["papel","lagarto"],
    "tesoura":["spock","pedra"],
    "papel":["tesoura","lagarto"]
}

tradutor={#Convenção usada para haver comunicação entre diferentes codigos
    "0":"pedra",
    "1":"spock",
    "2":"papel",
    "3":"lagarto",
    "4":"tesoura",
    "pedra":"0\n",# O \n é para auxiliar a função .readLine() no java
    "spock":"1\n", 
    "papel":"2\n",
    "lagarto":"3\n",
    "tesoura":"4\n"
}
```

- Funções para definir resultados a partir dos dicionarios:

```python
#Função para definir o resultado a partir de duas classes
def result(mine,opponent):
    if(opponent in winners[mine]):
        return 'Vitoria'
    elif(mine==opponent):
        return 'Empate'
    else:
        return 'Derrota'
    
#Função para verificar se uma classe perde para uma outra
def loser(mine,opponent):
    if(opponent in losers[mine]):
        return True
    else:
        return False
```

- Estabelecimento da conexão com o servidor e iníc do jogo

```python
# Define as strings com as informações de cada jogada
msgOpponent = []
msgAnswer = []
continua = True
       
#IP e porta que irá ser acessado
HOST,PORT = Endereco_Ip,40000

with socket(AF_INET,SOCK_STREAM) as s:
    #Tentativa de conexão
    s.connect((HOST,PORT))
    while continua:
        
        #Palpite que irá ser enviado
        answer = nextPlay(msgOpponent)
        s.sendall(tradutor[answer].encode())
        
        #Jogada do oponente
        opp = str(int.from_bytes(s.recv(1024), "big"))
        
        #Adicionando os valores
        msgAnswer.append(answer)
        msgOpponent.append(tradutor[opp])
        
        #Printando o log
        print(printAnswer(msgOpponent,msgAnswer))
        
        #Condinção de parada
        if(len(msgOpponent)==15):
            s.close()
            break

    #Gerando o pdf
    GeneratePDF(msgOpponent,msgAnswer)

    #Salvando no banco de dados
    saveMongo(msgOpponent,msgAnswer)

    #Verificando o pdf do banco de dados
    read_pdf('Resultados.pdf')
```

# Java

## MySQL

- Variáveis e construtor:

```java

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
```

- Metodo para salvar no MySQL uma tupla com o palpite do Bot, o que o oponente jogou e o resultado

```java
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
```

## Rodada

- Variaveis e construtor:

```java
public class Rodada {
    //Dicionario de classe para numero
    private HashMap<String,Integer> tradClasses;
    //Dicionario de numero para classe
    private HashMap<Integer,String> tradNumber;
    //Jogadas do bot java
    private ArrayList<String> jogJava;
    //Jogadas do adversario
    private ArrayList<String> jogOponent;
    //Resultados
    private ArrayList<String> results;
    //Dicionario para quem vence
    private HashMap<String,ArrayList<String> > winner;
    //MySQL server
    private MySQL mySQL;
    public Rodada(){
        mySQL = new MySQL();

        tradClasses= new HashMap<>();
        tradNumber= new HashMap<>();
        results = new ArrayList<>();
        jogJava= new ArrayList<>();
        jogOponent= new ArrayList<>();

        //Adicionando valores aos dicionarios
        tradClasses.put("pedra",0);
        tradClasses.put("spock",1);
        tradClasses.put("papel",2);
        tradClasses.put("lagarto",3);
        tradClasses.put("tesoura",4);
        tradNumber.put(0,"pedra");
        tradNumber.put(1,"spock");
        tradNumber.put(2,"papel");
        tradNumber.put(3,"lagarto");
        tradNumber.put(4,"tesoura");

        winner = new HashMap<>();
        //Definindo qual valor ganha de quem
        ArrayList ganhaDe = new ArrayList();
        ganhaDe.add("tesoura");
        ganhaDe.add("lagarto");
        winner.put("pedra",ganhaDe);//tesoura,lagarto
        ganhaDe = new ArrayList();
        ganhaDe.add("papel");
        ganhaDe.add("spock");
        winner.put("lagarto",ganhaDe);//papel,spock
        ganhaDe = new ArrayList();
        ganhaDe.add("tesoura");
        ganhaDe.add("pedra");
        winner.put("spock",ganhaDe);//tesoura,pedra
        ganhaDe = new ArrayList();
        ganhaDe.add("lagarto");
        ganhaDe.add("papel");
        winner.put("tesoura",ganhaDe);//lagarto,papel
        ganhaDe.add("spock");
        ganhaDe.add("pedra");
        winner.put("papel",ganhaDe);//spock,pedra

    }
```

- Metodos:

```java
		public int nextPlay(String jogadaCod){
        int jogadaCodInt = Integer.parseInt(jogadaCod);
        String Jogada = tradNumber.get(jogadaCodInt);
        String next = Guess();
        String my;
        String result;

        //Se não for a primeira jogada
        if(!jogJava.isEmpty()){
            my = jogJava.get(jogJava.size()-1);
        }
        //Se for a primeira jogada
        else{
            my = next;

        }
        result = getResult(my , Jogada );
        results.add(getResult(my,Jogada));

        //Salva no banco de dados as jogadas
        mySQL.saveRound(my,Jogada,result);

        //Adiciona as jogadas nas arraylist
        jogJava.add(next);
        jogOponent.add(Jogada);

        //Retorna a classe da proxima jogada
        return tradClasses.get(next);
    }
		 private String Guess(){
        String teste=" ";
        for(int i=0;4>i;i++){
            //Se for a primeira jogada
            if(jogOponent.isEmpty()){
                teste="papel";
                break;
            }
            else if(winner.get( tradNumber.get(i) ).contains(jogOponent.get(jogOponent.size()-1))){
                teste =  tradNumber.get(i);
            }
        }
        return teste;
    }
    //Metodo que fala qual o resultado das duas jogadas
    public String getResult(String mine, String opp){
        String result="";
        if(winner.get(mine).contains(opp)){
            return "Vitoria";
        }
        else if(mine==opp){
            return "Empate";
        }
        else{
            return "Derrota";
        }
    }
    //Metodo para adicionar as jogadas em um log
    public String logResults(){
        if(results.isEmpty()){return "";}
        String log=" ";
        for(int i=0;results.size()>i;i++) {
            String opp = jogOponent.get(i);
            String my =jogJava.get(i);
            String result = results.get(i);
            log +=("Na rodada "+(i+1)+":\n O adversario jogou "+opp+" e foi jogado pelo bot java " + my + " resultando em "+result+"\n");
        }
        log += "\n";
        return log;
    }
}
```

## Conexão

- Contrutor e variaveis:

```java
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
```

- Método para iniciar o jogo como servidor:

```java
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
```

## Main

- Onde a instância de servidor é iniciada na porta 40000

```java
public class Main {
    public static void main(String[] args) throws IOException {

        Conexao Section = new Conexao(40000);
        //Inicia uma sessão de jogo com o java como servidor
        Section.NewGameAsServidor();
    }
}
```
