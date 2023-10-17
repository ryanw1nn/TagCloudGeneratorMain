import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Takes in input text file, finds the 'x' most popular words and outputs them
 * into a HTML tag cloud.
 *
 * input file example: data/inputExample.txt
 *
 * output file example: data/outputExample.html
 *
 * @author Ryan Winn
 */
public final class TagCloudGenerator {

    /**
     * Default constructor--private to prevent instantiation.
     */
    private TagCloudGenerator() {
        // no code needed here
    }

    /**
     * characters for sepatators.
     */
    final static String separatorStr = " \t\n\r,-.!?[]';:/()*_0123456789~{}@$%&#";

    /**
     * compare and return the string in correct order and capital.
     */
    public static class alphabeticalComparator
            implements Comparator<HashMap.Entry<String, Integer>> {
        @Override
        public int compare(Entry<String, Integer> o1,
                Entry<String, Integer> o2) {
            return String.CASE_INSENSITIVE_ORDER.compare(
                    o1.getKey().toLowerCase(), o2.getKey().toLowerCase());
        }
    };

    /**
     * compare and return the count order from higher to lower.
     */
    public static class countComparator
            implements Comparator<HashMap.Entry<String, Integer>> {
        @Override
        public int compare(Entry<String, Integer> o1,
                Entry<String, Integer> o2) {
            return Integer.compare(o2.getValue(), o1.getValue());
        }
    };

    /**
     * Generates the set of characters in the given {@code String} into the
     * given {@code Set}.
     *
     * @param str
     *            the given {@code String}
     * @param charSet
     *            the {@code Set} to be replaced
     *
     * @replaces charSet
     * @ensures charSet = entries(str)
     */
    public static void generateElements(String str,
            HashSet<Character> charSet) {
        assert str != null : "Violation of: str is not null";
        assert charSet != null : "Violation of: charSet is not null";
        int length = str.length();
        for (int i = 0; i < length; i++) {
            if (!charSet.contains(str.charAt(i))) {
                charSet.add(str.charAt(i));
            }

        }

    }

