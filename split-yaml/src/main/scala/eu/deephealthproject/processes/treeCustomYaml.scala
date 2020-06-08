package eu.deephealthproject.processes

import eu.deephealthproject.processes.mainSplitYaml.LOG
import net.jcazevedo.moultingyaml._


object treeCustomYaml extends DefaultYamlProtocol {

  //  def snakeYamlObject: Object
  def matchYamlValue2String(value: YamlValue): String = value match {
    case mapValueYamlString: YamlString => mapValueYamlString.value
    case _ => ""
  }

  def matchYamlValue2ImagePath(value: YamlValue): Seq[ImagePath] = value match {
    case value: YamlArray => value.elements.map(
      (elem: YamlValue) => YamlImagePathFormat.read(elem)
    )
    case _ => Seq(ImagePath.apply(""))
  }

  def matchYamlValue2Split(value: YamlValue): Split = value match {
    case value: YamlObject => splitFormat.read(value)
    case _ => Split(Seq(-1), Seq(-1), Seq(-1))
  }

  implicit object YamlImagePathFormat extends YamlFormat[ImagePath] {

    def write(obj: ImagePath): YamlObject = obj match {

      case obj if obj.label == null && obj.values == null =>
        YamlObject.apply(
          (YamlString("location"), YamlString(obj.location))
        )

      case obj if obj.label == null =>
        YamlObject.apply(
          (YamlString("location"), YamlString(obj.location)),
          (YamlString("values"), YamlString(obj.values))
        )

      case obj if obj.values == null =>
        YamlObject.apply(
          (YamlString("location"), YamlString(obj.location)),
          (YamlString("label"), YamlString(obj.label))
        )

      case _ =>
        println(s"[DeepHealth] Image's YamlFormat write '${obj.toString}'")
        LOG.info(s"[DeepHealth] Image's YamlFormat write '${obj.toString}'")
        deserializationError(s"[DeepHealth] Image's YamlFormat write '${obj.toString}'")

    }

    def read(value: YamlValue): ImagePath = value match {

      case YamlObject(fields) // contains location
        if fields.contains(YamlString("location")) && fields.size == 1 =>
        ImagePath(
          matchYamlValue2String(fields(YamlString("location"))),
          null,
          null
        )

      case YamlObject(fields) // contains location & label (optional) & values (optional)
        if fields.contains(YamlString("location")) && fields.contains(YamlString("label")) &&
          fields.contains(YamlString("values")) =>
        ImagePath(
          matchYamlValue2String(fields(YamlString("location"))),
          matchYamlValue2String(fields(YamlString("label"))),
          matchYamlValue2String(fields(YamlString("values")))
        )

      case YamlObject(fields) // contains location & label (optional)
        if fields.contains(YamlString("location")) && fields.contains(YamlString("label")) && fields.size == 2 =>
        ImagePath(
          matchYamlValue2String(fields(YamlString("location"))),
          matchYamlValue2String(fields(YamlString("label"))),
          null
        )

      case YamlObject(fields) // contains location & values (optional)
        if fields.contains(YamlString("location")) && fields.contains(YamlString("values")) && fields.size == 2 =>
        ImagePath(
          matchYamlValue2String(fields(YamlString("location"))),
          null,
          matchYamlValue2String(fields(YamlString("values")))
        )

      case _ =>
        println(s"[DeepHealth] Image's DeserializationError '${value.prettyPrint}'")
        LOG.info(s"[DeepHealth] Image's DeserializationError '${value.prettyPrint}'")
        deserializationError(s"[DeepHealth] Image's DeserializationError '${value.prettyPrint}'")

    }

  }

  implicit val splitFormat = yamlFormat3(Split)

