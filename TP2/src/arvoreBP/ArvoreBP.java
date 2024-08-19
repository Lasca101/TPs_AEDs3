package arvoreBP;

import java.io.RandomAccessFile;
import java.util.Comparator;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

class Chave {

    public long esq;
    public int id;
    public long end;// ptr para arquivo de dados
    public long dir;

    public Chave(long esq, int id, long end, long dir) {
        this.esq = esq;
        this.id = id;
        this.end = end;
        this.dir = dir;
    }

    public String toString() {
        return "Chave [esq=" + esq + ", id=" + id + ", endereço =" + end + ", dir=" + dir + "]";
    }

}

class No {

    public int n;// numeros de elementos na página
    public No pai;
    public Long posArqInd;

    public ArrayList<Chave> chaves;

    public No(int n, Long posArqInd, ArrayList<Chave> chaves) {
        this.n = n;
        this.posArqInd = posArqInd;
        this.chaves = chaves;
    }

}

public class ArvoreBP {

    public No raiz;
    public int ordem = 8;
    public RandomAccessFile arvore;
    public int altura;

    public ArvoreBP(){
        
    }

    // ---------- CONSTRUTOR -----------

    public void inicializar() throws Exception {
        this.arvore = new RandomAccessFile("C:\\Users\\rodri\\OneDrive\\Área de Trabalho\\AEDs3\\TP2\\data\\arvoreBP\\arvore.db", "rw");
        if (arvore.length() == 0) {// se arquivo estiver vazio, inicializa árvore com raiz null
            this.altura = 0;
            this.raiz = null;
            arvore.seek(0);
            arvore.writeInt(0);
            arvore.writeInt(8);
        } else {// se não, le altura da arvore e a posição da raiz do arquivo e carrega a raiz
            arvore.seek(0);
            this.altura = arvore.readInt();
            System.out.println(altura);
            long posRaiz = arvore.readLong();
            //System.out.println("posição raiz: " + posRaiz);
            //System.out.println("tamanho arquivo: " + arvore.length());
            arvore.seek(posRaiz);
            this.raiz = lerNo();
            //System.out.println("saiu construtor");
        }
    }

    public ArvoreBP(RandomAccessFile arvore) throws Exception {
        this.arvore = arvore;

        // Lendo altura da árvore e posição da raiz
        arvore.seek(0);
        this.altura = arvore.readInt();
        long posRaiz = arvore.readLong();

        // Carregando a raiz
        arvore.seek(posRaiz);
        this.raiz = lerNo();
    }

    // ---------- NÓS ----------

    // le um nó da árvore
    private No lerNo() throws Exception {
        ArrayList<Chave> chaves = new ArrayList<>();// armazena registros do nó
        long posArqInd = arvore.getFilePointer();
        No no = new No(-1, (long) -1, chaves);
        if (posArqInd != -1) {
            int n = arvore.readInt();// número de registros no nó
            long dir = 0;
            long esq = 0;

            // le cada registro do nó
            for (int i = 0; i < n; i++) {
                // se for primero registro, le ponteiro mais a esquerda
                if (i == 0) {
                    esq = arvore.readLong();
                } else {
                    esq = dir;
                }

                int id = arvore.readInt();
                long ptrArqDados = arvore.readLong();
                dir = arvore.readLong();

                Chave registro = new Chave(esq, id, ptrArqDados, dir);
                chaves.add(registro);
            }
            // cria nó com registros lidos
            no.n = n;
            no.posArqInd = posArqInd;
            no.chaves = chaves;

        } else {
            Chave chave = new Chave(-1, -1, -1, -1);
            No tmp = new No(1, arvore.length(), new ArrayList<Chave>(Arrays.asList(chave)));
            escreverNo(tmp);
            arvore.seek(posArqInd);
        }

        return no;

    }

