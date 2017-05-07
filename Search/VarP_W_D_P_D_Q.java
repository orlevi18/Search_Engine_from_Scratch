package Search;
import java.util.HashMap;
import java.util.Map;


public class VarP_W_D_P_D_Q extends TermFeature
{
	private Map<String,Double> vars = new HashMap<String,Double>();
	
	public VarP_W_D_P_D_Q(SumP_W_D_P_D_Q sum)
	{
		calculate_vars(sum);
		this.termWeights = this.vars;
	}
	
	 private void calculate_vars(SumP_W_D_P_D_Q sum)
	 {
		 for(String w: sum.sums.keySet())
		 {
			 double s = 0.0;
			 double mean = sum.sums.get(w)/sum.length;
		
			 for(int i = 0 ; i<sum.length;i++)
			 {
				 double d = sum.dinit[i].getTermProb(w)*sum.P_d_q[i];
					
				 s += (d-mean)*(d-mean);
			 }
			 this.vars.put(w, s/sum.length);
		 }
	 }
}	

