package eu.deephealthproject.processes

case class ImagePath(location: String, label: String)

case class Split(training: Seq[Int], validation: Seq[Int], test: Seq[Int])

case class DataSet(name: String, description: String, images: Seq[ImagePath], split: Split) {

  type TupleDS = ((Seq[ImagePath], Split), Int)

  def apply(name: String, description: String, images: Seq[ImagePath], split: Split): DataSet =
    DataSet(name, description, images, split)

  override def toString: String = s"name: $name," +
    s"\ndescription: $description,\nfirst images: {\n\t${images.head.location},\n\t${images.head.label}\n}\nsplit:{" +
    s"\n\ttrain: ${split.training.length},\n\tvalidation: ${split.validation.length},\n\ttest: ${split.test.length}\n}"

  private def sizeXperiment[A](set: Seq[A], long: Int): Int = if (set.length <= long) {
    1
  } else {
    Math.round(set.length / long)
  }

  private def calculatePercentage(sets: Seq[Int]*): Seq[Double] = {

    //    val total = sets.reduce(_.length + _.length) toDouble
    val total = sets map (_.length) sum

    sets map (_.length.toDouble / total)

  }

  private def combineSplitDataSet(t1: TupleDS, t2: TupleDS): TupleDS = {

    val seqImagePath: Seq[ImagePath] = t1._1._1 ++ t2._1._1

    val splitCombine: Split = {
      val split1: Split = t1._1._2
      val split2: Split = t2._1._2

      Split(
        split1.training ++ split2.training,
        split1.validation ++ split2.validation,
        split1.test ++ split2.test
      )
    }

    val partitionNumber: Int = Math.min(t1._2, t2._2)

    ((seqImagePath, splitCombine), partitionNumber)

  }

  def splitDataSet(splitNumber: Int): Seq[TupleDS] = {

    // Divide the set of images into groups according to the input
    val imagesSet: Seq[Seq[ImagePath]] = images grouped sizeXperiment(images, splitNumber) toSeq
    val imagesSetLengths: Seq[(Seq[ImagePath], Int)] = imagesSet zip imagesSet.map(_.length)

    // calculate the training, validation and test percentage of the original YAML file
    val Seq(pTraining: Double, pValidation: Double, _) = calculatePercentage(split.training, split.validation, split.test)

    //
    val seqSplitDS: Seq[TupleDS] = imagesSetLengths.
      map { tuple =>
        (tuple._1, {
          val n1 = (tuple._2 * pTraining) toInt
          val n2 = (n1 + tuple._2 * pValidation) toInt
          val n3 = tuple._2

          val elemSplitClass = Seq(0, n1, n2, n3).sliding(2, 1)
            .toList
            .map { list => Range(list.head, list.last).toList }

          Split.apply(elemSplitClass.head, elemSplitClass(1), elemSplitClass.last)

        })
      }.zipWithIndex

    seqSplitDS.length match {
      case length if length > splitNumber =>

        val lastElement = seqSplitDS.last
        val lastLastElement = seqSplitDS.init.last
        seqSplitDS.init.init :+ combineSplitDataSet(lastLastElement, lastElement)

      case _ => seqSplitDS

    }
  }

}