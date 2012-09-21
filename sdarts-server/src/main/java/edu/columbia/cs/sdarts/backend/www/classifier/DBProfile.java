package edu.columbia.cs.sdarts.backend.www.classifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;


public class DBProfile {

	private TreeSet terms;
	private String DBname;
	//private boolean isCategory;
	//private DBProfile[] childrenProfiles;

	public DBProfile() {
		this.terms = new TreeSet();
		this.DBname = new String();
//		this.isCategory = false;
	}

	public DBProfile(DBProfile db) {
		this.terms = new TreeSet();
		this.terms.addAll(db.terms);
		this.DBname = new String();
//		this.isCategory = false;
	}

//	public DBProfile(DBProfile[] dbs) {
//		// This defines a "category" profile
//		this.childrenProfiles = dbs;
//		this.terms = new TreeSet();
//		for (int i=0; i< dbs.length; i++) {
//			this.mergeWith(dbs[i]);
//		}
//		this.DBname = new String();
//		this.isCategory = true;
//	}

//	public boolean isCategory() {
//		return isCategory;
//	}

//	public DBProfile[] getChildrenProfiles() {
//		return this.childrenProfiles;
//	}

//	public int getNumOfDBs() {
//		if (!this.isCategory()) {
//			return 1;
//		}
//		int result=0;
//		for (int i=0; i < childrenProfiles.length; i++) {
//			result+=childrenProfiles[i].getNumOfDBs();
//		}
//		return result;
//	}

	public TreeSet getEntries() {
		return this.terms;
	}

	public String getName() {
		return this.DBname;
	}

	public void setName(String name) {
		this.DBname = new String(name);
	}


	private	ProfileEntry getEntry(String term) {
		ProfileEntry pe = new ProfileEntry(term);
		if (terms.contains(pe)) {
			return (ProfileEntry) terms.tailSet(pe).first();
		} else {
			return null;
		}
	}

	private	void removeEntry(String term) {
		ProfileEntry pe = new ProfileEntry(term);
		if (terms.contains(pe)) {
			this.terms.remove( (ProfileEntry) terms.tailSet(pe).first() );
		}

	}

	private void syncToCompare(DBProfile dbprofile) {

		TreeSet words = this.getWords();
		for (Iterator it=words.iterator(); it.hasNext();) {
			String term = (String)it.next();

			if (!dbprofile.contains(term)) {
				this.removeEntry(term);
			}
		}
	}



    public boolean contains(String term) {
        if (this.getEntry(term)!=null)
        	return true;
        else
        	return false;
    }

	public void addTerm(String term, double of, double rf) {
		terms.add(new ProfileEntry(term, of, rf));
    }

	public void addTerm(String term) {
		terms.add(new ProfileEntry(term));
	}

	public void mergeWith(DBProfile db) {
		TermSet dbTerms = db.getWords();
		for (Iterator it = dbTerms.iterator(); it.hasNext(); ) {
			String s = (String)it.next();
			double rf = db.getDF(s,true);
			double of = db.getDF(s,false);
			double currentof = this.getDF(s,false);

			if (this.contains(s)) {
				if (!this.hasRF(s) && db.hasRF(s)) {
					this.setRF(s,rf);
				}
				this.setOF(s,currentof+of);
			} else {
				this.addTerm(s,of,rf);
			}
		}
	}

	public void setRFs(DBProfile db) {
		TermSet dbTerms = db.getWords();
		for (Iterator it = dbTerms.iterator(); it.hasNext(); ) {
			String s = (String)it.next();
			double rf = db.getDF(s,true);

			if (this.contains(s)) {
				if (!this.hasRF(s) && db.hasRF(s)) {
					this.setRF(s,rf);
				}
				//this.setOF(s,currentof+of);
			}
		}
	}

/*
	public void equalize() {
		for (Iterator it = terms.iterator(); it.hasNext(); ) {
			String s = ((ProfileEntry)it.next()).getTerm();
			if (!this.hasRF(s)) {
				this.setRF(s,this.getDF(s,false));
			}
		}
	}
*/
	public void clean() {
		for (Iterator it = terms.iterator(); it.hasNext(); ) {
			String s = ((ProfileEntry)it.next()).getTerm();
			if (!this.hasRF(s)) {
				it.remove();
			}
		}
	}
	
