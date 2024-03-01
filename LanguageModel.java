import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) {
		String window = "";
        char c;
        In in = new In(fileName);
        while (!in.isEmpty() && window.length() < windowLength) {
            window += in.readChar();
        }
        while (!in.isEmpty()) {
            c = in.readChar();
            List probs;
            if (CharDataMap.containsKey(window)) {
                probs = CharDataMap.get(window);
            } else {
                probs = new List();
                CharDataMap.put(window, probs);
            }
            probs.update(c);
            window = window.substring(1) + c;
        }
        for (List probs : CharDataMap.values()) {
            calculateProbabilities(probs);
        }
	}

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	public void calculateProbabilities(List probs) {				
        double prevCp = 0;
        int charCount = 0;

        for (int i = 0; i < probs.getSize(); i++) {
            charCount += probs.get(i).count;
        }
        for (int i = 0; i < probs.getSize(); i++) {
            probs.get(i).p = (double)probs.get(i).count / charCount;
            probs.get(i).cp = probs.get(i).p + prevCp;
            prevCp = probs.get(i).cp;
        }
	}

    // Returns a random character from the given probabilities list.
	public char getRandomChar(List probs) {
        double r = randomGenerator.nextDouble();
		for(int i = 0; i < probs.getSize(); i++) {
            if (probs.get(i).cp >= r) {
                return probs.get(i).chr;
            }
        }
        return ' ';
	}

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
		if (initialText.length() < windowLength) {
            return initialText;
        }
        StringBuilder generatedText = new StringBuilder(initialText);
        while (generatedText.length() <= textLength + 5) {
            String window = generatedText.substring(Math.max(0, generatedText.length() - windowLength));
            if (!CharDataMap.containsKey(window)) {
                return generatedText.toString();
            }
            char c = getRandomChar(CharDataMap.get(window));
            generatedText.append(c);
        }
        return generatedText.toString();
	}

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {
		/*LanguageModel lm = new LanguageModel(2);
        List probs = new List();
        String s = "committee ";
        for (int i = s.length() - 1; i >= 0; i--) {
            probs.update(s.charAt(i));
        }
        lm.calculateProbabilities(probs);
        int T = Integer.parseInt(args[0]);
        System.out.println("expected distribution:\n");
        for (int i = 0; i < probs.getSize(); i++) {
            System.out.println(probs.get(i).chr + " should occur " + probs.get(i).p);
        }
        int[] count =  new int[probs.getSize()];
        for (int t = 0; t < T; t++) {
            char c = lm.getRandomChar(probs);
            count[probs.indexOf(c)]++;
        }
        System.out.printf("actual distribution after %d trails:\n", T);
        for (int i = 0; i < probs.getSize(); i++) {
            System.out.println(probs.get(i).chr + " occured" + (double) count[i] / T);
        }
        
        lm.train("galileo.txt");
        System.out.println(lm);*/
        int windowLength = Integer.parseInt(args[0]);
        String initialText = args[1];
        int generatedTextLength = Integer.parseInt(args[2]);
        Boolean randomGeneration = args[3].equals("random");
        String fileName = args[4];
        // Create the LanguageModel object
        LanguageModel lm;
        if (randomGeneration)
           lm = new LanguageModel(windowLength);
        else
           lm = new LanguageModel(windowLength, 20);
        // Trains the model, creating the map.
        lm.train(fileName);
        // Generates text, and prints it.
        System.out.println(lm.generate(initialText, generatedTextLength));
    }
}
