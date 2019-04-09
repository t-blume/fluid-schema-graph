package zbw.cau.gotham.schema;

import java.io.*;


/**
 * @author Blume Till
 */
public class FileSchemaGraph implements ISchemaGraph {
    private String folder;

    private int numberOfProperties = -1;
    private int numberOfTypes = -1;

    public FileSchemaGraph(String folder) {
        this.folder = folder;
    }

    public PropertyNode getPropertyNode(String property) {
        //null pointer here?
        File file = new File(folder + File.separator + "properties" + File.separator + property.hashCode());
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file); ObjectInputStream ois = new ObjectInputStream(fis)) { //irgendwas scheitert im Object Input Stream
                return (PropertyNode) ois.readObject();
            } catch (ClassNotFoundException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void addPropertyNode(String property, PropertyNode propertyNode) {

    }

    @Override
    public int getNumberOfPropertyNodes() {
        if (numberOfProperties < 0)
            //numberOfProperties = new File(folder + File.separator + "properties").list().length; //TODO something scalable
            numberOfProperties = -1;
        return numberOfProperties;
    }

    public TypeNode getTypeNode(String type) {
        File file = new File(folder + File.separator + "types" + File.separator + type.hashCode());
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file); ObjectInputStream ois = new ObjectInputStream(fis)) {
                return (TypeNode) ois.readObject();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void addTypeNode(String type, TypeNode typeNode) {

    }

    @Override
    public int getNumberOfTypeNodes() {
        if (numberOfTypes < 0)
           // numberOfTypes = new File(folder + File.separator + "types").list().length; //TODO something scalable
            numberOfTypes = -1;
        return numberOfTypes;
    }



}
