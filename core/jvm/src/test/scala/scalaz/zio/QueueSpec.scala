package scalaz.zio

import scala.collection.immutable.Range
import org.specs2.specification.AroundTimeout
import scalaz.zio.QueueSpec.waitForSize
import scalaz.zio.duration._

class QueueSpec(implicit ee: org.specs2.concurrent.ExecutionEnv) extends AbstractRTSSpec with AroundTimeout {

  def is =
    "QueueSpec".title ^ s2"""
    Make a Queue and
    add values then call
     `take` to retrieve them in correct order. $e1
    `take` is called by fiber waiting on values to be added to the queue and
     join the fiber to get the added values correctly. $e2
    fork 10 takers and offer 10 values, join takers, the result must contain all offered values $e3
    fork 10 putters and offer for each one 10 values then take the values 100 times,
     the values must be correct after join those fibers $e4
    make a bounded queue with capacity = 10, then put 10 values then add 10 other values and
     check that `offer`is suspended $e5
    make a bounded queue with capacity = 5, offer 10 values in a fiber and
     check that you can take the 10 values $e6
    `take` can be interrupted and all resources in takers are released $e7
    `offer` can be interrupted and all resources in putters are released $e8
    in an unbounded queue add values then call `take`, the values must be in correct order $e9
    in an unbounded queue add values then call `takeAll`, the values must be in correct order $e10
    in an unbounded queue call `takeAll` in an empty queue must return an empty list $e11
    in a queue with capacity = 4 add 5 values then call `takeAll`,
     it must return a list with the 4 first values in correct order $e12
    make an empty queue, and `takeUpTo` with max = 2, must return an empty list $e13
    make a bounded queue of size 100, call `takeUpTo` with max = 101  without adding values
     must return an empty list $e14
    make a bounded queue, offer 2 values, `takeUpTo` with max = 2, must return a list that contains
     the first 2 offered values $e15
    make a bounded queue, offer 4 values, `takeUpTo` with max = 2, must return a list that contains
     the first 2 values $e16
    make a bounded queue, offer 4 values, `takeUpTo` with max = 10, must return a list that contains
     the offered values $e17
    make a bounded queue, offer 4 values, `takeUpTo` with max = 0, must return an empty list $e18
    make a bounded queue, offer 1 value, `takeUpTo` with max = -1, must return an empty list $e19
    make a bounded queue, offer 2 values, `takeUpTo` with max = 2, offer 2 values again,
     and `takeUpTo` with max = 2 again, the first result must be a list that contains the first 2 values and
      the second one must be a list with the second 2 values in order $e20
    make a bounded queue, offer 4 values, `takeUpTo` with max = 2, and then `takeUpTo` again with max = 2;
     the first result must contain the first 2 values and the second one must
      contain the next 2 values in order $e21
    make a bounded queue of size 4, fork offer 5 values, and `takeUpTo` with max=5 must return a list that
     contains the first 4 values in correct order $e22
    make a bounded queue of size 10 then call `offerAll` with a list of 10 elements to add
     all values in the queue $e23
    make a bounded queue of size 2 then call `offerAll` with a list of 3 elements. The producer should be
     suspended and the queue should have the same size as the elements offered $e24
    `offerAll` can be interrupted and all resources are released $e25
    `offerAll should preserve the order of the list $e26
    `offerAll` does preserve the order of the list when it exceeds the queue's capacity $e27
    make a bounded queue of size 50 then fork 100 takers, and offer as many elements as there are takers,
     the values must be correct after joining those fibers $e28
    make a bounded queue of size 256 then fork 64 takers, and offer more elements than there are takers,
     the values must be correct after joining those fibers $e29
    make a bounded queue of size 32 then fork 128 takers, and offer more elements than there are takers and
     capacity in the queue, the values must be correct after joining those fibers $e30
    fork some takers, and offer less elements than there are takers in the queue, the values must be correct
     after joining those fibers $e31
    make bounded queue of size 2 then offer more elements than there is capacity in the queue, taking elements
     should work correctly $e32
    make bounded queue offer more elements than there are takers and capacity in the queue, taking elements
     should preserve putters queue order $e33
    make bounded queue of size 100 then `offer` one element then `offerAll` some elements without exceeding
     the queue's capacity, when calling `takeAll` the values should be in correct order $e34
    make bounded queue `offer` some elements then `offerAll` elements exceeding the queue's capacity,
     the values should be in correct order $e35
    make a bounded queue of size 3, `take` a value in a fork, then `shutdown` the queue,
     the fork should be interrupted $e36
    make a sliding queue of size 1, `take` a value in a fork, then `shutdown` the queue,
      the fork should be interrupted $e37
    make a bounded queue of size 2, `offer` a value 3 times, then `shutdown` the queue, the third fork should be interrupted $e38
    make a bounded queue of size 1, `shutdown` the queue, then `offer` an element, `offer` should be interrupted $e39
    make a bounded queue of size 1, `shutdown` the queue, then `take` an element, `take` should be interrupted $e40
    make a bounded queue of size 1, `shutdown` the queue, then `takeAll` elements, `takeAll` should be interrupted $e41
    make a bounded queue of size 1, `shutdown` the queue, then `takeUpTo` 1 element, `takeUpTo` should be interrupted $e42
    make a bounded queue of size 1, `shutdown` the queue, then get the `size`, `size` should be interrupted $e43
    make a bounded queue, fill it with one offer waiting, calling `take` should free the waiting offer $e44
    make a bounded queue, fill it with one offer waiting, calling `takeAll` should free the waiting offer $e45
    make a bounded queue, fill it with one offer waiting, calling `takeUpTo` should free the waiting offer $e46
    make a bounded queue with capacity 2, fill it then offer 3 more items, calling `takeAll` 3 times should return the first 2 items, then the next 2, then the last one $e47
    make a sliding queue of size 2, offering 3 values should return false and the first should be dropped $e48
    make a sliding queue of size 2, offering 3 values should return false$e49
    make a sliding queue of size 100, offer values and retrieve in correct order $e50
    make a sliding queue, forking takers, offering values and joining fibers should return correct value $e51
    make a sliding queue of size 2, offering 6 values the queue slides correctly $e52
    make a bounded queue, create a shutdown hook completing a promise, then shutdown the queue, the promise should be completed $e53
    make a bounded queue, create a shutdown hook completing a promise twice, then shutdown the queue, both promises should be completed $e54
    make a bounded queue, shut it down, create a shutdown hook completing a promise, the promise should be completed immediately $e55
    make a dropping queue of size 4, offering 5 values and the last should be dropped $e56
    make a dropping queue of size 2, offering 6 values should return false $e57
    make a dropping queue of size 128, offer values up to 256 and retrieve up to 128 in correct order $e58
    make a dropping queue, forking takers, offering values and joining fibers should return the correct value $e59
    make a dropping queue of size 2, offering 6 values the queue drops offers correctly $e60
    make a dropping queue of size 5, offer 3 values and receive all 3 values back and should return true $e61
    make a dropping queue of size 2, fork a take and then offer 4 values. Must return first item upon join $e62
    make a sliding queue of size 2, fork a take and then offer 4 values. Must return last item upon join $e63
    make a sliding queue of size 5 and offer 3 values. offerAll must return true $e64
    make a bounded queue of size 5 and offer 3 values. offerAll must return true $e65
    """