    // escreve novo nó(inteiro) no arquivo
    private void escreverNo(No no) throws Exception {
        arvore.seek(no.posArqInd);// vai para posição do nó na árvore
        arvore.writeInt(no.n);// escreve qntd de registros no nó

        if (no.n > 0) {
            for (int i = 0; i < no.n; i++) {
                escreverRegistro(no.chaves.get(i), i, no.n);
            }
        } else {
            for (int i = 0; i < ordem - 1; i++) {
                escreverRegistro(i, ordem - 1);
            }
        }

    }

    // escreve parte de um nó no arquivo e preenche aloca resto do espaço necessário
    private void escreverNo(Chave chave) throws Exception {
        arvore.seek(arvore.length());
        arvore.writeInt(1);// número de registros no nó
        escreverRegistro(chave, 0, 1);

        // reserva espaço necessário para resto do nó
        for (int i = 1; i < ordem - 1; i++) {
            arvore.writeLong(-1);
            arvore.writeInt(-1);
            arvore.writeLong(-1);
        }
    }

    // insere novo registro em folha
    private void inserirRegistroNo(No no, Chave chave) throws Exception {
        no.chaves.add(chave);
        no = ordenaRegistros(no);
        no.n++;
        escreverNo(no);
    }

    // ---------- REGISTROS ----------

    // escreve registro no arquivo
    private void escreverRegistro(Chave chave, int i, int n) throws Exception {
        arvore.writeLong(chave.esq);
        arvore.writeInt(chave.id);
        arvore.writeLong(chave.end);
        if (i == n - 1) {
            arvore.writeLong(chave.dir);
        }
    }

    // preenche espaços vazios(sem registro) de um nó com valor padrão
    private void escreverRegistro(int i, int n) throws Exception {
        arvore.writeLong(-1);
        arvore.writeInt(-1);
        arvore.writeLong(-1);
        if (i == n) {
            arvore.writeLong(-1);
        }
    }

    // le um registro
    private Chave lerRegistro() throws Exception {
        long esq = arvore.readLong();
        int id = arvore.readInt();
        long end = arvore.readLong();
        long dir = arvore.readLong();
        Chave chave = new Chave(esq, id, end, dir);
        return chave;
    }

    // ordena os registros da folha
    private No ordenaRegistros(No no) throws Exception {
        Comparator<Chave> comparator = new Comparator<Chave>() {
            @Override
            public int compare(Chave chave1, Chave chave2) {
                return Integer.compare(chave1.id, chave2.id);
            }
        };
        Collections.sort(no.chaves, comparator);
        return no;
    }

    // ---------- RAIZ ----------

    // escreve raiz e uma escreverNo para alocar espaço para restante dela
    private void escreverRaiz(Chave chave) throws Exception {
        arvore.seek(4);// pula altura da árvore
        arvore.writeLong(8);// escreve posição da raiz
        escreverNo(chave);// escreve primeiro registro da raiz e reserva resto do espaço para ela
    }

    // em caso de split, escreve nova raiz
    private void escreverRaiz(No raiz) throws Exception {
        arvore.seek(0);
        arvore.writeInt(this.altura + 1);// com split, aumenta altura da árvore no arquivo
        this.altura++;

        arvore.writeLong(raiz.posArqInd);
        raiz.posArqInd = raiz.posArqInd;
        this.raiz = raiz;
        escreverNo(raiz);
    }

    private void iniciarRaiz(No no) throws Exception {
        arvore.seek(0);
        arvore.writeInt(this.altura + 1);
        this.altura++;
        arvore.writeLong(arvore.length() + 4);// escreve nova posição da raiz
        no.posArqInd = arvore.length();
        arvore.seek(arvore.length());
        this.raiz = no;
        escreverNo(no.chaves.get(0));// inicializa nova raiz com primeiro registro do arraylist

    }

