import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
//import java.util.Map;

public class SpamHamFilter {	

	public static HashMap<String, Integer> hashSpamWords;
	public static HashMap<String, Integer> hashHamWords;
	public static int numOfSpamMessages = 0;
	public static int numOfSpamWords = 0;
	public static int numOfHamMessages = 0;
	public static int numOfHamWords = 0;
	public static int numOfAllMessages = 0;
	public static float spamPriorProbability = 0;
	public static float hamPriorProbability = 0;

	public static void train(String input) {

		hashSpamWords = new HashMap<String, Integer>();
		hashHamWords = new HashMap<String, Integer>();

		String[] lines = input.split("\n");

		for (String l : lines) { 
			String[] line = l.split("\t");

			
			// IF MESSAGE IS SPAM
			if (line[0].equals("spam")) {
				String format = punctuationRemover(line[1]).toLowerCase();
				String[] spamWords = format.split(" ");
				for (String sw : spamWords) {

					if (!(sw.equals(""))) // not empty word
					{
						if (hashSpamWords.containsKey(sw)) {
							hashSpamWords.put(sw, hashSpamWords.get(sw) + 1); // increasing value
						} else {
							hashSpamWords.put(sw, 1);
						}
					}
					numOfSpamWords++;
				}

				numOfSpamMessages++;
			}

			// IF MESSAGE IS HAM
			else if (line[0].equals("ham")) {
				String format = punctuationRemover(line[1]).toLowerCase();
				String[] hamWords = format.split(" ");
				for (String hw : hamWords) {
					if (!(hw.equals(""))) // not empty word
					{
						if (hashHamWords.containsKey(hw)) {
							hashHamWords.put(hw, hashHamWords.get(hw) + 1); // increasing  value											
						} else {
							hashHamWords.put(hw, 1);
						}
					}
					numOfHamWords++;
				}

				numOfHamMessages++;
			}

			numOfAllMessages++;

		}

		// for (Map.Entry entry : hashSpamWords.entrySet()) {
		// System.out.println(entry.getKey() + "-" + entry.getValue());
		// }
		// for (Map.Entry entry : hashHamWords.entrySet()) {
		// System.out.println(entry.getKey() + "- " + entry.getValue());
		// }
		spamPriorProbability = numOfSpamMessages / (float) numOfAllMessages;
		hamPriorProbability = numOfHamMessages / (float) numOfAllMessages;

		System.out.println("numSpamWORDS in training set: " + numOfSpamWords);
		System.out.println("numHamWORDS in training set: " + numOfHamWords);
		System.out.println("numSpam Messages: " + numOfSpamMessages);
		System.out.println("numHam Messages: " + numOfHamMessages);
		System.out.println("numAll Messages: " + numOfAllMessages);
		System.out.println("SpamPriorProbability: " + spamPriorProbability);
		System.out.println("HamPriorProbability: " + hamPriorProbability);
		System.out.println("__________________________________________________________________________________________________________________________________________________________________________________________");

	}

