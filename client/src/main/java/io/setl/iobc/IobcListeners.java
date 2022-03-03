package io.setl.iobc;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import io.setl.iobc.authenticate.AuthenticatedMessage;

/**
 * Bean for tracking message listeners. Note that listeners should support sufficient equality semantics to identify duplicates during add, and identify correct
 * listeners to remove.
 *
 * <p>It is assumed by this implementation that adding and removing listeners will be a rare event over the lifetime of the application.</p>
 *
 * @author Simon Greatrix on 24/11/2021.
 */
@Component
@Slf4j
public class IobcListeners {

  /** Set of all listeners. */
  private final CopyOnWriteArrayList<IobcListener> listeners = new CopyOnWriteArrayList<>();


  /**
   * Add a listener to those that receive message events.
   *
   * @param newListener the new listener
   *
   * @return True if the new listener was added. False if the new listener was already known.
   */
  public boolean addListener(IobcListener newListener) {
    if (newListener == null) {
      log.error("Attempt to add NULL listener to IobcListeners", new NullPointerException());
      return false;
    }
    return listeners.addIfAbsent(newListener);
  }


  /**
   * Notify all listeners of the message.
   *
   * @param message the message
   */
  public void notifyListeners(AuthenticatedMessage message) {
    if (log.isDebugEnabled()) {
      log.debug("Relaying message {} / {} of type {} to listeners", message.getUserId(), message.getMessageId(), message.getType());
    }
    Iterator<IobcListener> iterator = listeners.iterator();
    while (iterator.hasNext()) {
      IobcListener listener = iterator.next();
      try {
        listener.accept(message);
      } catch (Exception e) {
        log.error("Listener {} failed to process message {}", listener, message, e);
      }
    }
  }


  /**
   * Remove a listener from those that receive message events.
   *
   * @param toRemove the listener to remove
   *
   * @return True if the listener was remove. False if the listener was not known.
   */
  public boolean removeListener(IobcListener toRemove) {
    if (toRemove == null) {
      log.error("Attempted to remove NULL listener from IobcListeners", new NullPointerException());
      return true;
    }
    return listeners.remove(toRemove);
  }

}
