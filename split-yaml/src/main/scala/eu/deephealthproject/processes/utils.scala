package eu.deephealthproject.processes

import org.slf4j.Logger

import scala.util.{Failure, Success, Try}

object utils {

  def checkArgument[A](argumentString: A)(implicit LOG: Logger): Try[A] =
    Try(argumentString) match {
      case Success(value) => Success(value)
      case Failure(exception) => LOG.error(s"[DeepHealth] Argument invalid: '$argumentString'")
        Failure(exception)

    }
}