  def e1 = unsafeRun(
    for {
      queue <- Queue.bounded[Int](100)
      o1    <- queue.offer(10)
      v1    <- queue.take
      o2    <- queue.offer(20)
      v2    <- queue.take
    } yield (v1 must_=== 10).and(v2 must_=== 20).and(o1 must beTrue).and(o2 must beTrue)
  )

  def e2 = unsafeRun(
    for {
      queue <- Queue.bounded[String](100)
      f1 <- queue.take
             .seqWith(queue.take)(_ + _)
             .fork
      _ <- queue.offer("don't ") *> queue.offer("give up :D")
      v <- f1.join
    } yield v must_=== "don't give up :D"
  )

  def e3 =
    unsafeRun(for {
      queue  <- Queue.bounded[Int](10)
      f      <- IO.forkAll(List.fill(10)(queue.take))
      values = Range.inclusive(1, 10).toList
      _      <- values.map(queue.offer).foldLeft[IO[Nothing, Boolean]](IO.now(false))(_ *> _)
      v      <- f.join
    } yield v must containTheSameElementsAs(values))

  def e4 =
    unsafeRun(for {
      queue  <- Queue.bounded[Int](10)
      values = Range.inclusive(1, 10).toList
      f      <- IO.forkAll(values.map(queue.offer))
      _      <- waitForSize(queue, 10)
      l      <- queue.take.repeat(Schedule.recurs(9) *> Schedule.identity[Int].collect)
      _      <- f.join
    } yield l must containTheSameElementsAs(values))

