package filodb.core

import java.nio.ByteBuffer
import scala.math.Ordering

/**
 * Definitions for sort keys and key ranges.
 * Sort keys must be linearizable so all the data in a partition can be evenly divisible into segments.
 * For more info, see [[doc/sorted_chunk_merge.md]].
 */

/**
 * A typeclass for working with sort keys.
 */
trait SortKeyHelper[K] {
  def ordering: Ordering[K]    // must be comparable

  /**
   * Returns the inclusive start and exclusive end keys for the segment corresponding to a sort key.
   * Must return the same start and end for all keys within [start, end) of a segment.
   */
  def getSegment(key: K): (K, K)

  def toBytes(key: K): ByteBuffer
  def fromBytes(bytes: ByteBuffer): K
}

/**
 * A typeclass for a Long-based sort key
 */
case class LongKeyHelper(segmentLen: Long) extends SortKeyHelper[Long] {
  def ordering: Ordering[Long] = Ordering.Long
  def getSegment(key: Long): (Long, Long) = {
    val segmentNum = key / segmentLen
    (segmentNum * segmentLen, (segmentNum + 1) * segmentLen)
  }
  def toBytes(key: Long): ByteBuffer =
    ByteBuffer.allocate(java.lang.Long.BYTES).putLong(key).flip.asInstanceOf[ByteBuffer]
  def fromBytes(bytes: ByteBuffer): Long = bytes.getLong
}

