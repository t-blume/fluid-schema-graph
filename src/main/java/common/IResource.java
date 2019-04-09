package common;

/**
 * An identification of a resource
 * 
 * @author Bastian
 * 
 */
public interface IResource {

	/**
	 * Returns an additional representation of the resource which can be used in
	 * the Triple or Quad format
	 * 
	 * @return A Tripe/Quad conform representation of the resource
	 */
	String toN3();



}
