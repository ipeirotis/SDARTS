package edu.columbia.cs.sdarts.backend.www.contentsummarybuilder;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.Arrays;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class WWWContentSummary {
	
	private class TermLetters extends Object
	{
		char[] content;
		
		public TermLetters(String str)
		{
			content = str.toCharArray();
		}

		public int hashCode()
		{
			return String.valueOf(content).hashCode();
		}
		
		public boolean equals(Object o)
		{
			return String.valueOf(content).equals(o.toString());
		}
			
		public String toString()
		{
			return String.valueOf(content);
		}	
	}
	
	private class TermInfo
	{
		long SampleFrequency;
		long TermFrequency;
		
		public TermInfo(long sf, long rf)
		{
			this.SampleFrequency = sf;
			this.TermFrequency = rf; 		
		}
	}
	
	private class objDouble extends Object
	{
		double val;
		
		public objDouble(double value)
		{
			val = value;
		}
		
		public int hashCode()
		{
			return (int)val;
		}
		
		public boolean equals(Object o)
		{
			if (((objDouble)o).val == val)
				return true;
				
			return false;
		}
			
		public String toString()
		{
			return Double.toString(val);
		}
		
		public void setValue(double value)
		{
			val = value;
		}
		
		public void addValue(double value)
		{
			val += value;
		}
	}
	
	private class Coefficients extends Object
	{
		double A;
		double B;
		
		public Coefficients(double one, double two)
		{
			this.A = one;
			this.B = two;
		}
	}
		
	private Hashtable terms;
	private TreeSet common_terms;
	private Hashtable intermediate_results;
	private Hashtable new_intermediate_results;
	private long lastSamplingPoint;
	
	private int sampleSize;
	private int estimatedSize;
	private int estimatedVocabularySize;
	private String DBname;
	
	private double A = 0;
	private double B = 0;
	private Hashtable WordToRankMap = new Hashtable();
	

	public WWWContentSummary() {
		this.terms = new Hashtable();
		this.intermediate_results = new Hashtable();
		this.new_intermediate_results = new Hashtable();
		this.common_terms = new TreeSet();
		this.lastSamplingPoint = 0;
		this.DBname = new String();
		this.sampleSize = 0;
		this.estimatedSize = -1;
		this.estimatedVocabularySize = -1;
		
	}

	public String getName() {
		return this.DBname;
	}

	public void setName(String name) {
		this.DBname = new String(name);
	}

	public boolean contains(String term) {
		if (terms.containsKey(new TermLetters(term))) {
			if (this.getSampleFrequency(term) > 0)
				return true;
			else
				return false;
		} else
			return false;
	}

	public Set getWords() {
		//return new TreeSet(this.termsSF.keySet());
		return this.terms.keySet();
	}

	public void addTerm(String term, boolean bFilterCommonWords) {
		if (bFilterCommonWords && this.common_terms.contains(term))
			return;
			
		this.terms.put(new TermLetters(term), new TermInfo(1, 0));
	}

	public void setSampleFrequency(String term, long sf) {
		((TermInfo)(this.terms.get(new TermLetters(term)))).SampleFrequency = sf;
	}

	public long getSampleFrequency(String term) {
		TermInfo t = ((TermInfo)(this.terms.get(new TermLetters(term))));
		if (t == null)
			return 0;
		
		return t.SampleFrequency;
	}

	public void setTermFrequency(String term, long rf) {
		((TermInfo)(this.terms.get(new TermLetters(term)))).TermFrequency = rf;
	}
	public double getTermFrequency(String term) {
		TermInfo t = ((TermInfo)(this.terms.get(new TermLetters(term))));
		if (t == null)
			return 0;
		
		return t.TermFrequency;
	}

	public void increaseSampleFrequency(String term) {
		((TermInfo)(this.terms.get(new TermLetters(term)))).SampleFrequency ++;
	}
	
	public void increaseSampleSize() {
		this.sampleSize++;
	}
	
	public void increaseSampleTermFrequency(String term) {
		if (this.common_terms.contains(term))
			return;
			
		((TermInfo)(this.terms.get(new TermLetters(term)))).TermFrequency ++;
	}


	/**
	 * @return
	 */
	public int getSampleSize() {
		return sampleSize;
	}
	/**
	 * @param i
	 */
	public void setSampleSize(int i) {
		sampleSize = i;
	}
	/**
	 * @return
	 */
	
	public void DetectCommonWords()
	{
		Enumeration en = terms.keys();
		while (en.hasMoreElements())
		{
			TermLetters key = (TermLetters)en.nextElement();
			if (((TermInfo)(this.terms.get(key))).SampleFrequency == sampleSize)
			{
				common_terms.add(key.toString());
				System.out.println("Removing common word --> " + key.toString());
			}
				
		}
		//for (Iterator iter = terms.)
	}
	
	public void ClearResults()
	{
		terms.clear();
		
		this.sampleSize = 0;
	}
	
	public void GetIntermediateResults()
	{
		if (lastSamplingPoint > this.sampleSize - 20)
			return;
			
		Hashtable spread = new Hashtable();
		
		Enumeration en = terms.keys();
		
		while (en.hasMoreElements())
		{
			TermLetters key = (TermLetters)en.nextElement();
			double log_sf = Math.ceil(Math.log(((TermInfo)(this.terms.get(key))).SampleFrequency) / Math.log(2));
			
			objDouble storable_log_sf = new objDouble(log_sf);
						
			if (spread.containsKey(storable_log_sf))
				((objDouble)spread.get(storable_log_sf)).addValue(1);
			else
				spread.put(storable_log_sf, new objDouble(1));	
			 	
		}
		
		double[] x = new double[spread.size()];
		double[] y = new double[spread.size()];
		
		Set s = spread.keySet();
		
		int cnt = 0;
		for (Iterator iter = s.iterator(); iter.hasNext();)
		{
			objDouble val_x = (objDouble)iter.next();
			x[cnt] = val_x.val;
			y[cnt] = Math.log(((objDouble)spread.get(val_x)).val) / Math.log(2);
			cnt++;
		}
		
		double[] coef = VisualNumerics.math.Statistics.linearFit(x, y);
		
		intermediate_results.put(new objDouble(this.sampleSize), new Coefficients(coef[1], coef[0]));
		
		/**********************************************************************************************/
		double[] SFs = new double[terms.size()];
		en = terms.keys();
		
		cnt = 0;
		while (en.hasMoreElements())
		{
			TermLetters key = (TermLetters)en.nextElement();
			SFs[cnt] = ((TermInfo)(this.terms.get(key))).SampleFrequency;
			
			cnt ++;	
		}
		
		Arrays.sort(SFs);
		
		x = new double[terms.size()];
		y = new double[terms.size()];
		
		int rank_elements = 0;
		double rank_value = SFs[terms.size() - 1];
		int cntr = 0;
		for (int i = terms.size() - 1; i >= 0 ; i --)
		{
			if (rank_value != SFs[i])
			{
				x[cntr] = Math.log(rank_value)/Math.log(2);
				double rank = (double)terms.size() - 1.0 - (double)i - ((double)(rank_elements - 1)) / 2.0;
				y[cntr] = Math.log(rank)/Math.log(2);
				
				cntr ++;
				rank_value = SFs[i];
				rank_elements = 1;	
			}
			else
				rank_elements ++;
		}
		
		if (rank_elements > 1)
		{
			x[cntr] = Math.log(rank_value)/Math.log(2);
			double rank = (double)terms.size() - ((double)(rank_elements - 1)) / 2.0;
			y[cntr] = Math.log(rank)/Math.log(2);
			cntr ++;
		}
		
		double[] x1 = new double[cntr];
		double[] y1 = new double[cntr];
		
		for (int i = 0; i < cntr; i++)
		{
			x1[i] = x[i];
			y1[i] = y[i];
		}
		
		coef = VisualNumerics.math.Statistics.linearFit(y1, x1);
		
		new_intermediate_results.put(new objDouble(this.sampleSize), new Coefficients(coef[1], coef[0]));
		/**********************************************************************************************/
		
		lastSamplingPoint = this.sampleSize;
	}
	
	public void PrintCoefficients()
	{
		Enumeration en = intermediate_results.keys();
		
		System.out.println("log(freq)-log(freq)");
		System.out.println("Sample size" + ", " + "A              " + ", " + "B");
		
		while (en.hasMoreElements())
		{
			objDouble key = (objDouble)en.nextElement();
			
			Coefficients coeffs = (Coefficients)intermediate_results.get(key);
			
			System.out.println(key.val + ",       " + coeffs.A + ", " + coeffs.B);
		}
		/**************************************************************************************/
		System.out.println("log(freq)-log(rank)");
		System.out.println("Sample size" + ", " + "A              " + ", " + "B");
		en = new_intermediate_results.keys();
		while (en.hasMoreElements())
		{
			objDouble key = (objDouble)en.nextElement();
	
			Coefficients coeffs = (Coefficients)new_intermediate_results.get(key);
	
			System.out.println(key.val + ",       " + coeffs.A + ", " + coeffs.B);
		}
		/**************************************************************************************/
	}
	
	public String GetRandomWord(double lowBound, double highBound)
	{	
		while(true)
		{
			long index = Math.round(Math.random() * (double)terms.size());
			
			Enumeration en = terms.keys();
			
			TermLetters key = null;
			int cnt = 0;
			
			while (en.hasMoreElements())
			{
				key = (TermLetters)en.nextElement();
				cnt ++;
				
				if (cnt >= index)
				{
					double sf = ((TermInfo)(this.terms.get(key))).SampleFrequency;
					if (sf >= lowBound && sf <= highBound)
						return key.toString();
				}
			}
		}
	}
	
	private void CalculateCoefficients(double EstimatedDBSize)
	{
		double[] x = new double[new_intermediate_results.size()];
		double[] a = new double[new_intermediate_results.size()];
		double[] b = new double[new_intermediate_results.size()];
		
		Enumeration en = new_intermediate_results.keys();
		
		int cnt = 0;
		while (en.hasMoreElements())
		{
			objDouble key = (objDouble)en.nextElement();
		
			Coefficients coeffs = (Coefficients)new_intermediate_results.get(key);
			
			x[cnt] = Math.log(key.val) / Math.log(2);
			a[cnt] = coeffs.A;
			b[cnt] = coeffs.B;
			
			cnt++;
		}
		
		double[] coef = VisualNumerics.math.Statistics.linearFit(x, a);
		
		this.A = coef[1] * (Math.log(EstimatedDBSize) / Math.log(2)) + coef[0];
		
		coef = VisualNumerics.math.Statistics.linearFit(x, b);
		 
		this.B = coef[1] * (Math.log(EstimatedDBSize) / Math.log(2)) + coef[0];
		
		this.B = Math.pow(2, this.B);
	}
	
	private double CalculateRealFrequency(double SampleFrequency)
	{
		Double val = (Double)WordToRankMap.get(new Double(SampleFrequency));
		return this.B *  Math.pow(val.doubleValue(), this.A) + 1;
	}
	
	private void BuildWordToRankMap()
	{
		double[] SFs = new double[terms.size()];
		
		Enumeration en = terms.keys();
		
		int cnt = 0;
		while (en.hasMoreElements())
		{
			TermLetters key = (TermLetters)en.nextElement();
			SFs[cnt] = ((TermInfo)(this.terms.get(key))).SampleFrequency;
			cnt ++;	
		}

		Arrays.sort(SFs);

		int rank_elements = 0;
		double rank_value = SFs[terms.size() - 1];

		for (int i = terms.size() - 1; i >= 0 ; i --)
		{
			if (rank_value != SFs[i])
			{
				double rank = (double)terms.size() - 1.0 - (double)i - ((double)(rank_elements - 1)) / 2.0;
				
				WordToRankMap.put(new Double(rank_value), new Double(rank));
				
				rank_value = SFs[i];
				rank_elements = 1;	
			}
			else
				rank_elements ++;
		}

		if (rank_elements > 1)
		{
			double rank = (double)terms.size() - ((double)(rank_elements - 1)) / 2.0;
			WordToRankMap.put(new Double(rank_value), new Double(rank));
		}
			
	}
	
	public void WriteContentSummary(String filename, double EstimatedDBSize) throws IOException
	{
		CalculateCoefficients(EstimatedDBSize);
		
		BuildWordToRankMap();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
		
		bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		bw.newLine();		bw.write("<starts:scontent-summary");
		bw.newLine();
		bw.write("xmlns:starts=\"http://sdarts.cs.columbia.edu/STARTS/\"");
		bw.newLine();
		bw.write("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
		bw.newLine();
		bw.write("xsi:schemaLocation=\"http://sdarts.cs.columbia.edu/STARTS/ http://sdarts.cs.columbia.edu/xsd/starts.xsd\"");
		bw.newLine();
		bw.write("version=\"Starts 1.0\"");
		bw.newLine();
		bw.write("stemming=\"false\"");
		bw.newLine();
		bw.write("stopwords=\"false\"");
		bw.newLine();
		bw.write("case-sensitive=\"true\"");
		bw.newLine();
		bw.write("fields=\"false\"");
		bw.newLine();
		bw.write("numdocs=\"" + Long.toString((long)Math.round(EstimatedDBSize)) + "\"");
		bw.newLine();
		bw.write("sample_size=\"" +  this.sampleSize + "\">");
		bw.newLine();
		bw.write("<starts:field-freq-info>");
		bw.newLine();
		bw.write("<starts:field type-set=\"basic1\" name=\"body-of-text\"/>");
		bw.newLine();
		
		Enumeration en = terms.keys();

		while (en.hasMoreElements())
		{
			TermLetters key = (TermLetters)en.nextElement();
			double val = ((TermInfo)(this.terms.get(key))).SampleFrequency;
			double frequency = CalculateRealFrequency(val);
			
			if (frequency > EstimatedDBSize)
				frequency = (long)EstimatedDBSize;
							
			long freq = (long)Math.round(frequency);
			
			long term_frequency = (long)Math.round(frequency * ((TermInfo)(this.terms.get(key))).TermFrequency / ((TermInfo)(this.terms.get(key))).SampleFrequency);
			
			
			bw.write("<starts:term>");
			bw.newLine();
			bw.write("<starts:value>" + key.toString() + "</starts:value>");
			bw.newLine();
			bw.write("</starts:term>");
			bw.newLine();
			bw.write("<starts:term-freq>" + Long.toString(term_frequency) + "</starts:term-freq>");
			bw.newLine();
			bw.write("<starts:doc-freq>" + Long.toString(freq) + "</starts:doc-freq>");
			bw.newLine();
		}
		
		bw.write("</starts:field-freq-info>");
		bw.newLine();		
		bw.write("</starts:scontent-summary>");
		bw.newLine();
		bw.close();	
	}

}
