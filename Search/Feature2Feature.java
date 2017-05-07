package Search;
import java.util.Map.Entry;

public class Feature2Feature extends TermFeature
{
	public Feature2Feature(TermFeature tf1,TermFeature tf2)
	{		
		for(Entry<String, Double> entry: tf1.termWeights.entrySet())			
		{
			String term = entry.getKey();
			this.termWeights.put(term,entry.getValue()/tf2.termWeights.get(term));
		}  
	}
}
