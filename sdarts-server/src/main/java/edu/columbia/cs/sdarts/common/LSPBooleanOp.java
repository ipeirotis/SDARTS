
package edu.columbia.cs.sdarts.common;

import java.io.IOException;

import edu.columbia.cs.sdarts.util.XMLWriter;

/**
 * Represents a "boolean-op" as defined in the STARTS 1.0 specification.
 * Boolean operators are used inside of rankings
 * ({@link edu.columbia.cs.sdarts.common.LSPRanking LSPRanking})
 * and filters
 * ({@link edu.columbia.cs.sdarts.common.LSPFilter LSPFilter}).
 * <p>
 * A boolean operator can be one
 * of three types:
 * <ul>
 * <li><code>AND</code>
 * <li><code>OR</code>
 * <li><code>AND_NOT</code>
 * </ul>
 * It can either be instaniate by the type code, or by name
 * ("and", "or", "and-not").
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class LSPBooleanOp extends LSPObject {
    // ------------ FIELDS ------------
    public static final int AND     = 0;
    public static final int OR      = 1;
    public static final int AND_NOT = 2;
    public int type;
    public String name;


    // ------------ FIELDS ------------
    /**
     * Instantiate an <code>LSPBooleanOp</code> by name
     * @param name either "and", "or", "and-not"
     */
    public LSPBooleanOp (String name) {
        if (name.equals ("and")) {
          type = AND;
        }
        else if (name.equals ("or")) {
          type = OR;
        }
        else if (name.equals ("and-not")) {
          type = AND_NOT;
        }
        else {
          throw new IllegalArgumentException ("illegal booleanop: " + name);
        }
        this.name = name;
    }

    /**
     * Instantiate an <code>LSPBooleanOp</code> by code
     * @param name either <code>AND</code>, <code>OR</code>, <code>AND_NOT</code>
     */
    public LSPBooleanOp (int code) {
      switch (code) {
        case AND:
          name = "and";
        break;

        case OR:
          name = "or";
        break;

        case AND_NOT:
          name = "and-not";
        break;

        default:
          throw new IllegalArgumentException ("illegal booleanop: " + code);
      }

      this.type = code;
    }

    /**
     * @return the type of the <code>LSPBooleanOp</code>: either
     * <code>AND</code>, <code>OR</code>, or <code>AND_NOT</code>.
     */
    public int getType () {
      return type;
    }

    public void toXML (XMLWriter writer) throws IOException {
      writer.setDefaultFormat();
      writer.enterNamespace (STARTS.NAMESPACE_NAME);
      writer.printStartElement ("boolean-op", true);
      writer.printAttribute ("name", name);
      writer.printEmptyElementClose ();
      writer.exitNamespace();
    }
}




