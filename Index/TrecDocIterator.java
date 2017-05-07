package Index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.FieldInfo.IndexOptions;

public class TrecDocIterator implements Iterator<Document> {

	protected BufferedReader rdr;
	protected boolean at_eof = false;
	
	public TrecDocIterator(File file) throws FileNotFoundException {
		rdr = new BufferedReader(new FileReader(file));
		System.out.println("Reading " + file.toString());
	}
	
	public boolean hasNext() {
		return !at_eof;
	}

	public Document next() {
		Document doc = new Document();
		StringBuffer sb = new StringBuffer();
		try {
			String line;
			Pattern docno_tag = Pattern.compile("<DOCNO>\\s*(\\S+)\\s*<");
			boolean in_doc = false;
			while (true) 
			{
				line = rdr.readLine();
				if (line == null) {
					at_eof = true;
					break;
				}
				if (!in_doc) {
					if (line.startsWith("<DOC>"))
						in_doc = true;
					//else
						continue;
				}
				if (line.startsWith("</DOC>")) {
					in_doc = false;
					//sb.append(line);
					break;
				}

				Matcher m = docno_tag.matcher(line);
				if (m.find()) {
					String docno = m.group(1);
					doc.add(new StringField("docno", docno, Field.Store.YES));
					continue;
				}

				sb.append(line+"\n");
			}
			if (sb.length() > 0)
			{
				System.out.println(sb.toString());
				
				FieldType ft = new FieldType();
				ft.setIndexed(true);
				ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
				ft.setStored(true);
				ft.setStoreTermVectors(true);
				ft.setTokenized(true); 
				//ft.setStoreTermVectorPositions(true);
				doc.add(new Field("contents", sb.toString(), ft)); 
				//doc.add(new TextField("contents", sb.toString(), Field.Store.NO));
			}
		} catch (IOException e) {
			doc = null;
		}
		return doc;
	}

	@Override
	public void remove() {
		// Do nothing, but don't complain
	}

}
