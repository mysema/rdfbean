package ${package}.services;

import java.io.Serializable;
import java.util.Collection;

/**
 * DAO defines an interface for transactional RDFBean DAOs
 *
 */
public interface DAO<Entity, Id extends Serializable> {    

    /**
     * Get all persisted instances
     * 
     * @return
     */
    Collection<Entity> getAll();

    /**
     * Get the persisted instance with the given id
     * 
     * @param id
     * @return
     */
    Entity getById( Id id );

    /**
     * Remove the persisted instance
     * 
     * @param entity
     */
    void remove( Entity entity );
    
    /**
     * Removed the persisted entity with the given id
     * 
     * @param id
     */
    void remove(Id id);

    /**
     * Save the given entity
     * 
     * @param entity
     * @return
     */
    Entity save( Entity entity );
    
    /**
     * Save all the given entities
     * 
     * @param entities
     */
    void saveAll( Iterable<? extends Entity> entities);

}