    /**
     * Store all of the content in input file into the string.
     *
     * @param in
     * @return input file
     */
    public static String readFromFile(BufferedReader in) {

        String x = "";
        try {
            String nextLine = in.readLine();
            while (nextLine != null) {
                x = nextLine + " " + x;
                nextLine = in.readLine();
            }
        } catch (IOException e) {
            System.err.println("Error reading from file");
        }
        return x;
    }

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code separators}) or "separator string" (maximal length string of
     * characters in {@code separators}) in the given {@code text} starting at
     * the given {@code position}.
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @param separators
     * @return the first word or separator string found in {@code text} starting
     *         at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures <pre>
     * nextWordOrSeparator =
    * text[position, position + |nextWordOrSeparator|) and
     * if entries(text[position, position + 1)) intersection separators = {}
     * then
     * entries(nextWordOrSeparator) intersection separators = {} and
     * (position + |nextWordOrSeparator| = |text| or
     * entries(text[position, position + |nextWordOrSeparator| + 1))
     * intersection separators /= {})
     * else
     * entries(nextWordOrSeparator) is subset of separators and
    * (position + |nextWordOrSeparator| = |text| or
     * entries(text[position, position + |nextWordOrSeparator| + 1))
     * is not subset of separators)
     * </pre>
     */
    private static String nextWordOrSeparator(String text, int position,
            HashSet<Character> separators) {
        assert text != null : "Violation of: text is not null";
        assert separators != null : "Violation of: separators is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        int count = position;
        String result = "";
        if (!separators.contains(text.charAt(position))) {
            while (count < text.length()
                    && !separators.contains(text.charAt(count))) {
                result = result + text.charAt(count);
                count++;

            }
        } else {
            while (count < text.length()
                    && separators.contains(text.charAt(count))) {

                result = result + text.charAt(count);
                count++;

            }

        }
        result = result.toLowerCase();
        return result;
    }

    /**
     * Calculate the wordFontSize given the each sorted count from high to low.
     *
     * @param maxCount
     * @param minCount
     * @param y
     * @return integer that represent the proportion of the font size given the
     *         count size.
     */
    public static int wordFontSize(int maxCount, int minCount, int y) {
        double x = (double) (y - minCount) / (maxCount - minCount);
        return ((int) (37 * x)) + 11;
    }

    /**
     * extract all of the word from the input file content by calling
     * nextwordorSeparator method and if the word matches the value +1 . if not
     * add word into new word and the value +1 and put all the non repeated word
     * into queue and sort the queue
     *
     * @param fileContent
     * @param containsAllWord
     * @param separators
     * @ensure sortList is sorted list wordCount will contain the correct key
     *         and wordCount as value
     *
     */
    public static void getMap(String fileContent,
            HashMap<String, Integer> containsAllWord,
            HashSet<Character> separators) {
        int position = 0;

        while (position < fileContent.length()) {

            String term = nextWordOrSeparator(fileContent, position,
                    separators);
            if (!separators.contains(term.charAt(0))) {
                if (containsAllWord.containsKey(term)) {
                    int value = containsAllWord.remove(term);
                    value++;
                    containsAllWord.put(term, value);
                    //wordCount.replaceValue(term, wordCount.value(term) + 1);
                } else {
                    containsAllWord.put(term, 1);
                }
            }
            position += term.length();
        }

    }

    /**
     * Generates map count from high to low.
     *
     * @param containsAllWord
     * @param nTag
     * @param valueComp
     * @return map count
     */
    public static PriorityQueue<Entry<String, Integer>> sortMapInCount(
            HashMap<String, Integer> containsAllWord, int nTag,
            Comparator<Entry<String, Integer>> valueComp) {

        //sorts the sortMap from count high to low for all.
        PriorityQueue<HashMap.Entry<String, Integer>> sortedValues = new PriorityQueue<>(
                new countComparator());
        sortedValues.addAll(containsAllWord.entrySet());
        PriorityQueue<Entry<String, Integer>> nTagValues = new PriorityQueue<>(
                new Comparator<Entry<String, Integer>>() {
                    @Override
                    public int compare(Entry<String, Integer> e1,
                            Entry<String, Integer> e2) {
                        return e1.getValue().compareTo(e2.getValue());
                    }
                });
        // create a map with only number of tags from count high to low.
        for (int i = 0; i < nTag; i++) {
            HashMap.Entry<String, Integer> notSortedYet = sortedValues.poll();

            nTagValues.add(notSortedYet);

        }

        return nTagValues;
    }

    /**
     * generate the html files.
     *
     * @param fileOut
     * @param inputFile
     * @param nTag
     * @param sortedMap
     * @param minCount
     * @param maxCount
     */
    public static void outputHtml(PrintWriter fileOut, String inputFile,
            int nTag, SortedMap<String, Integer> sortedMap, int minCount,
            int maxCount) {
        // output the html
        fileOut.println("<html>");
        fileOut.println("<head>");
        fileOut.println("<title>" + "Top " + nTag + " words in " + inputFile
                + "</title>");
        fileOut.println("<link href="
                + "\"http://web.cse.ohio-state.edu/software/2231/web-sw2/assignments/"
                + "projects/tag-cloud-generator/data/tagcloud.css\""
                + " rel=\"stylesheet\"" + " type=\"text/css\">");
        fileOut.println("<link href=\"tagcloud.css\"" + " rel=\"stylesheet\""
                + " type=\"text/css\">");
        fileOut.println(
                "<link href=\"tagcloud.css\" rel=\"stylesheet\" type=\"text/css\">");
        fileOut.println("<style type=\"text/css\"></style>");
        fileOut.println("</head>");
        fileOut.println("<body>");
        fileOut.println(
                "<h2>" + "Top " + nTag + " words in " + inputFile + "</h2>");
        fileOut.println("<hr>");
        fileOut.println("<div class=\"cdiv\">");
        fileOut.println("<p class=\"cbox\">");
        // output the correct font size of html.
        for (SortedMap.Entry<String, Integer> x : sortedMap.entrySet()) {
            int fontSize = wordFontSize(maxCount, minCount, x.getValue());
            fileOut.println("<span style=\"cursor:default\" class=\"f"
                    + fontSize + "\" title=\"count: " + x.getValue() + "\">"
                    + x.getKey() + "</span>");
        }

        fileOut.println("</p>");
        fileOut.println("</div>");
        fileOut.println("</body>");
        fileOut.println("</html>");
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments; unused here
     */
    @SuppressWarnings("resource")
    public static void main(String[] args) throws IOException {

        HashSet<Character> separatorSet = new HashSet<>();

        generateElements(separatorStr, separatorSet);
        HashMap<String, Integer> containsAllWord = new HashMap<>();
        SortedMap<String, Integer> wordCount = new TreeMap<String, Integer>();
        wordCount.comparator();
        // ask user for the input file
        System.out.println("What is the input file name?: ");
        String inputFile = "";
        Scanner in = new Scanner(System.in);
        BufferedReader input = null;
        String readFile = null;
        try {
            inputFile = in.nextLine();
            input = new BufferedReader(new FileReader(inputFile));
            readFile = readFromFile(input);

        } catch (IOException e) {
            System.err.println("Error opening file");
            in.close();
            return;
        }
        //get the output file

        System.out.println("What is the output file name?: ");
        PrintWriter output = null;
        String outputFile = null;
        try {
            outputFile = in.nextLine();
            output = new PrintWriter(
                    new BufferedWriter(new FileWriter(outputFile)));

        } catch (IOException e) {
            System.err.println("Error opening Writer");
            return;
        }
        //get number of words in tag cloud
        System.out
                .println("Enter the number of words to be in the tag cloud: ");
        // load all the input without separators from input file into the hash map.
        getMap(readFile, containsAllWord, separatorSet);
        // ask user for number of tags.
        int nTag = 0;
        try {
            String nTagString = in.nextLine();
            nTag = Integer.parseInt(nTagString);

        } catch (NumberFormatException e) {
            System.err.println("Error on reading number of words");

        }
        // make sure user enters the correct size the of the ntag.
        while (nTag > containsAllWord.size()) {
            System.out.println("enter the correct size of the tag cloud!");
            nTag = input.read();
        }

        try {
            output = new PrintWriter(
                    new BufferedWriter(new FileWriter(outputFile)));
        } catch (IOException e) {
            System.err.println("Error with output file");
            return;
        }

        Comparator<Entry<String, Integer>> valueComp = new countComparator();

        int minCount = Integer.MAX_VALUE;
        int maxCount = Integer.MIN_VALUE;
        // generate the map count from high to low based on the number of tags.
        PriorityQueue<Entry<String, Integer>> sortByCount = sortMapInCount(
                containsAllWord, nTag, valueComp);

        SortedMap<String, Integer> alphaSorted = new TreeMap<String, Integer>();
        // get the min and max word count from sortByCount map.
        while (sortByCount.size() != 0) {
            Entry<String, Integer> dequeued = sortByCount.poll();
            //System.out.println(dequeued.getValue());
            int temp = dequeued.getValue();

            //System.out.println(minCount);
            if (minCount > temp) {
                minCount = temp;
            }
            if (maxCount <= temp) {
                maxCount = temp;
            }
            alphaSorted.put(dequeued.getKey(), dequeued.getValue());
        }

        // Create a new ArrayList to store the entries in the PriorityQueue
        List<HashMap.Entry<String, Integer>> entryList = new ArrayList<>(
                alphaSorted.entrySet());

        // Sort the list using the alphabetical comparator
        Collections.sort(entryList, new alphabeticalComparator());

        // Create a new SortedMap to store the sorted entries
        SortedMap<String, Integer> sortedMap = new TreeMap<>();

        // Add the sorted entries from the list to the new SortedMap
        for (HashMap.Entry<String, Integer> entry : entryList) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        // output the file.
        outputHtml(output, inputFile, nTag, sortedMap, minCount, maxCount);

        try {
            input.close();
            output.close();

        } catch (IOException e) {
            System.err.println("Error closing the file");

        }
    }
}
