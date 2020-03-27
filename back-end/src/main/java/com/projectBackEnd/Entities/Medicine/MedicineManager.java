package main.java.com.projectBackEnd.Entities.Medicine;
import java.util.List;
import java.util.stream.Collectors;
import java.io.Serializable;

import main.java.com.projectBackEnd.EntityManager;
import main.java.com.projectBackEnd.HibernateUtility;

/**
 * MedicineManager defines methods to interact with the Medicine table in the database.
 * This class extends the EntityManager - supplying it with the rest of its interface methods.
 *
 * https://examples.javacodegeeks.com/enterprise-java/hibernate/hibernate-annotations-example/
 */
public class MedicineManager extends EntityManager implements MedicineManagerInterface {

    private static MedicineManagerInterface medicineManager;

    private MedicineManager() {
        super();
        setSubclass(Medicine.class);
        HibernateUtility.addAnnotation(Medicine.class);
        medicineManager = this;
    }

    /**
     * Get medicine manager interface
     * @return medicineManager ; if none has been defined, create a new MedicineManager()
     */
    public static MedicineManagerInterface getMedicineManager() {
        return (medicineManager != null) ? medicineManager : new MedicineManager();
    }

    /**
     * @return a list of all medicines in database
     */
    public List<Medicine> getAllMedicines() {
        return (List<Medicine>) super.getAll();
    }

    /**
     * Create and insert a medicine intto the database
     * @param name
     * @param type
     * @return newly created medicine object, with its assigned ID from the database.
     */
    public Medicine addMedicine(String name, String type) {
        Medicine newMedicine = new Medicine(name, type);
        super.insertTuple(newMedicine);
        return newMedicine;
    }

    /**
     * Insert input medicine object t/into database
     * @param med
     * @return added object with a replaced ID.
     */
    public Medicine addMedicine(Medicine med) {
        super.insertTuple(med);
        return med;
    }

    /**
     * @return primary key
     */
    @Override
    public Medicine getByPrimaryKey(Serializable id) {
        return (Medicine) super.getByPrimaryKey(id);
    }

    /**
     * Update attributes of the object
     * Transactional annotation is inherited so no need to tag these methods
     * @return updated object
     */
    public Medicine update(Medicine med) {
        return (Medicine) super.update(med);
    }
}