    // aponta para folha onde novo id sera inserido
    private void apontaParaFolha(int id, long ptr, No no) throws Exception {
        long pos = 0;
        int alturaAtual = 0;
        boolean jaExiste = false;
        //System.out.println("aponta folha");

        while (alturaAtual < this.altura - 1) {
            for (int i = 0; i < no.n; i++) {
                if (id < no.chaves.get(i).id) {
                    pos = no.chaves.get(i).esq;
                    //System.out.println("aponta folha2");
                    break;
                }
                if (id == no.chaves.get(i).id) {
                    jaExiste = true;
                    //System.out.println("aponta folha3");

                    break;
                }
            }
            if (id > no.chaves.get(no.n - 1).id) {
                pos = no.chaves.get(no.n-1).dir;
                //System.out.println("aponta folha4");

            }
            if (id == no.chaves.get(no.n - 1).id) {
                jaExiste = true;
                //System.out.println("aponta folha5");
                break;
            }
            // le o proximo nó e define seu pai como o atual
            arvore.seek(pos);
            //System.out.println("antes");
            No prox = lerNo();
            prox.pai = no;
            alturaAtual++;
        }
        if (!jaExiste) {
            //System.out.println("vai para inserir folha");
            inserirNaFolha(id, ptr, no);
        }
        //System.out.println("nao entrou em inserir folha");

    }

    // insere novo registro na folha ja existente
    private void inserirNaFolha(int id, long ptr, No pai) throws Exception {
        long pos = 0;
        Boolean jaExiste = false;
        //System.out.println("inserir folha");
        for (int i = 0; i < pai.n; i++) {
            if (id < pai.chaves.get(i).id && id != -1) {
                pos = pai.chaves.get(i).esq;
                break;
            }
            if (id == pai.chaves.get(i).id) {
                jaExiste = true;
                break;
            }
        }
        if (id > pai.chaves.get(pai.n - 1).id && id != -1) {
            pos = pai.chaves.get(pai.n-1).dir;
        }
        if (id == pai.chaves.get(pai.n - 1).id) {
            jaExiste = true;
        }

        if (!jaExiste) {
            arvore.seek(pos);// posiciona ptr na folha onde registro será inserido
            No folha = lerNo();
            folha.pai = pai;
            Chave chave = new Chave(-1, id, ptr, -1);

            // cabe na folha
            if (folha.n < ordem - 1) {
                inserirRegistroNo(folha, chave);
            } else {// folha cheia
                //System.out.println("chama split");
                split(folha, chave);
            }
        }

    }

    // divide nó que ja esta cheio, no caso de uma nova inserção nesse mesmo nó
    public void split(No no, Chave chave) throws Exception {
        Chave meio;
        int mediano = (ordem / 2) - 1; // calcula a posição da chave mediana
        if (no.pai == null) {
            mediano = 0;
            this.altura = this.altura++;
            arvore.seek(0);
            arvore.writeInt(this.altura);
        }
    
        No novoNo;
        meio = no.chaves.get(mediano); // seleciona a chave mediana
        
        if (no.pai != null && no.pai.n < ordem - 1) { // se o nó pai não estiver cheio
            ArrayList<Chave> maiores = new ArrayList<Chave>(no.chaves.subList(mediano + 1, no.chaves.size()));
    
            for (int i = mediano; i < no.n; i++) {
                no.chaves.remove(mediano);
            }
    
            no.n = no.chaves.size();
    
            novoNo = new No(maiores.size(), arvore.length(), maiores);
    
            meio.esq = no.posArqInd;
            meio.dir = novoNo.posArqInd;
    
            novoNo.pai = no.pai;
    
            if (chave.id > meio.id) {
                novoNo.chaves.add(chave);
                novoNo.n = novoNo.chaves.size();
                novoNo = ordenaRegistros(novoNo);
            } else {
                no.chaves.add(chave);
                no.n = no.chaves.size();
                no = ordenaRegistros(no);
            }
    
            escreverNo(no);
            escreverNo(novoNo);
    
            if (no.pai == null) {
                No novaRaiz = new No(1, arvore.length(), new ArrayList<Chave>(Arrays.asList(meio)));
                escreverRaiz(novaRaiz);
            } else if (no.pai.n < ordem - 1) {
                no.pai.chaves.add(meio);
                no.pai.n++;
                ordenaRegistros(no);
                escreverNo(no);
            } else {
                split(no, meio);
            }
    
        } else {
            ArrayList<Chave> maiores = new ArrayList<Chave>(no.chaves.subList(mediano + 1, no.chaves.size()));
    
            for (int i = mediano; i < no.n; i++) {
                no.chaves.remove(mediano);
            }
    
            no.n = no.chaves.size();
    
            novoNo = new No(maiores.size(), arvore.length(), maiores);
    
            meio.esq = no.posArqInd;
            meio.dir = novoNo.posArqInd;
    
            novoNo.pai = no.pai;
    
            if (chave.id > meio.id) {
                novoNo.chaves.add(chave);
                novoNo.n = novoNo.chaves.size();
                novoNo = ordenaRegistros(novoNo);
            } else {
                no.chaves.add(chave);
                no.n = no.chaves.size();
                no = ordenaRegistros(no);
            }
    
            escreverNo(no);
            escreverNo(novoNo);
    
            No novaRaiz = new No(1,  arvore.length(), new ArrayList<Chave>(Arrays.asList(meio)));
            escreverRaiz(novaRaiz);
        }    
    }
    
    

