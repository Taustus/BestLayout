import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * Класс <code>Generator</code> представляет собой генератор раскладок
 * и алгоритм для поиска лучшей раскладки среди них
 *
 * @author Sergey Babikov (staustus@gmail.com)
 */
class Generator {

    private Random rnd;
    private DictionaryPredictiveSystem predictiveSystem;
    private int n, depth;

    private List<String> allWords;
    private Character[][][] set;
    private Character[][] buttons;

    Generator(int n, int depth, DictionaryPredictiveSystem dict, List<String> allWords) {
        rnd = new Random();
        predictiveSystem = dict;

        this.n = n;
        this.depth = depth;

        this.allWords = allWords;
        set = new Character[n][][];
        buttons = new Character[8][];

        initiate();
    }

    private void initiate() {
        String letters = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя";

        List<Character> excluded;

        //Generating set
        for (int k = 0; k < set.length; k++) {
            excluded = convertStringToCharList(letters);
            //Generating layout
            for (int i = 0; i < buttons.length; i++) {
                //Every button can have maximum 8 letters
                buttons[i] = new Character[8];
                int size;
                //'bound' and 'counter' should optimize our random
                int bound = 8;
                int counter = 0;
                //In 'while' we should find optimal amount of letters in current button
                while (true) {
                    //1 + [0;8} <= 8 - can't be bigger than length of button array
                    size = 1 + rnd.nextInt(bound);
                    int buttonsLeft = 8 - (i + 1);
                    //In every button should be a letter...
                    if (size > excluded.size() - buttonsLeft) {
                        //Here we trying to optimize random
                        if (++counter % 5 == 0 && bound > 2) {
                            bound--;
                        }
                    }
                    //...but also we have to insert all letters in layout
                    else if (!(excluded.size() - size > buttonsLeft * 8) &&
                            //And lower bound also should be >= 3
                            size >= 3 && (excluded.size() - size) / 3 >= buttonsLeft) {
                        //Break if we're ok
                        break;
                    }
                }
                for (int l = 0; l < size; l++) {
                    //Random letter from a list
                    int index = rnd.nextInt(excluded.size());
                    //Add it to layout
                    buttons[i][l] = excluded.get(index);
                    //And delete it, because we can't have duplicates
                    excluded.remove(index);
                }

            }
            //Array is reference type, so we should clone it
            set[k] = buttons.clone();
        }
    }

