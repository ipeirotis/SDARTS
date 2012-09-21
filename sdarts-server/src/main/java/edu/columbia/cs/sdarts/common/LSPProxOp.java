
package edu.columbia.cs.sdarts.common;

import java.io.IOException;

import edu.columbia.cs.sdarts.util.XMLWriter;


/**
 * Represents a "prox-op" as defined in the STARTS 1.0 specification.
 * Proximity operators are used inside of rankings
 * ({@link edu.columbia.cs.sdarts.common.LSPRanking LSPRanking})
 * and filters
 * ({@link edu.columbia.cs.sdarts.common.LSPFilter LSPFilter}).
 * <p>A proximity operator has two
 * properties
 * <ul>
 * <li><code>proximity</code>
 * <li><code>wordOrderMatters</code>
 * </ul>
 * Basically, this operator is applied to two other words - in a search
 * language, it evaluates to true if the two words are within the
 * <code>proximity</code> of each other. <code>wordOrderMatters</code>
 * simply says whether this operator is commutative (<code>false</code>) 
 * or not <code>true</code>.
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class LSPProxOp extends LSPObject {
    // ------------ FIELDS ------------
    private int proximity;
    private boolean wordOrderMatters;



    // ------------ METHODS ------------
    /**
     * Create a new proximity operator
     * @param proximity the proximity in the operator
     * @param wordOrderMatters whether the operator is commutative
     */
    public LSPProxOp (int proximity, boolean wordOrderMatters) {
      this.proximity = proximity;
      this.wordOrderMatters = wordOrderMatters;
    }

    // -------- ACCESSORS --------
    /**
     * Returns the proximity in this operator
     * @return the proximity in this operator
     */
    public int getProximity () {
      return proximity;
    }

    /**
     * Returns whether this operator is commutative (<code>false</code>) 
     * or not <code>true</code>.
     * @return whether this operator is commutative (<code>false</code>) 
     * or not <code>true</code>.
     */
    public boolean wordOrderMatters () {
      return wordOrderMatters;
    }

    public void toXML (XMLWriter writer) throws IOException {
      writer.setDefaultFormat();
      writer.enterNamespace(STARTS.NAMESPACE_NAME);
      writer.printStartElement ("prox-op", true);
      writer.printAttribute ("proximity", proximity);
      writer.printAttribute ("word-order-matters", wordOrderMatters);
      writer.printEmptyElementClose();
      writer.exitNamespace();
    }
}
