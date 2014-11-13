package ixa.pipe.ned;

import ixa.kaflib.KAFDocument;
import ixa.kaflib.Entity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.util.List;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class CLI {

    public static void main(String[] args) throws Exception { // throws IOException {
    	
    	Namespace parsedArguments = null;

        // create Argument Parser
        ArgumentParser parser = ArgumentParsers.newArgumentParser(
            "ixa-pipe-ned-1.0.jar").description(
            "ixa-pipe-ned-1.0 is a multilingual Named Entity Disambiguation module "
                + "developed by IXA NLP Group based on DBpedia Spotlight API.\n");

        // specify port
        parser
            .addArgument("-p", "--port")
            .choices("2010","2020","2030","2040","2050","2060")
            .required(true)
            .help(
                "It is REQUIRED to choose a port number. Port numbers are assigned " +
                "alphabetically by language code: de:2010, en:2020, es:2030, fr:2040, it:2050, nl:2060");

        parser.addArgument("-H", "--host").setDefault("http://localhost").help("Choose hostname in which dbpedia-spotlight rest " +
        		"server is being executed; this value defaults to 'http://localhost'");
	parser.addArgument("-e", "--endpoint").setDefault("disambiguate");
	parser.addArgument("-i", "--index").setDefault("none");
	parser.addArgument("-n", "--name").setDefault("none");
        
        /*
         * Parse the command line arguments
         */

        // catch errors and print help
        try {
          parsedArguments = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
          parser.handleError(e);
          System.out
              .println("Run java -jar target/ixa-pipe-ned-1.0.jar -help for details");
          System.exit(1);
        }

        /*
         * Load port and host parameters; host defaults to http://localhost
         */

	String port = parsedArguments.getString("port");
	String host = parsedArguments.getString("host");
	String endpoint = parsedArguments.getString("endpoint");
	String index = parsedArguments.getString("index");
	String hashName = parsedArguments.getString("name");
	
	// Input
	BufferedReader stdInReader = null;
	// Output
	BufferedWriter w = null;
	
	stdInReader = new BufferedReader(new InputStreamReader(System.in,"UTF-8"));
	w = new BufferedWriter(new OutputStreamWriter(System.out,"UTF-8"));
	KAFDocument kaf = KAFDocument.createFromStream(stdInReader);
	
	String lang = kaf.getLang();
	KAFDocument.LinguisticProcessor lp = kaf.addLinguisticProcessor("entities", "ixa-pipe-ned-" + lang, "1.1.0");
	lp.setBeginTimestamp();

	Annotate annotator = new Annotate(index,hashName,lang);

	try {	    
	    List<Entity> entities = kaf.getEntities();
	    if (!entities.isEmpty()){
		annotator.disambiguateNEsToKAF(kaf, host, port, endpoint);
	    }
	}
	catch (Exception e){
	      System.err.println("Disambiguation failed: ");
	      e.printStackTrace();
	}
	finally {
	    lp.setEndTimestamp();
	    w.write(kaf.toString());
	    w.close();
	}
    }

}
