import java.util.ArrayList;
import java.util.HashMap;

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
