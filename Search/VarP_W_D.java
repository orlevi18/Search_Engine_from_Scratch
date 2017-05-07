package Search;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


public class VarP_W_D extends TermFeature
{
	private Map<String,Double> vars = new HashMap<String,Double>();

	public VarP_W_D(AvgP_W_D avg)
	{
		calculate_vars(avg);
		this.termWeights = this.vars;
	}
	
	 private void calculate_vars(AvgP_W_D avg)
	 {
	 	for(Entry<String,Double> entry: avg.means.entrySet())
		 {
	 		 String term = entry.getKey();
			 double sum = 0.0;
			 double mean = entry.getValue();
			 for(int i = 0 ; i<avg.length;i++)
			 {
				 double d = avg.dinit[i].getTermProb(term);
				 sum += (d-mean)*(d-mean);
			 }
			this.vars.put(term, sum/avg.length);		
		 }		 
	 }
}