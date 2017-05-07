package Search;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.search.Query;

public class QueryLanguageModel extends LanguageModel
{
	public  QueryLanguageModel(Query query)
	{
		Map<String,Long> termFreq = new HashMap<String,Long>();
		String q = query.toString(field);
		for(String term: q.split("\\s+"))
		{	
			long freq = 0;
			if(termFreq.containsKey(term))
			{
				freq = termFreq.get(term);
			}
			termFreq.put(term,freq+1);
			++length;
		}
		for(Entry<String,Long> entry: termFreq.entrySet())
		{
			termProb.put(entry.getKey(), entry.getValue()/Double.valueOf(length));
		}	
	}
}