	public void removeLowProbTerms(double ts, boolean useRF) {

		double N = this.getEstimatedDBSize(useRF);
		for (Iterator it = terms.iterator(); it.hasNext(); ) {
			String s = ((ProfileEntry)it.next()).getTerm();
			double p = this.getDF(s,useRF)/N;
			if (p<ts) {
				it.remove();
			}
		}
	}
		
	public void removeLowFreqTerms(double frequency) {
		
		
		for (Iterator it = terms.iterator(); it.hasNext(); ) {
			String s = ((ProfileEntry)it.next()).getTerm();
			double of = this.getDF(s,false);			
			double rf = this.getDF(s,true);
			if (Math.round(of)<frequency) {
				this.setOF(s,0.0);
				//System.out.println("Set OF=0 for t="+s);
			}
			if (Math.round(rf)<frequency) {
				this.setRF(s,0.0);
				//System.out.println("Set RF=0 for t="+s);
			}
		}
	}
			



	

	public boolean hasRF(String term) {
		ProfileEntry pe = this.getEntry(term);
		if (pe==null) return false;
		if (pe.getRF()==-1)
			return false;
		else
			return true;
    }

	public void setRF(String term, double rf) {
		if (rf<0) rf=0;
		ProfileEntry pe = this.getEntry(term);
		if (pe!=null) pe.setRF(rf);
    }

	public void setOF(String term, double of) {
		if (of<0) of=0;
		ProfileEntry pe = this.getEntry(term);
		if (pe!=null) pe.setOF(of);
    }

	public void increaseOF(String term) {
		ProfileEntry pe = this.getEntry(term);
		if (pe!=null) pe.increaseOF();
    }

    public double getDF(String term, boolean normalize) {

		ProfileEntry pe = this.getEntry(term);
		if (pe==null) {
			return 0;
		}
		if (normalize) {
			return  pe.getRF();
		} else {
			return pe.getOF();
		}
    }

    public TreeSet getTerms() {
		return this.terms;
	}


	public double[] getCoverageVector(Classifier cl) {
		double[] cov = new double[cl.getNumberOfCategories()];

		for (int i=0; i<cl.getNumberOfRules()-1; i++) {
			String query = cl.getRule(i).getString();
			String category = cl.getRule(i).getCategory();
			int k = cl.resolveCategory(category);
			double fr = this.getDF(query, true);
			cov[k] += fr;
		}
		return cov;
	}



