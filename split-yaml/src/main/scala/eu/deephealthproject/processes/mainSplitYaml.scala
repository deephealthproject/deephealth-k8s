package eu.deephealthproject.processes

import java.io.{BufferedWriter, File, FileWriter}

import eu.deephealthproject.processes.utils._
import net.jcazevedo.moultingyaml._
import org.slf4j.{Logger, LoggerFactory}

import scala.io.Source
import scala.util.Try

object mainSplitYaml {

  import treeCustomYaml._

  implicit val LOG: Logger = LoggerFactory.getLogger(this.getClass) /**/

  def main(args: Array[String]): Unit = {

    val conditions: Seq[Try[Any]] = Seq(
      checkArgument(args.length == 3), checkArgument(args.head), checkArgument(args(1)), checkArgument(args(2))
    )

    if (conditions.map(_.isSuccess).reduce(_ && _)) {

      val filename: String = conditions(1).get.toString
      val splitNumber: Int = conditions(2).get.toString.toInt
      val outputDir: String = conditions(3).get.toString

      println(s"[DeepHealth] Start split-yaml process '$filename'")
      LOG.info(s"[DeepHealth] Start split-yaml process '$filename''")

      val datasetYaml: String = Source.fromFile(filename)
        .mkString
        .stripMargin

      val dataset: DataSet = datasetYaml.parseYaml.convertTo[DataSet]

      val splitds: Seq[((Seq[ImagePath], Split), Int)] = dataset.splitDataSet(splitNumber)

      val splitdsToDataSet: Seq[YamlValue] = splitds.map { elem =>
        val datasetName = dataset.name match {
          case name if name != null => s"${dataset.name}-"
          case _ => ""
        }
        DataSet.apply(
          s"{$datasetName}part-${elem._2}",
          dataset.description,
          dataset.classes,
          dataset.features,
          elem._1._1,
          elem._1._2
        ).toYaml
      }

      // FileWriter
      val outputFileName = filename split "/" last

      val partFileName = splitdsToDataSet.indices map {
        index => s"$outputDir/part-${"%03d".format(index)}-$outputFileName"
      }

      (splitdsToDataSet.map(_.prettyPrint).toList zip partFileName) foreach {
        tuple =>

          println(s"[DeepHealth] Write '${tuple._2}' file")
          LOG.info(s"[DeepHealth] Write '${tuple._2}' file")

          val file = new File(tuple._2)
          val writer = new BufferedWriter(new FileWriter(file))
          writer.write(tuple._1)
          writer.close()

      }

      println(s"[DeepHealth] Finish split-yaml process. Check the directory '$outputDir'")
      LOG.info(s"[DeepHealth] Finish split-yaml process. Check the directory '$outputDir'")

    }
  }

}