  def e5 =
    unsafeRun((for {
      queue        <- Queue.bounded[Int](10)
      _            <- queue.offer(1).repeat(Schedule.recurs(9))
      refSuspended <- Ref[Boolean](true)
      _            <- (queue.offer(2).repeat(Schedule.recurs(9)) *> refSuspended.set(false)).fork
      isSuspended  <- refSuspended.get
    } yield isSuspended must beTrue).supervised)

  def e6 =
    unsafeRun(
      for {
        queue  <- Queue.bounded[Int](5)
        values = Range.inclusive(1, 10).toList
        _      <- IO.forkAll(values.map(queue.offer))
        _      <- waitForSize(queue, 10)
        l <- queue.take
              .repeat(Schedule.recurs(9) *> Schedule.identity[Int].collect)
      } yield l must containTheSameElementsAs(values)
    )

  def e7 = unsafeRun(
    for {
      queue <- Queue.bounded[Int](100)
      f     <- queue.take.fork
      _     <- waitForSize(queue, -1)
      _     <- f.interrupt
      size  <- queue.size
    } yield size must_=== 0
  )

  def e8 = unsafeRun(
    for {
      queue <- Queue.bounded[Int](2)
      _     <- queue.offer(1)
      _     <- queue.offer(1)
      f     <- queue.offer(1).fork
      _     <- waitForSize(queue, 3)
      _     <- f.interrupt
      size  <- queue.size
    } yield size must_=== 2
  )

  def e9 = unsafeRun(
    for {
      queue <- Queue.unbounded[Int]
      _     <- queue.offer(1)
      _     <- queue.offer(2)
      _     <- queue.offer(3)
      v1    <- queue.take
      v2    <- queue.take
      v3    <- queue.take
    } yield (v1 must_=== 1).and(v2 must_=== 2).and(v3 must_=== 3)
  )

  def e10 = unsafeRun(
    for {
      queue <- Queue.unbounded[Int]
      _     <- queue.offer(1)
      _     <- queue.offer(2)
      _     <- queue.offer(3)
      v     <- queue.takeAll
    } yield v must_=== List(1, 2, 3)
  )

  def e11 = unsafeRun(
    for {
      queue <- Queue.unbounded[Int]
      c     <- queue.takeAll
      _     <- queue.offer(1)
      _     <- queue.take
      v     <- queue.takeAll
    } yield (c must_=== List.empty).and(v must_=== List.empty)
  )

  def e12 =
    unsafeRun(for {
      queue  <- Queue.bounded[Int](4)
      values = List(1, 2, 3, 4)
      _      <- values.map(queue.offer).foldLeft(IO.now(false))(_ *> _)
      _      <- queue.offer(5).fork
      _      <- waitForSize(queue, 5)
      v      <- queue.takeAll
      c      <- queue.take
    } yield (v must containTheSameElementsAs(values)).and(c must_=== 5))

  def e13 = unsafeRun(
    for {
      queue <- Queue.bounded[Int](100)
      list  <- queue.takeUpTo(2)
    } yield list must_=== Nil
  )

  def e14 = unsafeRun(
    for {
      queue <- Queue.bounded[Int](100)
      list  <- queue.takeUpTo(101)
    } yield list must_=== Nil
  )

  def e15 = unsafeRun(
    for {
      queue <- Queue.bounded[Int](100)
      _     <- queue.offer(10)
      _     <- queue.offer(20)
      list  <- queue.takeUpTo(2)
    } yield list must_=== List(10, 20)
  )

  def e16 = unsafeRun(
    for {
      queue <- Queue.bounded[Int](100)
      _     <- queue.offer(10)
      _     <- queue.offer(20)
      _     <- queue.offer(30)
      _     <- queue.offer(40)
      list  <- queue.takeUpTo(2)
    } yield list must_=== List(10, 20)
  )

  def e17 = unsafeRun(
    for {
      queue <- Queue.bounded[Int](100)
      _     <- queue.offer(10)
      _     <- queue.offer(20)
      _     <- queue.offer(30)
      _     <- queue.offer(40)
      list  <- queue.takeUpTo(10)
    } yield list must_=== List(10, 20, 30, 40)
  )

