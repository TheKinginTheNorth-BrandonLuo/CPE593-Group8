public static List<String> ngrams(int n, String sentence) {
    List<String> ngrams = new ArrayList<>();
    String[] words = sentence.split("\\s+");
    for (int i = 0; i < words.length - n + 1; i++)
        ngrams.add(concat(words, i, i + n));
    return ngrams;
    }

public static String concat(String[] words, int start, int end) {
    StringBuilder sb = new StringBuilder();
    for (int i = start; i < end; i++)
        sb.append(i > start ? " " : "").append(words[i]);
    return sb.toString();
    }

public static HashMap<String, Integer> sortByValue(HashMap<String, Integer> unSortedMap) {
        // System.out.println("Unsorted Map : " + unSortedMap);

        // LinkedHashMap保留插入元素的顺序
    LinkedHashMap<String, Integer> reverseSortedMap = new LinkedHashMap<>();

        // 使用 Comparator.reverseOrder() 进行反向排序
    unSortedMap.entrySet()
            .stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .forEachOrdered(x -> reverseSortedMap.put(x.getKey(), x.getValue()));

        // System.out.println("Reverse Sorted Map   : " + reverseSortedMap);

    return reverseSortedMap;
    }

public static void main(String[] args) {
    HashMap<String, Integer> count = new HashMap<>();
    String text = "This is just a simple test to test how n gram worked.";

        // 生成n为1~3的N元语法
//        for (int n = 1; n <= 3; n++) {
//            for (String ngram : ngrams(n, text)) {
//                System.out.println(ngram);
//            }
//            System.out.println();
//        }

    for (String ngram : ngrams(3, text)) {
        // counting ngram by using HashMap
        if (!count.containsKey(ngram)) {
            count.put(ngram, 1);
        } else if (count.containsKey(ngram)) {
            count.replace(ngram, count.get(ngram) + 1);
        }
        System.out.println(ngram);
    }

        // 按出现次由多到少的顺序打印ngram
    System.out.println("\nCounting Result: ");
    for (Map.Entry<String, Integer> entry : sortByValue(count).entrySet()) {
        System.out.println(entry.getKey() + ": " + entry.getValue());
    }

}
