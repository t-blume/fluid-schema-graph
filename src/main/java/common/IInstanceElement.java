package common;

import java.util.Set;

/**
 * A representation of an instance modeled by RDF statements. Instances are
 * described by their properties and types. Instances are expected to be
 * uniquely identified by their {@link IResource} locator.
 * 
 * @author Bastian
 * 
 */
public interface IInstanceElement extends ILocatable {

	

	/**
	 * Returns all {@link IQuint}s associated with the instance
	 * 
	 * @return The instance's quint-data
	 */
	Set<IQuint> getOutgoingQuints();
	/**
	 * Returns all {@link IQuint}s associated with the incoming properties of the instance
	 *
	 * @return The instance's incoming quint-data
	 */
	Set<IQuint> getIncomingQuints();

	/**
	 * Adds an observation to the instance
	 * 
	 * @param q
	 */
	void addOutgoingQuint(IQuint q);

	/**
	 * Adds quints regarding incoming properties
	 *
	 * @param q
	 */
	void addIncomingQuint(IQuint q);

	/**
	 * creates a new IInstanceElement with same set of quints
	 * @return
	 */
	IInstanceElement clone();
}
