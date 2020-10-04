public final class Viewport
{
   public int row;
   public int col;
   public int numRows;
   public int numCols;

   public Viewport(int numRows, int numCols)
   {
      this.numRows = numRows;
      this.numCols = numCols;
   }

   /**
    * Set the viewport's row and column counts to the specified values.
    */
   public void shift(int col, int row)
   {
      this.col = col;
      this.row = row;
   }

   /**
    * Check if the viewport contains the specified Point p.
    */
   public boolean contains(Point p)
   {
      return p.y >= this.row && p.y < this.row + this.numRows &&
              p.x >= this.col && p.x < this.col + this.numCols;
   }


   public Point viewportToWorld(int col, int row)
   {
      return new Point(col + this.col, row + this.row);
   }

   public  Point worldToViewport(int col, int row)
   {
      return new Point(col - this.col, row - this.row);
   }
   public void drawViewport(WorldView view)
   {
      view.drawBackground();
      view.drawEntities();
   }

}
