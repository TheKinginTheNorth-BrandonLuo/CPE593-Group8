package in.jwinnow;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class Fingerprint {
    
    private final int minDetectedLength;
    
    private int windowSize;

    public Fingerprint(int minDetectedLength, int noiseThreshold) {
        this.minDetectedLength = minDetectedLength;
        if (noiseThreshold > minDetectedLength) {
            throw new IllegalArgumentException("Noise threshold, k, "
                    + "should not be greater than minimum match "
                    + "guarantee threshold, t.");
        }
        this.windowSize = minDetectedLength - noiseThreshold + 1;
    }

    public Fingerprint() {
        this(8, 4);
    }
    
    private List<Integer> getHashesForNGramsOfChars(String text) {

        List<Integer> hashes = new ArrayList<>();
        if (text.length() < this.minDetectedLength) {
            int h = getHash(text);
            hashes.add(h);
        } else {
            for (int i=0;i<text.length() - this.minDetectedLength + 1; i++) {
                hashes.add(
                    getHash(
                        text.substring(i, i+this.minDetectedLength)
                    ));
            }
        }
        return hashes;
    }
    
    private List<Integer> getHashesForNGramsOfWords(String text, String delim) {

        Iterator<String> tok = Splitter.on(delim).trimResults()
                .omitEmptyStrings().split(text).iterator();

        List<Integer> ngrams = new ArrayList<>();
        List<String> list = new ArrayList<>();
        while (tok.hasNext()) {
            list.add(tok.next());
            if (list.size() == this.minDetectedLength) {
                ngrams.add(getHash(String.join(" ", list)));
                list.remove(0);
            }
        }
        if (ngrams.isEmpty() && list.size() > 0) {
            ngrams.add(getHash(String.join(" ", list)));
        }
        return ngrams;
    }

    protected int getHash(String token) {
        Hasher hasher = Hashing.md5().newHasher();
        hasher.putString(token, Charset.defaultCharset());
        int h = hasher.hash().asInt();
        return Math.abs(h%10000);
    }
    
    public Set<Integer> winnowUsingWords(String text) {
        List<Integer> nh = getHashesForNGramsOfWords(text, " ");
        Set<Integer> fp = new TreeSet();
        for (int i=0; i<nh.size()-this.windowSize+1; i++) {
            List<Integer> s = new ArrayList(nh.subList(i, i+this.windowSize));
            fp.add(Collections.min(s));
        }
        return fp;
    }
    
    public Set<Integer> winnowUsingCharacters(String text) {
        text = removeWhiteSpaceAndMakeLowercase(text);
        List<Integer> nh = getHashesForNGramsOfChars(text);
        Set<Integer> fp = new TreeSet();
        for (int i=0; i<nh.size()-this.windowSize+1; i++) {
            List<Integer> s = new ArrayList(nh.subList(i, i+this.windowSize));
            fp.add(Collections.min(s));
        }
        return fp;
    }
    
    protected String removeWhiteSpaceAndMakeLowercase(String text) {
        return text.replaceAll("\\s+","").toLowerCase();
    }
    
    public HashMap getParams() {
        HashMap p = new HashMap();
        p.put("minDetectedLength", this.minDetectedLength);
        p.put("windowSize", this.windowSize);
        return p;
    }
    public static void main(String[] args) {
        Fingerprint fh = new Fingerprint();
        Set<String> animals = ImmutableSet.of("duck", "monkey");
        Set<String> fruits = ImmutableSet.of("apple", "orange", "banana");        
        Set<List<String>> product = Sets.cartesianProduct(animals, fruits);
        
        String str = product.stream().map(e->e.get(0)+" and "+e.get(1)).
                collect(Collectors.toList()).toString();
        str = str.substring(1, str.length()-1);
        System.out.println("Winnowing params: "+fh.getParams());
        System.out.println("Input string: \""+str+"\"");
        System.out.println("Fingerprint: "+fh.winnowUsingCharacters(str));
    }
}
