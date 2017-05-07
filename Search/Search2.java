package Search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Search2 {

public static ColLanguageModel colLM;
	
	/** Simple command-line based search demo. */
	public static void main(String[] args) throws Exception 
	{
		
		String index = "C:/IR/Index/";
		String field = "contents";
		String queries = "C:/IR/Query/test.sgml";
		String out = "C:/IR/Result/ql.sgml";
		String param = "";
		String numOfFeedbackDocsStr = "";
		int numOfFeedbackDocs = 3;
		String expansionTermsStr = "";
		int expansionTerms = 5;
		String queryAnchorStr = "";
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
				numOfFeedbackDocsStr = args[i+1];
				numOfFeedbackDocs = Integer.parseInt(args[i+1]);
				i++;
			}
			if("-expansion".equals(args[i])) 
			{
				expansionTermsStr = args[i+1];
				expansionTerms = Integer.parseInt(args[i+1]);
				i++;
			}
			if("-beta".equals(args[i])) 
			{
				queryAnchorStr = args[i+1];
				queryAnchor = Double.parseDouble(args[i+1]);
				i++;
			}		
			if("-out".equals(args[i])) 
			{
				out = args[i+1];
				i++;
			}							
		}
		param = "_dinit_" + numOfFeedbackDocsStr + "_expansion_" + expansionTermsStr + "_beta_" + queryAnchorStr;

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
				doBatchRMSearch(reader,searcher, parser, qid, q,numOfFeedbackDocs,expansionTerms,queryAnchor,out,param);
			}
			
		}
		System.out.println("completed" + param);
		in.close();
		reader.close();
	}	
	
	public static void doBatchRMSearch(IndexReader reader,IndexSearcher searcher,QueryParser parser,String qid,Query query,
			int numOfFeedbackDocs,int expansionTerms,double queryAnchor,String out,String param)	
			throws IOException, ParseException {

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
		
		if(numOfFeedbackDocs > numTotalHits)
		{
			numOfFeedbackDocs = numTotalHits;
		}
		
		// build dinit
		DocLanguageModel[] dinit = new DocLanguageModel[numOfFeedbackDocs];
		
		int j=0;
		for(Entry<Integer,Double> entry: sortedDocScoresMap.descendingMap().entrySet())
		{
			if(j>=numOfFeedbackDocs)
			{
				break;
			}
			dinit[j] = new DocLanguageModel(entry.getKey(),reader);
			++j;
		}

		// run feature		

		
		/*AvgP_W_D avg_p_w_d = new AvgP_W_D(dinit);
		avg_p_w_d.truncate(expansionTerms);
		
	
		VarP_W_D var_p_w_d = new VarP_W_D(avg_p_w_d);
		var_p_w_d.truncate(expansionTerms);
		
		
		Feature2Feature avg2var_p_w_d = new Feature2Feature(avg_p_w_d,var_p_w_d);
		avg2var_p_w_d.truncate(expansionTerms);*/
		
	
		SumP_W_D_P_D_Q sum_p_w_d_p_d_q = new SumP_W_D_P_D_Q(query,dinit);
		sum_p_w_d_p_d_q.truncate(expansionTerms);

		
		/*VarP_W_D_P_D_Q var_p_w_d_p_d_q = new VarP_W_D_P_D_Q(sum_p_w_d_p_d_q);
		var_p_w_d_p_d_q.truncate(expansionTerms);
		

		Feature2Feature sum2var_p_w_d_p_d_q = new Feature2Feature(sum_p_w_d_p_d_q,var_p_w_d_p_d_q);
		sum2var_p_w_d_p_d_q.truncate(expansionTerms);
		
	
		DocFreq df = new DocFreq(dinit);
		df.truncate(expansionTerms);
		
	
		DocFreqMultiplier df_avg_p_w_d = new DocFreqMultiplier(df,avg_p_w_d);
		df_avg_p_w_d.truncate(expansionTerms);
		
	
		DocFreqMultiplier df_var_p_w_d = new DocFreqMultiplier(df,var_p_w_d);
		df_var_p_w_d.truncate(expansionTerms);
		
	
		DocFreqMultiplier df_avg2var_p_w_d = new DocFreqMultiplier(df,avg2var_p_w_d);
		df_avg2var_p_w_d.truncate(expansionTerms);		
		
	
		DocFreqMultiplier df_sum_p_w_d_p_d_q = new DocFreqMultiplier(df,sum_p_w_d_p_d_q);
		df_sum_p_w_d_p_d_q.truncate(expansionTerms);

		
		DocFreqMultiplier df_var_p_w_d_p_d_q = new DocFreqMultiplier(df,var_p_w_d_p_d_q);
		df_var_p_w_d_p_d_q.truncate(expansionTerms);
		
		
		DocFreqMultiplier df_sum2var_p_w_d_p_d_q = new DocFreqMultiplier(df,sum2var_p_w_d_p_d_q);
		df_sum2var_p_w_d_p_d_q.truncate(expansionTerms);*/
	
		
		QueryLanguageModel qLM = new QueryLanguageModel(query);	
		
		
		RM3LanguageModel rm3 = new RM3LanguageModel(qLM,sum_p_w_d_p_d_q,queryAnchor);
		rm3.print_res(reader,searcher,parser, qid, out, param, "rm3");
		
		/*RM3LanguageModel df_w1 = new RM3LanguageModel(qLM,df,queryAnchor);
		df_w1.print_res(reader,searcher,parser, qid, out, param, "df_w1");	

		RM3LanguageModel df_sum_p_w_d_p_d_q_w1 = new RM3LanguageModel(qLM,df_sum_p_w_d_p_d_q,queryAnchor);
		df_sum_p_w_d_p_d_q_w1.print_res(reader,searcher,parser, qid, out, param, "df_sum_p_w_d_p_d_q_w1");	
		
	
		RM_Weight df_w2_rm = new RM_Weight(df,sum_p_w_d_p_d_q,expansionTerms);
		RM3LanguageModel df_w2_rm3 = new RM3LanguageModel(qLM,df_w2_rm,queryAnchor);
		df_w2_rm3.print_res(reader,searcher,parser, qid, out, param, "df_w2");
		
		RM_Weight df_sum_p_w_d_p_d_q_w2_rm = new RM_Weight(df_sum_p_w_d_p_d_q,sum_p_w_d_p_d_q,expansionTerms);
		RM3LanguageModel df_sum_p_w_d_p_d_q_w2_rm3 = new RM3LanguageModel(qLM,df_sum_p_w_d_p_d_q_w2_rm,queryAnchor);
		df_sum_p_w_d_p_d_q_w2_rm3.print_res(reader,searcher,parser, qid, out, param, "df_sum_p_w_d_p_d_q_w2");
		
		
		
		
		
		
		
		RM3LanguageModel avg_p_w_d_w1 = new RM3LanguageModel(qLM,avg_p_w_d,queryAnchor);
		avg_p_w_d_w1.print_res(reader,searcher,parser, qid, out, param, "avg_p_w_d_w1");		
		
		RM3LanguageModel var_p_w_d_w1 = new RM3LanguageModel(qLM,var_p_w_d,queryAnchor);
		var_p_w_d_w1.print_res(reader,searcher,parser, qid, out, param, "var_p_w_d_w1");		
		
		RM3LanguageModel avg2var_p_w_d_w1 = new RM3LanguageModel(qLM,avg2var_p_w_d,queryAnchor);
		avg2var_p_w_d_w1.print_res(reader,searcher,parser,qid, out, param, "avg2var_p_w_d_w1");		

		RM3LanguageModel df_avg_p_w_d_w1 = new RM3LanguageModel(qLM,df_avg_p_w_d,queryAnchor);
		df_avg_p_w_d_w1.print_res(reader,searcher,parser, qid, out, param, "df_avg_p_w_d_w1");	
	
		RM3LanguageModel df_var_p_w_d_w1 = new RM3LanguageModel(qLM,df_var_p_w_d,queryAnchor);
		df_var_p_w_d_w1.print_res(reader,searcher,parser,  qid, out, param, "df_var_p_w_d_w1");	

		RM3LanguageModel df_avg2var_p_w_d_w1 = new RM3LanguageModel(qLM,df_avg2var_p_w_d,queryAnchor);
		df_avg2var_p_w_d_w1.print_res(reader,searcher,parser, qid, out, param, "df_avg2var_p_w_d_w1");			
		
		
		RM_Weight avg_p_w_d_w2_rm = new RM_Weight(avg_p_w_d,sum_p_w_d_p_d_q,expansionTerms);
		RM3LanguageModel avg_p_w_d_w2_rm3 = new RM3LanguageModel(qLM,avg_p_w_d_w2_rm,queryAnchor);
		avg_p_w_d_w2_rm3.print_res(reader,searcher,parser, qid, out, param, "avg_p_w_d_w2");
		
		RM_Weight var_p_w_d_w2_rm = new RM_Weight(var_p_w_d,sum_p_w_d_p_d_q,expansionTerms);
		RM3LanguageModel var_p_w_d_w2_rm3 = new RM3LanguageModel(qLM,var_p_w_d_w2_rm,queryAnchor);
		var_p_w_d_w2_rm3.print_res(reader,searcher,parser, qid, out, param, "var_p_w_d_w2");		
		
		RM_Weight avg2var_p_w_d_w2_rm = new RM_Weight(avg2var_p_w_d,sum_p_w_d_p_d_q,expansionTerms);
		RM3LanguageModel avg2var_p_w_d_w2_rm3 = new RM3LanguageModel(qLM,avg2var_p_w_d_w2_rm,queryAnchor);
		avg2var_p_w_d_w2_rm3.print_res(reader,searcher,parser, qid, out, param, "avg2var_p_w_d_w2");
		
		RM_Weight df_avg_p_w_d_w2_rm = new RM_Weight(df_avg_p_w_d,sum_p_w_d_p_d_q,expansionTerms);
		RM3LanguageModel df_avg_p_w_d_w2_rm3 = new RM3LanguageModel(qLM,df_avg_p_w_d_w2_rm,queryAnchor);
		df_avg_p_w_d_w2_rm3.print_res(reader,searcher,parser, qid, out, param, "df_avg_p_w_d_w2");
		
		RM_Weight df_var_p_w_d_w2_rm = new RM_Weight(df_var_p_w_d,sum_p_w_d_p_d_q,expansionTerms);
		RM3LanguageModel df_var_p_w_d_w2_rm3 = new RM3LanguageModel(qLM,df_var_p_w_d_w2_rm,queryAnchor);
		df_var_p_w_d_w2_rm3.print_res(reader,searcher,parser, qid, out, param, "df_var_p_w_d_w2");	
		
		RM_Weight df_avg2var_p_w_d_w2_rm = new RM_Weight(df_avg2var_p_w_d,sum_p_w_d_p_d_q,expansionTerms);
		RM3LanguageModel df_avg2var_p_w_d_w2_rm3 = new RM3LanguageModel(qLM,df_avg2var_p_w_d_w2_rm,queryAnchor);
		df_avg2var_p_w_d_w2_rm3.print_res(reader,searcher,parser, qid, out, param, "df_avg2var_p_w_d_w2");		
		
		
		
		
		
		
		
		
		
		RM3LanguageModel var_p_w_d_p_d_q_w1 = new RM3LanguageModel(qLM,var_p_w_d_p_d_q,queryAnchor);
		var_p_w_d_p_d_q_w1.print_res(reader,searcher,parser, qid, out, param, "var_p_w_d_p_d_q_w1");

		RM3LanguageModel sum2var_p_w_d_p_d_q_w1 = new RM3LanguageModel(qLM,sum2var_p_w_d_p_d_q,queryAnchor);
		sum2var_p_w_d_p_d_q_w1.print_res(reader,searcher,parser,qid, out, param, "sum2var_p_w_d_p_d_q_w1");
		
		RM3LanguageModel df_var_p_w_d_p_d_q_w1 = new RM3LanguageModel(qLM,df_var_p_w_d_p_d_q,queryAnchor);
		df_var_p_w_d_p_d_q_w1.print_res(reader,searcher,parser, qid, out, param, "df_var_p_w_d_p_d_q_w1");

		RM3LanguageModel df_sum2var_p_w_d_p_d_q_w1 = new RM3LanguageModel(qLM,df_sum2var_p_w_d_p_d_q,queryAnchor);
		df_sum2var_p_w_d_p_d_q_w1.print_res(reader,searcher,parser,  qid, out, param, "df_sum2var_p_w_d_p_d_q_w1");
		
		
		RM_Weight var_p_w_d_p_d_q_w2_rm = new RM_Weight(var_p_w_d_p_d_q,sum_p_w_d_p_d_q,expansionTerms);
		RM3LanguageModel var_p_w_d_p_d_q_w2_rm3 = new RM3LanguageModel(qLM,var_p_w_d_p_d_q_w2_rm,queryAnchor);
		var_p_w_d_p_d_q_w2_rm3.print_res(reader,searcher,parser,qid, out, param, "var_p_w_d_p_d_q_w2");
		
		RM_Weight sum2var_p_w_d_p_d_q_w2_rm = new RM_Weight(sum2var_p_w_d_p_d_q,sum_p_w_d_p_d_q,expansionTerms);
		RM3LanguageModel sum2var_p_w_d_p_d_q_w2_rm3 = new RM3LanguageModel(qLM,sum2var_p_w_d_p_d_q_w2_rm,queryAnchor);
		sum2var_p_w_d_p_d_q_w2_rm3.print_res(reader,searcher,parser,qid, out, param, "sum2var_p_w_d_p_d_q_w2");	
		
		RM_Weight df_var_p_w_d_p_d_q_w2_rm = new RM_Weight(df_var_p_w_d_p_d_q,sum_p_w_d_p_d_q,expansionTerms);
		RM3LanguageModel df_var_p_w_d_p_d_q_w2_rm3 = new RM3LanguageModel(qLM,df_var_p_w_d_p_d_q_w2_rm,queryAnchor);
		df_var_p_w_d_p_d_q_w2_rm3.print_res(reader,searcher,parser, qid, out, param, "df_var_p_w_d_p_d_q_w2");
		
		RM_Weight df_sum2var_p_w_d_p_d_q_w2_rm = new RM_Weight(df_sum2var_p_w_d_p_d_q,sum_p_w_d_p_d_q,expansionTerms);
		RM3LanguageModel df_sum2var_p_w_d_p_d_q_w2_rm3 = new RM3LanguageModel(qLM,df_sum2var_p_w_d_p_d_q_w2_rm,queryAnchor);
		df_sum2var_p_w_d_p_d_q_w2_rm3.print_res(reader,searcher,parser, qid, out, param, "df_sum2var_p_w_d_p_d_q_w2");*/
	}
}