    public void loadFromFile(File profile) {
		this.terms = new TreeSet();

		String linein;
		try {
			BufferedReader br = new BufferedReader(new FileReader(profile));
			while ( (linein = br.readLine() ) != null) {
				StringTokenizer st = new StringTokenizer(linein, "#");
				String term = st.nextToken();
				double of = Double.parseDouble(st.nextToken());
				double rf = Double.parseDouble(st.nextToken());
				if (Double.isNaN(of) || Double.isInfinite(of) || of<0) of=0.0;
				if (Double.isNaN(rf) || Double.isInfinite(rf) || rf<0) rf=0.0;				
				ProfileEntry pe = new ProfileEntry(term,of,rf);
				terms.add(pe);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    public void writeToFile(File profile) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(profile));

			for (Iterator it = terms.iterator(); it.hasNext(); ) {
				ProfileEntry pe = (ProfileEntry)it.next();
				bw.write(pe.toString());
				bw.newLine();
			}
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }


	public class Result {
		public int T;
		public Result() {}
	}

	// This method returns a DBProfile with the ranks instead of the OFs.
	// It also accepts as a parameter a dummy containter object to pass
	// back the two values needed for the calculation of Spearman coefficient
	// The sum(f(k)^3-f(k)) and n^3-n
    public DBProfile getRank(Result r) {

		TreeSet rank = new TreeSet();
		for (Iterator it = terms.iterator(); it.hasNext(); ) {
			ProfileEntry pe1 = (ProfileEntry)it.next();
			ProfileEntry pe2 = new ProfileEntry() {

				public int compareTo(Object o) {
					ProfileEntry pe = null;

					try {
						pe = (ProfileEntry) o;
					}
					catch (Exception e) {
						throw new ClassCastException("Trying to compare a ProfileEntry with a non-compatible class") ;
					}

					if (this.getOF()  > pe.getOF() ) {
						return -1;
					} else {
						return 1;
					}

				}
			};
			pe2.set(pe1);
			rank.add(pe2);
		}


		int runningRank = 0;
		double runningCount = 1.0;
		int currentRank = 0;
		double runningOF = -1;

		r.T=0;

		DBProfile result = new DBProfile();
		TreeSet temp = new TreeSet();
		for (Iterator it = rank.iterator(); it.hasNext(); ) {
			ProfileEntry pe = (ProfileEntry)it.next();
			currentRank++;
			if (pe.getOF()!=runningOF) {
				r.T += runningCount*runningCount*runningCount-runningCount;
				for (Iterator it2 = temp.iterator(); it2.hasNext(); ) {
					ProfileEntry pe2 = (ProfileEntry)it2.next();
					result.addTerm(pe2.getTerm(), (runningRank+1.0) + (runningCount-1.0)/2 , -1);
				}
				temp = new TreeSet();

				//System.out.println("For rank="+runningRank+" -> "+runningCount+" words");
				runningCount=1;
				runningRank=currentRank;
				runningOF=pe.getOF();

			} else {
				runningCount++;
			}
			temp.add(pe);
			//result.addTerm(pe.getTerm(), runningRank, -1);

		}
		r.T += runningCount*runningCount*runningCount-runningCount;
		for (Iterator it2 = temp.iterator(); it2.hasNext(); ) {
			ProfileEntry pe2 = (ProfileEntry)it2.next();
			result.addTerm(pe2.getTerm(), (runningRank+1) + (runningCount-1)/2 , -1);
		}


		return result;

    }

/*
    public void writeRankToFile(File profile) {

		try {
			TreeSet rank = new TreeSet();
			for (Iterator it = terms.iterator(); it.hasNext(); ) {
				ProfileEntry pe1 = (ProfileEntry)it.next();
				ProfileEntry pe2 = new ProfileEntry() {

					public int compareTo(Object o) {
						ProfileEntry pe = null;

						try {
							pe = (ProfileEntry) o;
						}
						catch (Exception e) {
							throw new ClassCastException("Trying to compare a ProfileEntry with a non-compatible class") ;
						}

						if (this.getOF()  > pe.getOF() ) {
							return -1;
						} else {
							return 1;
						}

					}
				};
				pe2.set(pe1);
				rank.add(pe2);
			}

			BufferedWriter bw = new BufferedWriter(new FileWriter(profile));
			bw.write("x\ty\tz");
			bw.newLine();
			int runningRank = 0;
			int currentRank = 0;
			double runningOF = -1;
			TreeSet temp = new TreeSet();
			for (Iterator it = rank.iterator(); it.hasNext(); ) {
				ProfileEntry pe = (ProfileEntry)it.next();
				currentRank++;
				if (pe.getOF()!=runningOF) {

					runningRank=currentRank;
					runningOF=pe.getOF();
				}
				if (pe.getRF() != 0) {
				//	pe.setOF(runningRank);
					String rf = (pe.getRF()==-1)?"NA":""+pe.getRF();
					String of = (pe.getOF()==-1)?"NA":""+pe.getOF();
					bw.write(runningRank+"\t"+rf+"\t"+of);
					bw.newLine();
				}
			}
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
*/

/*
    public TreeSet getWordSortedOnRank() {

		TreeSet rank = new TreeSet();
		for (Iterator it = terms.iterator(); it.hasNext(); ) {
			ProfileEntry pe1 = (ProfileEntry)it.next();
			ProfileEntry pe2 = new ProfileEntry() {

				public int compareTo(Object o) {
					ProfileEntry pe = null;

					try {
						pe = (ProfileEntry) o;
					}
					catch (Exception e) {
						throw new ClassCastException("Trying to compare a ProfileEntry with a non-compatible class") ;
					}

					if (this.getOF()  > pe.getOF() ) {
						return -1;
					} else {
						return 1;
					}

				}
			};
			pe2.set(pe1);
			rank.add(pe2);
		}

		return rank;

    }
*/

    public int getEstimatedDBSize(boolean normalized) {
		double max=0;
		for (Iterator it = terms.iterator(); it.hasNext(); ) {
			ProfileEntry pe = (ProfileEntry)it.next();
			if (normalized)
				max = (pe.getRF()>max)?pe.getRF():max;
			else
				max = (pe.getOF()>max)?pe.getOF():max;
		}
		return (int)(max);

    }


    public TermSet getWords() {
        TermSet result = new TermSet();
		for (Iterator it = terms.iterator(); it.hasNext(); ) {
			ProfileEntry pe = (ProfileEntry)it.next();
			result.add(pe.getTerm());
		}
		return result;
    }


/*
	public double normalize(double a, double b, double c) {
		// TOFIX: Implement the normalization
		// using Mandelbrot's formula. See NLP book p.25
		// This procedure will be normalizing the RFs of
		// the terms
		DBProfile rank = this.getRank(new Result());

		if (a==0) {
			TreeSet t = this.getWordSortedOnRank();
			// Find the first term with a real frequency
			// and based on its ranking, estimate A
			for (Iterator it = t.iterator(); it.hasNext(); ) {
				String term = ((ProfileEntry)it.next()).getTerm();
				if (hasRF(term) && this.getDF(term,true)!=0) {
					int r = (int)rank.getDF(term,false); // get Ranking
					int f = (int)this.getDF(term,true); // get Ranking
					a = r*f;
					//System.out.println("A="+a+",F="+f+",R="+r);
					break;
				}
			}

		}



		for (Iterator it = terms.iterator(); it.hasNext(); ) {
			String term = ((ProfileEntry)it.next()).getTerm();
			if (!hasRF(term)) {
				int r = (int)rank.getDF(term,false); // get Ranking
				if (r+c<=0) c=0;
				int f = (int)(a*Math.pow( r+c, -b))+1; //estimate frequency
				this.setRF(term,f);
			}
		}

		return a;

	}

*/



	public DBProfile getNormalizedContentSummary() {

		DBProfile rank = this.getRank(new Result());

		Vector xs = new Vector();
		Vector ys = new Vector();

		for (Iterator it = terms.iterator(); it.hasNext(); ) {
			String term = ((ProfileEntry)it.next()).getTerm();
			if (hasRF(term)) {
				double r = rank.getDF(term,false); // get Rank
				double f = this.getDF(term,true); // get Frequency
				if (f<1.0) continue;
				r = Math.log(r); //natural log
				f = Math.log(f);

				xs.add(new Double(r));
				ys.add(new Double(f));
			}
		}

		double[] x = new double[xs.size()];
		double[] y = new double[ys.size()];
		for (int i=0; i<x.length; i++) {
			x[i] = ((Double)xs.get(i)).doubleValue();
			y[i] = ((Double)ys.get(i)).doubleValue();
		}

		// The frequency will be equal to log(f) = coef[0] + coef[1]*log(r)
		// f = (e^coef[0]) * r ^ coef[1]
  		double[] coef = VisualNumerics.math.Statistics.linearFit(x, y);


		DBProfile newCS = new DBProfile();

		for (Iterator it = terms.iterator(); it.hasNext(); ) {
			String term = ((ProfileEntry)it.next()).getTerm();
			double freq = 0.0;
			double obsfreq = this.getDF(term,false);
			if (!hasRF(term)) {
				double r = rank.getDF(term,false);

				freq = (int)Math.round(Math.exp(coef[0]) * Math.pow(r, coef[1]))+1;

			} else {
				freq = this.getDF(term,true);
			}
			newCS.addTerm(term,obsfreq,freq);
		}

		return newCS;

	}


	public double[] getCoefficientsFromSample() {

		DBProfile rank = this.getRank(new Result());

		Vector xs = new Vector();
		Vector ys = new Vector();

		for (Iterator it = terms.iterator(); it.hasNext(); ) {
			String term = ((ProfileEntry)it.next()).getTerm();
			//if (hasRF(term)) {
				double r = rank.getDF(term,false); // get Rank in Sample
				double f = this.getDF(term,false); // get Sample Frequency
				if (f<1.0) continue;
				r = Math.log(r); //natural log
				f = Math.log(f);

				xs.add(new Double(r));
				ys.add(new Double(f));
				//System.out.println("W="+term+"\tF="+f+"\tR="+r);
			//}
		}

		double[] x = new double[xs.size()];
		double[] y = new double[ys.size()];
		for (int i=0; i<x.length; i++) {
			x[i] = ((Double)xs.get(i)).doubleValue();
			y[i] = ((Double)ys.get(i)).doubleValue();
		}

		// The frequency will be equal to log(f) = coef[0] + coef[1]*log(r)
		// f = (e^coef[0]) * r ^ coef[1]
		double[] coef = VisualNumerics.math.Statistics.linearFit(x, y);


		return coef;

	}


    public int getTotalWords() {
        return terms.size();
    }

	public double getTotalOFreqs() {
		double n=0;

		for (Iterator it=terms.iterator(); it.hasNext(); ) {
			n += ((ProfileEntry)it.next()).getOF();
		}

		return n;
	}


/////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////
// Below we have only functions to evaluate the quality of content
// summaries
/////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////



	public double getNonWeightedPrecision(DBProfile correct, boolean usingRF) {
		TermSet cw = correct.getWords();
		
		int a=0, b=0;
		for (Iterator it = this.getWords().iterator(); it.hasNext();) {
			String term = (String)it.next(); 
			double f = this.getDF(term,usingRF);
			if (f>0) {
				a++; 
				if (cw.contains(term)) b++;			 
			}
		}
		return (double)b/(double)a;
	}

	public double getNonWeightedRecall(DBProfile correct, boolean usingRF) {
		TermSet cw = correct.getWords();
		
		int a=0, b=0;
		for (Iterator it = cw.iterator(); it.hasNext();) {
			String term = (String)it.next(); 
			double f = correct.getDF(term,usingRF);
			if (f>0) {
				a++; 
				if ( this.getDF(term,usingRF) >0) b++;			 
			}
		}

		return (double)b/(double)a;
	}



	public double getNormCoverage(DBProfile correct) {
		TermSet cw = correct.getWords();
		int correctSize =0, coverage=0;

		for (Iterator it=cw.iterator(); it.hasNext(); ) {
			String term = (String)it.next();
			correctSize += (int)correct.getDF(term,false);
		}

		for (Iterator it=terms.iterator(); it.hasNext(); ) {
			String term = ((ProfileEntry)it.next()).getTerm();
			coverage += (int)correct.getDF(term,false);
		}

		return (double)coverage/(double)correctSize;
	}


	public double getPrecision(DBProfile correct, boolean usingRF) {
		TermSet cw = correct.getWords();
		double approxSize =0, common=0;

		for (Iterator it=terms.iterator(); it.hasNext(); ) {
			String term = ((ProfileEntry)it.next()).getTerm();
			approxSize += this.getDF(term,usingRF);
		}
		
		if (approxSize==0) return 1.0d;
		

		for (Iterator it=cw.iterator(); it.hasNext(); ) {
			String term = (String)it.next();
			double cf = this.getDF(term,usingRF);
			if (cf>0) {
				common += cf;
			}
		}

		return  common/approxSize;
	}




	public double getRecall(DBProfile correct, boolean usingRF) {
		TermSet cw = correct.getWords();
		double correctSize =0, common =0;

		for (Iterator it=cw.iterator(); it.hasNext(); ) {
			String term = (String)it.next();
			correctSize += correct.getDF(term,usingRF);
		}

		if (correctSize==0) return 1.0d;

		for (Iterator it=terms.iterator(); it.hasNext(); ) {
			String term = ((ProfileEntry)it.next()).getTerm();
			if (correct.contains(term)) {
				common += correct.getDF(term,usingRF);
			}
		}

		return common/correctSize;
	}


	public double getKL(DBProfile correct, boolean usingRF) {
		TermSet cw = correct.getWords();
		
		double kl = 0;

		double Ncor = 0;
		for (Iterator it=cw.iterator(); it.hasNext(); ) {
			String term = (String)it.next();
			
			double q = this.getDF(term,usingRF);
			double p = correct.getDF(term,usingRF);			
			if (p>0 && q>0) {	
				Ncor += p;
			}
		}
		
		if (Ncor==0) return 0;

		double Napp = 0;
		for (Iterator it=this.getWords().iterator(); it.hasNext(); ) {
			String term = (String)it.next();
			double q = this.getDF(term,usingRF);
			double p = correct.getDF(term,usingRF);			
			if (p>0 && q>0) {				
				Napp += q;
			}
		}

		if (Napp==0) return 0;

		for (Iterator it=cw.iterator(); it.hasNext(); ) {
			String term = (String)it.next();
			double q = this.getDF(term,usingRF) / Napp;
			double p = correct.getDF(term,usingRF) / Ncor;
			
			if (p>0 && q>0) {
				kl += p * Math.log(p/q)/Math.log(2);					
			}
		}

		return kl;
	}


	public double getKL(DBProfile correct, DBProfile simple, boolean usingRF) {
		TermSet cw = correct.getWords();
		
		double kl = 0;

		double Ncor = 0;
		for (Iterator it=cw.iterator(); it.hasNext(); ) {
			String term = (String)it.next();
			
			double q = this.getDF(term,usingRF);
			double p = correct.getDF(term,usingRF);			
			double s = simple.getDF(term,usingRF);			

			if (p>0 && q>0 && s>0) {	
				Ncor += p;
			}
		}
		
		if (Ncor==0) return 0;

		double Napp = 0;
		for (Iterator it=this.getWords().iterator(); it.hasNext(); ) {
			String term = (String)it.next();
			double q = this.getDF(term,usingRF);
			double p = correct.getDF(term,usingRF);			
			double s = simple.getDF(term,usingRF);			

			if (p>0 && q>0 && s>0) {	
				Napp += q;
			}
		}

		if (Napp==0) return 0;

		for (Iterator it=cw.iterator(); it.hasNext(); ) {
			String term = (String)it.next();
			double q = this.getDF(term,usingRF) / Napp;
			double p = correct.getDF(term,usingRF) / Ncor;
		
			double s = simple.getDF(term,usingRF);			

			if (p>0 && q>0 && s>0) {	
				kl += p * Math.log(p/q)/Math.log(2);					
			}
		}

		return kl;
	}


	public static double[] getAllKL(DBProfile A, DBProfile S, DBProfile C) {
		// A: approximate
		// S: simple approximate (not shrunk)
		// C: complete
		double[] result = new double[8];

		TermSet cw = C.getWords();
		
		double NcRF=0, NcOF=0, NaOF=0, NaRF=0, NcsRF=0, NcsOF=0, NasRF=0, NasOF=0;;
		
		for (Iterator it=cw.iterator(); it.hasNext(); ) {
			String term = (String)it.next();
			
			double arf = A.getDF(term,true);
			double aof = A.getDF(term,false);			
			
			double srf = S.getDF(term,true);
			double sof = S.getDF(term,false);			

			double crf = C.getDF(term,true);
			double cof = crf;

			if (arf>0 && crf>0) {	
				NcRF += crf;
				NaRF += arf;				
			}
			if (aof>0 && cof>0) {	
				NcOF += cof;
				NaOF += aof;				
			}			
			if (arf>0 && crf>0 && srf>0) {	
				NcsRF += crf;
				NasRF += arf;				
			}
			if (aof>0 && cof>0 && sof>0) {	
				NcsOF += cof;
				NasOF += aof;				
			}			
		}


		for (Iterator it=cw.iterator(); it.hasNext(); ) {
			String term = (String)it.next();
			
			double arf = A.getDF(term,true);
			double aof = A.getDF(term,false);			
			
			double srf = S.getDF(term,true);
			double sof = S.getDF(term,false);			

			double crf = C.getDF(term,true);
			double cof = crf;		

			if (arf>0 && crf>0 && NaRF>0 && NcRF>0) {
				double p = arf / NaRF;
				double q = crf / NcRF;				
				result[0] += p * Math.log(p/q)/Math.log(2);
				result[1] += q * Math.log(q/p)/Math.log(2);				
			}
			
			
			if (aof>0 && cof>0 && NaOF>0 && NcOF>0) {	
				double p = aof / NaOF;
				double q = cof / NcOF;				
				result[2] += p * Math.log(p/q)/Math.log(2);
				result[3] += q * Math.log(q/p)/Math.log(2);					
			}			
			
			if (arf>0 && crf>0 && srf>0  && NasRF>0 && NcsRF>0) {
				double p = arf / NasRF;
				double q = crf / NcsRF;				
				result[4] += p * Math.log(p/q)/Math.log(2);
				result[5] += q * Math.log(q/p)/Math.log(2);				
			}
			
			
			if (aof>0 && cof>0 && sof>0 && NasOF>0 && NcsOF>0) {	
				double p = aof / NasOF;
				double q = cof / NcsOF;				
				result[6] += p * Math.log(p/q)/Math.log(2);
				result[7] += q * Math.log(q/p)/Math.log(2);					
			}	
		}

		return result;
	}



	public double getAvgError(DBProfile correct) {
		double avgerror=0;
		double est, cor;
		int n=0;

		for (Iterator it=terms.iterator(); it.hasNext(); ) {
			ProfileEntry pe = (ProfileEntry)it.next();
			String term = pe.getTerm();
			est = pe.getRF();
			cor = correct.getDF(term,false);
			 if (cor<3) continue;
			if (cor==0) {
				// Ignore zeros ?
				//if (est!=0) System.out.println("OOPS!");
				n++;
				avgerror = ((n-1.0)*avgerror)/(1.0*n) ;
			} else {
				double err = Math.abs(1.0*(est-cor))/(1.0*cor);
				n++;
				avgerror = ((n-1.0)*avgerror)/(1.0*n) + err/(1.0*n);
				//System.out.println("Term="+term+", err="+err+"%, avg="+avgerror);
				//System.out.println("Est="+est+", Cor="+cor+" N="+n);
			}
		}

		return avgerror;
	}


	public double getNormError(DBProfile correct) {
		double error=0, totalweight=0;
		double est, cor;
		//int n=0;

		double size = correct.getTotalOFreqs();

		for (Iterator it=terms.iterator(); it.hasNext(); ) {
			ProfileEntry pe = (ProfileEntry)it.next();
			String term = pe.getTerm();

			est = pe.getRF();
			cor = correct.getDF(term,false);

			double termWeight = cor/size;

			if (cor!=0) {
				double err = Math.abs(1.0*(est-cor))/(1.0*cor);
				error += termWeight * err;
				totalweight += termWeight;
			}
		}

		return error/totalweight;
	}


	public double getTruncError(DBProfile correct) {
		double error=0, totalweight=0;
		double est, cor;
		//int n=0;

		double size = correct.getTotalOFreqs();

		for (Iterator it=terms.iterator(); it.hasNext(); ) {
			ProfileEntry pe = (ProfileEntry)it.next();
			String term = pe.getTerm();

			est = pe.getRF();
			cor = correct.getDF(term,false);
			if (cor<4) continue;

			double termWeight = cor/size;

			if (cor!=0) {
				double err = Math.abs(1.0*(est-cor))/(1.0*cor);
				error += termWeight * err;
				totalweight += termWeight;
			}
		}

		return error/totalweight;
	}


	public double getSpearman(DBProfile correct) {

		// Keep only the common words
		DBProfile copy1 = new DBProfile(this);
		DBProfile copy2 = new DBProfile(correct);

		copy1.syncToCompare(copy2);
		copy2.syncToCompare(copy1);
//		copy1.writeRankToFile(new File("rank1.txt"));
//		copy2.writeRankToFile(new File("rank2.txt"));

		TermSet common = copy1.getWords();

		Result r1 = new Result();
		copy1 = copy1.getRank(r1);
		Result r2 = new Result();
		copy2 = copy2.getRank(r2);

		double sum = 0;
		for (Iterator it = common.iterator(); it.hasNext();) {
			String t = (String)it.next();
			double rank1 = copy1.getDF(t,false);
			double rank2 = copy2.getDF(t,false);
			double d = rank1 - rank2;
			sum += d*d;
		}

		double N = common.size();
		N = Math.pow(N,3)-N;


		double T1 = (double)r1.T;
		double T2 = (double)r2.T;
		double D = (double) sum;

		double R = (1- (6/N)*(D+ T1/12.0 + T2/12.0) )/(Math.sqrt(1-T1/N)*Math.sqrt(1-T2/N));


		return R;

	}


}




