import java.util.*;


/**
 * Класс <code>DictionaryPredictiveSystem</code> представляет собой модель
 * предикативной системы со словарем.
 *
 * @author Pavel Manakhov (manakhovpavel@gmail.com)
 */
public class DictionaryPredictiveSystem extends Dictionary
		implements PredictiveTextSystem {

	static class PSNode extends Node {

		public PSNode(char character, int depth) {
			super(character, depth);
		}

		@Override
		protected Node newNode(char character, int depth) {
			return new PSNode(character, depth);
		}

		public TreeMap<Long, List<String>> getWordwormsByPattern(TreeMap<Long, List<String>> wordforms,
																 String prefix, List<String> pattern, int maxFrequency) {

			// Если шаблон поиска пуст...
			if (pattern.isEmpty())
				return wordforms; // ...вернуть список как он есть

			// Получаем группу символов, соответсвующую номеру буквы в слове
			String chracterGroup = "";
			//Если еще 'нажимаем' клавишу
			if (depth < pattern.size()) {
				chracterGroup = pattern.get(depth);
			}
			//Или берем всех детей текущей буквы и идем по ним
			else {
				for (Map.Entry<Character, Node> childNode : children.entrySet()) {
					chracterGroup += childNode.getKey();
				}
			}

			// Движемся по всем символам в группе
			for (int index = 0; index < chracterGroup.length(); index++) {

				// Находим соответсвующий узел
				final Node childNode = children.get(chracterGroup.charAt(index));

				// Если такая буква есть в текущей ветви...
				if (childNode != null) {
					//Если глубина больше количества клавиш и частота ребенка больше максимальной найденной
					if (depth >= pattern.size()) {
						wordforms.computeIfAbsent(childNode.frequency, k -> new ArrayList<>());
						wordforms.get(childNode.frequency).add(prefix + childNode.character);
						maxFrequency = (int) childNode.frequency;
					}
					// иначе - двигаемся дальше по шаблону
					//Если у ребенка есть дети, иначе не пойдем
					if (childNode.children.size() != 0) {
						((PSNode) childNode).getWordwormsByPattern(
								wordforms, prefix + childNode.character, pattern, maxFrequency);
					}
				}
			}

			return wordforms;
		}

	}

	/**
	 * Создает модель предикативной системы с пустым словарем.
	 */
	DictionaryPredictiveSystem() {
	}

	public static DictionaryPredictiveSystem newInstance() {
		return new DictionaryPredictiveSystem();
	}

	@Override
	protected PSNode rootNode() {
		if (root == null)
			root = new PSNode(' ' /*символ корневого узла не имеет значения*/, 0);

		return (PSNode) root;
	}

	/**
	 * Возвращает упорядоченный по вероятности ввода (1-ый элемент -
	 * наиболее вероятный) список словоформ, длина которых равна
	 * длине шаблона поиска. Словоформы, имеющие одинаковую частоту,
	 * будут упорядочены по алфавиту.
	 *
	 * @param regex шаблон поиска на основе регулярных выражений
	 *              вида <code>[мно][абвг]</code>.
	 * @return список словоформ по убыванию вероятности ввода. Если
	 * шаблон поиска пуст, то метод вернет пустой immutable список.
	 */
	@Override
	public List<String> getWordsByPattern(String regex) {
		final String emptyPrefix = "";
		List<String> pattern = convertRegexPattern(regex);

		/* Создаем набор, элементы которого будут упорядочены по
		 * частоте употреблений и наполняем его
		 */
		TreeMap<Long, List<String>> wordformSet = rootNode().getWordwormsByPattern(
				new TreeMap<Long, List<String>>(),
				emptyPrefix,
				pattern, 0);
		List<String> firstThree = new ArrayList<>();

		ArrayList<Long> keys = new ArrayList<Long>(wordformSet.keySet());

		for (int i = keys.size() - 1; i > -1 && firstThree.size() < 3; i--) {
			try {
				for (int l = 0; l < wordformSet.get(keys.get(i)).size() && firstThree.size() < 3; l++) {
					firstThree.add(wordformSet.get(keys.get(i)).get(l));
				}
			}
			catch (Exception e){
				System.out.println(e.getMessage());
			}
		}
		return firstThree;
	}

	/**
	 * Возвращает список групп символов, содержащихся в <code>regex</code>.
	 *
	 * @param regex шаблон поиска на основе регулярных выражений.
	 * @return список групп символов.
	 * @throws NullPointerException если <code>regex</code> равен <tt>null</tt>.
	 */
	protected static List<String> convertRegexPattern(String regex) {
		if (regex == null)
			throw new NullPointerException();

		// Если шаблон пуст...
		if (regex.isEmpty())
			return new ArrayList<String>(); // ...возвращаем пустой список

		// Проверка корректности шаблона
		if (!regex.matches("(\\[[\\p{L}\\p{M}\\p{N}_]+\\])+"))
			throw new IllegalArgumentException();

		/* Убираем первую квадратную скобку. Если этого не сделать,
		 * первым элементом списка будет пустая строка
		 */
		final String modifiedPattern = regex.replaceFirst("\\[", "");

		// Разбиваем шаблон поиска
		return Arrays.asList(modifiedPattern.split("[\\[\\]]+"));
	}

}