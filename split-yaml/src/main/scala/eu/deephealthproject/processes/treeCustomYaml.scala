package eu.deephealthproject.processes

import eu.deephealthproject.processes.mainSplitYaml.LOG
import net.jcazevedo.moultingyaml._

//object customYaml extends DefaultYamlProtocol {
//  implicit val imagenPathFormat = yamlFormat2(ImagePath)
//  implicit val splitFormat = yamlFormat3(Split)
//  implicit val datasetFormat = yamlFormat4(DataSet)
//}

object treeCustomYaml extends DefaultYamlProtocol {

  //  def snakeYamlObject: Object
  def matchYamlValue(value: YamlValue): String = value match {
    case mapValueYamlString: YamlString => mapValueYamlString.value
    case _ => ""
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
        println(s"[DeepHealth] YamlFormat write '${obj.toString}'")
        LOG.info(s"[DeepHealth] YamlFormat write '${obj.toString}'")
        deserializationError(s"[DeepHealth] YamlFormat write '${obj.toString}'")

    }

    def read(value: YamlValue): ImagePath = value match {

      case YamlObject(fields) // contains location
        if fields.contains(YamlString("location")) && fields.size == 1 =>
        ImagePath(
          matchYamlValue(fields(YamlString("location"))),
          null,
          null
        )

      case YamlObject(fields) // contains location & label (optional) & values (optional)
        if fields.contains(YamlString("location")) && fields.contains(YamlString("label")) &&
          fields.contains(YamlString("values")) =>
        ImagePath(
          matchYamlValue(fields(YamlString("location"))),
          matchYamlValue(fields(YamlString("label"))),
          matchYamlValue(fields(YamlString("values")))
        )

      case YamlObject(fields) // contains location & label (optional)
        if fields.contains(YamlString("location")) && fields.contains(YamlString("label")) && fields.size == 2 =>
        ImagePath(
          matchYamlValue(fields(YamlString("location"))),
          matchYamlValue(fields(YamlString("label"))),
          null
        )

      case YamlObject(fields) // contains location & values (optional)
        if fields.contains(YamlString("location")) && fields.contains(YamlString("values")) && fields.size == 2 =>
        ImagePath(
          matchYamlValue(fields(YamlString("location"))),
          null,
          matchYamlValue(fields(YamlString("values")))
        )

      case _ =>
        println(s"[DeepHealth] DeserializationError '${value.prettyPrint}'")
        LOG.info(s"[DeepHealth] DeserializationError '${value.prettyPrint}'")
        deserializationError(s"[DeepHealth] DeserializationError '${value.prettyPrint}'")

    }
  }

  implicit val splitFormat = yamlFormat3(Split)
  implicit val datasetFormat = yamlFormat4(DataSet)

}