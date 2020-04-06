package eu.deephealthproject.processes

import net.jcazevedo.moultingyaml.DefaultYamlProtocol

object customYaml extends DefaultYamlProtocol {
  implicit val imagenPathFormat = yamlFormat2(ImagePath)
  implicit val splitFormat = yamlFormat3(Split)
  implicit val datasetFormat = yamlFormat4(DataSet)
}
