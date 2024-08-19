package arvoreBP;

//Rodrigo Drummond e Tiago Lascasas
//https://github.com/DigoDrummond/Tp-AedsIII

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Scanner;
import resources.Netflix;

public class ArvoreBPlus {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        ArvoreBP arvoreBP = new ArvoreBP();

        FileOutputStream arqByte;
        DataOutputStream dos;

        // Escrevendo dados do .csv no arquivo .db
        try {

            FileReader fileReader = new FileReader(
                    "TP2/src/resources/netflix.csv");
            BufferedReader arq = new BufferedReader(fileReader);
            RandomAccessFile raf = new RandomAccessFile(
                    "TP2/data/arvoreBP/data.db", "rw");

            arqByte = new FileOutputStream(
                    "TP2/data/arvoreBP/data.db");
            dos = new DataOutputStream(arqByte);
            byte[] ba;
            long ptr;
            int primeiraVez = 0;

            if (primeiraVez == 0) {
                arvoreBP.inicializar();
                arq.readLine();// le primeira linha do .csv que tem nomes dos atributos
                while (arq.ready()) {
                    Netflix programa = new Netflix();
                    programa.ler(arq.readLine());
                    ptr = raf.getFilePointer();// pega posição do registro no arquivo de dados
                    ba = programa.toByteArray();
                    dos.writeBoolean(false);
                    dos.writeShort(ba.length);
                    dos.write(ba);
                    // System.out.println("passou");
                    arvoreBP.inserir(programa.getId(), ptr);
                }

            }

            menu(arvoreBP);

            dos.close();
            arqByte.close();
            arq.close();
            raf.close();
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n" + e.getLocalizedMessage());
        }

