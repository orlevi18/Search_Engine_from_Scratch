package Search;
import java.util.Map.Entry;


public class DocFreqMultiplier extends TermFeature
{
	public DocFreqMultiplier(DocFreq df,TermFeature tf)
	{
		for(Entry<String,Double> entry: df.termWeights.entrySet())
		{
			String key = entry.getKey();
			this.termWeights.put(key,entry.getValue()*tf.termWeights.get(key));
		}
	}
}
