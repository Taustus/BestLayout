import org.simpleframework.xml.*;
import org.simpleframework.xml.core.Persister;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Класс представляет собой загрузчик словаря в "сыром" виде.
 */
@Root(name = "dictionary", strict = false)
public class XMLDictionaryPersister implements DictionaryPersister {

    private static final String FORMATTED_XML_HEADER = "<?xml version=\"1.0\" encoding=\"%s\"?>\n";

    @ElementList(name = "lemmata", inline = false)
    private List<Word> wordsList;

    @ElementMap(name = "default_view", key = "wordform", value = "frequency", required = false)
    public HashMap<String, Integer> words;

    /**
     * Создает пустой загрузчик словаря.
     */
    XMLDictionaryPersister() {
        this.wordsList = new ArrayList<>();
        this.words = new HashMap<String, Integer>();
    }

    /**
     * Создает пустой загрузчик словаря.
     *
     * @return экземпляр класса загрузчика.
     */
    public static XMLDictionaryPersister newInstance() {
        return new XMLDictionaryPersister();
    }

    public static XMLDictionaryPersister newInstance(InputStream stream,
                                                     Charset encoding, int wordsCount) throws Exception {
        if (stream == null || encoding == null)
            throw new NullPointerException();

        final Serializer serializer = new Persister();
        XMLDictionaryPersister toReturn = serializer.read(XMLDictionaryPersister.class, new InputStreamReader(
                stream, encoding));
        toReturn.CreateMap(wordsCount);
        return toReturn;
    }

    private void CreateMap(int wordsCount) throws Exception {
        if (words.size() == 0) {
            //All wordForms from words to one list
            List<String> lst = new ArrayList<>();
            for (Word word : wordsList) {
                for (WordForm anotherWord : word.forms) {
                    lst.add(anotherWord.form);
                }
            }
            Random rnd = new Random();
            //Calculate amount of each word and add to map
            for (int i = 0; i < wordsCount; i++) {
                int index = rnd.nextInt(lst.size() - 1);
                words.put(lst.get(index), Collections.frequency(lst, lst.get(index)));
                lst.remove(index);
                System.out.println(String.format("\nWords left:\t%s\nHashmap size:\t%s", wordsCount - (i + 1), words.size()));
            }

            wordsList.removeAll(wordsList);
            OutputStream os = new FileOutputStream("src/main/resources/small_dictionary.xml");
            write(os, Charset.forName("UTF-8"));
        }
    }

    /**
     * @throws NullPointerException     если <code>wordform</code> равен <tt>null</tt>.
     * @throws IllegalArgumentException если <code>wordform</code> пуст или
     *                                  <code>frequency</code> меньше 1.
     */
    @Override
    public boolean add(String wordform, long frequency) {
        if (wordform == null)
            throw new NullPointerException();

        if (wordform.isEmpty() || frequency < 1)
            throw new IllegalArgumentException();

        // Проверка наличия словоформы
        if (words.containsKey(wordform))
            return false;

        words.put(wordform, (int) frequency);

        return true;
    }

    /**
     * @throws NullPointerException если <code>wordform</code> равен <tt>null</tt>.
     */
    @Override
    public int getFrequency(String wordform) {
        if (wordform == null)
            throw new NullPointerException();

        /* Будем считать, что слово нулевой длины встречается
         * в словаре 0 раз
         */
        Integer freq = words.get(wordform);
        if (freq == null)
            return 0;

        return freq;
    }

    public ArrayList<String> takeAllWords(){
        return new ArrayList<String>(words.keySet());
    }

    public void write(OutputStream stream, Charset encoding) throws Exception {
        if (stream == null || encoding == null)
            throw new NullPointerException();

        final Serializer serializer = new Persister();

        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(stream, encoding);
        outputStreamWriter.write(String.format(FORMATTED_XML_HEADER, encoding.name()));
        outputStreamWriter.flush();

        serializer.write(this, outputStreamWriter);
    }

    @Override
    public Iterator<String> iterator() {
        final Set<String> wordforms = words.keySet();

        return wordforms.iterator();
    }

}
