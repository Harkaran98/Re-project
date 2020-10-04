import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;
import processing.core.PImage;
import processing.core.PApplet;

public final class Functions
{




   public static final int ORE_REACH = 1;


   public static final String QUAKE_ID = "quake";
   public static final int QUAKE_ACTION_PERIOD = 1100;
   public static final int QUAKE_ANIMATION_PERIOD = 100;
   public static final int QUAKE_ANIMATION_REPEAT_COUNT = 10;

   public static final int COLOR_MASK = 0xffffff;
   public static final int KEYED_IMAGE_MIN = 5;
   private static final int KEYED_RED_IDX = 2;
   private static final int KEYED_GREEN_IDX = 3;
   private static final int KEYED_BLUE_IDX = 4;

   public static final int PROPERTY_KEY = 0;

   public static final String BGND_KEY = "background";
   public static final int BGND_NUM_PROPERTIES = 4;
   public static final int BGND_ID = 1;
   public static final int BGND_COL = 2;
   public static final int BGND_ROW = 3;

   public static final String MINER_KEY = "miner";
   public static final int MINER_NUM_PROPERTIES = 7;
   public static final int MINER_ID = 1;
   public static final int MINER_COL = 2;
   public static final int MINER_ROW = 3;
   public static final int MINER_LIMIT = 4;
   public static final int MINER_ACTION_PERIOD = 5;
   public static final int MINER_ANIMATION_PERIOD = 6;

   public static final String OBSTACLE_KEY = "obstacle";
   public static final int OBSTACLE_NUM_PROPERTIES = 4;
   public static final int OBSTACLE_ID = 1;
   public static final int OBSTACLE_COL = 2;
   public static final int OBSTACLE_ROW = 3;


   public static final int ORE_NUM_PROPERTIES = 5;
   public static final int ORE_ID = 1;
   public static final int ORE_COL = 2;
   public static final int ORE_ROW = 3;
   public static final int ORE_ACTION_PERIOD = 4;

   public static final String SMITH_KEY = "blacksmith";
   public static final int SMITH_NUM_PROPERTIES = 4;
   public static final int SMITH_ID = 1;
   public static final int SMITH_COL = 2;
   public static final int SMITH_ROW = 3;

   public static final String VEIN_KEY = "vein";
   public static final int VEIN_NUM_PROPERTIES = 5;
   public static final int VEIN_ID = 1;
   public static final int VEIN_COL = 2;
   public static final int VEIN_ROW = 3;
   public static final int VEIN_ACTION_PERIOD = 4;


   /**
    * Gets the current image associated with the specified entity.
    */
   public static PImage getCurrentImage(Object entity)
   {
      if (entity instanceof Background)
      {
         return ((Background)entity).images
            .get(((Background)entity).imageIndex);
      }
      else if (entity instanceof Entity)
      {
         return ((Entity)entity).images.get(((Entity)entity).imageIndex);
      }
      else
      {
         throw new UnsupportedOperationException(
            String.format("getCurrentImage not supported for %s",
            entity));
      }
   }









   /**
    * TODO
    */

   /**
    * TODO
    */


   /**
    * TODO
    */

   /**
    * TODO
    */

   /**
    * TODO
    */


   /**
    * TODO
    */

   /**
    * TODO
    */


   /**
    * TODO
    */


   /**
    * TODO
    */


   /**
    * TODO
    */

   /**
    * TODO
    */


   /**
    * TODO
    */








   /**
    * Asks the scheduler to removes the specified pending event.
    */
   public static void removePendingEvent(EventScheduler scheduler,
      Event event)
   {
      List<Event> pending = scheduler.pendingEvents.get(event.entity);

      if (pending != null)
      {
         pending.remove(event);
      }
   }

   /**
    * Asks the scheduler to execute all events that take place 
    * before the specified time.
    */
   public static void updateOnTime(EventScheduler scheduler, long time)
   {
      while (!scheduler.eventQueue.isEmpty() &&
         scheduler.eventQueue.peek().time < time)
      {
         Event next = scheduler.eventQueue.poll();
         
         removePendingEvent(scheduler, next);
         
         next.action.executeAction(scheduler);
      }
   }


   /**
    * Loads images listed in given file ("in"), and places them in the imageStore.
    */
   public static void loadImages(Scanner in, ImageStore imageStore,
      PApplet screen)
   {
      int lineNumber = 0;
      while (in.hasNextLine())
      {
         try
         {
            processImageLine(imageStore.images, in.nextLine(), screen);
         }
         catch (NumberFormatException e)
         {
            System.out.println(String.format("Image format error on line %d",
               lineNumber));
         }
         lineNumber++;
      }
   }

