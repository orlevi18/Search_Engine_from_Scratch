package Search;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public abstract class LanguageModel
{
	protected Map<String,Double> termProb = new HashMap<String,Double>();
	protected int length = 0;
	protected static String field = "contents";

	public int getLength() 
	{
		return this.length;
	}
	
	public double getTermProb(String term) // this is P(w|d)
	{
		return termProb.get(term);
	}
	
	public Set<String> getTerms() 
	{
		return termProb.keySet();
	}
	
	public double klDiv(DocLanguageModel that)
	{
		double kldScore = 0;
		for(Entry<String,Double> entry: this.termProb.entrySet())
		{
			String key = entry.getKey();
			if(Search2.colLM.termProb.containsKey(key))
			{
				Double val = entry.getValue();;
				kldScore += val*Math.log(val/(that.getTermProb(key)));
			}
		}
		return kldScore;
	}
	
	public void print()
	{
		for(Entry<String,Double> entry: this.termProb.entrySet())
		{
			System.out.println(entry.getKey()+" -> "+entry.getValue());
		}
	}
	public double check()
	{
		double sum = 0.0;
		for(Entry<String,Double> entry: this.termProb.entrySet())
		{
			sum += entry.getValue();
		}	
		return (sum);
	}
}

