
package edu.columbia.cs.sdarts.frontend;

import java.io.File;
import java.net.InetAddress;

import sdlip.helpers.ServerDaslTransport;


/**
 * The class used as the actual SDARTS server process. The
 * {@link edu.columbia.cs.sdarts.frontend.SDARTS#main (String[]) main()} method
 * reads in parameters, usually passed to it by the
 * <code>sdarts.sh</code> script. It creates the
 * {@link edu.columbia.cs.sdarts.frontend.FrontEndLSP FrontEndLSP}, which then
 * initializes itself, and registers the <code>FrontEndLSP</code>
 * with an instance of <code>sdlip.helpers.ServerDaslTransport</code>.
 * <p>
 * The <code>sdarts.sh</code> should also pass in one hidden parameter
 * that the user does not enter: <code>SDARTS_HOME</code>. This value
 * is stored in the public static variable {@link #SDARTS_HOME}. It is the
 * directory where SDARTS is installed, and is used throughout the
 * framework. For one thing, it is used to create {@link #CONFIG_DIRECTORY},
 * another public static variable that the framework uses to locate the
 * <code>/config</code> directory, which is where all configuration files
 * and wrapper configurations are stored.
 * <p>
 * To understand the parameters of the main method, here is a copy of the
 * usage string from the <code>sdarts.sh</code> script:
 * <pre>
 * Usage:
  sdarts.sh -?"
  sdarts.sh [port [lsp_name]]
  echo
    -? - Prints this help message
    [port] - The port where SDARTS shoud listen for requests
             Default is 8080. (optional)
    lsp_name - The name that SDARTS should register itself
               under, for clients to contact it. For example
               if you entered 'lsp1', and were running on
               host 'www.cs.columbia.edu', a client would
               contact SDARTS with the URL:
               'http://www.cs.columbia.edu:8080/lsp1'
 * </pre>
 * This usage string does not show the hidden <code>SDARTS_HOME</code>
 * parameter mentioned above.
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class SDARTS {
    public static final int DEFAULT_PORT = 8080;
    public static String DEFAULT_LSPNAME = "sdarts";
    public static String SDARTS_HOME = ".";
    public static String CONFIG_DIRECTORY;

    /**
     * Starts the server process. See above for parameter descriptions.
     * @param args the parameters for starting the server
     */
    public static void main (String[] args) {
    int port = DEFAULT_PORT;
    String lspName = DEFAULT_LSPNAME;
    String usage = "Expected parameters: [port [lspName [SDARTS_HOME]]]";

    try {
        if (args.length == 3) {
          port = new Integer (args[0]).intValue();
          lspName = args[1];
          SDARTS_HOME = args[2];
        }
        else if (args.length == 2) {
          lspName = args[0];
          SDARTS_HOME = args[1];
        }
        else if (args.length == 1) {
          SDARTS_HOME = args[0];
        }
    }
    catch (Exception e) {
        System.err.println(usage);
        System.exit(1);
    }

      // Set up where to find config files
      CONFIG_DIRECTORY = SDARTS_HOME + File.separator + "config";

      // Boot up the server!
    try {
      SDARTS sdarts = new SDARTS (port, lspName);
    }
    catch (Exception e) {
      System.err.println(usage);
      e.printStackTrace();
      System.exit(1);
    }
    }

    /**
     * Creates a SDARTS server and starts it running
     * @param port the port to listen at
     * @param lspName the name by which the SDARTS server will be known
     * to HTTP/DASL clients
     */
    public SDARTS (int port, String lspName) throws Exception {
    System.out.println ("Creating front end and initializing back ends");
    System.out.println ("--------------------------------------------");
    init (port, lspName, new FrontEndLSP());
    }


    private void init (int port, String lspName, FrontEndLSP frontEnd)
  throws Exception {
    System.out.println ("Initialization successful!");
    System.out.println ("--------------------------------------------");
    lspName = "/"+lspName;
    ServerDaslTransport t1 =
      new ServerDaslTransport (port, lspName, frontEnd);
    System.out.println ("SDARTS LSP online!");
    System.out.println ("Using HTTP/DASL transport layer");

    String hostName = InetAddress.getLocalHost().getHostName();
    System.out.println ("Server available at: ");
    System.out.println ("http://"+hostName+":"+port+lspName);
    //t1.setDBG(new DBG (DBG.VERBOSE));
    }
}
