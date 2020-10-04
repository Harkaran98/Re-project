import java.util.List;
import java.util.Optional;

import processing.core.PImage;

public final class Background
{
   public String id;
   public List<PImage> images;
   public int imageIndex;

   public Background(String id, List<PImage> images)
   {
      this.id = id;
      this.images = images;
   }

   public void setBackground(WorldModel world, Point pos)
   {
      if (world.withinBounds(pos))
      {
         world.setBackgroundCell(pos,this);
      }
   }










}