  def e18 = unsafeRun(
    for {
      queue <- Queue.bounded[Int](100)
      _     <- queue.offer(10)
      _     <- queue.offer(20)
      _     <- queue.offer(30)
      _     <- queue.offer(40)
      list  <- queue.takeUpTo(0)
    } yield list must_=== Nil
  )

  def e19 = unsafeRun(
    for {
      queue <- Queue.bounded[Int](100)
      _     <- queue.offer(10)
      list  <- queue.takeUpTo(-1)
    } yield list must_=== Nil
  )

  def e20 = unsafeRun(
    for {
      queue <- Queue.bounded[Int](100)
      _     <- queue.offer(10)
      _     <- queue.offer(20)
      list1 <- queue.takeUpTo(2)
      _     <- queue.offer(30)
      _     <- queue.offer(40)
      list2 <- queue.takeUpTo(2)
    } yield (list1, list2) must_=== ((List(10, 20), List(30, 40)))
  )

  def e21 = unsafeRun(
    for {
      queue <- Queue.bounded[Int](100)
      _     <- queue.offer(10)
      _     <- queue.offer(20)
      _     <- queue.offer(30)
      _     <- queue.offer(40)
      list1 <- queue.takeUpTo(2)
      list2 <- queue.takeUpTo(2)
    } yield (list1, list2) must_=== ((List(10, 20), List(30, 40)))
  )

  def e22 =
    unsafeRun((for {
      queue  <- Queue.bounded[Int](4)
      values = List(1, 2, 3, 4)
      _      <- values.map(queue.offer).foldLeft(IO.now(false))(_ *> _)
      _      <- queue.offer(5).fork
      _      <- waitForSize(queue, 5)
      l      <- queue.takeUpTo(5)
    } yield l must_=== List(1, 2, 3, 4)).supervised)

  def e23 =
    unsafeRun(for {
      queue  <- Queue.bounded[Int](10)
      orders = Range.inclusive(1, 10).toList
      _      <- queue.offerAll(orders)
      _      <- waitForSize(queue, 10)
      l      <- queue.takeAll
    } yield l must_=== orders)

  def e24 =
    unsafeRun(for {
      queue  <- Queue.bounded[Int](2)
      orders = Range.inclusive(1, 3).toList
      _      <- queue.offerAll(orders).fork
      size   <- waitForSize(queue, 3)
      l      <- queue.takeAll
    } yield (size must_=== 3).and(l must_=== List(1, 2)))

  def e25 =
    unsafeRun(for {
      queue   <- Queue.bounded[Int](2)
      orders1 = Range.inclusive(1, 2).toList
      orders2 = Range.inclusive(3, 4).toList
      _       <- queue.offerAll(orders1)
      f       <- queue.offerAll(orders2).fork
      _       <- waitForSize(queue, 4)
      _       <- f.interrupt
      l1      <- queue.takeAll
      l2      <- queue.takeAll
    } yield (l1 must_=== orders1).and(l2 must_=== Nil))

  def e26 =
    unsafeRun(for {
      queue  <- Queue.bounded[Int](100)
      orders = Range.inclusive(1, 100).toList
      _      <- queue.offerAll(orders)
      _      <- waitForSize(queue, 100)
      l      <- queue.takeAll
    } yield l must_=== orders)

  def e27 =
    unsafeRun(for {
      queue  <- Queue.bounded[Int](64)
      orders = Range.inclusive(1, 128).toList
      _      <- queue.offerAll(orders).fork
      _      <- waitForSize(queue, 128)
      l      <- queue.takeAll
    } yield l must_=== Range.inclusive(1, 64).toList)

  def e28 =
    unsafeRun(for {
      queue  <- Queue.bounded[Int](50)
      orders = Range.inclusive(1, 100).toList
      takers <- IO.forkAll(List.fill(100)(queue.take))
      _      <- waitForSize(queue, -100)
      _      <- queue.offerAll(orders)
      l      <- takers.join
      s      <- queue.size
    } yield (l.toSet must_=== orders.toSet).and(s must_=== 0))

  def e29 =
    unsafeRun(for {
      queue  <- Queue.bounded[Int](256)
      orders = Range.inclusive(1, 128).toList
      takers <- IO.forkAll(List.fill(64)(queue.take))
      _      <- waitForSize(queue, -64)
      _      <- queue.offerAll(orders)
      l      <- takers.join
      s      <- queue.size
      values = orders.take(64)
    } yield (l must containTheSameElementsAs(values)).and(s must_=== 64))

