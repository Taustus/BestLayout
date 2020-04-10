import org.apache.commons.lang3.tuple.Pair;
import org.simpleframework.xml.convert.Convert;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;

//I did my best....
public class Main {
    public static void main(String[] args) throws Exception {

        InputStream is;
        try {
            is = new FileInputStream("src/main/resources/small_dictionary.xml");
        }
        catch(FileNotFoundException e) {
            is = new FileInputStream("small_dictionary.xml");
        }
        XMLDictionaryPersister persister = new XMLDictionaryPersister().newInstance(is, Charset.forName("UTF-8"), 6000);

        System.out.println("XML deserialized!");

        DictionaryPredictiveSystem dict = new DictionaryPredictiveSystem();
        dict.fill(persister);
        System.out.println("Dictionary created and filled!");

        HW_2(dict, args);

        //HW_1
        /////////////////////////////////////////////////////////////////////////////////
        //Generator gen = new Generator(60, 6, dict, persister.takeAllWords());
        //System.out.println("Set generated!");

        //Character[][][] idol = new Character[][][]{{
        //        {'й', 'ц', 'ф', 'ы', 'я'},
        //        {'у', 'в', 'ч'},
        //        {'к', 'а', 'с'},
        //        {'е', 'п', 'м'},
        //        {'н', 'р', 'и', 'т'},
        //        {'г', 'о', 'ь'},
        //        {'ш', 'л', 'б'},
        //        {'щ', 'з', 'х', 'ъ', 'д', 'ж', 'э', 'ю'}}};
        //0.5103547417121262
        //double best = gen.findLocalOptium(idol, -1, Integer.MAX_VALUE, true).getLeft();

        //gen.layoutsToString(50);

        //Doesn't work yet
        //Pair<Double, Character[][]> temp = gen.findBestLayout();
        //gen.layoutsToString(temp.getRight());
        //System.out.println("KSPC = " + temp.getLeft());
    }

    static void HW_2(DictionaryPredictiveSystem dict, String[] args) throws IOException {
        //Замени "1" на args[0]
        String test;
        try {
            test = args[0];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            return;
        }
        Character[][][] idol = new Character[][][]{{
                {'ы', 'х', 'в', 'н', 'г'},
                {'ь', 'я', 'л', 'ц', 'е'},
                {'к', 'с', 'з'},
                {'ё', 'э', 'р', 'ж', 'п'},
                {'а', 'й', 'м'},
                {'ю', 'щ', 'о', 'б'},
                {'и', 'ш', 'д', 'ъ'},
                {'у', 'ф', 'т', 'ч'}}};
        String buttonsOrder = "";
        Generator.layoutsToString(idol[0]);
        for(int i = 0; i < test.length(); i++){
            Character[] temp = idol[0][Integer.parseInt(String.valueOf(test.charAt(i)))];
            for(int l = 0; l < temp.length; l++){
                if(l == 0){
                    buttonsOrder += "[";
                }
                buttonsOrder += temp[l];
                if(l == temp.length - 1){
                    buttonsOrder += "]";
                }
            }
        }
        DictionaryPredictiveSystem predictiveSystem = dict;
        int n = 10;
        List<String> lst = predictiveSystem.getWordsByPattern(buttonsOrder, n);
        //Твои слова через пробел
        String returnString = "";
        System.out.println(String.format("First %s words:", n));
        for(int i = 0; i < lst.size(); i++)
        {
            System.out.println(lst.get(i));
            returnString += lst.get(i) + " ";
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter("HW_2.txt"));
        writer.write(returnString);
        writer.close();
        System.out.println("File created!");
    }
}
