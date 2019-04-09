package zbw.cau.gotham.schema;


import common.IQuint;
import common.implementation.NodeResource;
import common.implementation.Quad;
import org.semanticweb.yars.nx.Resource;
import utils.Constants;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Blume Till on 16.11.2016.
 */
public class TypeNode implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 9109031995295581642L;
	private String type;
	private Set<String> superClasses;
    private Set<TypeNode> subClassOf;


    public TypeNode(String type) {
        this.type = type;
        subClassOf = new HashSet<>();
    }

    public String getType(){
        return type;
    }

    public Set<String> getSubClassOf(){
        if(superClasses != null)
            return superClasses;
        Set<TypeNode> recursiveNodes = getSuperClasses(new HashSet<>());
        superClasses = recursiveNodes.stream().map(TypeNode::getType).collect(Collectors.toCollection(HashSet::new));
        return superClasses;
    }

    /**
     * added parameter to prevent circles
     * @return
     */
    private Set<TypeNode> getSuperClasses(Set<TypeNode> knownTypeNodes){
        Set<TypeNode> recursiveNodes = new HashSet<>();
        subClassOf.forEach(TN -> {
            if(!knownTypeNodes.contains(TN))    //only add TypeNodes which are new
                recursiveNodes.add(TN);
        });
        knownTypeNodes.addAll(recursiveNodes); //those are now known as well
        for(TypeNode typeNode : recursiveNodes) //iterate over all nodes added in this step
            knownTypeNodes.addAll(typeNode.getSuperClasses(knownTypeNodes));

        return knownTypeNodes;
    }

    public void addSubClassOf(TypeNode subClassOf) {
        this.subClassOf.add(subClassOf);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TypeNode typeNode = (TypeNode) o;

        return type.equals(typeNode.type);

    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }


    public Stream<IQuint> stream() {
        Set<IQuint> quints = new HashSet<>();
        Set<String> superTypes = getSubClassOf();

        for (String s : superTypes)
            quints.add(new Quad(new NodeResource(new Resource(type)),
                    new NodeResource(new Resource(Constants.RDFS_SUBCLASSOF)),
                    new NodeResource(new Resource(s)),
                    new NodeResource(new Resource("http://schema-graph.fluid.uni-kiel.de"))));


        return quints.stream();
    }


    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUTF(type);
        out.writeObject(getSubClassOf());
    }

    @SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        type = in.readUTF();
        superClasses = (Set<String>) in.readObject();
    }

}
