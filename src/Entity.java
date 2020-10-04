import java.util.List;
import java.util.Optional;
import java.util.Random;

import processing.core.PImage;

public final class Entity
{
   public EntityKind kind;
   private String id;
   public Point position;
   public List<PImage> images;
   public int imageIndex;
   private int resourceLimit;
   private int resourceCount;
   public int actionPeriod;
   private int animationPeriod;

   private  final String BLOB_KEY = "blob";
   private  final String BLOB_ID_SUFFIX = " -- blob";
   private  final int BLOB_PERIOD_SCALE = 4;
   private  final int BLOB_ANIMATION_MIN = 50;
   private  final int BLOB_ANIMATION_MAX = 150;

   private  final Random rand = new Random();

   private  final String QUAKE_KEY = "quake";
   public  static final String ORE_KEY = "ore";


   private  final String ORE_ID_PREFIX = "ore -- ";
   private  final int ORE_CORRUPT_MIN = 20000;
   private  final int ORE_CORRUPT_MAX = 30000;


   public Entity(EntityKind kind, String id, Point position,
      List<PImage> images, int resourceLimit, int resourceCount,
      int actionPeriod, int animationPeriod)
   {
      this.kind = kind;
      this.id = id;
      this.position = position;
      this.images = images;
      this.imageIndex = 0;
      this.resourceLimit = resourceLimit;
      this.resourceCount = resourceCount;
      this.actionPeriod = actionPeriod;
      this.animationPeriod = animationPeriod;
   }

   public void executeMinerFullActivity(WorldModel world,
                                               ImageStore imageStore, EventScheduler scheduler)
   {
      Optional<Entity> fullTarget = world.findNearest(this.position,
              EntityKind.BLACKSMITH);

      if (fullTarget.isPresent() &&
              moveToFull(world, fullTarget.get(), scheduler))
      {
         transformFull(world, scheduler, imageStore);
      }
      else
      {
         scheduler.scheduleEvent(this,
                 Functions.createActivityAction(this, world, imageStore),
                 this.actionPeriod);
      }
   }

