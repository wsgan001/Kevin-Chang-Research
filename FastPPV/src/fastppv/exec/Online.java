package fastppv.exec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import fastppv.core.QueryProcessor;
import fastppv.data.Graph;
import fastppv.data.Node;
import fastppv.util.Config;
import fastppv.util.KeyValuePair;
import fastppv.util.io.TextReader;
import fastppv.util.io.TextWriter;


public class Online {

    public static void main(String[] args) throws Exception {
    	Config.hubType = args[0];
    	Config.numHubs = Integer.parseInt(args[1]);
    	Config.eta = Integer.parseInt(args[2]);    	
    	
        Graph graph = new Graph();
        graph.loadFromFile(Config.nodeFile, Config.edgeFile, true);

        System.out.println("Loading queries...");
        List<Node> qNodes = new ArrayList<Node>();
        // TextReader in = new TextReader(Config.queryFile);
        TextReader in = new TextReader(Config.nodeFile);
        String line;
        int num_node = Integer.parseInt(in.readln());
        char[] nodetype = new char[num_node];
        while ( (line = in.readln()) != null) {
            String[] split = line.split(" ");
            nodetype[Integer.parseInt(split[0])] = split[1].charAt(0);
            if (!split[1].equals("R"))
                continue;
        	int id = Integer.parseInt(split[0]);
            // int id = Integer.parseInt(line);
        	qNodes.add(graph.getNode(id));
        }
        in.close();
        
        System.out.println("Starting query processing...");
        
        QueryProcessor qp = new QueryProcessor(graph); //for community-based implementation; for the basic method, use  QueryProcessor qp = new QueryProcessor(graph);
       
        TextWriter out = new TextWriter(Config.outputDir + "/" + 
        		"fastppv-" + Config.hubType + "_" + Config.numHubs + "_" + Config.eta);
       
        int count = 0;
        String outbuffer = "";
        for (Node q : qNodes) {
            if (nodetype[q.id] != 'R')
                continue;
            count++;
            List<KeyValuePair> rankedResult = null;
            long start = System.currentTimeMillis();
            rankedResult = qp.query(q).getTopResult(Config.resultTop);
            long elapsed = (System.currentTimeMillis() - start);

            for (KeyValuePair e : rankedResult) {
                if (nodetype[e.key] == 'P')
                    outbuffer += (q.id + " " + e.key + " " + (int) e.value * Math.pow(10, 6) + "\n");
            }
            if (count % 10 == 0) {
                out.write(outbuffer);
                outbuffer = "";
                System.out.println(elapsed + "ms ");
            }
        }
        out.close();

        System.out.println();
    }

}
