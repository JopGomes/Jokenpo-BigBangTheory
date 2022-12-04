#!/usr/bin/env python
# coding: utf-8

# In[ ]:





# In[1]:


import statistics

from reportlab.pdfgen import canvas

import matplotlib.pyplot as plt

import random

from pymongo import MongoClient
import gridfs
import base64

from socket import *


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

#Função que gera as imagens
def GenerateImg(msgOpp,msgAnswer):
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
        
        img = GenerateImg()
        
        x_start=0
        y_start=600
        
        for img_file in img:
            pdf.drawImage(img_file, x_start, y_start, width=240, preserveAspectRatio=True, mask='auto')
            y_start-=200
            
        pdf.save()
        
        print('{}.pdf criado com sucesso!'.format(nome_pdf))
    except:
        print('Erro ao gerar {}.pdf'.format(nome_pdf))

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


# In[2]:



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
    "pedra":"0\n",
    "spock":"1\n",
    "papel":"2\n",
    "lagarto":"3\n",
    "tesoura":"4\n"
}

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


# In[3]:


# Define as strings com as informações de cada jogada
msgOpponent = []
msgAnswer = []
continua = True
       
#IP e porta que irá ser acessado
HOST,PORT = "172.15.1.211",40000

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


# In[ ]:




