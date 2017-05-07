package Search;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.BytesRef;

public class DocLanguageModel extends LanguageModel
{
	private static Double smoothingParam = 1000.0;

	public  DocLanguageModel(int docID,IndexReader ir) throws IOException
	{
		Map<String,Long> termFreq = new HashMap<String,Long>();
		Terms terms = ir.getTermVector(docID, field);
	
		//length = (int) terms.getSumTotalTermFreq();
		
		if(terms!=null)
		{
			TermsEnum termsEnum = null;
			TermsEnum te = terms.iterator(termsEnum);
			BytesRef text = null;
				while((text = te.next()) != null)
				{
					termFreq.put(text.utf8ToString(), te.totalTermFreq());
					//termProb.put(text.utf8ToString(), te.totalTermFreq()/Double.valueOf(length));
					length = (int) (length + te.totalTermFreq());
				}	
		}
		for(Entry<String,Long> entry: termFreq.entrySet())
		{
			termProb.put(entry.getKey(), entry.getValue()/Double.valueOf(length));
		}	
	}
	
	public double getTermProb(String term) // this is P(w|d)
	{
		double prob = 0;
		if(termProb.containsKey(term))
		{
			prob = termProb.get(term);
		}
		double lambda = smoothingParam / (smoothingParam + length);
		return (1-lambda)*prob + lambda*Search2.colLM.getTermProb(term);
	}
		
		double calculateP_q_d(Query query)
		{
			String[] queryTerms = query.toString(field).split("\\s+");
			double p_q_d = 1;
			
			for(String term: queryTerms)
			{	
				if(Search2.colLM.termProb.containsKey(term))
				{
					p_q_d *= this.getTermProb(term);
				}
			}
			return p_q_d;
		}
		
}	