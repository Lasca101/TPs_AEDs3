package hash;

import java.io.RandomAccessFile;
import java.util.ArrayList;


public class Hash {
    private int pGlobal;
    private int tamDir;
    private long diretorio[];
    private RandomAccessFile arqHash;
    private RandomAccessFile arqDir;

    public Hash(boolean persistir){
        try {
            this.arqHash = new RandomAccessFile("TP2/data/hash/hash.db", "rw");
            this.arqDir = new RandomAccessFile("TP2/data/hash/diretorio.db", "rw");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if(persistir) {
            try {
                arqDir.seek(0);
                this.pGlobal = arqDir.readInt();
                this.tamDir = arqDir.readInt();
                this.diretorio = new long[tamDir];
                for(int i=0; i<tamDir; i++){
                    this.diretorio[i] = arqDir.readLong();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            this.pGlobal = 0;
            this.tamDir = (int)Math.pow(2, pGlobal);
            this.diretorio = new long[tamDir];
            diretorio[0] = 0;
            iniciaBucket(0, 0);

            try {
                arqDir.writeInt(pGlobal);
                arqDir.writeInt(tamDir);
                for(int i=0; i<tamDir; i++){
                    arqDir.writeLong(diretorio[i]);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage() + "\n" + e.getLocalizedMessage());
            }
        }
    }

    public int getP() {
        return this.pGlobal;
    }

    public void setP(int pGlobal) {
        this.pGlobal = pGlobal;
    }

    public int getTamDir() {
        return this.tamDir;
    }

    public void setTamDir(int tamDir) {
        this.tamDir = tamDir;
    }

    public long[] getDiretorio() {
        return this.diretorio;
    }

    public void setDiretorio(long[] diretorio) {
        this.diretorio = diretorio;
    }

    public void setValDiretorio(int pos, long val) {
        this.diretorio[pos] = val;
        try {
            arqDir.seek(pos*8 + 8);
            arqDir.writeLong(val);
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n" + e.getLocalizedMessage());
        }
    }

    public long getValDiretorio(int pos) {
        return this.diretorio[pos];
    }

    public void duplicaDiretorio() {
        pGlobal++;
        tamDir *= 2;
        long novoDiretorio[] = new long[tamDir];
        for (int i = 0; i < tamDir/2; i++) {
            novoDiretorio[i] = diretorio[i];
        }
        int j = 0;
        for (int i = tamDir/2; i < tamDir; i++, j++) {
            novoDiretorio[i] = diretorio[j];
        }
        diretorio = novoDiretorio;

        try {
            arqDir.seek(0);
            arqDir.writeInt(pGlobal);
            arqDir.writeInt(tamDir);
            for(int i=0; i<tamDir; i++){
                arqDir.writeLong(diretorio[i]);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n" + e.getLocalizedMessage());
        }
    }

    public int hash(int k) {
        return k % tamDir;
    }

    public boolean confereBit(int index, int bit){
        //se retornar true, o bit é 1, se retornar false, o bit é 0
        //variavel bit é a posição do bit, sendo que 0 é o bit menos significativo (mais a direita)
        return (index & (1 << bit)) != 0;
    }

    public ArrayList<Integer> buscaIndices(long endBucket) {
        ArrayList<Integer> indices = new ArrayList<>();
        for(int i=0; i<tamDir; i++){
            if(diretorio[i] == endBucket){
                indices.add(i);
            }
        }
        return indices;
    }

    public void iniciaBucket(long endBucket, int pLocal) {
        try {
            arqHash.seek(endBucket);
            arqHash.writeInt(pLocal);
            for(int i=0; i<5292; i++){
                arqHash.writeByte(-1);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n" + e.getLocalizedMessage());
        }
    }

    public void zeraBucket(long endBucket){
        try {
            arqHash.seek(endBucket + 4);
            for(int i=0; i<5292; i++){
                arqHash.writeByte(-1);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n" + e.getLocalizedMessage());
        }
    }

    public boolean percorreBucketInserindo(int id, long end, int z){
        try {
            long endBucket = diretorio[z];
            long fim = endBucket + 5296;
            endBucket += 4;
            boolean inseriu = false;
            while(endBucket < fim){
                arqHash.seek(endBucket);
                if(arqHash.readByte() == -1){
                    arqHash.seek(endBucket);
                    arqHash.writeInt(id);
                    arqHash.writeLong(end);
                    endBucket = fim;
                    inseriu = true;
                } else {
                    endBucket += 12;
                }
            }
            return inseriu;
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n" + e.getLocalizedMessage());
        }
        return false;
    }

    public void reorganizaBucket(int id, long end, long endBucket, int x){
        try {
            long copiaEnd = endBucket;
            long fimBucket = endBucket + 5296;
            endBucket += 4;

            ArrayList<Integer> ids = new ArrayList<>();
            ArrayList<Long> ends = new ArrayList<>();
            arqHash.seek(endBucket);
            while(endBucket < fimBucket && arqHash.readByte() != -1){
                arqHash.seek(endBucket);
                ids.add(arqHash.readInt());
                ends.add(arqHash.readLong());
                endBucket += 12;
            }
            boolean repete = false;

            int y = hash(id);

            int count = 0;
            int tamIds = ids.size();
            for(int i=0; i<tamIds; i++){
                int hashId = hash(ids.get(i));
                if(hashId == y){
                    ++count;
                } else {
                    i = tamIds;
                }
            }
            if(count == tamIds){
                repete = true;
            }
            if(repete){
                if(copiaEnd == diretorio[y]){
                    insertHash(id, end);
                } else {
                    zeraBucket(copiaEnd);
                    for(int i=0; i<ids.size(); i++){
                        percorreBucketInserindo(ids.get(i), ends.get(i), y);
                    }
                }
            } else {
                ids.add(id);
                ends.add(end);
                zeraBucket(copiaEnd);
                for(int i=0; i<ids.size(); i++){
                    int z = hash(ids.get(i));
                    percorreBucketInserindo(ids.get(i), ends.get(i), z);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n" + e.getLocalizedMessage());
        }
    }

    public void insertHash(int id, long end) throws Exception{
        try {
            int x = hash(id);
            long endBucket = getDiretorio()[x];
            arqHash.seek(endBucket);
            int pLocal = arqHash.readInt();
            boolean inseriu = percorreBucketInserindo(id, end, x);

            if(pLocal > pGlobal){
                throw new Exception("Erro: pLocal maior que pGlobal");
            }
            if(inseriu == false){
                //primeiro tratamento se o p local for igual ao p global e o bucket estiver cheio
                if(pLocal == pGlobal) {
                    duplicaDiretorio();
                }
                //segundo tratamento se o p local for menor que o p global e o bucket estiver cheio
                pLocal++;
                arqHash.seek(endBucket);
                arqHash.writeInt(pLocal);
                long tamArq = arqHash.length();
                iniciaBucket(tamArq, pLocal);
                ArrayList<Integer> indices = buscaIndices(endBucket);
                for(int i = 0; i < indices.size(); i++){
                    if(confereBit(indices.get(i), pLocal-1)){
                        setValDiretorio(indices.get(i), tamArq);
                    }
                }
                reorganizaBucket(id, end, endBucket, x);

            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n" + e.getLocalizedMessage());
        }
        
    }

    public long searchHash(int id) {
        long endData = -1;
        try {
            int x = hash(id);
            long endBucket = diretorio[x];
            long fimBucket = endBucket + 5296;
            endBucket += 4;
            
            while(endBucket < fimBucket){
                arqHash.seek(endBucket);
                int idArq = arqHash.readInt();
                if(idArq == id){
                    endData = arqHash.readLong();
                    endBucket = fimBucket;
                }
                endBucket += 12;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n" + e.getLocalizedMessage());
        }
        return endData;
    }

    public void updateHash(int id, long end) {
        try {
            int x = hash(id);
            long endBucket = diretorio[x];
            long fimBucket = endBucket + 5296;
            endBucket += 4;
            
            while(endBucket < fimBucket){
                arqHash.seek(endBucket);
                int idArq = arqHash.readInt();
                if(idArq == id){
                    arqHash.writeLong(end);
                    endBucket = fimBucket;
                }
                endBucket += 12;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n" + e.getLocalizedMessage());
        }
    }

    public long deleteHash(int id) {
        long endData = -1;
        try {
            int x = hash(id);
            long endBucket = diretorio[x];
            long fimBucket = endBucket + 5296;
            endBucket += 4;
            
            while(endBucket < fimBucket){
                arqHash.seek(endBucket);
                int idArq = arqHash.readInt();
                if(idArq == id){
                    endData = arqHash.readLong();
                    arqHash.seek(endBucket);
                    arqHash.writeInt(-1);
                    arqHash.writeLong(-1);
                    arredaBucket(endBucket+12, fimBucket);
                    endBucket = fimBucket;
                }
                endBucket += 12;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n" + e.getLocalizedMessage());
        }
        return endData;
    }

    public void arredaBucket(long endBucket, long fimBucket){
        //endBucket é o endereço a partir do qual os registros serão movidos 12 bytes para a esquerda até o fim do bucket
        try {
            long copiaEnd = endBucket;
            ArrayList<Integer> ids = new ArrayList<>();
            ArrayList<Long> ends = new ArrayList<>();
            arqHash.seek(endBucket);
            while(endBucket < fimBucket && arqHash.readByte() != -1){
                arqHash.seek(endBucket);
                ids.add(arqHash.readInt());
                ends.add(arqHash.readLong());
                endBucket += 12;
            }
            arqHash.seek(copiaEnd-12);
            for(int i=0; i<ids.size(); i++){
                arqHash.writeInt(ids.get(i));
                arqHash.writeLong(ends.get(i));
            }
            arqHash.writeInt(-1);
            arqHash.writeLong(-1);
            
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n" + e.getLocalizedMessage());
        }
    }
}