   /**
    * TODO
    */
   public static void processImageLine(Map<String, List<PImage>> images,
      String line, PApplet screen)
   {
      String[] attrs = line.split("\\s");
      if (attrs.length >= 2)
      {
         String key = attrs[0];
         PImage img = screen.loadImage(attrs[1]);
         if (img != null && img.width != -1)
         {
            List<PImage> imgs = getImages(images, key);
            imgs.add(img);

            if (attrs.length >= KEYED_IMAGE_MIN)
            {
               int r = Integer.parseInt(attrs[KEYED_RED_IDX]);
               int g = Integer.parseInt(attrs[KEYED_GREEN_IDX]);
               int b = Integer.parseInt(attrs[KEYED_BLUE_IDX]);
               setAlpha(img, screen.color(r, g, b), 0);
            }
         }
      }
   }

   /**
    * Gets the images from the imageStore associated with the given key.
    */
   public static List<PImage> getImages(Map<String, List<PImage>> images,
      String key)
   {
      List<PImage> imgs = images.get(key);
      if (imgs == null)
      {
         imgs = new LinkedList<>();
         images.put(key, imgs);
      }
      return imgs;
   }

   /*
     Called with color for which alpha should be set and alpha value.
     setAlpha(img, color(255, 255, 255), 0));
   */
   public static void setAlpha(PImage img, int maskColor, int alpha)
   {
      int alphaValue = alpha << 24;
      int nonAlpha = maskColor & COLOR_MASK;
      img.format = PApplet.ARGB;
      img.loadPixels();
      for (int i = 0; i < img.pixels.length; i++)
      {
         if ((img.pixels[i] & COLOR_MASK) == nonAlpha)
         {
            img.pixels[i] = alphaValue | nonAlpha;
         }
      }
      img.updatePixels();
   }

   /**
    * Set the viewport's row and column counts to the specified values.
    */
   public static void shift(Viewport viewport, int col, int row)
   {
      viewport.col = col;
      viewport.row = row;
   }

   /**
    * Check if the viewport contains the specified Point p.
    */
   public static boolean contains(Viewport viewport, Point p)
   {
      return p.y >= viewport.row && p.y < viewport.row + viewport.numRows &&
         p.x >= viewport.col && p.x < viewport.col + viewport.numCols;
   }

   public static void load(Scanner in, WorldModel world, ImageStore imageStore)
   {
      int lineNumber = 0;
      while (in.hasNextLine())
      {
         try
         {
            if (!processLine(in.nextLine(), world, imageStore))
            {
               System.err.println(String.format("invalid entry on line %d",
                  lineNumber));
            }
         }
         catch (NumberFormatException e)
         {
            System.err.println(String.format("invalid entry on line %d",
               lineNumber));
         }
         catch (IllegalArgumentException e)
         {
            System.err.println(String.format("issue on line %d: %s",
               lineNumber, e.getMessage()));
         }
         lineNumber++;
      }
   }

   public static boolean processLine(String line, WorldModel world,
      ImageStore imageStore)
   {
      String[] properties = line.split("\\s");
      if (properties.length > 0)
      {
         switch (properties[PROPERTY_KEY])
         {
         case BGND_KEY:
            return parseBackground(properties, world, imageStore);
         case MINER_KEY:
            return parseMiner(properties, world, imageStore);
         case OBSTACLE_KEY:
            return parseObstacle(properties, world, imageStore);
         case ORE_KEY:
            return parseOre(properties, world, imageStore);
         case SMITH_KEY:
            return parseSmith(properties, world, imageStore);
         case VEIN_KEY:
            return parseVein(properties, world, imageStore);
         }
      }

      return false;
   }

   public static boolean parseBackground(String [] properties,
      WorldModel world, ImageStore imageStore)
   {
      if (properties.length == BGND_NUM_PROPERTIES)
      {
         Point pt = new Point(Integer.parseInt(properties[BGND_COL]),
            Integer.parseInt(properties[BGND_ROW]));
         String id = properties[BGND_ID];
         setBackground(world, pt,
            new Background(id, imageStore.getImageList(id)));
      }

      return properties.length == BGND_NUM_PROPERTIES;
   }

