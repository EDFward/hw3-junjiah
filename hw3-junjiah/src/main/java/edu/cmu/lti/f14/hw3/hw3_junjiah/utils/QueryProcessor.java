package edu.cmu.lti.f14.hw3.hw3_junjiah.utils;

import edu.cmu.lti.f14.hw3.hw3_junjiah.typesystems.Document;
import edu.cmu.lti.f14.hw3.hw3_junjiah.typesystems.Token;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * A query processor needs to process both queries and documents. For each query,
 * group documents of the same query ID then trying to get the rank of the relevant document.
 *
 * @author junjiah
 */
public class QueryProcessor {

  /**
   * A wrapper class for ranked relevant document.
   */
  class RankingEntry {
    public String text;

    public int rank;

    public int queryId;

    public double cosineSimilarity;

    public RankingEntry(String text, int rank, int queryId, double cosineSimilarity) {
      this.text = text;
      this.rank = rank;
      this.queryId = queryId;
      this.cosineSimilarity = cosineSimilarity;
    }
  }

  /**
   * Initialize score list and relevant document ranking information list.
   */
  public QueryProcessor() {
    scores = new ArrayList<Double>();
    queryResults = new ArrayList<RankingEntry>();
  }

  /**
   * Take different actions depending on whether the document is a query or a retrieved document.
   * Basically for each query, store the cosine similarities of following documents and finally
   * extract the rank of the relevant document along with other info, then wrap them to a list.
   *
   * @param document A Document annotation, from which some useful information can be retrieved
   */
  public void processDocument(Document document) {
    // construct the document vector
    ArrayList<Token> tokenList = Utils.fromFSListToCollection(document.getTokenList(), Token.class);
    Map<String, Integer> docVector = new HashMap<String, Integer>(tokenList.size());
    for (Token token : tokenList) {
      docVector.put(token.getText(), token.getFrequency());
    }

    if (document.getQueryID() != currentId) { // encountered a query
      if (currentId != -1) {
        // finish current query, and store desired ranking information to `queryResults`
        Collections.sort(scores, Collections.reverseOrder());
        // assume no ties. record the index and wrap it with other information
        int rank = scores.indexOf(relevantDocumentSimilarity);
        queryResults
                .add(new RankingEntry(relevantDocumentText, rank + 1, currentId, scores.get(rank)));
        // clean some stuff
        scores.clear();
        relevantDocumentText = null;
      }
      // update current processing info
      currentQueryVector = docVector;
      currentId = document.getQueryID();
    } else {
      double cosineSimilarity = computeCosineSimilarity(currentQueryVector, docVector);
      scores.add(cosineSimilarity);
      // if this is the relevant doc (only 1!), save info for later wrapping
      if (document.getRelevanceValue() == 1) {
        relevantDocumentSimilarity = cosineSimilarity;
        relevantDocumentText = document.getText();
      }
    }
  }

  /**
   * Write ranking information to a file comforting the specified format.
   *
   * @throws FileNotFoundException
   * @throws UnsupportedEncodingException
   */
  public void generateReport() throws FileNotFoundException, UnsupportedEncodingException {
    // first sort by query ID
    Collections.sort(queryResults, new Comparator<RankingEntry>() {
      @Override
      public int compare(RankingEntry o1, RankingEntry o2) {
        if (o1.queryId == o2.queryId) {
          return 0;
        } else if (o1.queryId < o2.queryId) {
          return -1;
        } else {
          return 1;
        }
      }
    });

    // then write to a file
    PrintWriter writer = new PrintWriter("report.txt", "UTF-8");
    for (RankingEntry entry : queryResults) {
      String line = String
              .format("cosine=%.4f\trank=%d\tqid=%d\trel=1\t%s", entry.cosineSimilarity, entry.rank,
                      entry.queryId, entry.text);
      writer.println(line);
    }
    writer.println("MRR=" + computeMeanReciprocalRank());
    writer.close();
  }

  /**
   * Compute the cosine similarity between 2 sparse document vector. Note that
   * cosineSimilarity = tfProduct / sqrt(queryNorm) * sqrt(docNorm)
   *
   * @return Cosine similarity
   */
  private double computeCosineSimilarity(Map<String, Integer> queryVector,
          Map<String, Integer> docVector) {
    // cosineSimilarity = tfProduct / sqrt(queryNorm) * sqrt(docNorm)
    int tfProduct = 0, queryNorm = 0, docNorm = 0;

    for (Map.Entry<String, Integer> entry : queryVector.entrySet()) {
      String term = entry.getKey();
      int queryTf = entry.getValue();
      queryNorm += queryTf * queryTf;

      if (docVector.containsKey(term)) {
        int docTf = docVector.get(term);
        tfProduct += docTf * queryTf;
      }
    }

    for (int docTf : docVector.values()) {
      docNorm += docTf * docTf;
    }

    return tfProduct / (Math.sqrt(queryNorm) * Math.sqrt(docNorm));
  }

  /**
   * Compute the mean reciprocal rank.
   *
   * @return Mean Reciprocal Rank
   */
  private double computeMeanReciprocalRank() {
    double MRR = 0.0;

    for (RankingEntry entry : queryResults) {
      MRR += 1.0f / entry.rank;
    }

    return MRR / queryResults.size();
  }

  /**
   * Score list to save cosine similarity of documents with one particular query ID
   */
  private List<Double> scores;

  private Map<String, Integer> currentQueryVector;

  private int currentId = -1;

  /**
   * Saving info about the relevant document for a particular query.
   */
  private double relevantDocumentSimilarity;

  private String relevantDocumentText;

  /**
   * Final results containing the ranks of all relevant documents, along with other information.
   */
  private List<RankingEntry> queryResults;

}
