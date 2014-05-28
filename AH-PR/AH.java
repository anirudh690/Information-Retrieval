package edu.asu.irs13;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;

import Jama.Matrix;

import java.io.File;
//import java.io.FileNotFoundException;
import java.util.Map.Entry;
import java.util.*;
import java.lang.Math;

//import javax.swing.text.Document;

public class AH {
	public static void main(String[] args) throws Exception {
	 // public 
		try {
			// the IndexReader object is the main handle that will give you
			// all the documents, terms and inverted index
			IndexReader r = IndexReader.open(FSDirectory
					.open(new File("index")));

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

			// System.out
			// .println("Time taken to calculate two-norm in tf/idf based approach ="
			// + (double) sw1.getNanoTime()
			// / 1000000000
			// + " seconds");

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
				//while(true) {
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

				// System.out.println("Time taken to obtain the results ="
				// + (double) sw1.getNanoTime() / 1000000000 + " seconds");

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

				int i = 0;
				Iterator<Integer> tree = sim_new.keySet().iterator();

				// Defining object to calculate base set

				auth_hub_calc ah = new auth_hub_calc();

				// Calculating the root set
				ArrayList<Integer> top_ten_docs = new ArrayList<Integer>();

				System.out.println("++++++Top 10 TF/IDF docs++++++");

				while (tree.hasNext() && i++ < 20) {
					Integer key = (Integer) tree.next();
					top_ten_docs.add(key);

					// float value = sim_new.get(key);
					// System.out.println("document - id :" + key +
					// " similarity " +
					// value);
					// System.out.println(key);
				}

				sw1.reset();
				sw1.start();

				// Calling function to calculate base set
				ArrayList<Integer> base_set = ah.getBaseSet(top_ten_docs);

				//sw1.stop();
				
				//System.out.println("Base set size :" + base_set.size());

				//System.out.println("Time taken to obtain the base set ="
				//		+ (double) sw1.getNanoTime() / 1000000000 + " seconds");

				//sw1.reset();
				//sw1.start();

				System.out.println("Base set :" + base_set.size());

				// Calculating adjacency matrix
				Matrix adjacencyMatrix = ah.getAdjacencyMatrix(base_set,
						top_ten_docs);

				//sw1.stop();

				//System.out
				//		.println("Time taken to obtain the adjacency matrix ="
				//				+ (double) sw1.getNanoTime() / 1000000000
				//				+ " seconds");
				// double deter = adjacencyMatrix.det();
				// System.out.println("Det value is = " + deter);

				//sw1.reset();

				//sw1.start();
				// Calculating the transpose of adjacency matrix
				Matrix adjacencyTranspose = adjacencyMatrix.transpose();
				//sw1.stop();
				/*
				System.out
						.println("Time taken to obtain the adjacency matrix transpose ="
								+ (double) sw1.getNanoTime()
								/ 1000000000
								+ " seconds");
				*/
				// Creating hashmaps to store authority and hub values - old and
				// new values are stored

				HashMap<Integer, Double> auth_new = new HashMap<Integer, Double>();
				HashMap<Integer, Double> hub_new = new HashMap<Integer, Double>();

				HashMap<Integer, Double> auth_old = new HashMap<Integer, Double>();
				HashMap<Integer, Double> hub_old = new HashMap<Integer, Double>();

				double val = 1;

				// Initializing both authority and hub values to 1

				for (Integer doc : base_set) {

					if (!auth_new.containsKey(doc)) {
						auth_new.put(doc, val);
					}

					if (!hub_new.containsKey(doc)) {
						hub_new.put(doc, val);
					}

					if (!auth_old.containsKey(doc)) {
						auth_old.put(doc, val);
					}

					if (!hub_old.containsKey(doc)) {
						hub_old.put(doc, val);
					}

				}

				// System.out.println("After setting hashmaps old and new to 1");

				// Power iteration to achieve steady state

				//sw1.reset();
				//sw1.start();

				int times = 0;
				while (true) {
					times++;

					int check_auth = 1;
					int check_hub = 1;

					Iterator<Integer> auth_itr_copy = auth_new.keySet()
							.iterator();

					// Copying new authority values to old authority hashmap

					while (auth_itr_copy.hasNext()) {
						Integer key = (Integer) auth_itr_copy.next();
						auth_old.put(key, auth_new.get(key));
					}

					Iterator<Integer> hub_itr_copy = hub_new.keySet()
							.iterator();

					// Copying new hub values into old hub hashmap

					while (hub_itr_copy.hasNext()) {
						Integer key = (Integer) hub_itr_copy.next();
						hub_old.put(key, hub_new.get(key));
					}

					// Calculating new authority using the iteration formula a1
					// = A'*a0

					for (int x = 0; x < base_set.size(); x++) {
						double value = 0;
						for (int y = 0; y < base_set.size(); y++) {
							value = value + adjacencyTranspose.get(x, y)
									* (hub_old.get(base_set.get(y)));
						}
						auth_new.put(base_set.get(x), value);
					}

					// Normalising the new authority values

					Iterator<Integer> auth_norm = auth_new.keySet().iterator();

					double auth_sum = 0;
					while (auth_norm.hasNext()) {
						Integer key = (Integer) auth_norm.next();
						auth_sum = auth_sum + Math.pow(auth_new.get(key), 2);
					}

					Iterator<Integer> auth_norm_insert = auth_new.keySet()
							.iterator();
					while (auth_norm_insert.hasNext()) {

						Integer key = (Integer) auth_norm_insert.next();
						double new_auth_val = auth_new.get(key)
								/ (Math.sqrt(auth_sum));
						auth_new.put(key, new_auth_val);
					}

					// Calculating new hub values using the iterative formula h1
					// = A*a1

					for (int x = 0; x < base_set.size(); x++) {
						double value = 0;
						for (int y = 0; y < base_set.size(); y++) {
							value = value + adjacencyMatrix.get(x, y)
									* (auth_new.get(base_set.get(y)));
						}
						hub_new.put(base_set.get(x), value);
					}

					// Normalising the new hub values

					Iterator<Integer> hub_norm = hub_new.keySet().iterator();

					double hub_sum = 0;
					while (hub_norm.hasNext()) {
						Integer key = (Integer) hub_norm.next();
						hub_sum = hub_sum + Math.pow(hub_new.get(key), 2);
					}

					Iterator<Integer> hub_norm_insert = hub_new.keySet()
							.iterator();
					while (hub_norm_insert.hasNext()) {

						Integer key = (Integer) hub_norm_insert.next();
						double new_hub_val = hub_new.get(key)
								/ (Math.sqrt(hub_sum));
						hub_new.put(key, new_hub_val);
					}

					// Comparing old and new authority/hub values

					Iterator<Integer> pow_auth_new = auth_new.keySet()
							.iterator();
					// Iterator<Integer> pow_auth_old = auth_old.keySet()
					// .iterator();
					Iterator<Integer> pow_hub_new = hub_new.keySet().iterator();
					// Iterator<Integer> pow_hub_old =
					// hub_old.keySet().iterator();

					// Stops the loop when difference between old and new values
					// is less than 0.0002

					while (pow_auth_new.hasNext()) {
						Integer key1 = (Integer) pow_auth_new.next();
						// Integer key2 = (Integer) pow_auth_old.next();

						if (Math.abs(auth_new.get(key1) - auth_old.get(key1)) > 0.0002) {
							check_auth = 0;
							break;
						}
					}

					while (pow_hub_new.hasNext()) {
						Integer key1 = (Integer) pow_hub_new.next();
						// Integer key2 = (Integer) pow_hub_old.next();

						if (Math.abs(hub_new.get(key1) - hub_old.get(key1)) > 0.0002) {
							check_hub = 0;
							break;
						}
					}

					if (check_auth == 0 || check_hub == 0)
						continue;
					else
						break;

				} // Power iteration ends

				//sw1.stop();

				//System.out
				//		.println("Time taken to complete the power iteration ="
				//				+ (double) sw1.getNanoTime() / 1000000000
				//				+ " seconds");

				// sim_new.clear();
				// sim.clear();
				// termfreq.clear();

				// Sorting the authority and hub values

				//sw1.reset();
				//sw1.start();

				HashMap<Integer, Double> auth_final = (HashMap<Integer, Double>) sortHashMap(auth_new);

				HashMap<Integer, Double> hub_final = (HashMap<Integer, Double>) sortHashMap(hub_new);

				sw1.stop();

				/*
				System.out
						.println("Time taken to sort the authority and hub values ="
								+ (double) sw1.getNanoTime()
								/ 1000000000
								+ " seconds");
				*/
				
				System.out.println("Time taken for giving the results" + + (double) sw1.getNanoTime()
								/ 1000000000
								+ " seconds" );
				
				Iterator<Integer> auth_final_val = auth_final.keySet()
						.iterator();
				Iterator<Integer> hub_final_val = hub_final.keySet().iterator();

				System.out.println("+++++++Top 10 Authorities++++++");

				// Printing the top 10 authority values to console

				int final_auth_count = 0;
				while (auth_final_val.hasNext() && final_auth_count++ < 10) {
					Integer key = (Integer) auth_final_val.next();
					//System.out.println(key);
					Document d = r.document(key);
					String url = d.getFieldable("path").stringValue(); // the 'path' field of the Document object holds the URL
					System.out.println(url.replace("%%", "/"));
					
				}

				System.out.println("+++++++Top 10 Hubs++++++");

				// Printing the top 10 hub values to console

				int final_hub_count = 0;
				while (hub_final_val.hasNext() && final_hub_count++ < 10) {
					Integer key = (Integer) hub_final_val.next();
					//System.out.println(key);
					Document d = r.document(key);
					String url = d.getFieldable("path").stringValue(); // the 'path' field of the Document object holds the URL
					System.out.println(url.replace("%%", "/"));
				}

				// Checking how many times the power iteration took place

				System.out.println("Loop has run for " + times + " times");

				System.out.print("query> ");
			}
			sc.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Function to sort the hashmap based on the values

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

}