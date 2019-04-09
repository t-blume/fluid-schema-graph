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
public class PropertyNode implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1628846112493024111L;

    private String property;

    private Set<PropertyNode> domain;
    private Set<String> domainProperties;
    private Set<PropertyNode> range;
    private Set<String> rangeProperties;
    private Set<PropertyNode> subPropertyOf;
    private Set<String> superProperties;

    // currently not used
    public PropertyNode(String property) {
        this.property = property;
        this.domain = new HashSet<>();
        this.range = new HashSet<>();
        this.subPropertyOf = new HashSet<>();
    }

    public String getProperty() {
        return property;
    }

    public Set<String> getDomain() {
        if (domainProperties != null)
            return domainProperties;

        domainProperties = domain.stream().map(PropertyNode::getProperty).collect(Collectors.toCollection(HashSet::new));
        return domainProperties;
    }

    public void addDomain(PropertyNode domain) {
        this.domain.add(domain);
    }

    public Set<String> getRange() {
        if (rangeProperties != null)
            return rangeProperties;

        rangeProperties = range.stream().map(PropertyNode::getProperty).collect(Collectors.toCollection(HashSet::new));
        return rangeProperties;
    }

    public void addRange(PropertyNode range) {
        this.range.add(range);
    }

    public Set<String> getSubPropertyOf() {
        if (superProperties != null)
            return superProperties;
        Set<PropertyNode> recursiveNodes = getSuperProperties(new HashSet<>());
        superProperties = recursiveNodes.stream().map(PropertyNode::getProperty).collect(Collectors.toCollection(HashSet::new));
        return superProperties;
    }

    public void addSubPropertyOf(PropertyNode subPropertyOf) {
        this.subPropertyOf.add(subPropertyOf);
    }


    private Set<PropertyNode> getSuperProperties(Set<PropertyNode> knownPropertyNodes) {
        Set<PropertyNode> recursiveNodes = new HashSet<>();
        subPropertyOf.forEach(PN -> {
            if (!knownPropertyNodes.contains(PN))    //only add PropertyNodes which are new
                recursiveNodes.add(PN);
        });
        knownPropertyNodes.addAll(recursiveNodes); //those are now known as well
        for (PropertyNode propertyNode : recursiveNodes) //iterate over all nodes added in this step
            knownPropertyNodes.addAll(propertyNode.getSuperProperties(knownPropertyNodes));

        return knownPropertyNodes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PropertyNode that = (PropertyNode) o;

        return property.equals(that.property);
    }

    @Override
    public int hashCode() {
        return property.hashCode();
    }


    public Stream<IQuint> stream() {
        Set<IQuint> quints = new HashSet<>();

        Set<String> domains = getDomain();
        Set<String> ranges = getRange();
        Set<String> superProperties = getSubPropertyOf();

        for (String d : domains)
            quints.add(new Quad(new NodeResource(new Resource(property)),
                    new NodeResource(new Resource(Constants.RDFS_DOMAIN)),
                    new NodeResource(new Resource(d)),
                    new NodeResource(new Resource("http://schema-graph.fluid.uni-kiel.de"))));

        for (String r : ranges)
            quints.add(new Quad(new NodeResource(new Resource(property)),
                    new NodeResource(new Resource(Constants.RDFS_RANGE)),
                    new NodeResource(new Resource(r)),
                    new NodeResource(new Resource("http://schema-graph.fluid.uni-kiel.de"))));

        for (String s : superProperties)
            quints.add(new Quad(new NodeResource(new Resource(property)),
                    new NodeResource(new Resource(Constants.RDFS_SUBPROPERTYOF)),
                    new NodeResource(new Resource(s)),
                    new NodeResource(new Resource("http://schema-graph.fluid.uni-kiel.de"))));


        return quints.stream();
    }


    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUTF(property);
        out.writeObject(getDomain());
        out.writeObject(getRange());
        out.writeObject(getSubPropertyOf());

    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        property = in.readUTF();
        domainProperties = (Set<String>) in.readObject();
        rangeProperties = (Set<String>) in.readObject();
        superProperties = (Set<String>) in.readObject();
    }
}