  implicit object YamlDataSetFormat extends YamlFormat[DataSet] {

    def imagesConvert2YamlArray(images: Seq[ImagePath]): YamlArray = {
      YamlArray.apply(images.map(YamlImagePathFormat.write): _*)
    }

    def write(obj: DataSet): YamlObject = obj match {

      // Cases with three null
      case obj if obj.description == null && obj.classes == null && obj.features == null =>
        YamlObject.apply(
          (YamlString("name"), YamlString(obj.name)),
          (YamlString("images"), imagesConvert2YamlArray(obj.images)),
          (YamlString("split"), splitFormat.write(obj.split))
        )

      case obj if obj.name == null && obj.classes == null && obj.features == null =>
        YamlObject.apply(
          (YamlString("description"), YamlString(obj.description)),
          (YamlString("images"), imagesConvert2YamlArray(obj.images)),
          (YamlString("split"), splitFormat.write(obj.split))
        )

      case obj if obj.name == null && obj.description == null && obj.features == null =>
        YamlObject.apply(
          (YamlString("classes"), YamlString(obj.classes)),
          (YamlString("images"), imagesConvert2YamlArray(obj.images)),
          (YamlString("split"), splitFormat.write(obj.split))
        )

      case obj if obj.name == null && obj.description == null && obj.classes == null =>
        YamlObject.apply(
          (YamlString("features"), YamlString(obj.features)),
          (YamlString("images"), imagesConvert2YamlArray(obj.images)),
          (YamlString("split"), splitFormat.write(obj.split))
        )

      // Cases with two null
      case obj if obj.name == null && obj.description == null =>
        YamlObject.apply(
          (YamlString("classes"), YamlString(obj.classes)),
          (YamlString("features"), YamlString(obj.features)),
          (YamlString("images"), imagesConvert2YamlArray(obj.images)),
          (YamlString("split"), splitFormat.write(obj.split))
        )

      case obj if obj.name == null && obj.classes == null =>
        YamlObject.apply(
          (YamlString("description"), YamlString(obj.description)),
          (YamlString("features"), YamlString(obj.features)),
          (YamlString("images"), imagesConvert2YamlArray(obj.images)),
          (YamlString("split"), splitFormat.write(obj.split))
        )

      case obj if obj.name == null && obj.features == null =>
        YamlObject.apply(
          (YamlString("description"), YamlString(obj.description)),
          (YamlString("classes"), YamlString(obj.classes)),
          (YamlString("images"), imagesConvert2YamlArray(obj.images)),
          (YamlString("split"), splitFormat.write(obj.split))
        )

      case obj if obj.description == null && obj.classes == null =>
        YamlObject.apply(
          (YamlString("name"), YamlString(obj.name)),
          (YamlString("features"), YamlString(obj.features)),
          (YamlString("images"), imagesConvert2YamlArray(obj.images)),
          (YamlString("split"), splitFormat.write(obj.split))
        )

      case obj if obj.description == null && obj.features == null =>
        YamlObject.apply(
          (YamlString("name"), YamlString(obj.name)),
          (YamlString("classes"), YamlString(obj.classes)),
          (YamlString("images"), imagesConvert2YamlArray(obj.images)),
          (YamlString("split"), splitFormat.write(obj.split))
        )

      case obj if obj.classes == null && obj.features == null =>
        YamlObject.apply(
          (YamlString("name"), YamlString(obj.name)),
          (YamlString("description"), YamlString(obj.description)),
          (YamlString("images"), imagesConvert2YamlArray(obj.images)),
          (YamlString("split"), splitFormat.write(obj.split))
        )

      // Cases with one null
      case obj if obj.name == null =>
        YamlObject.apply(
          (YamlString("description"), YamlString(obj.description)),
          (YamlString("classes"), YamlString(obj.classes)),
          (YamlString("features"), YamlString(obj.features)),
          (YamlString("images"), imagesConvert2YamlArray(obj.images)),
          (YamlString("split"), splitFormat.write(obj.split))
        )

      case obj if obj.description == null =>
        YamlObject.apply(
          (YamlString("name"), YamlString(obj.name)),
          (YamlString("classes"), YamlString(obj.classes)),
          (YamlString("features"), YamlString(obj.features)),
          (YamlString("images"), imagesConvert2YamlArray(obj.images)),
          (YamlString("split"), splitFormat.write(obj.split))
        )

      case obj if obj.classes == null =>
        YamlObject.apply(
          (YamlString("name"), YamlString(obj.name)),
          (YamlString("description"), YamlString(obj.description)),
          (YamlString("features"), YamlString(obj.features)),
          (YamlString("images"), imagesConvert2YamlArray(obj.images)),
          (YamlString("split"), splitFormat.write(obj.split))
        )

      case obj if obj.features == null =>
        YamlObject.apply(
          (YamlString("name"), YamlString(obj.name)),
          (YamlString("description"), YamlString(obj.description)),
          (YamlString("classes"), YamlString(obj.classes)),
          (YamlString("images"), imagesConvert2YamlArray(obj.images)),
          (YamlString("split"), splitFormat.write(obj.split))
        )

      case _ =>
        println(s"[DeepHealth] YamlFormat write '${obj.toString}'")
        LOG.info(s"[DeepHealth] YamlFormat write '${obj.toString}'")
        deserializationError(s"[DeepHealth] YamlFormat write '${obj.toString}'")

    }

    def read(value: YamlValue): DataSet = value match {

      case YamlObject(fields) // contains location & values (optional)
        if fields.contains(YamlString("name")) &&
          fields.contains(YamlString("description")) &&
          fields.contains(YamlString("classes")) &&
          fields.contains(YamlString("features")) &&
          fields.size == 6 =>
        DataSet(
          matchYamlValue2String(fields(YamlString("name"))),
          matchYamlValue2String(fields(YamlString("description"))),
          matchYamlValue2String(fields(YamlString("classes"))),
          matchYamlValue2String(fields(YamlString("features"))),
          matchYamlValue2ImagePath(fields(YamlString("images"))),
          matchYamlValue2Split(fields(YamlString("split")))
        )

      case YamlObject(fields) // contains location & values (optional)
        if fields.contains(YamlString("name")) &&
          fields.contains(YamlString("classes")) &&
          fields.contains(YamlString("features")) &&
          fields.size == 5 =>
        DataSet(
          matchYamlValue2String(fields(YamlString("name"))),
          null,
          matchYamlValue2String(fields(YamlString("classes"))),
          matchYamlValue2String(fields(YamlString("features"))),
          matchYamlValue2ImagePath(fields(YamlString("images"))),
          matchYamlValue2Split(fields(YamlString("split")))
        )

      case YamlObject(fields) // contains location & values (optional)
        if fields.contains(YamlString("name")) &&
          fields.contains(YamlString("description")) &&
          fields.contains(YamlString("features")) &&
          fields.size == 5 =>
        DataSet(
          matchYamlValue2String(fields(YamlString("name"))),
          matchYamlValue2String(fields(YamlString("description"))),
          null,
          matchYamlValue2String(fields(YamlString("features"))),
          matchYamlValue2ImagePath(fields(YamlString("images"))),
          matchYamlValue2Split(fields(YamlString("split")))
        )

      case YamlObject(fields) // contains location & values (optional)
        if fields.contains(YamlString("name")) &&
          fields.contains(YamlString("description")) &&
          fields.contains(YamlString("classes")) &&
          fields.size == 5 =>
        DataSet(
          matchYamlValue2String(fields(YamlString("name"))),
          matchYamlValue2String(fields(YamlString("description"))),
          matchYamlValue2String(fields(YamlString("classes"))),
          null,
          matchYamlValue2ImagePath(fields(YamlString("images"))),
          matchYamlValue2Split(fields(YamlString("split")))
        )

      case YamlObject(fields) // contains location & values (optional)
        if fields.contains(YamlString("name")) &&
          fields.contains(YamlString("description")) &&
          fields.size == 4 =>
        DataSet(
          matchYamlValue2String(fields(YamlString("name"))),
          matchYamlValue2String(fields(YamlString("description"))),
          null,
          null,
          matchYamlValue2ImagePath(fields(YamlString("images"))),
          matchYamlValue2Split(fields(YamlString("split")))
        )

      case YamlObject(fields) // contains location & values (optional)
        if fields.contains(YamlString("name")) &&
          fields.contains(YamlString("classes")) &&
          fields.size == 4 =>
        DataSet(
          matchYamlValue2String(fields(YamlString("name"))),
          null,
          matchYamlValue2String(fields(YamlString("classes"))),
          null,
          matchYamlValue2ImagePath(fields(YamlString("images"))),
          matchYamlValue2Split(fields(YamlString("split")))
        )

      case YamlObject(fields) // contains location & values (optional)
        if fields.contains(YamlString("name")) &&
          fields.contains(YamlString("features")) &&
          fields.size == 4 =>
        DataSet(
          matchYamlValue2String(fields(YamlString("name"))),
          null,
          null,
          matchYamlValue2String(fields(YamlString("features"))),
          matchYamlValue2ImagePath(fields(YamlString("images"))),
          matchYamlValue2Split(fields(YamlString("split")))
        )

      case YamlObject(fields) // contains location & values (optional)
        if fields.contains(YamlString("description")) &&
          fields.contains(YamlString("classes")) &&
          fields.size == 4 =>
        DataSet(
          null,
          matchYamlValue2String(fields(YamlString("description"))),
          matchYamlValue2String(fields(YamlString("classes"))),
          null,
          matchYamlValue2ImagePath(fields(YamlString("images"))),
          matchYamlValue2Split(fields(YamlString("split")))
        )

      case YamlObject(fields) // contains location & values (optional)
        if fields.contains(YamlString("description")) &&
          fields.contains(YamlString("features")) &&
          fields.size == 4 =>
        DataSet(
          null,
          matchYamlValue2String(fields(YamlString("description"))),
          null,
          matchYamlValue2String(fields(YamlString("features"))),
          matchYamlValue2ImagePath(fields(YamlString("images"))),
          matchYamlValue2Split(fields(YamlString("split")))
        )

      case YamlObject(fields) // contains location & values (optional)
        if fields.contains(YamlString("classes")) &&
          fields.contains(YamlString("features")) &&
          fields.size == 4 =>
        DataSet(
          null,
          null,
          matchYamlValue2String(fields(YamlString("classes"))),
          matchYamlValue2String(fields(YamlString("features"))),
          matchYamlValue2ImagePath(fields(YamlString("images"))),
          matchYamlValue2Split(fields(YamlString("split")))
        )

      case YamlObject(fields) // contains location & values (optional)
        if fields.contains(YamlString("name")) &&
          fields.size == 3 =>
        DataSet(
          matchYamlValue2String(fields(YamlString("name"))),
          null,
          null,
          null,
          matchYamlValue2ImagePath(fields(YamlString("images"))),
          matchYamlValue2Split(fields(YamlString("split")))
        )

      case YamlObject(fields) // contains location & values (optional)
        if fields.contains(YamlString("description")) &&
          fields.size == 3 =>
        DataSet(
          null,
          matchYamlValue2String(fields(YamlString("description"))),
          null,
          null,
          matchYamlValue2ImagePath(fields(YamlString("images"))),
          matchYamlValue2Split(fields(YamlString("split")))
        )

      case YamlObject(fields) // contains location & values (optional)
        if fields.contains(YamlString("classes")) &&
          fields.size == 3 =>
        DataSet(
          null,
          null,
          matchYamlValue2String(fields(YamlString("classes"))),
          null,
          matchYamlValue2ImagePath(fields(YamlString("images"))),
          matchYamlValue2Split(fields(YamlString("split")))
        )

      case YamlObject(fields) // contains location & values (optional)
        if fields.contains(YamlString("features")) &&
          fields.size == 3 =>
        DataSet(
          null,
          null,
          null,
          matchYamlValue2String(fields(YamlString("features"))),
          matchYamlValue2ImagePath(fields(YamlString("images"))),
          matchYamlValue2Split(fields(YamlString("split")))
        )

      case YamlObject(fields) // contains location & values (optional)
        if fields.size == 2 =>
        DataSet(
          null,
          null,
          null,
          null,
          matchYamlValue2ImagePath(fields(YamlString("images"))),
          matchYamlValue2Split(fields(YamlString("split")))
        )

      case _ =>
        println(s"[DeepHealth] DeserializationError '${value.prettyPrint}'")
        LOG.info(s"[DeepHealth] DeserializationError '${value.prettyPrint}'")
        deserializationError(s"[DeepHealth] DeserializationError '${value.prettyPrint}'")

    }

  }

}