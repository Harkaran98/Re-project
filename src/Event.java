import java.util.List;

public final class Event
{
   public Action action;
   public long time;
   private Entity entity;

   public Event(Action action, long time, Entity entity)
   {
      this.action = action;
      this.time = time;
      this.entity = entity;
   }


   /**
    * Asks the scheduler to removes the specified pending event.
    */
   public void removePendingEvent(EventScheduler es)
   {
      List<Event> pending = es.pendingEvents.get(this.entity);

      if (pending != null)
      {
         pending.remove(this);
      }
   }
}
