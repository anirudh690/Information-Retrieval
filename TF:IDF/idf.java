package edu.asu.irs13;

import org.apache.commons.lang3.time.StopWatch;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;

import java.io.File;
import java.io.IOException;
//import java.io.FileNotFoundException;
import java.util.Map.Entry;
import java.util.*;
import java.lang.Math;
import java.nio.file.Files;
import java.nio.file.Paths;

public class searchEngine_idf{
	public static void main(String[] args) throws Exception {
		
		try {
		// the IndexReader object is the main handle that will give you
		// all the documents, terms and inverted index
		IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));

		// You can figure out the number of documents using the maxDoc()
		// function
		// System.out.println("The number of documents in this index is: " +
		// r.maxDoc());

		// HashMap which contains the document-id and the maximum occurrences of
		// any word in that doc
		HashMap<Integer, Integer> maxCountHm = new HashMap<Integer, Integer>();
		HashMap<Integer, Double> twonorm = new HashMap<Integer, Double>();

		
		StopWatch sw1 = new StopWatch();
		sw1.start();
		
		// int i = 0;
		// int k=0;
		// You can find out all the terms that have been indexed using the
		// terms() function
		// TermEnum is used to iterate over the inverted index
		TermEnum t = r.terms();
		while (t.next()) {
			// TermDocs is used to iterate over all the docs list for the word
			// in inverted index
			TermDocs td = r.termDocs(t.term());

			while (td.next()) {
				// twonorm.put(td.doc(), )
				if (!(maxCountHm.containsKey(td.doc()))) {
					maxCountHm.put(td.doc(), td.freq());
				} else if (maxCountHm.containsKey(td.doc())
						&& maxCountHm.get(td.doc()) < td.freq()) {
					maxCountHm.put(td.doc(), td.freq());
				}
				// k++;
			}
			// System.out.println(t.term().text());
			// i++;
			// System.out.println("Total number of words in this inverted index is : "
			// + i);
		}
		
		sw1.stop();
		
		//System.out.println("Time taken to calculate max word count in tf/idf based approach =" + (double)sw1.getNanoTime()/1000000000 + " seconds");
		//Calculating idf's of every word in the inverted index
		
		HashMap<String, Double> word_idf = new HashMap<String, Double>();
		
		TermEnum te = r.terms();
		while(te.next()) 
		{
			double idf = Math.log10((float)r.maxDoc() / r.docFreq(te.term())) ;
			word_idf.put(te.term().text(), idf);	
		}
		
		//HashMap<String, Double> w_idf = (HashMap<String, Double>) sortIdf(word_idf);
		
		
		//Iterating hashmap of idf's
//		Iterator<String> itr_idf = word_idf.keySet().iterator();
//		while(itr_idf.hasNext())
//		{
//			String word = (String)itr_idf.next();
//			double idf = (double) word_idf.get(word);
//			System.out.println(word + " = " + idf);
//		}
		
		//Iterating through sorted idf's hashmap
		
//		Iterator<String> itr_idf = w_idf.keySet().iterator();
//		while(itr_idf.hasNext())
//		{
//			String word = (String)itr_idf.next();
//			double idf = (double) w_idf.get(word);
//			System.out.println(word + " = " + idf);
//		}

		
		

		
		sw1.reset();
		
		sw1.start();
		
		// Calculating mod-d
		
		TermEnum tenum = r.terms();
		while (tenum.next()) {
			// TermDocs is used to iterate over all the docs list for the word
			// in inverted index
			TermDocs tdocs = r.termDocs(tenum.term());
			double idf = (double)word_idf.get(tenum.term().text());
			while (tdocs.next()) {
				int count = tdocs.freq();
				float tf = (float) count / maxCountHm.get(tdocs.doc());
				// System.out.println(tdocs.doc() + " = " + tf);
				double modd = tf * tf * idf * idf;
				if (!twonorm.containsKey(tdocs.doc())) {
					twonorm.put(tdocs.doc(), modd);
				}

				else if (twonorm.containsKey(tdocs.doc())) {
					twonorm.put(tdocs.doc(), twonorm.get(tdocs.doc()) + modd);
				}
			}
		}
		
