import java.util.*;

public final class EventScheduler
{
   public PriorityQueue<Event> eventQueue;
   public Map<Entity, List<Event>> pendingEvents;
   public double timeScale;

   public static final int QUAKE_ANIMATION_REPEAT_COUNT = 10;

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
                    Functions.createActivityAction(entity, world, imageStore),
                    entity.actionPeriod);
            this.scheduleEvent( entity, Functions.createAnimationAction(entity, 0),
                    entity.getAnimationPeriod());
            break;

         case MINER_NOT_FULL:
            this.scheduleEvent( entity,
                    Functions.createActivityAction(entity, world, imageStore),
                    entity.actionPeriod);
            this.scheduleEvent( entity,
                    Functions.createAnimationAction(entity, 0), entity.getAnimationPeriod());
            break;

         case ORE:
            this.scheduleEvent( entity,
                    Functions.createActivityAction(entity, world, imageStore),
                    entity.actionPeriod);
            break;

         case ORE_BLOB:
            this.scheduleEvent( entity,
                    Functions.createActivityAction(entity, world, imageStore),
                    entity.actionPeriod);
            this.scheduleEvent( entity,
                    Functions.createAnimationAction(entity, 0), entity.getAnimationPeriod());
            break;

         case QUAKE:
            this.scheduleEvent( entity,
                    Functions.createActivityAction(entity, world, imageStore),
                    entity.actionPeriod);
            this.scheduleEvent( entity,
                    Functions.createAnimationAction(entity, QUAKE_ANIMATION_REPEAT_COUNT),
                    entity.getAnimationPeriod());
            break;

         case VEIN:
            this.scheduleEvent(entity,
                    Functions.createActivityAction(entity, world, imageStore),
                    entity.actionPeriod);
            break;

         default:
      }
   }



   /**
    * Asks the scheduler to execute all events that take place
    * before the specified time.
    */
   public void updateOnTime(long time)
   {
      while (!this.eventQueue.isEmpty() &&
              this.eventQueue.peek().time < time)
      {
         Event next = this.eventQueue.poll();

         next.removePendingEvent(this);

         next.action.executeAction(this);
      }
   }


}
