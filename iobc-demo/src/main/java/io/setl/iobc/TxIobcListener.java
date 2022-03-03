package io.setl.iobc;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

import io.setl.iobc.authenticate.AuthenticatedMessage;
import io.setl.iobc.authenticate.MessageContent;
import io.setl.iobc.model.ErrorDetails;
import io.setl.iobc.model.Response;
import io.setl.iobc.model.Response.InReplyTo;

/**
 * Listen for transaction results.
 *
 * @author Simon Greatrix on 25/11/2021.
 */
public class TxIobcListener implements IobcExtendedListener {

  /** How long to keep unexpected messages for. */
  private static final long KEEP_MESSAGES = 5 * 60 * 1000L;

  private final HashMap<InReplyTo, CompletableFuture<MessageContent>> futures = new HashMap<>();

  private final Object lock = new Object();

  private final LinkedHashMap<InReplyTo, AuthenticatedMessage> pending = new LinkedHashMap<>() {
    @Override
    protected boolean removeEldestEntry(Entry<InReplyTo, AuthenticatedMessage> eldest) {
      long now = System.currentTimeMillis();
      return (now - eldest.getValue().getCreateTime()) > KEEP_MESSAGES;
    }
  };

  private final String userId;


  public TxIobcListener(String userId) {
    this.userId = userId;
  }


  @Override
  public void acceptFailure(InReplyTo inReplyTo, String type, ErrorDetails errorDetails) {
    System.out.format("FAILURE in reply to: %s%ntype=%s%ndetails=%s%n", inReplyTo, type, errorDetails);
    CompletableFuture<MessageContent> future = futures.remove(inReplyTo);
    future.completeExceptionally(new IllegalStateException());
  }


  @Override
  public void acceptSuccess(InReplyTo inReplyTo, String type, MessageContent content) {
    System.out.format("SUCCESS in reply to: %s%ntype=%s%ndetails=%s%n", inReplyTo, type, content);
    CompletableFuture<MessageContent> future = futures.remove(inReplyTo);
    future.complete(content);
  }


  @Override
  public boolean shouldIgnore(AuthenticatedMessage message) {
    Response response = (Response) message.getContent();
    InReplyTo inReplyTo = response.getInReplyTo();
    // must be for this user
    if (!userId.equals(inReplyTo.getUserId())) {
      return true;
    }

    // may not have a future for it yet.
    synchronized (lock) {
      if (futures.containsKey(inReplyTo)) {
        // have a future, so do not ignore it
        return false;
      }

      // do not have a future, so wait for us to get a future and ignore it for now
      pending.put(inReplyTo, message);
      return true;
    }
  }


  /**
   * Provide a future which will complete when there is a reply.
   *
   * @param inReplyTo the message ID we are waiting for a reply to
   *
   * @return a future
   */
  public CompletableFuture<MessageContent> waitFor(InReplyTo inReplyTo) {
    CompletableFuture<MessageContent> newFuture = new CompletableFuture<>();
    synchronized (lock) {
      futures.put(inReplyTo, newFuture);
      AuthenticatedMessage message = pending.remove(inReplyTo);
      if (message != null) {
        accept(message);
      }
    }
    return newFuture;
  }

}