  def e30 =
    unsafeRun(for {
      queue  <- Queue.bounded[Int](32)
      orders = Range.inclusive(1, 256).toList
      takers <- IO.forkAll(List.fill(128)(queue.take))
      _      <- waitForSize(queue, -128)
      _      <- queue.offerAll(orders).fork
      l      <- takers.join
      _      <- waitForSize(queue, 128)
      values = orders.take(128)
    } yield l must containTheSameElementsAs(values))

  def e31 =
    unsafeRun(for {
      queue  <- Queue.bounded[Int](200)
      values = Range.inclusive(1, 100).toList
      takers <- IO.forkAll(List.fill(100)(queue.take))
      _      <- waitForSize(queue, -100)
      _      <- IO.forkAll(List.fill(100)(queue.take))
      _      <- waitForSize(queue, -200)
      _      <- queue.offerAll(values)
      l      <- takers.join
      s      <- queue.size
    } yield (l must containTheSameElementsAs(values)).and(s must_=== -100))

  def e32 =
    unsafeRun(for {
      queue  <- Queue.bounded[Int](2)
      orders = Range.inclusive(1, 3).toList
      _      <- queue.offerAll(orders).fork
      _      <- waitForSize(queue, 3)
      v1     <- queue.take
      v2     <- queue.take
      v3     <- queue.take
    } yield (v1 must_=== 1).and(v2 must_=== 2).and(v3 must_=== 3))

  def e33 =
    unsafeRun(
      for {
        queue   <- Queue.bounded[Int](2)
        orders  = Range.inclusive(1, 3).toList
        orders2 = Range.inclusive(4, 5).toList
        _       <- queue.offerAll(orders).fork
        _       <- waitForSize(queue, 3)
        _       <- queue.offerAll(orders2).fork
        _       <- waitForSize(queue, 5)
        v1      <- queue.take
        v2      <- queue.take
        v3      <- queue.take
        v4      <- queue.take
        v5      <- queue.take
      } yield (v1 must_=== 1).and(v2 must_=== 2).and(v3 must_=== 3).and(v4 must_=== 4).and(v5 must_=== 5)
    )

  def e34 =
    unsafeRun(
      for {
        queue  <- Queue.bounded[Int](1000)
        orders = Range.inclusive(2, 1000).toList
        _      <- queue.offer(1)
        _      <- queue.offerAll(orders)
        _      <- waitForSize(queue, 1000)
        v1     <- queue.takeAll
      } yield v1 must_=== Range.inclusive(1, 1000).toList
    )

  def e35 =
    unsafeRun(
      for {
        queue  <- Queue.bounded[Int](32)
        orders = Range.inclusive(3, 35).toList
        _      <- queue.offer(1)
        _      <- queue.offer(2)
        _      <- queue.offerAll(orders).fork
        _      <- waitForSize(queue, 35)
        v      <- queue.takeAll
        v1     <- queue.take
        v2     <- queue.take
        v3     <- queue.take
      } yield
        (v must_=== Range.inclusive(1, 32).toList)
          .and(v1 must_=== 33)
          .and(v2 must_=== 34)
          .and(v3 must_=== 35)
    )

  def e36 =
    unsafeRunSync(
      for {
        queue <- Queue.bounded[Int](3)
        f     <- queue.take.fork
        _     <- waitForSize(queue, -1)
        _     <- queue.shutdown
        _     <- f.join
      } yield ()
    ) must_=== ExitResult.interrupted

  def e37 =
    unsafeRunSync(
      for {
        queue <- Queue.sliding[Int](1)
        f     <- queue.take.fork
        _     <- waitForSize(queue, -1)
        _     <- queue.shutdown
        _     <- f.join
      } yield ()
    ) must_=== ExitResult.interrupted

  def e38 =
    unsafeRunSync(
      for {
        queue <- Queue.bounded[Int](2)
        _     <- queue.offer(1)
        _     <- queue.offer(1)
        f     <- queue.offer(1).fork
        _     <- waitForSize(queue, 3)
        _     <- queue.shutdown
        _     <- f.join
      } yield ()
    ) must_=== ExitResult.interrupted

  def e39 =
    unsafeRunSync(
      for {
        queue <- Queue.bounded[Int](1)
        _     <- queue.shutdown
        _     <- queue.offer(1)
      } yield ()
    ) must_=== ExitResult.interrupted

