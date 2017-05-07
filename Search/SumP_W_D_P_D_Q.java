package Search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.search.Query;

public class SumP_W_D_P_D_Q extends TermFeature
{
	DocLanguageModel[] dinit;
	Double[] P_d;
	Double[] P_d_q;
	Double[] P_q_d;
	Double Sum_P_d_P_q_d = 0.0;
	Map<String,Double> sums = new HashMap<String,Double>();
	
	public SumP_W_D_P_D_Q(Query query,DocLanguageModel[] dinit)
	{
		this.dinit = dinit;
		this.length = dinit.length;
		populateP_q_d(query);
	    populateP_d();
		populateP_d_q();
		calculate_sum();
		this.termWeights = this.sums;
	}
	
	 private void calculate_sum()
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
				 sum += this.dinit[i].getTermProb(w)*P_d_q[i];
			 }
			this.sums.put(w, sum);		
		 }
	 }	
	 private void populateP_d()
	 {
		 this.P_d = new Double[this.length];
		 for(int i=0 ;i<this.length;i++)
		 {
			 this.P_d[i] = 1/Double.valueOf(this.length);
		 }
	 }
	 private void populateP_q_d(Query query)
	 {
		 this.P_q_d = new Double[this.length];
		 for(int i=0 ;i<this.length;i++)
		 {
			 this.P_q_d[i] = this.dinit[i].calculateP_q_d(query);
		 }
	 }
	 private void populateP_d_q()
	 {
		 this.P_d_q = new Double[this.length];
			
		 for(int i = 0 ; i<this.length;i++)
		 {
				this.Sum_P_d_P_q_d +=  P_d[i]*P_q_d[i];
		 }
		for(int i=0 ;i<this.length;i++)
		{
			this.P_d_q[i] = P_d[i]*P_q_d[i]/Sum_P_d_P_q_d;
		}	
	 }	
}