        sc.close();
    }

    // função menu
    public static void menu(ArvoreBP arvoreBP) throws Exception {
        Scanner sc = new Scanner(System.in);

        System.out.println("#--------------- MENU ---------------#");
        System.out.print(
                "\n1) Adicionar novo registro na base de dados\n2) Ler registro da base\n3) Atualizar registro\n4) Deletar registro\n5) Sair\n Opção: ");

        int opcao = sc.nextInt();
        // Create
        if (opcao == 1) {
            // Montando o objeto Netflix para enviar para a função create
            System.out.println("\n#------------------------------------#");
            System.out.println("Adicionar novo registro na base de dados");
            sc.nextLine();
            String type;
            int selecao;
            // permite que usuário escolha somente uma das duas opções
            while (true) {
                System.out.print("Selecione o tipo [ TV Show(1) / Movie(2) ]: ");
                selecao = sc.nextInt();
                if (selecao == 1 || selecao == 2) {
                    break;
                } else {
                    System.out.println("Opção inválida.");
                }
            }
            if (selecao == 1) {
                type = "TV Show";
            } else {
                type = "Movie";
            }

            sc.nextLine();// pega \n entre leitura de int e string
            // titulo
            System.out.print("Titulo: ");
            String title = sc.nextLine();
            // diretor
            System.out.print("Diretor: ");
            String director = sc.nextLine();
            // data
            System.out.print("Data de lançamento(dd/MM/yyyy): ");
            String data = sc.nextLine();
            // formata data digitada
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date dataFormatada = sdf.parse(data);
            ;
            // trasforma data em long para ser armazenado no objeto
            long unixTime = 0;
            unixTime = dataFormatada.getTime();

            // coloca atributo tipo em vetor de tamanho fixo
            char[] tipo = new char[7];
            for (int i = 0; i < type.length(); i++) {
                tipo[i] = type.charAt(i);
            }
            // cria novo objeto Netflix
            Netflix novo = new Netflix(tipo, title, director, unixTime);

            // inserindo novo registro na árvore
            arvoreBP.inserir(novo.getId(), create(novo, arvoreBP));
            menu(arvoreBP);

            // Read
        } else if (opcao == 2) {
            System.out.println("\n#------------------------------------#\nLer registro da base");
            System.out.print("Digite o id da série/filme que você deseja buscar na base de dados: ");
            int id = lerInteiro(sc);
            read(id);

            menu(arvoreBP);

            // Update
        } else if (opcao == 3) {
            System.out.println("\n#------------------------------------#\nAtualizar registro.");
            System.out.print("Digite o id do registro que você deseja atualizar: ");
            int id = lerInteiro(sc);
            int selecao;
            String type;
            // permite que usuário escolha somente uma das duas opções
            while (true) {
                System.out.print("Selecione o tipo [ TV Show(1) / Movie(2) ]: ");
                selecao = sc.nextInt();
                if (selecao == 1 || selecao == 2) {
                    break;
                } else {
                    System.out.println("Opção inválida.");
                }
            }
            if (selecao == 1) {
                type = "TV Show";
            } else {
                type = "Movie";
            }
            sc.nextLine();// pega \n entre leitura de int e string
            System.out.print("Titulo: ");
            String title = sc.nextLine();
            System.out.print("Diretor: ");
            String director = sc.nextLine();
            System.out.print("Data de lançamento(dd/MM/yyyy): ");
            // le data
            String data = sc.nextLine();
            // formata data digitada
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date dataFormatada = new Date();
            // trasforma data em long para ser armazenado no objeto
            long unixTime = 0;
            dataFormatada = sdf.parse(data);
            unixTime = dataFormatada.getTime();

            // coloca atributo tipo em vetor de tamanho fixo
            char[] tipo = new char[7];
            for (int i = 0; i < type.length(); i++) {
                tipo[i] = type.charAt(i);
            }
            // cria novo objeto Netflix
            Netflix atualizado = new Netflix(id, tipo, title, director, unixTime);
            update(atualizado, arvoreBP);
            menu(arvoreBP);

            // Delete
        } else if (opcao == 4) {
            System.out.println("\n#------------------------------------#\nDeletar registro.");
            System.out.print("Digite o id do registro que você deseja deletar: ");
            int id = lerInteiro(sc);
            delete(id);

            menu(arvoreBP);

        } else if (opcao == 5) {
            System.out.println("\n#------------------------------------#\nPrograma encerrado.");
            System.out.println("#------------------------------------#\n");
            System.exit(0);
        } else {
            System.out.println("Opção inválida.");
            menu(arvoreBP);
        }

        sc.close();
    }

    public static long create(Netflix netflix, ArvoreBP arvoreBP) throws Exception {
        RandomAccessFile arq;
        long pos;// posição no arquivo de dados
        try {
            arq = new RandomAccessFile("TP2/data/arvoreBP/data.db", "rw");
            byte[] ba;
            arq.seek(0);
    
            arq.seek(arq.length());// ponteiro para final do arquivo
            pos = arq.getFilePointer();
            ba = netflix.toByteArray();
            arq.writeBoolean(false);// não é lapide
            arq.writeShort(ba.length);// tamanho do novo registro
            arq.write(ba);// registro
    
            // Inserir na árvore
            arvoreBP.inserir(netflix.getId(), pos);
    
            arq.close();
            return pos;
    
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    

    public static void read(long pos) throws Exception {
        RandomAccessFile arq;
        try {
            arq = new RandomAccessFile("TP2/data/arvoreBP/data.db", "rw");
            // move ponteiro do arquivo para priemeiro registro, pulando byts de registro do
            // último id registrado
            arq.seek(pos);
    
            // Lê os dados do registro
            boolean lapide = arq.readBoolean();
            short tamanhoRegistro = arq.readShort();
            byte[] ba = new byte[tamanhoRegistro - Integer.BYTES]; // Excluindo o tamanho do ID
            arq.read(ba);
    
            // Cria um objeto Netflix a partir dos dados lidos
            Netflix programa = new Netflix();
            programa.fromByteArray(ba);
    
            // Exibe o registro
            System.out.println(programa.toString());
    
            arq.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    public static void update(Netflix netflix, ArvoreBP arvoreBP) {
        RandomAccessFile arq;
        try {
            arq = new RandomAccessFile("TP2/data/arvoreBP/data.db", "rw");
            long endData = -1; 
            endData = arvoreBP.buscar(netflix.getId());   //busca endereço do registro a ser atualizado
            boolean atualizado = false;

            if (endData == -1) {
                System.out.println("\nID não encontrado.\n");
            } else {
                byte[] ba = netflix.toByteArray();//transforma objeto em vetor de bytes
                arq.seek(endData+1);
                short tamanhoRegistro = arq.readShort();
                int baLength = ba.length;
                if (tamanhoRegistro >= baLength) {
                    // Posiciona o ponteiro para o início do registro considerando lápide e tamanho
                    arq.seek(endData);
                    arq.writeBoolean(false); // Mantém a lápide como falsa
                    arq.writeShort(ba.length); // Atualiza o tamanho se necessário
                    arq.write(ba); // Escreve o novo registro sobre o antigo
                    for(int i=0; i<(tamanhoRegistro - baLength); i++){
                        arq.writeByte(-1);
                    }
                    atualizado = true;
                    System.out.println("Registro atualizado com sucesso!");
                }else{
                    //se id for encontrado mas registro for maior que antigo, marca antigo como lápide
                    arq.seek(endData);
                    arq.writeBoolean(true);//atualiza registro desatualizado para lápide, para evitar conflito na leitura
                }

                if (!atualizado) {
                    // Se o registro foi encontrado mas não atualizado devido ao tamanho, marca o
                    // antigo como removido e adiciona o novo ao final
                    long tamArq = arq.length();
                    arvoreBP.atualizar(netflix.getId(), create(netflix, arvoreBP));
                    arq.seek(tamArq);
                    arq.writeBoolean(false); // Lápide para o novo registro
                    arq.writeShort(ba.length); // Tamanho do novo registro
                    arq.write(ba); // Dados do novo registro
                    System.out.println("Registro atualizado e movido para o final do arquivo.");
                }
            }

            arq.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void delete(int id) throws Exception {
        RandomAccessFile arq;
        try {
            arq = new RandomAccessFile("TP2/data/arvoreBP/data.db", "rw");
            // move ponteiro do arquivo para priemeiro registro, pulando byts de registro do
            // último id registrado
            arq.seek(4);
            long ptr = arq.getFilePointer();
            boolean idValido = false;

            while (arq.getFilePointer() < arq.length()) {
                boolean lapide = arq.readBoolean();
                short tam = arq.readShort();
                ptr += 3;
                // se for lapide pega tamanho do registro e pula para próximo
                if (lapide) {
                    ptr += tam;
                    arq.seek(ptr);
                    // se não for lápide
                } else {
                    int idArq = arq.readInt();
                    // se id for encontrado
                    if (idArq == id) {
                        ptr -= 3;
                        arq.seek(ptr);// move ponteiro para inicio do registro
                        arq.writeBoolean(true);
                        arq.seek(arq.length());
                        idValido = true;
                        System.out.println("\nRegistro deletado com sucesso.\n");
                        // se não for igual ao procurado
                    } else {
                        ptr += tam;
                        arq.seek(ptr);
                    }
                }
            }
            // caso não encontre o id procurado
            if (idValido == false) {
                System.out.println("\nID não encontrado.\n");
            }

            arq.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // função interna do programa para verificar se id digitado pelo usuário é
    // valido, prende em looping até digitar valor válido
    private static int lerInteiro(Scanner sc) {
        while (!sc.hasNextInt()) {
            System.out.println("Isso não é um número inteiro.");
            System.out.print("Insira um valor válido: ");
            sc.next(); // Consume o valor não inteiro para evitar um loop infinito
        }
        return sc.nextInt();
    }

}