  def e40 =
    unsafeRunSync(
      for {
        queue <- Queue.bounded[Int](1)
        _     <- queue.shutdown
        _     <- queue.take
      } yield ()
    ) must_=== ExitResult.interrupted

  def e41 =
    unsafeRunSync(
      for {
        queue <- Queue.bounded[Int](1)
        _     <- queue.shutdown
        _     <- queue.takeAll
      } yield ()
    ) must_=== ExitResult.interrupted

  def e42 =
    unsafeRunSync(
      for {
        queue <- Queue.bounded[Int](1)
        _     <- queue.shutdown
        _     <- queue.takeUpTo(1)
      } yield ()
    ) must_=== ExitResult.interrupted

  def e43 =
    unsafeRunSync(
      for {
        queue <- Queue.bounded[Int](1)
        _     <- queue.shutdown
        _     <- queue.size
      } yield ()
    ) must_=== ExitResult.interrupted

  def e44 = unsafeRun(
    for {
      queue <- Queue.bounded[Int](2)
      _     <- queue.offerAll(List(1, 2))
      f     <- queue.offer(3).fork
      _     <- waitForSize(queue, 3)
      v1    <- queue.take
      v2    <- queue.take
      _     <- f.join
    } yield (v1 must_=== 1).and(v2 must_=== 2)
  )

  def e45 = unsafeRun(
    for {
      queue <- Queue.bounded[Int](2)
      _     <- queue.offerAll(List(1, 2))
      f     <- queue.offer(3).fork
      _     <- waitForSize(queue, 3)
      v1    <- queue.takeAll
      _     <- f.join
    } yield v1 must_=== List(1, 2)
  )

  def e46 = unsafeRun(
    for {
      queue <- Queue.bounded[Int](2)
      _     <- queue.offerAll(List(1, 2))
      f     <- queue.offer(3).fork
      _     <- waitForSize(queue, 3)
      v1    <- queue.takeUpTo(2)
      _     <- f.join
    } yield v1 must_=== List(1, 2)
  )

  def e47 = unsafeRun(
    for {
      queue <- Queue.bounded[Int](2)
      _     <- queue.offerAll(List(1, 2))
      f     <- queue.offerAll(List(3, 4, 5)).fork
      _     <- waitForSize(queue, 5)
      v1    <- queue.takeAll
      v2    <- queue.takeAll
      v3    <- queue.takeAll
      _     <- f.join
    } yield (v1 must_=== List(1, 2)).and(v2 must_=== List(3, 4)).and(v3 must_=== List(5))
  )

  def e48 = unsafeRun(
    for {
      queue <- Queue.sliding[Int](2)
      _     <- queue.offer(1)
      v1    <- queue.offer(2)
      v2    <- queue.offer(3)
      l     <- queue.takeAll
    } yield (l must_=== List(2, 3)).and(v1 must beTrue).and(v2 must beFalse)
  )

  def e49 = unsafeRun(
    for {
      queue <- Queue.sliding[Int](2)
      v     <- queue.offerAll(List(1, 2, 3))
      size  <- queue.size
    } yield (size must_=== 2).and(v must beFalse)
  )

  def e50 = unsafeRun(
    for {
      queue <- Queue.sliding[Int](100)
      _     <- queue.offer(1)
      _     <- queue.offer(2)
      _     <- queue.offer(3)
      l     <- queue.takeAll
    } yield l must_=== List(1, 2, 3)
  )

  def e51 = unsafeRun(
    for {
      queue <- Queue.sliding[Int](5)
      f1 <- queue.take
             .seqWith(queue.take)(_ + _)
             .fork
      _ <- queue.offer(1) *> queue.offer(2)
      v <- f1.join
    } yield v must_=== 3
  )

  def e52 = unsafeRun(
    for {
      queue <- Queue.sliding[Int](2)
      v1    <- queue.offerAll(Iterable(1, 2, 3, 4, 5, 6))
      l     <- queue.takeAll
    } yield (l must_=== List(5, 6)).and(v1 must beFalse)
  )

  def e53 = unsafeRun(
    for {
      queue <- Queue.bounded[Int](3)
      p     <- Promise.make[Nothing, Boolean]
      _     <- (queue.awaitShutdown *> p.complete(true)).fork
      _     <- queue.shutdown
      res   <- p.get
    } yield res must beTrue
  )

