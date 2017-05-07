package Search;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AvgP_W_D extends TermFeature
{
	protected DocLanguageModel[] dinit;
	Map<String,Double> means = new HashMap<String,Double>();

	public AvgP_W_D(DocLanguageModel[] dinit)
	{
		this.dinit = dinit;
		this.length = dinit.length;
		
		calculate_means();
		this.termWeights = this.means;
	}

	public void calculate_means()
	{
		 Set<String> terms = new HashSet<String>();
		 for(int i = 0 ; i<this.length;i++)
		 {
			terms.addAll(this.dinit[i].getTerms());
		 }	
		 for(String w: terms)
		 {
			 double sum = 0.0;
			 for(int i = 0 ; i<this.length;i++)
			 {
				 sum += this.dinit[i].getTermProb(w);
			 }
			this.means.put(w, sum/length);		
		 }	
	}
}