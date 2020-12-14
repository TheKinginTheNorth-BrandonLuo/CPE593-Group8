import com.google.common.base.Splitter;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;


import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Winnowing {

    /** Substring matching is at least as long as the noise threshold before it can be detected (used for filtering)*/
    private final int minDetectedLength;
    /** The size of the sliding window */
    private int windowSize;

    static public String readFile(File file) {

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            // Write the content read each time to the memory, and then get it from the memory
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            // As long as you don’t finish reading, keep reading
            while ((len = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            // Get all the data written in the memory
            byte[] data = outputStream.toByteArray();
            fileInputStream.close();
            return new String(data);
            //return new String(data, "GBK")
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Initialization parameters, sliding window size = minDetectedLength - noiseThreshold + 1
     *
     * @param minDetectedLength The shortest length of the substring that can be monitored
     * @param noiseThreshold Noise threshold, do not detect matches smaller than this value
     */
    public Winnowing(int minDetectedLength, int noiseThreshold) {
        this.minDetectedLength = minDetectedLength;
        if (noiseThreshold > minDetectedLength) {
            throw new IllegalArgumentException("The noise threshold cannot be greater than the minimum matching guarantee threshold!");
        }
        this.windowSize = minDetectedLength - noiseThreshold + 1;
    }
    /** use Winnowing(8, 4) to initialize */
    public Winnowing() {
        this(8, 4);
    }

    /** ----Calculate the digital fingerprint of N-Grams composed of words separated by spaces---- */
    public Set<Integer> winnowUsingWords(String text) {
        List<Integer> nh = getHashesForNGramsOfWords(text, " ");
        return buildFingerprintSet(nh);
    }
    // First mark the given text with the given delimiter to get the word list.
     // Then calculate the hash value of each N-Grams/shingle composed of words, store it in a list and return
    private List<Integer> getHashesForNGramsOfWords(String text, String delimiter) {
        //Divide the text based on the delimiter and remove spaces in the result (trimResults method) and empty strings (omitEmptyStrings method)
        Iterator<String> tok = Splitter.on(delimiter).trimResults()
                .omitEmptyStrings().split(text).iterator();
        List<Integer> n_grams = new ArrayList<>();
        List<String> list = new ArrayList<>();
        while (tok.hasNext()) {
            list.add(tok.next());
            if (list.size() == this.minDetectedLength) {
                n_grams.add(getHash(String.join(" ", list)));
                list.remove(0);
            }
        }
        /* when tokens is shorter than minDetectedLength */
        if (n_grams.isEmpty() && list.size() > 0) {
            n_grams.add(getHash(String.join(" ", list)));
        }
        return n_grams;
    }

    /** ----Calculate the digital fingerprint of N-Grams composed of characters. Preprocessing: so the letters are changed to lowercase and spaces are removed---- */
    public Set<Integer> winnowUsingCharacters(String text) {
        text = pretreatment(text);//预处理
        System.out.println("预处理后："+text);
        List<Integer> nh = getHashesForNGramsOfChars(text);
        return buildFingerprintSet(nh);
    }
    //Pretreatment
    private String pretreatment(String text) {
        String textWithoutPunctuation = text.replaceAll( "[\\pP+~$`^=|<>～｀＄＾＋＝｜＜＞￥×]" , "");//去除标点符号
        return textWithoutPunctuation.replaceAll("\\s+","").toLowerCase();// Remove blank characters and replace uppercase letters with lowercase letters
    }
    // Calculate the hash value of each N-Grams (composed of characters in the input text), and the size of each N-Grams is minDetectedLength
    private List<Integer> getHashesForNGramsOfChars(String text) {
        List<Integer> hashes = new ArrayList<>();
        if (text.length() < this.minDetectedLength) {
            int h = getHash(text);
            hashes.add(h);
        } else {
            for (int i=0;i<text.length() - this.minDetectedLength + 1; i++) {

                hashes.add(getHash(text.substring(i, i+this.minDetectedLength)));
            }
        }
        return hashes;
    }

    /** MD5 hash function (can be replaced by other hash functions)*/
    @SuppressWarnings("UnstableApiUsage")
    private int getHash(String token) {
        Hasher hasher = Hashing.md5().newHasher();
        hasher.putString(token, Charset.defaultCharset());
        int h = hasher.hash().asInt();
        return Math.abs(h%10000);//Returns the absolute value of the hash value after the remainder of 10000 (mod 10000)
    }

    private Set<Integer> buildFingerprintSet(List<Integer> nHash){
        Set<Integer> fp = new TreeSet<>();
        for (int i=0; i<nHash.size()-this.windowSize+1; i++) {
            List<Integer> s = new ArrayList<>(nHash.subList(i, i+this.windowSize));
            fp.add(Collections.min(s));
        }
        return fp;
    }

    /** Returns the currently used winnowing parameter value (minDetectedLength, windowSize)*/
    public HashMap getParams() {
        HashMap<String,Integer> params = new HashMap<>();
        params.put("minDetectedLength", this.minDetectedLength);
        params.put("windowSize", this.windowSize);
        return params;
    }
    public static void main(String[] args){
        Winnowing winnow = new Winnowing(10, 6);
        System.out.println("Test 1: Copy Detection for very short text:");

        double startTime = System.nanoTime();

        String dataSet1 = readLargeFile("example1.txt");
        String dataSet2 = readLargeFile("example2.txt");
        Set<Integer> set1 = winnow.winnowUsingCharacters(dataSet1);
        Set<Integer> set2 = winnow.winnowUsingCharacters(dataSet2);
        System.out.println(set1);
        System.out.println(set2);
        String similar_percent_1 = winnow.JaccardSimilarityComparison(set1, set2);
        String similar_percent_2 = winnow.CosineSimilarityComparison(set1, set2);

        double endTime = System.nanoTime();

        double time = (endTime - startTime) / 1000000000.0;
        System.out.println("Jaccard Similarity Comparision: " + similar_percent_1 + "%");
        System.out.println("Cosine Similarity Comparision: " + similar_percent_2 + "%");
        System.out.println("Total Execution Time: " + time + " s");

        System.out.println("##################################################################");

        System.out.println("Test 2: Copy Detection for long text:");
        startTime = System.nanoTime();

        String dataSet3 = readLargeFile("example3.txt");
        String dataSet4 = readLargeFile("example4.txt");

        Set<Integer> set3 = winnow.winnowUsingCharacters(dataSet3);
        Set<Integer> set4 = winnow.winnowUsingCharacters(dataSet4);
        System.out.println(set3);
        System.out.println(set4);
        String similar_percent_3 = winnow.JaccardSimilarityComparison(set3, set4);
        String similar_percent_4 = winnow.CosineSimilarityComparison(set3, set4);

        endTime = System.nanoTime();

        time = (endTime - startTime) / 1000000000.0;
        System.out.println("Jaccard Similarity Comparision: " + similar_percent_3 + "%");
        System.out.println("Cosine Similarity Comparision: " + similar_percent_4 + "%");
        System.out.println("Total Execution Time: " + time + " s");

        System.out.println("##################################################################");

        System.out.println("Test 3: Copy Detection for a whole book:");
        startTime = System.nanoTime();

        String dataSet5 = readLargeFile("text.txt");
        String dataSet6 = readLargeFile("text2.txt");

        Set<Integer> set5 = winnow.winnowUsingCharacters(dataSet5);
        Set<Integer> set6 = winnow.winnowUsingCharacters(dataSet6);
        System.out.println(set5);
        System.out.println(set6);
        String similar_percent_5 = winnow.JaccardSimilarityComparison(set5, set6);
        String similar_percent_6 = winnow.CosineSimilarityComparison(set5, set6);

        endTime = System.nanoTime();

        time = (endTime - startTime) / 1000000000.0;
        System.out.println("Jaccard Similarity Comparision: " + similar_percent_5 + "%");
        System.out.println("Cosine Similarity Comparision: " + similar_percent_6 + "%");
        System.out.println("Total Execution Time: " + time + " s");
    }
}
