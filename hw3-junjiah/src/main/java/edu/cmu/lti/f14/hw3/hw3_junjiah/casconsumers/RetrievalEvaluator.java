package edu.cmu.lti.f14.hw3.hw3_junjiah.casconsumers;

import edu.cmu.lti.f14.hw3.hw3_junjiah.typesystems.Document;
import edu.cmu.lti.f14.hw3.hw3_junjiah.utils.QueryProcessor;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import java.io.IOException;

/**
 * All operations on documents & queries are encapsulated in a {@link QueryProcessor}.
 */
public class RetrievalEvaluator extends CasConsumer_ImplBase {

  /**
   * Initialize the query processor.
   * @throws ResourceInitializationException
   */
  @Override
  public void initialize() throws ResourceInitializationException {
    queryProcessor = new QueryProcessor();
  }

  /**
   * Feed the document to the query processor.
   */
  @Override
  public void processCas(CAS aCas) throws ResourceProcessException {

    JCas jcas;
    try {
      jcas = aCas.getJCas();
    } catch (CASException e) {
      throw new ResourceProcessException(e);
    }

    FSIterator it = jcas.getAnnotationIndex(Document.type).iterator();

    if (it.hasNext()) {
      Document doc = (Document) it.next();
      queryProcessor.processDocument(doc);
    }

  }

  /**
   * Generate report from the query processor.
   */
  @Override
  public void collectionProcessComplete(ProcessTrace arg0) throws ResourceProcessException,
          IOException {

    super.collectionProcessComplete(arg0);
    queryProcessor.generateReport();
  }

  /**
   * A query processor accepts {@link edu.cmu.lti.f14.hw3.hw3_junjiah.typesystems.Document} and
   * saves necessary information to process ranking, sorting, etc. And finally generates the ranking
   * information of relevant documents.
   */
  private QueryProcessor queryProcessor;
}
