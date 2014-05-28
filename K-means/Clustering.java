package edu.asu.irs13;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
//import java.util.Random;

import java.util.Random;

import org.apache.lucene.index.Term;

public class Clustering {
	public static HashMap<Integer, HashMap<Integer,Double>> clustering(int top_50[],
			HashMap<Integer, HashMap<Term, Double>> docTermMatrix,
			HashMap<Integer, Double> twonorm, int k) {

		// Generate three random numbers to set them as centroids
		Random ran = new Random();

		// Declare HashMap for storing cluster numbers and document ids
		HashMap<Integer, HashMap<Integer, Double>> clusters = new HashMap<Integer, HashMap<Integer, Double>>();

		// Declare HashMap for storing duplicate cluster numbers and doc ids
		// which is used for convergence
		HashMap<Integer, HashMap<Integer, Double>> old_clusters = new HashMap<Integer, HashMap<Integer, Double>>();

		// Declare ArrayList for storing 3 centroid vectors
		ArrayList<HashMap<Term, Double>> centroid_vector = new ArrayList<HashMap<Term, Double>>();

		// Get initial centroid vectors
		for (int i = 0; i < 50; i++) {
			int a = ran.nextInt(50);
			//int a = i;
			centroid_vector.add(docTermMatrix.get(top_50[a]));
		}

		// double sim_final[] = new double[50];

		// Calculate similarity values
		
		int count = 0;
		while (true) {
			count ++;
			old_clusters = new HashMap<Integer, HashMap<Integer, Double>>();
			if(count!=1) {
				// Copy new cluster values to old cluster values from second iteration
				Iterator<Integer> itr_new = clusters.keySet().iterator();
				while(itr_new.hasNext()) {
					Integer key = (Integer)itr_new.next();
					old_clusters.put(key, clusters.get(key));
				}
			}
			
			clusters = new HashMap<Integer, HashMap<Integer, Double>>();
			
			for (int i = 0; i < 50; i++) {
				double sim = 0;
				int cluster_number = 0;
				for (int j = 0; j < k; j++) {
					if (sim < similarity(top_50[i], centroid_vector.get(j),
							docTermMatrix, twonorm)) {
						sim = similarity(top_50[i], centroid_vector.get(j),
								docTermMatrix, twonorm);
						//System.out.println(" Cluster number" + j);
						cluster_number = j;
					}
				}
				HashMap<Integer, Double> inner_hm = new HashMap<Integer, Double>();
				if (clusters.containsKey(cluster_number)) {
					inner_hm = clusters.get(cluster_number);
				}
				inner_hm.put(top_50[i], sim);
				clusters.put(cluster_number, inner_hm);
			}

			Iterator<Integer> itr = clusters.keySet().iterator();

			// Calculate new centroid vectors
			centroid_vector = new ArrayList<HashMap<Term, Double>>();
			//int cluster = 0 ;
			while (itr.hasNext()) {
				//cluster++;
				HashMap<Term, Double> centroidVec = new HashMap<Term, Double>();
				Integer key = (Integer) itr.next();
				//System.out.println("Cluster number =" + key);
				Iterator<Integer> itr1 = clusters.get(key).keySet().iterator();
				while (itr1.hasNext()) {
					HashMap<Term, Double> hm = docTermMatrix.get((Integer) itr1
							.next());
					Iterator<Term> itr2 = hm.keySet().iterator();
					while (itr2.hasNext()) {
						Term t = (Term) itr2.next();
						if (centroidVec.containsKey(t)) {
							centroidVec.put(t, centroidVec.get(t) + hm.get(t));
						} else {
							centroidVec.put(t, hm.get(t));
						}
					}
					
				}
				Iterator<Term> itr3 = centroidVec.keySet().iterator();
				while(itr3.hasNext()) {
					Term key3 = (Term)itr3.next();
					Double val3 = centroidVec.get(key3)/(double)centroidVec.size();
					centroidVec.put(key3, val3);
				}
				//System.out.println("Test");
				centroid_vector.add(centroidVec);
			}
			
			//Compare old cluster and new cluster to see if the document ids have changed in the clusters
			Iterator<Integer> itr_new_comp = clusters.keySet().iterator();
			
			//To break the loop

			if (count > 1) {
				int flag = 0;
				while (itr_new_comp.hasNext()) {
					Integer key1 = (Integer) itr_new_comp.next();
					if (!clusters.get(key1).keySet()
							.equals(old_clusters.get(key1).keySet())) {
						flag = 1;
						break;
					}
				}
				if (flag == 0) {
					break;
				}
			}
		}
		return clusters;
	}

	public static double similarity(int d1, HashMap<Term, Double> doc2,
			HashMap<Integer, HashMap<Term, Double>> docTermMatrix,
			HashMap<Integer, Double> docTermMod) {
		HashMap<Term, Double> doc1 = docTermMatrix.get(d1);

		Iterator<Term> itr1 = doc1.keySet().iterator();
		Iterator<Term> itr2 = doc2.keySet().iterator();

		double modCentroid = 0;
		while (itr2.hasNext()) {
			Term t = (Term) itr2.next();
			modCentroid += doc2.get(t) * doc2.get(t);
		}

		//Iterator<Term> itr3 = doc2.keySet().iterator();
		double sum = 0;
		while (itr1.hasNext()) {
			Term key1 = (Term) itr1.next();
			if(doc2.containsKey(key1)) {
				sum = sum + (doc1.get(key1) * doc2.get(key1));
			}
		}
		return sum / (Math.sqrt(docTermMod.get(d1)) * Math.sqrt(modCentroid));
	}
}