   public static boolean parseMiner(String [] properties, WorldModel world,
      ImageStore imageStore)
   {
      if (properties.length == MINER_NUM_PROPERTIES)
      {
         Point pt = new Point(Integer.parseInt(properties[MINER_COL]),
            Integer.parseInt(properties[MINER_ROW]));
         Entity entity = createMinerNotFull(properties[MINER_ID],
            Integer.parseInt(properties[MINER_LIMIT]),
            pt,
            Integer.parseInt(properties[MINER_ACTION_PERIOD]),
            Integer.parseInt(properties[MINER_ANIMATION_PERIOD]),
                 imageStore.getImageList(MINER_KEY));
         tryAddEntity(world, entity);
      }

      return properties.length == MINER_NUM_PROPERTIES;
   }

   public static boolean parseObstacle(String [] properties, WorldModel world,
      ImageStore imageStore)
   {
      if (properties.length == OBSTACLE_NUM_PROPERTIES)
      {
         Point pt = new Point(
            Integer.parseInt(properties[OBSTACLE_COL]),
            Integer.parseInt(properties[OBSTACLE_ROW]));
         Entity entity = createObstacle(properties[OBSTACLE_ID],
            pt, imageStore.getImageList(OBSTACLE_KEY));
         tryAddEntity(world, entity);
      }

      return properties.length == OBSTACLE_NUM_PROPERTIES;
   }

   public static boolean parseOre(String [] properties, WorldModel world,
      ImageStore imageStore)
   {
      if (properties.length == ORE_NUM_PROPERTIES)
      {
         Point pt = new Point(Integer.parseInt(properties[ORE_COL]),
            Integer.parseInt(properties[ORE_ROW]));
         Entity entity = createOre(properties[ORE_ID],
            pt, Integer.parseInt(properties[ORE_ACTION_PERIOD]),
                 imageStore.getImageList(ORE_KEY));
         tryAddEntity(world, entity);
      }

      return properties.length == ORE_NUM_PROPERTIES;
   }

   public static boolean parseSmith(String [] properties, WorldModel world,
      ImageStore imageStore)
   {
      if (properties.length == SMITH_NUM_PROPERTIES)
      {
         Point pt = new Point(Integer.parseInt(properties[SMITH_COL]),
            Integer.parseInt(properties[SMITH_ROW]));
         Entity entity = createBlacksmith(properties[SMITH_ID],
            pt, imageStore.getImageList(SMITH_KEY));
         tryAddEntity(world, entity);
      }

      return properties.length == SMITH_NUM_PROPERTIES;
   }

   public static boolean parseVein(String [] properties, WorldModel world,
      ImageStore imageStore)
   {
      if (properties.length == VEIN_NUM_PROPERTIES)
      {
         Point pt = new Point(Integer.parseInt(properties[VEIN_COL]),
            Integer.parseInt(properties[VEIN_ROW]));
         Entity entity = createVein(properties[VEIN_ID],
            pt,
            Integer.parseInt(properties[VEIN_ACTION_PERIOD]),
                 imageStore.getImageList(VEIN_KEY));
         tryAddEntity(world, entity);
      }

      return properties.length == VEIN_NUM_PROPERTIES;
   }

   public static void tryAddEntity(WorldModel world, Entity entity)
   {
      if (isOccupied(world, entity.position))
      {
         // arguably the wrong type of exception, but we are not
         // defining our own exceptions yet
         throw new IllegalArgumentException("position occupied");
      }

      entity.addEntity(world);
   }



   public static boolean isOccupied(WorldModel world, Point pos)
   {
      return world.withinBounds(pos) &&
         getOccupancyCell(world, pos) != null;
   }


   public static int distanceSquared(Point p1, Point p2)
   {
      int deltaX = p1.x - p2.x;
      int deltaY = p1.y - p2.y;

      return deltaX * deltaX + deltaY * deltaY;
   }









   public static Optional<PImage> getBackgroundImage(WorldModel world,
      Point pos)
   {
      if (world.withinBounds(pos))
      {
         return Optional.of(getCurrentImage(getBackgroundCell(world, pos)));
      }
      else
      {
         return Optional.empty();
      }
   }

   public static void setBackground(WorldModel world, Point pos,
      Background background)
   {
      if (world.withinBounds(pos))
      {
         setBackgroundCell(world, pos, background);
      }
   }



   public static Entity getOccupancyCell(WorldModel world, Point pos)
   {
      return world.occupancy[pos.y][pos.x];
   }



