package Index;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
//import java.lang.reflect.Constructor;
import java.util.Properties;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
/*import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;*/
import org.apache.lucene.benchmark.byTask.feeds.DocData;
import org.apache.lucene.benchmark.byTask.feeds.NoMoreDataException;
import org.apache.lucene.benchmark.byTask.feeds.TrecContentSource;
import org.apache.lucene.benchmark.byTask.utils.Config;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.FieldInfo.IndexOptions;
//import org.apache.lucene.search.similarities.LMDirichletSimilarity;
//import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Index {

	public static void main(String[] args) throws Exception{
		
		/*if (args.length < 6){
			System.err.println("Expected input: <propFile> <indexFolder> <stemming> <stopping> <similarityClass> <similarityParam>");
			System.exit(1);
		}*/
		
		//first parameter - collection property file
		String propFile = args[0];
		String indexDir = args[1];
		//Boolean stemming = Boolean.valueOf(args[2]);
		//Boolean stopping = Boolean.valueOf(args[3]);
		//String similarityClass = args[4];
		//String similarityParam = args[5];
		
		//loading trecContentSource properties file
		TrecContentSource tcs = new TrecContentSource();
		Properties prop = new Properties();
		try{
			InputStream input = new FileInputStream(new File(propFile));
			prop.load(input);
		}catch(IOException e){
			System.err.print("problem with properties file");
			e.printStackTrace();
			System.exit(1);
		}
        
		//setting properties
		Config config = new Config(prop);
		tcs.setConfig(config);
		
		//opening index directory. initializing the proper analyser.
		FSDirectory dir = FSDirectory.open(new File(indexDir));
		Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_48);
			//IndexUtils.getAnalyzer(stemming,stopping);
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_48,analyzer);
	    //Similarity  sim = Class.forName(similarityClass).asSubclass(Similarity.class).newInstance();
		//iwc.setSimilarity(sim);
		IndexWriter writer = new IndexWriter(dir, iwc);
		
		FieldType idType = new FieldType();
		idType.setIndexed(true);
		idType.setStored(true);
		idType.setTokenized(false);
		
		FieldType contentType;
		contentType = new FieldType();
		contentType.setIndexed(true);
		contentType.setStored(true);
		contentType.setTokenized(true);
		contentType.setStoreTermVectors(true);
		//contentType.setStoreTermVectorOffsets(true);
		//contentType.setStoreTermVectorPositions(true);
		
		contentType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
		
		int count = 0;
		DocData docData = new DocData();
		Boolean notFinished = true;
		BufferedWriter bw = new BufferedWriter(new FileWriter("problematicFiles.txt"));
		while(notFinished){
			try{
				docData = tcs.getNextDocData(docData);
				Document indexedDocument = new Document();
				Field docID = new Field("docno",docData.getName(),idType);
				indexedDocument.add(docID);
				
				Field content = new Field("contents",docData.getBody(),contentType);
				indexedDocument.add(content);
				writer.addDocument(indexedDocument);
				count = count +1;
				if((count % 10)==0){
					System.out.println("processed " + count + " docs");
				}
			}catch(NoMoreDataException e){
				writer.forceMerge(1);
				writer.close();
				System.out.println("terminated");
				System.out.println("processed " + count + " files");
				notFinished = false;
			}
			catch(Exception e){
				bw.write(docData.getID() + "\n");
			}
		}
		bw.close();
		tcs.close();
	}
}
