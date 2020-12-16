import com.google.common.base.Splitter;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import java.io.*;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.*;

/***
 * CPE-593 Data Structure and Algorithm 
 * Final Project Code for WinnowingAlgorithm 
 * Yinghao Wang 10455443 
 * Shuai Hao 10432811 
 * Fan Luo 10442682 
 * Dec 13,2020
 */

public class Winnowing {

    /**
     * Substring matches are at least as long as the window threshold before they
     * can be detected）
     */
    private final int minDetectedLength;
    /** The size of the sliding window */
    private int slidingWindowSize;
    /** K-gram value */
    private int kGramDetectedLength;

    /**
     * read file part using file input stream
     */

    static public String readLargeFile(String filename) {
        if (filename.length() == 0)
            return "";
        try {
            File file = new File(filename);
            FileInputStream fileInputStream = new FileInputStream(file);
            // Write each read content to the memory, and then get it from the memory
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int buf = 0;
            // As long as you don’t finish reading, keep reading
            while ((buf = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, buf);
            }
            // Get all the data written in the memory
            byte[] resultData = outputStream.toByteArray();
            fileInputStream.close();
            return new String(resultData);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Initialization parameters, sliding window size = minDetectedLength -
     * kGramDetectedLength + 1
     *
     * @param minDetectedLength   The shortest length of the substring that can be
     *                            monitored
     * @param kGramDetectedLength K-gram value
     */
    public Winnowing(int minDetectedLength, int kGramDetectedLength) {
        this.minDetectedLength = minDetectedLength;
        if (kGramDetectedLength > minDetectedLength) {
            throw new IllegalArgumentException(
                    "The sliding window threshold cannot be greater than the minimum matching guarantee threshold!");
        }
        this.slidingWindowSize = minDetectedLength - kGramDetectedLength + 1;/** w = N - K +1 */
        this.kGramDetectedLength = kGramDetectedLength;
    }

    /** Winnowing(8, 4) */
    public Winnowing() {
        this(8, 4);
    }

    /**
     * ---- Calculate the digital fingerprint of N-Grams composed of words separated
     * by spaces ----
     */
    public Set<Integer> winnowUsingWords(String text) {
        List<Integer> nh = getHashesForNGramsOfWords(text, " ");
        return buildFingerprintSet(nh);
    }

    // First mark the given text with the given delimiter to get the word list.
    // Then calculate the hash value of each N-Grams/shingle composed of words,
    // store it in a list and return
    private List<Integer> getHashesForNGramsOfWords(String text, String delimiter) {
        // Divide the text based on the delimiter and remove spaces in the result
        // (trimResults method) and empty strings (omitEmptyStrings method)
        Iterator<String> tok = Splitter.on(delimiter).trimResults().omitEmptyStrings().split(text).iterator();
        List<Integer> n_grams = new ArrayList<>();
        List<String> list = new ArrayList<>();
        while (tok.hasNext()) {
            list.add(tok.next());
            if (list.size() == this.minDetectedLength) {
                n_grams.add(getHash(String.join(" ", list)));
                list.remove(0);
            }
        }
        /* when tokens are shorter than minDetectedLength */
        if (n_grams.isEmpty() && list.size() > 0) {
            n_grams.add(getHash(String.join(" ", list)));
        }
        return n_grams;
    }

    /**
     * ---- Calculate the digital fingerprint of N-Grams composed of characters.
     * Preprocessing: the letters become lowercase and spaces are removed ----
     */
    public Set<Integer> winnowUsingCharacters(String text) {
        text = preTreatInputText(text);
        List<Integer> nh = getHashesForNGramsOfChars(text);
        return buildFingerprintSet(nh);
    }

    // pretreatment
    private String preTreatInputText(String text) {
        String regex = "[\\pP+~$`^=|<>～｀＄＾＋＝｜＜＞￥×]";
        String textWithoutPunctuation = text.replaceAll(regex, "Remove punctuation");// Remove punctuation
        return textWithoutPunctuation.replaceAll("\\s+", "").toLowerCase();// Remove blank characters and replace
                                                                           // uppercase letters with lowercase letters
    }

    // compute k-Grams（
    private List<Integer> getHashesForNGramsOfChars(String text) {
        List<Integer> hashes = new ArrayList<>();
        if (text.length() < this.kGramDetectedLength) {// N = 8, k = 4, W = 8 - 4 + 1 this.minDetectedLength should be
                                                       // modify to this.kGramDetectedLength
            int h = getHash(text);
            hashes.add(h);
        } else {
            for (int i = 0; i < text.length() - this.kGramDetectedLength + 1; i++) {
                String slidingText = text.substring(i, i + this.kGramDetectedLength);
                hashes.add(getHash(slidingText));
            }
        }
        return hashes;
    }

    /** MD5 hash function */
    private int getHash(String str) {
        Hasher hash = Hashing.md5().newHasher();
        hash.putString(str, Charset.defaultCharset());
        int h = hash.hash().asInt();
        return Math.abs(h % 10000000);// Returns the absolute value of the hash value after the remainder of 10000000
                                      // (mod 10000000)
    }

    /** CRC32 hash function */
    // private int getHash_2(String str){
    // Hasher hash = Hashing.crc32().newHasher();
    // hash.putString(str, Charset.defaultCharset());
    // int h = hash.hash().asInt();
    // return Math.abs(h % 10000000);
    // }

    // /** sha256 */
    // private int getHash_3(String str){
    // Hasher hash = Hashing.sha256().newHasher();
    // hash.putString(str, Charset.defaultCharset());
    // int h = hash.hash().asInt();
    // return Math.abs(h % 10000000);
    // }

    /**
     * H(c1...ck) = c1*b^(k - 1) + c2*b^(k - 2) + ... + c(k - 1)*b + ck our
     * self-defined hash algorithm
     */
    private int getHashedByOurOwn(String str) {
        long hash = 0;
        // this is our self-defined base value
        int base = 19960906;
        // k should be equal to kGramDetectedLength
        int k = 4;
        for (int i = 0; i < str.length(); i++) {
            long temp = (base ^ (k - i - 1)) % 10000000;
            hash += str.charAt(i) * temp;
        }
        return (int) (hash % 10000000);
    }

    // get min value in each window to build a fingerprint set
    private Set<Integer> buildFingerprintSet(List<Integer> hashedValueList) {
        if (hashedValueList.size() == 0)
            return new HashSet<>();
        Set<Integer> fingerprint = new TreeSet<>();
        for (int i = 0; i < hashedValueList.size() - this.slidingWindowSize + 1; i++) {
            List<Integer> temp = new ArrayList<>(hashedValueList.subList(i, i + this.slidingWindowSize));
            fingerprint.add(Collections.min(temp));
        }
        return fingerprint;
    }

    /**
     * Returns the currently used winnowing parameter value (minDetectedLength,
     * windowSize)
     */
    public HashMap getAllParameters() {
        HashMap<String, Integer> allvalue = new HashMap<>();
        allvalue.put("minDetectedLength", this.minDetectedLength);
        allvalue.put("slidingWindowSize", this.slidingWindowSize);
        allvalue.put("kGramDetectedLength", this.kGramDetectedLength);
        return allvalue;
    }

    /** Jaccard Similarity Algorithm */
    public String JaccardSimilarityComparison(Set<Integer> set1, Set<Integer> set2) {
        int leftLen = set1.size();
        int rightLen = set2.size();
        int count = 0;
        for (Integer left : set1) {
            for (Integer right : set2) {
                if (left.equals(right)) {
                    count++;
                }
            }
        }
        DecimalFormat df = new DecimalFormat(".000");
        double res = (count * 1.0) / (leftLen + rightLen - count) * 100;
        return df.format(res);
    }

    /**
     * improved Jaccard Similarity Algorithm
     */
    public String improvedJaccardSimilarityComparision(Set<Integer> set1, Set<Integer> set2) {
        int p = 1;
        int leftLen = set1.size();
        int rightLen = set2.size();
        int count = 0;
        for (Integer left : set1) {
            for (Integer right : set2) {
                if (left.equals(right)) {
                    count++;
                }
            }
        }
        int leftUnionRight = leftLen + rightLen - count;
        int lengthIndex = Math.abs(leftLen - rightLen);
        DecimalFormat df = new DecimalFormat(".000");
        double res = (count * 1.0) / (leftUnionRight + p * lengthIndex) * 100;
        return df.format(res);
    }

    /** Cosine Similarity Comparision Algorithm */
    public String CosineSimilarityComparison(Set<Integer> set1, Set<Integer> set2) {
        ArrayList<Integer> list1 = new ArrayList<>();
        ArrayList<Integer> list2 = new ArrayList<>();
        list1.addAll(set1);
        list2.addAll(set2);

        int leftLen = list1.size();
        int rightLen = list2.size();
        int maxLen = Math.max(leftLen, rightLen);
        int minLen = Math.min(leftLen, rightLen);

        long mod = 1000000000;
        long up_mul = 0;
        long deLeft = 0;
        long deRight = 0;

        for (int i = 0; i < maxLen; i++) {
            long temp = i < minLen ? list1.get(i) * list2.get(i) : 0;
            up_mul = (up_mul + temp) % mod;
            long leftMul = (list1.size() - 1) >= i ? (long) Math.pow(list1.get(i), 2) : 0;
            long rightMul = (list2.size() - 1) >= i ? (long) Math.pow(list2.get(i), 2) : 0;
            deLeft = (deLeft + leftMul) % mod;
            deRight = (deRight + rightMul) % mod;
        }
        // round to 3 digits
        long den = (long) (Math.sqrt(deLeft) * Math.sqrt(deRight)) % mod;
        DecimalFormat df = new DecimalFormat(".000");
        double res = (up_mul * 1.0 / den) * 100;
        return df.format(res);

    }

    /**
     * compute whole time
     */
    public static void main(String[] args) {
        /**
         * note: according the result, cosine similarity comparison is not suit for
         * making comparison in our algorithm we will not use it in the test part, we
         * only use jaccard algorithm
         */
        Winnowing winnow = new Winnowing(10, 4);
        System.out.println("Test 1: Copy Detection for very short text:");

        double startTime = System.nanoTime();

        String dataSet1 = readLargeFile("example1.txt");
        String dataSet2 = readLargeFile("example2.txt");
        Set<Integer> set1 = winnow.winnowUsingCharacters(dataSet1);
        Set<Integer> set2 = winnow.winnowUsingCharacters(dataSet2);
        System.out.println(set1);
        System.out.println(set2);
        String similar_percent_1 = winnow.JaccardSimilarityComparison(set1, set2);
        // String similar_percent_2 = winnow.CosineSimilarityComparison(set1, set2);

        double endTime = System.nanoTime();

        double time = (endTime - startTime) / 1000000000.0;
        System.out.println("Jaccard Similarity Comparision: " + similar_percent_1 + "%");
        // System.out.println("Cosine Similarity Comparision: " + similar_percent_2 +
        // "%");
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
        // String similar_percent_4 = winnow.CosineSimilarityComparison(set3, set4);

        endTime = System.nanoTime();

        time = (endTime - startTime) / 1000000000.0;
        System.out.println("Jaccard Similarity Comparision: " + similar_percent_3 + "%");
        // System.out.println("Cosine Similarity Comparision: " + similar_percent_4 +
        // "%");
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
        // String similar_percent_6 = winnow.CosineSimilarityComparison(set5, set6);

        endTime = System.nanoTime();

        time = (endTime - startTime) / 1000000000.0;
        System.out.println("Jaccard Similarity Comparision: " + similar_percent_5 + "%");
        // System.out.println("Cosine Similarity Comparision: " + similar_percent_6 +
        // "%");
        System.out.println("Total Execution Time: " + time + " s");
    }
}
