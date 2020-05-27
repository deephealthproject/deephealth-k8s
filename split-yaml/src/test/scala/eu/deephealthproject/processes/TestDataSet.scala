package eu.deephealthproject.processes

import net.jcazevedo.moultingyaml._
import org.scalatest.FlatSpec

import scala.io.Source

class TestDataSet extends FlatSpec {

  type TupleDS = ((Seq[ImagePath], Split), Int)

  import treeCustomYaml._

  val datasetEmpty: DataSet = DataSet.apply(
    "empty", "", Seq(ImagePath("", "", null)), Split(Seq(-1), Seq(-1), Seq(-1))
  )

  "DeepHealth Project: Split DataSet Format" should "Load Dataset from YAML file" in {

    val filename: String = "src/test/resources/isic_segmentation_small.yml"

    val dataset: DataSet = Source.fromFile(filename)
      .mkString
      .stripMargin
      .parseYaml
      .convertTo[DataSet]

    assert(dataset.name == "ISIC")
    assert(dataset.description == "ISIC segmentation dataset")
    assert(dataset.images.head.location == "images_segmentation/ISIC_0014480.jpg")
    assert(dataset.images.head.label == "ground_truth/ISIC_0014480_segmentation.png")

  }


  it should "Split Dataset" in {

    val datasetResult: DataSet = Source.fromFile("src/test/resources/isic_segmentation_small.yml")
      .mkString
      .stripMargin
      .parseYaml
      .convertTo[DataSet]

    val datasetPartZero: DataSet = Source.fromFile("src/test/resources/part-000-isic_segmentation.yml")
      .mkString
      .stripMargin
      .parseYaml
      .convertTo[DataSet]

    val datasetPartOne: DataSet = Source.fromFile("src/test/resources/part-001-isic_segmentation.yml")
      .mkString
      .stripMargin
      .parseYaml
      .convertTo[DataSet]

    val datasetSplit: Seq[DataSet] = datasetResult.splitDataSet(2)
      .map { elem =>
        DataSet.apply(
          s"${datasetResult.name}-part-${elem._2}",
          datasetResult.description,
          elem._1._1,
          elem._1._2
        )

      }

    assert(datasetSplit.head.images == datasetPartZero.images)
    assert(datasetSplit.last.images == datasetPartOne.images)

    assert(datasetSplit.head.split == datasetPartZero.split)
    assert(datasetSplit.last.split == datasetPartOne.split)

  }

}
