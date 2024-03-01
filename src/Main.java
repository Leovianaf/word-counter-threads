import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    private static int totalWords = 0; // Variável que guarda o total de palavras de todos os arquivos
    private static final Lock lock = new ReentrantLock(); // Criação de lock para acesso a totalWords

    public static void main(String[] args) throws InterruptedException, ExecutionException {

        Scanner scanner = new Scanner(System.in);

        // Solicita ao usuário o caminho da pasta
        System.out.println("Insira o caminho da pasta: ");
        String folderPath = scanner.nextLine();

        // Solicita ao usuário o número de threads
        System.out.println("Insira o número de threads: ");
        int maxThreads = scanner.nextInt();

        scanner.close();

        File folder = new File(folderPath); // Cria um objeto File com o caminho da pasta
        File[] files = folder.listFiles(File::isFile); // Lista os arquivos da pasta

        long[] executionTimes = new long[maxThreads]; // Vetor para armazenar os tempos de execução

        for(int numThreads = 1; numThreads <= maxThreads; numThreads++) {
            // Cria um Executor com o número de threads informado
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);

            try {
                // Cria uma lista que servirá para dar call() em todos os arquivos de files
                List<Callable<Integer>> listaCalls = new ArrayList<>();

                if (files != null) {
                    for (File file : files) { // cria lista com N calls, cada call para um arquivo
                        listaCalls.add(new WordCounter(file));
                    }
                }

                // Variával que guarda o tempo inicial da execução com n threads
                long startTime = System.currentTimeMillis();

                // Chamando todas as calls para executarem de uma só vez
                List<Future<Integer>> results = executor.invokeAll(listaCalls);

                // Imprimir os resultados das contagens de cada arquivo para acompanharmos
                // Printa ao final da execução das threads
                System.out.println();
                for (int i = 0; i < results.size(); i++) {
                    System.out.println("Palavras do arquivo " + (i + 1) + ": " + results.get(i).get());
                }
                System.out.println();

                executor.shutdown(); // Encerra o executor

                // Variával que guarda o tempo final da execução com n threads
                long endTime = System.currentTimeMillis();

                // Calcula o tempo decorrido e armazena no vetor
                executionTimes[numThreads - 1] = endTime - startTime;


            } finally {
                executor.shutdownNow(); // Para assegurar que o executor vai ser encerrado
            }
        }

        // Imprimir número total de palavras
        System.out.println("\nTotal de palavras de todos os arquivos: " + totalWords + "\n");

        // Imprime o tempo decorrido para cada quantidade de threads
        for(int n = 1; n <= maxThreads; n++){
            System.out.println("Tempo decorrido com " + n + " threads: " + executionTimes[n - 1] + " milissegundos\n");
        }
    }

    public static void updateTotalWords(int count) {
        lock.lock(); // A partir daqui só passa uma thread por vez
        try {
            totalWords += count; // Atualiza o total de palavras
            System.out.println(Thread.currentThread());
        } finally {
            lock.unlock(); // Libera a trava para que outra thread possa acessar
        }
    }

    // Classe interna que implementa a interface Callable para contar as palavras dos arquivos
    private static class WordCounter implements Callable<Integer> {
        private final File file;

        public WordCounter(File file) {
            this.file = file;
        }

        @Override
        public Integer call() throws Exception {
            int count = countWordsFromFile(file);
            updateTotalWords(count);
            return count;
        }

        private int countWordsFromFile(File file) {
            Scanner sc = null;
            int wordCount = 0;

            try {
                sc = new Scanner(file); // sc recebe o arquivo
                while (sc.hasNext()) { // Loop vai continuar rodando enquanto houverem palavras no txt
                    sc.next(); // Palavra atual é "consumida"
                    wordCount++; // Variável de iteração da quantidade
                    // São considerados separadores de palavras espaço e quebra de linha
                }
            } catch (IOException e) { // Exceção para se por exemplo o arquivo tiver acesso negado, etc
                System.out.println("Erro: " + e.getMessage());
            } finally {
                if (sc != null) {
                    sc.close();
                }
            }

            return wordCount;
        }
    }
}