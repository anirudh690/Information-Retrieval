package edu.asu.irs13;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;

import Jama.Matrix;

import java.io.File;
//import java.io.FileNotFoundException;
import java.util.Map.Entry;
import java.util.*;
import java.lang.Math;

public class pageRankGlobal {
	public static void main(String[] args) throws Exception {

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
				HashMap<String, HashMap<Integer, Float>> termfreq = new HashMap<String, HashMap<Integer, Float>>();
				HashMap<Integer, Double> sim = new HashMap<Integer, Double>();
				String[] terms = str.split("\\s+");
				// System.out.println("Relavant document id's are as follows :");

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

				//auth_hub_calc ah = new auth_hub_calc();

				// Calculating the root set
				//ArrayList<Integer> top_ten_docs = new ArrayList<Integer>();
				
				System.out.println("++++++Top 10 TF/IDF docs++++++");

				while (tree.hasNext() && i++ < 10) {
					Integer key = (Integer) tree.next();
					//top_ten_docs.add(key);

					// float value = sim_new.get(key);
					// System.out.println("document - id :" + key +
					// " similarity " +
					// value);
					 System.out.println(key);
				}

				// Calling function to calculate base set
				//ArrayList<Integer> base_set = ah.getBaseSet(top_ten_docs);
				
				//System.out.println("Base set :" + base_set);

				// Calculating adjacency matrix
				//Matrix adjacencyMatrix = ah.getAdjacencyMatrix(base_set,
				//		top_ten_docs);
				//double deter = adjacencyMatrix.det();
				//System.out.println("Det value is = " + deter);

				// Calculating the transpose of adjacency matrix
				//Matrix adjacencyTranspose = adjacencyMatrix.transpose();
				
				//Page Rank calculation starts
				
				LinkAnalysis.numDocs = 25054;
				LinkAnalysis l = new LinkAnalysis();
				HashMap<Integer,int[]> cit_map = new HashMap<Integer, int[]>();
				HashMap<Integer, Integer> sink = new HashMap<Integer, Integer>();
				
				int link_size, numDocs=25054;
				int linksLen[] = new int[numDocs];
				
				//Calculate citations hashmap, links length array and sink node hashmap
				
				for(int doc=0; doc<numDocs; doc++) {
					
					//get Links length
					link_size = l.getLinks(doc).length;
					linksLen[i]=link_size;
					
					//if length is zero, add sink nodes to hashmap
					if(link_size == 0) {
						sink.put(doc, 1);
					}
					
					//calculate citations and add them in hashmap
					
					int cit[] = l.getCitations(doc);
					cit_map.put(doc, cit);
				}
				
				
				//Declare Rank_new and Rank_old arrays of numDocs size
				Matrix rank_new = new Matrix(numDocs, 1);
				Matrix rank_old;
				double m_doc[] = new double[numDocs];;
				double s = 1.0/numDocs;
				
				//Declare matrices
				//Matrix newRank;
				//Matrix oldRank;
				
				//Initialize c
				double c = 0.8;
				
				//Initialize ranks to 1/numDocs
				for(int doc=0;doc<numDocs;doc++)
				{
					rank_new.set(doc, 0,s);
	
				}
				
				//Calculate sinkNode values
				double Z = 0;
				for(int doc:sink.keySet())
				{
					Z = Z + rank_new.get(doc,0);				
				}
				
				Z = Z/numDocs;
				
				//Initialize Reset Matrix value
				double K = 1/numDocs;
				
				System.out.println("Starting power iteration");
				
				int count=0;
				//Start power iteration
				while(true && count < 20) {
					rank_old = new Matrix(numDocs,1);
					rank_old = rank_new.copy();
					
					m_doc = new double[numDocs];
					//Calculate M value for each doc
					for(int doc=0; doc<numDocs; doc++) {
						int cit[] = cit_map.get(doc);
						double m_val = 0;
						for(int arr : cit) {
							m_val = m_val + rank_old.get(arr, 0) / linksLen[arr]; 
						}
						m_doc[doc] = m_val;
					}
					
					//Calculate M_star
					for(int doc=0; doc<numDocs; doc++) {
						rank_new.set(doc, 0, (c * (m_doc[doc] + Z) + (1-c)*K));
					}
					
					double two_norm = rank_new.norm2();
					for(int doc=0; doc<numDocs; doc++) {
						rank_new.set(doc, 0, rank_new.get(doc, 0)/two_norm);
					}
					
					
					//Convert arrays to matrices
					//newRank = new Matrix(rank_new);
					//oldRank = new Matrix(rank_old);
					
					//calculate difference between two matrices, i.e values should differ by 0.0002
					//double difference = (rank_new.minus(rank_old)).norm1();
					for(int row=0; row<numDocs; row++) {
						if(Math.abs(rank_new.get(row, 0) - rank_old.get(row,0)) < 0.0002) 
							break;
					}
					
					count++;
					System.out.println(count);
				} //end of power iteration
				
				/*
				double max = rank_new.get(0, 0);
				double min = rank_new.get(0, 0);
				for(int doc=0;doc<numDocs;doc++)
				{
					if(rank_new.get(i, 0) < min)
					{
						min = rank_new.get(i, 0);
					}
					if(rank_new.get(i, 0) > max)
					{
						max = rank_new.get(i, 0);
					}
				}
				for(int doc=0;doc<numDocs;doc++)
				{
					rank_new.set(i, 0, (rank_new.get(i, 0)-min)/(max - min));
				}
				*/
				
				//Add similarity vector with page rank results
				
				HashMap<Integer, Double> pg = new HashMap<Integer, Double>(); 
				double sim_final = 0;
				Iterator<Integer> sim_itr = sim_new.keySet().iterator();
				while(sim_itr.hasNext()) {
					Integer key = (Integer)sim_itr.next();
					for(int row=0; row<numDocs; row++) {
						if(row==key) {
							sim_final = 0.4*(rank_new.get(row, 0)) + 0.6*(sim_new.get(key));
							pg.put(key, sim_final);
						}
					}
				}
				
				HashMap<Integer, Double> sort_final = (HashMap<Integer, Double>) sortHashMap(pg);
				System.out.println("Top 10 page ranks");
				int value_final = 0;
				
				Iterator<Integer> print_doc = sort_final.keySet().iterator();
				
				while(print_doc.hasNext() && value_final < 10) {
					value_final ++;
					Integer key = (Integer)print_doc.next();
					System.out.println(key);
				}
				
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