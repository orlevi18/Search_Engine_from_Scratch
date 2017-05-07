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
//import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.queryparser.classic.QueryParser;
//import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
//import org.apache.lucene.search.ScoreDoc;
//import org.apache.lucene.search.TopDocs;
//import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.Version;

/** Simple command-line based search demo. */
public class Search 
{
	private Search() {}

	public static ColLanguageModel colLM;
	
	/** Simple command-line based search demo. */
	public static void main(String[] args) throws Exception 
	{
		/*String usage =
				"Usage:\tjava BatchSearch [-index dir] [-simfn similarity] [-field f] [-queries file]";
		if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0])))
		{
			System.out.println(usage);
			System.out.println("Supported similarity functions:\ndefault: DefaultSimilary (tfidf)\n");
			System.exit(0);
		}*/

		String index = "C:/IR/Index/";
		String field = "contents";
		String queries = "C:/IR/Query/test.sgml";
				//"C:/IR/query/test.sgml";//"C:/IR/query/title-queries2.301-450";
		String out = "C:/IR/Result/ql.sgml";
		//String simstring = "default";
		int numOfFeedbackDocs = 3;
		int expansionTerms = 5;
		double queryAnchor = 0.7;

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
			if("-dinit".equals(args[i])) 
			{
				numOfFeedbackDocs = Integer.parseInt(args[i+1]);
				i++;
			}
			if("-expansion".equals(args[i])) 
			{
				expansionTerms = Integer.parseInt(args[i+1]);
				i++;
			}
			if("-beta".equals(args[i])) 
			{
				queryAnchor = Double.parseDouble(args[i+1]);
				i++;
			}		
			if("-out".equals(args[i])) 
			{
				out = System.getProperty("user.home") + "/OUT/RM3/" + args[i+1];
				i++;
			}							
			/*if ("-index".equals(args[i])) 
			{
				index = args[i+1];
				i++;
			} */
			/*else if ("-field".equals(args[i])) 
			{
				field = args[i+1];
				i++;
			} */
			/*else if ("-queries".equals(args[i]))
			{
				queries = args[i+1];
				i++;
			} */
			/*else if ("-simfn".equals(args[i])) 
			{
				simstring = args[i+1];
				i++;
			}*/
		}

		/*Similarity simfn = null;
		if ("default".equals(simstring)) {
			simfn = new DefaultSimilarity();
		} else if ("bm25".equals(simstring)) {
			simfn = new BM25Similarity();
		} else if ("dfr".equals(simstring)) {
			simfn = new DFRSimilarity(new BasicModelP(), new AfterEffectL(), new NormalizationH2());
		} else if ("lm".equals(simstring)) {
			simfn = new LMDirichletSimilarity();
		}
		if (simfn == null) {
			System.out.println(usage);
			System.out.println("Supported similarity functions:\ndefault: DefaultSimilary (tfidf)");
			System.out.println("bm25: BM25Similarity (standard parameters)");
			System.out.println("dfr: Divergence from Randomness model (PL2 variant)");
			System.out.println("lm: Language model, Dirichlet smoothing");
			System.exit(0);
		}*/

		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(index)));
		//IndexSearcher searcher = new IndexSearcher(reader);
		//searcher.setSimilarity(simfn);
		Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_48);

		colLM = new ColLanguageModel(reader);
		//colLM.print();

		BufferedReader in = null;
		//if (queries != null) {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(queries), "UTF-8"));
		/*} else {
			in = new BufferedReader(new InputStreamReader(new FileInputStream("queries"), "UTF-8"));
		}*/
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
			
			// test parser
			/*String[] pair = line.split(" ", 2);
			Query query = parser.parse(pair[1]);
			doBatchRMSearch(reader, pair[0], query,numOfFeedbackDocs,expansionTerms,queryAnchor,out,"rm3");*/
