package edu.cmu.lti.f14.hw3.hw3_junjiah.annotators;

import edu.cmu.lti.f14.hw3.hw3_junjiah.typesystems.Document;
import edu.cmu.lti.f14.hw3.hw3_junjiah.typesystems.Token;
import edu.cmu.lti.f14.hw3.hw3_junjiah.utils.Utils;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.*;

public class DocumentVectorAnnotator extends JCasAnnotator_ImplBase {

  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {

    FSIterator<Annotation> iter = jcas.getAnnotationIndex().iterator();
    if (iter.isValid()) {
      iter.moveToNext();
      Document doc = (Document) iter.get();
      createTermFreqVector(jcas, doc);
    }

  }

  /**
   * A basic white-space tokenizer, it deliberately does not split on punctuation!
   *
   * @param doc input text
   * @return a list of tokens.
   */

  List<String> tokenize0(String doc) {
    List<String> res = new ArrayList<String>();

    Collections.addAll(res, doc.split("\\s+"));
    return res;
  }

  /**
   * Saved tf vectors to the annotation {@link edu.cmu.lti.f14.hw3.hw3_junjiah.typesystems.Document}.
   *
   * @param jcas
   * @param doc
   */
  private void createTermFreqVector(JCas jcas, Document doc) {
    String docText = doc.getText();
    // tokenize the text
    List<String> tokenStrings = tokenize0(docText);
    // token strings without duplicates
    Set<String> tokenSet = new HashSet<String>(tokenStrings);
    // list contains tokens
    List<Token> tokens = new ArrayList<Token>();

    for (String tokenString : tokenSet) {
      int frequency = Collections.frequency(tokenStrings, tokenString);
      Token t = new Token(jcas);
      t.setText(tokenString);
      t.setFrequency(frequency);
      tokens.add(t);
    }
    FSList f = Utils.fromCollectionToFSList(jcas, tokens);
    doc.setTokenList(f);
  }

}
