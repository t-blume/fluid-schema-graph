package zbw.cau.gotham.schema;

import common.IQuintHandler;
import utils.BasicUtils;

import java.io.File;
import java.util.HashMap;

public class InMemorySchemaGraph implements ISchemaGraph {

    HashMap<String, PropertyNode> schemaPropertyHashMap = new HashMap<>();
    HashMap<String, TypeNode> schemaTypeHashMap = new HashMap<>();
    @Override
    public PropertyNode getPropertyNode(String property) {
        return schemaPropertyHashMap.get(property);
    }

    @Override
    public void addPropertyNode(String property, PropertyNode propertyNode) {
        if (!schemaPropertyHashMap.containsKey(property))
            schemaPropertyHashMap.put(property,
                    new PropertyNode(property));
    }

    @Override
    public int getNumberOfPropertyNodes() {
        return schemaPropertyHashMap.size();
    }

    @Override
    public TypeNode getTypeNode(String type) {
        return schemaTypeHashMap.get(type);
    }

    @Override
    public void addTypeNode(String type, TypeNode typeNode) {
        if (!schemaTypeHashMap.containsKey(type))
            schemaTypeHashMap.put(type,
                    new TypeNode(type));
    }

    @Override
    public int getNumberOfTypeNodes() {
        return schemaTypeHashMap.size();
    }



    public void stream(IQuintHandler func) {
        schemaPropertyHashMap.values().stream().forEach(N -> N.stream().forEach(X -> func.handle(X)));
        schemaTypeHashMap.values().stream().forEach(N -> N.stream().forEach(X -> func.handle(X)));
    }

    public void persist(String folder) {
        System.out.println("Saving Schema Graph permanently on disk.");
        BasicUtils.createFile(folder + File.separator + "properties" + File.separator + "dummy");
        BasicUtils.createFile(folder + File.separator + "types" + File.separator + "dummy");

        Thread thread1 = new Thread(() -> schemaPropertyHashMap.entrySet().parallelStream().forEach(ENTRY -> {
                    try {
                        BasicUtils.writeObjectToFile(ENTRY.getValue(), new File(folder + File.separator + "properties" + File.separator + ENTRY.getKey().hashCode()));
                    } catch (StackOverflowError e) {
                        System.err.println("Properties: " + ENTRY);
                        e.printStackTrace();
                    }
                }
        ));
        thread1.start();

        Thread thread2 = new Thread(() -> schemaTypeHashMap.entrySet().parallelStream().forEach(ENTRY -> {
                    try {
                        BasicUtils.writeObjectToFile(ENTRY.getValue(), new File(folder + File.separator + "types" + File.separator + ENTRY.getKey().hashCode()));
                    } catch (StackOverflowError e) {
                        System.err.println("Types: " + ENTRY);
                        e.printStackTrace();
                    }
                }
        ));
        thread2.start();

        System.out.println("Both threads running...");
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Done writing!");
    }
}
