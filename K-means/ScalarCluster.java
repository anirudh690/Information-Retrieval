package edu.asu.irs13;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.lucene.index.Term;

public class ScalarCluster {
	public static HashMap<Term, HashMap<Term,Double>> scalarCluster(ArrayList<Term> unique_terms, HashMap<Integer, HashMap<Term, Double>> docTermMatrix, int top_50[]) {
		
		//Get correlation matrix
		HashMap<Term, HashMap<Term, Double>> correlation = new HashMap<Term,HashMap<Term, Double>>();
		
		//Get all similarities of term-term in a hashmap
		HashMap<Term, Double> similar = new HashMap<Term, Double>();
		Iterator<Term> itr1 = unique_terms.iterator();
		while(itr1.hasNext()) {
			HashMap<Term, Double> hm = new HashMap<Term, Double>();
			Term t1 = (Term)itr1.next();
			Iterator<Term> itr2 = unique_terms.iterator();
			while(itr2.hasNext()) {
				double sum = 0;
				Term t2 = (Term)itr2.next();
				for(int i = 0; i < 50 ; i++) {
					if(docTermMatrix.get(top_50[i]).containsKey(t1) && docTermMatrix.get(top_50[i]).containsKey(t2)) {
						sum += (double) ( docTermMatrix.get(top_50[i]).get(t1) * docTermMatrix.get(top_50[i]).get(t2) );
					}
				}
				
				if(t1.toString().equals(t2.toString())) {
					similar.put(t1,sum);
				}
				hm.put(t2, sum);	
			}
			correlation.put(t1, hm);
		}
		
		//HashMap<Term, HashMap<Term,Double>> normalisedCorrelation = new HashMap<Term,HashMap<Term, Double>>();
		
		int count = 0;
		//Normalise the correlation matrix to get Association clusters using the formula Aij = Aij/(Aii+Ajj-Aij)
		Iterator<Term> itr3 = correlation.keySet().iterator();
		while(itr3.hasNext()) {
			Term t1 = (Term)itr3.next();
			HashMap<Term, Double> inner = correlation.get(t1);
			Iterator<Term> itr4 = inner.keySet().iterator();
			while(itr4.hasNext()) {
				Term t2 = (Term)itr4.next();
				double val = (double)(inner.get(t2)/(similar.get(t1)+similar.get(t2)-inner.get(t2)));
				inner.put(t2, val);
				//System.out.println(val);
				if(t1.text().toString().equals(t2.text().toString())) {
					count++;
				}
			}
			correlation.put(t1, inner);
		}
		
		System.out.println(count);
		
		
		System.out.println("Printing normalised cluster values");
		
		Iterator<Term> itr_test3 = correlation.keySet().iterator();
		int test_counter2 = 0;
		while(itr_test3.hasNext() && test_counter2++<1) {
			Term test3 = (Term)itr_test3.next();
			Iterator<Term> itr_test4 = correlation.get(test3).keySet().iterator();
			int counter2 = 0;
			while(itr_test4.hasNext() && counter2++<1) {
				Term test4 = (Term)itr_test4.next();
				double val = (double)correlation.get(test3).get(test4);
				if(test3.text().toString().equals(test4.text().toString())) {
			    System.out.println(test3);
			    System.out.println(test4);
			    System.out.println(val);
				}
			}
		}

		
		//Get Scalar CLusters using the formula Suv = (Au.Av) / (|Au| |Av|)
		
		HashMap<Term, HashMap<Term,Double>> scalarCluster = new HashMap<Term, HashMap<Term, Double>>();
		
		Iterator<Term> itr5 = correlation.keySet().iterator();
		while(itr5.hasNext()) {
			Term t1 = (Term)itr5.next();
			HashMap<Term,Double> hm1 = correlation.get(t1);
			Iterator<Term> itr6 = hm1.keySet().iterator();
			HashMap<Term,Double> inner = new HashMap<Term,Double>();
			while(itr6.hasNext()) {
				Term t2 = (Term)itr6.next();
				double dotProductValue = dotProduct(correlation.get(t1), correlation.get(t2));
				inner.put(t2, dotProductValue);
			}
			scalarCluster.put(t1, inner);
		}
		return scalarCluster;
	}
	public static double dotProduct(HashMap<Term,Double> term1, HashMap<Term,Double> term2) {
		double dotProductValue = 0;
		Iterator<Term> itr1 = term1.keySet().iterator();
		Iterator<Term> itr2 = term2.keySet().iterator();
		double val1 = 0;
		double val2 = 0;
		if(term1.size()==term2.size()) {
			//System.out.println("Test");
			while(itr1.hasNext() && itr2.hasNext()) {
				Term t1 = (Term)itr1.next();
				Term t2 = (Term)itr2.next();
				dotProductValue += (double) (term1.get(t1)*term2.get(t2));
				val1 +=(double) term1.get(t1)*term1.get(t1);
				val2 +=(double) term2.get(t2)*term1.get(t2);
			}
		}
		else {
			System.out.println("Not equal");
		}
		if (val1 > 0 && val2 > 0) {
			return (double) (dotProductValue)
					/ (Math.sqrt(val1) * Math.sqrt(val2));
		}		else 
			return 0;
	}
}
