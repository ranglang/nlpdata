package nlpdata.datasets.conll

import nlpdata.structure._
import nlpdata.util._

/** Represents a single CoNLL annotation file.
  *
  * @param id the unique ID of the file, present on its first line
  * @param sentences all of the sentences in the annotation file
  */
case class CoNLLFile(
  path: CoNLLPath,
  sentences: Vector[CoNLLSentence]
)

/** Represents an annotated sentence from the CoNLL data.
  *
  * As of now, we're lazy-loading the members of this class,
  * only implementing them as we need them.
  * I believe coref spans are already implemented in the coref annotation project;
  * if necessary ask me (Julian) and I can put them in.
  */
case class CoNLLSentence(
  path: CoNLLSentencePath,
  partNum: Int,
  words: List[Word],
  syntaxTree: SyntaxTree,
  predicateArgumentStructures: List[PredicateArgumentStructure]
  // nerSpans: Nothing, // TODO
  // corefSpans: List[CorefSpan] // TODO
) {
  def sentenceNum = path.sentenceNum
}

object CoNLLSentence {

  implicit object CoNLLSentenceHasTokens extends HasTokens[CoNLLSentence] {
    override def getTokens(sentence: CoNLLSentence): Vector[String] =
      sentence.words.map(_.token).toVector
  }

}

case class CoNLLPath(
  split: String, // development, train
  language: String, // arabic, chinese, english
  domain: String, // depends on language; e.g., nw, bc, wb
  source: String, // e.g., wsj, xinhua
  section: Int, // always falls within 0-99
  name: String, // filename prefix, usually equal to source
  number: Int // always falls within 0-99
) {

  def documentId =
    f"$domain%s/$source%s/$section%02d/$name%s_$section%02d$number%02d"

  def suffix =
    s"v4/data/$split/data/$language/annotations/$documentId.v4_gold_conll"
}

object CoNLLPath {
  private[this] val pathSuffixRegex =
    """v4/data/(.*?)/data/(.*?)/annotations/(.*?)/(.*?)/([0-9]{2})/(.*?)_[0-9]{2}([0-9]{2}).v4_gold_conll""".r
  private[this] object IntMatch {
    def unapply(s: String): Option[Int] = scala.util.Try(s.toInt).toOption
  }

  def fromPathSuffix(s: String): Option[CoNLLPath] = s match {
    case pathSuffixRegex(
        split,
        language,
        domain,
        source,
        IntMatch(section),
        name,
        IntMatch(number)
        ) =>
      Some(CoNLLPath(split, language, domain, source, section, name, number))
    case _ => None
  }
}

/** Represents a unique index to a CoNLL sentence.
  *
  * This can be used to easily serialize a sentence without worrying about the data definition changing.
  * The FileManager extension methods for the conll package include one to retrieve a sentence directly
  * from such a path.
  *
  * @param filePath the path to the CoNLL file containing this sentence
  * @param sentenceNum the index of this sentence in the document
  */
case class CoNLLSentencePath(
  filePath: CoNLLPath,
  sentenceNum: Int
) {
  override def toString = s"${filePath.suffix}:$sentenceNum"
}