   public void executeMinerNotFullActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler)
   {
      Optional<Entity> notFullTarget = world.findNearest(this.position,
              EntityKind.ORE);

      if (!notFullTarget.isPresent() ||
              !moveToNotFull(world, notFullTarget.get(), scheduler) ||
              !transformNotFull(world, scheduler, imageStore))
      {
         scheduler.scheduleEvent(this,
                 Functions.createActivityAction(this, world, imageStore),
                 this.actionPeriod);
      }
   }

   public void executeOreActivity(WorldModel world,
                                         ImageStore imageStore, EventScheduler scheduler)
   {
      Point pos = this.position;  // store current position before removing

      this.removeEntity(world);
      scheduler.unscheduleAllEvents(this);

      Entity blob = Functions.createOreBlob(this.id + BLOB_ID_SUFFIX,
              pos, this.actionPeriod / BLOB_PERIOD_SCALE,
              BLOB_ANIMATION_MIN +
                      rand.nextInt(BLOB_ANIMATION_MAX - BLOB_ANIMATION_MIN),
              imageStore.getImageList(BLOB_KEY));

      blob.addEntity(world);
      scheduler.scheduleActions(blob, world, imageStore);
   }

   public void executeOreBlobActivity(WorldModel world,
                                             ImageStore imageStore, EventScheduler scheduler)
   {
      Optional<Entity> blobTarget = world.findNearest(this.position, EntityKind.VEIN);
      long nextPeriod = this.actionPeriod;

      if (blobTarget.isPresent())
      {
         Point tgtPos = blobTarget.get().position;

         if (moveToOreBlob(world, blobTarget.get(), scheduler))
         {
            Entity quake = Functions.createQuake(tgtPos,
                    imageStore.getImageList(QUAKE_KEY));

            quake.addEntity(world);
            nextPeriod += this.actionPeriod;
            scheduler.scheduleActions(quake, world, imageStore);
         }
      }

      scheduler.scheduleEvent(this,
              Functions.createActivityAction(this, world, imageStore),
              nextPeriod);
   }
   public void executeQuakeActivity(WorldModel world,
                                           ImageStore imageStore, EventScheduler scheduler)
   {
      scheduler.unscheduleAllEvents(this);
      this.removeEntity(world);
   }

   public void executeVeinActivity(WorldModel world,
                                          ImageStore imageStore, EventScheduler scheduler)
   {
      Optional<Point> openPt = world.findOpenAround(this.position);

      if (openPt.isPresent())
      {
         Entity ore = Functions.createOre(ORE_ID_PREFIX + this.id,
                 openPt.get(), ORE_CORRUPT_MIN +
                         rand.nextInt(ORE_CORRUPT_MAX - ORE_CORRUPT_MIN),
                 imageStore.getImageList(ORE_KEY));
         ore.addEntity(world);
         scheduler.scheduleActions(ore, world, imageStore);
      }

      scheduler.scheduleEvent(this,
              Functions.createActivityAction(this, world, imageStore),
              this.actionPeriod);
   }

   /**
    * Gets the next image associated with the specified entity,
    * by updating the entity's imageIndex.
    */
   public void nextImage()
   {
      this.imageIndex = (this.imageIndex + 1) % this.images.size();
   }

   /**
    * Gets the specified entity's animation period.
    */
   public int getAnimationPeriod()
   {
      switch (this.kind)
      {
         case MINER_FULL:
         case MINER_NOT_FULL:
         case ORE_BLOB:
         case QUAKE:
            return this.animationPeriod;
         default:
            throw new UnsupportedOperationException(
                    String.format("getAnimationPeriod not supported for %s",
                            this.kind));
      }
   }


   public  boolean moveToFull(WorldModel world,
                                    Entity target, EventScheduler scheduler)
   {
      if (position.adjacent(this.position, target.position))
      {
         return true;
      }
      else
      {
         Point nextPos = nextPositionMiner(world, target.position);

         if (!this.position.equals(nextPos))
         {
            Optional<Entity> occupant = world.getOccupant(nextPos);
            if (occupant.isPresent())
            {
               scheduler.unscheduleAllEvents(occupant.get());
            }

            moveEntity(world, nextPos);
         }
         return false;
      }
   }
   public void transformFull(WorldModel world,
                                    EventScheduler scheduler, ImageStore imageStore)
   {
      Entity miner = Functions.createMinerNotFull(this.id, this.resourceLimit,
              this.position, this.actionPeriod, this.animationPeriod,
              this.images);

      this.removeEntity(world);
      scheduler.unscheduleAllEvents(this);

      miner.addEntity(world);
      scheduler.scheduleActions(miner, world, imageStore);
   }

   public boolean moveToNotFull(WorldModel world,
                                       Entity target, EventScheduler scheduler)
   {
      if (position.adjacent(this.position, target.position))
      {
         this.resourceCount += 1;
         target.removeEntity(world);
         scheduler.unscheduleAllEvents(target);

         return true;
      }
      else
      {
         Point nextPos = this.nextPositionMiner(world, target.position);

         if (!this.position.equals(nextPos))
         {
            Optional<Entity> occupant = world.getOccupant(nextPos);
            if (occupant.isPresent())
            {
               scheduler.unscheduleAllEvents(occupant.get());
            }

            this.moveEntity(world,nextPos);
         }
         return false;
      }
   }

   public  boolean transformNotFull(WorldModel world,
                                          EventScheduler scheduler, ImageStore imageStore)
   {
      if (this.resourceCount >= this.resourceLimit)
      {
         Entity miner = Functions.createMinerFull(this.id, this.resourceLimit,
                 this.position, this.actionPeriod, this.animationPeriod,
                 this.images);

         this.removeEntity(world);
         scheduler.unscheduleAllEvents(this);

         miner.addEntity(world);
         scheduler.scheduleActions(miner, world, imageStore);

         return true;
      }

      return false;
   }

   public boolean moveToOreBlob(WorldModel world,
                                       Entity target, EventScheduler scheduler)
   {
      if (position.adjacent(this.position, target.position))
      {
         target.removeEntity(world);
         scheduler.unscheduleAllEvents(target);
         return true;
      }
      else
      {
         Point nextPos = nextPositionOreBlob(world, target.position);

         if (!this.position.equals(nextPos))
         {
            Optional<Entity> occupant = world.getOccupant(nextPos);
            if (occupant.isPresent())
            {
               scheduler.unscheduleAllEvents(occupant.get());
            }

            moveEntity(world, nextPos);
         }
         return false;
      }
   }

   /**
    * Gets the miner entity's next position.
    */
   public  Point nextPositionMiner(WorldModel world,
                                         Point destPos)
   {
      int horiz = Integer.signum(destPos.x - this.position.x);
      Point newPos = new Point(this.position.x + horiz,
              this.position.y);

      if (horiz == 0 || world.isOccupied(newPos))
      {
         int vert = Integer.signum(destPos.y - this.position.y);
         newPos = new Point(this.position.x,
                 this.position.y + vert);

         if (vert == 0 || world.isOccupied(newPos))
         {
            newPos = this.position;
         }
      }

      return newPos;
   }
   public  void moveEntity(WorldModel Model, Point pos)
   {
      Point oldPos = this.position;
      if (Model.withinBounds(pos) && !pos.equals(oldPos))
      {
         Model.setOccupancyCell(oldPos, null);
         Model.removeEntityAt(pos);
         Model.setOccupancyCell(pos, this);
         this.position = pos;
      }
   }

   /*
  Assumes that there is no entity currently occupying the
  intended destination cell.
*/
   public void addEntity(WorldModel model)
   {
      if (model.withinBounds(this.position))
      {
         model.setOccupancyCell(this.position, this);
         model.entities.add(this);
      }
   }
   public void removeEntity(WorldModel m)
   {
      m.removeEntityAt(this.position);
   }

   /**
    * Gets the ore blob entity's next position.
    */
   public Point nextPositionOreBlob(WorldModel world,
                                           Point destPos)
   {
      int horiz = Integer.signum(destPos.x - this.position.x);
      Point newPos = new Point(this.position.x + horiz,
              this.position.y);

      Optional<Entity> occupant = world.getOccupant(newPos);

      if (horiz == 0 ||
              (occupant.isPresent() && !(occupant.get().kind == EntityKind.ORE)))
      {
         int vert = Integer.signum(destPos.y - this.position.y);
         newPos = new Point(this.position.x, this.position.y + vert);
         occupant = world.getOccupant(newPos);

         if (vert == 0 ||
                 (occupant.isPresent() && !(occupant.get().kind == EntityKind.ORE)))
         {
            newPos = this.position;
         }
      }

      return newPos;
   }

   public void tryAddEntity(WorldModel world)
   {
      if (world.isOccupied(this.position))
      {
         // arguably the wrong type of exception, but we are not
         // defining our own exceptions yet
         throw new IllegalArgumentException("position occupied");
      }

      this.addEntity(world);
   }


}
