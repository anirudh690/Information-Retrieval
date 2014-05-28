package edu.asu.irs13;

import org.apache.commons.lang3.time.StopWatch;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;
import org.apache.lucene.document.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map.Entry;
import java.util.*;
import java.lang.Math;

public class searchEngine{
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
		HashMap<Integer, Float> twonorm = new HashMap<Integer, Float>();

		// int i = 0;
		// int k=0;
		// You can find out all the terms that have been indexed using the
		// terms() function
		// TermEnum is used to iterate over the inverted index
		
		StopWatch sw1 = new StopWatch();
		sw1.start();
		
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
		
		//System.out.println("Time taken to calculate maximum word count of any word in a document is = " + (double)sw1.getNanoTime()/1000000000 + " seconds");

		// Calculating mod-d
		
		StopWatch watch = new StopWatch();
		watch.start();

		TermEnum tenum = r.terms();
		while (tenum.next()) {
			// TermDocs is used to iterate over all the docs list for the word
			// in inverted index
			TermDocs tdocs = r.termDocs(tenum.term());

			while (tdocs.next()) {
				int count = tdocs.freq();
				float tf = (float) count / maxCountHm.get(tdocs.doc());
				// System.out.println(tdocs.doc() + " = " + tf);
				float tfsqr = tf * tf;

				if (!twonorm.containsKey(tdocs.doc())) {
					twonorm.put(tdocs.doc(), tfsqr);
				}

				else if (twonorm.containsKey(tdocs.doc())) {
					twonorm.put(tdocs.doc(), twonorm.get(tdocs.doc()) + tfsqr);
				}
			}
		}
		watch.stop();
		System.out.println("Time taken for calculating two-norm in tf-based calculation is = " + (double)watch.getNanoTime()/1000000000 + " seconds");
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
			HashMap<Integer, Float> sim = new HashMap<Integer, Float>();
			String[] terms = str.split("\\s+");
			System.out.println("Relavant document id's are as follows :");
			
			StopWatch sw2 = new StopWatch();
			sw2.start();
			for (String word : terms) {
				Term term = new Term("contents", word);
				TermDocs tdocs = r.termDocs(term);
				HashMap<Integer, Float> tmp1 = new HashMap<Integer, Float>();
				//while (tdocs.next()) {

				while (tdocs.next() && tdocs.doc() < 23000) {
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
					float similarity;
					int docid = itr1.next();
					Float tfvalue = termfreq.get(outer_key).get(docid);
					similarity = (float) ((tfvalue) / (Math
							.sqrt((double) twonorm.get(docid))));
					if (!sim.containsKey(docid)) {
						sim.put(docid, similarity);
					} else if (sim.containsKey(docid)) {
						sim.put(docid, (sim.get(docid) + similarity));
					}
					 //System.out.println(outer_key + "  " + docid + "  " +
					// tfvalue);
				}
			}
			
			sw2.stop();
			
			System.out.println("Time taken to obtain the results = " + (double)sw2.getNanoTime()/1000000000 + " seconds");
			System.out.println("The number of documents containing the query words = " + sim.size());
			StopWatch sw3 = new StopWatch();
			sw3.start();
			HashMap<Integer, Float> sim_new = (HashMap<Integer, Float>) sortHashMap(sim);
			sw3.stop();
			
			//System.out.println("Time taken to sort the relavant documents =" + (double)sw3.getNanoTime()/1000000000 + " seconds");
			if(sim_new.isEmpty())
			{
				System.out.println("The results for this keyword does not exist in this set of documents !");
			}
			
			org.jsoup.nodes.Document doc; 
			
			int i = 0;
			Iterator<Integer> tree = sim_new.keySet().iterator();

			while (tree.hasNext() && i++ < 10) {
				Integer key = (Integer) tree.next();
				// float value = sim_new.get(key);
				// System.out.println("document - id :" + key + " similarity " +
				// value);
				//System.out.println(key);
				org.apache.lucene.document.Document d = r.document(key);
				String url = d.getFieldable("path").stringValue(); // the 'path' field of the Document object holds the URL
				System.out.println(url.replace("%%", "/"));
				String url_modified = "http://" + url.replace("%%", "/");
				doc = Jsoup.connect(url_modified).get();
				//System.out.println("Test");
				//Elements elements = doc.select("div[class=chpstaff] p");
				//String content = elements.first().text();
				//System.out.println(content);
				//Elements elements = doc.select("p");
		        //System.out.println(elements.html());
				Elements paragraphs = doc.select("p");
				for(Element p: paragraphs) {
					System.out.println(p.text());
				}
					
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
		
	
	private static Map<Integer, Float> sortHashMap(
			Map<Integer, Float> unsortedMap) {

		List<Entry<Integer, Float>> list = new LinkedList<Entry<Integer, Float>>(
				unsortedMap.entrySet());

		// Sorting the list based on values
		Collections.sort(list, new Comparator<Entry<Integer, Float>>() {
			public int compare(Entry<Integer, Float> obj1,
					Entry<Integer, Float> obj2) {
				return obj2.getValue().compareTo(obj1.getValue());
			}
		});

		// Maintaining insertion order with the help of LinkedList
		Map<Integer, Float> sortedMap = new LinkedHashMap<Integer, Float>();
		for (Entry<Integer, Float> entry : list) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}
	
}
