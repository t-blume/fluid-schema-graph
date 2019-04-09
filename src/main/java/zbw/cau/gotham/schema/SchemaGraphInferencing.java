package zbw.cau.gotham.schema;


import common.IInstanceElement;
import common.IQuint;
import utils.Constants;
import utils.LRUCache;

import java.io.PrintStream;
import java.util.*;

import static utils.Constants.RDF_TYPE;


/**
 * TODO: INCLUDE INCOMING PROPERTIES
 *
 * @author Blume Till
 */
public class SchemaGraphInferencing {


    public ISchemaGraph getSchemaGraph() {
        return schemaGraph;
    }

    public void setSchemaGraph(ISchemaGraph schemaGraph) {
        this.schemaGraph = schemaGraph;
    }

    ISchemaGraph schemaGraph;

    private LRUCache<String, Set<String>> recursivePropertyCache;
    private LRUCache<String, Set<String>> recursiveTypeCache;

    private final int cacheSize;

    boolean trackStatistics = true;
    //statistics
    private int cacheFlushes = 0;
    private List<Integer> superPropertiesAdded = new LinkedList<>();
    private List<Integer> superTypesAdded = new LinkedList<>();


    private int cacheHits = 0;

    public SchemaGraphInferencing() {
        this(5000, null);
    }

    public SchemaGraphInferencing(int cacheSize) {
        this(cacheSize, null);
    }

    public SchemaGraphInferencing(String folder) {
        this(5000, folder);
    }

    public SchemaGraphInferencing(int cacheSize, String folder) {
        if (folder == null)
            schemaGraph = new InMemorySchemaGraph();
        else
            schemaGraph = new FileSchemaGraph(folder);

        this.cacheSize = cacheSize;
        recursivePropertyCache = new LRUCache<>(cacheSize, 0.75f);
        recursiveTypeCache = new LRUCache<>(cacheSize, 0.75f);

    }


    public void printStatistics(PrintStream printStream) {
        printStream.println("==== Schema Graph Statistics ====");
        printStream.println("Property count: " + schemaGraph.getNumberOfPropertyNodes());
        printStream.println("Property Inferences: " + superPropertiesAdded.size());

        int totalPropsInferred = 0;
        for (Integer x : superPropertiesAdded)
            totalPropsInferred += x;

        printStream.println("Total Properties Inferred: " + totalPropsInferred);
        printStream.println("Average Properties Inferred: " + (double) totalPropsInferred / (double) superPropertiesAdded.size());

        printStream.println("Type count: " + schemaGraph.getNumberOfTypeNodes());
        printStream.println("Type Inferences: " + superTypesAdded.size());

        int totalTypesInferred = 0;
        for (Integer x : superTypesAdded)
            totalTypesInferred += x;

        printStream.println("Total Types Inferred: " + totalTypesInferred);
        printStream.println("Average Types Inferred: " + (double) totalTypesInferred / (double) superTypesAdded.size());

        printStream.println("== Cache-stats ==");
        printStream.println("Cache Hits: " + cacheHits);
        printStream.println("Cache flushes: " + cacheFlushes);
        printStream.println("--------------------------");

    }

    /**
     * Return a set of inferable TypeInformation for the given instance
     * To ensure maximal inference, infer all properties before calling
     * this method
     *
     * @param instance
     * @return
     */
    public Set<String> inferSubjectTypes(IInstanceElement instance) {
        if (instance == null)
            return null;

        Set<String> tmpTypes = new HashSet<>();
        //outgoing quints
        for (IQuint quint : instance.getOutgoingQuints()) {
            //add types directly
            if (quint.getPredicate().toString().equals(RDF_TYPE))
                tmpTypes.add(quint.getObject().toString());
            else {
                PropertyNode tmpNode = schemaGraph.getPropertyNode(quint.getPredicate().toString());
                if (tmpNode != null) {
                    //if we have domain information, add them as types
                    //TODO: Invalid use of domain/range check? e.g. inferred type is no type?
                    tmpTypes.addAll(tmpNode.getDomain());
                }
            }
        }
        //TODO: incoming properties
        return inferTypesFromTypeGraph(tmpTypes);
    }


    /**
     * Return a set of inferrable properties for the given instance
     *
     * @param instance
     * @return
     */
    public HashMap<String, Set<String>> inferProperties(IInstanceElement instance) {
        if (instance == null)
            return null;

        Set<String> instanceProperties = new HashSet<>();
        for (IQuint quint : instance.getOutgoingQuints()) {
            //add non-type properties
            if (!quint.getPredicate().toString().equals(RDF_TYPE))
                instanceProperties.add(quint.getPredicate().toString());
        }

        HashMap<String, Set<String>> inferrableProperties = new HashMap<>();
        //use type graph to infer additional types based on all currently known types
        instanceProperties.forEach(PROP -> {
            Set<String> superProperties;
            Set<String> newProps = getPropertyCache(PROP);
            if (newProps == null) {
                PropertyNode tmp = schemaGraph.getPropertyNode(PROP);
                //if present, add all connected SuperProperties
                if (tmp != null && !(superProperties = tmp.getSubPropertyOf()).isEmpty()) {
                    newProps = superProperties;
                    if (trackStatistics)
                        superPropertiesAdded.add(newProps.size());
                    addPropertyCache(PROP, newProps);
                }

            }
            if (newProps != null)
                inferrableProperties.put(PROP, newProps);
        });
        return inferrableProperties;
    }

