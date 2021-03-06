package common;

/**
 * A 5-Tuple representing a single statement made in the RDF N-Quad notation
 * (@see <a href="http://google.com">http://www.w3.org/TR/n-quads/</a>) with an
 * additional timestamp.
 * 
 * A quint consists of subject, predicate, object, context and observation time.
 * 
 * @author Bastian
 * 
 */
public interface IQuint {

	/**
	 * The subject of the RDF-statement in N-Quad notation will be returned
	 * 
	 * @return The subject of the quint
	 */
	IResource getSubject();

	/**
	 * The predicate of the RDF-statement in N-Quad notation will be returned
	 * 
	 * @return The predicate of the quint
	 */
	IResource getPredicate();

	/**
	 * The object of the RDF-statement in N-Quad notation will be returned
	 * 
	 * @return The object of the quint
	 */
	IResource getObject();

	/**
	 * The context of the RDF-statement in N-Quad notation will be returned. The
	 * context may be null statement is not known or irrelevant
	 * 
	 * @return The context of the quint
	 */
	IResource getContext();

	/**
	 * The time this quint was observed.
	 * 
	 * @return The timestamp corresponding to the time, the quint was observed
	 */
	ITimestamp getTimestamp();
}