  def e54 = unsafeRun(
    for {
      queue <- Queue.bounded[Int](3)
      p1    <- Promise.make[Nothing, Boolean]
      p2    <- Promise.make[Nothing, Boolean]
      _     <- (queue.awaitShutdown *> p1.complete(true)).fork
      _     <- (queue.awaitShutdown *> p2.complete(true)).fork
      _     <- queue.shutdown
      res1  <- p1.get
      res2  <- p2.get
    } yield (res1 must beTrue).and(res2 must beTrue)
  )

  def e55 = unsafeRun(
    for {
      queue <- Queue.bounded[Int](3)
      _     <- queue.shutdown
      p     <- Promise.make[Nothing, Boolean]
      _     <- (queue.awaitShutdown *> p.complete(true)).fork
      res   <- p.get
    } yield res must beTrue
  )

  def e56 = unsafeRun(
    for {
      capacity <- IO.now(4)
      queue    <- Queue.dropping[Int](capacity)
      iter     = Range.inclusive(1, 5)
      _        <- queue.offerAll(iter)
      ta       <- queue.takeAll
    } yield (ta must_=== List(1, 2, 3, 4)).and(ta.size must_=== capacity)
  )

  def e57 = unsafeRun(
    for {
      capacity <- IO.now(2)
      queue    <- Queue.dropping[Int](capacity)
      v1       <- queue.offerAll(Iterable(1, 2, 3, 4, 5, 6))
      ta       <- queue.takeAll
    } yield (ta.size must_=== 2).and(v1 must beFalse)
  )

  def e58 = unsafeRun(
    for {
      capacity <- IO.now(128)
      queue    <- Queue.dropping[Int](capacity)
      iter     = Range.inclusive(1, 256)
      _        <- queue.offerAll(iter)
      ta       <- queue.takeAll
    } yield (ta must_=== Range.inclusive(1, 128).toList).and(ta.size must_=== capacity)
  )

  def e59 = unsafeRun(
    for {
      queue <- Queue.dropping[Int](5)
      f1 <- queue.take
             .seqWith(queue.take)(_ + _)
             .fork
      _ <- queue.offer(1) *> queue.offer(2)
      v <- f1.join
    } yield v must_=== 3
  )

  def e60 = unsafeRun(
    for {
      capacity <- IO.now(2)
      queue    <- Queue.dropping[Int](capacity)
      iter     = Range.inclusive(1, 6)
      _        <- queue.offerAll(iter)
      ta       <- queue.takeAll
    } yield (ta must_=== List(1, 2)).and(ta.size must_=== capacity)
  )

  def e61 = unsafeRun(
    for {
      capacity <- IO.now(5)
      queue    <- Queue.dropping[Int](capacity)
      iter     = Range.inclusive(1, 3)
      v1       <- queue.offerAll(iter)
      ta       <- queue.takeAll
    } yield (ta must_=== List(1, 2, 3)).and(v1 must beTrue)
  )

  def e62 = unsafeRun(
    for {
      capacity <- IO.now(2)
      queue    <- Queue.dropping[Int](capacity)
      iter     = Range.inclusive(1, 4)
      f        <- queue.take.fork
      _        <- waitForSize(queue, -1)
      oa       <- queue.offerAll(iter.toList)
      j        <- f.join
    } yield (j must_=== 1).and(oa must beFalse)
  )

  def e63 = unsafeRun(
    for {
      capacity <- IO.now(2)
      queue    <- Queue.sliding[Int](capacity)
      iter     = Range.inclusive(1, 4)
      _        <- queue.take.fork
      _        <- waitForSize(queue, -1)
      oa       <- queue.offerAll(iter.toList)
      t        <- queue.take
    } yield (t must_=== 3).and(oa must beFalse)
  )

  def e64 = unsafeRun(
    for {
      capacity <- IO.now(5)
      queue    <- Queue.sliding[Int](capacity)
      iter     = Range.inclusive(1, 3)
      oa       <- queue.offerAll(iter.toList)
    } yield oa must beTrue
  )

  def e65 = unsafeRun(
    for {
      capacity <- IO.now(5)
      queue    <- Queue.bounded[Int](capacity)
      iter     = Range.inclusive(1, 3)
      oa       <- queue.offerAll(iter.toList)
    } yield oa must beTrue
  )

}

object QueueSpec {

  def waitForSize[A](queue: Queue[A], size: Int): IO[Nothing, Int] =
    (queue.size <* IO.sleep(10.millis)).repeat(Schedule.doWhile(_ != size))

}
