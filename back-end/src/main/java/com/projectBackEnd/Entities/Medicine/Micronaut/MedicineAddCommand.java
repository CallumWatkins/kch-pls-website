package main.java.com.projectBackEnd.Entities.Medicine.Micronaut;
import javax.validation.constraints.NotNull;
//import io.micronaut.core.annotation.Introspected;
//TODO Reinsert tags as I couldn't get them to compile on IntelliJ Jars.

/**
 * MedicineAddCommand is an implementation of the Command design pattern.
 * It creates mock medicine objects and reduced memory use.
 * It is used by the controller to insert a medicine object into the database/
 */
//@Introspected
public class MedicineAddCommand {


    @NotNull
    private String name;

    @NotNull
    private String type;


    /**
     * Default constructor
     */
    public MedicineAddCommand(){}


    /**
     * Main constructor
     * @param name  Name of the Medicine
     * @param type  Type of the Medicine
     */
    public MedicineAddCommand(String name, String type){
        setName(name);
        setType(type);
    }


    /**
     * Get the name of the medicine object
     * @return Name
     */
    public String getName() {
        return name;
    }


    /**
     * Change the name of the medicine object
     * @param name New name
     */
    public void setName(String name) {
        this.name = (name == null || "".equals(name.trim())) ? "Unnamed" : name;
    }


    /**
     * Get the type of the medicine object
     * @return type
     */
    public String getType() {
        return type;
    }


    /**
     * Change the type of the medicine object
     * @param type New type
     */
    public void setType(String type) {
        this.type = (type == null || "".equals(type.trim())) ? "Undefined" : type;
    }

}