	public static void test(String input) {

		int TP=0;
		int FN=0;
		int FP=0;
		int TN=0;
		int TP1=0;
		int FN1=0;
		int FP1=0;
		int TN1=0;
		
		String[] line = input.split("\n");
		for (String l : line) {
			String[] separate = l.split("\t");
			String trueClass = separate[0].toUpperCase();
			String predictedClass = " ";    //predicted using m-estimate
			String predictedClass1 = " ";   //predicted using Laplace
			String message = punctuationRemover(separate[1]).toLowerCase();
			String[] words = message.split(" ");
			System.out.println("Messae class: " + trueClass);
			System.out.println("Message: " + message);
		
			double likelihoodSpam = 1;
			double likelihoodHam = 1;
			double likelihoodSpam1 = 1;
			double likelihoodHam1 = 1;
			double posteriorProbabilitySpam = 0;
			double posteriorProbabilityHam = 0;
			double posteriorProbabilitySpam1 = 0;
			double posteriorProbabilityHam1 = 0;
		
			
			for (String w : words) {
	
				//System.out.println(w);
				
				float numTrainWordsSpam = 0;
				float numTrainWordsHam = 0;
				int numTrainWordsSpam1 = 0;
				int numTrainWordsHam1 = 0;
				
				if (hashSpamWords.get(w) != null) // if spam contains key w
				{
					numTrainWordsSpam =  hashSpamWords.get(w)+ (float)(1 * 0.5); // don't allow to have 0 value (M-estimate m=1, p=1/2)
					numTrainWordsSpam1 = hashSpamWords.get(w) + 1;		//don't allow to have 0 value Laplace 											
				} else {
					numTrainWordsSpam = (float) (1 * 0.5);
					numTrainWordsSpam1 = 1;
				}
				
				//System.out.println("(m-estimate) In training spam messages appear  " + numTrainWordsSpam + " times");
				//System.out.println("(laplace) In training spam messages appear " + numTrainWordsSpam1 + " times");
				
				if (hashHamWords.get(w) != null) // if ham contains key w
				{
					numTrainWordsHam = hashHamWords.get(w) + (float) (1 * 0.5);  // don't allow to have 0 value (M-estimate m=1, p=1/2)
					numTrainWordsHam1= hashHamWords.get(w) + 1;  //don't allow to have 0 value Laplace 			
				} else {
					numTrainWordsHam = (float) (1 * 0.5);
					numTrainWordsHam1= 1;	
				}
				
				//System.out.println("(m-estimate) In training ham messages appear " + numTrainWordsHam + " times");
				//System.out.println("(laplace) In training ham messages appear   " + numTrainWordsHam1 + " times");
				
				

				likelihoodSpam = likelihoodSpam * (numTrainWordsSpam / (float) (numOfSpamWords + 1));
				likelihoodSpam1 = likelihoodSpam1 * (numTrainWordsSpam1 / (float) (numOfSpamWords + 2));  
						
				//System.out.println("(m-estimate) Likelihood spam: " + likelihoodSpam);
				//System.out.println("(laplace) Likelihood spam: " + likelihoodSpam1);
				
				likelihoodHam = likelihoodHam * (numTrainWordsHam / (float) (numOfHamWords + 1));
				likelihoodHam1 = likelihoodHam1 * (numTrainWordsHam1 / (float) (numOfHamWords + 2));
						
				//System.out.println("(m-estimate) Likelihood ham: " + likelihoodHam);
				//System.out.println("(laplace) Likelihood ham: " + likelihoodHam1);
			}
			
			
			
			
			posteriorProbabilitySpam = likelihoodSpam * spamPriorProbability; //don't compute evidence because is same for both probabilities
			posteriorProbabilitySpam1 = likelihoodSpam1 * spamPriorProbability; 
			
			posteriorProbabilityHam = likelihoodHam * hamPriorProbability; //don't compute evidence because is same for both probabilities
			posteriorProbabilityHam1 = likelihoodHam1 * hamPriorProbability;

		//	System.out.println("Probability to be spam message using M-estimate: " + posteriorProbabilitySpam);
		//	System.out.println("Probability to be ham message  using M-estimate: " + posteriorProbabilityHam);
		//	System.out.println("Probability to be spam message using Laplace: " + posteriorProbabilitySpam1);
		//	System.out.println("Probability to be ham message  using Laplace: " + posteriorProbabilityHam1);

			if (posteriorProbabilitySpam > posteriorProbabilityHam) {
				predictedClass= "SPAM";
				System.out.println("Using NaiveBayes with m-estimation message is declared like SPAM!!");
			} else if (posteriorProbabilitySpam < posteriorProbabilityHam) {
				predictedClass= "HAM";
				System.out.println("Using NaiveBayes with m-estimation message is declared like HAM!!");
			} else
			{
				System.out.println("Using NaiveBayes with m-estimation message have equal probability to be SPAM and HAM!!");
			}

			//spam = positiveClass  ham=negativaClass
			if(trueClass.equals("SPAM") && predictedClass.equals("SPAM"))
			{
				TP++; //truePositive
			}
			if(trueClass.equals("SPAM") && predictedClass.equals("HAM") )
			{
				FN++; //falseNegative
			}
			if(trueClass.equals("HAM") && predictedClass.equals("SPAM") )
			{
				FP++; //falsePositive
			}
			if(trueClass.equals("HAM") && predictedClass.equals("HAM") )
			{
				TN++; //truePositive
			}
			
		
	
			if (posteriorProbabilitySpam1 > posteriorProbabilityHam1) {
				predictedClass1= "SPAM";
				System.out.println("Using NaiveBayes with Laplace message is declared like SPAM!!");
			} else if (posteriorProbabilitySpam1 < posteriorProbabilityHam1) {
				predictedClass1= "HAM";
				System.out.println("Using NaiveBayes with Laplace message is declared like HAM!!");
			} else
			{
				System.out.println("Using NaiveBayes with Laplace message have equal probability to be SPAM and HAM!!");
			}
			
			
			//spam = positiveClass  ham=negativaClass
			if(trueClass.equals("SPAM") && predictedClass1.equals("SPAM") )
			{
				TP1++; //truePositive
			}
			if(trueClass.equals("SPAM") && predictedClass1.equals("HAM") )
			{
				FN1++; //falseNegative
			}
			if(trueClass.equals("HAM") && predictedClass1.equals("SPAM") )
			{
				FP1++; //falsePositive
			}
			if(trueClass.equals("HAM") && predictedClass1.equals("HAM") )
			{
				TN1++; //truePositive
			}
			
     System.out.println("_____________________________________________________________________________________________________________________________________________________________________________________________");
		}
		
		System.out.println("Accuracy of classifier using m-estimation: " + (TP+TN)/(float)(TP+TN+FP+FN));
		System.out.println("Accuracy of classifier using Laplace: " + (TP1+TN1)/(float)(TP1+TN1+FP1+FN1));
		System.out.println("Precision of classifier using m-estimation: " + TP/(float)(TP+FP));
		System.out.println("Precision of classifier using Laplace: " + TP1/(float)(TP1+FP1));
	}

	public static String punctuationRemover(String s) {
		String output = "";
		for (int i = 0; i < s.length(); i++) {
			if (Character.isAlphabetic(s.charAt(i)) || (s.charAt(i) == ' ') || Character.isDigit(s.charAt(i))) {
				output += s.charAt(i);
			}
		}
		return output;
	}
	
	
	public static String readFile(FileInputStream file)
	{
		BufferedReader br = null;
		StringBuilder sb = null;
		String content = "";
		try {
			br = new BufferedReader(new InputStreamReader(file, "UTF-8"));
			sb = new StringBuilder();
			String contentLine = br.readLine(); // first line
			while (contentLine != null) {
				// System.out.println(contentLine);
				sb.append(contentLine);
				sb.append(System.lineSeparator()); // for new Line
				contentLine = br.readLine();
			}
			content = sb.toString();
			// System.out.print(content);

		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ioe) {
				System.out.println("Error in closing the BufferedReader");
			}
		}
		
		return content;
	}
	

	public static void main(String[] args) throws IOException {
		// reading input Files
		FileInputStream trainSet = new FileInputStream(args[0]);
		FileInputStream testSet = new FileInputStream(args[1]);
		train(readFile(trainSet));
		test(readFile(testSet));
	}

}
