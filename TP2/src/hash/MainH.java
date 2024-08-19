package hash;

//Rodrigo Drummond e Tiago Lascasas
//https://github.com/DigoDrummond/Tp-AedsIII

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import resources.Netflix;

public class MainH {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);

        System.out.println("#------------------------------------#");
        System.out.println("Deseja persistir os arquivos de dados já existentes? [S/N]");
        String resposta = sc.nextLine();
        if(resposta.intern() == "S" || resposta.intern() == "s"){
            System.out.println("Arquivos de dados persistidos.");
            Hash hash = new Hash(true);
            menu(hash);
        } else if(resposta.intern() == "N" || resposta.intern() == "n"){
            System.out.println("Arquivos de dados não persistidos.");

            FileOutputStream arqByte;
            DataOutputStream dosData;
            RandomAccessFile data;

            FileOutputStream fileHash;
            FileOutputStream fileDir;
            
            // Escrevendo dados do .csv no arquivo .db
            try {
                FileReader fileReader = new FileReader("TP2/src/resources/netflix.csv");
                BufferedReader arq = new BufferedReader(fileReader);

                arqByte = new FileOutputStream("TP2/data/hash/data.db");
                dosData = new DataOutputStream(arqByte);
                data = new RandomAccessFile("TP2/data/hash/data.db", "rw");

                fileHash = new FileOutputStream("TP2/data/hash/hash.db");
                fileHash.close();
                fileDir = new FileOutputStream("TP2/data/hash/diretorio.db");
                fileDir.close();

                Hash hash = new Hash(false);

                byte[] ba;
                arq.readLine();// le primeira linha do .csv que tem nomes dos atributos
                dosData.writeInt(0);
                int idFinal = 0;
                while (arq.ready()) {
                    Netflix programa = new Netflix();
                    programa.ler(arq.readLine());

                    hash.insertHash(programa.getId(), data.length());

                    ba = programa.toByteArray();
                    dosData.writeBoolean(false);
                    dosData.writeShort(ba.length);
                    dosData.write(ba);
                    idFinal = programa.getId();
                }
                data.seek(0);
                data.writeInt(idFinal);

                arqByte.close();
                dosData.close();
                data.close();
                fileReader.close();
                arq.close();

                menu(hash);
            } catch (Exception e) {
                System.out.println(e.getMessage() + "\n" + e.getLocalizedMessage());
            }
        } else {
            System.out.println("Opção inválida.");
            System.exit(0);
        }
        sc.close();
    }

    // função menu
    public static void menu(Hash hash) throws Exception {
        Scanner sc = new Scanner(System.in);

        System.out.println("#--------------- MENU ---------------#");
        System.out.print("\n1) Adicionar novo registro na base de dados\n2) Ler registro da base\n3) Atualizar registro\n4) Deletar registro\n5) Sair\n Opção: ");

        switch (sc.nextInt()) {
            //Create
            case 1:
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
                //titulo
                System.out.print("Titulo: ");
                String title = sc.nextLine();
                //diretor
                System.out.print("Diretor: ");
                String director = sc.nextLine();
                //data
                System.out.print("Data de lançamento(dd/MM/yyyy): ");
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
                Netflix novo = new Netflix(tipo, title, director, unixTime);

                // Chamando a função create com o objeto montado
                create(novo, hash);
                menu(hash);
                break;

            //Read
            case 2:
                System.out.println("\n#------------------------------------#\nLer registro da base");
                System.out.print("Digite o id da série/filme que você deseja buscar na base de dados: ");
                int id = lerInteiro(sc);
                read(id, hash);

                menu(hash);
                break;

            //Update    
            case 3:
                System.out.println("\n#------------------------------------#\nAtualizar registro.");
                System.out.print("Digite o id do registro que você deseja atualizar: ");
                id = lerInteiro(sc);
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
                title = sc.nextLine();
                System.out.print("Diretor: ");
                director = sc.nextLine();
                System.out.print("Data de lançamento(dd/MM/yyyy): ");
                // le data
                data = sc.nextLine();
                // formata data digitada
                sdf = new SimpleDateFormat("dd/MM/yyyy");
                dataFormatada = new Date();
                // trasforma data em long para ser armazenado no objeto
                unixTime = 0;
                dataFormatada = sdf.parse(data);
                unixTime = dataFormatada.getTime();

                // coloca atributo tipo em vetor de tamanho fixo
                tipo = new char[7];
                for (int i = 0; i < type.length(); i++) {
                    tipo[i] = type.charAt(i);
                }
                // cria novo objeto Netflix
                Netflix atualizado = new Netflix(id, tipo, title, director, unixTime);
                update(atualizado, hash);
                menu(hash);
                break;


            //Delete
            case 4:
                System.out.println("\n#------------------------------------#\nDeletar registro.");
                System.out.print("Digite o id do registro que você deseja deletar: ");
                id = lerInteiro(sc);
                delete(id, hash);

                menu(hash);
                break;

            case 5:
                System.out.println("\n#------------------------------------#\nPrograma encerrado.");
                System.out.println("#------------------------------------#\n");
                System.exit(0);
                break;

            //
            default:
                System.out.println("Opção inválida.");
                menu(hash);
                break;
        }
        sc.close();
    }

    public static void create(Netflix netflix, Hash hash) throws Exception {
        RandomAccessFile arq;
        try {
            arq = new RandomAccessFile("TP2/data/hash/data.db", "rw");
            byte[] ba;
            arq.seek(0);
            //pega ultimo id do .db e o atualiza
            int ultimoId = arq.readInt();
            ultimoId++;
            arq.seek(0);
            arq.writeInt(ultimoId);

            netflix.setId(ultimoId);

            long tamArq = arq.length();
            arq.seek(tamArq);//ponteiro para final do arquivo 
            ba = netflix.toByteArray();
            arq.writeBoolean(false);//não é lapide
            arq.writeShort(ba.length);//tamanho do novo registro
            arq.write(ba);//registro

            arq.close();

            hash.insertHash(netflix.getId(), tamArq);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void read(int id, Hash hash) throws Exception {
        RandomAccessFile arq;
        try {
            arq = new RandomAccessFile("TP2/data/hash/data.db", "rw");

            long endData = hash.searchHash(id);
            
            if (endData == -1) {
                System.out.println("\nID não encontrado.\n");
            } else {
                arq.seek(endData);
                boolean lapide = arq.readBoolean();
                if(lapide){
                    System.out.println("\nRegistro deletado.\n");
                } else {
                    short tam = arq.readShort();
                    byte[] ba = new byte[tam];
                    arq.read(ba);
                    //Novo objeto netflix recebe registro lido
                    Netflix programa = new Netflix();
                    programa.fromByteArray(ba);
                    System.out.println(programa.toString());
                }
            }

            arq.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void update(Netflix netflix, Hash hash) {
        RandomAccessFile arq;
        try {
            arq = new RandomAccessFile("TP2/data/hash/data.db", "rw");
            long endData = -1; 
            endData = hash.searchHash(netflix.getId());   //busca endereço do registro a ser atualizado
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
                    hash.updateHash(netflix.getId(), tamArq);
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

    public static void delete(int id, Hash hash) throws Exception {
        RandomAccessFile arq;
        try {
            arq = new RandomAccessFile("TP2/data/hash/data.db", "rw");
            long endData = -1;
            endData = hash.deleteHash(id);

            if (endData == -1) {
                System.out.println("\nID não encontrado.\n");
            } else {
                arq.seek(endData);
                arq.writeBoolean(true);
                System.out.println("\nRegistro deletado com sucesso.\n");
                
            }

            arq.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // função interna do programa para verificar se id digitado pelo usuário é valido, prende em looping até digitar valor válido
    private static int lerInteiro(Scanner sc) {
        while (!sc.hasNextInt()) {
            System.out.println("Isso não é um número inteiro.");
            System.out.print("Insira um valor válido: ");
            sc.next(); // Consume o valor não inteiro para evitar um loop infinito
        }
        return sc.nextInt();
    }

}