    /**
     * Return a set of inferable TypeInformation about the object
     * for the given statement
     *
     * @param quint
     * @return
     */
    public Set<String> inferObjectTypes(IQuint quint) {
        if (quint == null)
            return null;
        //get range information
        PropertyNode tmp = schemaGraph.getPropertyNode(quint.getPredicate().toString());
        if (tmp != null) {
            Set<String> tmpTypes = tmp.getRange();
            return inferTypesFromTypeGraph(tmpTypes);
        } else
            return new HashSet<>();
    }


    /**
     * Add a RDFS statement to SchemaGraph.
     *
     * @param schemaStatement
     * @return
     */

    public boolean add(IQuint schemaStatement) {
        if (schemaStatement == null)
            return false;

        clearCache(); //cache possible not up to date //FIXME make efficient
        switch (schemaStatement.getPredicate().toString()) {
            case Constants.RDFS_DOMAIN: {
                //Inferable Knowledge: A Instance having the subject of this
                //statement as property, has also the object of this statement as type
                //create entry

                //if object-property does not exist, create it
                schemaGraph.addPropertyNode(schemaStatement.getObject().toString(),
                        new PropertyNode(schemaStatement.getObject().toString()));

                //if subject-property does not exist, create it
                schemaGraph.addPropertyNode(schemaStatement.getSubject().toString(),
                        new PropertyNode(schemaStatement.getSubject().toString()));

                //create link between them both
                schemaGraph.getPropertyNode(schemaStatement.getSubject().toString()).addDomain(
                        schemaGraph.getPropertyNode(schemaStatement.getObject().toString()));

                return true;
            }
            case Constants.RDFS_RANGE: {
                //Inferable Knowledge: A Instance having the subject of this
                //statement as property, the corresponding object TypeCluster also
                //has the object of this statement as type

                //if object-property does not exist, create it
                schemaGraph.addPropertyNode(schemaStatement.getObject().toString(),
                        new PropertyNode(schemaStatement.getObject().toString()));

                //if subject-property does not exist, create it
                schemaGraph.addPropertyNode(schemaStatement.getSubject().toString(),
                        new PropertyNode(schemaStatement.getSubject().toString()));

                //create link between them both
                schemaGraph.getPropertyNode(schemaStatement.getSubject().toString()).addRange(
                        schemaGraph.getPropertyNode(schemaStatement.getObject().toString()));

                return true;
            }
            case Constants.RDFS_SUBCLASSOF: {
                //Inferable Knowledge: A Instance having the subject of this
                //statement as type, has also the object of this statement as type

                //if object-property does not exist, create it
                schemaGraph.addTypeNode(schemaStatement.getObject().toString(),
                        new TypeNode(schemaStatement.getObject().toString()));

                //if subject-property does not exist, create it
                schemaGraph.addTypeNode(schemaStatement.getSubject().toString(),
                        new TypeNode(schemaStatement.getSubject().toString()));

                //create link between them both
                schemaGraph.getTypeNode(schemaStatement.getSubject().toString()).addSubClassOf(
                        schemaGraph.getTypeNode(schemaStatement.getObject().toString()));

                return true;
            }
            case Constants.RDFS_SUBPROPERTYOF: {
                //Inferable Knowledge: A Instance having the subject of this
                //statement as property, has also the object of this statement as property

                //if object-property does not exist, create it
                schemaGraph.addPropertyNode(schemaStatement.getObject().toString(),
                        new PropertyNode(schemaStatement.getObject().toString()));

                //if subject-property does not exist, create it
                schemaGraph.addPropertyNode(schemaStatement.getSubject().toString(),
                        new PropertyNode(schemaStatement.getSubject().toString()));

                //create link between them both
                schemaGraph.getPropertyNode(schemaStatement.getSubject().toString()).addSubPropertyOf(
                        schemaGraph.getPropertyNode(schemaStatement.getObject().toString()));

                return true;
            }
            default: {
                System.out.println("Ups: " + schemaStatement.getPredicate().toString());
                return false;
            }
        }
    }


    private Set<String> getPropertyCache(String term) {
        Set<String> newTerms = recursivePropertyCache.get(term);
        if (trackStatistics && newTerms != null)
            cacheHits++;

        return newTerms;
    }

    private Set<String> getTypeCache(String term) {
        Set<String> newTerms = recursiveTypeCache.get(term);
        if (trackStatistics && newTerms != null)
            cacheHits++;

        return newTerms;
    }

    private void addPropertyCache(String term, Set<String> terms) {
        recursivePropertyCache.put(term, terms);
    }

    private void addTypeCache(String term, Set<String> terms) {
        recursiveTypeCache.put(term, terms);
    }

    private void clearCache() {
        if (recursivePropertyCache.size() > 0 || recursiveTypeCache.size() > 0) {
            recursivePropertyCache = new LRUCache<>(cacheSize, 0.75f);
            recursiveTypeCache = new LRUCache<>(cacheSize, 0.75f);
            cacheFlushes++;
        }
    }

    private Set<String> inferTypesFromTypeGraph(Set<String> types) {
        Set<String> allTypes = new HashSet<>();
        //use type graph to infer additional types based on all currently known types
        types.forEach(TYPE -> {
            Set<String> newTypes = getTypeCache(TYPE);
            if (newTypes == null) {
                TypeNode tmp = schemaGraph.getTypeNode(TYPE);
                //if present, add all connected SuperClasses
                if (tmp != null) {
                    newTypes = tmp.getSubClassOf();
                    if (trackStatistics)
                        superTypesAdded.add(newTypes.size());
                    addTypeCache(TYPE, newTypes);
                }
            }
            if (newTypes != null)
                allTypes.addAll(newTypes);
            //add type itself anyway
            allTypes.add(TYPE);
        });
        return allTypes;
    }


}
