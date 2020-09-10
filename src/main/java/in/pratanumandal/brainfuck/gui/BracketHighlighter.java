package in.pratanumandal.brainfuck.gui;

import javafx.application.Platform;

import java.util.*;

public class BracketHighlighter {

    // the code area
    private final CustomCodeArea codeArea;

    // the map of bracket pairs existing in code
    private Map<Integer, Integer> brackets;

    // the list of highlighted bracket pairs
    private List<BracketPair> bracketPairs;

    /**
     * Style lists
     */
    private static final List<String> LOOP_STYLE = Collections.singletonList("loop");
    private static final List<String> MATCH_STYLE = Arrays.asList("match", "loop");

    /**
     * Parameterized constructor
     * @param codeArea the code area
     */
    public BracketHighlighter(CustomCodeArea codeArea) {
        this.codeArea = codeArea;

        this.brackets = new HashMap<>();
        this.bracketPairs = new ArrayList<>();

        this.codeArea.addTextInsertionListener((start, end, text) -> clearBracket());
        this.codeArea.textProperty().addListener((obs, oldVal, newVal) -> initializeBrackets(newVal));
        this.codeArea.caretPositionProperty().addListener((obs, oldVal, newVal) -> Platform.runLater(() -> highlightBracket(newVal)));
    }

    /**
     * Method to initialize the bracket pairs (do only when text is changed)
     *
     * @param code the next text
     */
    public synchronized void initializeBrackets(String code) {
        // clear bracket map
        this.brackets.clear();

        // compute matching brackets and add to map
        Stack<Integer> stack = new Stack<>();
        int index = 0;

        while (index < code.length()) {
            int i = code.indexOf("[", index);
            int j = code.indexOf("]", index);

            if (i == -1 && j == -1) {
                break;
            } else if (i != -1 && (i < j || j == -1)) {
                stack.push(i);
                index = i + 1;
            } else if (j != -1) {
                if (!stack.isEmpty()) {
                    int k = stack.pop();
                    brackets.put(k, j);
                    brackets.put(j, k);
                }
                index = j + 1;
            }
        }
    }

    /**
     * Highlight the matching bracket at new caret position
     *
     * @param newVal the new caret position
     */
    private synchronized void highlightBracket(int newVal) {
        // first clear existing bracket highlights
        this.clearBracket();

        // detect caret position both before and after bracket
        String prevChar = (newVal > 0 && newVal <= codeArea.getLength()) ? codeArea.getText(newVal - 1, newVal) : "";
        if (prevChar.equals("[") || prevChar.equals("]")) newVal--;

        // get other half of matching bracket
        Integer other = this.brackets.get(newVal);

        if (other != null) {
            // other half exists
            BracketPair pair = new BracketPair(newVal, other);

            // highlight pair
            styleBrackets(pair, MATCH_STYLE);

            // add bracket pair to list
            this.bracketPairs.add(pair);
        }
    }

    /**
     * Highlight the matching bracket at current caret position
     */
    public synchronized void highlightBracket() {
        this.highlightBracket(codeArea.getCaretPosition());
    }

    /**
     * Clear the existing highlighted bracket styles
     */
    public synchronized void clearBracket() {
        // get iterator of bracket pairs
        Iterator<BracketPair> iterator = this.bracketPairs.iterator();

        // loop through bracket pairs and clear all
        while (iterator.hasNext()) {
            // get next bracket pair
            BracketPair pair = iterator.next();

            // clear pair
            styleBrackets(pair, LOOP_STYLE);

            // remove bracket pair from list
            iterator.remove();
        }
    }

    /**
     * Set a list of styles to a pair of brackets
     *
     * @param pair pair of brackets
     * @param styles the style list to set
     */
    private void styleBrackets(BracketPair pair, List<String> styles) {
        styleBracket(pair.start, styles);
        styleBracket(pair.end, styles);
    }

    /**
     * Set a list of styles for a position
     *
     * @param pos the position
     * @param styles the style list to set
     */
    private void styleBracket(int pos, List<String> styles) {
        if (pos < codeArea.getLength()) {
            String text = codeArea.getText(pos, pos + 1);
            if (text.equals("[") || text.equals("]")) {
                codeArea.setStyle(pos, pos + 1, styles);
            }
        }
    }

    /**
     * Class representing a pair of matching bracket indices
     */
    static class BracketPair {

        private int start;
        private int end;

        public BracketPair(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        @Override
        public String toString() {
            return "BracketPair{" +
                    "start=" + start +
                    ", end=" + end +
                    '}';
        }

    }

}