    public Pair<Double, Integer> findLocalOptium(Character[][][] set, int lastBest, double best, boolean initial) {
        double bestValue = best;
        int bestIndex = -1;
        //For all layouts
        for (int k = lastBest + 1; k < set.length; k++) {
            if(set[k] == null){
                continue;
            }
            //Main criterion of choosing best layout
            double kspc = 0;
            //Foreach word
            for (String mainWord : allWords) {
                //Order of buttons to type a word
                //System.out.println("\n" + mainWord + ":\n");
                String buttonsOrder = "";
                double kspcForWord = 0;
                //For all letters in this word
                for (int chr = 0; chr < mainWord.length(); chr++) {
                    //For all buttons
                    for (int i = 0; i < 8; i++) {
                        String buttonSet = "";
                        boolean goodButton = false;
                        //For all letters in button
                        for (int let = 0; let < set[k][i].length; let++) {
                            //Aware of nulls
                            if (set[k][i][let] != null) {
                                //Add to button set
                                buttonSet += set[k][i][let];
                                //If button contains current letter of a word we'll choose it
                                if (set[k][i][let].equals(mainWord.charAt(chr))) {
                                    goodButton = true;
                                }
                            } else {
                                break;
                            }
                        }
                        //Add button if there's a letter of a word
                        if (goodButton) {
                            //Add button letters
                            buttonsOrder += String.format("[%s]", buttonSet);
                            //Take words that were predicted
                            List<String> lst = predictiveSystem.getWordsByPattern(buttonsOrder);
                            //Button was "pressed"
                            kspcForWord += 1;
                            //Space button taps
                            int localCounter = 0;
                            for (String word : lst) {
                                ++localCounter;
                                if (word.equals(mainWord)) {
                                    //Add space taps
                                    kspcForWord += localCounter;
                                    //System.out.println(String.format("Количество букв: %s\nКоличество нажатий: %s", word.length(), kspcForWord));
                                    //To break cycle of mainWord chars
                                    chr = mainWord.length();
                                    //Break local cycle
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }
                int frequency = (int) predictiveSystem.getFrequency(mainWord);
                kspc += (frequency * kspcForWord) / (mainWord.length() * frequency);
            }
            System.out.println(String.format("\nLayout #%s checked!\nKSPC = %s\n", k, kspc));
            if (kspc < bestValue && k != bestIndex) {
                bestValue = kspc;
                bestIndex = k;
                if (!initial) {
                    break;
                }
            }
        }
        //If we didn't find optimum return null
        return (bestIndex != -1 ? new ImmutablePair<>(bestValue, bestIndex) : null);
    }

    private Character[][][] setNewSet(Character[][] optimum) {
        Character[][][] newSet = new Character[n][][];
        Random rnd = new Random();
        //For every layout
        for (int k = 0; k < n; k++) {
            //Reference type.......
            Character[][] temp = cloneLayout(optimum);

            int firstBtn = rnd.nextInt(8);
            int secondBtn = rnd.nextInt(8);
            int firstLength = calculateLength(temp, firstBtn);
            int secondLength = calculateLength(temp, secondBtn);

            if (firstLength > 3) {
                if (secondLength < 7) {
                    temp[secondBtn][secondLength] = temp[firstBtn][firstLength - 1];
                    temp[firstBtn][firstLength - 1] = null;
                    newSet[k] = temp.clone();
                }
                else {
                    k--;
                }
            } else {
                if (secondLength > 3) {
                    temp[firstBtn][firstLength] = temp[secondBtn][secondLength - 1];
                    temp[secondBtn][secondLength - 1] = null;
                    newSet[k] = temp.clone();
                } else {
                    k--;
                }
            }
        }
        return newSet.clone();
    }

    private int calculateLength(Character[][] optimum, int btnIndex) {
        int length = optimum[0].length;
        for (int i = 0; i < optimum[btnIndex].length; i++) {
            if (optimum[btnIndex][i] == null) {
                length = i;
                break;
            }
        }
        return length;
    }

    Pair<Double, Character[][]> solution_lowest;

    Pair<Double, Character[][]> recursion(Character[][][][] storage, Pair<Double, Integer> kspc_index,
                                               Pair<Double, Character[][]> solution, List<Integer> depth_layoutIndex,
                                               int depth) {
        try {
            //If initial == false then take first layout which KSPC less than lowest
            boolean initial = depth == 0;
            //If depth == 0 we don't want to change main set
            storage[depth] = depth > 0 ? setNewSet(cloneLayout(storage[depth - 1][kspc_index.getRight()])) : cloneSet(set);
            System.out.println("\nDepth: " + depth + "\n");
            //If depth == 0, then solution'll be equals null
            //To avoid NullReferenceException we should use this ternary operator
            solution = solution != null ? solution : new ImmutablePair<>(Double.MAX_VALUE, null);
            //If we don't have info about this depth we should start from the first index
            int lastBest = depth_layoutIndex.size() > depth ? depth_layoutIndex.get(depth) : -1;
            //Find best layout and it's KSPC
            kspc_index = findLocalOptium(storage[depth], lastBest, solution.getLeft(), initial);

            if (kspc_index != null) {
                System.out.println(String.format("Index of chosen layout: %s\nKSPC of chosen layout: %s", kspc_index.getRight(), kspc_index.getLeft()));
                //Set new best KSPC
                solution = new ImmutablePair<>(kspc_index.getLeft(), cloneLayout(storage[depth][kspc_index.getRight()]));
                //If we were not so deep
                if (depth_layoutIndex.size() - 1 < depth) {
                    depth_layoutIndex.add(kspc_index.getRight());
                }
                //Else change index of current best layout
                else {
                    depth_layoutIndex.set(depth, kspc_index.getRight());
                }
                if(depth < this.depth - 1){
                    solution = recursion(storage, kspc_index, solution, depth_layoutIndex, depth + 1);
                }
                if(lastBest < n && depth != 0){
                    solution = recursion(storage, kspc_index, solution, depth_layoutIndex, depth);
                }
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        if(depth == 0){
                set[depth_layoutIndex.get(0)] = null;
            if(depth_layoutIndex.get(0) != n - 1){
                Pair<Double, Character[][]> temp = recursion(storage, kspc_index, null, depth_layoutIndex, depth);
                solution = temp.getLeft() < solution.getLeft() ? temp : solution;
                solution_lowest = solution_lowest == null || solution_lowest.getLeft() > solution.getLeft() ? solution : solution_lowest;
            }
        }
        return solution;
    }

    Pair<Double, Character[][]> findBestLayout(){
        Character[][][][] storage = new Character[depth][n][8][];
        //storage[0] = cloneSet(set);
        Pair<Double, Integer> kspc_index = null;//findLocalOptium(set, -1, Integer.MAX_VALUE, true);
        Pair<Double, Character[][]> solution = null;//(kspc_index.getLeft(), cloneLayout(storage[0][kspc_index.getRight()]));
        List<Integer> depth_layoutIndex = new ArrayList<>();
        //depth_layoutIndex.add(kspc_index.getRight());
        return recursion(storage, kspc_index, solution, depth_layoutIndex, 0);
    }

    private Character[][] cloneLayout(Character[][] set) {
        Character[][] clonedLayout = new Character[set.length][];
        for (int i = 0; i < set.length; i++) {
            clonedLayout[i] = set[i].clone();
        }
        return clonedLayout;
    }

    private Character[][][] cloneSet(Character[][][] set) {
        Character[][][] clonedSet = new Character[set.length][][];
        for (int i = 0; i < set.length; i++) {
            if(set[i] != null){
                clonedSet[i] = cloneLayout(set[i]);
            }
        }
        return clonedSet;
    }

    void layoutsToString(int n) {
        for (int i = 0; i < n && i < set.length; i++) {
            System.out.println(String.format("Layout #%s:\n", i + 1));
            for (int l = 0; l < set[i].length; l++) {
                String button = String.format("Button %s:\t", l);
                for (int chr = 0; chr < set[i][l].length; chr++) {
                    if (set[i][l][chr] != null) {
                        button += set[i][l][chr].toString() + " ";
                    } else {
                        break;
                    }
                }
                System.out.println(button);
            }
            System.out.println();
        }
    }

    void layoutsToString(Character[][] layout) {
        System.out.println(String.format("Layout #%s:\n", 666));
        for (int l = 0; l < layout.length; l++) {
            String button = String.format("Button %s:\t", l);
            for (int chr = 0; chr < layout[l].length; chr++) {
                if (layout[l][chr] != null) {
                    button += layout[l][chr].toString() + " ";
                } else {
                    break;
                }
            }
            System.out.println(button);

            System.out.println();
        }
    }

    private List<Character> convertStringToCharList(String str) {
        List<Character> chars = new ArrayList<>();
        for (char ch : str.toCharArray()) {
            chars.add(ch);
        }
        return chars;
    }
}