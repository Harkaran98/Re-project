import java.util.*;

public final class WorldModel
{
   public int numRows;
   public int numCols;
   public Background background[][];
   public Entity occupancy[][];
   public Set<Entity> entities;

   public WorldModel(int numRows, int numCols, Background defaultBackground)
   {
      this.numRows = numRows;
      this.numCols = numCols;
      this.background = new Background[numRows][numCols];
      this.occupancy = new Entity[numRows][numCols];
      this.entities = new HashSet<>();

      for (int row = 0; row < numRows; row++)
      {
         Arrays.fill(this.background[row], defaultBackground);
      }
   }

   public Optional<Entity> findNearest(Point pos,
                                              EntityKind kind)
   {
      List<Entity> ofType = new LinkedList<>();
      for (Entity entity : this.entities)
      {
         if (entity.kind == kind)
         {
            ofType.add(entity);
         }
      }

      return nearestEntity(ofType, pos);
   }

   public void removeEntity(Entity entity)
   {
      removeEntityAt(this, entity.position);
   }

   /*
    Assumes that there is no entity currently occupying the
    intended destination cell.
 */
   public  void addEntity(Entity entity)
   {
      if (withinBounds(this, entity.position))
      {
         setOccupancyCell(this, entity.position, entity);
         this.entities.add(entity);
      }
   }


}
