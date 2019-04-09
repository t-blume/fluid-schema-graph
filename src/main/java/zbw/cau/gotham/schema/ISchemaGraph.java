package zbw.cau.gotham.schema;

public interface ISchemaGraph {

    PropertyNode getPropertyNode(String property);

    void addPropertyNode(String property, PropertyNode propertyNode);

    int getNumberOfPropertyNodes();

    TypeNode getTypeNode(String type);

    void addTypeNode(String type, TypeNode typeNode);

    int getNumberOfTypeNodes();

}
