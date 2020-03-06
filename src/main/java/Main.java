import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.Charset;

//I did my best....
public class Main {
    public static void main(String[] args) throws Exception {

        InputStream is = new FileInputStream("src/main/resources/dict.opcorpora.xml");

        XMLDictionaryPersister persister = new XMLDictionaryPersister().newInstance(is, Charset.forName("UTF-8"));
        System.out.println("XML deserialized!");

        DictionaryPredictiveSystem dict = new DictionaryPredictiveSystem();
        dict.fill(persister);
        System.out.println("Dictionary created and filled!");

        Generator gen = new Generator(1000000, 5, dict, persister.takeAllWords());
        System.out.println("Set generated!");

        //gen.layoutsToString(50);

        //Doesn't work yet
        gen.findBestLayout();
    }
}
