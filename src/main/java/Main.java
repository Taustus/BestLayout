import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.nio.charset.Charset;

//I did my best....
public class Main {
    public static void main(String[] args) throws Exception {

        //InputStream is = new FileInputStream("src/main/resources/dict.opcorpora.xml");
        InputStream is = new FileInputStream("src/main/resources/small_dictionary.xml");

        XMLDictionaryPersister persister = new XMLDictionaryPersister().newInstance(is, Charset.forName("UTF-8"), 6000);
        System.out.println("XML deserialized!");

        //OutputStream os = new FileOutputStream("src/main/resources/small_dictionary.xml");
        //persister.write(os, Charset.forName("UTF-8"));

        DictionaryPredictiveSystem dict = new DictionaryPredictiveSystem();
        dict.fill(persister);
        System.out.println("Dictionary created and filled!");

        Generator gen = new Generator(4, 3, dict, persister.takeAllWords());
        System.out.println("Set generated!");

        Character[][][] idol = new Character[][][]{{
                {'й', 'ц', 'ф', 'ы', 'я'},
                {'у', 'в', 'ч'},
                {'к', 'а', 'с'},
                {'е', 'п', 'м'},
                {'н', 'р', 'и', 'т'},
                {'г', 'о', 'ь'},
                {'ш', 'л', 'б'},
                {'щ', 'з', 'х', 'ъ', 'д', 'ж', 'э', 'ю'}}};
        //0.5103547417121262
        //double best = gen.findLocalOptium(idol, -1, Integer.MAX_VALUE, true).getLeft();

        //gen.layoutsToString(50);

        //Doesn't work yet
        Pair<Double, Character[][]> temp = gen.findBestLayout();
        gen.layoutsToString(temp.getRight());
        System.out.println("KSPC = " + temp.getLeft());
    }
}
