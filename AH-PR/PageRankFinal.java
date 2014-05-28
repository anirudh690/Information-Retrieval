 package edu.asu.irs13;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;

import java.io.File;
//import java.text.DecimalFormat;
//import java.io.FileNotFoundException;
import java.util.Map.Entry;
import java.util.*;
import java.lang.Math;

public class PageRankFinal {
	public static void main(String[] args) throws Exception {

		try {
			
			//Calculate pageRank
			
			PageRankCalculation pgCalculation = new PageRankCalculation();
			double pageRankResult[][]=pgCalculation.pgCalc();
			
			// the IndexReader object is the main handle that will give you
			// all the documents, terms and inverted index
			IndexReader r = IndexReader.open(FSDirectory
					.open(new File("index")));

			// You can figure out the number of documents using the maxDoc()
			// function
			// System.out.println("The number of documents in this index is: " +
			// r.maxDoc());

			// HashMap which contains the document-id and the maximum
			// occurrences of
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
				// TermDocs is used to iterate over all the docs list for the
				// word
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

			// System.out.println("Time taken to calculate max word count in tf/idf based approach ="
			// + (double)sw1.getNanoTime()/1000000000 + " seconds");
			// Calculating idf's of every word in the inverted index

			HashMap<String, Double> word_idf = new HashMap<String, Double>();

			TermEnum te = r.terms();
			while (te.next()) {
				double idf = Math.log10((float) r.maxDoc()
						/ r.docFreq(te.term()));
				word_idf.put(te.term().text(), idf);
			}

			// HashMap<String, Double> w_idf = (HashMap<String, Double>)
			// sortIdf(word_idf);

			// Iterating hashmap of idf's
			// Iterator<String> itr_idf = word_idf.keySet().iterator();
			// while(itr_idf.hasNext())
			// {
			// String word = (String)itr_idf.next();
			// double idf = (double) word_idf.get(word);
			// System.out.println(word + " = " + idf);
			// }

			// Iterating through sorted idf's hashmap

			// Iterator<String> itr_idf = w_idf.keySet().iterator();
			// while(itr_idf.hasNext())
			// {
			// String word = (String)itr_idf.next();
			// double idf = (double) w_idf.get(word);
			// System.out.println(word + " = " + idf);
			// }

			sw1.reset();

			sw1.start();

			// Calculating mod-d

			TermEnum tenum = r.terms();
			while (tenum.next()) {
				// TermDocs is used to iterate over all the docs list for the
				// word
				// in inverted index
				TermDocs tdocs = r.termDocs(tenum.term());
				double idf = (double) word_idf.get(tenum.term().text());
				while (tdocs.next()) {
					int count = tdocs.freq();
					float tf = (float) count / maxCountHm.get(tdocs.doc());
					// System.out.println(tdocs.doc() + " = " + tf);
					double modd = tf * tf * idf * idf;
					if (!twonorm.containsKey(tdocs.doc())) {
						twonorm.put(tdocs.doc(), modd);
					}

					else if (twonorm.containsKey(tdocs.doc())) {
						twonorm.put(tdocs.doc(), twonorm.get(tdocs.doc())
								+ modd);
					}
				}
			}

			sw1.stop();

			/*
			System.out
					.println("Time taken to calculate two-norm in tf/idf based approach ="
							+ (double) sw1.getNanoTime()
							/ 1000000000
							+ " seconds");
			*/
			// Iterating through the modd data structure

			// Iterator <Entry<Integer, Float>> itr_modd =
			// twonorm.entrySet().iterator();
			// while(itr_modd.hasNext())
			// {
			// Map.Entry<Integer, Float> entries = (Map.Entry<Integer,
			// Float>)itr_modd.next();
			// System.out.println(entries.getKey() + " = " +
			// entries.getValue());
			// }

			// Iterator <Entry<Integer, Integer>> itr =
			// maxCountHm.entrySet().iterator();
			// while(itr.hasNext())
			// {
			// Map.Entry<Integer, Integer> entries = (Map.Entry<Integer,
			// Integer>)itr.next();
			// System.out.println(entries.getKey() + " = " +
			// entries.getValue());
			// itr.remove();
			// }

			// System.out.println("Total number of words in this inverted index is : "
			// + i);
			// System.out.println("K value is :" + k);

			// -------- Now let us use all of the functions above to make
			// something
			// useful --------
			// The following bit of code is a worked out example of how to get a
			// bunch of documents
			// in response to a query and show them (without ranking them
			// according
			// to TF/IDF)
			Scanner sc = new Scanner(System.in);
			String str = "";
			System.out.print("query> ");

			while (!(str = sc.nextLine()).equals("quit")) {
				HashMap<String, HashMap<Integer, Float>> termfreq = new HashMap<String, HashMap<Integer, Float>>();
				HashMap<Integer, Double> sim = new HashMap<Integer, Double>();
				String[] terms = str.split("\\s+");
				//System.out.println("Relavant document id's are as follows :");

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
						similarity = (float) ((tfvalue * idf) / (Math
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
				/*
				System.out.println("Time taken to obtain the results ="
						+ (double) sw1.getNanoTime() / 1000000000 + " seconds");
				*/
				sw1.reset();
				sw1.start();

				HashMap<Integer, Double> sim_new = (HashMap<Integer, Double>) sortHashMap(sim);
				sw1.stop();

				// System.out.println("Time taken to sort the documents = " +
				// (double)sw1.getNanoTime()/1000000000 + " seconds");

				if (sim_new.isEmpty()) {
					System.out
							.println("The results for this keyword does not exist in this set of documents !");
				}

				int count = 0;
				Iterator<Integer> tree = sim_new.keySet().iterator();

				// Defining object to calculate base set
				//auth_hub_calc ah = new auth_hub_calc();

				// Calculating the base set

				// LinkAnalysis l = new LinkAnalysis();

				ArrayList<Integer> top_ten_docs = new ArrayList<Integer>();
				
				//System.out.println("TF/IDF top 10 docs");
				
				while (tree.hasNext() && count++ < 10) {
					Integer key = (Integer) tree.next();
					top_ten_docs.add(key);

					// float value = sim_new.get(key);
					// System.out.println("document - id :" + key +
					// " similarity " +
					// value);
					 //System.out.println(key);
				}
				
				
				
				//Add similarity vector with page rank results
				HashMap<Integer, Double> pg = new HashMap<Integer, Double>(); 
				double sim_final = 0;
				int maxDocs=25054;
				Iterator<Integer> sim_itr = sim_new.keySet().iterator();
				while(sim_itr.hasNext()) {
					Integer key = (Integer)sim_itr.next();
					for(int row=0; row<maxDocs; row++) {
						if(row==key) {
							sim_final = 0.4*(pageRankResult[row][0]) + 0.6*(sim_new.get(key));
							pg.put(key, sim_final);
						}
					}
				}
				
				HashMap<Integer, Double> sort_final = (HashMap<Integer, Double>) sortHashMap(pg);
				System.out.println("Top 10 page ranks");
				int value_final = 0;
				
				Iterator<Integer> print_doc = sort_final.keySet().iterator();
				String format = "%-40s%s%n";
				//String stri = "Page Rank Value";
				//System.out.printf(format, stri);
				
				while(print_doc.hasNext() && value_final < 10) {
					value_final ++;
					Integer key = (Integer)print_doc.next();
					System.out.println(key);
					Document d = r.document(key);
					//String format = "%-40s%s%n";
					String url = d.getFieldable("path").stringValue(); // the 'path' field of the Document object holds the URL
					System.out.printf(format, url.replace("%%", "/") ,sort_final.get(key));
				}

				System.out.print("query> ");
			}
			sc.close();
		} catch (Exception e) {
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

	/*
	 * private static Map<String, Double> sortIdf( Map<String, Double>
	 * unsortedMap) {
	 * 
	 * List<Entry<String, Double>> list = new LinkedList<Entry<String, Double>>(
	 * unsortedMap.entrySet());
	 * 
	 * // Sorting the list based on values Collections.sort(list, new
	 * Comparator<Entry<String, Double>>() { public int compare(Entry<String,
	 * Double> obj1, Entry<String, Double> obj2) { return
	 * obj2.getValue().compareTo(obj1.getValue()); } });
	 * 
	 * // Maintaining insertion order with the help of LinkedList Map<String,
	 * Double> sortedMap = new LinkedHashMap<String, Double>(); for
	 * (Entry<String, Double> entry : list) { sortedMap.put(entry.getKey(),
	 * entry.getValue()); }
	 * 
	 * return sortedMap; }
	 */
}