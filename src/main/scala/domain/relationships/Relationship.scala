package domain.relationships

import domain.WithId

trait Many[M <: WithId[ID], ID] {
  def childrenIds: List[ID]
}

trait One[O <: WithId[ID], ID] {
  def parentId: ID
}