//			
//			Bits liveDocs = MultiFields.getLiveDocs(reader);
//			HashMap<Integer,DocLanguageModel> DocLMs = new HashMap<Integer,DocLanguageModel>();
//			for (int i=0; i<reader.maxDoc(); i++) {
//				if (liveDocs != null && !liveDocs.get(i))
//					continue;
//				Document doc = reader.document(i);
//				String docno = doc.get("docno");
//				DocLanguageModel docLM = new DocLanguageModel(i,reader,colLM);
//				DocLMs.put(i, docLM);
//			}
		
			
			// robust parser
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
				doBatchRMSearch(reader, qid, q,numOfFeedbackDocs,expansionTerms,queryAnchor,out,"ql");
			}
			
		}
		in.close();
		reader.close();
	}

	/*public static void doBatchSearch(BufferedReader in, IndexSearcher searcher, String qid, Query query, String runtag)	
throws IOException {

// Collect enough docs to show 5 pages
TopDocs results = searcher.search(query, 1000);
ScoreDoc[] hits = results.scoreDocs;
HashMap<String, String> seen = new HashMap<String, String>(1000);
int numTotalHits = results.totalHits;

int start = 0;
int end = Math.min(numTotalHits, 1000);

for (int i = start; i < end; i++) {
Document doc = searcher.doc(hits[i].doc);
String docno = doc.get("docno");
// There are duplicate document numbers in the FR collection, so only output a given
// docno once.
if (seen.containsKey(docno)) {
continue;
}
seen.put(docno, docno);
System.out.println(qid+" Q0 "+docno+" "+i+" "+hits[i].score+" "+runtag);
}
}	*/
	
	/**
	 * This function performs a top-1000 search for the query as a basic TREC run.
	 */
	/*public static void doBatchSearch2(BufferedReader in, IndexSearcher searcher, IndexReader reader,String qid, Query query, ColLanguageModel colLM, String runtag)	
			throws IOException {

		TopDocs results = searcher.search(query, 1000);
		ScoreDoc[] hits = results.scoreDocs;
		HashMap<String, String> seen = new HashMap<String, String>(1000);
		int numTotalHits = results.totalHits;

		QueryLanguageModel qLM = new QueryLanguageModel(query);
		//qLM.print();

		HashMap<String,Double> docScoresMap = new HashMap<String,Double>();
		ValueComparator bvc =  new ValueComparator(docScoresMap);
		TreeMap<String,Double> sortedDocScoresMap = new TreeMap<String,Double>(bvc);

		int start = 0;
		int end = Math.min(numTotalHits, 1000);

		for (int i = start; i < end; i++) 
		{
			DocLanguageModel docLM = new DocLanguageModel(hits[i].doc,reader,colLM);
			Document doc = searcher.doc(hits[i].doc);
			String docno = doc.get("docno");
			docScoresMap.put(docno,qLM.klDiv(docLM));
		}

		sortedDocScoresMap.putAll(docScoresMap);

		int i = 0;
		for(Entry<String,Double> entry: sortedDocScoresMap.entrySet())
		{
			System.out.println(qid + " Q0 "+ entry.getKey() + " " + i + " " + entry.getValue() + " " + runtag);
			++i;
		}*/



		/*

for (int i = start; i < end; i++) 
{
Document doc = searcher.doc(hits[i].doc);
String docno = doc.get("docno");*/
		// There are duplicate document numbers in the FR collection, so only output a given
		// docno once.
		/*if (seen.containsKey(docno)) {
continue;
}
seen.put(docno, docno);
System.out.println(qid+" Q0 "+docno+" "+i+" "+hits[i].score+" "+runtag);
}
}
	}*/

	public static void doBatchRMSearch(IndexReader reader,String qid,Query query,
			int numOfFeedbackDocs,int expansionTerms,double queryAnchor,String out,String runtag)	
			throws IOException {

		// run Query Likelihood
		HashMap<Integer,Double> docScoresMap = new HashMap<Integer,Double>();
		IntValueComparator bvc =  new IntValueComparator(docScoresMap);
		TreeMap<Integer,Double> sortedDocScoresMap = new TreeMap<Integer,Double>(bvc);
		
		Bits liveDocs = MultiFields.getLiveDocs(reader);
		
		for (int i=0; i<reader.maxDoc(); i++) 
		{
			if (liveDocs != null && !liveDocs.get(i))
				continue;
			DocLanguageModel docLM = new DocLanguageModel(i,reader);
			//docLM.print();
			docScoresMap.put(i,docLM.calculateP_q_d(query));
			if(i%100==0)
			{
				System.out.println("query " + qid + "doc " + i + " - calculating QL");
			}
			//System.out.println(i + "---------------------------------------------- " + docLM.check());
		}		
		
		sortedDocScoresMap.putAll(docScoresMap);
		
		// build dinit
		
		// **** int end = Math.min(numTotalHits, numOfFeedbackDocs);
		//DocLanguageModel[] dinit = new DocLanguageModel[numOfFeedbackDocs];
		//Double[] p_q_d = new Double[numOfFeedbackDocs];
		
		/*System.out.println("query " + qid + " - calculating rm..");
		int j=0;
		for(Entry<Integer,Double> entry: sortedDocScoresMap.descendingMap().entrySet())
		{
			if(j>=numOfFeedbackDocs)
			{
				break;
			}
			dinit[j] = new DocLanguageModel(entry.getKey(),reader);
			++j;
		}*/

		// run feature		
		
		// feature 1
		//AvgP_W_D avgP_w_d = new AvgP_W_D(dinit);
		//avgP_w_d.truncate(numOfTerms);
		
		// feature 2
		//VarP_W_D varP_w_d = new VarP_W_D(avgP_w_d);
		//varP_w_d.truncate(numOfTerms);
		
		// feature 3
		//Feature2Feature avg2varP_w_d = new Feature2Feature(avgP_w_d,varP_w_d);
		//avg2varP_w_d.truncate(numOfTerms);
		
		// feature 4
		/*SumP_W_D_P_D_Q sumP_w_d_p_d_q = new SumP_W_D_P_D_Q(query,dinit);
		sumP_w_d_p_d_q.truncate(expansionTerms);
		sumP_w_d_p_d_q.print();*/
		// feature 5
		//VarP_W_D_P_D_Q varP_w_d_p_d_q = new VarP_W_D_P_D_Q(sumP_w_d_p_d_q);
		//varP_w_d_p_d_q.truncate(numOfTerms);
		
		// feature 6
		//Feature2Feature sum2varP_w_d_p_d_q = new Feature2Feature(sumP_w_d_p_d_q,varP_w_d_p_d_q);
		//sum2varP_w_d_p_d_q.truncate(numOfTerms);
		//System.out.println("sum2varP_w_d_p_d_q" + " " + sum2varP_w_d_p_d_q.check());
		
		// feature 7
		//DocFreq df = new DocFreq(dinit);
		//df.truncate(numOfTerms);
		//System.out.println("df" + " " + df.check());
		
		// feature 8
		//DocFreqMultiplier df_avgP_w_d = new DocFreqMultiplier(df,avgP_w_d);
		//df_avgP_w_d.truncate(numOfTerms);
		
		// feature 9
		//DocFreqMultiplier df_varP_w_d = new DocFreqMultiplier(df,varP_w_d);
		//df_varP_w_d.truncate(numOfTerms);
		
		// feature 10
		//DocFreqMultiplier df_avg2varP_w_d = new DocFreqMultiplier(df,avg2varP_w_d);
		//df_avg2varP_w_d.truncate(numOfTerms);
		
		// feature 11
		//DocFreqMultiplier df_sumP_w_d_p_d_q = new DocFreqMultiplier(df,sumP_w_d_p_d_q);
		//df_sumP_w_d_p_d_q.truncate(numOfTerms);
		
		// feature 12
		//DocFreqMultiplier df_varP_w_d_p_d_q = new DocFreqMultiplier(df,varP_w_d_p_d_q);
		//df_varP_w_d_p_d_q.truncate(numOfTerms);
		
		// feature 13
		//DocFreqMultiplier df_sum2varP_w_d_p_d_q = new DocFreqMultiplier(df,sum2varP_w_d_p_d_q);
		//df_sum2varP_w_d_p_d_q.truncate(numOfTerms);
		
		//System.out.println("df_sum2varP_w_d_p_d_q" + " " + df_sum2varP_w_d_p_d_q.check());
		
		// run RM3
		//QueryLanguageModel qLM = new QueryLanguageModel(query);	
		//System.out.println("qlm" + " " + qLM.check());
		
		// weight func 1
		//RM3LanguageModel RM3 = new RM3LanguageModel(qLM,sumP_w_d_p_d_q,queryAnchor);
		//RM3.print();
		//System.out.println("rm3" + " " + RM3.check());
		
		// weight func 2
		//RM_Weight rmw = new RM_Weight(avgP_w_d,sumP_w_d_p_d_q,numOfTerms);
		//RM3LanguageModel RM3_w2 = new RM3LanguageModel(qLM,rmw,0.7);
		
	
	
		
//		for(Entry<Integer, DocLanguageModel> entry: DocLMs.entrySet()){
//			Integer key = entry.getKey();
//			DocLanguageModel docLM = entry.getValue();
//			Document doc = reader.document(key);
//			String docno = doc.get("docno");
//			docScoresMap.put(docno,RM.klDiv(docLM));
//		}

		/*docScoresMap.clear();
		sortedDocScoresMap.clear();
		
		for (int i=0; i<reader.maxDoc(); i++) 
		{
			if (liveDocs != null && !liveDocs.get(i))
				continue;
			DocLanguageModel docLM = new DocLanguageModel(i,reader);
			docScoresMap.put(i,RM3.klDiv(docLM));
			if(i%100==0)
			{
				System.out.println("query " + qid + "doc " + i + " - calculating KL");
			}
		}			
		
		sortedDocScoresMap.putAll(docScoresMap);*/

		// print ql
		System.out.println("query " + qid + " - printing..");
		int i = 0;
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(out, true)));
		for(Entry<Integer,Double> entry: sortedDocScoresMap.descendingMap().entrySet())
		{
			Document doc = reader.document(entry.getKey());
			String docno = doc.get("docno");
			writer.println(qid + " Q0 "+ docno + " " + i + " " + entry.getValue() + " " + runtag);
			++i;
		}
		
		// print rm3
		/*System.out.println("query " + qid + " - printing..");
		int i = 0;
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(out, true)));
		for(Entry<Integer,Double> entry: sortedDocScoresMap.entrySet())
		{
			Document doc = reader.document(entry.getKey());
			String docno = doc.get("docno");
			writer.println(qid + " Q0 "+ docno + " " + i + " " + entry.getValue() + " " + runtag);
			++i;
		}*/
		writer.close();
		System.out.println("done!");
	}
}