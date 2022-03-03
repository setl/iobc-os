package io.setl.iobc.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import io.setl.common.ParameterisedException;

/**
 * Helper for finding blocks that match a specified time.
 *
 * @author Simon Greatrix on 25/11/2021.
 */
@Component
@Slf4j
public class BlockForTimeHelper {

  /** A function that gets the epoch second time associated with a block number. */
  public interface TimeFetcher {

    /**
     * Get the epoch second associated with a block number.
     *
     * @param blockNumber the block number
     *
     * @return the epoch second
     *
     * @throws ParameterisedException if the information is unavaiable
     */
    long getEpochTime(long blockNumber) throws ParameterisedException;


    /**
     * Get the block number and epoch second of the latest block.
     *
     * @return A <code>BlockTime</code> instance
     *
     * @throws ParameterisedException if information is unavailable
     */
    BlockTime getLatestBlock() throws ParameterisedException;

  }



  /**
   * Tuple holding a block number and its associated time.
   */
  @Data
  @AllArgsConstructor
  @Builder
  public static class BlockTime {

    long blockNumber;

    long epochTime;

  }


  /**
   * Find the block that matches or immediately precedes the specified time.
   *
   * @param horizonBlock do not consider blocks before this one
   * @param targetTime   the time to match
   * @param fetcher      a fetcher of block timestamps
   *
   * @return the appropriate block
   *
   * @throws ParameterisedException if information is unavailable
   */
  public long findBlock(long horizonBlock, long targetTime, TimeFetcher fetcher) throws ParameterisedException {
    BlockTime high = fetcher.getLatestBlock();
    if (targetTime >= high.getEpochTime()) {
      // the timestamp is after the latest block, so the latest block is the best match
      log.debug("Target time {} is above high point of block {} at {}", targetTime, high.blockNumber, high.epochTime);
      return high.getBlockNumber();
    }

    BlockTime low = new BlockTime(horizonBlock, fetcher.getEpochTime(horizonBlock));
    if (targetTime <= low.epochTime) {
      log.debug("Target time {} is before horizon point of block {} at {}", targetTime, low.blockNumber, low.epochTime);
      return horizonBlock;
    }

    // The target time is strictly between our 'low' and 'high' points, so now we interpolate.
    long gapSize = high.blockNumber - low.blockNumber;
    while (gapSize > 1) {
      long gapTime = high.epochTime - low.epochTime;
      double blocksPerSecond = (double) gapSize / gapTime;
      long testBlock = low.blockNumber + (long) (blocksPerSecond * (targetTime - low.epochTime));

      // as double-to-long rounds down, we might hit the low end again, but we won't hit the top end
      if (testBlock == low.blockNumber) {
        testBlock++;
      }
      long testTime = fetcher.getEpochTime(testBlock);

      // Update our bounds
      if (targetTime < testTime) {
        high.epochTime = testTime;
        high.blockNumber = testBlock;
        log.debug("Tested block {} which is at time {}, and above the target time {}", testBlock, testTime, targetTime);
      } else if (targetTime > testTime) {
        low.epochTime = testTime;
        low.blockNumber = testBlock;
        log.debug("Tested block {} which is at time {}, and below the target time {}", testBlock, testTime, targetTime);
      } else {
        // exact match
        log.debug("Tested block {} which is at time {} and matches the target time", testBlock, testTime);
        return testBlock;
      }

      // continue the search
      gapSize = high.blockNumber - low.blockNumber;
    }

    // bounded match
    log.debug("Matched target {} between {} and {} with block {}", targetTime, low.epochTime, high.epochTime, low.blockNumber);
    return low.blockNumber;
  }

}
