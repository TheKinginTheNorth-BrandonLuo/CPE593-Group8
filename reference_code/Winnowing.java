package com.single.common.utils.Fingerprint;

import com.google.common.base.Splitter;
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

public class Winnowing {

    private final int minDetectedLength;
    private int windowSize;

    public Winnowing(int minDetectedLength, int noiseThreshold) {
        this.minDetectedLength = minDetectedLength;
        if (noiseThreshold > minDetectedLength) {
            throw new IllegalArgumentException("噪声阈值不能大于最小匹配保证阈值！");
        }
        this.windowSize = minDetectedLength - noiseThreshold + 1;
    }
    public Winnowing() {
        this(8, 4);
    }

    public Set<Integer> winnowUsingWords(String text) {
        List<Integer> nh = getHashesForNGramsOfWords(text, " ");
        return buildFingerprintSet(nh);
    }
    private List<Integer> getHashesForNGramsOfWords(String text, String delimiter) {
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
        /* 当tokens比minDetectedLength短 */
        if (n_grams.isEmpty() && list.size() > 0) {
            n_grams.add(getHash(String.join(" ", list)));
        }
        return n_grams;
    }

    public Set<Integer> winnowUsingCharacters(String text) {
        text = pretreatment(text);//预处理
        System.out.println("预处理后："+text);
        List<Integer> nh = getHashesForNGramsOfChars(text);
        return buildFingerprintSet(nh);
    }
    private String pretreatment(String text) {
        String textWithoutPunctuation = text.replaceAll( "[\\pP+~$`^=|<>～｀＄＾＋＝｜＜＞￥×]" , "");
        return textWithoutPunctuation.replaceAll("\\s+","").toLowerCase();
    }
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

    private int getHash(String token) {
        Hasher hasher = Hashing.md5().newHasher();
        hasher.putString(token, Charset.defaultCharset());
        int h = hasher.hash().asInt();
        return Math.abs(h%10000);
    }

    private Set<Integer> buildFingerprintSet(List<Integer> nHash){
        Set<Integer> fp = new TreeSet<>();
        for (int i=0; i<nHash.size()-this.windowSize+1; i++) {
            List<Integer> s = new ArrayList<>(nHash.subList(i, i+this.windowSize));
            fp.add(Collections.min(s));
        }
        return fp;
    }

    public HashMap getParams() {
        HashMap<String,Integer> params = new HashMap<>();
        params.put("minDetectedLength", this.minDetectedLength);
        params.put("windowSize", this.windowSize);
        return params;
    }
}