    public static int count = 0;
    // ---------- CRUD -----------
    public void inserir(int id, long ptrDados) throws Exception {
        //System.out.println("Inseridos:" + count++);
        if (this.raiz == null) {// se árvore não existir
            Chave chave = new Chave(100, id, ptrDados, 192);
            No raiz = new No(1, (long) 8, new ArrayList<Chave>(Arrays.asList(chave)));
            iniciarRaiz(raiz);
            No esq = new No(0, arvore.length(), new ArrayList<Chave>());
            escreverNo(esq);
            No dir = new No(0, arvore.length(), new ArrayList<Chave>());
            escreverNo(dir);
        } else {
            apontaParaFolha(id, ptrDados, this.raiz);
        }
    }

    public long buscar(int id) throws Exception{
        //le altura da árvore e posição da raiz do cabeçalho do arquivo
        arvore.seek(0);
        this.altura = arvore.readInt();
        long posRaiz = arvore.readLong();
        arvore.seek(posRaiz);

        No no = lerNo();

        long pos = 0;
        int alturaAtual = 0;

        while(alturaAtual< this.altura){
            for(int i=0;i<no.n;i++){
                if(id==no.chaves.get(i).id){//se encontrar retorna endereço no arquivo de dados
                    return no.chaves.get(i).end;
                }
                if(id < no.chaves.get(i).id){// se for menor buscar no filho a esquerda
                    pos = no.chaves.get(i).esq;
                    break;
                }
            }
            if(id > no.chaves.get(no.n-1).id){
                pos = no.chaves.get(no.n-1).dir;
            }
            if(id == no.chaves.get(no.n-1).id){
                return no.chaves.get(no.n-1).end;
            }

            arvore.seek(pos);
            No prox = lerNo();
            prox.pai = no;
            alturaAtual++;
            no = prox;
        }
        return -1;
    }

    public void atualizar(int id, long ptrDados) throws Exception{
        arvore.seek(0);
        this.altura = arvore.readInt();
        long posRaiz = arvore.readLong();
        arvore.seek(posRaiz);

        No no = lerNo();

        long pos = 0;
        int alturaAtual = 0;

        while(alturaAtual< this.altura){
            for(int i=0;i<no.n;i++){
                if(id==no.chaves.get(i).id){
                    no.chaves.get(i).end = ptrDados;
                    escreverNo(no);
                    return;
                }
                if(id < no.chaves.get(i).id){
                    pos = no.chaves.get(i).esq;
                    break;
                }
            }
            if(id > no.chaves.get(no.n-1).id){
                pos = no.chaves.get(no.n-1).dir;
            }
            if(id == no.chaves.get(no.n-1).id){
                no.chaves.get(no.n-1).end = ptrDados;
                escreverNo(no);
                return;
            }

            arvore.seek(pos);
            No prox = lerNo();
            prox.pai = no;
            alturaAtual++;
            no = prox;
        }
    }
}
