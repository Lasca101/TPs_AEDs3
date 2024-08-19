package listaInvertida;
//package ListaInvertida;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class Lista_Invertida {

    private RandomAccessFile idx;
    private RandomAccessFile data;

    public Lista_Invertida() {
        try {
            File dir = new File("TP2/data/listaInvertida");
            if (!dir.exists()) {
                dir.mkdirs(); // Cria o diretório se ele não existir
                
            }
            File idxFile = new File(dir, "indices.db");
            File dataFile = new File(dir, "data.db");
            this.idx = new RandomAccessFile(idxFile, "rw");
            this.data = new RandomAccessFile(dataFile, "rw");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void criaLista(ArrayList<String> termos, int idFilme) {
        for (String termo : termos) {
            long pos = procuraTermo(termo);
            if (pos == -1) { // Se o termo não existir, adiciona
                adicionaTermo(termo);
                pos = procuraTermo(termo); // Recupera a posição após adicionar
            }
            if (pos != -1) { // Se encontrou a posição, insere o ID
                insereId(termo, idFilme);
            }
        }
    }

    // adiciona novo termo ao arquivo de dados(termos), com ponteiro para inicio da
    // lista encadeada de ids
    private void adicionaTermo(String termo) {
        try {
            idx.seek(idx.length());
            idx.writeUTF(termo);
            idx.writeLong(-1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // adiciona novo id a lista encadeada de ids, retornando a posição desse id
    private long novoId(int id) {
        try {
            long pos = data.length();
            data.seek(pos);
            data.writeInt(id);
            data.writeLong(-1);
            return pos;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    // procura termo no arquivo de índice e retorna sua posição
    private long procuraTermo(String termo) {
        try {
            idx.seek(0);
            while (idx.getFilePointer() < idx.length()) {
                long pos = idx.getFilePointer();
                String termoAtual = idx.readUTF();
                idx.skipBytes(8);
                if (termo.equals(termoAtual)) {
                    return pos;
                }
            }
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    // insere novo id no arquivo de dados
    public void insereId(String termo, int id) {
        try {
            long pos = procuraTermo(termo);
            if (pos != -1) { // se termo existir
                long inicio = inicioLista(pos);
                if (inicio == -1) {// se termo não tiver lista encadeada
                    long atual = novoId(id);
                    idx.seek(pos);
                    idx.readUTF();
                    idx.writeLong(atual);
                } else {// procura final
                    data.seek(inicio);
                    data.readInt();

                    long atual = data.getFilePointer();
                    long prox = data.readLong();

                    while (prox != -1) {
                        data.readInt();
                        atual = data.getFilePointer();
                        prox = data.readLong();
                    }

                    long posCerta = novoId(id);
                    data.seek(atual);
                    data.writeLong(posCerta);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // pega ponteiro para inicio da lista encadeada de ids do termo
    private long inicioLista(long pos) {
        try {
            idx.seek(pos);
            idx.readUTF();// le termo
            long inicio = idx.readLong();
            return inicio;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    // pega todos os ids relacionados a um termo
    public ArrayList<Integer> pegaIds(String termo) {
        ArrayList<Integer> ids = new ArrayList<>();
        try {
            long pos = procuraTermo(termo);// procura o termo no arquivo de indices
            //System.out.println("Termo buscado: " + termo);
            //System.out.println("Posição encontrada no índice: " + pos);

            if (pos != -1) {// se termo foi encontrado
                long posListaEncadeada = inicioLista(pos);// inicio da lista encadeada
                while (posListaEncadeada != -1) {
                    data.seek(posListaEncadeada);// inicio do registro atual
                    int id = data.readInt();
                    ids.add(id);
                    posListaEncadeada = data.readLong();// le ponteiro para proximo da lista encadeada
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ids;
    }

    public void close() {
        try {
            if (idx != null) {
                idx.close();
            }
            if (data != null) {
                data.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
