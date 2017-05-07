package Search;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;

public class RM3LanguageModel extends LanguageModel 
{
	public RM3LanguageModel(QueryLanguageModel q,LanguageModel r,Double beta)
	{
		for(Entry<String,Double> entry: r.termProb.entrySet())
		{
			this.termProb.put(entry.getKey(), (1-beta)*entry.getValue());
		}
		for(Entry<String,Double> entry: q.termProb.entrySet())
		{
			String key = entry.getKey();
			double val = 0;
			if(this.termProb.containsKey(key))
			{
				val = this.termProb.get(key);
			}
			this.termProb.put(key, val + beta*entry.getValue());
		}		
	}
	public void print_res(IndexReader ir,IndexSearcher is,QueryParser qp,String qid,String out,String param,String runtag) throws IOException, ParseException
	{
		HashMap<Integer,Double> docScoresMap = new HashMap<Integer,Double>();
		IntValueComparator bvc =  new IntValueComparator(docScoresMap);
		TreeMap<Integer,Double> sortedDocScoresMap = new TreeMap<Integer,Double>(bvc);
		
		int maxDoc = ir.maxDoc();
		String qstr = "";
		for(String s: this.getTerms())
		{
			qstr = qstr + " " + s;
		}
		Query q = qp.parse(qstr);
		
		TopDocs results = is.search(q,null,maxDoc,Sort.INDEXORDER,false,false);
		ScoreDoc[] hits = results.scoreDocs;
		int numTotalHits = results.totalHits;
		
		for(int i=0; i<numTotalHits; i++) 
		{
			DocLanguageModel docLM = new DocLanguageModel(hits[i].doc,ir);
			docScoresMap.put(hits[i].doc,this.klDiv(docLM));
		}	
		
		sortedDocScoresMap.putAll(docScoresMap);
		
		int i = 0;
		int end = 1000;
		if(end > numTotalHits)
		{
			end = numTotalHits;
		}
		
		out = out + "/" + runtag + "/" + runtag + param + ".txt";
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(out, true)));
		for(Entry<Integer,Double> entry: sortedDocScoresMap.entrySet())
		{
			if(i>=end)
			{
				break;
			}			
			Document doc = ir.document(entry.getKey());
			String docno = doc.get("docno");
			writer.println(qid + " Q0 "+ docno + " " + i + " " + entry.getValue() + " " + runtag);
			++i;
		}
		writer.close();	
		System.out.println("query " + qid + " - " + runtag + param + " " + System.currentTimeMillis());
	}	
}
