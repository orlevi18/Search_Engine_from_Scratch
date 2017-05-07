package Search;
import java.util.ArrayList;


public class RM_Weight extends LanguageModel
{
	 public RM_Weight(TermFeature tf,SumP_W_D_P_D_Q sum,int numOfTerms)
	 {
		 double s = 0.0;
		 ArrayList<String> terms = tf.selectTerms(numOfTerms);
		 for(String term: terms)
		 {
			 s += sum.sums.get(term);
		 }
		 for(String term: terms)
		 {
			 this.termProb.put(term, sum.sums.get(term)/s);
		 }
	 }
}
