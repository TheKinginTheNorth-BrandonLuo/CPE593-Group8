package clouddataprocess;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Text similarity calculation
 * Judgment method：Jaccard similarity coefficient, Evaluate their similarity by calculating the size of the intersection of the two sets divided by the size of the union
 * Algorithm description:
 * 1、Word separation
 * 2、Extract the main part of the word
 * 3.Delete unqualified words
 * 4、Count the number of unique words in the intersection
 * 5、Count the number of unique words in the union
 * 6、Divide the value in 2 by the value in 3 (intersectionSize/(double)unionSize)
 */
public class JaccardTextSimilarity {
    protected static final Logger LOGGER = LoggerFactory.getLogger(JaccardTextSimilarity.class);
    public static double getSimilarity(String document1, String document2) {
        //Get the text corresponding to the stem and encapsulate it into a collection
        List<String> wordslist1 = getlema(document1);
        List<String> wordslist2 = getlema(document2);
        Set<String> words2Set = new HashSet<>();
        words2Set.addAll(wordslist2);
        //Find intersection
        Set<String> intersectionSet = new ConcurrentSkipListSet<>();
        wordslist1.parallelStream().forEach(word -> {
            if (words2Set.contains(word)) {
                intersectionSet.add(word);
            }
        });
        //
        int intersectionSize = intersectionSet.size();
        //Size of intersection
        Set<String> unionSet = new HashSet<>();
        wordslist1.forEach(word -> unionSet.add(word));
        wordslist2.forEach(word -> unionSet.add(word));
        // size of Union 
        int unionSize = unionSet.size();
        //Similarity
        double score = intersectionSize / (double) unionSize;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Size of intersection：" + intersectionSize);
            LOGGER.debug("size of Union ：" + unionSize);
            LOGGER.debug("Similarity=" + intersectionSize + "/(double)" + unionSize + "=" + score);
        }
        return score;
    }
    public static List<String> getlema(String text){
        //The set of words corresponding to the stem
        List<String> wordslist = new ArrayList<>();;
        //StanfordCoreNLP
        Properties props = new Properties();  // set up pipeline properties
        props.put("annotators", "tokenize, ssplit, pos, lemma");   
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation document = new Annotation(text);
        pipeline.annotate(document);
        List<CoreMap> words = document.get(CoreAnnotations.SentencesAnnotation.class);
        for(CoreMap word_temp: words) {
            for (CoreLabel token: word_temp.get(CoreAnnotations.TokensAnnotation.class)) {
                String lema = token.get(CoreAnnotations.LemmaAnnotation.class); 
                wordslist.add(lema);
//                System.out.println(lema);
            }
        }
        return wordslist;
    }
    public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException {
//      BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( new File("")),"gbk"));

        String text1 = "Gridspot can link up idle computers instances across the world to provide large scale efforts with the computing power they require at affordable prices 0103 centsCPU hour These Linux instances run Ubuntu inside a virtual machine You are able to bid on access to these instances and specify the requirements of your tasks or jobs When your bid is fulfilled you can start running the instances using SSH anywhere youd like There are grant options available to defray costs for researchers and nonprofits The Gridspot API allows you to manage instances and identify new ones You can list available instances access them and stop the instances if you so choose Each API call requires an API key that can be generated from your account page";
        String text2 = "Chapoo is a cloudbased platform for collaboration and project information management The service allows project managers designers engineers and other contributors to improve productivity through better collaboration communication and coordination Chapoo offers a fullfeatured RESTful API for live queries and reports Example functions include getting items contained in a binder getting the contact image of a contact getting a list of forms from a folder and creating new folders Results are returned in JSON format";
        System.out.println(getSimilarity(text1,text2));
        System.out.println(getSimilarity(text1,text1));
    }
}