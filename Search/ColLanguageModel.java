package Search;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

public class ColLanguageModel extends LanguageModel 
{	
	public ColLanguageModel(IndexReader ir) throws IOException
	{
		Map<String,Long> termFreq = new HashMap<String,Long>();
		Terms terms = SlowCompositeReaderWrapper.wrap(ir).terms(field);
		TermsEnum te = terms.iterator(null);
		BytesRef text = null;
		while((text = te.next()) != null)
		{
			Term term = new Term(field,text);
			Long freq = ir.totalTermFreq(term);
			termFreq.put(text.utf8ToString(),freq);
			length += freq;
		}
		for(Entry<String,Long> entry: termFreq.entrySet())
		{
			termProb.put(entry.getKey(), entry.getValue()/Double.valueOf(length));
		}	
	}
}