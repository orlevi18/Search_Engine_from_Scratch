package Search;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

public abstract class TermFeature extends LanguageModel
{
	protected Map<String,Double> termWeights = new HashMap<String,Double>();
	
	public void truncate(int numOfTerms)
	{
		StringValueComparator bvc =  new StringValueComparator(this.termWeights);
		TreeMap<String,Double> sortedTermWeights = new TreeMap<String,Double>(bvc);
		
		sortedTermWeights.putAll(this.termWeights);
		
		double sum = 0.0;
		int i = 0;
		for(Entry<String,Double> entry: sortedTermWeights.descendingMap().entrySet())
		{
			if(i >= numOfTerms)
			{
				break;
			}
			sum += entry.getValue();
			++i;
		}
		
		int j = 0;
		for(Entry<String,Double> entry: sortedTermWeights.descendingMap().entrySet())
		{
			if(j >= numOfTerms)
			{
				break;
			}
			this.termProb.put(entry.getKey(), entry.getValue()/sum);
			++j;
		}
	}		
	
	ArrayList<String> selectTerms(int numOfTerms)
	{
		ArrayList<String> a = new ArrayList<String>();
		StringValueComparator bvc =  new StringValueComparator(this.termWeights);
		TreeMap<String,Double> sortedTermWeights = new TreeMap<String,Double>(bvc);
		
		sortedTermWeights.putAll(this.termWeights);
		
		int i=0;
		for(Entry<String,Double> entry: sortedTermWeights.descendingMap().entrySet())
		{
			if(i >= numOfTerms)
			{
				break;
			}
			a.add(entry.getKey());
			++i;
		}
		return a;
	}
}