		sw1.stop();
		
		System.out.println("Time taken to calculate two-norm in tf/idf based approach =" + (double)sw1.getNanoTime()/1000000000 + " seconds");

		//Iterating through the modd data structure

//		 Iterator <Entry<Integer, Float>> itr_modd =
//		 twonorm.entrySet().iterator();
//		 while(itr_modd.hasNext())
//		 {
//		 Map.Entry<Integer, Float> entries = (Map.Entry<Integer,
//		 Float>)itr_modd.next();
//		 System.out.println(entries.getKey() + " = " + entries.getValue());
//		 }

		// Iterator <Entry<Integer, Integer>> itr =
		// maxCountHm.entrySet().iterator();
		// while(itr.hasNext())
		// {
		// Map.Entry<Integer, Integer> entries = (Map.Entry<Integer,
		// Integer>)itr.next();
		// System.out.println(entries.getKey() + " = " + entries.getValue());
		// itr.remove();
		// }

		// System.out.println("Total number of words in this inverted index is : "
		// + i);
		// System.out.println("K value is :" + k);

		// -------- Now let us use all of the functions above to make something
		// useful --------
		// The following bit of code is a worked out example of how to get a
		// bunch of documents
		// in response to a query and show them (without ranking them according
		// to TF/IDF)
		Scanner sc = new Scanner(System.in);
		String str = "";
		System.out.print("query> ");