   public static Background getBackgroundCell(WorldModel world, Point pos)
   {
      return world.background[pos.y][pos.x];
   }

   public static void setBackgroundCell(WorldModel world, Point pos,
      Background background)
   {
      world.background[pos.y][pos.x] = background;
   }

   public static Point viewportToWorld(Viewport viewport, int col, int row)
   {
      return new Point(col + viewport.col, row + viewport.row);
   }

   public static Point worldToViewport(Viewport viewport, int col, int row)
   {
      return new Point(col - viewport.col, row - viewport.row);
   }

   public static int clamp(int value, int low, int high)
   {
      return Math.min(high, Math.max(value, low));
   }

   public static void shiftView(WorldView view, int colDelta, int rowDelta)
   {
      int newCol = clamp(view.viewport.col + colDelta, 0,
         view.world.numCols - view.viewport.numCols);
      int newRow = clamp(view.viewport.row + rowDelta, 0,
         view.world.numRows - view.viewport.numRows);

      shift(view.viewport, newCol, newRow);
   }

   public static void drawBackground(WorldView view)
   {
      for (int row = 0; row < view.viewport.numRows; row++)
      {
         for (int col = 0; col < view.viewport.numCols; col++)
         {
            Point worldPoint = viewportToWorld(view.viewport, col, row);
            Optional<PImage> image = getBackgroundImage(view.world,
               worldPoint);
            if (image.isPresent())
            {
               view.screen.image(image.get(), col * view.tileWidth,
                  row * view.tileHeight);
            }
         }
      }
   }

   public static void drawEntities(WorldView view)
   {
      for (Entity entity : view.world.entities)
      {
         Point pos = entity.position;

         if (contains(view.viewport, pos))
         {
            Point viewPoint = worldToViewport(view.viewport, pos.x, pos.y);
            view.screen.image(getCurrentImage(entity),
               viewPoint.x * view.tileWidth, viewPoint.y * view.tileHeight);
         }
      }
   }

   public static void drawViewport(WorldView view)
   {
      drawBackground(view);
      drawEntities(view);
   }

   public static Action createAnimationAction(Entity entity, int repeatCount)
   {
      return new Action(ActionKind.ANIMATION, entity, null, null, repeatCount);
   }

   public static Action createActivityAction(Entity entity, WorldModel world,
      ImageStore imageStore)
   {
      return new Action(ActionKind.ACTIVITY, entity, world, imageStore, 0);
   }

   public static Entity createBlacksmith(String id, Point position,
      List<PImage> images)
   {
      return new Entity(EntityKind.BLACKSMITH, id, position, images,
         0, 0, 0, 0);
   }

   public static Entity createMinerFull(String id, int resourceLimit,
      Point position, int actionPeriod, int animationPeriod,
      List<PImage> images)
   {
      return new Entity(EntityKind.MINER_FULL, id, position, images,
         resourceLimit, resourceLimit, actionPeriod, animationPeriod);
   }

   public static Entity createMinerNotFull(String id, int resourceLimit,
      Point position, int actionPeriod, int animationPeriod,
      List<PImage> images)
   {
      return new Entity(EntityKind.MINER_NOT_FULL, id, position, images,
         resourceLimit, 0, actionPeriod, animationPeriod);
   }

   public static Entity createObstacle(String id, Point position,
      List<PImage> images)
   {
      return new Entity(EntityKind.OBSTACLE, id, position, images,
         0, 0, 0, 0);
   }

   public static Entity createOre(String id, Point position, int actionPeriod,
      List<PImage> images)
   {
      return new Entity(EntityKind.ORE, id, position, images, 0, 0,
         actionPeriod, 0);
   }

   public static Entity createOreBlob(String id, Point position,
      int actionPeriod, int animationPeriod, List<PImage> images)
   {
      return new Entity(EntityKind.ORE_BLOB, id, position, images,
            0, 0, actionPeriod, animationPeriod);
   }

   public static Entity createQuake(Point position, List<PImage> images)
   {
      return new Entity(EntityKind.QUAKE, QUAKE_ID, position, images,
         0, 0, QUAKE_ACTION_PERIOD, QUAKE_ANIMATION_PERIOD);
   }

   public static Entity createVein(String id, Point position, int actionPeriod,
      List<PImage> images)
   {
      return new Entity(EntityKind.VEIN, id, position, images, 0, 0,
         actionPeriod, 0);
   }
}
