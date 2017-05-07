package Search;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Search1 {

public static ColLanguageModel colLM;
	
	/** Simple command-line based search demo. */
	public static void main(String[] args) throws Exception 
	{
		
		String index = "C:/IR/Index/";
		String field = "contents";
		String queries = "C:/IR/Query/test.sgml";
		String out = "C:/IR/Result/ql.sgml";

		for(int i = 0;i < args.length;i++) 
		{
			if("-index".equals(args[i])) 
			{
				index = args[i+1];
				i++;
			}
			if("-queries".equals(args[i])) 
			{
				queries = args[i+1];
				i++;
			}			
			if("-out".equals(args[i])) 
			{
				out = args[i+1];
				i++;
			}							
		}

		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(index)));
		IndexSearcher searcher = new IndexSearcher(reader);
		
		colLM = new ColLanguageModel(reader);

		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(queries), "UTF-8"));

		Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_48);
		QueryParser parser = new QueryParser(Version.LUCENE_48, field, analyzer);
		
		String qid="";
		
		while (true) 
		{
			String line = in.readLine();

			if (line == null || line.length() == -1) {
				break;
			}

			line = line.trim();
			if (line.length() == 0) {
				break;
			}
			
			if(line.startsWith("<number>"))
			{
				int start = line.indexOf("<number>")+8;
				int end = line.indexOf("</number>");
				qid = line.substring(start,end);
			}			
			if(line.startsWith("<title>"))
			{
				int start = line.indexOf("<title>")+7;
				int end = line.indexOf("</title>");
				String qstr = line.substring(start,end);
				Query q = parser.parse(qstr);
				doBatchRMSearch(reader,searcher, qid, q,out);
			}
			
		}
		System.out.println("completed ql");
		in.close();
		reader.close();
	}	
	
	public static void doBatchRMSearch(IndexReader reader,IndexSearcher searcher,String qid,Query query,String out)	
			throws IOException {

		// run Query Likelihood
		HashMap<Integer,Double> docScoresMap = new HashMap<Integer,Double>();
		IntValueComparator bvc =  new IntValueComparator(docScoresMap);
		TreeMap<Integer,Double> sortedDocScoresMap = new TreeMap<Integer,Double>(bvc);
		
		int maxDoc = reader.maxDoc();
		
		TopDocs results = searcher.search(query,null,maxDoc,Sort.INDEXORDER,false,false);
		ScoreDoc[] hits = results.scoreDocs;
		int numTotalHits = results.totalHits;
		
		for(int i=0; i<numTotalHits; i++) 
		{
			DocLanguageModel docLM = new DocLanguageModel(hits[i].doc,reader);
			docScoresMap.put(hits[i].doc,docLM.calculateP_q_d(query));
		}		
		
		sortedDocScoresMap.putAll(docScoresMap);
		
		int i = 0;
		int end = 1000;
		if(end > numTotalHits)
		{
			end = numTotalHits;
		}
		out = out + "/ql/ql.txt";
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(out, true)));
		for(Entry<Integer,Double> entry: sortedDocScoresMap.descendingMap().entrySet())
		{
			if(i>=end)
			{
				break;
			}			
			Document doc = reader.document(entry.getKey());
			String docno = doc.get("docno");
			writer.println(qid + " Q0 "+ docno + " " + i + " " + entry.getValue() + " " + "ql");
			++i;
		}
		writer.close();
		System.out.println("query " + qid + " - ql");
	}	
}


