import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    private Character[][] findLocalOptium(Character[][][] set) {
        int bestValue = Integer.MAX_VALUE;
        //For all layouts
        for (int k = 0; k < set.length; k++) {
            //Main criterion of choosing best layout
            long kspc = 0;
            //For output
            int counter = 0;
            //Foreach word
            for (String mainWord : allWords) {
                //Order of buttons to type a word
                System.out.println("\n" + mainWord + ":\n");
                String buttonsOrder = "";
                int kspcForWord = 0;
                if(mainWord.equals("абдурохмоновну")){
                    int fuck = 9;
                }
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
                            }
                            else {
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
                            kspc += 1;
                            kspcForWord += 1;
                            //Space button taps
                            int localCounter = 0;
                            for (String word : lst) {
                                ++localCounter;
                                if (word.equals(mainWord)) {
                                    //Add space taps
                                    kspc += localCounter;
                                    kspcForWord += localCounter;
                                    System.out.println(String.format("Количество букв: %s\nКоличество нажатий: %s", word.length(), kspcForWord));
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
                if(++counter % 500 == 0){
                    System.out.println(String.format("%s of %s words were checked!", counter, allWords.size()));
                }
            }
            System.out.println(String.format("Layout #%s checked!\nKSPC = %s\n", k, kspc));
        }
        return new Character[8][];
    }

    private void setNewSet(Character[][] optimum) {
        Character[][][] newSet = new Character[n][][];
        Character[][] template = findLocalOptium(set);
        //For every layout
        for (int k = 0; k < n; k++) {
            //For every button
            for (int i = 0; i < 8; i++) {
                for (int swap = 0; swap < 7; swap++) {
                    Character temp = set[k][i][swap];
                    set[k][i][swap] = set[k][i][swap + 1];
                    set[k][i][swap + 1] = temp;
                }
            }
        }
    }

    Character[][] findBestLayout() {
        Character[][] localOptimum = findLocalOptium(set);
        for (int i = 0; i < depth; i++) {
            //setNewSet();
        }
        return new Character[8][];
    }

    void layoutsToString(int n){
        for (int i = 0; i < n && i < set.length; i++){
            System.out.println(String.format("Layout #%s:\n", i + 1));
            for (int l = 0; l < set[i].length; l++){
                String button = String.format("Button %s:\t", l);
                for (int chr = 0; chr < set[i][l].length; chr++){
                    if(set[i][l][chr] != null){
                        button += set[i][l][chr].toString() + " ";
                    }
                    else{
                        break;
                    }
                }
                System.out.println(button);
            }
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