		while (!(str = sc.nextLine()).equals("quit")) {
			HashMap<String, HashMap<Integer, Float>> termfreq = new HashMap<String, HashMap<Integer, Float>>();
			HashMap<Integer, Double> sim = new HashMap<Integer, Double>();
			String[] terms = str.split("\\s+");
			System.out.println("Relavant document id's are as follows :");
			
			sw1.reset();
			sw1.start();

			
			for (String word : terms) {
				Term term = new Term("contents", word);
				TermDocs tdocs = r.termDocs(term);
				HashMap<Integer, Float> tmp1 = new HashMap<Integer, Float>();
				while (tdocs.next()) {
					float tf = (float) (tdocs.freq())
							/ (maxCountHm.get(tdocs.doc()));
					tmp1.put(tdocs.doc(), tf);
				}
				termfreq.put(word, tmp1);
			}

			Iterator<String> itr = termfreq.keySet().iterator();

			while (itr.hasNext()) {
				String outer_key = itr.next();
				// Map<Integer, Float> map = termfreq.get(outer_key);
				Iterator<Integer> itr1 = termfreq.get(outer_key).keySet()
						.iterator();
				while (itr1.hasNext()) {
					// System.out.println("In similarities loop");
					double similarity;
					int docid = itr1.next();
					double idf = word_idf.get(outer_key);
					Float tfvalue = termfreq.get(outer_key).get(docid);
					similarity = (float) ((tfvalue*idf) / (Math
							.sqrt((double) twonorm.get(docid))));
					if (!sim.containsKey(docid)) {
						sim.put(docid, similarity);
					} else if (sim.containsKey(docid)) {
						sim.put(docid, (sim.get(docid) + similarity));
					}
					// System.out.println(outer_key + "  " + docid + "  " +
					// tfvalue);
				}
			}
			sw1.stop();
			
			System.out.println("Time taken to obtain the results =" + (double)sw1.getNanoTime()/1000000000 + " seconds");
		
			
			sw1.reset();
			sw1.start();
			
			HashMap<Integer, Double> sim_new = (HashMap<Integer, Double>) sortHashMap(sim);
			sw1.stop();
			
			//System.out.println("Time taken to sort the documents = " + (double)sw1.getNanoTime()/1000000000 + " seconds");
			
			if(sim_new.isEmpty())
			{
				System.out.println("The results for this keyword does not exist in this set of documents !");
			}
			
			int i = 0;
			Iterator<Integer> tree = sim_new.keySet().iterator();

			while (tree.hasNext() && i++ < 10) {
				Integer key = (Integer) tree.next();
				// float value = sim_new.get(key);
				// System.out.println("document - id :" + key + " similarity " +
				// value);
				System.out.println(key);
				Document d = r.document(key);
				String url = d.getFieldable("path").stringValue(); // the 'path' field of the Document object holds the URL
				System.out.println(url.replace("%%", "/"));
				String doc_path = "/Users/Anirudh/Dropbox/ASU/Sem-2/IR/IR-Assignment-3/Projectclass/result3/";
				String final_path = doc_path + url;
				String file_content = readFile(final_path);
				//System.out.println(file_content);
				ArrayList<String> html_parser = htmlParser(file_content, str);
				if(html_parser.size() != 0)
					System.out.print(html_parser.get(0));
				//getSnippet(html_parser, str);
				
			}
			// sim_new.clear();
			// sim.clear();
			// termfreq.clear();

			System.out.print("query> ");
		}
		sc.close();
	}
		catch (Exception e)
		{
						e.printStackTrace();
		}
	}

	private static Map<Integer, Double> sortHashMap(
			Map<Integer, Double> unsortedMap) {

		List<Entry<Integer, Double>> list = new LinkedList<Entry<Integer, Double>>(
				unsortedMap.entrySet());

		// Sorting the list based on values
		Collections.sort(list, new Comparator<Entry<Integer, Double>>() {
			public int compare(Entry<Integer, Double> obj1,
					Entry<Integer, Double> obj2) {
				return obj2.getValue().compareTo(obj1.getValue());
			}
		});

		// Maintaining insertion order with the help of LinkedList
		Map<Integer, Double> sortedMap = new LinkedHashMap<Integer, Double>();
		for (Entry<Integer, Double> entry : list) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}
	
	
	static ArrayList<String> htmlParser(String file_content, String query) {
		ArrayList<String> parser = new ArrayList<String>();
		String[] terms = query.split("\\s+");
		org.jsoup.nodes.Document doc = Jsoup.parse(file_content);
		Elements p = doc.select("p");
		for(Element text : p) {
			for(int i=0; i<terms.length; i++) {
				if(text.text().toString().contains(terms[i]))
					parser.add(text.text().toString());
			}
		}
		if(parser.size()<2) {
			for(Element text : p) {
				for(int i=parser.size(); i<2; i++) {
						parser.add(text.text().toString());
				}
			}
		}
		return parser;
	}
	
	static String readFile(String path) 
			  throws IOException 
			{
			  byte[] encoded = Files.readAllBytes(Paths.get(path));
			  return new String(encoded);
			}
	
	public static void getSnippet(ArrayList<String> html_parser, String query) {
		String terms[] = query.split("\\s+");
		for(int i=0; i<html_parser.size(); i++) {
			for(int j=0; j<terms.length; j++) {
				if(html_parser.get(i).contains(terms[j])) {
					int index = html_parser.get(i).indexOf(terms[i]);
					for(int p=0; p<10; p++) {
						//if(html_parser.get(index)) {
							
						}
					}
				}
			}
		}
	

	
	
/*
	private static Map<String, Double> sortIdf(
			Map<String, Double> unsortedMap) {

		List<Entry<String, Double>> list = new LinkedList<Entry<String, Double>>(
				unsortedMap.entrySet());

		// Sorting the list based on values
		Collections.sort(list, new Comparator<Entry<String, Double>>() {
			public int compare(Entry<String, Double> obj1,
					Entry<String, Double> obj2) {
				return obj2.getValue().compareTo(obj1.getValue());
			}
		});

		// Maintaining insertion order with the help of LinkedList
		Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
		for (Entry<String, Double> entry : list) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}
	*/
}
