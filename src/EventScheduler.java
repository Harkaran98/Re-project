import java.util.*;

public final class EventScheduler
{
   public PriorityQueue<Event> eventQueue;
   public Map<Entity, List<Event>> pendingEvents;
   public double timeScale;

   public EventScheduler(double timeScale)
   {
      this.eventQueue = new PriorityQueue<>(new EventComparator());
      this.pendingEvents = new HashMap<>();
      this.timeScale = timeScale;
   }


   /**
    * Asks the scheduler to schedule an Action for the given entity,
    * to be take after the specified period of time.
    */
   public void scheduleEvent(Entity entity, Action action, long afterPeriod)
   {
      long time = System.currentTimeMillis() +
              (long)(afterPeriod * timeScale);
      Event event = new Event(action, time, entity);

      eventQueue.add(event);

      // update list of pending events for the given entity
      List<Event> pending = pendingEvents.getOrDefault(entity,
              new LinkedList<>());
      pending.add(event);
      pendingEvents.put(entity, pending);
   }

   /**
    * Asks the scheduler to unschedule all events for the given entity.
    */
   public void unscheduleAllEvents(Entity entity)
   {
      List<Event> pending = this.pendingEvents.remove(entity);

      if (pending != null)
      {
         for (Event event : pending)
         {
            this.eventQueue.remove(event);
         }
      }
   }

   public void scheduleActions(Entity entity,WorldModel world, ImageStore imageStore)
   {
      switch (entity.kind)
      {
         case MINER_FULL:
            this.scheduleEvent( entity,
                    createActivityAction(entity, world, imageStore),
                    entity.actionPeriod);
            this.scheduleEvent( entity, createAnimationAction(entity, 0),
                    entity.getAnimationPeriod());
            break;

         case MINER_NOT_FULL:
            this.scheduleEvent( entity,
                    createActivityAction(entity, world, imageStore),
                    entity.actionPeriod);
            this.scheduleEvent( entity,
                    createAnimationAction(entity, 0), entity.getAnimationPeriod());
            break;

         case ORE:
            this.scheduleEvent( entity,
                    createActivityAction(entity, world, imageStore),
                    entity.actionPeriod);
            break;

         case ORE_BLOB:
            this.scheduleEvent( entity,
                    createActivityAction(entity, world, imageStore),
                    entity.actionPeriod);
            this.scheduleEvent( entity,
                    createAnimationAction(entity, 0), entity.getAnimationPeriod());
            break;

         case QUAKE:
            this.scheduleEvent( entity,
                    createActivityAction(entity, world, imageStore),
                    entity.actionPeriod);
            this.scheduleEvent( entity,
                    createAnimationAction(entity, QUAKE_ANIMATION_REPEAT_COUNT),
                    entity.getAnimationPeriod());
            break;

         case VEIN:
            this.scheduleEvent(entity,
                    createActivityAction(entity, world, imageStore),
                    entity.actionPeriod);
            break;

         default:
      }
   }
}
